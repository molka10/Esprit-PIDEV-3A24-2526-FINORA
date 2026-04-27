package com.example.finora.services.bourse;

import com.example.finora.entities.TransactionBourse;
import com.example.finora.utils.DBConnection;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;

public class ServiceAlertesMontant {

    private static final double SEUIL_ALERTE = 50000.0;
    private static boolean alerteActivee = false;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Connection connection;

    public ServiceAlertesMontant() {
        try {
            this.connection = DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    public static void demarrerSurveillance(int idUtilisateur) {
        if (alerteActivee)
            return;
        alerteActivee = true;

        scheduler.scheduleAtFixedRate(() -> verifierMontant(idUtilisateur),
                0, 30, TimeUnit.SECONDS);
    }

    private static void verifierMontant(int idUtilisateur) {
        ServiceTransactionBourse serviceTransaction = new ServiceTransactionBourse();
        List<TransactionBourse> transactions = serviceTransaction.getAll();

        double montantTotal = 0;
        for (TransactionBourse t : transactions) {
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
                        "Veuillez consulter votre conseiller financier.");
        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: white;");
        alert.showAndWait();
    }

    public static void arreterSurveillance() {
        alerteActivee = false;
        scheduler.shutdownNow();
    }
}