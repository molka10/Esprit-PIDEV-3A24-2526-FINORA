package com.example.gestionwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

import com.example.gestionwallet.models.categorie;
import com.example.gestionwallet.services.servicecategorie;

import java.util.List;

public class AddCategoryController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> priorityBox;
    @FXML private VBox categoryListBox;

    private servicecategorie sc = new servicecategorie();
    private String categoryType;
    private int editingId = -1;
    private String role;

    public void setRole(String role) {
        this.role = role;
    }

    @FXML
    public void initialize() {

        priorityBox.getItems().addAll("HAUTE", "MOYENNE", "BASSE");

        // Style blanc + violet
        nameField.setStyle("-fx-background-color:#f3f0fa; -fx-text-fill:#4b0082; -fx-background-radius:10;");
        priorityBox.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");
    }


    public void setCategoryType(String type) {
        this.categoryType = type;
        loadCategories();
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void saveCategory() {

        String name = nameField.getText();
        String priority = priorityBox.getValue();

        if (name == null || name.trim().isEmpty()) {
            showError("Le nom est obligatoire.");
            return;
        }

        if (!name.matches("[a-zA-Z ]+")) {
            showError("Le nom doit contenir uniquement des lettres.");
            return;
        }

        if (priority == null) {
            showError("Veuillez choisir une priorité.");
            return;
        }

        if (editingId == -1) {

            categorie c = new categorie(name, priority, categoryType, role);
            sc.ajouter(c);

        } else {

            categorie c = new categorie(editingId, name, priority, categoryType, role);
            sc.modifier(c);
            editingId = -1;
        }

        nameField.clear();
        priorityBox.setValue(null);

        loadCategories();
    }

    private void loadCategories() {

        categoryListBox.getChildren().clear();

        GridPane table = new GridPane();
        table.setHgap(40);
        table.setVgap(15);
        table.setPadding(new Insets(10));
        table.setStyle("-fx-background-color:white; -fx-background-radius:15; "
                + "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.1), 10,0,0,3);");

        Label headerNom = new Label("Nom Catégorie");
        headerNom.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");

        Label headerPriorite = new Label("Priorité");
        headerPriorite.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");

        Label headerAction = new Label("Actions");
        headerAction.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");

        table.add(headerNom, 0, 0);
        table.add(headerPriorite, 1, 0);
        table.add(headerAction, 2, 0);

        int row = 1;

        List<categorie> list = sc.afficher();

        for (categorie c : list) {

            if (!c.getType().equals(categoryType)) continue;

            Label nomLabel = new Label(c.getNom());
            nomLabel.setStyle("-fx-text-fill:#4b0082;");

            Label prioriteLabel = new Label(c.getPriorite());
            prioriteLabel.setStyle(getPriorityStyle(c.getPriorite()));

            Button editBtn = new Button("Modifier");
            editBtn.setStyle("-fx-background-color:#dcd6f7; -fx-text-fill:#6a0dad; -fx-background-radius:8;");

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color:#cdb4f6; -fx-text-fill:#4b0082; -fx-background-radius:8;");

            HBox actions = new HBox(10, editBtn, deleteBtn);

            table.add(nomLabel, 0, row);
            table.add(prioriteLabel, 1, row);
            table.add(actions, 2, row);

            deleteBtn.setOnAction(e -> {
                sc.supprimer(c.getId_category());
                loadCategories();
            });

            editBtn.setOnAction(e -> {
                nameField.setText(c.getNom());
                priorityBox.setValue(c.getPriorite());
                editingId = c.getId_category();
            });

            row++;
        }

        categoryListBox.getChildren().add(table);
    }

    private String getPriorityStyle(String priority) {

        switch (priority) {
            case "HAUTE":
                return "-fx-text-fill:#2ecc71; -fx-font-weight:bold;"; // vert
            case "MOYENNE":
                return "-fx-text-fill:#f39c12; -fx-font-weight:bold;"; // orange
            case "BASSE":
                return "-fx-text-fill:#e74c3c; -fx-font-weight:bold;"; // rouge
            default:
                return "-fx-text-fill:#4b0082;";
        }
    }

}
