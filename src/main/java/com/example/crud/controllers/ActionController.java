package com.example.crud.controllers;

import com.example.crud.models.Action;
import com.example.crud.models.Bourse;
import com.example.crud.services.ServiceAction;
import com.example.crud.services.ServiceBourse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ActionController implements Initializable {

    @FXML private TextField tfId;
    @FXML private ComboBox<Bourse> cbBourse;
    @FXML private TextField tfSymbole;
    @FXML private TextField tfNomEntreprise;
    @FXML private ComboBox<String> cbSecteur;
    @FXML private TextField tfPrix;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbStatut;

    @FXML private TextField searchField;
    @FXML private Label lblTotal;
    @FXML private FlowPane cardsContainer;

    private final ServiceAction serviceAction = new ServiceAction();
    private final ServiceBourse serviceBourse = new ServiceBourse();

    private Integer pendingPreselectBourseId = null;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("db-executor");
        return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        cbSecteur.setItems(FXCollections.observableArrayList(
                "Technologie", "Finance", "Santé", "Énergie", "Consommation",
                "Industrie", "Immobilier", "Télécommunications", "Services", "Autre"
        ));
        cbStatut.setItems(FXCollections.observableArrayList("DISPONIBLE", "INDISPONIBLE"));

        cbSecteur.getSelectionModel().selectFirst();
        cbStatut.getSelectionModel().selectFirst();

        cbBourse.setConverter(new StringConverter<>() {
            @Override public String toString(Bourse b) {
                if (b == null) return "";
                return b.getNomBourse() + " (" + b.getDevise() + " - " + b.getPays() + ")";
            }
            @Override public Bourse fromString(String s) { return null; }
        });

        cbBourse.valueProperty().addListener((obs, o, n) -> {
            if (n == null) chargerActionsAsync(null);
            else chargerActionsAsync(n.getIdBourse());
        });

        cardsContainer.setHgap(15);
        cardsContainer.setVgap(15);

        chargerBoursesAsync();
        chargerActionsAsync(null);
    }

    // ✅ appelé depuis BourseController
    public void preselectionnerBourseParId(int idBourse) {
        pendingPreselectBourseId = idBourse;

        Platform.runLater(() -> {
            if (cbBourse.getItems() != null && !cbBourse.getItems().isEmpty()) {
                for (Bourse b : cbBourse.getItems()) {
                    if (b.getIdBourse() == idBourse) {
                        cbBourse.setValue(b); // listener => filtre
                        pendingPreselectBourseId = null;
                        break;
                    }
                }
            }
        });
    }

    // ✅ bouton retour
    @FXML
    private void retourBourses(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/crud/bourse-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Bourses");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur retour : " + e.getMessage());
        }
    }

    private void chargerBoursesAsync() {
        Task<List<Bourse>> task = new Task<>() {
            @Override protected List<Bourse> call() {
                return serviceBourse.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            List<Bourse> bourses = task.getValue();
            cbBourse.setItems(FXCollections.observableArrayList(bourses));

            if (pendingPreselectBourseId != null) {
                for (Bourse b : bourses) {
                    if (b.getIdBourse() == pendingPreselectBourseId) {
                        cbBourse.setValue(b); // filtre auto
                        pendingPreselectBourseId = null;
                        break;
                    }
                }
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Erreur bourses : " + (ex != null ? ex.getMessage() : "inconnue"));
            if (ex != null) ex.printStackTrace();
        });

        dbExecutor.submit(task);
    }

    private void chargerActionsAsync(Integer idBourseOrNull) {
        Task<List<Action>> task = new Task<>() {
            @Override protected List<Action> call() {
                List<Action> all = serviceAction.getAll();
                if (idBourseOrNull == null) return all;

                return all.stream()
                        .filter(a -> a.getIdBourse() == idBourseOrNull)
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> afficherCartes(task.getValue()));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Erreur actions : " + (ex != null ? ex.getMessage() : "inconnue"));
            if (ex != null) ex.printStackTrace();
        });

        dbExecutor.submit(task);
    }

    // ======================
    // CRUD
    // ======================
    @FXML
    void ajouterAction(ActionEvent event) {
        if (!validerFormulaire()) return;

        Bourse b = cbBourse.getValue();
        Action action = new Action(
                b,
                tfSymbole.getText().trim(),
                tfNomEntreprise.getText().trim(),
                cbSecteur.getValue(),
                Double.parseDouble(tfPrix.getText().trim()),
                Integer.parseInt(tfQuantite.getText().trim()),
                cbStatut.getValue()
        );

        Task<Void> task = new Task<>() {
            @Override protected Void call() { serviceAction.add(action); return null; }
        };

        task.setOnSucceeded(e -> {
            showSuccess("✅ Action ajoutée !");
            clearFields();
            afficherActions(null);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Erreur ajout : " + (ex != null ? ex.getMessage() : "inconnue"));
            if (ex != null) ex.printStackTrace();
        });

        dbExecutor.submit(task);
    }

    @FXML
    void modifierAction(ActionEvent event) {
        if (tfId.getText().trim().isEmpty()) { showError("ID obligatoire !"); return; }
        if (!validerFormulaire()) return;

        Action action = new Action(
                cbBourse.getValue(),
                tfSymbole.getText().trim(),
                tfNomEntreprise.getText().trim(),
                cbSecteur.getValue(),
                Double.parseDouble(tfPrix.getText().trim()),
                Integer.parseInt(tfQuantite.getText().trim()),
                cbStatut.getValue()
        );
        action.setIdAction(Integer.parseInt(tfId.getText().trim()));

        Task<Void> task = new Task<>() {
            @Override protected Void call() { serviceAction.update(action); return null; }
        };

        task.setOnSucceeded(e -> {
            showSuccess("✏️ Modifié !");
            clearFields();
            afficherActions(null);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Erreur modif : " + (ex != null ? ex.getMessage() : "inconnue"));
            if (ex != null) ex.printStackTrace();
        });

        dbExecutor.submit(task);
    }

    @FXML
    void supprimerAction(ActionEvent event) {
        if (tfId.getText().trim().isEmpty()) { showError("ID obligatoire !"); return; }

        int id = Integer.parseInt(tfId.getText().trim());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'action ?");
        confirm.setContentText("ID " + id + " - irréversible !");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Action a = new Action();
        a.setIdAction(id);

        Task<Void> task = new Task<>() {
            @Override protected Void call() { serviceAction.delete(a); return null; }
        };

        task.setOnSucceeded(e -> {
            showSuccess("🗑 Supprimé !");
            clearFields();
            afficherActions(null);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Erreur suppression : " + (ex != null ? ex.getMessage() : "inconnue"));
            if (ex != null) ex.printStackTrace();
        });

        dbExecutor.submit(task);
    }

    @FXML
    void afficherActions(ActionEvent event) {
        Bourse b = cbBourse.getValue();
        if (b == null) chargerActionsAsync(null);
        else chargerActionsAsync(b.getIdBourse());
    }

    @FXML
    private void handleRechercher(ActionEvent event) {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) { showError("Entrez un mot-clé !"); return; }

        Task<List<Action>> task = new Task<>() {
            @Override protected List<Action> call() {
                List<Action> all = serviceAction.getAll();
                return all.stream()
                        .filter(a -> a.getSymbole().toLowerCase().contains(keyword)
                                || a.getNomEntreprise().toLowerCase().contains(keyword))
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> afficherCartes(task.getValue()));
        task.setOnFailed(e -> showError("Erreur recherche"));

        dbExecutor.submit(task);
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        searchField.clear();
        clearFields();
        cbBourse.setValue(null);
        afficherActions(null);
    }

    @FXML
    private void viderFormulaire(ActionEvent event) {
        clearFields();
    }

    // ======================
    // CARDS (même style bourse)
    // ======================
    private void afficherCartes(List<Action> actions) {
        cardsContainer.getChildren().clear();

        if (actions == null || actions.isEmpty()) {
            Label empty = new Label("⚠️ Aucune action à afficher.");
            empty.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:14px;");
            cardsContainer.getChildren().add(empty);
            lblTotal.setText("📋 Total : 0 action(s)");
            return;
        }

        for (Action a : actions) {
            cardsContainer.getChildren().add(buildActionCard(a));
        }

        lblTotal.setText("📋 Total : " + actions.size() + " action(s)");
    }

    private VBox buildActionCard(Action a) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(290);
        card.setCursor(Cursor.HAND);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #dfe6e9;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
        );

        Label title = new Label("📈 " + a.getSymbole() + " - " + a.getNomEntreprise());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill:#2c3e50;");

        Label bourse = new Label("🏦 Bourse : " + (a.getBourse() != null ? a.getBourse().getNomBourse() : "N/A"));
        Label secteur = new Label("🏷 Secteur : " + a.getSecteur());
        Label prix = new Label("💰 Prix : " + String.format("%.2f", a.getPrixUnitaire()));
        Label qte = new Label("📦 Quantité : " + a.getQuantiteDisponible());
        Label statut = new Label("📌 Statut : " + a.getStatut());

        bourse.setStyle("-fx-text-fill:#34495e;");
        secteur.setStyle("-fx-text-fill:#34495e;");
        prix.setStyle("-fx-text-fill:#34495e;");
        qte.setStyle("-fx-text-fill:#34495e;");
        statut.setStyle("-fx-text-fill:#34495e;");

        card.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            tfId.setText(String.valueOf(a.getIdAction()));
            cbBourse.setValue(a.getBourse());
            tfSymbole.setText(a.getSymbole());
            tfNomEntreprise.setText(a.getNomEntreprise());
            cbSecteur.setValue(a.getSecteur());
            tfPrix.setText(String.valueOf(a.getPrixUnitaire()));
            tfQuantite.setText(String.valueOf(a.getQuantiteDisponible()));
            cbStatut.setValue(a.getStatut());
        });

        card.getChildren().addAll(title, bourse, secteur, prix, qte, statut);
        return card;
    }

    private boolean validerFormulaire() {
        if (cbBourse.getValue() == null) { showError("Choisissez une bourse !"); return false; }
        if (tfSymbole.getText().trim().isEmpty()) { showError("Symbole obligatoire !"); return false; }
        if (tfNomEntreprise.getText().trim().isEmpty()) { showError("Entreprise obligatoire !"); return false; }
        if (cbSecteur.getValue() == null) { showError("Secteur obligatoire !"); return false; }

        try { Double.parseDouble(tfPrix.getText().trim()); }
        catch (Exception e) { showError("Prix invalide !"); return false; }

        try { Integer.parseInt(tfQuantite.getText().trim()); }
        catch (Exception e) { showError("Quantité invalide !"); return false; }

        return true;
    }

    private void clearFields() {
        tfId.clear();
        tfSymbole.clear();
        tfNomEntreprise.clear();
        tfPrix.clear();
        tfQuantite.clear();
        cbSecteur.getSelectionModel().selectFirst();
        cbStatut.getSelectionModel().selectFirst();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
