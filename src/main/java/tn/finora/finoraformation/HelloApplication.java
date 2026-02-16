package tn.finora.finoraformation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ✅ Your FXML is directly in src/main/resources (root)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation_list.fxml"));

        Scene scene = new Scene(loader.load(), 1000, 650);

        // ✅ Your CSS is also in root
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Finora - Gestion Formation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
