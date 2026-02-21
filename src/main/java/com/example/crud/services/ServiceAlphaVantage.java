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
 * 📊 ServiceAlphaVantage - Prix boursiers en temps réel
 * API : https://www.alphavantage.co/
 *
 * Limites gratuites :
 * - 25 requêtes/jour
 * - 5 requêtes/minute
 */
public class ServiceAlphaVantage {

    // ⚠️ METS TA CLÉ API ICI
    // Obtiens-la sur : https://www.alphavantage.co/support/#api-key
    private static final String API_KEY = " 13FYK3XSWHB578NR";

    private static final String BASE_URL = "https://www.alphavantage.co/query";

    // 💾 CACHE (important car seulement 25 requêtes/jour)
    private static Map<String, DonneesAction> cache = new HashMap<>();
    private static Map<String, Long> cacheTimestamp = new HashMap<>();
    private static final long CACHE_DUREE = 3600000; // 1 heure

    // ⏱️ Anti-spam (5 req/min max)
    private static long dernierAppel = 0;
    private static final long DELAI_MIN = 12000; // 12 secondes entre chaque requête

    public static class DonneesAction {
        public String symbole;
        public double prix;
        public double variation;
        public double variationPourcent;
        public double ouverture;
        public double plusHaut;
        public double plusBas;
        public long volume;
        public String derniereUpdate;
        public boolean fromCache;
    }

    /**
     * Vérifie si la clé API est configurée
     */
    public boolean estConfigure() {
        return API_KEY != null && !API_KEY.isBlank()
                && !API_KEY.equals("TON_API_KEY_ICI");
    }

    /**
     * Récupère le prix actuel d'une action
     * @param symbole Code de l'action (ex: "AAPL", "MSFT", "GOOGL")
     * @return Prix actuel
     */
    public double getPrixActuel(String symbole) {
        DonneesAction donnees = getDonneesAction(symbole);
        return donnees != null ? donnees.prix : 0.0;
    }

    /**
     * Récupère toutes les données d'une action
     * @param symbole Code de l'action
     * @return Objet DonneesAction complet
     */
    public DonneesAction getDonneesAction(String symbole) {

        if (!estConfigure()) {
            System.err.println("❌ Clé Alpha Vantage non configurée");
            return null;
        }

        // Vérifier le cache
        if (cache.containsKey(symbole)) {
            long timestamp = cacheTimestamp.get(symbole);
            if (System.currentTimeMillis() - timestamp < CACHE_DUREE) {
                System.out.println("✅ Données chargées depuis le cache pour " + symbole);
                DonneesAction data = cache.get(symbole);
                data.fromCache = true;
                return data;
            }
        }

        // Vérifier le délai entre requêtes
        long maintenant = System.currentTimeMillis();
        if (maintenant - dernierAppel < DELAI_MIN) {
            long attente = (DELAI_MIN - (maintenant - dernierAppel)) / 1000;
            System.out.println("⏱️ Attente de " + attente + " secondes avant la prochaine requête...");

            // Si donnée en cache (même expirée), la retourner
            if (cache.containsKey(symbole)) {
                return cache.get(symbole);
            }
            return null;
        }

        dernierAppel = maintenant;

        try {
            // Construire l'URL
            String urlString = BASE_URL +
                    "?function=GLOBAL_QUOTE" +
                    "&symbol=" + symbole +
                    "&apikey=" + API_KEY;

            System.out.println("🌐 Appel API Alpha Vantage pour " + symbole);

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

                // Vérifier si l'API retourne une erreur
                if (json.has("Note")) {
                    System.err.println("⚠️ Limite de requêtes atteinte : " + json.getString("Note"));
                    // Retourner cache si disponible
                    if (cache.containsKey(symbole)) {
                        return cache.get(symbole);
                    }
                    return null;
                }

                if (json.has("Error Message")) {
                    System.err.println("❌ Erreur API : " + json.getString("Error Message"));
                    return null;
                }

                if (!json.has("Global Quote")) {
                    System.err.println("❌ Symbole introuvable : " + symbole);
                    return null;
                }

                JSONObject quote = json.getJSONObject("Global Quote");

                // Créer l'objet de données
                DonneesAction donnees = new DonneesAction();
                donnees.symbole = symbole;
                donnees.prix = quote.getDouble("05. price");
                donnees.variation = quote.getDouble("09. change");
                donnees.variationPourcent = parseDouble(quote.getString("10. change percent").replace("%", ""));
                donnees.ouverture = quote.getDouble("02. open");
                donnees.plusHaut = quote.getDouble("03. high");
                donnees.plusBas = quote.getDouble("04. low");
                donnees.volume = quote.getLong("06. volume");
                donnees.derniereUpdate = quote.getString("07. latest trading day");
                donnees.fromCache = false;

                // Sauvegarder en cache
                cache.put(symbole, donnees);
                cacheTimestamp.put(symbole, System.currentTimeMillis());

                System.out.println("✅ Données récupérées pour " + symbole + " : " + donnees.prix);

                return donnees;

            } else {
                System.err.println("❌ Erreur HTTP " + responseCode);
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'appel à Alpha Vantage : " + e.getMessage());
            e.printStackTrace();

            // Retourner cache si disponible
            if (cache.containsKey(symbole)) {
                return cache.get(symbole);
            }
            return null;
        }
    }

