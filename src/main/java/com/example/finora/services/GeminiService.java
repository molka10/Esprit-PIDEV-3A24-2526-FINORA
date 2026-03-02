package com.example.finora.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeminiService {

    // ---- GROQ CONFIG ----
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";

    // Keep prompts safe / fast
    private static final int MAX_TITLE_CHARS = 200;
    private static final int MAX_CONTENT_CHARS = 4000;

    private static String apiKey() {
        String k = System.getenv("GROQ_API_KEY");
        if (k == null || k.isBlank())
            k = System.getProperty("GROQ_API_KEY");
        if (k == null || k.isBlank()) {
            throw new IllegalStateException(
                    "Clé API manquante. Définissez GROQ_API_KEY (console.groq.com)");
        }
        return k.trim();
    }

    public List<QuizQuestion> generateQuiz(String lessonTitle, String lessonContent) throws Exception {

        String title = safe(lessonTitle);
        String content = safe(lessonContent);

        if (title.length() > MAX_TITLE_CHARS)
            title = title.substring(0, MAX_TITLE_CHARS);
        if (content.length() > MAX_CONTENT_CHARS)
            content = content.substring(0, MAX_CONTENT_CHARS);

        // ✅ Prompt stricter + safer (no giant quoted string)
        String prompt = """
                Tu es un professeur.
                Génère EXACTEMENT 5 questions QCM (4 choix) basées sur la leçon ci-dessous.

                RÈGLES STRICTES:
                - Réponds UNIQUEMENT avec un JSON valide, sans texte avant/après.
                - Format EXACT:
                {
                  "questions": [
                    {
                      "question": "...",
                      "options": ["A","B","C","D"],
                      "correct": 0
                    }
                  ]
                }
                - "correct" est l'index (0-3) de la bonne réponse.
                - 5 questions obligatoires.

                TITRE:
                %s

                CONTENU:
                %s
                """.formatted(title, content);

        // Build OpenAI-style request for Groq
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("temperature", 0.2);
        body.addProperty("max_tokens", 900); // ✅ prevents weird failures

        JsonArray messages = new JsonArray();

        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", "Tu réponds uniquement en JSON valide.");
        messages.add(system);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", prompt);
        messages.add(user);

        body.add("messages", messages);

        // ✅ Retry on transient server errors
        String responseText = sendWithRetries(body.toString(), 3);

        // Parse response
        JsonObject root = JsonParser.parseString(responseText).getAsJsonObject();
        String text = root.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        return parseCleanJson(text);
    }

    private String sendWithRetries(String jsonBody, int maxAttempts) throws Exception {
        Exception last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpURLConnection conn = null;

            try {
                conn = (HttpURLConnection) new URL(GROQ_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey());
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(45000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                String resp = readAll(is);

                if (code >= 200 && code < 300)
                    return resp;

                // retry on transient errors
                if (code == 500 || code == 502 || code == 503 || code == 504) {
                    last = new RuntimeException("Groq transient error (" + code + "): " + resp);
                    Thread.sleep(700L * attempt);
                    continue;
                }

                throw new RuntimeException("Erreur API (" + code + "): " + resp);

            } catch (Exception e) {
                last = e;
                // retry on IO-ish errors too
                Thread.sleep(700L * attempt);
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
        }

        throw last != null ? last : new RuntimeException("Groq failed (unknown).");
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null)
            return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
        }
        return sb.toString();
    }

    private List<QuizQuestion> parseCleanJson(String text) {

        text = safe(text)
                .replace("```json", "")
                .replace("```", "")
                .trim();

        String cleanJson;
        try {
            cleanJson = extractJson(text);
        } catch (Exception e) {
            return List.of(new QuizQuestion(
                    "Quiz generation failed. Please try again.",
                    List.of("Option A", "Option B", "Option C", "Option D"),
                    0));
        }

        JsonObject quizJson;
        try {
            quizJson = JsonParser.parseString(cleanJson).getAsJsonObject();
        } catch (Exception e) {
            return List.of(new QuizQuestion(
                    "AI returned invalid JSON. Try again.",
                    List.of("Option A", "Option B", "Option C", "Option D"),
                    0));
        }

        JsonArray questionsArray = quizJson.getAsJsonArray("questions");
        if (questionsArray == null || questionsArray.size() == 0) {
            return List.of(new QuizQuestion(
                    "No questions generated.",
                    List.of("Option A", "Option B", "Option C", "Option D"),
                    0));
        }

        List<QuizQuestion> questions = new ArrayList<>();

        for (JsonElement el : questionsArray) {
            JsonObject q = el.getAsJsonObject();

            String questionText = q.has("question") ? safe(q.get("question").getAsString()) : "Question invalide";

            List<String> options = new ArrayList<>();
            if (q.has("options") && q.get("options").isJsonArray()) {
                for (JsonElement opt : q.getAsJsonArray("options")) {
                    options.add(safe(opt.getAsString()));
                }
            }

            while (options.size() < 4)
                options.add("Option manquante");
            if (options.size() > 4)
                options = options.subList(0, 4);

            int correct = 0;
            if (q.has("correct")) {
                try {
                    correct = q.get("correct").getAsInt();
                } catch (Exception ignored) {
                }
            }
            if (correct < 0 || correct > 3)
                correct = 0;

            questions.add(new QuizQuestion(questionText, options, correct));
        }

        // ensure exactly 5 (teacher requirement)
        if (questions.size() > 5)
            questions = questions.subList(0, 5);
        while (questions.size() < 5) {
            questions.add(new QuizQuestion(
                    "Question manquante (réessaie).",
                    List.of("Option A", "Option B", "Option C", "Option D"),
                    0));
        }

        return questions;
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start)
            return text.substring(start, end + 1);
        throw new RuntimeException("JSON invalide reçu de l'IA");
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    public static class QuizQuestion {
        public final String question;
        public final List<String> options;
        public final int correctIndex;

        public QuizQuestion(String question, List<String> options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }
}