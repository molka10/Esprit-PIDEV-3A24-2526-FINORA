package com.example.finora.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;

import com.example.finora.entities.transaction;
import com.example.finora.entities.categorie;
import com.example.finora.services.servicetransaction;
import com.example.finora.services.servicecategorie;



public class AddTransactionController {

    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryBox;
    @FXML private Label typeLabel;

    private Object parentController;
    private String currentType;
    private servicetransaction st = new servicetransaction();
    private servicecategorie sc = new servicecategorie();
    private int currentUserId;


    public void setParentController(Object controller) {
        this.parentController = controller;

    }
    public void setUserId(int id){
        this.currentUserId = id;
    }
    @FXML
    public void initialize() {

        nameField.setStyle("-fx-background-color:#f3f0fa; -fx-text-fill:#4b0082; -fx-background-radius:10;");
        amountField.setStyle("-fx-background-color:#f3f0fa; -fx-text-fill:#4b0082; -fx-background-radius:10;");
        datePicker.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");
        categoryBox.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate firstDay = today.withDayOfMonth(1);
                java.time.LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

                if (empty || date.isBefore(firstDay) || date.isAfter(lastDay)) {
                    setDisable(true);
                    setStyle("-fx-background-color:#eeeeee;");
                }
            }
        });
    }



    public void setType(String type) {

        this.currentType = type;

        typeLabel.setText(type);
        typeLabel.setStyle("-fx-text-fill:#8e44ad; -fx-font-weight:bold; -fx-font-size:16;");

        loadCategoriesByType(type);
    }

    private void loadCategoriesByType(String type) {

        categoryBox.getItems().clear();

        List<categorie> list = sc.afficher();

        for (categorie c : list) {

            if (c.getType().equalsIgnoreCase(type)) {
                categoryBox.getItems().add(c.getNom());
            }
        }
    }
    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void saveTransaction() {

        String name = nameField.getText();
        String amountText = amountField.getText();
        String categoryName = categoryBox.getValue();

        if (name == null || name.trim().isEmpty()) {
            showError("Le nom est obligatoire.");
            return;
        }

        if (!name.matches("[a-zA-Z ]+")) {
            showError("Le nom doit contenir uniquement des lettres.");
            return;
        }

        if (amountText == null || amountText.trim().isEmpty()) {
            showError("Le montant est obligatoire.");
            return;
        }

        if (!amountText.matches("\\d+(\\.\\d+)?")) {
            showError("Le montant doit contenir uniquement des chiffres.");
            return;
        }

        double amount = Double.parseDouble(amountText);

        if (datePicker.getValue() == null) {
            showError("La date est obligatoire.");
            return;
        }

        if (categoryName == null) {
            showError("Veuillez choisir une catégorie.");
            return;
        }

        try {

            String type = currentType;

            if (type.equals("OUTCOME")) {
                amount = -Math.abs(amount);
            }

            List<categorie> list = sc.afficher();

            int categoryId = -1;

            for (categorie c : list) {
                if (c.getNom().equals(categoryName)
                        && c.getType().equalsIgnoreCase(currentType)) {
                    categoryId = c.getId_category();
                    break;
                }
            }

            transaction t = new transaction(
                    name,
                    type,
                    amount,
                    java.sql.Date.valueOf(datePicker.getValue()),
                    "MANUAL",
                    currentUserId,
                    categoryId

            );

            st.ajouter(t);

            if (parentController instanceof UserController) {
                ((UserController) parentController).loadTransactions();
            }
            if (parentController instanceof EntrepriseController) {
                ((EntrepriseController) parentController).loadTransactions();
            }

            ((Stage) nameField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAddCategory() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/finora/add-category.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            AddCategoryController controller = loader.getController();


            controller.setCategoryType(currentType);

            stage.showAndWait();

            loadCategoriesByType(currentType);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
