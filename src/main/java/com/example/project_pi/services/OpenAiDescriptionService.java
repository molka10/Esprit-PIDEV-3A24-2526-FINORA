package com.example.project_pi.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/**
 * Calls OpenAI Responses API to generate a professional Appel d'Offre description.
 * Endpoint: POST https://api.openai.com/v1/responses
 */
public class OpenAiDescriptionService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String generateDescription(
            String titre,
            String categorie,
            String type,
            double budgetMin,
            double budgetMax,
            String devise,
            String dateLimiteIso,
            String extraNotes
    ) throws IOException, InterruptedException {

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("OPENAI_API_KEY not set in environment variables.");
        }

        String model = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-5-mini");

        String prompt = """
                You are helping write a PROFESSIONAL tender (appel d'offre) description in French.
                Output ONLY the final description (no markdown, no title), 120-220 words.

                Context:
                - Titre: %s
                - Catégorie: %s
                - Type: %s
                - Budget min: %s
                - Budget max: %s
                - Devise: %s
                - Date limite: %s
                - Notes (optional): %s

                Requirements:
                - Include: objectives, scope, expected deliverables, evaluation criteria, timeline hints.
                - Sound formal and realistic for a university/business tender.
                """.formatted(
                safe(titre),
                safe(categorie),
                safe(type),
                fmtBudget(budgetMin),
                fmtBudget(budgetMax),
                safe(devise),
                safe(dateLimiteIso),
                safe(extraNotes)
        );

        // Minimal JSON for Responses API
        // docs: responses.create: model + input text :contentReference[oaicite:1]{index=1}
        String json = """
                {
                  "model": "%s",
                  "input": [
                    { "role": "user", "content": [ { "type": "input_text", "text": %s } ] }
                  ]
                }
                """.formatted(model, jsonString(prompt));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Bearer " + apiKey) // Bearer auth :contentReference[oaicite:2]{index=2}
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("OpenAI error " + res.statusCode() + ": " + res.body());
        }

        // Extract output_text (simple parse)
        // Response has "output_text" in many SDK examples; in raw JSON it appears as output_text.
        String body = res.body();
        String out = extractJsonStringField(body, "output_text");
        if (out == null || out.isBlank()) {
            // Fallback: try "text"
            out = extractFirstTextValue(body);
        }
        return (out == null || out.isBlank()) ? "Description indisponible (réponse vide)." : out.trim();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s.trim();
    }

    private String fmtBudget(double v) {
        return v <= 0 ? "-" : String.valueOf(v);
    }

    // --- tiny helpers (no extra libs) ---
    private String jsonString(String s) {
        if (s == null) s = "";
        String escaped = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }

    private String extractJsonStringField(String json, String field) {
        if (json == null) return null;
        String key = "\"" + field + "\"";
        int i = json.indexOf(key);
        if (i < 0) return null;
        int colon = json.indexOf(':', i);
        if (colon < 0) return null;
        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;

        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int p = firstQuote + 1; p < json.length(); p++) {
            char c = json.charAt(p);
            if (esc) {
                sb.append(c == 'n' ? '\n' : c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return null;
    }

    private String extractFirstTextValue(String json) {
        // naive fallback for nested "text":"..."
        return extractJsonStringField(json, "text");
    }
}