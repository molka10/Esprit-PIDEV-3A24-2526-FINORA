package com.example.gestionwallet.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.Parent;



import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.services.servicetransaction;

public class WalletController {

    @FXML private VBox outcomeBox;
    @FXML private VBox incomeBox;
    @FXML private Label balanceLabel;

    private double balance = 0;
    private VBox outcomeList;
    private VBox incomeList;

    private servicetransaction st = new servicetransaction();

    @FXML
    public void initialize() {

        VBox outcomeCard = createCard("Outcome", false);
        VBox incomeCard = createCard("Income", true);

        outcomeBox.getChildren().add(outcomeCard);
        incomeBox.getChildren().add(incomeCard);

        loadTransactions();
    }

    private VBox createCard(String title, boolean isIncome) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#6a0dad;");

        Button addBtn = new Button("+ Add");
        addBtn.setStyle("-fx-background-color:#8e44ad; -fx-text-fill:white; -fx-background-radius:10;");
        addBtn.setOnAction(e -> openAddTransaction(isIncome));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, titleLabel, spacer, addBtn);

        VBox list = new VBox(10);
        list.setPadding(new Insets(10));

        if (isIncome) incomeList = list;
        else outcomeList = list;

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.setStyle("-fx-background:white;");

        VBox card = new VBox(15, header, scrollPane);
        card.setPadding(new Insets(20));
        card.setPrefWidth(420);
        card.setStyle("-fx-background-color:white; -fx-background-radius:20; "
                + "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.1), 20,0,0,5);");

        return card;
    }

    private void openAddTransaction(boolean isIncome) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gestionwallet/add-transaction.fxml")
            );

            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());

            AddTransactionController controller = loader.getController();
            controller.setType(isIncome ? "INCOME" : "OUTCOME");
            controller.setParentController(this);

            stage.setScene(scene);
            stage.setTitle("Add Transaction");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadTransactions() {

        incomeList.getChildren().clear();
        outcomeList.getChildren().clear();
        balance = 0;

        for (transaction t : st.afficher()) {
            addTransaction(t);
        }

        updateBalanceLabel();
    }

    private void addTransaction(transaction t) {

        HBox item = new HBox(10);
        item.setPrefWidth(500);
        item.setMinHeight(60);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");

        Label nameLabel = new Label(t.getNom_transaction());
        nameLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#4b0082;");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(250);

        Label amountLabel = new Label(t.getMontant() + " DT");
        amountLabel.setStyle("-fx-text-fill:#8e44ad; -fx-font-weight:bold;");

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color:#dcd6f7; -fx-text-fill:#6a0dad; -fx-background-radius:8;");

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color:#cdb4f6; -fx-text-fill:#4b0082; -fx-background-radius:8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        item.getChildren().addAll(nameLabel, spacer, amountLabel, editBtn, deleteBtn);

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Supprimer transaction ?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    st.supprimer(t.getId_transaction());
                    loadTransactions();
                }
            });
        });

        editBtn.setOnAction(e -> {

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Modifier Transaction");

            VBox card = new VBox(20);
            card.setPadding(new Insets(30));
            card.setStyle("-fx-background-color:white; -fx-background-radius:20;");

            Label title = new Label("Modifier Transaction");
            title.setStyle("-fx-text-fill:#6a0dad; -fx-font-size:18; -fx-font-weight:bold;");

            Label nameLabelPopup = new Label("Nom:");
            nameLabelPopup.setStyle("-fx-text-fill:#4b0082;");

            TextField nameField = new TextField(t.getNom_transaction());
            nameField.setStyle("-fx-background-color:#f3f0fa; -fx-text-fill:#4b0082; -fx-background-radius:10;");

            Label amountLabelPopup = new Label("Montant:");
            amountLabelPopup.setStyle("-fx-text-fill:#4b0082;");

            TextField amountField = new TextField(String.valueOf(Math.abs(t.getMontant())));
            amountField.setStyle("-fx-background-color:#f3f0fa; -fx-text-fill:#4b0082; -fx-background-radius:10;");

            Button saveBtn = new Button("Enregistrer");
            saveBtn.setStyle("-fx-background-color:#8e44ad; -fx-text-fill:white; -fx-background-radius:10;");

            Button cancelBtn = new Button("Annuler");
            cancelBtn.setStyle("-fx-background-color:#e8e3f8; -fx-text-fill:#6a0dad; -fx-background-radius:10;");

            HBox buttons = new HBox(15, cancelBtn, saveBtn);

            card.getChildren().addAll(
                    title,
                    nameLabelPopup, nameField,
                    amountLabelPopup, amountField,
                    buttons
            );

            StackPane root = new StackPane(card);
            root.setStyle("-fx-background-color:#f4f4f8;");

            Scene scene = new Scene(root, 400, 300);
            dialogStage.setScene(scene);

            saveBtn.setOnAction(ev -> {

                String newName = nameField.getText();
                String amountText = amountField.getText();

                if (newName == null || newName.trim().isEmpty()) {
                    showError("Le nom est obligatoire.");
                    return;
                }

                if (!newName.matches("[a-zA-Z ]+")) {
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

                double newAmount = Double.parseDouble(amountText);

                if (t.getType().equals("OUTCOME")) {
                    newAmount = -Math.abs(newAmount);
                }

                t.setNom_transaction(newName);
                t.setMontant(newAmount);

                st.modifier(t);
                loadTransactions();
                dialogStage.close();
            });

            cancelBtn.setOnAction(ev -> dialogStage.close());
            dialogStage.showAndWait();
        });

        if (t.getType().equals("INCOME"))
            incomeList.getChildren().add(item);
        else
            outcomeList.getChildren().add(item);

        balance += t.getMontant();
    }

    private void updateBalanceLabel() {

        balanceLabel.setText("Balance: " + balance + " DT");

        if (balance >= 0) {
            balanceLabel.setStyle("-fx-font-size:26; -fx-font-weight:bold; -fx-text-fill:#2ecc71;");
        } else {
            balanceLabel.setStyle("-fx-font-size:26; -fx-font-weight:bold; -fx-text-fill:#e74c3c;");
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
    private void openAdminPage() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gestionwallet/admin-dashboard.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Admin Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void goToAnalyse() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestionwallet/analyse.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) balanceLabel.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
