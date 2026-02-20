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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
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

            // ✅ Protection contre null
            String categorie = t.getCategorie();
            if (categorie == null || categorie.trim().isEmpty()) {
                categorie = "Sans categorie";
            }

            double montant = Math.abs(t.getMontant());

            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(categorie, montant);

            if ("INCOME".equals(t.getType())) {

                incomeSeries.getData().add(data);
                totalIncome += t.getMontant();
                balance += t.getMontant();

            } else {

                outcomeSeries.getData().add(data);
                totalOutcome += montant;
                balance += t.getMontant();
            }

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setOnMouseClicked(e ->
                            showTransactionDetails(t));
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
            balanceLabel.setStyle("-fx-font-size:24; -fx-font-weight:bold; -fx-text-fill:#2ecc71;");
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

        TextField amountField = new TextField(String.valueOf(Math.abs(t.getMontant())));
        amountField.setStyle("-fx-background-radius:10; -fx-padding:8;");

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
                buttons
        );

        card.setStyle("""
        -fx-padding:25;
        -fx-background-color:white;
        -fx-background-radius:20;
        -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.15), 20,0,0,5);
    """);

        Scene scene = new Scene(card, 350, 320);
        dialogStage.setScene(scene);

        saveBtn.setOnAction(ev -> {

            String newName = nameField.getText();
            String amountText = amountField.getText();

            if (newName.isEmpty() || amountText.isEmpty()) {
                showError("Champs obligatoires !");
                return;
            }

            double newAmount = Double.parseDouble(amountText);

            if ("OUTCOME".equalsIgnoreCase(t.getType())) {
                newAmount = -Math.abs(newAmount);
            }

            t.setNom_transaction(newName);
            t.setMontant(newAmount);

            st.modifier(t);
            loadTransactions();
            dialogStage.close();
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

        Calendar incomeCal = new Calendar("Income");
        incomeCal.setStyle(Calendar.Style.STYLE2);

        Calendar outcomeCal = new Calendar("Outcome");
        outcomeCal.setStyle(Calendar.Style.STYLE3);

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

        Scene scene = new Scene(calendarView, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

}
