package com.example.finora.controllers;

import javafx.fxml.FXML;
import com.example.finora.HelloApplication;
import com.example.finora.utils.Session;
import com.example.finora.entities.User;

public class RoleChoiceController {

    @FXML
    private void chooseAdmin() {
        ensureUser().setRole("ADMIN");
        HelloApplication.showFormations();
    }

    @FXML
    private void chooseUser() {
        ensureUser().setRole("USER");
        HelloApplication.showFormations();
    }

    private User ensureUser() {
        User u = Session.getCurrentUser();
        if (u == null) {
            u = new User();
            Session.setCurrentUser(u);
        }
        return u;
    }
}