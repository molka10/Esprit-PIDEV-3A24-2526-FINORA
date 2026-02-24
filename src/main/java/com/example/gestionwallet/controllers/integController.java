package com.example.gestionwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    private void changeScene(String fxmlPath, Button button){
        try{
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) button.getScene().getWindow();
            stage.getScene().setRoot(root);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    private void goToUser() {
        changeScene("/com/example/gestionwallet/user.fxml", userButton);
    }

    @FXML
    private void goToEntreprise() {
        changeScene("/com/example/gestionwallet/entreprise.fxml", entrepriseButton);
    }

    @FXML
    private void goToAdmin() {
        changeScene("/com/example/gestionwallet/admin-dashboard.fxml", adminButton);
    }

}