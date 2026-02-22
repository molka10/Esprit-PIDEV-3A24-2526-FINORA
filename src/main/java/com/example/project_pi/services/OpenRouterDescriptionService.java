package com.example.project_pi.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

/**
 * OpenRouter (OpenAI-compatible) - Free router model: "openrouter/free"
 * Docs: https://openrouter.ai/docs (OpenAI-compatible schema)
 */
public class OpenRouterDescriptionService {
    private static final String API_KEY = System.getenv("OPENROUTER_API_KEY");
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


        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IOException("OPENROUTER_API_KEY not set (Environment variables).");
        }

        // OpenRouter is OpenAI-compatible; use the free router:
        String model = "openrouter/free";

        String prompt = """
Tu es un assistant qui rédige une DESCRIPTION PROFESSIONNELLE d’un appel d’offre en français.

IMPORTANT:
- Longueur maximale: 900 caractères.
- Texte clair, structuré en paragraphes courts.
- Pas de markdown.
- Pas de titre.
- Pas de liste à puces.

Contexte:
- Titre: %s
- Catégorie: %s
- Type: %s
- Budget min: %s
- Budget max: %s
- Devise: %s
- Date limite: %s
- Notes: %s
""".formatted(
                safe(titre),
                safe(categorie),
                safe(type),
                fmt(budgetMin),
                fmt(budgetMax),
                safe(devise),
                safe(dateLimiteIso),
                safe(extraNotes)
        );

        String json = """
{
  "model": "%s",
  "messages": [
    {"role":"user","content": %s}
  ],
  "temperature": 0.6,
  "max_tokens": 300
}
""".formatted(model, jsonString(prompt));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                // Optional but recommended headers on OpenRouter:
                .header("HTTP-Referer", "http://localhost") // can be anything for local app
                .header("X-Title", "Gestion Appel d'Offre")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("OpenRouter error " + res.statusCode() + ": " + res.body());
        }

        // Extract choices[0].message.content (simple parse)
        String content = extractFirstContent(res.body());
        if (content == null || content.isBlank()) {
            throw new IOException("Empty AI response.");
        }
        return content.trim();
    }

    private String safe(String s) { return (s == null || s.isBlank()) ? "-" : s.trim(); }
    private String fmt(double v) { return v <= 0 ? "-" : String.valueOf(v); }

    private String jsonString(String s) {
        if (s == null) s = "";
        String escaped = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }

    // very small JSON extraction: finds "content":"...."
    private String extractFirstContent(String json) {
        if (json == null) return null;
        String key = "\"content\"";
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
}