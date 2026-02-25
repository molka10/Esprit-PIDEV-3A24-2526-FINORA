package com.example.finora.controllers;

import javafx.fxml.FXML;
import com.example.finora.finoraformation.HelloApplication;
import com.example.finora.utils.AppRole;
import com.example.finora.utils.UserSession;

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