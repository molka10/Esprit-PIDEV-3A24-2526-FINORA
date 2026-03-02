package com.example.finora.services.appeldoffre;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OpenRouterDescriptionService {

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

        // ✅ read env var at runtime (NOT static)
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("OPENROUTER_API_KEY not set in IntelliJ Run Configuration (Environment variables).");
        }

        // ✅ If you want to keep free model, keep this:
        // String model = "openrouter/free";

        // ✅ More reliable default model (if your key allows it on OpenRouter):
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
                .timeout(Duration.ofSeconds(45))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                // OpenRouter recommended headers:
                .header("HTTP-Referer", "http://localhost")
                .header("X-Title", "Gestion Appel d'Offre")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IOException("OpenRouter error " + res.statusCode() + ":\n" + res.body());
        }

        String content = extractChoicesMessageContent(res.body());
        if (content == null || content.isBlank()) {
            throw new IOException("Empty AI response. Raw:\n" + res.body());
        }
        return content.trim();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s.trim();
    }

    private String fmt(double v) {
        return v <= 0 ? "-" : String.valueOf(v);
    }

    private String jsonString(String s) {
        if (s == null) s = "";
        String escaped = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }

    /**
     * Extracts: choices[0].message.content
     * Simple robust parse without extra libs.
     */
    private String extractChoicesMessageContent(String json) {
        if (json == null) return null;

        // Find "choices"
        int choicesIdx = json.indexOf("\"choices\"");
        if (choicesIdx < 0) return null;

        // Find first "content" after that
        int contentIdx = json.indexOf("\"content\"", choicesIdx);
        if (contentIdx < 0) return null;

        int colon = json.indexOf(':', contentIdx);
        if (colon < 0) return null;

        // move to first quote of the string
        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;

        StringBuilder sb = new StringBuilder();
        boolean esc = false;

        for (int i = firstQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);

            if (esc) {
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
                esc = false;
                continue;
            }

            if (c == '\\') {
                esc = true;
                continue;
            }

            if (c == '"') {
                return sb.toString();
            }

            sb.append(c);
        }

        return null;
    }
}