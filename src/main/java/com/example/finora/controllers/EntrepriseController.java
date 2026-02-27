package com.example.finora.controllers;

import com.example.finora.services.PredictionService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import com.example.finora.entities.transaction;
import com.example.finora.services.servicetransaction;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import javafx.scene.layout.FlowPane;
import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.*;
import com.example.finora.entities.categorie;
import com.example.finora.services.servicecategorie;

public class EntrepriseController {

    @FXML private Label balanceLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalOutcomeLabel;
    @FXML private LineChart<String, Number> incomeChart;
    @FXML private LineChart<String, Number> outcomeChart;
    @FXML private PieChart pieChart;
    @FXML private Label averageLabel;
    @FXML private FlowPane monthlyContainer;
    @FXML private Label totalTransactionsLabel;
    @FXML private TextField itemField;
    @FXML private TextField priceField;
    @FXML private ListView<HBox> wishlistView;
    @FXML private Button backButton;
    @FXML private ComboBox<String> currencyBox;
    @FXML private Label aiResultLabel;
    @FXML private VBox incomeContainer;
    @FXML private VBox outcomeContainer;

    private ObservableList<HBox> wishlist = FXCollections.observableArrayList();
    private String currentCurrency = "DT";
    private double conversionRate = 1.0;
    private final servicetransaction st = new servicetransaction();
    private servicecategorie sc = new servicecategorie();
    private double balance = 0;
    private int currentEntrepriseId = 2;


    @FXML
    public void initialize() {

        incomeChart.setLegendVisible(false);
        outcomeChart.setLegendVisible(false);

        loadTransactions();
        wishlistView.setItems(wishlist);
        currencyBox.getItems().addAll("DT", "EUR", "USD");
        currencyBox.setValue("DT");
    }


    @FXML
    private void handleSearch() {

        loadTransactions();
    }
    private VBox createHeader() {

        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(30);

        Label h1 = new Label("Name");
        Label h2 = new Label("Category");
        Label h3 = new Label("Amount");
        Label h4 = new Label("Date");

        h1.setStyle("-fx-font-weight:bold;");
        h2.setStyle("-fx-font-weight:bold;");
        h3.setStyle("-fx-font-weight:bold;");
        h4.setStyle("-fx-font-weight:bold;");

        headerGrid.add(h1, 0, 0);
        headerGrid.add(h2, 1, 0);
        headerGrid.add(h3, 2, 0);
        headerGrid.add(h4, 3, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        ColumnConstraints col4 = new ColumnConstraints();

        col1.setHgrow(Priority.ALWAYS);
        col2.setHgrow(Priority.ALWAYS);
        col3.setHgrow(Priority.ALWAYS);
        col4.setHgrow(Priority.ALWAYS);

        headerGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        VBox headerCard = new VBox(headerGrid);
        headerCard.setStyle("""
        -fx-background-color:#eeeeee;
        -fx-padding:10;
        -fx-background-radius:10;
    """);

        return headerCard;
    }
    public void loadTransactions() {

        incomeChart.getData().clear();
        outcomeChart.getData().clear();
        incomeContainer.getChildren().clear();
        outcomeContainer.getChildren().clear();

        incomeContainer.getChildren().add(createHeader());
        outcomeContainer.getChildren().add(createHeader());
        int totalTransactions = 0;
        double totalIncome = 0;
        double totalOutcome = 0;
        balance = 0;
        double currentMonthIncome = 0;
        double previousMonthIncome = 0;

        double currentMonthOutcome = 0;
        double previousMonthOutcome = 0;

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int previousMonth = today.minusMonths(1).getMonthValue();
        int currentYear = today.getYear();
        Set<LocalDate> uniqueDays = new HashSet<>();

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> outcomeSeries = new XYChart.Series<>();


        for (transaction t : st.afficherParUser(currentEntrepriseId)) {

            transaction currentTransaction = t;

            int categoryId = currentTransaction.getCategory_id();
            categorie cat = sc.getById(categoryId);

            if (cat == null) continue;

            String categorie = cat.getNom();
            String categoryType = cat.getType();

            double montant = currentTransaction.getMontant();
            LocalDate date = currentTransaction.getDate_transaction().toLocalDate();
            uniqueDays.add(date);

            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(categorie, Math.abs(montant));

            if (categoryType.equalsIgnoreCase("INCOME")) {

                incomeSeries.getData().add(data);
                totalIncome += montant;
                balance += montant;

            } else if (categoryType.equalsIgnoreCase("OUTCOME")) {

                outcomeSeries.getData().add(data);
                totalOutcome += Math.abs(montant);
                balance += montant;
            }


            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setOnMouseClicked(e ->
                            showTransactionDetails(currentTransaction));
                }
            });
            totalTransactions++;
            totalTransactionsLabel.setText(String.valueOf(totalTransactions));
            // ===== TABLE ROW =====

