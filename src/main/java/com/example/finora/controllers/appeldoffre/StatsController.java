package com.example.finora.controllers.appeldoffre;

import com.example.finora.services.appeldoffre.AppelOffreService;
import com.example.finora.services.appeldoffre.CandidatureService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.Map;

public class StatsController {

    @FXML
    private PieChart appelPie;
    @FXML
    private PieChart candidaturePie;

    @FXML
    private Label aoTotal;
    @FXML
    private Label candTotal;
    @FXML
    private Label pubRate;

    private final AppelOffreService aoService = new AppelOffreService();
    private final CandidatureService candService = new CandidatureService();

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    public void refresh() {
        try {
            var aos = aoService.getAll();
            aoTotal.setText(String.valueOf(aos.size()));

            long published = aos.stream().filter(a -> "published".equalsIgnoreCase(a.getStatut())).count();
            pubRate.setText(aos.isEmpty() ? "-" : String.format("%.0f%%", (published * 100.0) / aos.size()));

            Map<String, Integer> byStatut = new HashMap<>();
            for (var a : aos) {
                String s = (a.getStatut() == null || a.getStatut().isBlank()) ? "unknown" : a.getStatut().toLowerCase();
                byStatut.put(s, byStatut.getOrDefault(s, 0) + 1);
            }
            appelPie.setData(FXCollections.observableArrayList(
                    byStatut.entrySet().stream().map(e -> new PieChart.Data(e.getKey(), e.getValue())).toList()));
        } catch (Exception e) {
            aoTotal.setText("-");
            pubRate.setText("-");
            appelPie.setData(FXCollections.observableArrayList());
        }

        try {
            var cands = candService.getAll();
            candTotal.setText(String.valueOf(cands.size()));

            Map<String, Integer> byStatut = new HashMap<>();
            for (var c : cands) {
                String s = (c.getStatut() == null || c.getStatut().isBlank()) ? "unknown" : c.getStatut().toLowerCase();
                byStatut.put(s, byStatut.getOrDefault(s, 0) + 1);
            }
            candidaturePie.setData(FXCollections.observableArrayList(
                    byStatut.entrySet().stream().map(e -> new PieChart.Data(e.getKey(), e.getValue())).toList()));
        } catch (Exception e) {
            candTotal.setText("-");
            candidaturePie.setData(FXCollections.observableArrayList());
        }
    }

    @FXML
    private void onReturn() {
        if (com.example.finora.controllers.AdminShellController.getInstance() != null) {
            com.example.finora.controllers.AdminShellController.getInstance()
                    .loadCenterSafe("/ui/home/AdminHome.fxml");
        } else if (com.example.finora.controllers.EntrepriseShellController.getInstance() != null) {
            com.example.finora.controllers.EntrepriseShellController.getInstance()
                    .loadCenterSafe("/ui/home/EntrepriseHome.fxml");
        } else if (com.example.finora.controllers.UserShellController.getInstance() != null) {
            com.example.finora.controllers.UserShellController.getInstance()
                    .loadCenterSafe("/ui/home/UserHome.fxml");
        }
    }
}
