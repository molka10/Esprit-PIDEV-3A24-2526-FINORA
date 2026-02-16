package com.example.gestionwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.services.servicetransaction;

public class AdminController {

    @FXML private VBox transactionContainer;
    @FXML private VBox walletContainer;

    private servicetransaction st = new servicetransaction();

    @FXML
    public void initialize() {
        loadTransactions();
        loadWallet();
    }

    private void loadTransactions() {

        transactionContainer.getChildren().clear();

        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setSpacing(0);
        header.setStyle("-fx-background-color:#8e44ad; -fx-background-radius:10;");

        header.getChildren().addAll(
                createHeaderLabel("User", 120),
                createHeaderLabel("Type", 120),
                createHeaderLabel("Montant", 150),
                createHeaderLabel("Date", 180)
        );

        transactionContainer.getChildren().add(header);

        for (transaction t : st.afficher()) {

            HBox row = new HBox();
            row.setPadding(new Insets(10));
            row.setSpacing(0);
            row.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");

            row.getChildren().addAll(
                    createCell("User", 120),
                    createCell(t.getType(), 120),
                    createMontantCell(t.getMontant(), 150),
                    createCell(t.getDate_transaction().toString(), 180)
            );

            transactionContainer.getChildren().add(row);
        }
    }

    private void loadWallet() {

        walletContainer.getChildren().clear();

        double total = 0;

        for (transaction t : st.afficher()) {
            total += t.getMontant();
        }

        HBox row = new HBox();
        row.setPadding(new Insets(15));
        row.setSpacing(40);
        row.setStyle("-fx-background-color:white; -fx-background-radius:10; "
                + "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.1), 10,0,0,3);");

        Label user = new Label("User");
        user.setStyle("-fx-text-fill:#6a0dad;");
        user.setPrefWidth(120);

        Label role = new Label("Utilisateur");
        role.setStyle("-fx-text-fill:#6a0dad;");
        role.setPrefWidth(150);

        Label solde = new Label(total + " DT");
        solde.setPrefWidth(150);

        if (total >= 0) {
            solde.setStyle("-fx-text-fill:#2ecc71; -fx-font-weight:bold;"); // vert
        } else {
            solde.setStyle("-fx-text-fill:#e74c3c; -fx-font-weight:bold;"); // rouge
        }


        row.getChildren().addAll(user, role, solde);

        walletContainer.getChildren().add(row);
    }

    private Label createHeaderLabel(String text, double width) {

        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-text-fill:white; -fx-font-weight:bold;");
        return label;
    }

    private Label createCell(String text, double width) {

        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-text-fill:#4b0082;");
        return label;
    }

    private Label createMontantCell(double montant, double width) {

        Label label = new Label(montant + " DT");
        label.setPrefWidth(width);

        if (montant >= 0) {
            label.setStyle("-fx-text-fill:#2ecc71; -fx-font-weight:bold;"); // vert INCOME
        } else {
            label.setStyle("-fx-text-fill:#e74c3c; -fx-font-weight:bold;"); // rouge OUTCOME
        }

        return label;
    }

}
