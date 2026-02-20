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





import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileOutputStream;

import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.services.servicetransaction;

public class AdminController {

    @FXML private VBox transactionContainer;
    @FXML private VBox walletContainer;

    @FXML private VBox totalBox;
    @FXML private VBox incomeBox;
    @FXML private VBox outcomeBox;
    @FXML private VBox countBox;

    @FXML private TextField userSearchField;

    @FXML private ComboBox<String> filterBox;
    @FXML private DatePicker datePicker;
    @FXML private PieChart walletChart;
    @FXML private LineChart<String, Number> transactionLineChart;


    private servicetransaction st = new servicetransaction();

    @FXML
    public void initialize() {

        filterBox.getItems().addAll("All", "INCOME", "OUTCOME");
        filterBox.setValue("All");

        loadTransactions();
        loadWallet();
        loadChart();
        loadLineChart();


    }

    // ================= TRANSACTIONS =================
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
                createCell("User",120),
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

    // ================= WALLET + 4 STATS =================
    private void loadWallet(){

        walletContainer.getChildren().clear();

        double total = 0;
        double income = 0;
        double outcome = 0;
        int count = 0;

        for(transaction t : st.afficher()){
            total += t.getMontant();
            if(t.getMontant() >= 0) income += t.getMontant();
            else outcome += t.getMontant();
            count++;
        }

        // ===== HEADER ROW =====
        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setSpacing(30);
        header.setStyle("-fx-background-color:#8e44ad; -fx-background-radius:10;");

        header.getChildren().addAll(
                createHeaderLabel("User",100),
                createHeaderLabel("Role",120),
                createHeaderLabel("Total",100),
                createHeaderLabel("Income",100),
                createHeaderLabel("Outcome",100),
                createHeaderLabel("Transactions",120)
        );

        walletContainer.getChildren().add(header);

        // ===== DATA ROW =====
        HBox row = new HBox();
        row.setPadding(new Insets(10));
        row.setSpacing(30);
        row.setStyle("-fx-background-color:#f3f0fa; -fx-background-radius:10;");

        Label user = createCell("User",100);
        Label role = createCell("Utilisateur",120);
        Label totalLabel = createCell(total + " DT",100);
        Label incomeLabel = createCell(income + " DT",100);
        Label outcomeLabel = createCell(outcome + " DT",100);
        Label countLabel = createCell(String.valueOf(count),120);

        incomeLabel.setStyle("-fx-text-fill:#2ecc71; -fx-font-weight:bold;");
        outcomeLabel.setStyle("-fx-text-fill:#e74c3c; -fx-font-weight:bold;");
        totalLabel.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");
        countLabel.setStyle("-fx-text-fill:#6a0dad; -fx-font-weight:bold;");

        row.getChildren().addAll(
                user,
                role,
                totalLabel,
                incomeLabel,
                outcomeLabel,
                countLabel
        );

        walletContainer.getChildren().add(row);
    }


    // ================= PDF =================
    @FXML
    private void downloadPDF() {

        // ===== CHOICE DIALOG =====
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Wallet",
                "Wallet",
                "Transactions",
                "Wallet + Transactions");

        dialog.setTitle("Choix PDF");
        dialog.setHeaderText("Que veux-tu exporter ?");
        dialog.setContentText("Choisir :");

        // ✅ appliquer ton dashboard.css
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/gestionwallet/dashboard.css").toExternalForm()
        );


        var result = dialog.showAndWait();

        if (result.isEmpty()) return;

        String choix = result.get();

        try {

            // ===== FILE CHOOSER =====
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );

            File file = fc.showSaveDialog(null);
            if (file == null) return;

            // ===== PDF CREATION =====
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // ===== BIG TITLE =====
            BaseColor violet = new BaseColor(106,13,173);
            Font bigTitle = new Font(Font.FontFamily.HELVETICA,22,Font.BOLD,violet);

            Paragraph title = new Paragraph("ADMIN DASHBOARD", bigTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Generated on: " + java.time.LocalDate.now()));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            // ===== WALLET =====
            if (choix.equals("Wallet") || choix.equals("Wallet + Transactions")) {
                generateWalletSection(doc);
            }

            // ===== TRANSACTIONS =====
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

        Font titleFont = new Font(Font.FontFamily.HELVETICA,18,Font.BOLD,violet);
        Font headerFont = new Font(Font.FontFamily.HELVETICA,12,Font.BOLD,BaseColor.WHITE);
        Font normalFont = new Font(Font.FontFamily.HELVETICA,11);
        Font greenFont = new Font(Font.FontFamily.HELVETICA,11,Font.BOLD,green);
        Font redFont = new Font(Font.FontFamily.HELVETICA,11,Font.BOLD,red);

        Paragraph title = new Paragraph("WALLET SUMMARY", titleFont);
        doc.add(title);
        doc.add(new Paragraph(" "));

        double total=0,income=0,outcome=0;
        int count=0;

        for(transaction t: st.afficher()){
            total+=t.getMontant();
            if(t.getMontant()>=0) income+=t.getMontant();
            else outcome+=t.getMontant();
            count++;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        String[] headers = {"User","Role","Total","Income","Outcome","Transactions"};

        for(String h:headers){
            PdfPCell cell = new PdfPCell(new Phrase(h,headerFont));
            cell.setBackgroundColor(violet);
            table.addCell(cell);
        }

        table.addCell(new Phrase("User",normalFont));
        table.addCell(new Phrase("Utilisateur",normalFont));
        table.addCell(new Phrase(total+" DT",normalFont));
        table.addCell(new Phrase(income+" DT",greenFont));
        table.addCell(new Phrase(outcome+" DT",redFont));
        table.addCell(new Phrase(String.valueOf(count),normalFont));

        doc.add(table);
        doc.add(new Paragraph(" "));
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

            table.addCell(new Phrase("User",normalFont));
            table.addCell(new Phrase(t.getType(),normalFont));

            if(t.getMontant()>=0)
                table.addCell(new Phrase(t.getMontant()+" DT",greenFont));
            else
                table.addCell(new Phrase(t.getMontant()+" DT",redFont));

            table.addCell(new Phrase(t.getDate_transaction().toString(),normalFont));
        }

        doc.add(table);
    }





    // ================= HELPERS =================
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

        // ===== TRANSACTIONS =====
        transactionContainer.getChildren().clear();

        for(transaction t : st.afficher()){

            if(userSearch == null || userSearch.isEmpty()){
                addTransactionRow(t);
            }
            else{
                if("User".toLowerCase()
                        .contains(userSearch.toLowerCase())){
                    addTransactionRow(t);
                }
            }
        }

        // ===== WALLET =====
        walletContainer.getChildren().clear();

        if(userSearch == null || userSearch.isEmpty()){
            loadWallet();
            return;
        }

        if("User".toLowerCase()
                .contains(userSearch.toLowerCase())){
            loadWallet();
        }
    }
    @FXML
    private void resetSearch(){

        // vider champ recherche
        userSearchField.clear();

        // recharger transactions normales
        loadTransactions();

        // recharger wallet normal
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

        // éviter division par zéro
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

        data.get(0).getNode().setStyle("-fx-pie-color: #6a0dad;");   // violet foncé
        data.get(1).getNode().setStyle("-fx-pie-color: #b39ddb;");   // violet clair

    }
    private void loadLineChart() {

        transactionLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total par date");

        // Map pour regrouper par date
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

        // Ajouter au chart
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

        // Style violet
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


}
