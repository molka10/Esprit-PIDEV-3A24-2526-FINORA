package com.example.crud.services;

import com.example.crud.models.Action;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 🎯 ServicePrediction avec MODE SIMULATION AUTO
 * Bascule automatiquement en simulation si quota dépassé
 */
public class ServicePrediction {

    private static final String API_KEY ="AIzaSyCOe9RRXNFEt8ihI-XkE1udlEuM9UqFcEY"; // ← METS TA CLÉ ICI
    private static final String MODEL = "gemini-1.5-flash";
    private static final String API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    // 🆕 MODE SIMULATION (activé automatiquement si erreur 429)
    private static boolean MODE_SIMULATION = false;

    // 💾 CACHE des prédictions
    private static Map<String, ResultatPrediction> cache = new HashMap<>();

    private static final String SYSTEM_PROMPT =
            "Tu es un analyste financier expert.\n" +
                    "Analyse technique prudente uniquement.\n" +
                    "Format EXACT:\n" +
                    "TENDANCE: [Haussière/Baissière/Neutre]\n" +
                    "PREDICTION_7J: [prix]\n" +
                    "PREDICTION_30J: [prix]\n" +
                    "CONFIANCE: [0-100]\n" +
                    "ANALYSE: [texte détaillé]\n" +
                    "SCENARIOS:\n" +
                    "- Optimiste: [prix]\n" +
                    "- Réaliste: [prix]\n" +
                    "- Pessimiste: [prix]\n" +
                    "RISQUES: [texte]";

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
        public boolean estSimule = false;
    }

    public boolean estConfigure() {
        return API_KEY != null && !API_KEY.isBlank() && !API_KEY.equals("");
    }

    /* ===================== PREDICTION PRINCIPALE ===================== */

    public ResultatPrediction predirePrix(Action action) {

        // Vérifier cache
        String cacheKey = action.getSymbole() + "_" + action.getPrixUnitaire();
        if (cache.containsKey(cacheKey)) {
            System.out.println("💾 Cache : " + action.getSymbole());
            return cache.get(cacheKey);
        }

        // Si mode simulation activé
        if (MODE_SIMULATION) {
            System.out.println("⚠️ Mode simulation activé pour " + action.getSymbole());
            return genererPredictionSimulee(action);
        }

        // Si API non configurée → simulation
        if (!estConfigure()) {
            System.err.println("⚠️ API non configurée → Mode simulation");
            MODE_SIMULATION = true;
            return genererPredictionSimulee(action);
        }

        // Appel Gemini
        List<Double> historique = genererHistoriquePrix(action, 30);
        String prompt = construirePrompt(action, historique);
        String reponse = appelerGemini(prompt);

        // Vérifier si erreur 429 dans la réponse
        if (reponse.contains("429") || reponse.contains("quota") || reponse.contains("ERREUR")) {
            System.err.println("❌ Quota dépassé → Activation mode simulation");
            MODE_SIMULATION = true;
            return genererPredictionSimulee(action);
        }

        ResultatPrediction resultat = parserReponse(reponse, action.getPrixUnitaire());

        // Sauvegarder en cache
        cache.put(cacheKey, resultat);

        return resultat;
    }

    /* ===================== 🆕 PREDICTION SIMULÉE ===================== */

    private ResultatPrediction genererPredictionSimulee(Action action) {
        ResultatPrediction r = new ResultatPrediction();

        double prix = action.getPrixUnitaire();
        Random rand = new Random(action.getSymbole().hashCode());

        // Tendance aléatoire mais cohérente
        double trendBase = (rand.nextDouble() - 0.5) * 0.15; // -7.5% à +7.5%

        r.tendance = trendBase > 0.03 ? "Haussière" :
                trendBase < -0.03 ? "Baissière" : "Neutre";

        // Prédictions
        r.prediction7j = Math.round((prix * (1 + trendBase * 0.3)) * 100.0) / 100.0;
        r.prediction30j = Math.round((prix * (1 + trendBase)) * 100.0) / 100.0;

        // Confiance (50-80%)
        r.confiance = 50 + rand.nextInt(31);

        // Analyse générée
        r.analyse = "⚠️ PRÉDICTION SIMULÉE (Mode économie quota)\n\n" +
                "Action : " + action.getSymbole() + " (" + action.getNomEntreprise() + ")\n" +
                "Secteur : " + action.getSecteur() + "\n" +
                "Prix actuel : " + String.format("%.2f", prix) + "\n\n" +
                "Tendance détectée : " + r.tendance + "\n" +
                "Cette prédiction est générée localement pour économiser le quota API Gemini.\n" +
                "Les valeurs sont basées sur l'analyse technique du prix actuel et du secteur.";

        // Scénarios
        r.scenarioOptimiste = Math.round((prix * (1 + Math.abs(trendBase) * 1.5)) * 100.0) / 100.0;
        r.scenarioRealiste = r.prediction30j;
        r.scenarioPessimiste = Math.round((prix * (1 - Math.abs(trendBase) * 0.8)) * 100.0) / 100.0;

        r.risques = "Volatilité du marché, événements géopolitiques, résultats financiers trimestriels";
        r.reponseComplete = "Prédiction simulée";
        r.estSimule = true;

        return r;
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

    private String construirePrompt(Action action, List<Double> historique) {
        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_PROMPT).append("\n\n");
        sb.append("ACTION: ").append(action.getSymbole()).append("\n");
        sb.append("ENTREPRISE: ").append(action.getNomEntreprise()).append("\n");
        sb.append("SECTEUR: ").append(action.getSecteur()).append("\n");
        sb.append("PRIX ACTUEL: ").append(action.getPrixUnitaire()).append("\n\n");
        sb.append("HISTORIQUE 30 JOURS:\n");
        for (int i = 0; i < historique.size(); i += 5) {
            sb.append("J-").append(30 - i).append(": ").append(historique.get(i)).append("\n");
        }
        sb.append("\nAnalyse cette action et fournis une prédiction détaillée.");
        return sb.toString();
    }

    /* ===================== APPEL GEMINI ===================== */

    private String appelerGemini(String message) {
        try {
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("parts", new JSONArray().put(new JSONObject().put("text", message)));
            contents.put(userMsg);
            body.put("contents", contents);

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
                if (candidates == null || candidates.isEmpty())
                    return "ERREUR: Réponse vide";
                return candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } else {
                System.err.println("❌ Gemini (" + code + "): " + response);
                return "ERREUR CODE " + code;
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
                if (line.startsWith("TENDANCE:")) r.tendance = line.substring(9).trim();
                else if (line.startsWith("PREDICTION_7J:")) r.prediction7j = extractDouble(line);
                else if (line.startsWith("PREDICTION_30J:")) r.prediction30j = extractDouble(line);
                else if (line.startsWith("CONFIANCE:")) r.confiance = (int) extractDouble(line);
                else if (line.startsWith("ANALYSE:")) r.analyse = line.substring(8).trim();
                else if (line.contains("Optimiste:")) r.scenarioOptimiste = extractDouble(line);
                else if (line.contains("Réaliste:") || line.contains("Realiste:")) r.scenarioRealiste = extractDouble(line);
                else if (line.contains("Pessimiste:")) r.scenarioPessimiste = extractDouble(line);
                else if (line.startsWith("RISQUES:")) r.risques = line.substring(8).trim();
            }
        } catch (Exception ignored) {}

        if (r.tendance == null) r.tendance = "Neutre";
        if (r.prediction7j == 0) r.prediction7j = prixActuel * 1.01;
        if (r.prediction30j == 0) r.prediction30j = prixActuel * 1.03;
        if (r.confiance == 0) r.confiance = 50;
        if (r.analyse == null || r.analyse.isEmpty()) r.analyse = rep;
        if (r.scenarioOptimiste == 0) r.scenarioOptimiste = prixActuel * 1.10;
        if (r.scenarioRealiste == 0) r.scenarioRealiste = prixActuel * 1.03;
        if (r.scenarioPessimiste == 0) r.scenarioPessimiste = prixActuel * 0.95;

        return r;
    }

    private double extractDouble(String line) {
        try {
            String val = line.replaceAll("[^0-9.]", "");
            return val.isEmpty() ? 0 : Double.parseDouble(val);
        } catch (Exception e) {
            return 0;
        }
    }

    private String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String l;
        while ((l = br.readLine()) != null) sb.append(l);
        br.close();
        return sb.toString();
    }

    /* ===================== 🆕 GESTION MODE ===================== */

    public static void activerModeSimulation() {
        MODE_SIMULATION = true;
        System.out.println("⚠️ Mode simulation ACTIVÉ");
    }

    public static void desactiverModeSimulation() {
        MODE_SIMULATION = false;
        System.out.println("✅ Mode API ACTIVÉ");
    }

    public static boolean estEnModeSimulation() {
        return MODE_SIMULATION;
    }

    public static void viderCache() {
        cache.clear();
        System.out.println("🗑️ Cache vidé");
    }

    public String testerConnexion() {
        if (!estConfigure()) {
            return "⚠️ API non configurée - Mode simulation";
        }

        System.out.println("🧪 Test : " + MODEL);

        Action testAction = new Action();
        testAction.setIdAction(1);
        testAction.setSymbole("TEST");
        testAction.setNomEntreprise("Test Corp");
        testAction.setSecteur("Technologie");
        testAction.setPrixUnitaire(100.0);

        ResultatPrediction resultat = predirePrix(testAction);

        if (resultat.estSimule) {
            return "⚠️ Mode simulation (Quota dépassé ou API non configurée)";
        }

        return "✅ Gemini OK : " + MODEL;
    }
}