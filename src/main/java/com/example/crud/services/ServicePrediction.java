package com.example.crud.services;

import com.example.crud.models.Action;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🎯 ServicePrediction
 * Prédiction de prix d'actions alimentée par Claude API
 */
public class ServicePrediction {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";

    // ⚠️ IMPORTANT : Remplacez par votre vraie clé API Anthropic
    private static final String API_KEY = "AIzaSyCWUpwiko52_lnqvX3MIfr5gbIVRO2Xv74";

    // Prompt système pour l'analyse financière
    private static final String SYSTEM_PROMPT =
            "Tu es un analyste financier expert spécialisé dans la prédiction de prix d'actions. " +
                    "Tu analyses les données historiques et les tendances du marché pour fournir des prévisions. " +
                    "Tes réponses doivent être :\n" +
                    "1. Basées sur l'analyse technique (tendances, support/résistance)\n" +
                    "2. Claires et structurées\n" +
                    "3. Accompagnées de scénarios (optimiste, réaliste, pessimiste)\n" +
                    "4. Avec des avertissements sur les risques\n\n" +
                    "Format de réponse attendu :\n" +
                    "TENDANCE: [Haussière/Baissière/Neutre]\n" +
                    "PREDICTION_7J: [prix estimé à 7 jours]\n" +
                    "PREDICTION_30J: [prix estimé à 30 jours]\n" +
                    "CONFIANCE: [pourcentage de confiance]\n" +
                    "ANALYSE: [explication détaillée]\n" +
                    "SCENARIOS:\n" +
                    "- Optimiste: [prix]\n" +
                    "- Réaliste: [prix]\n" +
                    "- Pessimiste: [prix]\n" +
                    "RISQUES: [principaux risques identifiés]";

    /**
     * Classe pour stocker les résultats de prédiction
     */
    public static class ResultatPrediction {
        public String tendance;          // Haussière, Baissière, Neutre
        public double prediction7j;      // Prix prédit à 7 jours
        public double prediction30j;     // Prix prédit à 30 jours
        public int confiance;            // Pourcentage de confiance (0-100)
        public String analyse;           // Explication détaillée
        public double scenarioOptimiste;
        public double scenarioRealiste;
        public double scenarioPessimiste;
        public String risques;
        public String reponseComplete;   // Réponse brute de Claude

        @Override
        public String toString() {
            return String.format(
                    "Tendance: %s\n" +
                            "Prédiction 7j: %.2f\n" +
                            "Prédiction 30j: %.2f\n" +
                            "Confiance: %d%%",
                    tendance, prediction7j, prediction30j, confiance
            );
        }
    }

    /**
     * Génère un historique de prix simulé pour une action
     */
    public List<Double> genererHistoriquePrix(Action action, int nbJours) {
        List<Double> historique = new ArrayList<>();
        double prixActuel = action.getPrixUnitaire();
        Random rand = new Random(action.getIdAction());

        // Générer des prix avec tendance réaliste
        for (int i = 0; i < nbJours; i++) {
            double variation = (rand.nextDouble() - 0.5) * (prixActuel * 0.03); // ±3% par jour
            prixActuel += variation;
            if (prixActuel < action.getPrixUnitaire() * 0.7) {
                prixActuel = action.getPrixUnitaire() * 0.7; // Plancher
            }
            if (prixActuel > action.getPrixUnitaire() * 1.3) {
                prixActuel = action.getPrixUnitaire() * 1.3; // Plafond
            }
            historique.add(Math.round(prixActuel * 100.0) / 100.0);
        }

        return historique;
    }

    /**
     * Prédit le prix futur d'une action
     */
    public ResultatPrediction predirePrix(Action action) {
        // Générer l'historique
        List<Double> historique30j = genererHistoriquePrix(action, 30);

        // Construire le prompt avec les données
        String userMessage = construirePromptAnalyse(action, historique30j);

        // Appeler Claude API
        String reponseIA = appellerClaudeAPI(userMessage);

        // Parser la réponse
        return parserReponse(reponseIA, action.getPrixUnitaire());
    }

    /**
     * Construit le prompt d'analyse avec les données
     */
    private String construirePromptAnalyse(Action action, List<Double> historique) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Analyse cette action et prédis son prix futur :\n\n");
        prompt.append("ACTION: ").append(action.getSymbole()).append(" - ").append(action.getNomEntreprise()).append("\n");
        prompt.append("SECTEUR: ").append(action.getSecteur()).append("\n");
        prompt.append("PRIX ACTUEL: ").append(action.getPrixUnitaire()).append(" ").append(action.getBourse().getDevise()).append("\n\n");

        prompt.append("HISTORIQUE DES 30 DERNIERS JOURS:\n");
        for (int i = 0; i < historique.size(); i++) {
            if (i % 5 == 0) { // Afficher tous les 5 jours pour ne pas surcharger
                prompt.append("J-").append(30 - i).append(": ").append(historique.get(i)).append("\n");
            }
        }

