package com.example.crud.controllers;

import com.example.crud.models.Bourse;
import com.example.crud.services.ServiceBourse;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeSet;

public class BourseController implements Initializable {

    @FXML private TextField tfId;
    @FXML private TextField tfNom;
    @FXML private ComboBox<String> cbPays;
    @FXML private ComboBox<String> cbDevise;
    @FXML private ComboBox<String> cbStatut;

    @FXML private TextField searchField;
    @FXML private Label lblTotal;

    @FXML private FlowPane cardsContainer;

    private final ServiceBourse service = new ServiceBourse();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        TreeSet<String> paysSet = new TreeSet<>();
        for (String code : Locale.getISOCountries()) {
            Locale locale = new Locale("", code);
            String nomPays = locale.getDisplayCountry(Locale.FRENCH);
            if (nomPays != null && !nomPays.isBlank()) paysSet.add(nomPays);
        }
        cbPays.setItems(FXCollections.observableArrayList(paysSet));

        cbDevise.setItems(FXCollections.observableArrayList("TND", "EUR", "USD", "GBP", "JPY", "CHF", "CAD"));
        cbStatut.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));

        cbDevise.getSelectionModel().selectFirst();
        cbStatut.getSelectionModel().selectFirst();

        afficherBourses(null);
    }

    // ======================
    // CRUD
    // ======================
    @FXML
    void ajouterBourse(ActionEvent event) {
        try {
            String nom = tfNom.getText().trim();
            String pays = cbPays.getValue();
            String devise = cbDevise.getValue();
            String statut = cbStatut.getValue();

            if (nom.isEmpty() || pays == null || pays.isBlank() || devise == null || statut == null) {
                showError("Nom, Pays, Devise, Statut sont obligatoires.");
                return;
            }

            service.add(new Bourse(nom, pays, devise, statut));
            showSuccess("✅ Bourse ajoutée !");
            clearFields();
            afficherBourses(null);

        } catch (Exception e) {
            showError("Erreur ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void modifierBourse(ActionEvent event) {
        try {
            String idStr = tfId.getText().trim();
            if (idStr.isEmpty()) { showError("ID obligatoire pour modifier."); return; }
            int id = Integer.parseInt(idStr);

            String nom = tfNom.getText().trim();
            String pays = cbPays.getValue();
            String devise = cbDevise.getValue();
            String statut = cbStatut.getValue();

            if (nom.isEmpty() || pays == null || pays.isBlank() || devise == null || statut == null) {
                showError("Nom, Pays, Devise, Statut sont obligatoires.");
                return;
            }

            Bourse b = new Bourse(id, nom, pays, devise, statut, null);
            service.update(b);

            showSuccess("✏️ Bourse modifiée !");
            clearFields();
            afficherBourses(null);

        } catch (NumberFormatException e) {
            showError("ID invalide !");
        } catch (Exception e) {
            showError("Erreur modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void supprimerBourse(ActionEvent event) {
        try {
            String idStr = tfId.getText().trim();
            if (idStr.isEmpty()) { showError("ID obligatoire pour supprimer."); return; }
            int id = Integer.parseInt(idStr);

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer la bourse ?");
            confirm.setContentText("Voulez-vous supprimer la bourse ID " + id + " ?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            Bourse b = new Bourse();
            b.setIdBourse(id);
            service.delete(b);

            showSuccess("🗑️ Bourse supprimée !");
            clearFields();
            afficherBourses(null);

        } catch (NumberFormatException e) {
            showError("ID invalide !");
        } catch (Exception e) {
            showError("Erreur suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================
    // DISPLAY
    // ======================
    @FXML
    void afficherBourses(ActionEvent event) {
        try {
            var bourses = service.getAll();
            cardsContainer.getChildren().clear();

            if (bourses.isEmpty()) {
                Label empty = new Label("⚠️ Aucune bourse.");
                empty.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:14px;");
                cardsContainer.getChildren().add(empty);
            } else {
                for (Bourse b : bourses) {
                    cardsContainer.getChildren().add(creerCarteBourse(b));
                }
            }

            lblTotal.setText("📋 Total : " + bourses.size() + " bourse(s)");

        } catch (Exception e) {
            showError("Erreur affichage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox creerCarteBourse(Bourse b) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setPrefWidth(320);
        card.setCursor(Cursor.HAND);

        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-color:#dfe6e9;" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
        );

        Label title = new Label("🏦 " + b.getNomBourse());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-text-fill:#2c3e50;");

        Label id = new Label("🆔 ID : " + b.getIdBourse());
        Label pays = new Label("📍 Pays : " + b.getPays());
        Label devise = new Label("💰 Devise : " + b.getDevise());
        Label statut = new Label("📌 Statut : " + b.getStatut());

        Button btnSelect = new Button("Sélectionner");
        btnSelect.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:10;");

        Button btnActions = new Button("Voir actions");
        btnActions.setStyle("-fx-background-color:#16a085; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:10;");

        HBox actions = new HBox(10, btnSelect, btnActions);
        actions.setAlignment(Pos.CENTER_LEFT);

        Runnable select = () -> {
            tfId.setText(String.valueOf(b.getIdBourse()));
            tfNom.setText(b.getNomBourse());
            cbPays.setValue(b.getPays());
            cbDevise.setValue(b.getDevise());
            cbStatut.setValue(b.getStatut());
        };

        card.setOnMouseClicked(e -> select.run());
        btnSelect.setOnAction(e -> select.run());

        btnActions.setOnAction(e -> ouvrirActionsPourBourse(b));

        card.getChildren().addAll(title, id, pays, devise, statut, actions);
        return card;
    }

    private void ouvrirActionsPourBourse(Bourse bourse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/crud/action-view.fxml"));
            Parent root = loader.load();

            ActionController ctrl = loader.getController();

            // ✅ ICI LA CORRECTION
            ctrl.preselectionnerBourseParId(bourse.getIdBourse());

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setTitle("FINORA - Actions de " + bourse.getNomBourse());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur ouverture actions : " + ex.getMessage());
        }
    }

    // ======================
    // SEARCH / RESET
    // ======================
    @FXML
    private void handleRechercher(ActionEvent event) {
        try {
            String keyword = searchField.getText().trim().toLowerCase();
            if (keyword.isEmpty()) { showError("Entrez un mot-clé !"); return; }

            var resultats = service.searchByName(keyword);
            cardsContainer.getChildren().clear();

            for (Bourse b : resultats) {
                cardsContainer.getChildren().add(creerCarteBourse(b));
            }

            lblTotal.setText("📋 Résultats : " + resultats.size() + " bourse(s)");

        } catch (Exception e) {
            showError("Erreur recherche : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        searchField.clear();
        clearFields();
        afficherBourses(null);
    }

    @FXML
    private void viderFormulaire(ActionEvent event) {
        clearFields();
    }

    @FXML
    private void retourProfil(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/crud/utilisateur-static-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("FINORA - Choisir Profil");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur retour : " + e.getMessage());
        }
    }

    private void clearFields() {
        if (tfId != null) tfId.clear();
        if (tfNom != null) tfNom.clear();
        if (cbPays != null) cbPays.getSelectionModel().clearSelection();
        if (cbDevise != null) cbDevise.getSelectionModel().selectFirst();
        if (cbStatut != null) cbStatut.getSelectionModel().selectFirst();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
