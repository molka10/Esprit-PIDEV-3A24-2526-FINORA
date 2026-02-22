package com.example.crud.services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 📊 ServiceAlphaVantage - Prix boursiers en temps réel (AMÉLIORÉ)
 * API : https://www.alphavantage.co/
 */
public class ServiceAlphaVantage {

    private static final String API_KEY = "13FYK3XSWHB578NR";
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    // Cache
    private static Map<String, DonneesAction> cache = new HashMap<>();
    private static Map<String, Long> cacheTimestamp = new HashMap<>();
    private static final long CACHE_DUREE = 3600000; // 1 heure

    // Anti-spam
    private static long dernierAppel = 0;
    private static final long DELAI_MIN = 12000; // 12 secondes

    // 🆕 Liste symboles valides (actions US principales)
    private static final Set<String> SYMBOLES_VALIDES = new HashSet<>(Arrays.asList(
            "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "JPM",
            "V", "WMT", "MA", "JNJ", "PG", "DIS", "NFLX", "ADBE", "CRM",
            "PYPL", "INTC", "CSCO", "VZ", "T", "PFE", "KO", "PEP", "NKE",
            "MCD", "BA", "CAT", "GE", "IBM", "ORCL", "AMD", "QCOM", "TXN"
    ));

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
        public boolean estValide;
    }

    public boolean estConfigure() {
        return API_KEY != null && !API_KEY.isBlank()
                && !API_KEY.equals("TON_API_KEY_ICI");
    }

    /**
     * 🆕 Vérifier si le symbole est valide
     */
    public boolean estSymboleValide(String symbole) {
        if (symbole == null || symbole.isBlank()) return false;
        return SYMBOLES_VALIDES.contains(symbole.toUpperCase());
    }

    /**
     * 🆕 Suggérer un symbole valide
     */
    public String suggererSymbole(String symboleInvalide) {
        if (symboleInvalide == null || symboleInvalide.isBlank()) {
            return "AAPL";
        }

        String upper = symboleInvalide.toUpperCase();

        // Recherche par préfixe
        for (String valid : SYMBOLES_VALIDES) {
            if (valid.startsWith(upper.substring(0, Math.min(2, upper.length())))) {
                return valid;
            }
        }

        return "AAPL"; // Par défaut
    }

    public double getPrixActuel(String symbole) {
        DonneesAction donnees = getDonneesAction(symbole);
        return donnees != null ? donnees.prix : 0.0;
    }

    /**
     * 🔧 getDonneesAction AMÉLIORÉ avec validation
     */
    public DonneesAction getDonneesAction(String symbole) {

        if (!estConfigure()) {
            System.err.println("❌ Clé Alpha Vantage non configurée");
            return creerDonneesErreur(symbole, "API non configurée");
        }

        // 🆕 Vérification symbole valide
        if (!estSymboleValide(symbole)) {
            System.err.println("⚠️ Symbole invalide : " + symbole + " (non supporté par Alpha Vantage)");
            String suggestion = suggererSymbole(symbole);
            System.err.println("💡 Suggestion : Utilise " + suggestion + " à la place");
            return creerDonneesErreur(symbole, "Symbole invalide - US uniquement");
        }

        // Cache
        if (cache.containsKey(symbole)) {
            long timestamp = cacheTimestamp.get(symbole);
            if (System.currentTimeMillis() - timestamp < CACHE_DUREE) {
                System.out.println("✅ Cache : " + symbole);
                DonneesAction data = cache.get(symbole);
                data.fromCache = true;
                return data;
            }
        }

        // Délai anti-spam
        long maintenant = System.currentTimeMillis();
        if (maintenant - dernierAppel < DELAI_MIN) {
            long attente = (DELAI_MIN - (maintenant - dernierAppel)) / 1000;
            System.out.println("⏱️ Attente " + attente + "s...");

            if (cache.containsKey(symbole)) {
                return cache.get(symbole);
            }
            return creerDonneesErreur(symbole, "Attente anti-spam");
        }

        dernierAppel = maintenant;

        try {
            String urlString = BASE_URL +
                    "?function=GLOBAL_QUOTE" +
                    "&symbol=" + symbole +
                    "&apikey=" + API_KEY;

            System.out.println("🌐 API Alpha Vantage : " + symbole);

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

                if (json.has("Note")) {
                    System.err.println("⚠️ Quota atteint : " + json.getString("Note"));
                    if (cache.containsKey(symbole)) {
                        return cache.get(symbole);
                    }
                    return creerDonneesErreur(symbole, "Quota dépassé");
                }

                if (json.has("Error Message")) {
                    System.err.println("❌ Erreur API : " + json.getString("Error Message"));
                    return creerDonneesErreur(symbole, "Erreur API");
                }

                if (!json.has("Global Quote")) {
                    System.err.println("❌ Aucune donnée pour : " + symbole);
                    return creerDonneesErreur(symbole, "Données introuvables");
                }

                JSONObject quote = json.getJSONObject("Global Quote");

                // 🔧 Vérification quote non vide
                if (quote.isEmpty() || !quote.has("05. price")) {
                    System.err.println("❌ Quote vide pour : " + symbole);
                    return creerDonneesErreur(symbole, "Quote vide");
                }

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
                donnees.estValide = true;

                cache.put(symbole, donnees);
                cacheTimestamp.put(symbole, System.currentTimeMillis());

                System.out.println("✅ Récupéré : " + symbole + " = $" + donnees.prix);

                return donnees;

            } else {
                System.err.println("❌ HTTP " + responseCode);
                return creerDonneesErreur(symbole, "HTTP " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("❌ Exception : " + e.getMessage());

            if (cache.containsKey(symbole)) {
                return cache.get(symbole);
            }
            return creerDonneesErreur(symbole, "Erreur réseau");
        }
    }

    /**
     * 🆕 Créer des données d'erreur
     */
    private DonneesAction creerDonneesErreur(String symbole, String raison) {
        DonneesAction erreur = new DonneesAction();
        erreur.symbole = symbole;
        erreur.prix = 0.0;
        erreur.variation = 0.0;
        erreur.variationPourcent = 0.0;
        erreur.estValide = false;
        erreur.fromCache = false;
        erreur.derniereUpdate = "Erreur: " + raison;
        return erreur;
    }

    public Map<String, DonneesAction> getDonneesMultiples(String... symboles) {
        Map<String, DonneesAction> resultats = new HashMap<>();

        for (String symbole : symboles) {
            DonneesAction donnees = getDonneesAction(symbole);
            if (donnees != null && donnees.estValide) {
                resultats.put(symbole, donnees);
            }

            if (donnees != null && !donnees.fromCache && symboles.length > 1) {
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
     * 🆕 Lister tous les symboles valides
     */
    public List<String> getSymbolesValides() {
        return new ArrayList<>(SYMBOLES_VALIDES);
    }

    public static void viderCache() {
        cache.clear();
        cacheTimestamp.clear();
        System.out.println("🗑️ Cache vidé");
    }

    public static int tailleCache() {
        return cache.size();
    }

    public String testerConnexion() {
        if (!estConfigure()) {
            return "❌ Clé API non configurée";
        }

        System.out.println("🧪 Test avec AAPL...");

        DonneesAction apple = getDonneesAction("AAPL");

        if (apple == null || !apple.estValide) {
            return "❌ Échec - Vérifier clé API";
        }

        return "✅ OK - Prix AAPL : $" + apple.prix;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static void afficherDonnees(DonneesAction donnees) {
        if (donnees == null) {
            System.out.println("Aucune donnée");
            return;
        }

        if (!donnees.estValide) {
            System.out.println("❌ " + donnees.symbole + " : " + donnees.derniereUpdate);
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

    public static void main(String[] args) {
        ServiceAlphaVantage service = new ServiceAlphaVantage();

        System.out.println(service.testerConnexion());
        System.out.println();

        // Test symboles valides
        System.out.println("📋 Symboles valides :");
        service.getSymbolesValides().stream()
                .limit(10)
                .forEach(s -> System.out.println("  - " + s));

        System.out.println("\n🧪 Test AAPL :");
        DonneesAction apple = service.getDonneesAction("AAPL");
        afficherDonnees(apple);

        System.out.println("\n🧪 Test symbole invalide (MC) :");
        DonneesAction invalid = service.getDonneesAction("MC");
        afficherDonnees(invalid);
    }
}