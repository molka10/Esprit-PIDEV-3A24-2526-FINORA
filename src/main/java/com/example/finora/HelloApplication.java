package com.example.finora;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.example.finora.utils.ThemeManager;
import java.io.IOException;

public class HelloApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

        ThemeManager.apply(scene);

        // Start internal API for Investment Recommendations
        com.example.finora.controllers.investment.ApiServer.start();

        stage.setTitle("Finora");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    // --- Navigation methods for Formation Module ---
    public static void showFormations() {
        com.example.finora.utils.Navigator.goTo(primaryStage, "/formation/formation_list.fxml",
                "Finora Academy - Formations");
    }

    public static void showLessons() {
        com.example.finora.utils.Navigator.goTo(primaryStage, "/formation/lesson_list.fxml", "Finora Academy - Leçons");
    }

    public static void showRoleChoice() {
        com.example.finora.utils.Navigator.goTo(primaryStage, "/formation/role_choice.fxml", "Finora Academy - Rôle");
    }
}