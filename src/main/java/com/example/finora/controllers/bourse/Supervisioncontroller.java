package com.example.finora.controllers.bourse;

import com.example.finora.entities.TransactionBourse;
import com.example.finora.services.bourse.ServiceTransactionBourse;
import com.example.finora.utils.Navigator;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 🔍 SupervisionController
 * Dashboard Admin pour superviser toutes les transactions
 */
public class Supervisioncontroller implements Initializable {

    // Stats
    @FXML
    private Label lblVolumeTotal;
    @FXML
    private Label lblCommissionsTotal;
    @FXML
    private Label lblNbTransactions;
    @FXML
    private Label lblTransactionsSuspectes;

    // Filtres
    @FXML
    private ComboBox<String> cbFiltreType;
    @FXML
    private ComboBox<String> cbFiltreAction;
    @FXML
    private TextField tfMontantMin;
    @FXML
    private TextField tfMontantMax;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private Label lblResultatsFiltres;

    // Liste
    @FXML
    private VBox listeContainer;

    private final ServiceTransactionBourse serviceTransaction = new ServiceTransactionBourse();
    private List<TransactionBourse> toutesTransactions = new ArrayList<>();
    private List<TransactionBourse> transactionsFiltrees = new ArrayList<>();

    private static final double SEUIL_SUSPECT = 50000.0;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser filtres
        cbFiltreType.setItems(FXCollections.observableArrayList("Tous", "ACHAT", "VENTE"));
        cbFiltreType.getSelectionModel().selectFirst();

