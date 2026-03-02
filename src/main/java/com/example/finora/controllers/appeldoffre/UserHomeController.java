package com.example.finora.controllers.appeldoffre;

import com.example.finora.services.appeldoffre.AppelOffreService;
import com.example.finora.services.appeldoffre.CandidatureService;
import com.example.finora.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserHomeController {

    @FXML
    private Label publishedCount;
    @FXML
    private Label myCandCount;

    private final AppelOffreService aoService = new AppelOffreService();
    private final CandidatureService candService = new CandidatureService();

    @FXML
    public void initialize() {
        try {
            var allAo = aoService.getAll();
            publishedCount.setText(
                    String.valueOf(allAo.stream().filter(a -> "published".equalsIgnoreCase(a.getStatut())).count()));
        } catch (Exception e) {
            publishedCount.setText("-");
        }
        try {
            String email = Session.getCurrentUser().getEmail();
            if (email == null || email.isBlank()) {
                myCandCount.setText("-");
            } else {
                var all = candService.getAll();
                myCandCount.setText(
                        String.valueOf(all.stream().filter(c -> email.equalsIgnoreCase(c.getEmailCandidat())).count()));
            }
        } catch (Exception e) {
            myCandCount.setText("-");
        }
    }

    @FXML
    private void openAppels() {
        if (com.example.finora.controllers.UserShellController.getInstance() != null)
            com.example.finora.controllers.UserShellController.getInstance().loadCenterSafe("/ui/AppelOffreView.fxml");
    }

    @FXML
    private void openCands() {
        if (com.example.finora.controllers.UserShellController.getInstance() != null)
            com.example.finora.controllers.UserShellController.getInstance().loadCenterSafe("/ui/CandidatureView.fxml");
    }
}
