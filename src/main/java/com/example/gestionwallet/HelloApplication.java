package com.example.gestionwallet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {


        var url = HelloApplication.class.getResource(
                "/com/example/gestionwallet/integ.fxml"
        );


        if (url == null) {
            throw new RuntimeException("integ.fxml NOT FOUND");
        }

        FXMLLoader loader = new FXMLLoader(url);
        Scene scene = new Scene(loader.load());

        stage.setTitle("Wallet");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
