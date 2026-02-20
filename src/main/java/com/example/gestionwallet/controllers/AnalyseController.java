package com.example.gestionwallet.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import com.example.gestionwallet.services.servicetransaction;
import javafx.stage.Stage;

public class AnalyseController {

    @FXML
    private Label balanceLabel;

    @FXML
    private Label incomeLabel;

    @FXML
    private Label outcomeLabel;

    @FXML
    private BarChart<String, Number> barChart;

    private servicetransaction st = new servicetransaction();

    @FXML
    public void initialize() {

        double totalIncome = st.getTotalIncome();
        double totalOutcome = st.getTotalOutcome();
        double balance = totalIncome + totalOutcome;

        incomeLabel.setText(totalIncome + " DT");
        outcomeLabel.setText(totalOutcome + " DT");
        balanceLabel.setText(balance + " DT");

        loadChart(totalIncome, totalOutcome);
    }

    private void loadChart(double income, double outcome) {

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transactions");

        series.getData().add(new XYChart.Data<>("Income", income));
        series.getData().add(new XYChart.Data<>("Outcome", outcome));

        barChart.getData().clear();
        barChart.getData().add(series);
    }

    @FXML
    private void openAdminPage() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gestionwallet/admin-dashboard.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Admin Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
