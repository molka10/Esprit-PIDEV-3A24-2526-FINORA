package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.finorainves.SceneNavigator;
import com.example.finora.services.bourse.ServiceTransactionBourse;
import com.example.finora.utils.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardUserController {

    @FXML
    private Label lblDateTime;
    @FXML
    private Label lblWelcomeMessage;
    @FXML
    private Label lblTotalTransactions;
    @FXML
    private Label lblPortfolioValue;
    @FXML
    private Label lblTotalActions;
    @FXML
    private Label lblTotalCommissions;
    @FXML
    private Label lblLoginTime;

    @FXML
    public void initialize() {
        // ── Date/time ──
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm",
                java.util.Locale.FRENCH);
        if (lblDateTime != null)
            lblDateTime.setText(LocalDateTime.now().format(fmt));

        // ── Welcome message with user name ──
        User user = Session.getCurrentUser();
        if (user != null && lblWelcomeMessage != null) {
            lblWelcomeMessage
                    .setText("Bonjour " + user.getUsername() + " ! Accédez à vos outils financiers ci-dessous.");
        }

        // ── Login time ──
        if (lblLoginTime != null) {
            lblLoginTime.setText("Aujourd'hui à " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // ── Live stats from bourse ──
        loadBourseStats();
    }

    private void loadBourseStats() {
        try {
            ServiceTransactionBourse svc = new ServiceTransactionBourse();
            int userId = Session.getCurrentUser() != null ? Session.getCurrentUser().getId() : 0;

            int nbTransactions = svc.getNombreTransactions();
            double totalInvesti = svc.getTotalInvesti();
            double totalCommissions = svc.getTotalCommissions();

            if (lblTotalTransactions != null)
                lblTotalTransactions.setText(String.valueOf(nbTransactions));

            if (lblPortfolioValue != null)
                lblPortfolioValue.setText(String.format("%.2f TND", totalInvesti));

            if (lblTotalCommissions != null)
                lblTotalCommissions.setText(String.format("%.2f TND", totalCommissions));

            // Actions in portfolio (distinct actions)
            int actionsCount = svc.getNombreActions(userId);
            if (lblTotalActions != null)
                lblTotalActions.setText(String.valueOf(actionsCount));

        } catch (Exception e) {
            System.err.println("⚠️ Dashboard stats failed: " + e.getMessage());
        }
    }


    // ── Navigation handlers (delegate to SceneNavigator / shell) ──

    @FXML
    private void goBourse() {
        SceneNavigator.loadView("/bourse/dashboard-investisseur-view.fxml");
    }

    @FXML
    private void openPortefeuille() {
        SceneNavigator.loadView("/wallet/user.fxml");
    }

    @FXML
    private void openAppelOffre() {
        SceneNavigator.loadView("/ui/AppelOffreView.fxml");
    }

    @FXML
    private void openFormation() {
        SceneNavigator.loadView("/formation/formation_list.fxml");
    }

    @FXML
    private void openWallet() {
        SceneNavigator.loadView("/wallet/user.fxml");
    }

    @FXML
    private void openInvestment() {
        SceneNavigator.loadView("/investment/investment_cards.fxml");
    }
}
