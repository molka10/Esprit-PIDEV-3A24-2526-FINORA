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
 * Prédiction de prix d'actions alimentée par Google Gemini API
 */
public class ServicePrediction {

    // ✅ Endpoint Gemini API (AI Studio / Generative Language)
    // Astuce: si v1beta te pose problème, tu peux essayer v1.
    private static final String API_BASE =
            "https://generativelanguage.googleapis.com/v1/models/";

    private static final String MODEL =
            "gemini-pro";





    // ✅ Mets ta clé AI Studio ici
    private static final String API_KEY = "AIzaSyAx4bGSoJ_X0CdaJuEUimm8A1MAK9wpqRA";

    // Prompt système
    private static final String SYSTEM_PROMPT =
            "Tu es un analyste financier expert spécialisé dans l'analyse technique.\n" +
                    "Tu dois fournir des prévisions prudentes, avec scénarios, et avertissement risques.\n" +
                    "IMPORTANT: ceci est informatif, pas un conseil financier professionnel.\n\n" +
                    "Format EXACT attendu :\n" +
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
        return API_KEY != null && !API_KEY.isBlank() && !API_KEY.equals("VOTRE_CLE_GEMINI_ICI");
    }

    public void debugModels() {
        try {
            String endpoint =
                    "https://generativelanguage.googleapis.com/v1/models?key=" + API_KEY;

            HttpURLConnection conn =
                    (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Génère un historique de prix simulé pour une action
     */
    public List<Double> genererHistoriquePrix(Action action, int nbJours) {
        List<Double> historique = new ArrayList<>();
        double prixActuel = action.getPrixUnitaire();
        Random rand = new Random(action.getIdAction());

        for (int i = 0; i < nbJours; i++) {
            double variation = (rand.nextDouble() - 0.5) * (prixActuel * 0.03); // ±3%
            prixActuel += variation;

            double floor = action.getPrixUnitaire() * 0.7;
            double cap   = action.getPrixUnitaire() * 1.3;

            if (prixActuel < floor) prixActuel = floor;
            if (prixActuel > cap)   prixActuel = cap;

            historique.add(Math.round(prixActuel * 100.0) / 100.0);
        }
        return historique;
    }

    public ResultatPrediction predirePrix(Action action) {
        List<Double> historique30j = genererHistoriquePrix(action, 30);
        String promptUser = construirePromptAnalyse(action, historique30j);

        String reponseIA = appelerGemini(promptUser);
        return parserReponse(reponseIA, action.getPrixUnitaire());
    }

    private String construirePromptAnalyse(Action action, List<Double> historique) {
        String devise = (action.getBourse() != null && action.getBourse().getDevise() != null)
                ? action.getBourse().getDevise() : "TND";

        StringBuilder sb = new StringBuilder();
        sb.append("Analyse cette action et prédis son prix futur.\n\n");
        sb.append("ACTION: ").append(action.getSymbole()).append(" - ").append(action.getNomEntreprise()).append("\n");
        sb.append("SECTEUR: ").append(action.getSecteur()).append("\n");
        sb.append("PRIX ACTUEL: ").append(action.getPrixUnitaire()).append(" ").append(devise).append("\n\n");

        sb.append("HISTORIQUE 30J (1 point / 5 jours):\n");
        for (int i = 0; i < historique.size(); i += 5) {
            sb.append("J-").append(30 - i).append(": ").append(historique.get(i)).append("\n");
        }

        double min = historique.stream().min(Double::compare).orElse(action.getPrixUnitaire());
        double max = historique.stream().max(Double::compare).orElse(action.getPrixUnitaire());
        double moy = historique.stream().mapToDouble(Double::doubleValue).average().orElse(action.getPrixUnitaire());

        sb.append("\nMÉTRIQUES:\n");
        sb.append("- Min: ").append(String.format("%.2f", min)).append("\n");
        sb.append("- Max: ").append(String.format("%.2f", max)).append("\n");
        sb.append("- Moyenne: ").append(String.format("%.2f", moy)).append("\n");
        sb.append("- Volatilité approx: ").append(String.format("%.2f%%", ((max - min) / Math.max(moy, 0.0001)) * 100)).append("\n\n");

        sb.append("Donne une prédiction 7j et 30j, scénarios et risques, au format demandé.");
        return sb.toString();
    }

    /**
     * Appel Gemini : models/{model}:generateContent
     */
    private String appelerGemini(String userMessage) {
        if (!estConfigure()) {
            return "ERREUR: Clé Gemini manquante. Configure API_KEY avec ta clé AI Studio.";
        }

        try {
            JSONObject body = new JSONObject();

            // contents
            JSONArray contents = new JSONArray();

            // 0) SYSTEM PROMPT comme premier message user (compat v1beta)
            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "user");
            sysMsg.put("parts", new JSONArray().put(
                    new JSONObject().put("text", SYSTEM_PROMPT)
            ));
            contents.put(sysMsg);

            // 1) Message utilisateur
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("parts", new JSONArray().put(
                    new JSONObject().put("text", userMessage)
            ));
            contents.put(user);

            body.put("contents", contents);

            // generationConfig
            JSONObject cfg = new JSONObject();
            cfg.put("temperature", 0.4);
            cfg.put("maxOutputTokens", 1200);
            body.put("generationConfig", cfg);

            String endpoint = API_BASE + MODEL + ":generateContent?key=" + API_KEY;

            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readAll(is);

            if (code >= 200 && code < 300) {
                JSONObject json = new JSONObject(response);

                JSONArray candidates = json.optJSONArray("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    return "ERREUR: Réponse Gemini vide.";
                }

                JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                if (content == null) return "ERREUR: content manquant.";

                JSONArray parts = content.optJSONArray("parts");
                if (parts == null || parts.isEmpty()) return "ERREUR: parts manquant.";

                return parts.getJSONObject(0).optString("text", "ERREUR: text manquant.");

            } else {
                System.err.println("❌ Gemini API Error (" + code + "): " + response);

                if (code == 404) {
                    return "ERREUR: Modèle Gemini introuvable (404). Change MODEL.";
                }
                return "ERREUR: Gemini (" + code + "). " + response;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "ERREUR: " + e.getMessage();
        } }


        /**
         * Optionnel : liste les modèles disponibles (pour régler ton 404).
         * Utilisation : appelle cette méthode une fois et regarde la console.
         */
    public String listerModels() {
        if (!estConfigure()) return "Clé manquante.";
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models?key=" + API_KEY;
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readAll(is);

            System.out.println("🔎 ListModels code=" + code);
            System.out.println(response);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur listModels: " + e.getMessage();
        }
    }

    private ResultatPrediction parserReponse(String reponse, double prixActuel) {
        ResultatPrediction r = new ResultatPrediction();
        r.reponseComplete = reponse;

        try {
            String[] lignes = reponse.split("\n");
            for (String ligne : lignes) {
                String l = ligne.trim();

                if (l.startsWith("TENDANCE:")) r.tendance = l.substring("TENDANCE:".length()).trim();

                else if (l.startsWith("PREDICTION_7J:")) {
                    String val = l.substring("PREDICTION_7J:".length()).trim().replaceAll("[^0-9.]", "");
                    if (!val.isEmpty()) r.prediction7j = Double.parseDouble(val);
                }
                else if (l.startsWith("PREDICTION_30J:")) {
                    String val = l.substring("PREDICTION_30J:".length()).trim().replaceAll("[^0-9.]", "");
                    if (!val.isEmpty()) r.prediction30j = Double.parseDouble(val);
                }
                else if (l.startsWith("CONFIANCE:")) {
                    String val = l.substring("CONFIANCE:".length()).trim().replaceAll("[^0-9]", "");
                    if (!val.isEmpty()) r.confiance = Integer.parseInt(val);
                }
                else if (l.startsWith("ANALYSE:")) {
                    r.analyse = l.substring("ANALYSE:".length()).trim();
                }
                else if (l.contains("Optimiste:")) {
                    String val = l.substring(l.indexOf(":") + 1).trim().replaceAll("[^0-9.]", "");
                    if (!val.isEmpty()) r.scenarioOptimiste = Double.parseDouble(val);
                }
                else if (l.contains("Réaliste:") || l.contains("Realiste:")) {
                    String val = l.substring(l.indexOf(":") + 1).trim().replaceAll("[^0-9.]", "");
                    if (!val.isEmpty()) r.scenarioRealiste = Double.parseDouble(val);
                }
                else if (l.contains("Pessimiste:")) {
                    String val = l.substring(l.indexOf(":") + 1).trim().replaceAll("[^0-9.]", "");
                    if (!val.isEmpty()) r.scenarioPessimiste = Double.parseDouble(val);
                }
                else if (l.startsWith("RISQUES:")) {
                    r.risques = l.substring("RISQUES:".length()).trim();
                }
            }

        } catch (Exception ignored) {}

        // Defaults
        if (r.tendance == null) r.tendance = "Neutre";
        if (r.prediction7j == 0) r.prediction7j = prixActuel;
        if (r.prediction30j == 0) r.prediction30j = prixActuel;
        if (r.confiance <= 0) r.confiance = 50;
        if (r.analyse == null) r.analyse = reponse;
        if (r.risques == null) r.risques = "Risque marché, volatilité, news, liquidité.";

        return r;
    }

    private String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }
}
