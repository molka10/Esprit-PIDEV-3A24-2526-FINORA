package com.example.finora.finorainves;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.finora.finorainves.ApiServer;
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        ApiServer.start();
        SceneNavigator.setStage(stage);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/com/finora/finorainves/ui/RoleChoice.fxml"
                )
        );

        Scene scene = new Scene(loader.load(), 1000, 600);

        stage.setTitle("Finora - Choose Role");
        stage.setScene(scene);
        stage.show();

        System.out.println("✅ Primary Stage initialized");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
