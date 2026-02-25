package com.example.finora.finoraformation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showRoleChoice();
        stage.setTitle("Finora");
        stage.show();
    }

    // ✅ Single static navigation function (as you requested)
    public static void goTo(String fxml, double w, double h, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/" + fxml));
            Parent root = loader.load();

            Scene scene = new Scene(root, w, h);
            scene.getStylesheets().add(
                    Objects.requireNonNull(HelloApplication.class.getResource("/style.css")).toExternalForm()
            );

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showRoleChoice() {
        goTo("role_choice.fxml", 1250, 720, "Finora - Choix Espace");
    }

    public static void showFormations() {
        goTo("formation_list.fxml", 1250, 720, "Finora - Formations");
    }

    public static void showLessons() {
        goTo("lesson_list.fxml", 1250, 720, "Finora - Lessons");
    }

    public static void main(String[] args) {
        launch();
    }
}