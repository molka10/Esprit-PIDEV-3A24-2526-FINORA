package tn.finora.controllers;

import javafx.fxml.FXML;
import tn.finora.finoraformation.HelloApplication;
import tn.finora.utils.AppRole;
import tn.finora.utils.UserSession;

public class RoleChoiceController {

    @FXML
    private void chooseAdmin() {
        UserSession.setRole(AppRole.ADMIN);
        HelloApplication.showFormations();
    }

    @FXML
    private void chooseUser() {
        UserSession.setRole(AppRole.USER);
        HelloApplication.showFormations();
    }
}