package com.example.finora.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizAiCommentService {

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private static String apiKey() {
        String k = System.getenv("GROQ_API_KEY");
        if (k == null || k.isBlank()) k = System.getProperty("GROQ_API_KEY");
        if (k == null || k.isBlank()) {
            throw new IllegalStateException("Missing GROQ_API_KEY (env or -DGROQ_API_KEY=...)");
        }
        return k.trim();
    }

    public String analyzeFraud(String fraudReport) {
        String report = (fraudReport == null) ? "" : fraudReport.trim();

        try {
            String prompt = """
                You are an academic integrity examiner.

                TASK:
                Explain clearly WHY the quiz was considered suspicious, using ONLY the report.
                You MUST explicitly name the causes that appear in the report:
                - focus losses
                - fullscreen exits
                - fast answers
                - fraud score

                OUTPUT RULES:
                - 1 to 3 short sentences maximum
                - Must include the numeric values from the report (e.g., focus losses = 2)
                - No emojis
                - No vague text like "multiple reasons" without listing them

                Fraud report:
                %s
                """.formatted(report);

            JsonObject body = new JsonObject();
            body.addProperty("model", "llama-3.1-8b-instant");
            body.addProperty("temperature", 0.1);     // ✅ more deterministic
            body.addProperty("max_tokens", 110);      // ✅ short but complete

            JsonArray messages = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("role", "user");
            msg.addProperty("content", prompt);
            messages.add(msg);
            body.add("messages", messages);

            HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey());
            conn.setDoOutput(true);
            conn.setConnectTimeout(12000);
            conn.setReadTimeout(20000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder resp = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) resp.append(line);
            }

            if (code < 200 || code >= 300) {
                return fallback(report);
            }

            JsonObject root = JsonParser.parseString(resp.toString()).getAsJsonObject();
            String content = root.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString().trim();

            // ✅ If the AI response is vague, use fallback
            if (content.isBlank() || looksVague(content)) {
                return fallback(report);
            }

            return content;

        } catch (Exception e) {
            return fallback(report);
        }
    }

    private boolean looksVague(String text) {
        String t = text.toLowerCase();
        // typical vague phrases
        return t.contains("multiple") && !t.contains("focus")
                || t.contains("combination") && !t.contains("focus")
                || t.contains("suspicious behavior") && !(t.contains("focus") || t.contains("fullscreen") || t.contains("fast"));
    }

    // ✅ Local explanation if AI is unavailable / vague
    private String fallback(String report) {
        int focus = extractInt(report, "Focus losses:\\s*(\\d+)");
        int full = extractInt(report, "Fullscreen exits:\\s*(\\d+)");
        int fast = extractInt(report, "Fast answers:\\s*(\\d+)");
        int score = extractInt(report, "Score:\\s*(\\d+)");

        StringBuilder sb = new StringBuilder();

        sb.append("The quiz was flagged because ");
        boolean first = true;

        if (focus > 0) {
            sb.append("focus losses = ").append(focus);
            first = false;
        }
        if (full > 0) {
            if (!first) sb.append(", ");
            sb.append("fullscreen exits = ").append(full);
            first = false;
        }
        if (fast > 0) {
            if (!first) sb.append(", ");
            sb.append("fast answers = ").append(fast);
            first = false;
        }

        if (first) {
            sb.append("the fraud indicators increased");
        }

        if (score > 0) {
            sb.append(". Fraud score = ").append(score).append(".");
        } else {
            sb.append(".");
        }

        return sb.toString();
    }

    private int extractInt(String text, String regex) {
        if (text == null) return 0;
        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }
        return 0;
    }
}