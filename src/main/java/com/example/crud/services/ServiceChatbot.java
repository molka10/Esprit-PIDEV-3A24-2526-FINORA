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
    private static final String API_KEY = "AIzaSyBt_rZbStfGSKNlJk5fjccyw8UQW8OIDVY";

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
            // 1) Ajouter message user à l'historique
            Map<String, String> u = new HashMap<>();
            u.put("role", "user");
            u.put("text", userMessage.trim());
            history.add(u);

            // 2) Construire JSON (SANS systemInstruction)
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();

            // ✅ On met le system prompt comme 1er message "user"
            JSONObject sys = new JSONObject();
            sys.put("role", "user");
            sys.put("parts", new JSONArray().put(new JSONObject().put("text", SYSTEM_PROMPT)));
            contents.put(sys);

            // ✅ Ajouter l'historique
            for (Map<String, String> msg : history) {
                JSONObject m = new JSONObject();

                String role = msg.get("role");
                // Gemini veut "user" et "model"
                if ("assistant".equals(role)) role = "model";
                if (!"user".equals(role) && !"model".equals(role)) role = "user";

                m.put("role", role);
                m.put("parts", new JSONArray().put(new JSONObject().put("text", msg.get("text"))));
                contents.put(m);
            }

            body.put("contents", contents);

            // generationConfig (optionnel)
            JSONObject genCfg = new JSONObject();
            genCfg.put("temperature", 0.6);
            genCfg.put("maxOutputTokens", 800);
            body.put("generationConfig", genCfg);

            // 3) Appel HTTP
            String endpoint = BASE_URL + MODEL + ":generateContent?key=" + API_KEY;
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
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

                // Ajouter réponse à l'historique
                Map<String, String> a = new HashMap<>();
                a.put("role", "model");
                a.put("text", assistantText);
                history.add(a);

                return assistantText;
            } else {
                String err = readAll(conn.getErrorStream());
                System.err.println("❌ Gemini API Error (" + code + "): " + err);
                return "Désolé, problème Gemini (" + code + ").";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur connexion Gemini. Vérifie clé + internet.";
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
