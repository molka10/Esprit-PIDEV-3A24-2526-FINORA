package tn.finora.controllers;

import javafx.fxml.FXML;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.utils.AppRole;
import tn.finora.utils.UserSession;

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