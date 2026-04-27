package com.example.finora.utils;

import com.example.finora.entities.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

    public static <T> T goTo(Stage stage, String fxml, String title) {
        try {
            String path = fxml.startsWith("/") ? fxml : "/com/example/finora/" + fxml;
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(path));
            Parent root = loader.load();

            // Try to find contentArea in the current scene for nested navigation
            if (stage.getScene() != null
                    && stage.getScene().lookup("#contentArea") instanceof javafx.scene.layout.StackPane contentArea) {
                contentArea.getChildren().setAll(root);
                if (title != null)
                    stage.setTitle(title);
                return loader.getController();
            }

            // Fallback: Default full scene switch
            stage.setTitle(title);
            Scene scene = new Scene(root, 1200, 700);
            ThemeManager.apply(scene);
            stage.setScene(scene);
            stage.show();
            return loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T setRoot(Stage stage, String fxml, String title) {
        try {
            String path = fxml.startsWith("/") ? fxml : "/com/example/finora/" + fxml;
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(path));
            Parent root = loader.load();

            if (title != null)
                stage.setTitle(title);
            Scene scene = new Scene(root, 1200, 700);
            ThemeManager.apply(scene);
            stage.setScene(scene);
            stage.show();
            return loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void returnToDashboard(Stage stage) {
        User user = Session.getCurrentUser();
        if (user == null)
            return;

        String role = user.getRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            goTo(stage, "/wallet/admin-dashboard.fxml", "FINORA - Administration");
        } else if ("ENTREPRISE".equalsIgnoreCase(role)) {
            goTo(stage, "/wallet/entreprise.fxml", "FINORA - Entreprise");
        } else {
            goTo(stage, "/wallet/user.fxml", "FINORA - Investisseur");
        }
    }
}
