package tn.finora.finorainves;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {

    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void goTo(String fxmlFile, String title) {
        try {
            String fullPath = "/tn/finora/finorainves/ui/" + fxmlFile;

            var url = SceneNavigator.class.getResource(fullPath);
            if (url == null) {
                throw new RuntimeException("FXML not found: " + fullPath);
            }

            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);

            var cssUrl = SceneNavigator.class.getResource("/tn/finora/finorainves/styles/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("❌ Navigation error: " + e.getMessage(), e);
        }
    }}