            Label nameLabel = new Label(currentTransaction.getNom_transaction());
            Label categoryLabel = new Label(categorie);
            Label amountLabel = new Label(String.format("%.2f %s",
                    Math.abs(montant) * conversionRate,
                    currentCurrency));
            Label dateLabel = new Label(date.toString());

// Style
            nameLabel.setStyle("-fx-font-weight:bold;");
            categoryLabel.setStyle("-fx-text-fill:#6a0dad;");
            amountLabel.setStyle(
                    categoryType.equalsIgnoreCase("INCOME")
                            ? "-fx-text-fill:#2ecc71; -fx-font-weight:bold;"
                            : "-fx-text-fill:#e74c3c; -fx-font-weight:bold;"
            );
            dateLabel.setStyle("-fx-text-fill:#555;");

// Grid layout
            GridPane grid = new GridPane();
            grid.setHgap(30);

            grid.add(nameLabel, 0, 0);
            grid.add(categoryLabel, 1, 0);
            grid.add(amountLabel, 2, 0);
            grid.add(dateLabel, 3, 0);

            ColumnConstraints c1 = new ColumnConstraints();
            ColumnConstraints c2 = new ColumnConstraints();
            ColumnConstraints c3 = new ColumnConstraints();
            ColumnConstraints c4 = new ColumnConstraints();

            c1.setHgrow(Priority.ALWAYS);
            c2.setHgrow(Priority.ALWAYS);
            c3.setHgrow(Priority.ALWAYS);
            c4.setHgrow(Priority.ALWAYS);

            grid.getColumnConstraints().addAll(c1, c2, c3, c4);

