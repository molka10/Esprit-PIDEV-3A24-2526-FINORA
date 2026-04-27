package com.example.finora.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardAdminController {

    @FXML private Label lblDateTime;
    @FXML private Label lblWelcomeMessage;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalTransactions;
    @FXML private Label lblVolumeTotal;
    @FXML private Label lblTotalCommissions;

    private Timer dateTimeTimer;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        demarrerHorloge();
        chargerStatistiques();
    }

    private void demarrerHorloge() {
        if (lblDateTime == null) return;

        dateTimeTimer = new Timer(true);
        dateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (lblDateTime != null) {
                        lblDateTime.setText(sdf.format(new Date()));
                    }
                });
            }
        }, 0, 1000);
    }

    private void chargerStatistiques() {
        try {
            // TODO: connect real services
            if (lblTotalUsers != null) lblTotalUsers.setText("0");
            if (lblTotalTransactions != null) lblTotalTransactions.setText("0");
            if (lblVolumeTotal != null) lblVolumeTotal.setText("0 TND");
            if (lblTotalCommissions != null) lblTotalCommissions.setText("0 TND");

            if (lblWelcomeMessage != null) {
                lblWelcomeMessage.setText("Bienvenue ! Consultez les statistiques et gérez les modules");
            }
        } catch (Exception e) {
            System.err.println("Erreur stats: " + e.getMessage());
        }
    }

    // ======================
    // Buttons in FXML
    // ======================

    @FXML
    public void goBourse(ActionEvent event) {
        AdminShellController shell = AdminShellController.getInstance();
        if (shell != null) shell.goBourse(event);
    }

    @FXML
    public void goPortefeuille(ActionEvent event) {
        AdminShellController shell = AdminShellController.getInstance();
        if (shell != null) shell.goPortefeuille(event);
    }

    @FXML
    public void goAppelOffre(ActionEvent event) {
        AdminShellController shell = AdminShellController.getInstance();
        if (shell != null) shell.goAppelOffre(event);
    }

    @FXML
    public void goFormation(ActionEvent event) {
        AdminShellController shell = AdminShellController.getInstance();
        if (shell != null) shell.goFormation(event);
    }

    @FXML
    public void goInvestmentManagement(ActionEvent event) {
        AdminShellController shell = AdminShellController.getInstance();
        if (shell != null) shell.goInvestmentManagement(event);
    }

    @FXML
    public void goGestionUsers(ActionEvent event) {
        AdminShellController shell = AdminShellController.getInstance();
        if (shell != null) shell.goGestionUsers(event);
    }

    /** Call this when leaving the page if you have a lifecycle hook */
    public void cleanup() {
        if (dateTimeTimer != null) dateTimeTimer.cancel();
    }
}