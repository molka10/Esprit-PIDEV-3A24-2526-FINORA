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
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root, 900, 500);
            } else {
                scene.setRoot(root);
            }

            var cssUrl = SceneNavigator.class.getResource("/tn/finora/finorainves/styles/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Navigation error: " + e.getMessage(), e);
        }
    }
    public static void openModal(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/tn/finora/finorainves/ui/" + fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(

                    Modality.APPLICATION_MODAL); // bloque la fenêtre parente
            stage.showAndWait(); // attend la fermeture pour reprendre le flow
        } catch (IOException e) {
            throw new RuntimeException("Navigation error: " + e.getMessage(), e);
        }
    }
}