package com.example.crud.services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 💱 ServiceDevise - Conversion de devises en temps réel
 * API : https://www.exchangerate-api.com/
 *
 * Limites gratuites :
 * - 1500 requêtes/mois
 * - Mise à jour quotidienne
 */
public class ServiceDevise {

    // ⚠️ METS TA CLÉ API ICI
    // Obtiens-la sur : https://www.exchangerate-api.com/
    private static final String API_KEY = "3e2ba8a627f5382b93e1b85a";

    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    // 💾 CACHE (mise à jour quotidienne suffit)
    private static Map<String, Double> cache = new HashMap<>();
    private static Map<String, Long> cacheTimestamp = new HashMap<>();
    private static final long CACHE_DUREE = 86400000; // 24 heures

    // Devises principales
    public static class Devise {
        public static final String USD = "USD"; // Dollar américain
        public static final String EUR = "EUR"; // Euro
        public static final String TND = "TND"; // Dinar tunisien
        public static final String GBP = "GBP"; // Livre sterling
        public static final String JPY = "JPY"; // Yen japonais
        public static final String CHF = "CHF"; // Franc suisse
        public static final String CAD = "CAD"; // Dollar canadien
        public static final String AUD = "AUD"; // Dollar australien
        public static final String CNY = "CNY"; // Yuan chinois
    }

    /**
     * Vérifie si la clé API est configurée
     */
    public boolean estConfigure() {
        return API_KEY != null && !API_KEY.isBlank()
                && !API_KEY.equals("TON_API_KEY_ICI");
    }

    /**
     * Convertit un montant d'une devise à une autre
     * @param montant Montant à convertir
     * @param depart Devise de départ (ex: "USD")
     * @param arrivee Devise d'arrivée (ex: "EUR")
     * @return Montant converti
     */
    public double convertir(double montant, String depart, String arrivee) {
        double taux = getTaux(depart, arrivee);
        return taux > 0 ? Math.round(montant * taux * 100.0) / 100.0 : montant;
    }

    /**
     * Récupère le taux de change entre deux devises
     * @param depart Devise de départ
     * @param arrivee Devise d'arrivée
     * @return Taux de change (1 unité de départ = X unités d'arrivée)
     */
    public double getTaux(String depart, String arrivee) {

        if (!estConfigure()) {
            System.err.println("❌ Clé Exchange Rate API non configurée");
            return 1.0;
        }

        // Si même devise, retourner 1
        if (depart.equals(arrivee)) {
            return 1.0;
        }

        String cacheKey = depart + "_" + arrivee;

        // Vérifier le cache
        if (cache.containsKey(cacheKey)) {
            long timestamp = cacheTimestamp.get(cacheKey);
            if (System.currentTimeMillis() - timestamp < CACHE_DUREE) {
                System.out.println("✅ Taux " + depart + "/" + arrivee + " chargé depuis le cache");
                return cache.get(cacheKey);
            }
        }

        try {
            // Construire l'URL
            String urlString = BASE_URL + API_KEY + "/pair/" + depart + "/" + arrivee;

            System.out.println("🌐 Appel API Exchange Rate pour " + depart + "/" + arrivee);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                // Lire la réponse
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parser le JSON
                JSONObject json = new JSONObject(response.toString());

                // Vérifier le résultat
                String result = json.getString("result");

                if (!result.equals("success")) {
                    System.err.println("❌ Erreur API : " + json.optString("error-type", "Inconnue"));
                    return 1.0;
                }

                double taux = json.getDouble("conversion_rate");

                // Sauvegarder en cache
                cache.put(cacheKey, taux);
                cacheTimestamp.put(cacheKey, System.currentTimeMillis());

                System.out.println("✅ Taux récupéré : 1 " + depart + " = " + taux + " " + arrivee);

                return taux;

            } else {
                System.err.println("❌ Erreur HTTP " + responseCode);

                // Retourner cache si disponible
                if (cache.containsKey(cacheKey)) {
                    return cache.get(cacheKey);
                }
                return 1.0;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'appel à Exchange Rate API : " + e.getMessage());
            e.printStackTrace();

            // Retourner cache si disponible
            if (cache.containsKey(cacheKey)) {
                return cache.get(cacheKey);
            }
            return 1.0;
        }
    }

