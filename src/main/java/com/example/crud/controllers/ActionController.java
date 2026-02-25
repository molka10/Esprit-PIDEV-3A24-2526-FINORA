package com.example.crud.controllers;

import com.example.crud.models.Action;
import com.example.crud.models.Bourse;
import com.example.crud.services.ServiceAction;
import com.example.crud.services.ServiceBourse;
import com.example.crud.services.ServiceAlphaVantage;
import com.example.crud.services.ServiceAlphaVantage.DonneesAction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
 * 📈 ActionController - Version avec APIs intégrées
 *
 * ✅ ServiceAlphaVantage pour prix boursiers réels
 */
public class ActionController implements Initializable {

    @FXML private ComboBox<Bourse> cbBourse;
    @FXML private TextField searchField;
    @FXML private Label lblTotal;
    @FXML private VBox cardsContainer;

    private final ServiceAction serviceAction = new ServiceAction();
    private final ServiceBourse serviceBourse = new ServiceBourse();
    private final ServiceAlphaVantage alphaVantage = new ServiceAlphaVantage();

    private Integer pendingPreselectBourseId = null;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("db-executor");
        return t;
    });
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        cbBourse.setConverter(new StringConverter<>() {
            @Override public String toString(Bourse b) {
                if (b == null) return "Toutes les bourses";
                return b.getNomBourse() + " (" + b.getDevise() + " - " + b.getPays() + ")";
            }
            @Override public Bourse fromString(String s) { return null; }
        });

        cbBourse.valueProperty().addListener((obs, o, n) -> {
            if (n == null) chargerActionsAsync(null);
            else chargerActionsAsync(n.getIdBourse());
        });

        // 🆕 RECHERCHE DYNAMIQUE EN TEMPS RÉEL
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerActionsDynamique(newValue);
        });







        chargerBoursesAsync();
        chargerActionsAsync(null);

        // Test Alpha Vantage
        if (alphaVantage.estConfigure()) {
            System.out.println("✅ ServiceAlphaVantage configuré et prêt");
        } else {
            System.err.println("⚠️ ServiceAlphaVantage non configuré - Prix réels désactivés");
        }
    }
    public void preselectionnerBourseParId(int idBourse) {
        pendingPreselectBourseId = idBourse;

        Platform.runLater(() -> {
            if (cbBourse.getItems() != null && !cbBourse.getItems().isEmpty()) {
                for (Bourse b : cbBourse.getItems()) {
                    if (b.getIdBourse() == idBourse) {
                        cbBourse.setValue(b);
                        pendingPreselectBourseId = null;
                        break;
                    }
                }
            }
        });
    }


    private void filtrerActionsDynamique(String motCle) {
        Task<List<Action>> task = new Task<>() {
            @Override
            protected List<Action> call() {
                List<Action> toutes = serviceAction.getAll();

                if (motCle == null || motCle.isBlank()) {
                    return toutes;
                }

                String kw = motCle.toLowerCase();
                return toutes.stream()
                        .filter(a -> a.getSymbole().toLowerCase().contains(kw) ||
                                a.getNomEntreprise().toLowerCase().contains(kw) ||
                                a.getSecteur().toLowerCase().contains(kw))
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            List<Action> resultats = task.getValue();
            afficherCartes(resultats);
        });

        task.setOnFailed(e -> System.err.println("Erreur filtre: " + task.getException()));
        dbExecutor.submit(task);
    }

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

    // ======================
    // 📊 ACTUALISER PRIX ALPHA VANTAGE
    // ======================

    @FXML
    private void actualiserPrix() {
        if (!alphaVantage.estConfigure()) {
            showError("ServiceAlphaVantage non configuré !\n\nConfigurer la clé API dans ServiceAlphaVantage.java (ligne 23)");
            return;
        }

        Alert loading = new Alert(Alert.AlertType.INFORMATION);
        loading.setTitle("Alpha Vantage");
        loading.setHeaderText("Actualisation des prix en cours...");
        loading.setContentText("Cela peut prendre 30-60 secondes.\n(Limite : 25 requêtes/jour)");
        loading.show();

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                List<Action> actions = serviceAction.getAll();
                int compteur = 0;
                int limite = 10; // Limite pour ne pas dépasser quota gratuit

                for (Action action : actions) {
                    if (compteur >= limite) {
                        System.out.println("⚠️ Limite de " + limite + " actions atteinte");
                        break;
                    }

                    // 🌐 APPEL API ALPHA VANTAGE
                    DonneesAction donnees = alphaVantage.getDonneesAction(action.getSymbole());

                    if (donnees != null) {
                        if (!donnees.fromCache) {
                            // Prix récupéré depuis l'API
                            action.setPrixUnitaire(donnees.prix);
                            serviceAction.update(action);
                            compteur++;
                            System.out.println("✅ Prix mis à jour: " + action.getSymbole() + " = $" + donnees.prix);
                        } else {
                            // Prix depuis le cache
                            action.setPrixUnitaire(donnees.prix);
                            System.out.println("💾 Cache: " + action.getSymbole() + " = $" + donnees.prix);
                        }
                    } else {
                        System.out.println("⚠️ Pas de données pour: " + action.getSymbole());
                    }

                    // Pause pour respecter les limites (5 req/min)
                    if (compteur > 0 && compteur % 5 == 0) {
                        try {
                            Thread.sleep(60000); // 1 minute
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                return compteur;
            }
        };

        task.setOnSucceeded(e -> {
            loading.close();
            int nbMisAJour = task.getValue();
            showSuccess("✅ Prix mis à jour depuis Alpha Vantage !\n\n" +
                    nbMisAJour + " action(s) actualisée(s)");
            afficherActions(null);
        });

        task.setOnFailed(e -> {
            loading.close();
            showError("Erreur: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    // ======================
    // DIALOG AJOUT/MODIFICATION
    // ======================

    @FXML
    private void ouvrirDialogAjout(ActionEvent event) {
        ouvrirDialog(null);
    }

    private void ouvrirDialog(Action actionAModifier) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(actionAModifier == null ? "➕ Nouvelle Action" : "✏️ Modifier Action");
        dialog.setHeaderText(actionAModifier == null ? "Ajouter une nouvelle action" : "Modifier l'action " + actionAModifier.getSymbole());

        ButtonType btnSave = new ButtonType(actionAModifier == null ? "Ajouter" : "Modifier", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<Bourse> cbDialogBourse = new ComboBox<>();
        cbDialogBourse.setItems(cbBourse.getItems());
        cbDialogBourse.setConverter(cbBourse.getConverter());
        cbDialogBourse.setPrefWidth(300);

        TextField tfSymbole = new TextField();
        tfSymbole.setPromptText("Ex: AAPL, MSFT, GOOGL");

        TextField tfNomEntreprise = new TextField();
        tfNomEntreprise.setPromptText("Ex: Apple Inc.");

        ComboBox<String> cbSecteur = new ComboBox<>();
        cbSecteur.setItems(FXCollections.observableArrayList(
                "Technologie", "Finance", "Santé", "Énergie", "Consommation",
                "Industrie", "Immobilier", "Télécommunications", "Services", "Autre"
        ));

        TextField tfPrix = new TextField();
        tfPrix.setPromptText("Ex: 178.50");

        TextField tfQuantite = new TextField();
        tfQuantite.setPromptText("Ex: 1000");

        ComboBox<String> cbStatut = new ComboBox<>();
        cbStatut.setItems(FXCollections.observableArrayList("DISPONIBLE", "INDISPONIBLE"));
        cbStatut.getSelectionModel().selectFirst();

        if (actionAModifier != null) {
            cbDialogBourse.setValue(actionAModifier.getBourse());
            tfSymbole.setText(actionAModifier.getSymbole());
            tfNomEntreprise.setText(actionAModifier.getNomEntreprise());
            cbSecteur.setValue(actionAModifier.getSecteur());
            tfPrix.setText(String.valueOf(actionAModifier.getPrixUnitaire()));
            tfQuantite.setText(String.valueOf(actionAModifier.getQuantiteDisponible()));
            cbStatut.setValue(actionAModifier.getStatut());
        } else {
            cbSecteur.getSelectionModel().selectFirst();
        }

        int row = 0;
        grid.add(new Label("Bourse *"), 0, row);
        grid.add(cbDialogBourse, 1, row++);

        grid.add(new Label("Symbole *"), 0, row);
        grid.add(tfSymbole, 1, row++);

        grid.add(new Label("Nom Entreprise *"), 0, row);
        grid.add(tfNomEntreprise, 1, row++);

        grid.add(new Label("Secteur *"), 0, row);
        grid.add(cbSecteur, 1, row++);

        grid.add(new Label("Prix Unitaire *"), 0, row);
        grid.add(tfPrix, 1, row++);

        grid.add(new Label("Quantité *"), 0, row);
        grid.add(tfQuantite, 1, row++);

        grid.add(new Label("Statut *"), 0, row);
        grid.add(cbStatut, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnSave) {
                try {
                    if (cbDialogBourse.getValue() == null) {
                        showError("Veuillez sélectionner une bourse");
                        return;
                    }
                    if (tfSymbole.getText().trim().isEmpty()) {
                        showError("Le symbole est obligatoire");
                        return;
                    }
                    if (tfNomEntreprise.getText().trim().isEmpty()) {
                        showError("Le nom de l'entreprise est obligatoire");
                        return;
                    }

                    double prix = Double.parseDouble(tfPrix.getText().trim());
                    int quantite = Integer.parseInt(tfQuantite.getText().trim());

                    Action action = new Action(
                            cbDialogBourse.getValue(),
                            tfSymbole.getText().trim(),
                            tfNomEntreprise.getText().trim(),
                            cbSecteur.getValue(),
                            prix,
                            quantite,
                            cbStatut.getValue()
                    );

                    if (actionAModifier != null) {
                        action.setIdAction(actionAModifier.getIdAction());
                        serviceAction.update(action);
                        showSuccess("✏️ Action modifiée avec succès !");
                    } else {
                        serviceAction.add(action);
                        showSuccess("✅ Action ajoutée avec succès !");
                    }

                    afficherActions(null);

                } catch (NumberFormatException e) {
                    showError("Prix ou quantité invalide");
                } catch (Exception e) {
                    showError("Erreur : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // ======================
    // CHARGEMENT DES DONNÉES
    // ======================

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
                        cbBourse.setValue(b);
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

    @FXML
    void afficherActions(ActionEvent event) {
        Bourse b = cbBourse.getValue();
        if (b == null) chargerActionsAsync(null);
        else chargerActionsAsync(b.getIdBourse());
    }

    // ======================
    // RECHERCHE
    // ======================

    @FXML
    private void handleRechercher(ActionEvent event) {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            showError("Entrez un mot-clé !");
            return;
        }

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
        cbBourse.setValue(null);
        afficherActions(null);
    }

    // ======================
    // AFFICHAGE LISTE
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

        for (Action a : actions) {
            cardsContainer.getChildren().add(creerItemAction(a));
        }

        lblTotal.setText("📊 Total : " + actions.size() + " action(s)");
    }

    private HBox creerItemAction(Action action) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(16));
        item.setAlignment(Pos.CENTER_LEFT);

        item.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: transparent transparent #e5e7eb transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Gauche
        VBox gauche = new VBox(4);
        gauche.setPrefWidth(200);

        Label symbole = new Label(action.getSymbole());
        symbole.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label nom = new Label(action.getNomEntreprise());
        nom.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        gauche.getChildren().addAll(symbole, nom);

        // Centre
        VBox centre = new VBox(4);
        HBox.setHgrow(centre, Priority.ALWAYS);

        String devise = action.getBourse() != null ? action.getBourse().getDevise() : "USD";
        Label prix = new Label("💰 " + String.format("%.2f", action.getPrixUnitaire()) + " " + devise);
        prix.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        Label details = new Label("📦 " + action.getQuantiteDisponible() + " unités • " + action.getSecteur());
        details.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

        centre.getChildren().addAll(prix, details);

        // Droite
        HBox droite = new HBox(14);
        droite.setAlignment(Pos.CENTER_RIGHT);

        Random rand = new Random(action.getIdAction());
        double variation = (rand.nextDouble() * 10) - 5;
        String couleur = variation >= 0 ? "#10b981" : "#ef4444";
        String symboleVar = variation >= 0 ? "▲" : "▼";

        VBox varBox = new VBox(4);
        varBox.setAlignment(Pos.CENTER_RIGHT);
        varBox.setPrefWidth(110);

        Region barBackground = new Region();
        barBackground.setPrefSize(90, 5);
        barBackground.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 3;");

        Region barFill = new Region();
        double barWidth = Math.min(90, Math.abs(variation) / 10.0 * 90);
        barFill.setPrefSize(barWidth, 5);
        barFill.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 3;");

        StackPane barPane = new StackPane();
        barPane.setAlignment(Pos.CENTER_LEFT);
        barPane.getChildren().addAll(barBackground, barFill);

        Label varLabel = new Label(symboleVar + " " + String.format("%.2f%%", Math.abs(variation)));
        varLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

        varBox.getChildren().addAll(barPane, varLabel);

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );
        btnModifier.setOnAction(e -> ouvrirDialog(action));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #ef4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );
        btnSupprimer.setOnAction(e -> supprimerAction(action));

        droite.getChildren().addAll(varBox, btnModifier, btnSupprimer);

        item.getChildren().addAll(gauche, centre, droite);

        item.setOnMouseEntered(e ->
                item.setStyle("-fx-background-color: #f9fafb; -fx-border-color: transparent transparent #e5e7eb transparent; -fx-border-width: 0 0 1 0;")
        );

        item.setOnMouseExited(e ->
                item.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e5e7eb transparent; -fx-border-width: 0 0 1 0;")
        );

        return item;
    }

    private void supprimerAction(Action action) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'action " + action.getSymbole() + " ?");
        confirm.setContentText("Cette action est irréversible !");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                serviceAction.delete(action);
                showSuccess("🗑️ Action supprimée avec succès !");
                afficherActions(null);
            } catch (Exception e) {
                showError("Erreur suppression : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ======================
    // UTILS
    // ======================

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