    /**
     * Récupère les données de plusieurs actions (optimisé)
     * @param symboles Liste des symboles
     * @return Map symbole -> données
     */
    public Map<String, DonneesAction> getDonneesMultiples(String... symboles) {
        Map<String, DonneesAction> resultats = new HashMap<>();

        for (String symbole : symboles) {
            DonneesAction donnees = getDonneesAction(symbole);
            if (donnees != null) {
                resultats.put(symbole, donnees);
            }

            // Pause de 12 secondes entre chaque requête (sauf si cache)
            if (!donnees.fromCache && symboles.length > 1) {
                try {
                    Thread.sleep(DELAI_MIN);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return resultats;
    }

    /**
     * Vide le cache
     */
    public static void viderCache() {
        cache.clear();
        cacheTimestamp.clear();
        System.out.println("🗑️ Cache Alpha Vantage vidé");
    }

    /**
     * Taille du cache
     */
    public static int tailleCache() {
        return cache.size();
    }

    /**
     * Test de connexion
     */
    public String testerConnexion() {
        if (!estConfigure()) {
            return "❌ Clé API Alpha Vantage non configurée";
        }

        System.out.println("🧪 Test de connexion Alpha Vantage avec symbole AAPL...");

        DonneesAction apple = getDonneesAction("AAPL");

        if (apple == null) {
            return "❌ Échec du test - Vérifiez votre clé API et votre connexion internet";
        }

        return "✅ Connexion Alpha Vantage OK - Prix Apple (AAPL) : $" + apple.prix;
    }

    /**
     * Parse un double depuis une string (gère les erreurs)
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Méthode utilitaire pour afficher les informations
     */
    public static void afficherDonnees(DonneesAction donnees) {
        if (donnees == null) {
            System.out.println("Aucune donnée disponible");
            return;
        }

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  " + donnees.symbole + " - " + donnees.derniereUpdate + "  ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("  Prix : $" + donnees.prix);
        System.out.println("  Variation : " + donnees.variation + " (" + donnees.variationPourcent + "%)");
        System.out.println("  Ouverture : $" + donnees.ouverture);
        System.out.println("  Plus haut : $" + donnees.plusHaut);
        System.out.println("  Plus bas : $" + donnees.plusBas);
        System.out.println("  Volume : " + donnees.volume);
        System.out.println("  Source : " + (donnees.fromCache ? "Cache" : "API"));
        System.out.println("╚══════════════════════════════════════╝");
    }

    /**
     * Exemple d'utilisation
     */
    public static void main(String[] args) {
        ServiceAlphaVantage service = new ServiceAlphaVantage();

        // Test de connexion
        System.out.println(service.testerConnexion());
        System.out.println();

        // Récupérer données Apple
        DonneesAction apple = service.getDonneesAction("AAPL");
        afficherDonnees(apple);

        System.out.println("\n" + service.testerConnexion());

        // Tester le cache
        System.out.println("\n🔄 Test du cache...");
        DonneesAction appleCache = service.getDonneesAction("AAPL");
        afficherDonnees(appleCache);
    }
}