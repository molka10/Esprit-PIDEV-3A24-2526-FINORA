/*package tn.finora.services;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GeminiService {

    private static String apiKey() {
        String k = System.getenv("GEMINI_API_KEY");
        if (k == null || k.isBlank()) k = System.getProperty("GEMINI_API_KEY");
        if (k == null || k.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key missing. Set env GEMINI_API_KEY or JVM -DGEMINI_API_KEY=..."
            );
        }
        System.out.println("GEMINI KEY loaded, last4=" + k.trim().substring(k.trim().length()-4));
        return k.trim();
    }

    private static String apiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" +
                "gemini-2.0-flash:generateContent?key=" + apiKey();
    }

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
            """.formatted(lessonTitle, lessonContent);

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

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl()).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        // handle non-200 safely
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line);
        }

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Gemini error HTTP " + code + ": " + response);
        }

        return parseQuizResponse(response.toString());
    }

    private List<QuizQuestion> parseQuizResponse(String rawResponse) throws Exception {
        JsonObject root = JsonParser.parseString(rawResponse).getAsJsonObject();
        String text = root
                .getAsJsonArray("candidates").get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts").get(0).getAsJsonObject()
                .get("text").getAsString();

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
}*/
package tn.finora.services;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GeminiService {

    // --- CONFIGURATION ---
    // Récupère la clé API. Assurez-vous d'avoir la variable d'environnement GROQ_API_KEY
    private static String apiKey() {
        String k = System.getenv("GROQ_API_KEY");
        if (k == null || k.isBlank()) k = System.getProperty("GROQ_API_KEY");
        if (k == null || k.isBlank()) {
            throw new IllegalStateException("Clé API manquante. Définissez GROQ_API_KEY (obtenue gratuitement sur console.groq.com)");
        }
        return k.trim();
    }

    public List<QuizQuestion> generateQuiz(String lessonTitle, String lessonContent) throws Exception {

        // Prompt strict pour forcer le JSON
        String prompt = String.format("""
            Tu es un générateur de quiz automatique.
            À partir du titre: '%s' et du contenu: '%s'.
            
            RÈGLES STRICTES:
            1. Génère exactement 5 questions QCM.
            2. Réponds UNIQUEMENT avec le JSON.
            3. NE RAJOUTE AUCUN TEXTE AVANT OU APRÈS.
            4. Format attendu: {"questions": [{"question": "...", "options": ["A","B","C","D"], "correct": 0}]}
            """, lessonTitle, lessonContent);

        // Construction de la requête pour Groq (Format OpenAI)
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "llama-3.1-8b-instant"); // Modèle gratuit et rapide

        JsonArray messages = new JsonArray();
        JsonObject msg = new JsonObject();
        msg.addProperty("role", "user");
        msg.addProperty("content", prompt);
        messages.add(msg);
        requestBody.add("messages", messages);

        // Appel HTTP
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey());
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        // Envoi des données
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        // Gestion de la réponse
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line);
        }

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Erreur API (" + code + "): " + response);
        }

        // Lecture du contenu renvoyé par l'IA
        JsonObject root = JsonParser.parseString(response.toString()).getAsJsonObject();
        String text = root.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString();

        // Nettoyage et Parsing sécurisé
        return parseCleanJson(text);
    }

    // --- MÉTHODES DE PARSING SÉCURISÉ ---

    private List<QuizQuestion> parseCleanJson(String text) throws Exception {

        text = text.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        String cleanJson = extractJson(text);

        JsonObject quizJson = JsonParser.parseString(cleanJson).getAsJsonObject();
        JsonArray questionsArray = quizJson.getAsJsonArray("questions");

        List<QuizQuestion> questions = new ArrayList<>();

        if (questionsArray == null) {
            throw new RuntimeException("Réponse IA invalide : pas de 'questions'");
        }

        for (JsonElement el : questionsArray) {

            JsonObject q = el.getAsJsonObject();

            // ---- QUESTION TEXT SAFE ----
            String questionText = q.has("question")
                    ? q.get("question").getAsString()
                    : "Question invalide";

            // ---- OPTIONS SAFE ----
            List<String> options = new ArrayList<>();

            if (q.has("options") && q.get("options").isJsonArray()) {
                for (JsonElement opt : q.getAsJsonArray("options")) {
                    options.add(opt.getAsString());
                }
            }

            // Ensure EXACTLY 4 options
            while (options.size() < 4) {
                options.add("Option manquante");
            }

            if (options.size() > 4) {
                options = options.subList(0, 4);
            }

            // ---- CORRECT INDEX SAFE ----
            int correct = 0;
            if (q.has("correct")) {
                try {
                    correct = q.get("correct").getAsInt();
                } catch (Exception ignored) {}
            }

            // Clamp correct index between 0 and 3
            if (correct < 0 || correct > 3) {
                correct = 0;
            }

            questions.add(new QuizQuestion(questionText, options, correct));
        }

        // Ensure at least 1 question
        if (questions.isEmpty()) {
            questions.add(new QuizQuestion(
                    "Aucune question générée",
                    List.of("Option A", "Option B", "Option C", "Option D"),
                    0
            ));
        }

        return questions;
    }
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }

        throw new RuntimeException("JSON invalide reçu de l'IA");
    }

    // --- CLASSE INTERNE QUIZ QUESTION ---
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
//