        // Calculer quelques métriques
        double min = historique.stream().min(Double::compare).orElse(0.0);
        double max = historique.stream().max(Double::compare).orElse(0.0);
        double moyenne = historique.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        prompt.append("\nMÉTRIQUES:\n");
        prompt.append("- Prix minimum: ").append(String.format("%.2f", min)).append("\n");
        prompt.append("- Prix maximum: ").append(String.format("%.2f", max)).append("\n");
        prompt.append("- Prix moyen: ").append(String.format("%.2f", moyenne)).append("\n");
        prompt.append("- Volatilité: ").append(String.format("%.2f%%", ((max - min) / moyenne) * 100)).append("\n\n");

        prompt.append("Fournis une prédiction de prix pour 7 jours et 30 jours, avec analyse détaillée.");

        return prompt.toString();
    }

    /**
     * Appelle l'API Claude
     */
    private String appellerClaudeAPI(String userMessage) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", 2048);
            requestBody.put("system", SYSTEM_PROMPT);

            JSONArray messages = new JSONArray();
            JSONObject msg = new JSONObject();
            msg.put("role", "user");
            msg.put("content", userMessage);
            messages.put(msg);
            requestBody.put("messages", messages);

            // Envoyer la requête
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", API_KEY);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray content = jsonResponse.getJSONArray("content");
                return content.getJSONObject(0).getString("text");

            } else {
                System.err.println("❌ Erreur API (" + responseCode + ")");
                return "ERREUR: Impossible de contacter l'API de prédiction.";
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur prédiction : " + e.getMessage());
            e.printStackTrace();
            return "ERREUR: " + e.getMessage();
        }
    }

    /**
     * Parse la réponse de Claude en objet structuré
     */
    private ResultatPrediction parserReponse(String reponse, double prixActuel) {
        ResultatPrediction resultat = new ResultatPrediction();
        resultat.reponseComplete = reponse;

        try {
            // Extraire les informations avec regex simple
            String[] lignes = reponse.split("\n");

            for (String ligne : lignes) {
                ligne = ligne.trim();

                if (ligne.startsWith("TENDANCE:")) {
                    resultat.tendance = ligne.substring(9).trim();
                }
                else if (ligne.startsWith("PREDICTION_7J:")) {
                    String val = ligne.substring(14).trim().replaceAll("[^0-9.]", "");
                    resultat.prediction7j = Double.parseDouble(val);
                }
                else if (ligne.startsWith("PREDICTION_30J:")) {
                    String val = ligne.substring(15).trim().replaceAll("[^0-9.]", "");
                    resultat.prediction30j = Double.parseDouble(val);
                }
                else if (ligne.startsWith("CONFIANCE:")) {
                    String val = ligne.substring(10).trim().replaceAll("[^0-9]", "");
                    resultat.confiance = Integer.parseInt(val);
                }
                else if (ligne.startsWith("ANALYSE:")) {
                    resultat.analyse = ligne.substring(8).trim();
                }
                else if (ligne.contains("Optimiste:")) {
                    String val = ligne.substring(ligne.indexOf(":") + 1).trim().replaceAll("[^0-9.]", "");
                    resultat.scenarioOptimiste = Double.parseDouble(val);
                }
                else if (ligne.contains("Réaliste:")) {
                    String val = ligne.substring(ligne.indexOf(":") + 1).trim().replaceAll("[^0-9.]", "");
                    resultat.scenarioRealiste = Double.parseDouble(val);
                }
                else if (ligne.contains("Pessimiste:")) {
                    String val = ligne.substring(ligne.indexOf(":") + 1).trim().replaceAll("[^0-9.]", "");
                    resultat.scenarioPessimiste = Double.parseDouble(val);
                }
                else if (ligne.startsWith("RISQUES:")) {
                    resultat.risques = ligne.substring(8).trim();
                }
            }

            // Valeurs par défaut si parsing échoue
            if (resultat.tendance == null) resultat.tendance = "Neutre";
            if (resultat.prediction7j == 0) resultat.prediction7j = prixActuel;
            if (resultat.prediction30j == 0) resultat.prediction30j = prixActuel;
            if (resultat.confiance == 0) resultat.confiance = 50;
            if (resultat.analyse == null) resultat.analyse = "Analyse en cours...";

        } catch (Exception e) {
            System.err.println("Erreur parsing : " + e.getMessage());
            // Valeurs par défaut
            resultat.tendance = "Neutre";
            resultat.prediction7j = prixActuel;
            resultat.prediction30j = prixActuel;
            resultat.confiance = 50;
            resultat.analyse = reponse;
        }

        return resultat;
    }

    /**
     * Teste si l'API est configurée
     */
    public boolean estConfigure() {
        return API_KEY != null
                && !API_KEY.isEmpty()
                && !API_KEY.equals("VOTRE_CLE_API_ICI");
    }
}