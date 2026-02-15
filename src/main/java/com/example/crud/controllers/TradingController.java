package com.example.crud.controllers;

import com.example.crud.models.Action;
import com.example.crud.models.Bourse;
import com.example.crud.services.ServiceAction;
import com.example.crud.services.ServiceBourse;
import com.example.crud.services.ServiceTransaction;
import com.example.crud.utils.Session;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class TradingController {

    @FXML private Label lblRole;

    @FXML private FlowPane boursesContainer;
    @FXML private FlowPane actionsContainer;

    @FXML private Label lblBourseSelected;
    @FXML private Label lblActionSelected;
    @FXML private Label lblActionsTitle;

    @FXML private TextField tfQuantiteTrade;
    @FXML private TextField searchField;
    @FXML private Label lblMsg;

    private final ServiceBourse serviceBourse = new ServiceBourse();
    private final ServiceAction serviceAction = new ServiceAction();
    private final ServiceTransaction serviceTransaction = new ServiceTransaction();

    private Bourse selectedBourse = null;
    private Action selectedAction = null;

    private List<Action> cachedActions;

    @FXML
    public void initialize() {
        lblRole.setText("Profil : " + Session.getRole() + " (" + Session.getDisplayName() + ")");
        chargerBourses();
        chargerActions();
    }

    private void chargerBourses() {
        Task<List<Bourse>> task = new Task<>() {
            @Override protected List<Bourse> call() { return serviceBourse.getAll(); }
        };

        task.setOnSucceeded(e -> renderBourses(task.getValue()));
        task.setOnFailed(e -> showError("Erreur bourses : " + task.getException().getMessage()));

        new Thread(task).start();
    }

    private void chargerActions() {
        Task<List<Action>> task = new Task<>() {
            @Override protected List<Action> call() { return serviceAction.getAll(); }
        };

        task.setOnSucceeded(e -> {
            cachedActions = task.getValue();
            // si bourse sélectionnée => filtrer
            if (selectedBourse != null) {
                renderActions(cachedActions.stream()
                        .filter(a -> a.getIdBourse() == selectedBourse.getIdBourse())
                        .collect(Collectors.toList()));
            } else {
                renderActions(cachedActions);
            }
        });

        task.setOnFailed(e -> showError("Erreur actions : " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void renderBourses(List<Bourse> bourses) {
        boursesContainer.getChildren().clear();

        if (bourses == null || bourses.isEmpty()) {
            boursesContainer.getChildren().add(simpleEmpty("⚠️ Aucune bourse"));
            return;
        }

        for (Bourse b : bourses) {
            boursesContainer.getChildren().add(buildBourseCard(b));
        }
    }

    private void renderActions(List<Action> actions) {
        actionsContainer.getChildren().clear();

        if (actions == null || actions.isEmpty()) {
            actionsContainer.getChildren().add(simpleEmpty("⚠️ Aucune action"));
            return;
        }

        for (Action a : actions) {
            actionsContainer.getChildren().add(buildActionCard(a));
        }
    }

    private VBox buildBourseCard(Bourse b) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setPrefWidth(320);
        card.setCursor(Cursor.HAND);

        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-color:#dfe6e9;" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
        );

        Label title = new Label("🏦 " + b.getNomBourse());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:15px; -fx-text-fill:#2c3e50;");

        Label pays = new Label("📍 " + b.getPays());
        Label devise = new Label("💰 " + b.getDevise());
        Label statut = new Label("📌 " + b.getStatut());

        pays.setStyle("-fx-text-fill:#34495e;");
        devise.setStyle("-fx-text-fill:#34495e;");
        statut.setStyle("-fx-text-fill:#34495e;");

        card.setOnMouseClicked(e -> {
            selectedBourse = b;
            selectedAction = null;
            lblBourseSelected.setText(b.getNomBourse() + " (" + b.getPays() + ")");
            lblActionSelected.setText("Aucune");
            lblActionsTitle.setText("📈 Actions de " + b.getNomBourse());

            if (cachedActions != null) {
                List<Action> filtered = cachedActions.stream()
                        .filter(a -> a.getIdBourse() == b.getIdBourse())
                        .collect(Collectors.toList());
                renderActions(filtered);
            }
        });

        card.getChildren().addAll(title, pays, devise, statut);
        return card;
    }

    private VBox buildActionCard(Action a) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setPrefWidth(310);
        card.setCursor(Cursor.HAND);

        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-color:#dfe6e9;" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
        );

        Label title = new Label("📈 " + a.getSymbole() + " - " + a.getNomEntreprise());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:15px; -fx-text-fill:#2c3e50;");

        Label prix = new Label("💰 Prix : " + String.format("%.2f", a.getPrixUnitaire()));
        Label qte = new Label("📦 Dispo : " + a.getQuantiteDisponible());
        Label statut = new Label("📌 " + a.getStatut());

        prix.setStyle("-fx-text-fill:#34495e;");
        qte.setStyle("-fx-text-fill:#34495e;");
        statut.setStyle("-fx-text-fill:#34495e;");

        card.setOnMouseClicked(e -> {
            selectedAction = a;
            lblActionSelected.setText(a.getSymbole() + " - " + a.getNomEntreprise());
            lblMsg.setText("");
        });

        card.getChildren().addAll(title, prix, qte, statut);
        return card;
    }

    private Label simpleEmpty(String txt) {
        Label l = new Label(txt);
        l.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:14px;");
        return l;
    }

    // =========================
    // BUY / SELL (DB + transaction)
    // =========================
    @FXML
    private void acheter(ActionEvent event) {
        if (selectedAction == null) { showError("Sélectionne une action."); return; }
        int q = parseQty();
        if (q <= 0) return;

        try {
            // ✅ Update DB + insert transaction
            serviceTransaction.acheter(
                    selectedAction.getIdAction(),
                    q,
                    Session.getRole().name(),       // INVESTISSEUR/ENTREPRISE
                    Session.getDisplayName()
            );

            lblMsg.setText("🟢 Achat OK : " + q + " x " + selectedAction.getSymbole());
            showSuccess("Achat effectué ✅");

            // ✅ refresh list to see new quantity
            chargerActions();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void vendre(ActionEvent event) {
        if (selectedAction == null) { showError("Sélectionne une action."); return; }
        int q = parseQty();
        if (q <= 0) return;

        try {
            // ✅ Update DB + insert transaction
            serviceTransaction.vendre(
                    selectedAction.getIdAction(),
                    q,
                    Session.getRole().name(),
                    Session.getDisplayName()
            );

            lblMsg.setText("🔴 Vente OK : " + q + " x " + selectedAction.getSymbole());
            showSuccess("Vente effectuée ✅");

            // ✅ refresh list to see new quantity
            chargerActions();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private int parseQty() {
        String s = tfQuantiteTrade.getText().trim();
        if (s.isEmpty()) { showError("Entre une quantité."); return -1; }
        try {
            int q = Integer.parseInt(s);
            if (q <= 0) { showError("Quantité doit être > 0"); return -1; }
            return q;
        } catch (Exception e) {
            showError("Quantité invalide");
            return -1;
        }
    }

    // =========================
    // SEARCH + RESET
    // =========================
    @FXML
    private void rechercher(ActionEvent event) {
        String k = searchField.getText().trim().toLowerCase();
        if (k.isEmpty()) { showError("Entrez un mot-clé"); return; }
        if (cachedActions == null) return;

        List<Action> filtered = cachedActions.stream()
                .filter(a -> a.getSymbole().toLowerCase().contains(k)
                        || a.getNomEntreprise().toLowerCase().contains(k))
                .collect(Collectors.toList());

        selectedBourse = null;
        selectedAction = null;
        lblBourseSelected.setText("Aucune");
        lblActionSelected.setText("Aucune");
        lblActionsTitle.setText("📈 Actions (résultats recherche)");
        renderActions(filtered);
    }

    @FXML
    private void actualiser(ActionEvent event) {
        searchField.clear();
        tfQuantiteTrade.clear();
        lblMsg.setText("");

        selectedBourse = null;
        selectedAction = null;
        lblBourseSelected.setText("Aucune");
        lblActionSelected.setText("Aucune");
        lblActionsTitle.setText("📈 Actions (toutes)");

        chargerBourses();
        chargerActions();
    }

    @FXML
    private void retourProfil(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/crud/utilisateur-static-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FINORA - Sélection du Profil");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur retour : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
