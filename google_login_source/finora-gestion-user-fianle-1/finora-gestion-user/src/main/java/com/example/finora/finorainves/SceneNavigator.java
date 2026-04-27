package com.example.finora.finorainves;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SceneNavigator {

    private static StackPane contentArea;
    private static Stage mainStage;

    public static void setContentArea(StackPane area) {
        contentArea = area;
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void goTo(String fxml, String title) {
        loadView(fxml);
        if (mainStage != null && title != null) {
            mainStage.setTitle(title);
        }
    }

    public static void loadView(String fxml) {
        try {
            if (contentArea == null) {
                System.err.println("❌ SceneNavigator: contentArea is null!");
                return;
            }
            String path = fxml;
            if (!path.startsWith("/")) {
                // Try com.example.finora first if no leading slash
                path = "/com/example/finora/" + fxml;
            }

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(path));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            System.err.println("❌ SceneNavigator: Error loading " + fxml);
            e.printStackTrace();
        }
    }

    public static void goToRoleChoice() {
        // Fallback to a dashboard or main menu since RoleChoice.fxml
        // in investment folder is a standalone demo view
        goTo("investment_cards.fxml", "Investments");
    }
}
