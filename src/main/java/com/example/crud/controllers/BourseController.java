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
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.TreeSet;

/**
 * 🎨 BourseController - Version Moderne et Améliorée
 *
 * Améliorations :
 * - ✅ ID masqué (utilisé uniquement en interne)
 * - ✅ Mini-graphiques de tendance
 * - ✅ Design moderne type "trading app"
 * - ✅ Badges de statut colorés
 * - ✅ Interface allégée et élégante
 */
public class BourseController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private ComboBox<String> cbPays;
    @FXML private ComboBox<String> cbDevise;
    @FXML private ComboBox<String> cbStatut;

    @FXML private TextField searchField;
    @FXML private Label lblTotal;

    @FXML private FlowPane cardsContainer;

    private final ServiceBourse service = new ServiceBourse();

    // ID sélectionné (caché de l'utilisateur)
    private Integer selectedBourseId = null;

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
            if (selectedBourseId == null) {
                showError("Veuillez sélectionner une bourse à modifier.");
                return;
            }

            String nom = tfNom.getText().trim();
            String pays = cbPays.getValue();
            String devise = cbDevise.getValue();
            String statut = cbStatut.getValue();

            if (nom.isEmpty() || pays == null || pays.isBlank() || devise == null || statut == null) {
                showError("Nom, Pays, Devise, Statut sont obligatoires.");
                return;
            }

            Bourse b = new Bourse(selectedBourseId, nom, pays, devise, statut, null);
            service.update(b);

            showSuccess("✏️ Bourse modifiée !");
            clearFields();
            afficherBourses(null);

        } catch (Exception e) {
            showError("Erreur modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void supprimerBourse(ActionEvent event) {
        try {
            if (selectedBourseId == null) {
                showError("Veuillez sélectionner une bourse à supprimer.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer la bourse ?");
            confirm.setContentText("Cette action est irréversible.");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            Bourse b = new Bourse();
            b.setIdBourse(selectedBourseId);
            service.delete(b);

            showSuccess("🗑️ Bourse supprimée !");
            clearFields();
            afficherBourses(null);

        } catch (Exception e) {
            showError("Erreur suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================
    // DISPLAY - VERSION MODERNE
    // ======================
    @FXML
    void afficherBourses(ActionEvent event) {
        try {
            var bourses = service.getAll();
            cardsContainer.getChildren().clear();

            if (bourses.isEmpty()) {
                Label empty = new Label("⚠️ Aucune bourse.");
                empty.setStyle("-fx-text-fill:#9ca3af; -fx-font-size:14px;");
                cardsContainer.getChildren().add(empty);
            } else {
                for (Bourse b : bourses) {
                    cardsContainer.getChildren().add(creerCarteModerne(b));
                }
            }

            lblTotal.setText("📊 Total : " + bourses.size() + " bourse(s)");

        } catch (Exception e) {
            showError("Erreur affichage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🎨 Créer une carte moderne style "Trading App"
     * - ID masqué
     * - Mini-graphique de tendance
     * - Badge de statut coloré
     * - Design épuré
     */
    private VBox creerCarteModerne(Bourse b) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setPrefWidth(280);
        card.setCursor(Cursor.HAND);

        // Style moderne avec dégradé subtil
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f9fafb);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 2);"
        );

        // === HEADER: Nom + Badge Statut ===
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nomLabel = new Label(b.getNomBourse());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1f2937;");

        // Badge de statut coloré
        Label badgeStatut = creerBadgeStatut(b.getStatut());

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(nomLabel, spacer, badgeStatut);

        // === INFORMATIONS PRINCIPALES ===
        VBox infos = new VBox(6);

        // Pays avec emoji drapeau
        Label paysLabel = new Label("📍 " + b.getPays());
        paysLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        // Devise avec emoji monnaie
        Label deviseLabel = new Label("💰 " + b.getDevise());
        deviseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        infos.getChildren().addAll(paysLabel, deviseLabel);

        // === MINI GRAPHIQUE DE TENDANCE ===
        Path miniGraph = creerMiniGraphique();

        // === BOUTONS D'ACTION ===
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Button btnSelect = new Button("Sélectionner");
        btnSelect.setStyle(
                "-fx-background-color: #6366f1;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        Button btnActions = new Button("Actions");
        btnActions.setStyle(
                "-fx-background-color: #10b981;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        actions.getChildren().addAll(btnSelect, btnActions);

        // === ÉVÉNEMENTS ===
        Runnable select = () -> {
            // Stocker l'ID en interne (invisible pour l'utilisateur)
            selectedBourseId = b.getIdBourse();

            // Remplir le formulaire
            tfNom.setText(b.getNomBourse());
            cbPays.setValue(b.getPays());
            cbDevise.setValue(b.getDevise());
            cbStatut.setValue(b.getStatut());

            // Feedback visuel
            showInfo("Bourse sélectionnée : " + b.getNomBourse());
        };

        card.setOnMouseClicked(e -> select.run());
        btnSelect.setOnAction(e -> select.run());
        btnActions.setOnAction(e -> ouvrirActionsPourBourse(b));

        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #f9fafb, #f3f4f6);" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-color: #6366f1;" +
                            "-fx-border-width: 2;" +
                            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 15, 0, 0, 3);"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ffffff, #f9fafb);" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-color: #e5e7eb;" +
                            "-fx-border-width: 1;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 2);"
            );
        });

        card.getChildren().addAll(header, infos, miniGraph, actions);
        return card;
    }

    /**
     * 🎨 Créer un badge de statut coloré
     */
    private Label creerBadgeStatut(String statut) {
        Label badge = new Label(statut);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        (statut.equals("ACTIVE")
                                ? "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;"
                                : "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;")
        );
        return badge;
    }

    /**
     * 📈 Créer un mini-graphique de tendance (simulation)
     */
    private Path creerMiniGraphique() {
        Path path = new Path();
        Random rand = new Random();

        // Point de départ
        double startY = 20 + rand.nextDouble() * 10;
        path.getElements().add(new MoveTo(0, startY));

        // Générer 8 points aléatoires pour simuler une tendance
        for (int i = 1; i <= 8; i++) {
            double x = i * 30;
            double y = 15 + rand.nextDouble() * 15;
            path.getElements().add(new LineTo(x, y));
        }

        // Style du graphique
        path.setStroke(Color.web("#6366f1"));
        path.setStrokeWidth(2);
        path.setFill(null);

        // Conteneur avec hauteur fixe
        VBox container = new VBox(path);
        container.setPrefHeight(40);
        container.setAlignment(Pos.CENTER);

        return path;
    }

    private void ouvrirActionsPourBourse(Bourse bourse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/crud/action-view.fxml"));
            Parent root = loader.load();

            ActionController ctrl = loader.getController();
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
                cardsContainer.getChildren().add(creerCarteModerne(b));
            }

            lblTotal.setText("🔍 Résultats : " + resultats.size() + " bourse(s)");

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
        selectedBourseId = null; // Réinitialiser l'ID sélectionné
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

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}