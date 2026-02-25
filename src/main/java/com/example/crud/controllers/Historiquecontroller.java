package com.example.crud.controllers;

import com.example.crud.entities.Transaction;
import com.example.crud.services.ServiceTransaction;
import com.example.crud.services.Servicepdf;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 📜 HistoriqueController
 * Affiche l'historique complet des transactions
 */
public class Historiquecontroller implements Initializable {

    // Stat labels
    @FXML private Label lblTotalInvesti;
    @FXML private Label lblTotalVendu;
    @FXML private Label lblCommissions;
    @FXML private Label lblNbTransactions;

    // Filtres
    @FXML private ToggleButton btnTous;
    @FXML private ToggleButton btnAchats;
    @FXML private ToggleButton btnVentes;
    @FXML private TextField    searchField;

    // Liste
    @FXML private VBox listeContainer;

    private final ServiceTransaction serviceTransaction = new ServiceTransaction();
    private final Servicepdf servicePDF = new Servicepdf();
    private List<Transaction> toutesTransactions;
    private String filtreActif = "TOUS";

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r); t.setDaemon(true); return t;
    });

    // ── Source de retour (passée depuis le dashboard) ──────────
    private String fxmlRetour = "/com/example/crud/dashboard-investisseur-view.fxml";

    public void setFxmlRetour(String fxml) { this.fxmlRetour = fxml; }

    // ─────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerDonnees();

        // Recherche en temps réel
        searchField.textProperty().addListener((obs, o, kw) -> appliquerFiltre());
    }

    // ─────────────────────────────────────────────────────────
    //  CHARGEMENT
    // ─────────────────────────────────────────────────────────

    private void chargerDonnees() {
        Task<List<Transaction>> task = new Task<>() {
            @Override protected List<Transaction> call() {
                return serviceTransaction.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            toutesTransactions = task.getValue();
            mettreAJourStats();
            appliquerFiltre();
        });

        task.setOnFailed(e -> System.err.println("Erreur historique : " + task.getException()));
        executor.submit(task);
    }

    // ─────────────────────────────────────────────────────────
    //  STATS
    // ─────────────────────────────────────────────────────────

    private void mettreAJourStats() {
        lblTotalInvesti  .setText(String.format("%.2f TND", serviceTransaction.getTotalInvesti()));
        lblTotalVendu    .setText(String.format("%.2f TND", serviceTransaction.getTotalVendu()));
        lblCommissions   .setText(String.format("%.2f TND", serviceTransaction.getTotalCommissions()));
        lblNbTransactions.setText(String.valueOf(serviceTransaction.getNombreTransactions()));
    }

    // ─────────────────────────────────────────────────────────
    //  FILTRES
    // ─────────────────────────────────────────────────────────

    @FXML private void filtrerTous   (ActionEvent e) { filtreActif = "TOUS";  styleToggle(); appliquerFiltre(); }
    @FXML private void filtrerAchats (ActionEvent e) { filtreActif = "ACHAT"; styleToggle(); appliquerFiltre(); }
    @FXML private void filtrerVentes (ActionEvent e) { filtreActif = "VENTE"; styleToggle(); appliquerFiltre(); }

    private void styleToggle() {
        String actif  = "-fx-background-color: #00d4aa; -fx-text-fill: #0f1117; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;";
        String inactif= "-fx-background-color: #ffffff11; -fx-text-fill: #9ca3af; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;";
        btnTous   .setStyle("TOUS" .equals(filtreActif) ? actif : inactif);
        btnAchats .setStyle("ACHAT".equals(filtreActif) ? actif : inactif);
        btnVentes .setStyle("VENTE".equals(filtreActif) ? actif : inactif);
    }

    private void appliquerFiltre() {
        if (toutesTransactions == null) return;

        String kw = searchField.getText().trim().toLowerCase();

        List<Transaction> filtrées = toutesTransactions.stream()
                .filter(t -> "TOUS".equals(filtreActif) || t.getTypeTransaction().equals(filtreActif))
                .filter(t -> kw.isEmpty()
                        || t.getSymbole().toLowerCase().contains(kw)
                        || t.getNomEntreprise().toLowerCase().contains(kw))
                .collect(Collectors.toList());

        afficherListe(filtrées);
    }

    // ─────────────────────────────────────────────────────────
    //  AFFICHAGE DES LIGNES
    // ─────────────────────────────────────────────────────────

    private void afficherListe(List<Transaction> transactions) {
        listeContainer.getChildren().clear();

        if (transactions.isEmpty()) {
            Label empty = new Label("Aucune transaction à afficher.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b5563; -fx-padding: 40;");
            listeContainer.getChildren().add(empty);
            return;
        }

        for (Transaction t : transactions) {
            listeContainer.getChildren().add(creerLigne(t));
        }
    }

    private HBox creerLigne(Transaction t) {
        HBox row = new HBox();
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent transparent #ffffff0a transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Hover
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #ffffff05;" +
                        "-fx-border-color: transparent transparent #ffffff0a transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        ));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent transparent #ffffff0a transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        ));

        boolean isAchat = "ACHAT".equals(t.getTypeTransaction());

        // Badge ACHAT / VENTE
        Label badgeType = new Label(isAchat ? "▲ ACHAT" : "▼ VENTE");
        badgeType.setPrefWidth(80);
        badgeType.setAlignment(Pos.CENTER);
        badgeType.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;" +
                        (isAchat
                                ? "-fx-background-color: #00d4aa22; -fx-text-fill: #00d4aa;"
                                : "-fx-background-color: #ef444422; -fx-text-fill: #ef4444;")
        );

        // Symbole
        Label symLabel = new Label(t.getSymbole());
        symLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white; -fx-pref-width: 100;");

        // Nom entreprise
        Label nomLabel = new Label(t.getNomEntreprise());
        nomLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af;");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);

        // Quantité
        Label qteLabel = new Label(String.valueOf(t.getQuantite()));
        qteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e5e7eb; -fx-pref-width: 90; -fx-alignment: CENTER_RIGHT;");

        // Prix unitaire
        Label prixLabel = new Label(String.format("%.2f %s", t.getPrixUnitaire(), t.getDevise()));
        prixLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e5e7eb; -fx-pref-width: 110; -fx-alignment: CENTER_RIGHT;");

        // Montant total
        Label montantLabel = new Label(String.format("%.2f %s", t.getMontantTotal(), t.getDevise()));
        montantLabel.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 120; -fx-alignment: CENTER_RIGHT;" +
                        "-fx-text-fill: " + (isAchat ? "#00d4aa" : "#ef4444") + ";"
        );

        // Commission
        Label commLabel = new Label(String.format("%.2f %s", t.getCommission(), t.getDevise()));
        commLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b; -fx-pref-width: 110; -fx-alignment: CENTER_RIGHT;");

        // Date
        String dateStr = t.getDateTransaction() != null
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(t.getDateTransaction())
                : "—";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-pref-width: 140; -fx-alignment: CENTER_RIGHT;");

        // Bouton PDF
        Button btnPDF = new Button("📄 PDF");
        btnPDF.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white;" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;"
        );
        btnPDF.setOnAction(e -> {
            String pdfPath = servicePDF.genererFacture(t);
            if (pdfPath != null) {
                servicePDF.ouvrirPDF(pdfPath);
                showSuccess("✅ Facture générée et ouverte !\n" + pdfPath);
            } else {
                showError("❌ Erreur lors de la génération du PDF");
            }
        });

        HBox actionBox = new HBox(btnPDF);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPrefWidth(80);

        row.getChildren().addAll(badgeType, symLabel, nomLabel, qteLabel, prixLabel, montantLabel, commLabel, dateLabel, actionBox);
        return row;
    }

    // ─────────────────────────────────────────────────────────
    //  ACTUALISER
    // ─────────────────────────────────────────────────────────

    @FXML
    private void handleActualiser(ActionEvent event) {
        searchField.clear();
        filtreActif = "TOUS";
        styleToggle();
        chargerDonnees();
    }

    // ─────────────────────────────────────────────────────────
    //  RETOUR
    // ─────────────────────────────────────────────────────────

    @FXML
    private void retourDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlRetour));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FINORA - Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}