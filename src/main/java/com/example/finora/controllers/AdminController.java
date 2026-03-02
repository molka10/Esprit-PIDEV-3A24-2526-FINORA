package com.example.finora.controllers;

import com.example.finora.entities.transaction;
import com.example.finora.services.servicetransaction;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AdminController {

    @FXML
    private VBox transactionContainer;
    @FXML
    private VBox walletContainer;
    @FXML
    private TextField userSearchField;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private PieChart walletChart;
    @FXML
    private LineChart<String, Number> transactionLineChart;
    @FXML
    private Button backButton;
    @FXML
    private HBox statsContainer;
    @FXML
    private Label currencyRatesLabel;
    @FXML
    private StackPane mainRoot;
    private final servicetransaction st = new servicetransaction();
    private boolean darkMode = false;
    @FXML
    private AnchorPane root;
    @FXML
    private Button themeButton;

    @FXML
    public void initialize() {

        filterBox.getItems().addAll("All", "INCOME", "OUTCOME");
        filterBox.setValue("All");

        loadTransactions();
        loadWallet();
        loadChart();
        loadLineChart();
        loadStatsUsers();
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercheUser();
        });
        loadStatsUsers();
    }

    public String calculateStatus(double income, double outcome, double total) {

        if (total < 0 || Math.abs(outcome) > income) {
            return "CRITICAL";
        }

        if (Math.abs(outcome) >= income * 0.7) {
            return "RISK";
        }

        return "STABLE";
    }

    private void loadTransactions() {

        transactionContainer.getChildren().clear();

        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.getStyleClass().add("header-row");

        header.getChildren().addAll(
                createHeaderLabel("User", 120),
                createHeaderLabel("Type", 120),
                createHeaderLabel("Montant", 150),
                createHeaderLabel("Date", 180));

        transactionContainer.getChildren().add(header);

        for (transaction t : st.afficher()) {
            addTransactionRow(t);
        }

    }

    private void addTransactionRow(transaction t) {

        HBox row = new HBox();
        row.setPadding(new Insets(10));
        row.getStyleClass().add("wallet-row");

        row.getChildren().addAll(
                createCell(t.getUsername(), 120),
                createCell(t.getType(), 120),
                createMontantCell(t.getMontant(), 150),
                createCell(t.getDate_transaction().toString(), 180));

        transactionContainer.getChildren().add(row);
    }

    @FXML
    private void applyFilter() {

        transactionContainer.getChildren().clear();

        String type = filterBox.getValue();
        var date = datePicker.getValue();

        for (transaction t : st.afficher()) {

            boolean okType = type.equals("All") ||
                    t.getType().equalsIgnoreCase(type);

            boolean okDate = true;

            if (date != null) {
                okDate = t.getDate_transaction()
                        .toLocalDate()
                        .equals(date);
            }

            if (okType && okDate) {
                addTransactionRow(t);
            }
        }
    }

    @FXML
    private void resetFilter() {
        filterBox.setValue("All");
        datePicker.setValue(null);
        loadTransactions();
    }

    private void loadWallet() {

        walletContainer.getChildren().clear();

        Map<String, Double> totalMap = new TreeMap<>();
        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> outcomeMap = new TreeMap<>();
        Map<String, Integer> countMap = new TreeMap<>();

        for (transaction t : st.afficher()) {

            String role = t.getUsername();
            if (role == null || role.trim().isEmpty()) {
                continue;
            }

            totalMap.put(role,
                    totalMap.getOrDefault(role, 0.0) + t.getMontant());

            if (t.getMontant() >= 0)
                incomeMap.put(role,
                        incomeMap.getOrDefault(role, 0.0) + t.getMontant());
            else
                outcomeMap.put(role,
                        outcomeMap.getOrDefault(role, 0.0) + t.getMontant());

            countMap.put(role,
                    countMap.getOrDefault(role, 0) + 1);
        }

        HBox header = new HBox(30);
        header.setPadding(new Insets(10));
        header.getStyleClass().add("header-row");

        header.getChildren().addAll(
                createHeaderLabel("User", 120),
                createHeaderLabel("Total", 120),
                createHeaderLabel("Income", 120),
                createHeaderLabel("Outcome", 120),
                createHeaderLabel("Transactions", 120),
                createHeaderLabel("Status", 120)

        );

        walletContainer.getChildren().add(header);

        for (String role : totalMap.keySet()) {

            HBox row = new HBox(30);
            row.setPadding(new Insets(10));
            row.getStyleClass().add("wallet-row");

            Label roleLabel = createCell(role, 120);
            Label totalLabel = createCell(
                    String.format("%.2f DT", totalMap.get(role)),
                    120
            );
            Label incomeLabel = createCell(
                    String.format("%.2f DT", incomeMap.getOrDefault(role, 0.0)),
                    120
            );

            Label outcomeLabel = createCell(
                    String.format("%.2f DT", outcomeMap.getOrDefault(role, 0.0)),
                    120
            );
            Label countLabel = createCell(String.valueOf(countMap.get(role)), 120);

            double income = incomeMap.getOrDefault(role, 0.0);
            double outcome = outcomeMap.getOrDefault(role, 0.0);
            double total = totalMap.get(role);

            String statusValue = calculateStatus(income, outcome, total);

            Label statusLabel = createCell(statusValue, 120);

            // Couleur automatique
            switch (statusValue) {
                case "CRITICAL" -> statusLabel.getStyleClass().add("status-critical");
                case "RISK" -> statusLabel.setStyle("-fx-text-fill:orange; -fx-font-weight:bold;");
                case "STABLE" -> statusLabel.setStyle("-fx-text-fill:green; -fx-font-weight:bold;");
            }

            incomeLabel.setStyle("-fx-text-fill:#2ecc71; -fx-font-weight:bold;");
            outcomeLabel.setStyle("-fx-text-fill:#e74c3c; -fx-font-weight:bold;");
            totalLabel.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");
            countLabel.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");

            row.getChildren().addAll(
                    roleLabel, totalLabel, incomeLabel, outcomeLabel, countLabel, statusLabel);

            walletContainer.getChildren().add(row);
        }
    }

    @FXML
    private void downloadPDF() {

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Wallet",
                "Wallet",
                "Transactions",
                "Wallet + Transactions");

        dialog.setTitle("Choix PDF");
        dialog.setHeaderText("Que veux-tu exporter ?");
        dialog.setContentText("Choisir :");

        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/wallet/dashboard.css").toExternalForm());

        var result = dialog.showAndWait();

        if (result.isEmpty())
            return;

        String choix = result.get();

        try {

            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf"));

            File file = fc.showSaveDialog(null);
            if (file == null)
                return;

            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            BaseColor violet = new BaseColor(106, 13, 173);
            Font bigTitle = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, violet);

            Paragraph title = new Paragraph("ADMIN DASHBOARD", bigTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Generated on: " + java.time.LocalDate.now()));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            if (choix.equals("Wallet") || choix.equals("Wallet + Transactions")) {
                generateWalletSection(doc);
            }

            if (choix.equals("Transactions") || choix.equals("Wallet + Transactions")) {
                generateTransactionSection(doc);
            }

            doc.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "PDF généré avec succès !").showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur génération PDF").showAndWait();
        }
    }

    private void generateWalletSection(Document doc) throws Exception {

        BaseColor violet = new BaseColor(106, 13, 173);
        BaseColor green = new BaseColor(46, 204, 113);
        BaseColor red = new BaseColor(231, 76, 60);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
        Font greenFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, green);
        Font redFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, red);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        String[] headers = { "User", "Total", "Income", "Outcome", "Transactions" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(violet);
            table.addCell(cell);
        }

        Map<String, Double> totalMap = new TreeMap<>();
        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> outcomeMap = new TreeMap<>();
        Map<String, Integer> countMap = new TreeMap<>();

        for (transaction t : st.afficher()) {

            String role = t.getUsername();

            if (role == null || role.trim().isEmpty()) {
                continue;
            }

            totalMap.put(role,
                    totalMap.getOrDefault(role, 0.0) + t.getMontant());

            if (t.getMontant() >= 0)
                incomeMap.put(role,
                        incomeMap.getOrDefault(role, 0.0) + t.getMontant());
            else
                outcomeMap.put(role,
                        outcomeMap.getOrDefault(role, 0.0) + t.getMontant());

            countMap.put(role,
                    countMap.getOrDefault(role, 0) + 1);
        }

        for (String role : totalMap.keySet()) {

            table.addCell(new Phrase(role, normalFont));
            table.addCell(new Phrase(totalMap.get(role) + " DT", normalFont));
            table.addCell(new Phrase(incomeMap.getOrDefault(role, 0.0) + " DT", greenFont));
            table.addCell(new Phrase(outcomeMap.getOrDefault(role, 0.0) + " DT", redFont));
            table.addCell(new Phrase(String.valueOf(countMap.get(role)), normalFont));
        }

        doc.add(table);
    }

    private void generateTransactionSection(Document doc) throws Exception {

        BaseColor violet = new BaseColor(106, 13, 173);
        BaseColor green = new BaseColor(46, 204, 113);
        BaseColor red = new BaseColor(231, 76, 60);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, violet);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
        Font greenFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, green);
        Font redFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, red);

        Paragraph title = new Paragraph("TRANSACTIONS DETAILS", titleFont);
        doc.add(title);
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        String[] headers = { "User", "Type", "Montant", "Date" };

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(violet);
            table.addCell(cell);
        }

        for (transaction t : st.afficher()) {

            table.addCell(new Phrase(t.getUsername(), normalFont));
            table.addCell(new Phrase(t.getType(), normalFont));

            if (t.getMontant() >= 0)
                table.addCell(new Phrase(t.getMontant() + " DT", greenFont));
            else
                table.addCell(new Phrase(t.getMontant() + " DT", redFont));

            table.addCell(new Phrase(t.getDate_transaction().toString(), normalFont));
        }

        doc.add(table);
    }

    private Label createHeaderLabel(String t, double w) {
        Label l = new Label(t);
        l.setPrefWidth(w);
        l.getStyleClass().add("header-text");
        return l;
    }

    private Label createCell(String t, double w) {
        Label l = new Label(t);
        l.setPrefWidth(w);
        l.getStyleClass().add("cell-text");
        return l;
    }

    private Label createMontantCell(double m, double w) {
        Label l = new Label(m + " DT");
        l.setPrefWidth(w);
        if (m >= 0) {
            l.getStyleClass().add("income-text");
        } else {
            l.getStyleClass().add("outcome-text");
        }

        return l;
    }

    private void buildWalletTable(Map<String, Double> totalMap,
                                  Map<String, Double> incomeMap,
                                  Map<String, Double> outcomeMap,
                                  Map<String, Integer> countMap) {

        HBox header = new HBox(30);
        header.setPadding(new Insets(10));
        header.getStyleClass().add("header-row");

        header.getChildren().addAll(
                createHeaderLabel("User", 120),
                createHeaderLabel("Total", 120),
                createHeaderLabel("Income", 120),
                createHeaderLabel("Outcome", 120),
                createHeaderLabel("Transactions", 120),
                createHeaderLabel("Status", 120)
        );

        walletContainer.getChildren().add(header);

        for (String role : totalMap.keySet()) {

            HBox row = new HBox(30);
            row.setPadding(new Insets(10));
            row.getStyleClass().add("wallet-row");

            double income = incomeMap.getOrDefault(role, 0.0);
            double outcome = outcomeMap.getOrDefault(role, 0.0);
            double total = totalMap.get(role);

            String status = calculateStatus(income, outcome, total);

            Label roleLabel = createCell(role, 120);
            Label totalLabel = createCell(String.format("%.2f DT", total), 120);
            Label incomeLabel = createCell(String.format("%.2f DT", income), 120);
            Label outcomeLabel = createCell(String.format("%.2f DT", outcome), 120);
            Label countLabel = createCell(String.valueOf(countMap.get(role)), 120);
            Label statusLabel = createCell(status, 120);

            switch (status) {
                case "CRITICAL" -> statusLabel.getStyleClass().add("status-critical");
                case "RISK" -> statusLabel.setStyle("-fx-text-fill:orange; -fx-font-weight:bold;");
                case "STABLE" -> statusLabel.setStyle("-fx-text-fill:green; -fx-font-weight:bold;");
            }

            row.getChildren().addAll(
                    roleLabel, totalLabel, incomeLabel,
                    outcomeLabel, countLabel, statusLabel);

            walletContainer.getChildren().add(row);
        }
    }

    @FXML
    private void rechercheUser() {

        String userSearch = userSearchField.getText().trim().toLowerCase();

        transactionContainer.getChildren().clear();
        walletContainer.getChildren().clear();

        if (userSearch.isEmpty()) {
            loadTransactions();
            loadWallet();
            return;
        }

        Map<String, Double> totalMap = new TreeMap<>();
        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> outcomeMap = new TreeMap<>();
        Map<String, Integer> countMap = new TreeMap<>();

        for (transaction t : st.afficher()) {

            if (t.getUsername() != null &&
                    t.getUsername().toLowerCase().contains(userSearch)) {

                addTransactionRow(t);

                String role = t.getUsername();
                totalMap.put(role,
                        totalMap.getOrDefault(role, 0.0) + t.getMontant());

                if (t.getMontant() >= 0)
                    incomeMap.put(role,
                            incomeMap.getOrDefault(role, 0.0) + t.getMontant());
                else
                    outcomeMap.put(role,
                            outcomeMap.getOrDefault(role, 0.0) + t.getMontant());

                countMap.put(role,
                        countMap.getOrDefault(role, 0) + 1);
            }
        }

        buildWalletTable(totalMap, incomeMap, outcomeMap, countMap);
    }

    private void loadChart() {

        double income = 0;
        double outcome = 0;

        for (transaction t : st.afficher()) {

            if (t.getMontant() >= 0) {
                income += t.getMontant();
            } else {
                outcome += Math.abs(t.getMontant());
            }
        }

        double total = income + outcome;

        if (total == 0) {
            walletChart.setData(null);
            return;
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data(
                        "Income (" + Math.round((income / total) * 100) + "%)",
                        income),
                new PieChart.Data(
                        "Outcome (" + (int)((outcome / total) * 100) + "%)",
                        outcome)
        );

        walletChart.setData(data);
        walletChart.setLegendSide(Side.BOTTOM);
        walletChart.setLabelsVisible(true);
    }

    private void loadLineChart() {

        transactionLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total par date");

        Map<String, Double> totalParDate = new TreeMap<>();

        for (transaction t : st.afficher()) {

            String date = t.getDate_transaction()
                    .toLocalDate()
                    .toString();

            totalParDate.put(
                    date,
                    totalParDate.getOrDefault(date, 0.0)
                            + t.getMontant());
        }

        for (Map.Entry<String, Double> entry : totalParDate.entrySet()) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()));
        }

        transactionLineChart.getData().add(series);

        transactionLineChart.setAnimated(true);
        transactionLineChart.setLegendVisible(false);
        transactionLineChart.setCreateSymbols(true);

        Platform.runLater(() -> {
            series.getNode().setStyle(
                    "-fx-stroke: #6a0dad; -fx-stroke-width: 3px;");
        });

        transactionLineChart.setHorizontalGridLinesVisible(false);
        transactionLineChart.setVerticalGridLinesVisible(false);
        transactionLineChart.setAlternativeRowFillVisible(false);
        transactionLineChart.setAlternativeColumnFillVisible(false);

    }

    @FXML
    private void goBack() {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/finora/integ.fxml"));

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createStatCard(String title, int value, String color) {

        VBox box = new VBox(3);
        box.setPadding(new Insets(6));
        box.setPrefWidth(100);
        box.setMinWidth(100);

        box.getStyleClass().add("stat-card");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:" + color +
                "; -fx-font-size:14px; -fx-font-weight:bold;");

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setStyle("-fx-text-fill:" + color +
                "; -fx-font-size:18px; -fx-font-weight:bold;");

        box.getChildren().addAll(titleLabel, valueLabel);

        return box;
    }

    private void loadStatsUsers() {

        Set<Integer> users = new HashSet<>();

        int critical = 0;
        int risk = 0;
        int stable = 0;

        Map<String, Double> totalMap = new TreeMap<>();
        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> outcomeMap = new TreeMap<>();

        for (transaction t : st.afficher()) {

            users.add(t.getUser_id());

            String role = String.valueOf(t.getUser_id());

            totalMap.put(role,
                    totalMap.getOrDefault(role, 0.0) + t.getMontant());

            if (t.getMontant() >= 0)
                incomeMap.put(role,
                        incomeMap.getOrDefault(role, 0.0) + t.getMontant());
            else
                outcomeMap.put(role,
                        outcomeMap.getOrDefault(role, 0.0) + t.getMontant());
        }

        for (String role : totalMap.keySet()) {

            double income = incomeMap.getOrDefault(role, 0.0);
            double outcome = outcomeMap.getOrDefault(role, 0.0);
            double total = totalMap.get(role);

            String status = calculateStatus(income, outcome, total);

            switch (status) {
                case "CRITICAL" -> critical++;
                case "RISK" -> risk++;
                case "STABLE" -> stable++;
            }
        }

        statsContainer.getChildren().clear();

        statsContainer.getChildren().add(
                createStatCard("Users", users.size(), "#6a0dad"));

        statsContainer.getChildren().add(
                createStatCard("Transactions", st.afficher().size(), "#8e44ad"));

        statsContainer.getChildren().add(
                createStatCard("Critical", critical, "#e74c3c"));

        statsContainer.getChildren().add(
                createStatCard("Risk", risk, "#f39c12"));

        statsContainer.getChildren().add(
                createStatCard("Stable", stable, "#2ecc71"));
    }

    @FXML
    private void loadCurrencyRates() {

        if (!currencyRatesLabel.getText().isEmpty()) {
            currencyRatesLabel.setText("");
            return;
        }

        try {

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.er-api.com/v6/latest/TND"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            org.json.JSONObject json = new org.json.JSONObject(response.body());

            org.json.JSONObject rates = json.getJSONObject("rates");

            double eur = rates.getDouble("EUR");
            double usd = rates.getDouble("USD");
            double gbp = rates.getDouble("GBP");
            double cad = rates.getDouble("CAD");
            double jpy = rates.getDouble("JPY");

            currencyRatesLabel.setText(
                    "Base: TND\n\n" +

                            "1 TND = " + eur + " EUR\n" +
                            "1 TND = " + usd + " USD\n" +
                            "1 TND = " + gbp + " GBP\n" +
                            "1 TND = " + cad + " CAD\n" +
                            "1 TND = " + jpy + " JPY\n\n" +

                            "Inverse:\n\n" +
                            "1 EUR = " + (1 / eur) + " TND\n" +
                            "1 USD = " + (1 / usd) + " TND\n" +
                            "1 GBP = " + (1 / gbp) + " TND\n" +
                            "1 CAD = " + (1 / cad) + " TND\n" +
                            "1 JPY = " + (1 / jpy) + " TND");

        } catch (Exception e) {
            currencyRatesLabel.setText("Erreur API devises");
        }
    }


}