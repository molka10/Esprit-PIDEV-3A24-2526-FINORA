package com.example.finora.controllers;

import javafx.fxml.FXML;
import com.example.finora.finorainves.SceneNavigator;
import com.example.finora.utils.AppRole;
import com.example.finora.utils.UserSession;

public class RoleChoiceController {

    @FXML
    private void chooseAdmin(){

        UserSession.setRole(AppRole.ADMIN);

        SceneNavigator.goTo(
                "investment_cards.fxml",
                "Admin - Investment Management"
        );
    }

    @FXML
    private void chooseInvestisseur(){

        UserSession.setRole(AppRole.INVESTISSEUR);

        SceneNavigator.goTo(
                "investment_cards.fxml",
                "Investor - Investments"
        );
    }
}