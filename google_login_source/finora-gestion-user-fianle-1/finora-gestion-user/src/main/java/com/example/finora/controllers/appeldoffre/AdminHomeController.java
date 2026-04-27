package com.example.finora.controllers.appeldoffre;

import com.example.finora.services.appeldoffre.AppelOffreService;
import com.example.finora.services.appeldoffre.CandidatureService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminHomeController {

    @FXML
    private Label aoCount;
    @FXML
    private Label candCount;
    @FXML
    private Label publishedCount;

    private final AppelOffreService aoService = new AppelOffreService();
    private final CandidatureService candService = new CandidatureService();

    @FXML
    public void initialize() {
        try {
            var allAo = aoService.getAll();
            aoCount.setText(String.valueOf(allAo.size()));
            publishedCount.setText(
                    String.valueOf(allAo.stream().filter(a -> "published".equalsIgnoreCase(a.getStatut())).count()));
        } catch (Exception e) {
            aoCount.setText("-");
            publishedCount.setText("-");
        }
        try {
            candCount.setText(String.valueOf(candService.getAll().size()));
        } catch (Exception e) {
            candCount.setText("-");
        }
    }

    @FXML
    private void openAppels() {
        if (com.example.finora.controllers.AdminShellController.getInstance() != null)
            com.example.finora.controllers.AdminShellController.getInstance().loadCenterSafe("/ui/AppelOffreView.fxml");
    }

    @FXML
    private void openCands() {
        if (com.example.finora.controllers.AdminShellController.getInstance() != null)
            com.example.finora.controllers.AdminShellController.getInstance()
                    .loadCenterSafe("/ui/CandidatureView.fxml");
    }

    @FXML
    private void openStats() {
        if (com.example.finora.controllers.AdminShellController.getInstance() != null)
            com.example.finora.controllers.AdminShellController.getInstance().loadCenterSafe("/ui/StatsView.fxml");
    }
}
