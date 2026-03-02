package com.example.finora.controllers.appeldoffre;

import com.example.finora.services.appeldoffre.AppelOffreService;
import com.example.finora.services.appeldoffre.CandidatureService;
import com.example.finora.controllers.EntrepriseShellController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EntrepriseHomeController {

    @FXML
    private Label aoCount;
    @FXML
    private Label candCount;

    private final AppelOffreService aoService = new AppelOffreService();
    private final CandidatureService candService = new CandidatureService();

    @FXML
    public void initialize() {
        // In this version we don't yet filter by entreprise ownership (to add later)
        try {
            aoCount.setText(String.valueOf(aoService.getAll().size()));
        } catch (Exception e) {
            aoCount.setText("-");
        }
        try {
            candCount.setText(String.valueOf(candService.getAll().size()));
        } catch (Exception e) {
            candCount.setText("-");
        }
    }

    @FXML
    private void newAppel() {
        if (EntrepriseShellController.getInstance() != null)
            EntrepriseShellController.getInstance().loadCenterSafe("/ui/AppelOffreView.fxml");
    }

    @FXML
    private void openAppels() {
        if (EntrepriseShellController.getInstance() != null)
            EntrepriseShellController.getInstance().loadCenterSafe("/ui/AppelOffreView.fxml");
    }

    @FXML
    private void openCands() {
        if (EntrepriseShellController.getInstance() != null)
            EntrepriseShellController.getInstance().loadCenterSafe("/ui/CandidatureView.fxml");
    }
}
