package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.finora.utils.AppRole;
import tn.finora.utils.UserSession;

public class RoleChoiceController {

    @FXML
    private void chooseAdmin() {
        UserSession.setRole(AppRole.ADMIN);
        openMain();
    }

    @FXML
    private void chooseUser() {
        UserSession.setRole(AppRole.USER);
        openMain();
    }

    private void openMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation_list.fxml"));
            Scene scene = new Scene(loader.load(), 1250, 720);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            Stage stage = (Stage) scene.getWindow();
            if (stage == null) {
                // fallback: get current stage from any node later
                stage = new Stage();
            }
            stage.setTitle("Finora - Gestion Formation");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}