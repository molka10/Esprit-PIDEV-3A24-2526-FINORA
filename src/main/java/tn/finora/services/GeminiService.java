package tn.finora.services;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GeminiService {

    // ✅ Paste your Gemini API key here
    private static final String API_KEY = "PASTE_YOUR_KEY_HERE";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" +
                    "gemini-2.0-flash:generateContent?key=" + API_KEY;

    /**
     * Generates 5 MCQ questions from lesson content.
     * Returns a list of QuizQuestion objects.
     */
    public List<QuizQuestion> generateQuiz(String lessonTitle, String lessonContent) throws Exception {

        String prompt = """
            Tu es un professeur expert. Génère exactement 5 questions à choix multiples
            basées sur ce contenu de leçon.

            Titre de la leçon: %s
            Contenu: %s

            Réponds UNIQUEMENT avec un JSON valide, sans texte avant ou après.
            Format exact:
            {
              "questions": [
                {
                  "question": "La question ici?",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correct": 0
                }
              ]
            }

            - "correct" est l'index (0-3) de la bonne réponse dans "options"
            - 5 questions obligatoires
            - Questions claires et précises basées sur le contenu fourni
            """.formatted(lessonTitle, lessonContent);

        // Build request body
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Make HTTP POST request
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line);
        }

        return parseQuizResponse(response.toString());
    }

    private List<QuizQuestion> parseQuizResponse(String rawResponse) throws Exception {
        // Extract JSON from Gemini response
        JsonObject root = JsonParser.parseString(rawResponse).getAsJsonObject();
        String text = root
                .getAsJsonArray("candidates").get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts").get(0).getAsJsonObject()
                .get("text").getAsString();

        // Clean up markdown code blocks if present
        text = text.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonObject quizJson = JsonParser.parseString(text).getAsJsonObject();
        JsonArray questionsArray = quizJson.getAsJsonArray("questions");

        List<QuizQuestion> questions = new ArrayList<>();
        for (JsonElement el : questionsArray) {
            JsonObject q = el.getAsJsonObject();
            String question = q.get("question").getAsString();
            int correct = q.get("correct").getAsInt();

            List<String> options = new ArrayList<>();
            for (JsonElement opt : q.getAsJsonArray("options")) {
                options.add(opt.getAsString());
            }

            questions.add(new QuizQuestion(question, options, correct));
        }

        return questions;
    }

    // ── Inner model class ─────────────────────────────────────────
    public static class QuizQuestion {
        public final String question;
        public final List<String> options;
        public final int correctIndex;

        public QuizQuestion(String question, List<String> options, int correctIndex) {
            this.question     = question;
            this.options      = options;
            this.correctIndex = correctIndex;
        }
    }
}