package com.example.crud.services;

import com.example.crud.models.Transaction;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.List;
import java.util.concurrent.*;

public class ServiceAlertesMontant {

    private static final double SEUIL_ALERTE = 50000.0;
    private static boolean alerteActivee = false;

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public static void demarrerSurveillance(int idUtilisateur) {
        if (alerteActivee) return;
        alerteActivee = true;

        scheduler.scheduleAtFixedRate(() -> verifierMontant(idUtilisateur),
                0, 30, TimeUnit.SECONDS);
    }

    private static void verifierMontant(int idUtilisateur) {
        ServiceTransaction serviceTransaction = new ServiceTransaction();
        List<Transaction> transactions = serviceTransaction.getAll();

        double montantTotal = 0;
        for (Transaction t : transactions) {
            if ("ACHAT".equals(t.getTypeTransaction())) {
                montantTotal += t.getMontantTotal();
            }
        }

        if (montantTotal > SEUIL_ALERTE) {
            final double montantFinal = montantTotal; // ✅
            Platform.runLater(() -> declencherAlerte(montantFinal));

            alerteActivee = false;
            scheduler.shutdownNow();
        }
    }

    private static void declencherAlerte(double montant) {
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Impossible de jouer le son");
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🚨 ALERTE MONTANT");
        alert.setHeaderText("Seuil d'investissement dépassé !");
        alert.setContentText(
                "💰 Montant total investi : " + String.format("%.2f TND", montant) + "\n\n" +
                        "⚠️ Vous avez dépassé le seuil de 50,000 TND !\n" +
                        "Veuillez consulter votre conseiller financier."
        );
        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: white;");
        alert.showAndWait();
    }

    public static void arreterSurveillance() {
        alerteActivee = false;
        scheduler.shutdownNow();
    }
}