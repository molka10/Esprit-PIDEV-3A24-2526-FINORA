package com.example.finora.controllers.appeldoffre;

import com.example.finora.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

/**
 * Standalone role selector for the Appel d'Offre module.
 * Redirects to AppelOffreView based on the logged-in user's role.
 */
public class RoleSelectController {

    @FXML
    private void onAdmin(ActionEvent event) {
        navigateTo("/ui/AppelOffreView.fxml", event);
    }

    @FXML
    private void onEntreprise(ActionEvent event) {
        navigateTo("/ui/AppelOffreView.fxml", event);
    }

    @FXML
    private void onUser(ActionEvent event) {
        navigateTo("/ui/CandidatureView.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
