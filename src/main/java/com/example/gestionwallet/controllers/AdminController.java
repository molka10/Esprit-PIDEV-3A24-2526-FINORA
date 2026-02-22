package com.example.gestionwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Phrase;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import java.io.File;
import java.io.FileOutputStream;
import java.net.http.*;
import java.util.Set;
import java.util.HashSet;
import java.net.URI;

import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.services.servicetransaction;

public class AdminController {

    @FXML private VBox transactionContainer;
    @FXML private VBox walletContainer;
    @FXML private TextField userSearchField;
    @FXML private ComboBox<String> filterBox;
    @FXML private DatePicker datePicker;
    @FXML private PieChart walletChart;
    @FXML private LineChart<String, Number> transactionLineChart;
    @FXML private Button backButton;
    @FXML private HBox statsContainer;
    @FXML private Label currencyRatesLabel;

    private final servicetransaction st = new servicetransaction();

    @FXML
    public void initialize() {

        filterBox.getItems().addAll("All", "INCOME", "OUTCOME");
        filterBox.setValue("All");

        loadTransactions();
        loadWallet();
        loadChart();
        loadLineChart();
        loadStatsUsers();
    }


    private void loadTransactions() {

        transactionContainer.getChildren().clear();

        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color:#8e44ad; -fx-background-radius:10;");

        header.getChildren().addAll(
                createHeaderLabel("User",120),
                createHeaderLabel("Type",120),
                createHeaderLabel("Montant",150),
                createHeaderLabel("Date",180)
        );

        transactionContainer.getChildren().add(header);

        for (transaction t : st.afficher()) {
            addTransactionRow(t);
        }

    }

    private void addTransactionRow(transaction t){

        HBox row = new HBox();
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");

        row.getChildren().addAll(
                createCell(t.getRole(),120),
                createCell(t.getType(),120),
                createMontantCell(t.getMontant(),150),
                createCell(t.getDate_transaction().toString(),180)
        );

        transactionContainer.getChildren().add(row);
    }

    @FXML
    private void applyFilter(){

        transactionContainer.getChildren().clear();

        String type = filterBox.getValue();
        var date = datePicker.getValue();

        for(transaction t : st.afficher()){

            boolean okType =
                    type.equals("All") ||
                            t.getType().equalsIgnoreCase(type);

            boolean okDate = true;

            if(date != null){
                okDate = t.getDate_transaction()
                        .toLocalDate()
                        .equals(date);
            }

            if(okType && okDate){
                addTransactionRow(t);
            }
        }
    }


    @FXML
    private void resetFilter(){
        filterBox.setValue("All");
        datePicker.setValue(null);
        loadTransactions();
    }


    private void loadWallet(){

        walletContainer.getChildren().clear();

        Map<String, Double> totalMap = new TreeMap<>();
        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> outcomeMap = new TreeMap<>();
        Map<String, Integer> countMap = new TreeMap<>();

        for(transaction t : st.afficher()){

            String role = t.getRole();

            if(role == null || role.trim().isEmpty()){
                continue;
            }

            totalMap.put(role,
                    totalMap.getOrDefault(role,0.0) + t.getMontant());

            if(t.getMontant() >= 0)
                incomeMap.put(role,
                        incomeMap.getOrDefault(role,0.0) + t.getMontant());
            else
                outcomeMap.put(role,
                        outcomeMap.getOrDefault(role,0.0) + t.getMontant());

            countMap.put(role,
                    countMap.getOrDefault(role,0) + 1);
        }

        HBox header = new HBox(30);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color:#8e44ad; -fx-background-radius:10;");

        header.getChildren().addAll(
                createHeaderLabel("Role",120),
                createHeaderLabel("Total",120),
                createHeaderLabel("Income",120),
                createHeaderLabel("Outcome",120),
                createHeaderLabel("Transactions",120)
        );

        walletContainer.getChildren().add(header);

        for(String role : totalMap.keySet()){

            HBox row = new HBox(30);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");

            Label roleLabel = createCell(role,120);
            Label totalLabel = createCell(totalMap.get(role)+" DT",120);
            Label incomeLabel = createCell(incomeMap.getOrDefault(role,0.0)+" DT",120);
            Label outcomeLabel = createCell(outcomeMap.getOrDefault(role,0.0)+" DT",120);
            Label countLabel = createCell(String.valueOf(countMap.get(role)),120);

            incomeLabel.setStyle("-fx-text-fill:#2ecc71; -fx-font-weight:bold;");
            outcomeLabel.setStyle("-fx-text-fill:#e74c3c; -fx-font-weight:bold;");
            totalLabel.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");
            countLabel.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");

            row.getChildren().addAll(
                    roleLabel,totalLabel,incomeLabel,outcomeLabel,countLabel
            );

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
                getClass().getResource("/com/example/gestionwallet/dashboard.css").toExternalForm()
        );


        var result = dialog.showAndWait();

        if (result.isEmpty()) return;

        String choix = result.get();

        try {


            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );

            File file = fc.showSaveDialog(null);
            if (file == null) return;


            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();


            BaseColor violet = new BaseColor(106,13,173);
            Font bigTitle = new Font(Font.FontFamily.HELVETICA,22,Font.BOLD,violet);

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

        BaseColor violet = new BaseColor(106,13,173);
        BaseColor green = new BaseColor(46,204,113);
        BaseColor red = new BaseColor(231,76,60);

        Font headerFont = new Font(Font.FontFamily.HELVETICA,12,Font.BOLD,BaseColor.WHITE);
        Font normalFont = new Font(Font.FontFamily.HELVETICA,11);
        Font greenFont = new Font(Font.FontFamily.HELVETICA,11,Font.BOLD,green);
        Font redFont = new Font(Font.FontFamily.HELVETICA,11,Font.BOLD,red);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        String[] headers = {"Role","Total","Income","Outcome","Transactions"};

        for(String h:headers){
            PdfPCell cell = new PdfPCell(new Phrase(h,headerFont));
            cell.setBackgroundColor(violet);
            table.addCell(cell);
        }

        Map<String, Double> totalMap = new TreeMap<>();
        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> outcomeMap = new TreeMap<>();
        Map<String, Integer> countMap = new TreeMap<>();

        for(transaction t: st.afficher()){

            String role = t.getRole();

            if(role == null || role.trim().isEmpty()){
                continue;
            }



            totalMap.put(role,
                    totalMap.getOrDefault(role,0.0) + t.getMontant());

            if(t.getMontant()>=0)
                incomeMap.put(role,
                        incomeMap.getOrDefault(role,0.0)+t.getMontant());
            else
                outcomeMap.put(role,
                        outcomeMap.getOrDefault(role,0.0)+t.getMontant());

            countMap.put(role,
                    countMap.getOrDefault(role,0)+1);
        }

        for(String role : totalMap.keySet()){

            table.addCell(new Phrase(role,normalFont));
            table.addCell(new Phrase(totalMap.get(role)+" DT",normalFont));
            table.addCell(new Phrase(incomeMap.getOrDefault(role,0.0)+" DT",greenFont));
            table.addCell(new Phrase(outcomeMap.getOrDefault(role,0.0)+" DT",redFont));
            table.addCell(new Phrase(String.valueOf(countMap.get(role)),normalFont));
        }

        doc.add(table);
    }

