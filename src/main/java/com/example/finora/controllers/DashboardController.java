package com.example.finora.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import com.example.finora.utils.ThemeManager;
import javafx.application.Platform;
import javafx.scene.Scene;

public class DashboardController {

    @FXML private StackPane contentPane;

    @FXML private Button btnAppelOffre;
    @FXML private Button btnCandidature;

    @FXML
    private void initialize() {

        // Apply theme AFTER scene is ready
        Platform.runLater(() -> {
            Scene scene = contentPane.getScene();   // scene declared HERE
            ThemeManager.apply(scene);
        });

        loadScreen("/com/example/finora/ui/AppelOffreView.fxml");
        setActive(btnAppelOffre);
    }


    @FXML
    private void toggleTheme() {
        ThemeManager.toggle();
        ThemeManager.apply(contentPane.getScene()); // no "scene" variable here
    }



    @FXML
    private void goAppelOffre() {
        loadScreen("/com/example/finora/ui/AppelOffreView.fxml");
        setActive(btnAppelOffre);
    }

    @FXML
    private void goCandidature() {
        loadScreen("/com/example/finora/ui/CandidatureView.fxml");
        setActive(btnCandidature);
    }

    private void loadScreen(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  This method switches active style
    private void setActive(Button activeBtn) {

        // remove active style from both
        btnAppelOffre.getStyleClass().remove("nav-btn-active");
        btnCandidature.getStyleClass().remove("nav-btn-active");

        // add active style to selected button
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }
}
