package com.example.finora_user.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

    public static void goTo(Stage stage, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(Navigator.class.getResource("/com/example/finora_user/" + fxml));
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1200, 700));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