    private void generateTransactionSection(Document doc) throws Exception {

        BaseColor violet = new BaseColor(106,13,173);
        BaseColor green = new BaseColor(46,204,113);
        BaseColor red = new BaseColor(231,76,60);

        Font titleFont = new Font(Font.FontFamily.HELVETICA,18,Font.BOLD,violet);
        Font headerFont = new Font(Font.FontFamily.HELVETICA,12,Font.BOLD,BaseColor.WHITE);
        Font normalFont = new Font(Font.FontFamily.HELVETICA,11);
        Font greenFont = new Font(Font.FontFamily.HELVETICA,11,Font.BOLD,green);
        Font redFont = new Font(Font.FontFamily.HELVETICA,11,Font.BOLD,red);

        Paragraph title = new Paragraph("TRANSACTIONS DETAILS", titleFont);
        doc.add(title);
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        String[] headers = {"User","Type","Montant","Date"};

        for(String h:headers){
            PdfPCell cell = new PdfPCell(new Phrase(h,headerFont));
            cell.setBackgroundColor(violet);
            table.addCell(cell);
        }

        for(transaction t: st.afficher()){

            table.addCell(new Phrase(t.getRole(),normalFont));
            table.addCell(new Phrase(t.getType(),normalFont));

            if(t.getMontant()>=0)
                table.addCell(new Phrase(t.getMontant()+" DT",greenFont));
            else
                table.addCell(new Phrase(t.getMontant()+" DT",redFont));

            table.addCell(new Phrase(t.getDate_transaction().toString(),normalFont));
        }

        doc.add(table);
    }


    private Label createHeaderLabel(String t,double w){
        Label l=new Label(t);
        l.setPrefWidth(w);
        l.setStyle("-fx-text-fill:white; -fx-font-weight:bold;");
        return l;
    }

    private Label createCell(String t,double w){
        Label l=new Label(t);
        l.setPrefWidth(w);
        l.setStyle("-fx-text-fill:#4b0082;");
        return l;
    }

    private Label createMontantCell(double m,double w){
        Label l=new Label(m+" DT");
        l.setPrefWidth(w);
        l.setStyle("-fx-text-fill:"+(m>=0?"#2ecc71":"#e74c3c")+"; -fx-font-weight:bold;");
        return l;
    }

