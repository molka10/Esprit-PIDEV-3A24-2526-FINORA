package com.example.gestionwallet.controllers;

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
import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import java.time.LocalDate;
import java.time.LocalTime;
import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.services.servicetransaction;


public class WalletController {

    @FXML private Label balanceLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalOutcomeLabel;

    @FXML private LineChart<String, Number> incomeChart;
    @FXML private LineChart<String, Number> outcomeChart;

    private final servicetransaction st = new servicetransaction();

    private double balance = 0;

    @FXML
    public void initialize() {

        incomeChart.setLegendVisible(false);
        outcomeChart.setLegendVisible(false);

        loadTransactions();
    }


    // ================= LOAD DATA =================
    public void loadTransactions() {

        incomeChart.getData().clear();
        outcomeChart.getData().clear();

        double totalIncome = 0;
        double totalOutcome = 0;
        balance = 0;

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> outcomeSeries = new XYChart.Series<>();

        for (transaction t : st.afficher()) {

            transaction currentTransaction = t;

            String type = currentTransaction.getType().toUpperCase();

            String categorie = currentTransaction.getCategorie();
            if (categorie == null || categorie.trim().isEmpty()) {
                categorie = "Sans categorie";
            }

            double montant = currentTransaction.getMontant();

            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(categorie, Math.abs(montant));

            if (type.equals("INCOME")) {

                incomeSeries.getData().add(data);
                totalIncome += montant;
                balance += montant;

            } else if (type.equals("OUTCOME")) {

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
        }

        incomeChart.getData().add(incomeSeries);
        outcomeChart.getData().add(outcomeSeries);

        totalIncomeLabel.setText(totalIncome + " DT");
        totalOutcomeLabel.setText(totalOutcome + " DT");

        updateBalance();
    }


    // ================= BALANCE =================

    private void updateBalance() {

        balanceLabel.setText( balance + " DT");

        if (balance >= 0) {
            balanceLabel.setStyle("-fx-font-size:24; -fx-font-weight:bold; -fx-text-fill:#6a0dad;");
        } else {
            balanceLabel.setStyle("-fx-font-size:24; -fx-font-weight:bold; -fx-text-fill:#e74c3c;");
        }
    }

    // ================= DETAILS =================

    private void showTransactionDetails(transaction t) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Transaction");

        Label title = new Label(t.getNom_transaction());
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#6a0dad;");

        Label montant = new Label("Montant : " + t.getMontant() + " DT");
        montant.setStyle(
                t.getType().equals("INCOME")
                        ? "-fx-text-fill:#2ecc71; -fx-font-weight:bold;"
                        : "-fx-text-fill:#e74c3c; -fx-font-weight:bold;"
        );

        Label categorie = new Label("Catégorie : " + t.getCategorie());
        Label type = new Label("Type : " + t.getType());

        Button modifier = new Button("Modifier");
        modifier.setStyle("-fx-background-color:#8e44ad; -fx-text-fill:white; -fx-background-radius:10;");

        Button supprimer = new Button("Supprimer");
        supprimer.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:10;");

        Button close = new Button("Fermer");
        close.setStyle("-fx-background-color:#dcd6f7; -fx-text-fill:#6a0dad; -fx-background-radius:10;");

        HBox buttons = new HBox(15, modifier, supprimer, close);

        VBox root = new VBox(15, title, montant, categorie, type, buttons);
        root.setStyle("""
        -fx-padding:25;
        -fx-background-color:white;
        -fx-background-radius:20;
        -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.15), 20,0,0,5);
    """);

        Scene scene = new Scene(root, 350, 220);
        stage.setScene(scene);

        // Actions
        supprimer.setOnAction(e -> {
            st.supprimer(t.getId_transaction());
            loadTransactions();
            stage.close();
        });

        modifier.setOnAction(e -> {
            stage.close();
            openEditPopup(t);
        });

        close.setOnAction(e -> stage.close());

        stage.showAndWait();
    }


    // ================= EDIT POPUP =================
    private void openEditPopup(transaction t) {

        Stage dialogStage = new Stage();
        dialogStage.initOwner(balanceLabel.getScene().getWindow());
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Modifier Transaction");

        Label title = new Label("Modifier Transaction");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#6a0dad;");

        // ---------- NOM ----------
        Label nameLabel = new Label("Nom");
        nameLabel.setStyle("-fx-text-fill:#4b0082;");

        TextField nameField = new TextField(t.getNom_transaction());
        nameField.setStyle("-fx-background-radius:10; -fx-padding:8;");

        // ---------- MONTANT ----------
        Label amountLabel = new Label("Montant");
        amountLabel.setStyle("-fx-text-fill:#4b0082;");

        TextField amountField =
                new TextField(String.valueOf(Math.abs(t.getMontant())));
        amountField.setStyle("-fx-background-radius:10; -fx-padding:8;");

        // ---------- DATE ----------
        Label dateLabel = new Label("Date");
        dateLabel.setStyle("-fx-text-fill:#4b0082;");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(t.getDate_transaction().toLocalDate());
        datePicker.setStyle("-fx-background-radius:10;");

        // ---------- BOUTONS ----------
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

        Scene scene = new Scene(card, 350, 400);
        dialogStage.setScene(scene);
        nameField.requestFocus();

        // ================= SAVE ACTION =================

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


    // ================= ADD =================

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
                    getClass().getResource("/com/example/gestionwallet/add-transaction.fxml")
            );

            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());

            AddTransactionController controller = loader.getController();
            controller.setType(type);
            controller.setParentController(this);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADMIN =================

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

    // ================= NAVIGATION =================

    @FXML
    private void goToAnalyse() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/gestionwallet/analyse.fxml")
            );

            Parent root = loader.load();
            Stage stage = (Stage) balanceLabel.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ERROR =================

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

        calendarView.showWeekPage();
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowSourceTray(false);

        Calendar incomeCal = new Calendar("Income");
        incomeCal.setStyle(Calendar.Style.STYLE2); // green

        Calendar outcomeCal = new Calendar("Outcome");
        outcomeCal.setStyle(Calendar.Style.STYLE3); // red

        for (transaction t : st.afficher()) {

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
        scene.getStylesheets().add(getClass().getResource("/com/example/gestionwallet/dashboard.css").toExternalForm());
        scene.getRoot().setStyle("-fx-background-radius:20;");
        stage.setResizable(false);

    }


}
