package com.example.finora.services.bourse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * FINORA - Module Bourse
 * Push vers Power BI (Streaming Semantic Model / Streaming Dataset API +
 * Historic data)
 *
 * IMPORTANT:
 * - L'URL doit être celle fournie par Power BI et contenir ?key=...
 * - On envoie toujours un TABLEAU JSON []
 */
public final class ServicePowerBI {

    // ✅ Recommandé: mettre l'URL dans une variable d'environnement
    // IntelliJ > Run Config > Environment variables:
    // FINORA_POWERBI_PUSH_URL=https://api.powerbi.com/beta/.../rows?key=...
    private static final String POWERBI_PUSH_URL = "https://api.powerbi.com/beta/604f1a96-cbe8-43f8-abbf-f8eaf5d85730/datasets/a3961f27-2ffc-4aba-82b3-9b4ac51867e1/rows?experience=power-bi&key=SgxZVEycQdHYOmn3fvR0RhWiEiBXwWFkKiQfW2jpdJ1sAQDPGL10gzplD6sVvU2gcMidKoQbRTCQwTq4D4Jb8g%3D%3D";

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private static final Gson GSON = new GsonBuilder().create();

    private ServicePowerBI() {
    }

    /**
     * DTO EXACTEMENT aligné avec les colonnes du dataset Power BI "Transactions"
     * (mêmes noms !)
     */
    public static final class TransactionBI {
        public Number id_transaction;
        public String symbole;
        public String type;
        public Number quantite;
        public Number prix;
        public Number montant;
        public Number commission;
        public String date; // ISO DateTime string
        public String utilisateur;

        public TransactionBI(Number id_transaction, String symbole, String type,
                Number quantite, Number prix, Number montant, Number commission,
                String date, String utilisateur) {
            this.id_transaction = id_transaction;
            this.symbole = symbole;
            this.type = type;
            this.quantite = quantite;
            this.prix = prix;
            this.montant = montant;
            this.commission = commission;
            this.date = date;
            this.utilisateur = utilisateur;
        }
    }

    public static String toPowerBIDateTime(LocalDateTime dt) {
        // format ISO: 2026-02-24T14:30:00
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static void pushTransaction(TransactionBI t) throws IOException {
        pushTransactions(List.of(t));
    }

    public static void pushTransactions(List<TransactionBI> transactions) throws IOException {
        if (transactions == null || transactions.isEmpty())
            return;

        if (POWERBI_PUSH_URL == null || POWERBI_PUSH_URL.isBlank()
                || POWERBI_PUSH_URL.contains("COLLE_ICI_TON_URL")) {
            throw new IllegalStateException(
                    "❌ POWERBI_PUSH_URL non configurée. " +
                            "Mets FINORA_POWERBI_PUSH_URL ou colle l'URL Power BI (avec ?key=...).");
        }

        String payload = GSON.toJson(transactions); // array JSON

        HttpURLConnection conn = null;
        try {
            URL url = new URL(POWERBI_PUSH_URL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                String err = readStream(conn, true);
                String ok = readStream(conn, false);
                throw new IOException("Power BI push failed. HTTP " + code +
                        "\nResponse: " + (err != null ? err : ok));
            }

        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

    public static String testerConnexion() {
        try {
            TransactionBI t = new TransactionBI(
                    999999,
                    "TEST",
                    "ACHAT",
                    1,
                    1.0,
                    1.0,
                    0.0,
                    toPowerBIDateTime(LocalDateTime.now()),
                    "system");
            pushTransaction(t);
            return "✅ Connexion Power BI OK (ligne test envoyée)";
        } catch (Exception e) {
            return "❌ Connexion Power BI KO: " + e.getMessage();
        }
    }

    private static String readStream(HttpURLConnection conn, boolean error) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(error ? conn.getErrorStream() : conn.getInputStream(),
                        StandardCharsets.UTF_8))) {
            if (br == null)
                return null;
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            return sb.toString().trim();
        } catch (Exception ignored) {
            return null;
        }
    }
}