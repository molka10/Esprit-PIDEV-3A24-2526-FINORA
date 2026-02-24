package tn.finora.controllers;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import tn.finora.entities.InvestmentManagement;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentManagementService;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

public class InvestmentManagementCardsController {

    @FXML private FlowPane cardsPane;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> amountBarChart;
    @FXML private Label totalManagementLabel;
    @FXML private Label totalInvestedLabel;
    @FXML private Label activeRatioLabel;

    private final InvestmentManagementService service =
            new InvestmentManagementService();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(Locale.US);

    // ================= INIT =================

    @FXML
    public void initialize() {
        loadCards();
    }

    // ================= NAVIGATION =================

    @FXML
    private void onAdd() {
        AppState.setSelectedManagement(null);
        SceneNavigator.goTo("investment_management_form.fxml",
                "Add Investment Management");
    }

    @FXML
    private void onRefresh() {
        loadCards();
    }

    @FXML
    private void goToInvestments() {
        SceneNavigator.goTo("investment_cards.fxml", "Investments");
    }

    // ✅ DARK MODE FIXED
    @FXML
    private void toggleDarkMode() {

        if (cardsPane == null || cardsPane.getScene() == null) return;

        Parent root = cardsPane.getScene().getRoot();

        if (root.getStyleClass().contains("dark-root")) {
            root.getStyleClass().remove("dark-root");
        } else {
            root.getStyleClass().add("dark-root");
        }
    }

    // ================= LOAD DATA =================

    private void loadCards() {

        cardsPane.getChildren().clear();
        List<InvestmentManagement> list = service.getAll();

        if (list.isEmpty()) {
            cardsPane.getChildren().add(new Label("No management records found."));
        }

        for (InvestmentManagement m : list) {
            cardsPane.getChildren().add(buildCard(m));
        }

        loadStatistics(list);
        updateKPI(list);
    }

    // ================= KPI =================

    private void updateKPI(List<InvestmentManagement> list) {

        int total = list.size();

        double totalInvested = list.stream()
                .map(InvestmentManagement::getAmountInvested)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        long active = list.stream()
                .filter(m -> "active".equalsIgnoreCase(m.getStatus()))
                .count();

        double ratio = total == 0 ? 0 : (active * 100.0) / total;

        animateCounter(totalManagementLabel, total, false);
        animateCounter(totalInvestedLabel, totalInvested, true);
        animateCounter(activeRatioLabel, ratio, false);
    }

    private void animateCounter(Label label, double target, boolean currency) {

        DoubleProperty value = new SimpleDoubleProperty(0);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(value, target, Interpolator.EASE_OUT))
        );

        value.addListener((obs, oldVal, newVal) -> {

            if (currency) {
                label.setText(currencyFormat.format(newVal.doubleValue()));
            } else if (label == activeRatioLabel) {
                label.setText(String.format("%.1f %%", newVal.doubleValue()));
            } else {
                label.setText(String.valueOf(newVal.intValue()));
            }
        });

        timeline.play();
    }

    // ================= BUILD CARD =================

    private VBox buildCard(InvestmentManagement m) {

        VBox card = new VBox(10);
        card.setPrefWidth(280);

        Label title = new Label("Management #" + m.getManagementId());
        Label name = new Label(safe(m.getInvestmentName()));
        Label amount = new Label(
                m.getAmountInvested() != null
                        ? currencyFormat.format(m.getAmountInvested())
                        : "-"
        );

        Label status = new Label(safe(m.getStatus()).toUpperCase());

        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            AppState.setSelectedManagement(m);
            SceneNavigator.goTo("investment_management_form.fxml",
                    "Edit Management");
        });

        Button delete = new Button("Delete");
        delete.setOnAction(e -> onDelete(m));

        HBox actions = new HBox(10, edit, delete);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, name, amount, status, actions);

        return card;
    }

    private void onDelete(InvestmentManagement m) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete Management #" + m.getManagementId());
        alert.setContentText("Confirm deletion?");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(m.getManagementId());
                loadCards();
            }
        });
    }

    // ================= CHARTS =================

    private void loadStatistics(List<InvestmentManagement> list) {

        long active = list.stream()
                .filter(m -> "active".equalsIgnoreCase(m.getStatus()))
                .count();

        long closed = list.stream()
                .filter(m -> "closed".equalsIgnoreCase(m.getStatus()))
                .count();

        statusPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Active", active),
                new PieChart.Data("Closed", closed)
        ));

        statusPieChart.setLegendSide(Side.BOTTOM);

        Map<String, Double> totals = new HashMap<>();

        for (InvestmentManagement m : list) {

            String type = safe(m.getInvestmentType());

            double amount = 0.0;
            if (m.getAmountInvested() != null) {
                amount = m.getAmountInvested().doubleValue();
            }

            totals.put(type, totals.getOrDefault(type, 0.0) + amount);
        }

        amountBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Invested");

        totals.forEach((type, total) -> {

            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(type, 0.0);

            series.getData().add(data);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(1.5),
                            new KeyValue(data.YValueProperty(),
                                    total,
                                    Interpolator.EASE_OUT))
            );

            timeline.play();
        });

        amountBarChart.getData().add(series);

        animateCharts();
    }

    private void animateCharts() {

        FadeTransition fadePie =
                new FadeTransition(Duration.seconds(1), statusPieChart);
        fadePie.setFromValue(0);
        fadePie.setToValue(1);

        FadeTransition fadeBar =
                new FadeTransition(Duration.seconds(1), amountBarChart);
        fadeBar.setFromValue(0);
        fadeBar.setToValue(1);

        new ParallelTransition(fadePie, fadeBar).play();
    }

    // ================= EXPORT EXCEL =================

    @FXML
    private void onExportExcel() {

        List<InvestmentManagement> list = service.getAll();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Excel");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = chooser.showSaveDialog(cardsPane.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Management Report");

            String[] columns = {
                    "ID", "Investment", "Type",
                    "Amount", "Ownership %", "Start Date", "Status"
            };

            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowIndex = 1;

            for (InvestmentManagement m : list) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(m.getManagementId());
                row.createCell(1).setCellValue(safe(m.getInvestmentName()));
                row.createCell(2).setCellValue(safe(m.getInvestmentType()));

                row.createCell(3).setCellValue(
                        m.getAmountInvested() != null
                                ? m.getAmountInvested().doubleValue()
                                : 0.0
                );

                row.createCell(4).setCellValue(
                        m.getOwnershipPercentage() != null
                                ? m.getOwnershipPercentage().doubleValue()
                                : 0.0
                );

                row.createCell(5).setCellValue(
                        m.getStartDate() != null
                                ? m.getStartDate().toString()
                                : "-"
                );

                row.createCell(6).setCellValue(safe(m.getStatus()));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "Excel exported successfully!").showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}