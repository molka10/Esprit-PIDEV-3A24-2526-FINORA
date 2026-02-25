package com.example.finora.finorainves;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void goTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/tn/finora/finorainves/ui/" + fxml)
            );

            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 600);

            scene.getStylesheets().add(
                    SceneNavigator.class.getResource("/com/finora/finorainves/styles/style.css").toExternalForm()
            );

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            showError("Navigation error", e.getMessage());
        }
    }

    // 🔥 Version PRO avec injection
    public static <T> T openModal(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/tn/finora/finorainves/ui/" + fxml)
            );

            Parent root = loader.load();
            T controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            return controller;

        } catch (IOException e) {
            showError("Modal error", e.getMessage());
            return null;
        }
    }

    private static void showError(String title, String message) {
        System.err.println(title + ": " + message);
    }
    // =====================================================
// RETOUR VERS ROLE CHOICE
// =====================================================

    public static void goToRoleChoice() {

        // Reset session propre
        try {
            Class<?> sessionClass = Class.forName("tn.finora.utils.UserSession");
            sessionClass.getMethod("setRole", Class.forName("tn.finora.utils.AppRole"))
                    .invoke(null, new Object[]{null});
        } catch (Exception e) {
            // ignore si déjà null
        }

        goTo("RoleChoice.fxml", "Select Role");
    }
}