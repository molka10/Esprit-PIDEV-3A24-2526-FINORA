package com.example.finora.controllers.bourse;

import com.example.finora.entities.Action;
import com.example.finora.entities.Bourse;
import com.example.finora.services.bourse.ServiceAction;
import com.example.finora.services.bourse.ServiceBourse;
import com.example.finora.services.bourse.ServiceTransactionBourse;
import com.example.finora.utils.Navigator;
import com.example.finora.utils.Session;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class TradingController {

    @FXML
    private Label lblRole;

    @FXML
    private FlowPane boursesContainer;
    @FXML
    private FlowPane actionsContainer;

    @FXML
    private Label lblBourseSelected;
    @FXML
    private Label lblActionSelected;
    @FXML
    private Label lblActionsTitle;

    @FXML
    private TextField tfQuantiteTrade;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblMsg;

    private final ServiceBourse serviceBourse = new ServiceBourse();
    private final ServiceAction serviceAction = new ServiceAction();
    private final ServiceTransactionBourse serviceTransaction = new ServiceTransactionBourse();

    private Bourse selectedBourse = null;
    private Action selectedAction = null;

    private List<Action> cachedActions;

    @FXML
    public void initialize() {
        if (!Session.isLoggedIn() || Session.getCurrentUser() == null) {
            lblRole.setText("Profil : Non connecté");
        } else {
            lblRole.setText(
                    "Profil : " + Session.getCurrentUser().getRole() + " (" + Session.getCurrentUser().getUsername() + ")");
        }

        chargerBourses();
        chargerActions();
    }

    private int currentUserIdSafe() {
        if (!Session.isLoggedIn() || Session.getCurrentUser() == null) return -1;
        return Session.getCurrentUser().getId();
    }

    private String currentUserRoleSafe() {
        if (!Session.isLoggedIn() || Session.getCurrentUser() == null) return "INVESTISSEUR";
        return Session.getCurrentUser().getRole();
    }

    private String currentUserLabelSafe() {
        if (!Session.isLoggedIn() || Session.getCurrentUser() == null) return "USER_STATIC";
        return Session.getCurrentUser().getUsername();
    }

    private void chargerBourses() {
        Task<List<Bourse>> task = new Task<>() {
            @Override
            protected List<Bourse> call() {
                return serviceBourse.getAll();
            }
        };

        task.setOnSucceeded(e -> renderBourses(task.getValue()));
        task.setOnFailed(e -> showError("Erreur bourses : " + task.getException().getMessage()));

        new Thread(task).start();
    }

    private void chargerActions() {
        Task<List<Action>> task = new Task<>() {
            @Override
            protected List<Action> call() {
                return serviceAction.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            cachedActions = task.getValue();
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
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");

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

            tfQuantiteTrade.clear();
            lblMsg.setText("");

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
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");

        Label title = new Label("📈 " + a.getSymbole() + " - " + a.getNomEntreprise());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:15px; -fx-text-fill:#2c3e50;");

        Label prix = new Label("💰 Prix : " + String.format("%.2f", a.getPrixUnitaire()));
        Label qte = new Label("📦 Dispo : " + a.getQuantiteDisponible());
        Label statut = new Label("📌 " + a.getStatut());

        prix.setStyle("-fx-text-fill:#34495e;");
        qte.setStyle("-fx-text-fill:#34495e;");
        statut.setStyle("-fx-text-fill:#34495e;");

        // ✅ IMPORTANT: afficher combien l'utilisateur possède de cette action
        card.setOnMouseClicked(e -> {
            selectedAction = a;

            int userId = currentUserIdSafe();
            int possede = 0;
            if (userId > 0) {
                possede = serviceTransaction.getQuantitePossedee(userId, a.getIdAction());
            }

            lblActionSelected.setText(
                    a.getSymbole() + " - " + a.getNomEntreprise() + " | Possédé: " + possede
            );

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
    // BUY / SELL
    // =========================
    @FXML
    private void acheter(ActionEvent event) {
        if (selectedAction == null) {
            showError("Sélectionne une action.");
            return;
        }

        int userId = currentUserIdSafe();
        if (userId <= 0) {
            showError("Utilisateur non connecté.");
            return;
        }

        int q = parseQty();
        if (q <= 0) return;

        int stockDispo = selectedAction.getQuantiteDisponible();
        if (q > stockDispo) {
            showError("Quantité insuffisante ! Stock disponible : " + stockDispo);
            return;
        }

        try {
            serviceTransaction.acheter(
                    userId,
                    selectedAction.getIdAction(),
                    q,
                    currentUserRoleSafe(),
                    currentUserLabelSafe()
            );

            lblMsg.setText("🟢 Achat OK : " + q + " x " + selectedAction.getSymbole());
            showSuccess("Achat effectué ✅");

            // ✅ refresh marché (stock) + refresh affichage possédé
            chargerActions();
            refreshPossedeLabel();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void vendre(ActionEvent event) {
        if (selectedAction == null) {
            showError("Sélectionne une action.");
            return;
        }

        int userId = currentUserIdSafe();
        if (userId <= 0) {
            showError("Utilisateur non connecté.");
            return;
        }

        int q = parseQty();
        if (q <= 0) return;

        try {
            int possede = serviceTransaction.getQuantitePossedee(userId, selectedAction.getIdAction());

            if (possede <= 0) {
                showError("Vous ne possédez aucune action à vendre (Possédé: 0).");
                return;
            }

            if (q > possede) {
                showError("Vente impossible ! Tu possèdes : " + possede + " actions.");
                return;
            }

            serviceTransaction.vendre(
                    userId,
                    selectedAction.getIdAction(),
                    q,
                    currentUserRoleSafe(),
                    currentUserLabelSafe()
            );

            lblMsg.setText("🔴 Vente OK : " + q + " x " + selectedAction.getSymbole());
            showSuccess("Vente effectuée ✅");

            // ✅ refresh marché (stock) + refresh affichage possédé
            chargerActions();
            refreshPossedeLabel();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void refreshPossedeLabel() {
        if (selectedAction == null) return;

        int userId = currentUserIdSafe();
        if (userId <= 0) return;

        int possede = serviceTransaction.getQuantitePossedee(userId, selectedAction.getIdAction());
        lblActionSelected.setText(
                selectedAction.getSymbole() + " - " + selectedAction.getNomEntreprise() + " | Possédé: " + possede
        );
    }

    private int parseQty() {
        String s = tfQuantiteTrade.getText().trim();
        if (s.isEmpty()) {
            showError("Entre une quantité.");
            return -1;
        }
        try {
            int q = Integer.parseInt(s);
            if (q <= 0) {
                showError("Quantité doit être > 0");
                return -1;
            }
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
        if (k.isEmpty()) {
            showError("Entrez un mot-clé");
            return;
        }
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

        tfQuantiteTrade.clear();
        lblMsg.setText("");

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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.returnToDashboard(stage);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
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