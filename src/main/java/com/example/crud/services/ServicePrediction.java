package com.example.crud.services;

import com.example.crud.models.Action;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🎯 ServicePrediction (Gemini - Google AI Studio)
 */
public class ServicePrediction {

    // ✅ API Gemini (stable)
    private static final String API_BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    // ✅ Modèle qui fonctionne dans la majorité des projets AI Studio
    private static final String MODEL =
            "gemini-1.5-flash-001";

    // ⚠️ METS TA NOUVELLE CLE ICI (pas celle leakée)
    private static final String API_KEY =
            "AIzaSyDHdVbsRlLx6aMMuD5pmjkSzAXbM0jHPY4";

    private static final String SYSTEM_PROMPT =
            "Tu es un analyste financier expert.\n" +
                    "Analyse technique prudente uniquement.\n" +
                    "Ceci est informatif, pas un conseil financier.\n\n" +
                    "Format EXACT:\n" +
                    "TENDANCE: ...\n" +
                    "PREDICTION_7J: ...\n" +
                    "PREDICTION_30J: ...\n" +
                    "CONFIANCE: ...\n" +
                    "ANALYSE: ...\n" +
                    "SCENARIOS:\n" +
                    "- Optimiste: ...\n" +
                    "- Réaliste: ...\n" +
                    "- Pessimiste: ...\n" +
                    "RISQUES: ...";

    public static class ResultatPrediction {
        public String tendance;
        public double prediction7j;
        public double prediction30j;
        public int confiance;
        public String analyse;
        public double scenarioOptimiste;
        public double scenarioRealiste;
        public double scenarioPessimiste;
        public String risques;
        public String reponseComplete;
    }

    public boolean estConfigure() {
        return API_KEY != null && !API_KEY.isBlank()
                && !API_KEY.equals("MET_TA_NOUVELLE_CLE_ICI");
    }

    /* ===================== HISTORIQUE SIMULÉ ===================== */

    public List<Double> genererHistoriquePrix(Action action, int nbJours) {
        List<Double> historique = new ArrayList<>();
        double prix = action.getPrixUnitaire();
        Random rand = new Random(action.getIdAction());

        for (int i = 0; i < nbJours; i++) {
            double variation = (rand.nextDouble() - 0.5) * (prix * 0.03);
            prix += variation;

            double min = action.getPrixUnitaire() * 0.7;
            double max = action.getPrixUnitaire() * 1.3;

            if (prix < min) prix = min;
            if (prix > max) prix = max;

            historique.add(Math.round(prix * 100.0) / 100.0);
        }
        return historique;
    }

    /* ===================== PREDICTION ===================== */

    public ResultatPrediction predirePrix(Action action) {

        if (!estConfigure()) {
            ResultatPrediction r = new ResultatPrediction();
            r.tendance = "Erreur";
            r.analyse = "Clé Gemini non configurée.";
            return r;
        }

        List<Double> historique = genererHistoriquePrix(action, 30);
        String prompt = construirePrompt(action, historique);
        String reponse = appelerGemini(prompt);

        return parserReponse(reponse, action.getPrixUnitaire());
    }

    private String construirePrompt(Action action, List<Double> historique) {

        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_PROMPT).append("\n\n");

        sb.append("ACTION: ").append(action.getSymbole()).append("\n");
        sb.append("PRIX ACTUEL: ").append(action.getPrixUnitaire()).append("\n\n");

        sb.append("HISTORIQUE:\n");
        for (int i = 0; i < historique.size(); i += 5) {
            sb.append("J-").append(30 - i)
                    .append(": ").append(historique.get(i)).append("\n");
        }

        sb.append("\nAnalyse et prédis.");
        return sb.toString();
    }

    /* ===================== APPEL GEMINI ===================== */

    private String appelerGemini(String message) {

        try {
            JSONObject body = new JSONObject();

            JSONArray contents = new JSONArray();

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("parts", new JSONArray().put(
                    new JSONObject().put("text", message)
            ));

            contents.put(userMsg);
            body.put("contents", contents);

            JSONObject cfg = new JSONObject();
            cfg.put("temperature", 0.4);
            cfg.put("maxOutputTokens", 1000);
            body.put("generationConfig", cfg);

            String endpoint =
                    API_BASE + MODEL + ":generateContent?key=" + API_KEY;

            HttpURLConnection conn =
                    (HttpURLConnection) new URL(endpoint).openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/json; charset=utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String response = readAll(is);

            if (code >= 200 && code < 300) {

                JSONObject json = new JSONObject(response);
                JSONArray candidates = json.optJSONArray("candidates");

                if (candidates == null || candidates.isEmpty())
                    return "ERREUR: Réponse vide.";

                return candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } else {
                System.err.println("Gemini Error: " + response);
                return "ERREUR API Gemini (" + code + ")";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "ERREUR: " + e.getMessage();
        }
    }

    /* ===================== PARSING ===================== */

    private ResultatPrediction parserReponse(String rep, double prixActuel) {

        ResultatPrediction r = new ResultatPrediction();
        r.reponseComplete = rep;

        try {
            for (String line : rep.split("\n")) {
                line = line.trim();

                if (line.startsWith("TENDANCE:"))
                    r.tendance = line.substring(9).trim();

                else if (line.startsWith("PREDICTION_7J:"))
                    r.prediction7j =
                            extractDouble(line);

                else if (line.startsWith("PREDICTION_30J:"))
                    r.prediction30j =
                            extractDouble(line);

                else if (line.startsWith("CONFIANCE:"))
                    r.confiance =
                            (int) extractDouble(line);

                else if (line.startsWith("ANALYSE:"))
                    r.analyse =
                            line.substring(8).trim();

                else if (line.contains("Optimiste:"))
                    r.scenarioOptimiste =
                            extractDouble(line);

                else if (line.contains("Réaliste:")
                        || line.contains("Realiste:"))
                    r.scenarioRealiste =
                            extractDouble(line);

                else if (line.contains("Pessimiste:"))
                    r.scenarioPessimiste =
                            extractDouble(line);

                else if (line.startsWith("RISQUES:"))
                    r.risques =
                            line.substring(8).trim();
            }

        } catch (Exception ignored) {}

        if (r.tendance == null) r.tendance = "Neutre";
        if (r.prediction7j == 0) r.prediction7j = prixActuel;
        if (r.prediction30j == 0) r.prediction30j = prixActuel;
        if (r.confiance == 0) r.confiance = 50;
        if (r.analyse == null) r.analyse = rep;

        return r;
    }

    private double extractDouble(String line) {
        String val = line.replaceAll("[^0-9.]", "");
        return val.isEmpty() ? 0 : Double.parseDouble(val);
    }

    private String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br =
                new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String l;
        while ((l = br.readLine()) != null) sb.append(l);
        br.close();
        return sb.toString();
    }
}
