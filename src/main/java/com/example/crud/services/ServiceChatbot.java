package com.example.crud.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 🤖 ServiceChatbot (Gemini)
 * Chatbot Conseiller Financier alimenté par Google Gemini API
 */
public class ServiceChatbot {

    // ✅ Endpoint REST (Gemini API)
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    // ✅ Modèle qui marche avec generateContent (selon doc officielle)
    private static final String MODEL = "gemini-2.5-flash";

    // ⚠️ IMPORTANT : mets ta clé dans une variable d'environnement si possible
    private static final String API_KEY = "AIzaSyCWUpwiko52_lnqvX3MIfr5gbIVRO2Xv74";

    // Historique conversation : role = "user" ou "model"
    private final List<Map<String, String>> history = new ArrayList<>();

    private static final String SYSTEM_PROMPT =
            "Tu es un conseiller financier expert intégré dans FINORA, une plateforme de trading boursier tunisienne.\n" +
                    "Tu aides l'utilisateur à comprendre les actions, marchés, risques et portefeuille.\n" +
                    "Règles:\n" +
                    "1) Réponds en français, clair et structuré.\n" +
                    "2) Ne donne JAMAIS de garantie (pas de certitude d'achat/vente).\n" +
                    "3) Si info insuffisante, pose 1-2 questions.\n" +
                    "4) Si tu ne sais pas, dis-le.\n" +
                    "5) Termine par: 'Ceci est informatif, pas un conseil financier professionnel.'\n";

    public String envoyerMessage(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) return "";

        try {
            // Ajouter message utilisateur
            Map<String, String> u = new HashMap<>();
            u.put("role", "user");
            u.put("text", userMessage.trim());
            history.add(u);

            JSONObject requestBody = new JSONObject();

            JSONArray contents = new JSONArray();

            // ✅ 1) System prompt en 1er message (simple & stable)
            JSONObject sys = new JSONObject();
            sys.put("role", "user");
            sys.put("parts", new JSONArray().put(new JSONObject().put("text", SYSTEM_PROMPT)));
            contents.put(sys);

            // ✅ 2) Historique
            for (Map<String, String> msg : history) {
                JSONObject m = new JSONObject();
                m.put("role", msg.get("role")); // "user" ou "model"
                m.put("parts", new JSONArray().put(new JSONObject().put("text", msg.get("text"))));
                contents.put(m);
            }

            requestBody.put("contents", contents);

            JSONObject genCfg = new JSONObject();
            genCfg.put("temperature", 0.6);
            genCfg.put("maxOutputTokens", 800);
            requestBody.put("generationConfig", genCfg);

            String endpoint = BASE_URL + MODEL + ":generateContent";

            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // ✅ Auth recommandée: x-goog-api-key (doc officielle)
            conn.setRequestProperty("x-goog-api-key", API_KEY);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();

            if (code == 200) {
                String responseText = readAll(conn.getInputStream());
                JSONObject json = new JSONObject(responseText);

                String assistantText = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                Map<String, String> a = new HashMap<>();
                a.put("role", "model");
                a.put("text", assistantText);
                history.add(a);

                return assistantText;
            } else {
                String err = readAll(conn.getErrorStream());
                System.err.println("❌ Gemini API Error (" + code + "): " + err);

                // Petit message plus clair
                if (code == 403) return "Accès refusé (clé API / quota / API non activée).";
                if (code == 404) return "Modèle introuvable. Change le MODEL (ex: gemini-2.5-flash).";
                return "Désolé, problème technique Gemini. Réessaie.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur connexion Gemini. Vérifie internet + clé API + modèle.";
        }
    }

    public void reinitialiserConversation() {
        history.clear();
    }

    public boolean estConfigure() {
        return API_KEY != null && !API_KEY.isBlank() && !API_KEY.equals("MET_TA_CLE_ICI");
    }

    private String readAll(java.io.InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }
}