            VBox card = new VBox(grid);
            card.setStyle("""
    -fx-background-color:white;
    -fx-padding:12;
    -fx-background-radius:12;
    -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06), 6,0,0,2);
""");

// click open popup
            card.setOnMouseClicked(e -> showTransactionDetails(currentTransaction));

// add to correct container
            if (categoryType.equalsIgnoreCase("INCOME")) {
                incomeContainer.getChildren().add(card);
            } else {
                outcomeContainer.getChildren().add(card);
            }

        }

        incomeChart.getData().add(incomeSeries);
        outcomeChart.getData().add(outcomeSeries);

        totalIncomeLabel.setText(String.format("%.2f %s",
                totalIncome * conversionRate, currentCurrency));
        totalOutcomeLabel.setText(
                String.format("%.2f %s",
                        totalOutcome * conversionRate,
                        currentCurrency)
        );

        updateBalance();


        double totalAll = totalIncome + totalOutcome;

        double incomePercent = 0;
        double outcomePercent = 0;

        if (totalAll > 0) {
            incomePercent = (totalIncome / totalAll) * 100;
            outcomePercent = (totalOutcome / totalAll) * 100;
        }

        ObservableList<PieChart.Data> pieData =
                FXCollections.observableArrayList(
                        new PieChart.Data(
                                "Income " + String.format("%.1f", incomePercent) + " %",
                                incomePercent
                        ),
                        new PieChart.Data(
                                "Outcome " + String.format("%.1f", outcomePercent) + " %",
                                outcomePercent
                        )
                );

        pieChart.setData(pieData);
        pieChart.setLegendVisible(false);

        if (pieChart.getData().size() >= 2) {
            pieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #8e44ad;");
            pieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #6a0dad;");
        }


        double averagePerDay = 0;

        if (!uniqueDays.isEmpty()) {
            averagePerDay = totalAll / uniqueDays.size();
        }
        averageLabel.setText(
                "Moyenne / jour : " +
                        String.format("%.2f %s",
                                averagePerDay * conversionRate,
                                currentCurrency)
        );

        monthlyContainer.getChildren().clear();

        Map<String, Double> monthlyIncome = new HashMap<>();
        Map<String, Double> monthlyOutcome = new HashMap<>();

        for (transaction t : st.afficherParUser(currentEntrepriseId)) {

            LocalDate date = t.getDate_transaction().toLocalDate();
            String month = date.getMonth().toString();

            double amount = t.getMontant();

            int categoryId = t.getCategory_id();
            categorie cat = sc.getById(categoryId);

            if (cat == null) continue;

            if (cat.getType().equalsIgnoreCase("INCOME")) {
                monthlyIncome.put(month,
                        monthlyIncome.getOrDefault(month, 0.0) + amount);
            } else {
                monthlyOutcome.put(month,
                        monthlyOutcome.getOrDefault(month, 0.0) + Math.abs(amount));
            }
        }

        Set<String> allMonths = new HashSet<>();
        allMonths.addAll(monthlyIncome.keySet());
        allMonths.addAll(monthlyOutcome.keySet());

        for (String month : allMonths) {

            double incomeValue = monthlyIncome.getOrDefault(month, 0.0);
            double outcomeValue = monthlyOutcome.getOrDefault(month, 0.0);

            Label monthLabel = new Label(month);
            monthLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#6a0dad;");

            Label incomeLabel = new Label(
                    String.format("Income : %.2f %s",
                            incomeValue * conversionRate,
                            currentCurrency)
            );            incomeLabel.setStyle("-fx-text-fill:#8e44ad;");

            Label outcomeLabel = new Label(
                    String.format("Outcome : %.2f %s",
                            outcomeValue * conversionRate,
                            currentCurrency)
            );            outcomeLabel.setStyle("-fx-text-fill:#6a0dad;");

            VBox monthCard = new VBox(5, monthLabel, incomeLabel, outcomeLabel);
            monthCard.setStyle("""
            -fx-background-color:white;
            -fx-padding:15;
            -fx-background-radius:15;
            -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.08), 10,0,0,3);
        """);

            monthlyContainer.getChildren().add(monthCard);
        }
    }

    private void updateBalance() {

        balanceLabel.setText(
                String.format("%.2f %s",
                        balance * conversionRate,
                        currentCurrency)
        );

        if (balance >= 0) {
            balanceLabel.setStyle("-fx-font-size:24; -fx-font-weight:bold; -fx-text-fill:#6a0dad;");
        } else {
            balanceLabel.setStyle("-fx-font-size:24; -fx-font-weight:bold; -fx-text-fill:#e74c3c;");
        }
    }



    private void showTransactionDetails(transaction t) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(balanceLabel.getScene().getWindow());
        stage.setTitle("Transaction");


        Label title = new Label(t.getNom_transaction());
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#6a0dad;");

        double convertedAmount = t.getMontant() * conversionRate;

        Label montant = new Label(
                String.format("Montant : %.2f %s",
                        convertedAmount,
                        currentCurrency)
        );        montant.setStyle(
                t.getType().equals("INCOME")
                        ? "-fx-text-fill:#2ecc71; -fx-font-weight:bold;"
                        : "-fx-text-fill:#e74c3c; -fx-font-weight:bold;"
        );

        Label categorie = new Label("Catégorie : " + t.getCategorie());
        Label type = new Label("Type : " + t.getType());
        Label date = new Label("Date : " + t.getDate_transaction().toLocalDate());

        Button modifier = new Button("Modifier");
        modifier.setStyle("-fx-background-color:#8e44ad; -fx-text-fill:white; -fx-background-radius:10;");

        Button supprimer = new Button("Supprimer");
        supprimer.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:10;");

        Button close = new Button("Fermer");
        close.setStyle("-fx-background-color:#dcd6f7; -fx-text-fill:#6a0dad; -fx-background-radius:10;");

        HBox buttons = new HBox(15, modifier, supprimer, close);

        VBox root = new VBox(15, title, montant, categorie, type, date, buttons);
        root.setStyle("""
        -fx-padding:25;
        -fx-background-color:white;
        -fx-background-radius:20;
        -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.15), 20,0,0,5);
    """);

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);


        supprimer.setOnAction(e -> {
            st.supprimer(t.getId_transaction());
            loadTransactions();
            stage.close();
        });

        modifier.setOnAction(e -> {
            openEditPopup(t);
            stage.close();
        });

        close.setOnAction(e -> stage.close());

        stage.show();
    }



    private void openEditPopup(transaction t) {

        Stage dialogStage = new Stage();
        dialogStage.initOwner(balanceLabel.getScene().getWindow());
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Modifier Transaction");

        Label title = new Label("Modifier Transaction");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#6a0dad;");

        Label nameLabel = new Label("Nom");
        nameLabel.setStyle("-fx-text-fill:#4b0082;");

        TextField nameField = new TextField(t.getNom_transaction());
        nameField.setStyle("-fx-background-radius:10; -fx-padding:8;");

        Label amountLabel = new Label("Montant");
        amountLabel.setStyle("-fx-text-fill:#4b0082;");

        TextField amountField =
                new TextField(String.valueOf(Math.abs(t.getMontant())));
        amountField.setStyle("-fx-background-radius:10; -fx-padding:8;");

        Label dateLabel = new Label("Date");
        dateLabel.setStyle("-fx-text-fill:#4b0082;");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(t.getDate_transaction().toLocalDate());
        datePicker.setStyle("-fx-background-radius:10;");
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (empty) return;

                if (date.isBefore(firstDayOfMonth) || date.isAfter(lastDayOfMonth)) {
                    setDisable(true);
                    setStyle("-fx-background-color:#f2f2f2;");
                }
            }
        });

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("""
        -fx-background-color:#8e44ad;
        -fx-text-fill:white;
        -fx-background-radius:10;
        -fx-padding:8 20;
    """);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("""
        -fx-background-color:#e8e3f8;
        -fx-text-fill:#6a0dad;
        -fx-background-radius:10;
        -fx-padding:8 20;
    """);

        HBox buttons = new HBox(15, cancelBtn, saveBtn);

        VBox card = new VBox(15,
                title,
                nameLabel, nameField,
                amountLabel, amountField,
                dateLabel, datePicker,
                buttons
        );

        card.setStyle("""
        -fx-padding:25;
        -fx-background-color:white;
        -fx-background-radius:20;
        -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.15), 20,0,0,5);
    """);

        Scene scene = new Scene(card, 400, 350);
        dialogStage.setScene(scene);
        nameField.requestFocus();


        saveBtn.setOnAction(ev -> {

            try {

                String newName = nameField.getText();
                String amountText = amountField.getText();

                if (newName.isEmpty() || amountText.isEmpty()
                        || datePicker.getValue() == null) {
                    showError("Champs obligatoires !");
                    return;
                }

                double newAmount = Double.parseDouble(amountText);

                if ("OUTCOME".equalsIgnoreCase(t.getType())) {
                    newAmount = -Math.abs(newAmount);
                }

                t.setNom_transaction(newName);
                t.setMontant(newAmount);
                t.setDate_transaction(
                        java.sql.Date.valueOf(datePicker.getValue())
                );

                System.out.println("ID: " + t.getId_transaction());
                System.out.println("Category ID = " + t.getCategory_id());
                st.modifier(t);

                System.out.println("UPDATE DONE");

                loadTransactions();
                dialogStage.close();

            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur pendant modification !");
            }
        });

        cancelBtn.setOnAction(ev -> dialogStage.close());

        dialogStage.showAndWait();
    }



    @FXML
    private void openAddIncome() {

        openAddTransaction("INCOME");
    }

    @FXML
    private void openAddOutcome() {

        openAddTransaction("OUTCOME");
    }

    private void openAddTransaction(String type) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/finora/add-transaction.fxml")
            );

            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());

            AddTransactionController controller = loader.getController();
            controller.setType(type);
            controller.setParentController(this);
            controller.setUserId(currentEntrepriseId);


            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void openHistorique() {

        Stage stage = new Stage();
        stage.setTitle("Historique");

        CalendarView calendarView = new CalendarView();

        calendarView.showMonthPage();
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowSourceTray(false);

        Calendar incomeCal = new Calendar("Income");
        incomeCal.setStyle(Calendar.Style.STYLE1);

        Calendar outcomeCal = new Calendar("Outcome");
        outcomeCal.setStyle(Calendar.Style.STYLE5);

        for (transaction t : st.afficherParUser(currentEntrepriseId)) {

            Entry<String> entry = new Entry<>(t.getNom_transaction());

            LocalDate date = t.getDate_transaction().toLocalDate();
            entry.changeStartDate(date);
            entry.changeEndDate(date);

            entry.changeStartTime(LocalTime.of(9, 0));
            entry.changeEndTime(LocalTime.of(10, 0));

            if ("INCOME".equalsIgnoreCase(t.getType())) {
                incomeCal.addEntry(entry);
            } else {
                outcomeCal.addEntry(entry);
            }
        }

        CalendarSource source = new CalendarSource("Transactions");
        source.getCalendars().addAll(incomeCal, outcomeCal);

        calendarView.getCalendarSources().add(source);

        Scene scene = new Scene(calendarView, 1000, 650);

        stage.setScene(scene);
        stage.show();
        scene.getStylesheets().add(getClass().getResource("/com/example/finora/dashboard.css").toExternalForm());
        scene.getRoot().setStyle("-fx-background-radius:20;");
        stage.setResizable(false);

    }
    @FXML
    private void exportPDF() {

        try {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer Rapport");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF files", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(balanceLabel.getScene().getWindow());
            if (file == null) return;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            BaseColor purpleDark = new BaseColor(106, 13, 173);
            BaseColor purpleMedium = new BaseColor(142, 68, 173);
            BaseColor purpleLight = new BaseColor(232, 224, 248);

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, purpleDark);
            Paragraph title = new Paragraph("WALLET REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Date d'export : " + java.time.LocalDate.now()));
            document.add(new Paragraph(" "));

            Font statFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, purpleMedium);

            document.add(new Paragraph("Total Income : " + totalIncomeLabel.getText(), statFont));
            document.add(new Paragraph("Total Outcome : " + totalOutcomeLabel.getText(), statFont));
            document.add(new Paragraph("Balance : " + balanceLabel.getText(), statFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));


            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);


            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);

            PdfPCell h1 = new PdfPCell(new Phrase("Nom", headerFont));
            PdfPCell h2 = new PdfPCell(new Phrase("Type", headerFont));
            PdfPCell h3 = new PdfPCell(new Phrase("Categorie", headerFont));
            PdfPCell h4 = new PdfPCell(new Phrase("Montant", headerFont));

            h1.setBackgroundColor(purpleDark);
            h2.setBackgroundColor(purpleDark);
            h3.setBackgroundColor(purpleDark);
            h4.setBackgroundColor(purpleDark);

            table.addCell(h1);
            table.addCell(h2);
            table.addCell(h3);
            table.addCell(h4);


            for (transaction t : st.afficherParUser(currentEntrepriseId)) {

                table.addCell(t.getNom_transaction());
                table.addCell(t.getType());
                table.addCell(t.getCategorie());

                Font amountFont;

                if ("INCOME".equalsIgnoreCase(t.getType())) {
                    amountFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, purpleMedium);
                } else {
                    amountFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, purpleDark);
                }

                PdfPCell amountCell = new PdfPCell(
                        new Phrase(String.valueOf(t.getMontant()) + " DT", amountFont)
                );

                amountCell.setBackgroundColor(purpleLight);

                table.addCell(amountCell);
            }

            document.add(table);
            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("PDF généré avec succès ");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur export PDF !");
        }
    }

    @FXML
    private void exportExcel() {

        try {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel files", "*.xlsx")
            );

            File file = fileChooser.showSaveDialog(balanceLabel.getScene().getWindow());
            if (file == null) return;


            org.apache.poi.ss.usermodel.Workbook workbook =
                    new org.apache.poi.xssf.usermodel.XSSFWorkbook();

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Transactions");

            org.apache.poi.ss.usermodel.CellStyle headerStyle =
                    workbook.createCellStyle();

            org.apache.poi.ss.usermodel.Font headerFont =
                    workbook.createFont();

            headerFont.setBold(true);
            headerStyle.setFont(headerFont);


            org.apache.poi.ss.usermodel.Row header =
                    sheet.createRow(0);

            String[] columns = {"Nom", "Type", "Categorie", "Montant", "Date"};

            for (int i = 0; i < columns.length; i++) {

                org.apache.poi.ss.usermodel.Cell cell =
                        header.createCell(i);

                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;

            for (transaction t : st.afficherParUser(currentEntrepriseId)) {

                org.apache.poi.ss.usermodel.Row row =
                        sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(t.getNom_transaction());
                row.createCell(1).setCellValue(t.getType());
                row.createCell(2).setCellValue(t.getCategorie());
                row.createCell(3).setCellValue(t.getMontant());
                row.createCell(4).setCellValue(
                        t.getDate_transaction().toLocalDate().toString()
                );
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.FileOutputStream fileOut =
                    new java.io.FileOutputStream(file);

            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Excel généré avec succès ");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur export Excel !");
        }
    }
    @FXML
    private void addWishlistItem() {

        try {

            String name = itemField.getText();
            String priceText = priceField.getText();

            if (name.isEmpty() || priceText.isEmpty()) return;

            double price = Double.parseDouble(priceText);

            Label itemLabel = new Label( name);
            itemLabel.setStyle("-fx-font-weight:bold;");

            Label priceLabel = new Label(price + " DT");
            priceLabel.setStyle("-fx-text-fill:#6a0dad;");

            Button deleteBtn = new Button("✖");
            deleteBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:20;");

            HBox itemBox = new HBox(15, itemLabel, priceLabel, deleteBtn);
            itemBox.setStyle("""
            -fx-background-color:#f9f6ff;
            -fx-padding:10;
            -fx-background-radius:10;
        """);

            deleteBtn.setOnAction(e -> wishlistView.getItems().remove(itemBox));

            wishlistView.getItems().add(itemBox);

            itemField.clear();
            priceField.clear();

        } catch (Exception e) {
            showError("Montant invalide !");
        }
    }

    private double getExchangeRate(String from, String to) {

        try {

            String url = "https://open.er-api.com/v6/latest/" + from;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            org.json.JSONObject json =
                    new org.json.JSONObject(response.body());

            org.json.JSONObject rates = json.getJSONObject("rates");

            return rates.getDouble(to);

        } catch (Exception e) {
            e.printStackTrace();
            return 1.0;
        }
    }
    @FXML
    private void changeCurrency() {

        currentCurrency = currencyBox.getValue();

        switch (currentCurrency) {

            case "EUR":
                conversionRate = getExchangeRate("TND", "EUR");
                break;

            case "USD":
                conversionRate = getExchangeRate("TND", "USD");
                break;

            default:
                conversionRate = 1.0;
        }

        loadTransactions();
    }


    @FXML
    private void goBack() {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/finora/integ.fxml"));

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void predictEntrepriseAI() {

        double totalIncome = 0;
        double totalOutcome = 0;

        for (transaction t : st.afficherParUser(currentEntrepriseId)) {
            if (t.getMontant() >= 0) {
                totalIncome += t.getMontant();
            } else {
                totalOutcome += Math.abs(t.getMontant());
            }
        }

        double net = totalIncome - totalOutcome;

        String prompt = """
Tu es un expert en analyse financière et en stratégie d’entreprise.

Analyse en profondeur les données financières suivantes :

Données mensuelles de l’entreprise :
- Revenus totaux : %.2f DT
- Dépenses totales : %.2f DT
- Résultat net : %.2f DT

Travail demandé :

1. Analyser la rentabilité de l’entreprise.
2. Évaluer la stabilité financière (équilibre revenus/dépenses).
3. Déterminer le niveau de risque financier (Faible, Moyen, Élevé) avec justification.
4. Estimer les dépenses probables du mois prochain.
5. Prédire les types d’achats ou investissements probables le mois prochain.
6. Indiquer s’il existe un risque de baisse de performance ou de déficit futur.
7. Donner un score de santé financière sur 100 avec justification.
8. Fournir 4 recommandations stratégiques concrètes.

Contraintes :
- Ne modifier aucun chiffre.
- Ne pas inventer de nouvelles données.
- Analyse logique basée uniquement sur les informations fournies.
- Réponse structurée avec titres clairs.
- Réponse 100%% en français.
""".formatted(totalIncome, totalOutcome, net);

        try {

            PredictionService ps = new PredictionService();
            String result = ps.callAI(prompt);

            String finalText = """
ANALYSE IA – ENTREPRISE

%s
""".formatted(result);

            aiResultLabel.setText(finalText);

        } catch (Exception e) {
            aiResultLabel.setText("Erreur IA : " + e.getMessage());
        }
    }



}
