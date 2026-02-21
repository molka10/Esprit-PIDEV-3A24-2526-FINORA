package com.example.gestionwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class integController {

    @FXML
    private Button userButton;

    @FXML
    private Button entrepriseButton;

    @FXML
    private Button adminButton;

    @FXML
    private void initialize() {
        System.out.println("Page Wallet chargée");
    }

    @FXML
    private void goToUser() {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource(
                            "/com/example/gestionwallet/wallet.fxml"
                    )
            );

            Stage stage = (Stage) userButton.getScene().getWindow();
            stage.getScene().setRoot(root);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToEntreprise() {
        System.out.println("Entreprise clicked");
    }


    @FXML
    private void goToAdmin() {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource(
                            "/com/example/gestionwallet/admin-dashboard.fxml"
                    )
            );

            Stage stage = (Stage) adminButton.getScene().getWindow();
            stage.getScene().setRoot(root);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}