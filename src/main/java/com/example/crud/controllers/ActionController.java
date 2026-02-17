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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 🎨 ActionController - Version Moderne Style Mobile
 *
 * Améliorations :
 * - ✅ Interface liste verticale (style app mobile)
 * - ✅ Variations en couleur (vert/rouge)
 * - ✅ Formulaire caché (toggle)
 * - ✅ Design épuré et moderne
 */
public class ActionController implements Initializable {

    // Champs formulaire
    @FXML private TextField tfId;
    @FXML private ComboBox<Bourse> cbBourse;
    @FXML private TextField tfSymbole;
    @FXML private TextField tfNomEntreprise;
    @FXML private ComboBox<String> cbSecteur;
    @FXML private TextField tfPrix;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbStatut;

    // Recherche et affichage
    @FXML private TextField searchField;
    @FXML private Label lblTotal;

    // ✅ CHANGÉ : VBox au lieu de FlowPane pour liste verticale
    @FXML private VBox cardsContainer;

    // ✅ NOUVEAU : Container du formulaire pour le toggle
    @FXML private VBox formulaireContainer;

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

        chargerBoursesAsync();
        chargerActionsAsync(null);
    }

    // ✅ Appelé depuis BourseController
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

    // ✅ Bouton retour
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

    // ✅ NOUVEAU : Toggle formulaire
    @FXML
    private void afficherFormulaire(ActionEvent event) {
        if (formulaireContainer != null) {
            ScrollPane parent = (ScrollPane) formulaireContainer.getParent();
            boolean visible = parent.isVisible();
            parent.setVisible(!visible);
            parent.setManaged(!visible);

            if (!visible) {
                clearFields(); // Vider quand on ouvre
            }
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
    // AFFICHAGE STYLE LISTE MOBILE
    // ======================
    private void afficherCartes(List<Action> actions) {
        cardsContainer.getChildren().clear();

        if (actions == null || actions.isEmpty()) {
            Label empty = new Label("⚠️ Aucune action à afficher.");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-padding: 40;");
            cardsContainer.getChildren().add(empty);
            lblTotal.setText("📊 Total : 0 action(s)");
            return;
        }

        // ✅ Ajouter chaque action comme item de liste
        for (Action a : actions) {
            cardsContainer.getChildren().add(creerItemAction(a));
        }

        lblTotal.setText("📊 Total : " + actions.size() + " action(s)");
    }

    /**
     * ✅ NOUVEAU : Créer un item d'action style liste mobile
     */
    private HBox creerItemAction(Action action) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(16));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(Cursor.HAND);

        // Style avec bordure en bas
        item.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: transparent transparent #e5e7eb transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // === PARTIE GAUCHE : Symbole + Nom ===
        VBox gauche = new VBox(4);
        gauche.setPrefWidth(250);

        Label symbole = new Label(action.getSymbole());
        symbole.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label nom = new Label(action.getNomEntreprise());
        nom.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        gauche.getChildren().addAll(symbole, nom);

        // === PARTIE CENTRE : Prix + Détails ===
        VBox centre = new VBox(4);
        HBox.setHgrow(centre, Priority.ALWAYS);

        String devise = action.getBourse() != null ? action.getBourse().getDevise() : "USD";
        Label prix = new Label("💰 " + String.format("%.2f", action.getPrixUnitaire()) + " " + devise);
        prix.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        Label details = new Label("📦 " + action.getQuantiteDisponible() + " unités • " + action.getSecteur());
        details.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

        centre.getChildren().addAll(prix, details);

        // === PARTIE DROITE : Variation ===
        VBox droite = new VBox(4);
        droite.setAlignment(Pos.CENTER_RIGHT);
        droite.setPrefWidth(100);

        // Simuler variation (remplacer par vraies données plus tard)
        Random rand = new Random(action.getIdAction()); // Seed pour cohérence
        double variation = (rand.nextDouble() * 10) - 5; // -5% à +5%

        String couleur = variation >= 0 ? "#10b981" : "#ef4444";
        String symboleVar = variation >= 0 ? "▲" : "▼";

        Label varLabel = new Label(symboleVar + " " + String.format("%.2f%%", Math.abs(variation)));
        varLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

        // Indicateur coloré
        Region indicator = new Region();
        indicator.setPrefSize(8, 8);
        indicator.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 4;");

        droite.getChildren().addAll(indicator, varLabel);

        // === ASSEMBLER ===
        item.getChildren().addAll(gauche, centre, droite);

        // === ÉVÉNEMENTS ===
        item.setOnMouseClicked(e -> {
            // Remplir le formulaire avec les données
            tfId.setText(String.valueOf(action.getIdAction()));
            tfSymbole.setText(action.getSymbole());
            tfNomEntreprise.setText(action.getNomEntreprise());
            cbBourse.setValue(action.getBourse());
            cbSecteur.setValue(action.getSecteur());
            tfPrix.setText(String.valueOf(action.getPrixUnitaire()));
            tfQuantite.setText(String.valueOf(action.getQuantiteDisponible()));
            cbStatut.setValue(action.getStatut());

            // Afficher le formulaire si caché
            if (formulaireContainer != null) {
                ScrollPane parent = (ScrollPane) formulaireContainer.getParent();
                if (!parent.isVisible()) {
                    parent.setVisible(true);
                    parent.setManaged(true);
                }
            }
        });

        // Effet hover
        item.setOnMouseEntered(e ->
                item.setStyle(
                        "-fx-background-color: #f9fafb;" +
                                "-fx-border-color: transparent transparent #e5e7eb transparent;" +
                                "-fx-border-width: 0 0 1 0;"
                )
        );

        item.setOnMouseExited(e ->
                item.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: transparent transparent #e5e7eb transparent;" +
                                "-fx-border-width: 0 0 1 0;"
                )
        );

        return item;
    }

    // ======================
    // ANCIEN CODE (à supprimer si tu veux)
    // ======================
    @SuppressWarnings("unused")
    private VBox buildActionCard(Action a) {
        // Ancienne méthode de création de cartes
        // Tu peux la supprimer ou la garder en backup
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

        card.getChildren().addAll(title, bourse, secteur, prix, qte, statut);
        return card;
    }

    // ======================
    // VALIDATION & UTILS
    // ======================
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