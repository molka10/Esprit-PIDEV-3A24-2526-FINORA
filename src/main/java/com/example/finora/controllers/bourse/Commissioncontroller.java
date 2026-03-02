package com.example.finora.controllers.bourse;

import com.example.finora.entities.Commission;
import com.example.finora.services.bourse.Servicecommission;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 💰 CommissionController
 * Interface admin pour gérer les taux de commission
 */
public class Commissioncontroller implements Initializable {

    @FXML
    private Label lblTauxActuel;
    @FXML
    private Label lblRevenusTotaux;
    @FXML
    private Label lblRevenusMois;
    @FXML
    private VBox listeContainer;

    private final Servicecommission serviceCommission = new Servicecommission();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerDonnees();
    }

    // ─────────────────────────────────────────────────────────
    // CHARGEMENT
    // ─────────────────────────────────────────────────────────

    private void chargerDonnees() {
        Task<List<Commission>> task = new Task<>() {
            @Override
            protected List<Commission> call() {
                return serviceCommission.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            List<Commission> commissions = task.getValue();
            afficherListe(commissions);
            mettreAJourStats();
        });

        task.setOnFailed(e -> System.err.println("Erreur : " + task.getException()));
        executor.submit(task);
    }

    private void mettreAJourStats() {
        Commission active = serviceCommission.getCommissionActive("ACHAT");
        lblTauxActuel.setText(active.getTauxPourcentage() + "%");
        lblRevenusTotaux.setText(String.format("%.2f TND", serviceCommission.getRevenusTotaux()));
        lblRevenusMois.setText(String.format("%.2f TND", serviceCommission.getRevenusMoisActuel()));
    }

    // ─────────────────────────────────────────────────────────
    // AFFICHAGE
    // ─────────────────────────────────────────────────────────

    private void afficherListe(List<Commission> commissions) {
        listeContainer.getChildren().clear();

        if (commissions.isEmpty()) {
            Label empty = new Label("Aucune commission configurée.");
            empty.setStyle("-fx-text-fill: #6b7280; -fx-padding: 40; -fx-font-size: 14px;");
            listeContainer.getChildren().add(empty);
            return;
        }

        for (Commission c : commissions) {
            listeContainer.getChildren().add(creerLigne(c));
        }
    }

    private HBox creerLigne(Commission c) {
        HBox row = new HBox();
        row.setPadding(new Insets(14, 20, 14, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent transparent #ffffff0a transparent;" +
                        "-fx-border-width: 0 0 1 0;");

        // Hover
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #ffffff05;" +
                        "-fx-border-color: transparent transparent #ffffff0a transparent;" +
                        "-fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent transparent #ffffff0a transparent;" +
                        "-fx-border-width: 0 0 1 0;"));

        // Nom
        Label nomLabel = new Label(c.getNom());
        nomLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-pref-width: 250;");

        // Type
        Label typeLabel = new Label(c.getTypeTransaction());
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-pref-width: 150;");

        // Taux
        Label tauxLabel = new Label(c.getTauxPourcentage() + "%");
        tauxLabel.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00d4aa; -fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        // Badge statut
        Label badgeStatut = new Label(c.isActive() ? "✓ ACTIVE" : "✗ INACTIVE");
        badgeStatut.setPrefWidth(100);
        badgeStatut.setAlignment(Pos.CENTER);
        badgeStatut.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;" +
                        (c.isActive()
                                ? "-fx-background-color: #00d4aa22; -fx-text-fill: #00d4aa;"
                                : "-fx-background-color: #ef444422; -fx-text-fill: #ef4444;"));

        // Date modification
        String dateStr = c.getDateModification() != null
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(c.getDateModification())
                : "—";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        HBox.setHgrow(dateLabel, Priority.ALWAYS);

        // Boutons
        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> ouvrirDialogModifier(c));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #ef4444; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerCommission(c));

        HBox btnBox = new HBox(8, btnModifier, btnSupprimer);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPrefWidth(200);

        row.getChildren().addAll(nomLabel, typeLabel, tauxLabel, badgeStatut, dateLabel, btnBox);
        return row;
    }

    // ─────────────────────────────────────────────────────────
    // DIALOG AJOUT / MODIFICATION
    // ─────────────────────────────────────────────────────────

    @FXML
    private void ouvrirDialogAjout(ActionEvent event) {
        ouvrirDialog(null);
    }

    private void ouvrirDialogModifier(Commission commission) {
        ouvrirDialog(commission);
    }

    @FXML
    private void ouvrirSupervision(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.goTo(stage, "/bourse/supervision-view.fxml", "FINORA - Supervision");
    }

    private void ouvrirDialog(Commission commissionAModifier) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(commissionAModifier == null ? "➕ Nouvelle Commission" : "✏️ Modifier Commission");
        dialog.setHeaderText(commissionAModifier == null
                ? "Configurer une nouvelle commission"
                : "Modifier la commission " + commissionAModifier.getNom());

        ButtonType btnSave = new ButtonType(
                commissionAModifier == null ? "Ajouter" : "Modifier",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(480);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));

        // Champs
        TextField tfNom = new TextField();
        tfNom.setPromptText("Ex: Commission Standard");
        tfNom.setPrefWidth(320);

        ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList(
                "LES_DEUX", "ACHAT", "VENTE"));
        cbType.setPrefWidth(320);

        TextField tfTaux = new TextField();
        tfTaux.setPromptText("Ex: 0.5 pour 0.5%");
        tfTaux.setPrefWidth(320);

        CheckBox chkActive = new CheckBox("Active");
        chkActive.setSelected(true);

        // Pré-remplir si modification
        if (commissionAModifier != null) {
            tfNom.setText(commissionAModifier.getNom());
            cbType.setValue(commissionAModifier.getTypeTransaction());
            tfTaux.setText(String.valueOf(commissionAModifier.getTauxPourcentage()));
            chkActive.setSelected(commissionAModifier.isActive());
        } else {
            cbType.getSelectionModel().selectFirst();
        }

        int row = 0;
        grid.add(new Label("Nom *"), 0, row);
        grid.add(tfNom, 1, row++);
        grid.add(new Label("Type *"), 0, row);
        grid.add(cbType, 1, row++);
        grid.add(new Label("Taux (%) *"), 0, row);
        grid.add(tfTaux, 1, row++);
        grid.add(new Label("Statut"), 0, row);
        grid.add(chkActive, 1, row);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response != btnSave)
                return;

            String nom = tfNom.getText().trim();
            String type = cbType.getValue();
            String tauxStr = tfTaux.getText().trim();

            if (nom.isEmpty()) {
                showError("Le nom est obligatoire !");
                return;
            }
            if (type == null) {
                showError("Le type est obligatoire !");
                return;
            }
            if (tauxStr.isEmpty()) {
                showError("Le taux est obligatoire !");
                return;
            }

            try {
                double taux = Double.parseDouble(tauxStr);

                if (commissionAModifier == null) {
                    Commission newC = new Commission(nom, type, taux);
                    newC.setActive(chkActive.isSelected());
                    serviceCommission.add(newC);
                    showSuccess("✅ Commission ajoutée !");
                } else {
                    commissionAModifier.setNom(nom);
                    commissionAModifier.setTypeTransaction(type);
                    commissionAModifier.setTauxPourcentage(taux);
                    commissionAModifier.setActive(chkActive.isSelected());
                    serviceCommission.update(commissionAModifier);
                    showSuccess("✏️ Commission modifiée !");
                }

                chargerDonnees();

            } catch (NumberFormatException ex) {
                showError("Le taux doit être un nombre valide !");
            } catch (Exception ex) {
                showError("Erreur : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    // SUPPRIMER
    // ─────────────────────────────────────────────────────────

    private void supprimerCommission(Commission c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + c.getNom() + " ?");
        confirm.setContentText("Cette action est irréversible !");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                serviceCommission.delete(c);
                showSuccess("🗑️ Commission supprimée !");
                chargerDonnees();
            } catch (Exception ex) {
                showError("Erreur : " + ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // ACTUALISER
    // ─────────────────────────────────────────────────────────

    @FXML
    private void handleActualiser(ActionEvent event) {
        chargerDonnees();
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