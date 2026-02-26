package com.example.crud.services;

import com.example.crud.models.Transaction;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 📄 ServicePDF
 * Génère des factures/contrats PDF pour les transactions
 */
public class Servicepdf {

    String scriptPath = "src\\main\\java\\com\\example\\crud\\scripts\\generate_invoice.py";
    private static final String OUTPUT_DIR = "exports/factures/";

    public Servicepdf() {
        // Créer le dossier exports s'il n'existe pas
        new File(OUTPUT_DIR).mkdirs();
    }

    /**
     * Génère une facture PDF pour une transaction
     * @param transaction La transaction à documenter
     * @return Le chemin du fichier PDF généré
     */
    public String genererFacture(Transaction transaction) {
        try {
            // Nom du fichier
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("FACTURE_%s_%s_%d.pdf",
                    transaction.getTypeTransaction(),
                    transaction.getSymbole(),
                    transaction.getIdTransaction());
            String outputPath = OUTPUT_DIR + filename;

            // Préparer les données pour le script Python
            String[] command = {
                    "python3",
                    scriptPath,
                    "--output", outputPath,
                    "--type", transaction.getTypeTransaction(),
                    "--symbole", transaction.getSymbole(),
                    "--nom", transaction.getNomEntreprise(),
                    "--quantite", String.valueOf(transaction.getQuantite()),
                    "--prix", String.format("%.2f", transaction.getPrixUnitaire()),
                    "--total", String.format("%.2f", transaction.getMontantTotal()),
                    "--commission", String.format("%.2f", transaction.getCommission()),
                    "--devise", transaction.getDevise(),
                    "--date", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(transaction.getDateTransaction()),
                    "--id", String.valueOf(transaction.getIdTransaction())
            };

            // Exécuter le script Python
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Lire la sortie
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ PDF généré : " + outputPath);
                return outputPath;
            } else {
                System.err.println("❌ Erreur génération PDF (code " + exitCode + "):");
                System.err.println(output.toString());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur génération PDF : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ouvre le PDF généré dans le viewer par défaut du système
     */
    public void ouvrirPDF(String filepath) {
        if (filepath == null || !new File(filepath).exists()) {
            System.err.println("❌ Fichier introuvable : " + filepath);
            return;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                // Windows
                pb = new ProcessBuilder("cmd", "/c", "start", filepath);
            } else if (os.contains("mac")) {
                // macOS
                pb = new ProcessBuilder("open", filepath);
            } else {
                // Linux
                pb = new ProcessBuilder("xdg-open", filepath);
            }

            pb.start();
            System.out.println("✅ PDF ouvert : " + filepath);

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
}