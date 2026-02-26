package com.example.crud.controllers;

import com.example.crud.models.Bourse;
import com.example.crud.services.ServiceBourse;
import com.example.crud.services.ServiceDevise;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 🏛 BourseController - Version avec APIs intégrées
 *
 * ✅ ServiceDevise pour conversions automatiques
 */
public class BourseController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label lblTotal;
    @FXML private FlowPane cardsContainer;
    @FXML private Button btnTheme;
    private final ServiceBourse service = new ServiceBourse();
    private final ServiceDevise serviceDevise = new ServiceDevise();

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r); t.setDaemon(true); return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        afficherBourses(null);

        // 🆕 RECHERCHE DYNAMIQUE EN TEMPS RÉEL
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerBoursesDynamique(newValue);
        });

        // Test ServiceDevise
        if (serviceDevise.estConfigure()) {
            System.out.println("✅ ServiceDevise configuré et prêt");
        } else {
            System.err.println("⚠️ ServiceDevise non configuré - Conversions désactivées");
        }

    }

    // ============================================================
    //  CHARGEMENT
    // ============================================================

    private void afficherBourses(String filtre) {
        Task<List<Bourse>> task = new Task<>() {
            @Override protected List<Bourse> call() { return service.getAll(); }
        };

        task.setOnSucceeded(e -> {
            List<Bourse> bourses = task.getValue();

            if (filtre != null && !filtre.isBlank()) {
                String kw = filtre.toLowerCase();
                bourses = bourses.stream()
                        .filter(b -> b.getNomBourse().toLowerCase().contains(kw)
                                || b.getPays().toLowerCase().contains(kw))
                        .collect(Collectors.toList());
            }

            cardsContainer.getChildren().clear();
            for (Bourse b : bourses) {
                cardsContainer.getChildren().add(creerCarte(b));
            }
            lblTotal.setText("📊 Total : " + bourses.size() + " bourse(s)");
        });

        task.setOnFailed(e -> showError("Erreur chargement : " + task.getException().getMessage()));
        dbExecutor.submit(task);
    }

    // ============================================================
    //  CARTE BOURSE AVEC CONVERSIONS
    // ============================================================

    private VBox creerCarte(Bourse b) {
        VBox card = new VBox(12);
        card.setPrefWidth(310);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 2);"
        );

        // === HEADER : Nom + Badge statut ===
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nomLabel = new Label(b.getNomBourse());
        nomLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = creerBadgeStatut(b.getStatut());
        header.getChildren().addAll(nomLabel, spacer, badge);

        // === INFOS : Pays + Devise ===
        VBox infos = new VBox(6);
        Label paysLabel = new Label("📍 " + b.getPays());
        paysLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        Label deviseLabel = new Label("💰 " + b.getDevise());
        deviseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        infos.getChildren().addAll(paysLabel, deviseLabel);

        // === 💱 CONVERSIONS DEVISES ===
        VBox conversionBox = creerBoxConversion(b.getDevise());

        // === MINI GRAPHIQUE ===
        Pane chartPane = new Pane();
        chartPane.setPrefHeight(45);
        Path path = creerMiniGraphique(b.getIdBourse());
        chartPane.getChildren().add(path);

        // === BOUTONS ===
        HBox row1 = new HBox(8);
        row1.setAlignment(Pos.CENTER_LEFT);

        Button btnDetails = new Button("🔍 Détails");
        btnDetails.setStyle(
                "-fx-background-color: #6366f122; -fx-text-fill: #6366f1;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;" +
                        "-fx-border-color: #6366f144; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        btnDetails.setOnAction(e -> ouvrirPanelDetails(b));

        Button btnActions = new Button("📈 Actions");
        btnActions.setStyle(
                "-fx-background-color: #10b98122; -fx-text-fill: #10b981;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;" +
                        "-fx-border-color: #10b98144; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        btnActions.setOnAction(e -> ouvrirActionsPourBourse(b));

        row1.getChildren().addAll(btnDetails, btnActions);

        HBox row2 = new HBox(8);
        row2.setAlignment(Pos.CENTER_LEFT);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #3b82f622; -fx-text-fill: #3b82f6;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;" +
                        "-fx-border-color: #3b82f644; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        btnModifier.setOnAction(e -> ouvrirDialogModifier(b));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #ef444422; -fx-text-fill: #ef4444;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;" +
                        "-fx-border-color: #ef444444; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        btnSupprimer.setOnAction(e -> supprimerBourse(b));

        row2.getChildren().addAll(btnModifier, btnSupprimer);

        // Hover
        card.setOnMouseEntered(ev -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16;" +
                        "-fx-border-color: #6366f1; -fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 16, 0, 0, 4);"
        ));
        card.setOnMouseExited(ev -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16;" +
                        "-fx-border-color: #e5e7eb; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 2);"
        ));

        card.getChildren().addAll(header, infos, conversionBox, chartPane, row1, row2);
        return card;
    }

    // ============================================================
    //  💱 BOX CONVERSION DEVISES (API)
    // ============================================================

    private VBox creerBoxConversion(String deviseOrigine) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );

        Label titre = new Label("💱 Taux de change");
        titre.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");

        if (!serviceDevise.estConfigure()) {
            Label erreur = new Label("API non configurée");
            erreur.setStyle("-fx-font-size: 10px; -fx-text-fill: #ef4444;");
            box.getChildren().addAll(titre, erreur);
            return box;
        }

        // Conversions vers USD, EUR, TND
        String[] devisesTarget = {"USD", "EUR", "TND"};

        for (String target : devisesTarget) {
            if (target.equals(deviseOrigine)) continue;

            HBox ligne = new HBox(6);
            ligne.setAlignment(Pos.CENTER_LEFT);

            Label deviseLabel = new Label("1 " + deviseOrigine + " =");
            deviseLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");

            // 🌐 APPEL API
            double taux = serviceDevise.getTaux(deviseOrigine, target);

            Label tauxLabel = new Label(String.format("%.4f %s", taux, target));
            tauxLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

            ligne.getChildren().addAll(deviseLabel, tauxLabel);
            box.getChildren().add(ligne);
        }

        box.getChildren().add(0, titre);
        return box;
    }

    // ============================================================
    //  PANEL DÉTAILS AVEC CONVERSIONS COMPLÈTES
    // ============================================================

    private void ouvrirPanelDetails(Bourse b) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🔍 Détails — " + b.getNomBourse());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinWidth(480);

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(24));
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);");

        Label titre = new Label(b.getNomBourse());
        titre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label badgeCopy = creerBadgeStatut(b.getStatut());

        HBox titreRow = new HBox(12, titre, badgeCopy);
        titreRow.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().add(titreRow);

        // Corps
        VBox body = new VBox(0);
        body.setPadding(new Insets(0));

        body.getChildren().addAll(
                creerLigneDetail("📍 Pays", b.getPays(), "#6366f1"),
                creerLigneDetail("💰 Devise", b.getDevise(), "#10b981"),
                creerLigneDetail("📌 Statut", b.getStatut(),
                        b.getStatut().equals("ACTIVE") ? "#10b981" : "#ef4444"),
                creerLigneDetail("📅 Créée le",
                        b.getDateCreation() != null ? b.getDateCreation().toString() : "N/A", "#6b7280")
        );

        // 💱 Box Conversions détaillées
        VBox convBox = new VBox(10);
        convBox.setPadding(new Insets(20));
        convBox.setStyle("-fx-background-color: white;");

        Label convTitre = new Label("💱 Conversions de devises");
        convTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        if (serviceDevise.estConfigure()) {
            String[] devises = {"USD", "EUR", "TND", "GBP", "JPY"};

            for (String devise : devises) {
                if (devise.equals(b.getDevise())) continue;

                // 🌐 APPEL API
                double taux = serviceDevise.getTaux(b.getDevise(), devise);

                HBox ligneTaux = new HBox(10);
                ligneTaux.setAlignment(Pos.CENTER_LEFT);
                ligneTaux.setPadding(new Insets(8));
                ligneTaux.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6;");

                Label from = new Label("1 " + b.getDevise());
                from.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

                Label arrow = new Label("→");
                arrow.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af;");

                Label to = new Label(String.format("%.4f %s", taux, devise));
                to.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

                ligneTaux.getChildren().addAll(from, arrow, to);
                convBox.getChildren().add(ligneTaux);
            }
        } else {
            Label erreur = new Label("⚠️ API de conversion non configurée");
            erreur.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
            convBox.getChildren().add(erreur);
        }

        convBox.getChildren().add(0, convTitre);

        // Graphique
        VBox chartBox = new VBox(8);
        chartBox.setPadding(new Insets(20));
        chartBox.setStyle("-fx-background-color: white;");

        Label chartTitle = new Label("📊 Tendance de performance");
        chartTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        Pane bigChart = new Pane();
        bigChart.setPrefHeight(80);
        bigChart.setPrefWidth(420);
        Path bigPath = creerMiniGraphiqueGrand(b.getIdBourse(), 420, 80);
        bigChart.getChildren().add(bigPath);

        chartBox.getChildren().addAll(chartTitle, bigChart);

        content.getChildren().addAll(headerBox, body, convBox, chartBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }


    private void filtrerBoursesDynamique(String motCle) {
        Task<List<Bourse>> task = new Task<>() {
            @Override
            protected List<Bourse> call() {
                List<Bourse> toutes = service.getAll();

                if (motCle == null || motCle.isBlank()) {
                    return toutes;
                }

                String kw = motCle.toLowerCase();
                return toutes.stream()
                        .filter(b -> b.getNomBourse().toLowerCase().contains(kw) ||
                                b.getPays().toLowerCase().contains(kw) ||
                                b.getDevise().toLowerCase().contains(kw))
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            List<Bourse> resultats = task.getValue();
            cardsContainer.getChildren().clear();
            for (Bourse b : resultats) {
                cardsContainer.getChildren().add(creerCarte(b));
            }
            lblTotal.setText("📊 Total : " + resultats.size() + " bourse(s)");
        });

        task.setOnFailed(e -> System.err.println("Erreur filtre: " + task.getException()));
        dbExecutor.submit(task);
    }

    private HBox creerLigneDetail(String cle, String valeur, String couleur) {
        HBox ligne = new HBox();
        ligne.setPadding(new Insets(14, 20, 14, 20));
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-border-width: 0 0 1 0;");

        Region bar = new Region();
        bar.setPrefSize(4, 30);
        bar.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 2;");

        VBox textBox = new VBox(3);
        textBox.setPadding(new Insets(0, 0, 0, 14));

        Label cleLabel = new Label(cle);
        cleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        Label valLabel = new Label(valeur);
        valLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        textBox.getChildren().addAll(cleLabel, valLabel);
        ligne.getChildren().addAll(bar, textBox);
        return ligne;
    }

    // ============================================================
    //  DIALOG AJOUTER / MODIFIER
    // ============================================================

    @FXML
    private void ouvrirDialogAjout(ActionEvent event) {
        ouvrirDialog(null);
    }

    private void ouvrirDialogModifier(Bourse bourse) {
        ouvrirDialog(bourse);
    }

    private void ouvrirDialog(Bourse bourseAModifier) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(bourseAModifier == null ? "➕ Nouvelle Bourse" : "✏️ Modifier — " + bourseAModifier.getNomBourse());
        dialog.setHeaderText(bourseAModifier == null ? "Remplissez les informations de la bourse" : "Modifiez les informations");

        ButtonType btnSave = new ButtonType(
                bourseAModifier == null ? "Ajouter" : "Modifier",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(460);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));

        TextField tfNom = new TextField();
        tfNom.setPromptText("Ex: NYSE, Euronext, NASDAQ");
        tfNom.setPrefWidth(310);

        TreeSet<String> paysSet = new TreeSet<>();
        for (String code : java.util.Locale.getISOCountries()) {
            String nom = new java.util.Locale("", code).getDisplayCountry(java.util.Locale.FRENCH);
            if (nom != null && !nom.isBlank()) paysSet.add(nom);
        }
        ComboBox<String> cbPays = new ComboBox<>(FXCollections.observableArrayList(paysSet));
        cbPays.setPrefWidth(310);
        cbPays.setEditable(true);

        ComboBox<String> cbDevise = new ComboBox<>(FXCollections.observableArrayList(
                "TND", "EUR", "USD", "GBP", "JPY", "CHF", "CAD", "MAD", "DZD"));
        cbDevise.setPrefWidth(310);

        ComboBox<String> cbStatut = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        cbStatut.setPrefWidth(310);

        if (bourseAModifier != null) {
            tfNom.setText(bourseAModifier.getNomBourse());
            cbPays.setValue(bourseAModifier.getPays());
            cbDevise.setValue(bourseAModifier.getDevise());
            cbStatut.setValue(bourseAModifier.getStatut());
        } else {
            cbDevise.getSelectionModel().selectFirst();
            cbStatut.getSelectionModel().selectFirst();
        }

        int row = 0;
        grid.add(creerLabel("Nom de la Bourse *"), 0, row); grid.add(tfNom, 1, row++);
        grid.add(creerLabel("Pays *"), 0, row);             grid.add(cbPays, 1, row++);
        grid.add(creerLabel("Devise *"), 0, row);           grid.add(cbDevise, 1, row++);
        grid.add(creerLabel("Statut *"), 0, row);           grid.add(cbStatut, 1, row);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response != btnSave) return;

            String nom = tfNom.getText().trim();
            String pays = cbPays.getValue();
            String devise = cbDevise.getValue();
            String statut = cbStatut.getValue();

            if (nom.isEmpty()) { showError("Le nom est obligatoire !"); return; }
            if (pays == null || pays.isEmpty()) { showError("Le pays est obligatoire !"); return; }
            if (devise == null) { showError("La devise est obligatoire !"); return; }
            if (statut == null) { showError("Le statut est obligatoire !"); return; }

            try {
                if (bourseAModifier == null) {
                    Bourse newBourse = new Bourse(nom, pays, devise, statut);
                    service.add(newBourse);
                    showSuccess("✅ Bourse ajoutée !");
                } else {
                    bourseAModifier.setNomBourse(nom);
                    bourseAModifier.setPays(pays);
                    bourseAModifier.setDevise(devise);
                    bourseAModifier.setStatut(statut);
                    service.update(bourseAModifier);
                    showSuccess("✏️ Bourse modifiée !");
                }
                afficherBourses(null);
            } catch (Exception ex) {
                showError("Erreur : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    // ============================================================
    //  SUPPRIMER
    // ============================================================

    private void supprimerBourse(Bourse bourse) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + bourse.getNomBourse() + " ?");
        confirm.setContentText("⚠️ Cette action est irréversible !\nToutes les actions liées seront supprimées.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.delete(bourse);
                showSuccess("🗑️ Bourse supprimée !");
                afficherBourses(null);
            } catch (Exception ex) {
                showError("Erreur suppression : " + ex.getMessage());
            }
        }
    }

    // ============================================================
    //  NAVIGATION
    // ============================================================

    private void ouvrirActionsPourBourse(Bourse bourse) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/crud/action-view.fxml"));
            Parent root = loader.load();

            ActionController ctrl = loader.getController();
            ctrl.preselectionnerBourseParId(bourse.getIdBourse());

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setTitle("FINORA - Actions de " + bourse.getNomBourse());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur ouverture actions : " + ex.getMessage());
        }
    }

    @FXML
    private void retourProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/crud/utilisateur-static-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FINORA - Choisir Profil");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur retour : " + ex.getMessage());
        }
    }

    // ============================================================
    //  RECHERCHE / ACTUALISER
    // ============================================================

    @FXML
    private void handleRechercher(ActionEvent event) {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { showError("Entrez un mot-clé !"); return; }
        afficherBourses(kw);
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        searchField.clear();
        afficherBourses(null);
    }

    // ============================================================
    //  HELPERS
    // ============================================================

    private Label creerBadgeStatut(String statut) {
        Label badge = new Label(statut);
        if ("ACTIVE".equals(statut)) {
            badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 4 10;");
        } else {
            badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 4 10;");
        }
        return badge;
    }

    private Path creerMiniGraphique(int seed) {
        return creerMiniGraphiqueGrand(seed, 270, 40);
    }

    private Path creerMiniGraphiqueGrand(int seed, double width, double height) {
        Path path = new Path();
        Random rand = new Random(seed);
        int points = 10;
        double stepX = width / (points - 1);
        double midY = height / 2;

        double startY = midY + (rand.nextDouble() - 0.5) * (height * 0.6);
        path.getElements().add(new MoveTo(0, startY));

        for (int i = 1; i < points; i++) {
            double x = i * stepX;
            double y = midY + (rand.nextDouble() - 0.5) * (height * 0.6);
            path.getElements().add(new LineTo(x, y));
        }

        path.setStroke(Color.web("#6366f1"));
        path.setStrokeWidth(2.5);
        path.setFill(Color.TRANSPARENT);
        path.setSmooth(true);
        return path;
    }

    private Label creerLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        return l;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}