        chargerDonnees();
    }

    // ─────────────────────────────────────────────────────────
    // CHARGEMENT
    // ─────────────────────────────────────────────────────────

    private void chargerDonnees() {
        Task<List<TransactionBourse>> task = new Task<>() {
            @Override
            protected List<TransactionBourse> call() {
                return serviceTransaction.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            toutesTransactions = task.getValue();
            transactionsFiltrees = new ArrayList<>(toutesTransactions);

            // Remplir liste actions
            Set<String> symboles = toutesTransactions.stream()
                    .map(TransactionBourse::getSymbole)
                    .collect(Collectors.toSet());
            List<String> listeActions = new ArrayList<>(symboles);
            listeActions.add(0, "Toutes");
            cbFiltreAction.setItems(FXCollections.observableArrayList(listeActions));
            cbFiltreAction.getSelectionModel().selectFirst();

            mettreAJourStats();
            afficherTransactions(transactionsFiltrees);
        });

        task.setOnFailed(e -> System.err.println("Erreur : " + task.getException()));
        executor.submit(task);
    }

    // ─────────────────────────────────────────────────────────
    // STATISTIQUES
    // ─────────────────────────────────────────────────────────

    private void mettreAJourStats() {
        double volumeTotal = toutesTransactions.stream()
                .mapToDouble(TransactionBourse::getMontantTotal)
                .sum();

        double commissionsTotal = toutesTransactions.stream()
                .mapToDouble(TransactionBourse::getCommission)
                .sum();

        long nbSuspectes = toutesTransactions.stream()
                .filter(t -> t.getMontantTotal() > SEUIL_SUSPECT)
                .count();

        lblVolumeTotal.setText(String.format("%.2f TND", volumeTotal));
        lblCommissionsTotal.setText(String.format("%.2f TND", commissionsTotal));
        lblNbTransactions.setText(String.valueOf(toutesTransactions.size()));
        lblTransactionsSuspectes.setText(String.valueOf(nbSuspectes));
    }

    // ─────────────────────────────────────────────────────────
    // FILTRES
    // ─────────────────────────────────────────────────────────

    @FXML
    private void appliquerFiltres(ActionEvent event) {
        transactionsFiltrees = new ArrayList<>(toutesTransactions);

        // Filtre Type
        String type = cbFiltreType.getValue();
        if (type != null && !"Tous".equals(type)) {
            transactionsFiltrees = transactionsFiltrees.stream()
                    .filter(t -> t.getTypeTransaction().equals(type))
                    .collect(Collectors.toList());
        }

        // Filtre Action
        String action = cbFiltreAction.getValue();
        if (action != null && !"Toutes".equals(action)) {
            transactionsFiltrees = transactionsFiltrees.stream()
                    .filter(t -> t.getSymbole().equals(action))
                    .collect(Collectors.toList());
        }

        // Filtre Montant Min
        String montantMinStr = tfMontantMin.getText().trim();
        if (!montantMinStr.isEmpty()) {
            try {
                double min = Double.parseDouble(montantMinStr);
                transactionsFiltrees = transactionsFiltrees.stream()
                        .filter(t -> t.getMontantTotal() >= min)
                        .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
            }
        }

        // Filtre Montant Max
        String montantMaxStr = tfMontantMax.getText().trim();
        if (!montantMaxStr.isEmpty()) {
            try {
                double max = Double.parseDouble(montantMaxStr);
                transactionsFiltrees = transactionsFiltrees.stream()
                        .filter(t -> t.getMontantTotal() <= max)
                        .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
            }
        }

        // Filtre Date Début
        LocalDate dateDebut = dpDateDebut.getValue();
        if (dateDebut != null) {
            Date debut = Date.from(dateDebut.atStartOfDay(ZoneId.systemDefault()).toInstant());
            transactionsFiltrees = transactionsFiltrees.stream()
                    .filter(t -> t.getDateTransaction() != null && !t.getDateTransaction().before(debut))
                    .collect(Collectors.toList());
        }

        // Filtre Date Fin
        LocalDate dateFin = dpDateFin.getValue();
        if (dateFin != null) {
            Date fin = Date.from(dateFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            transactionsFiltrees = transactionsFiltrees.stream()
                    .filter(t -> t.getDateTransaction() != null && t.getDateTransaction().before(fin))
                    .collect(Collectors.toList());
        }

        afficherTransactions(transactionsFiltrees);
    }

    @FXML
    private void reinitialiserFiltres(ActionEvent event) {
        cbFiltreType.getSelectionModel().selectFirst();
        cbFiltreAction.getSelectionModel().selectFirst();
        tfMontantMin.clear();
        tfMontantMax.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);

        transactionsFiltrees = new ArrayList<>(toutesTransactions);
        afficherTransactions(transactionsFiltrees);
    }

    // ─────────────────────────────────────────────────────────
    // AFFICHAGE
    // ─────────────────────────────────────────────────────────

    private void afficherTransactions(List<TransactionBourse> transactions) {
        listeContainer.getChildren().clear();
        lblResultatsFiltres.setText("Affichage : " + transactions.size() + " transaction(s)");

        if (transactions.isEmpty()) {
            Label empty = new Label("Aucune transaction à afficher.");
            empty.setStyle("-fx-text-fill: #6b7280; -fx-padding: 40; -fx-font-size: 14px;");
            listeContainer.getChildren().add(empty);
            return;
        }

        for (TransactionBourse t : transactions) {
            listeContainer.getChildren().add(creerLigne(t));
        }
    }

    private HBox creerLigne(TransactionBourse t) {
        HBox row = new HBox();
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: transparent; -fx-border-color: transparent transparent #ffffff0a transparent; -fx-border-width: 0 0 1 0;");

        // Hover
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #ffffff05; -fx-border-color: transparent transparent #ffffff0a transparent; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color: transparent; -fx-border-color: transparent transparent #ffffff0a transparent; -fx-border-width: 0 0 1 0;"));

        boolean isAchat = "ACHAT".equals(t.getTypeTransaction());
        boolean estSuspect = t.getMontantTotal() > SEUIL_SUSPECT;

        // ID
        Label idLabel = new Label("#" + t.getIdTransaction());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-pref-width: 60;");

        // Badge Type
        Label badgeType = new Label(isAchat ? "▲ ACHAT" : "▼ VENTE");
        badgeType.setPrefWidth(80);
        badgeType.setAlignment(Pos.CENTER);
        badgeType.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;" +
                        (isAchat ? "-fx-background-color: #00d4aa22; -fx-text-fill: #00d4aa;"
                                : "-fx-background-color: #ef444422; -fx-text-fill: #ef4444;"));

        // Action
        Label actionLabel = new Label(t.getSymbole());
        actionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-pref-width: 120;");

        // Quantité
        Label qteLabel = new Label(String.valueOf(t.getQuantite()));
        qteLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #e5e7eb; -fx-pref-width: 90; -fx-alignment: CENTER_RIGHT;");

        // Prix unitaire
        Label prixLabel = new Label(String.format("%.2f", t.getPrixUnitaire()));
        prixLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #e5e7eb; -fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        // Montant
        Label montantLabel = new Label(String.format("%.2f", t.getMontantTotal()));
        montantLabel.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 120; -fx-alignment: CENTER_RIGHT;" +
                        "-fx-text-fill: " + (isAchat ? "#00d4aa" : "#ef4444") + ";");

        // Commission
        Label commLabel = new Label(String.format("%.2f", t.getCommission()));
        commLabel.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #f59e0b; -fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        // Date
        String dateStr = t.getDateTransaction() != null
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(t.getDateTransaction())
                : "—";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-pref-width: 140; -fx-alignment: CENTER_RIGHT;");

        // Alerte
        Label alerteLabel;
        if (estSuspect) {
            alerteLabel = new Label("⚠️ SUSPECT");
            alerteLabel.setStyle(
                    "-fx-background-color: #ef444422; -fx-text-fill: #ef4444;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 20; -fx-padding: 4 8;");
        } else {
            alerteLabel = new Label("✓ OK");
            alerteLabel.setStyle(
                    "-fx-background-color: #10b98122; -fx-text-fill: #10b981;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 20; -fx-padding: 4 8;");
        }
        alerteLabel.setPrefWidth(80);
        alerteLabel.setAlignment(Pos.CENTER);

        row.getChildren().addAll(idLabel, badgeType, actionLabel, qteLabel, prixLabel, montantLabel, commLabel,
                dateLabel, alerteLabel);
        return row;
    }

    // ─────────────────────────────────────────────────────────
    // EXPORT EXCEL
    // ─────────────────────────────────────────────────────────

    @FXML
    private void exporterExcel(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en CSV");
        fc.setInitialFileName("transactions_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Header
                writer.write(
                        "ID,Type,Symbole,Entreprise,Quantite,PrixUnitaire,MontantTotal,Commission,Devise,Date,Suspect\n");

                // Lignes
                for (TransactionBourse t : transactionsFiltrees) {
                    boolean suspect = t.getMontantTotal() > SEUIL_SUSPECT;
                    String dateStr = t.getDateTransaction() != null
                            ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(t.getDateTransaction())
                            : "";

                    writer.write(String.format("%d,%s,%s,\"%s\",%d,%.2f,%.2f,%.2f,%s,%s,%s\n",
                            t.getIdTransaction(),
                            t.getTypeTransaction(),
                            t.getSymbole(),
                            t.getNomEntreprise().replace("\"", "\"\""),
                            t.getQuantite(),
                            t.getPrixUnitaire(),
                            t.getMontantTotal(),
                            t.getCommission(),
                            t.getDevise(),
                            dateStr,
                            suspect ? "OUI" : "NON"));
                }

                showSuccess("✅ Export réussi !\n" + transactionsFiltrees.size() + " transactions exportées.\n\n"
                        + file.getAbsolutePath());

            } catch (Exception ex) {
                showError("Erreur export : " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // RETOUR
    // ─────────────────────────────────────────────────────────

    @FXML
    private void retourDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.goTo(stage, "/bourse/dashboard-admin-view.fxml", "Dashboard Admin");
    }

    // ─────────────────────────────────────────────────────────
    // UTILS
    // ─────────────────────────────────────────────────────────

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}