    /**
     * Récupère tous les taux pour une devise donnée
     * @param devise Devise de base
     * @return Map des taux (devise -> taux)
     */
    public Map<String, Double> getTousLesTaux(String devise) {

        if (!estConfigure()) {
            System.err.println("❌ Clé Exchange Rate API non configurée");
            return new HashMap<>();
        }

        try {
            String urlString = BASE_URL + API_KEY + "/latest/" + devise;

            System.out.println("🌐 Récupération de tous les taux pour " + devise);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(response.toString());

                if (!json.getString("result").equals("success")) {
                    return new HashMap<>();
                }

                JSONObject rates = json.getJSONObject("conversion_rates");
                Map<String, Double> taux = new HashMap<>();

                for (String key : rates.keySet()) {
                    taux.put(key, rates.getDouble(key));
                }

                System.out.println("✅ " + taux.size() + " taux récupérés");

                return taux;

            } else {
                System.err.println("❌ Erreur HTTP " + responseCode);
                return new HashMap<>();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Convertit un montant vers plusieurs devises
     * @param montant Montant de départ
     * @param deviseDepart Devise de départ
     * @param devisesArrivee Devises d'arrivée
     * @return Map devise -> montant converti
     */
    public Map<String, Double> convertirVersMultiples(double montant, String deviseDepart, String... devisesArrivee) {
        Map<String, Double> resultats = new HashMap<>();

        for (String devise : devisesArrivee) {
            double montantConverti = convertir(montant, deviseDepart, devise);
            resultats.put(devise, montantConverti);
        }

        return resultats;
    }

    /**
     * Formate un montant avec sa devise
     */
    public String formater(double montant, String devise) {
        String symbole = getSymbole(devise);
        return String.format("%.2f %s", montant, symbole);
    }

    /**
     * Retourne le symbole d'une devise
     */
    public String getSymbole(String devise) {
        switch (devise) {
            case "USD": return "$";
            case "EUR": return "€";
            case "TND": return "TND";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CHF": return "CHF";
            case "CAD": return "C$";
            case "AUD": return "A$";
            case "CNY": return "¥";
            default: return devise;
        }
    }

    /**
     * Vide le cache
     */
    public static void viderCache() {
        cache.clear();
        cacheTimestamp.clear();
        System.out.println("🗑️ Cache Exchange Rate vidé");
    }

    /**
     * Test de connexion
     */
    public String testerConnexion() {
        if (!estConfigure()) {
            return "❌ Clé API Exchange Rate non configurée";
        }

        System.out.println("🧪 Test de connexion Exchange Rate API (USD -> EUR)...");

        double taux = getTaux(Devise.USD, Devise.EUR);

        if (taux <= 0 || taux == 1.0) {
            return "❌ Échec du test - Vérifiez votre clé API";
        }

        return "✅ Connexion Exchange Rate OK - Taux USD/EUR : " + taux;
    }

    /**
     * Exemple d'utilisation
     */
    public static void main(String[] args) {
        ServiceDevise service = new ServiceDevise();

        // Test de connexion
        System.out.println(service.testerConnexion());
        System.out.println();

        // Conversion simple
        double montant = 1000;
        System.out.println("💰 Conversion de " + montant + " USD :");

        double eur = service.convertir(montant, Devise.USD, Devise.EUR);
        System.out.println("  → EUR : " + service.formater(eur, Devise.EUR));

        double tnd = service.convertir(montant, Devise.USD, Devise.TND);
        System.out.println("  → TND : " + service.formater(tnd, Devise.TND));

        double gbp = service.convertir(montant, Devise.USD, Devise.GBP);
        System.out.println("  → GBP : " + service.formater(gbp, Devise.GBP));

        System.out.println();

        // Conversion multiple
        System.out.println("💱 Conversion de 500 EUR vers plusieurs devises :");
        Map<String, Double> conversions = service.convertirVersMultiples(
                500, Devise.EUR,
                Devise.USD, Devise.TND, Devise.GBP, Devise.JPY
        );

        for (Map.Entry<String, Double> entry : conversions.entrySet()) {
            System.out.println("  → " + service.formater(entry.getValue(), entry.getKey()));
        }

        System.out.println();
        System.out.println("📊 Taille du cache : " + cache.size() + " taux");
    }
}