    @FXML
    private void rechercheUser(){

        String userSearch = userSearchField.getText();

        transactionContainer.getChildren().clear();
        walletContainer.getChildren().clear();

        if(userSearch == null || userSearch.isEmpty()){
            loadTransactions();
            loadWallet();
            return;
        }

        Map<String, Double> totalMap = new TreeMap<>();
        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> outcomeMap = new TreeMap<>();
        Map<String, Integer> countMap = new TreeMap<>();

        for(transaction t : st.afficher()){

            if(t.getRole() != null &&
                    t.getRole().toLowerCase().contains(userSearch.toLowerCase())){

                addTransactionRow(t);

                String role = t.getRole();

                totalMap.put(role,
                        totalMap.getOrDefault(role,0.0) + t.getMontant());

                if(t.getMontant() >= 0)
                    incomeMap.put(role,
                            incomeMap.getOrDefault(role,0.0) + t.getMontant());
                else
                    outcomeMap.put(role,
                            outcomeMap.getOrDefault(role,0.0) + t.getMontant());

                countMap.put(role,
                        countMap.getOrDefault(role,0) + 1);
            }
        }

        HBox header = new HBox(30);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color:#8e44ad; -fx-background-radius:10;");

        header.getChildren().addAll(
                createHeaderLabel("Role",120),
                createHeaderLabel("Total",120),
                createHeaderLabel("Income",120),
                createHeaderLabel("Outcome",120),
                createHeaderLabel("Transactions",120)
        );

        walletContainer.getChildren().add(header);


        for(String role : totalMap.keySet()){

            HBox row = new HBox(30);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");

            row.getChildren().addAll(
                    createCell(role,120),
                    createCell(totalMap.get(role)+" DT",120),
                    createCell(incomeMap.getOrDefault(role,0.0)+" DT",120),
                    createCell(outcomeMap.getOrDefault(role,0.0)+" DT",120),
                    createCell(String.valueOf(countMap.get(role)),120)
            );

            walletContainer.getChildren().add(row);
        }
    }

    @FXML
    private void resetSearch(){

        userSearchField.clear();

        loadTransactions();

        loadWallet();
    }

    private void loadChart(){

        double income = 0;
        double outcome = 0;

        for(transaction t : st.afficher()){
            if(t.getMontant() >= 0)
                income += t.getMontant();
            else
                outcome += Math.abs(t.getMontant());
        }

        double total = income + outcome;

        if(total == 0){
            walletChart.setData(null);
            return;
        }

        ObservableList<PieChart.Data> data =
                FXCollections.observableArrayList(
                        new PieChart.Data(
                                "Income (" + (int)((income/total)*100) + "%)",
                                income
                        ),
                        new PieChart.Data(
                                "Outcome (" + (int)((outcome/total)*100) + "%)",
                                outcome
                        )
                );
        walletChart.setData(data);

        walletChart.setAnimated(true);
        walletChart.setLegendSide(Side.BOTTOM);
        walletChart.setLabelsVisible(true);

        data.get(0).getNode().setStyle("-fx-pie-color: #6a0dad;");
        data.get(1).getNode().setStyle("-fx-pie-color: #b39ddb;");

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
                            + t.getMontant()
            );
        }

        for (Map.Entry<String, Double> entry : totalParDate.entrySet()) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        transactionLineChart.getData().add(series);

        transactionLineChart.setAnimated(true);
        transactionLineChart.setLegendVisible(false);
        transactionLineChart.setCreateSymbols(true);

        Platform.runLater(() -> {
            series.getNode().setStyle(
                    "-fx-stroke: #6a0dad; -fx-stroke-width: 3px;"
            );
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
                    getClass().getResource("/com/example/gestionwallet/integ.fxml")
            );

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private VBox createStatCard(String title, int value, String color) {

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle(
                "-fx-background-color:#f3f0fa;" +
                        "-fx-background-radius:15;" +
                        "-fx-border-radius:15;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-size:16px; -fx-font-weight:bold;");

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-size:22px; -fx-font-weight:bold;");

        box.getChildren().addAll(titleLabel, valueLabel);

        return box;
    }
    private void loadStatsUsers() {

        Set<String> users = new HashSet<>();
        Set<String> entreprises = new HashSet<>();

        for (transaction t : st.afficher()) {

            if (t.getRole() == null) continue;

            if (t.getRole().equalsIgnoreCase("USER")) {
                users.add(t.getRole());
            }
            else if (t.getRole().equalsIgnoreCase("ENTREPRISE")) {
                entreprises.add(t.getRole());
            }
        }

        int nbrUser = users.size();
        int nbrEntreprise = entreprises.size();

        statsContainer.getChildren().clear();

        statsContainer.getChildren().addAll(
                createStatCard("Users", nbrUser, "#6a0dad"),
                createStatCard("Entreprises", nbrEntreprise, "#8e44ad"),
                createStatCard("Total", nbrUser + nbrEntreprise, "#4b0082")
        );
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

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            org.json.JSONObject json =
                    new org.json.JSONObject(response.body());

            org.json.JSONObject rates =
                    json.getJSONObject("rates");

            double eur = rates.getDouble("EUR");
            double usd = rates.getDouble("USD");
            double gbp = rates.getDouble("GBP");
            double cad = rates.getDouble("CAD");
            double jpy = rates.getDouble("JPY");

            currencyRatesLabel.setText(
                    "Taux de change (Base: TND)\n\n" +
                            "EUR : " + eur + "\n" +
                            "USD : " + usd + "\n" +
                            "GBP : " + gbp + "\n" +
                            "CAD : " + cad + "\n" +
                            "JPY : " + jpy
            );

        } catch (Exception e) {
            currencyRatesLabel.setText("Erreur API devises");
        }
    }

}
