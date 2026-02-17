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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 🏦 DashboardTradingController
 * Dashboard Investisseur/Entreprise style Bourse Desktop
 *
 * Fonctionnalités :
 * - Portfolio + indices marché
 * - Graphique de performance
 * - Watchlist persistante
 * - Tableau marché avec variations
 * - Dialog Acheter / Vendre avec quantité précise
 */
public class DashboardInvestisseurController implements Initializable {

    // Header
    @FXML private Label lblDateTime;

    // Stat cards
    @FXML private Label lblPortfolio;
    @FXML private Label lblPortfolioChange;
    @FXML private Label lblMesActions;
    @FXML private Label lblWatchlistCount;

    // Chart
    @FXML private LineChart<String, Number> lineChart;

    // Sidebar user
    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarId;

    // Conteneurs
    @FXML private VBox watchlistContainer;
    @FXML private VBox newsContainer;
    @FXML private VBox marcheContainer;

    // Filtres
    @FXML private ComboBox<String> cbFiltreMarche;
    @FXML private TextField searchField;

    private final ServiceAction serviceAction = new ServiceAction();
    private final ServiceBourse serviceBourse = new ServiceBourse();

    // Watchlist en mémoire (IDs des actions favorites)
    private final Set<Integer> watchlistIds = new HashSet<>();
    // Portfolio simulé (map idAction → quantité possédée)
    private final Map<Integer, Integer> portfolio = new HashMap<>();
    // Prix d'achat moyen
    private final Map<Integer, Double> prixAchat = new HashMap<>();

    private List<Action> toutesActions = new ArrayList<>();
    private final Random random = new Random();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2, r -> { Thread t = new Thread(r); t.setDaemon(true); return t; });

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Date/heure
        lblDateTime.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.FRENCH)));

        // Configurer graphique
        configureChart();

        // Charger données
        chargerDonnees();

        // Charger news statiques
        chargerNews();

        // Refresh variations toutes les 4 secondes
        scheduler.scheduleAtFixedRate(() ->
                Platform.runLater(this::rafraichirVariations), 4, 4, TimeUnit.SECONDS);
    }

    // ============================================================
    //  CHARGEMENT DES DONNÉES
    // ============================================================

    private void chargerDonnees() {
        Task<List<Action>> task = new Task<>() {
            @Override protected List<Action> call() {
                return serviceAction.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            toutesActions = task.getValue();

            // Remplir filtre bourses
            List<String> nomsBourses = new ArrayList<>();
            nomsBourses.add("Toutes les bourses");
            toutesActions.stream()
                    .map(a -> a.getBourse() != null ? a.getBourse().getNomBourse() : "")
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .forEach(nomsBourses::add);
            cbFiltreMarche.setItems(FXCollections.observableArrayList(nomsBourses));

            // Stats
            lblMesActions.setText(toutesActions.size() + " actions");

            // Remplir tableau marché
            afficherTableauMarche(toutesActions);

            // Remplir watchlist
            rafraichirWatchlist();

            // Graphique
            remplirGraphique();

            // Portfolio
            calculerPortfolio();
        });
        task.setOnFailed(e -> System.err.println("Erreur chargement : " + task.getException()));
        new Thread(task).start();
    }

    // ============================================================
    //  TABLEAU MARCHÉ
    // ============================================================

    private void afficherTableauMarche(List<Action> actions) {
        marcheContainer.getChildren().clear();

        for (Action action : actions) {
            marcheContainer.getChildren().add(creerLigneMarche(action));
        }
    }

    private HBox creerLigneMarche(Action action) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #0f111733; -fx-background-radius: 8; -fx-cursor: hand;");

        // Badge symbole coloré
        String[] colors = {"#6366f1", "#f59e0b", "#10b981", "#ef4444", "#3b82f6", "#8b5cf6"};
        String color = colors[Math.abs(action.getSymbole().hashCode()) % colors.length];

        Label badge = new Label(action.getSymbole().length() > 4
                ? action.getSymbole().substring(0, 4) : action.getSymbole());
        badge.setStyle(
                "-fx-background-color: " + color + "33;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-font-weight: bold; -fx-font-size: 11px;" +
                        "-fx-background-radius: 6; -fx-padding: 4 8;" +
                        "-fx-pref-width: 50; -fx-alignment: CENTER;"
        );

        HBox symbolBox = new HBox(8, badge);
        symbolBox.setAlignment(Pos.CENTER_LEFT);
        symbolBox.setPrefWidth(120);

        // Nom entreprise
        Label nomLabel = new Label(action.getNomEntreprise());
        nomLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e5e7eb;");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);

        // Prix
        Label prixLabel = new Label(String.format("%.2f", action.getPrixUnitaire()));
        prixLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        // Variation (simulée)
        double var = (new Random(action.getIdAction() + System.currentTimeMillis() / 4000).nextDouble() * 8) - 4;
        String varStr = String.format("%+.2f%%", var);
        Label varLabel = new Label(varStr);
        varLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-text-fill: " + (var >= 0 ? "#00d4aa" : "#ef4444") + ";" +
                "-fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        // Volume
        Label volLabel = new Label(
                NumberFormat.getInstance(Locale.US).format((long) action.getQuantiteDisponible() * 1000)
        );
        volLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        // Bouton Trade
        Button btnTrade = new Button("Trade");
        btnTrade.setStyle(
                "-fx-background-color: #00d4aa22;" +
                        "-fx-text-fill: #00d4aa;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;" +
                        "-fx-border-color: #00d4aa44; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-pref-width: 80;"
        );
        btnTrade.setOnAction(e -> ouvrirDialogTrade(action));

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #ffffff0a; -fx-background-radius: 8; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #0f111733; -fx-background-radius: 8; -fx-cursor: hand;"));

        row.getChildren().addAll(symbolBox, nomLabel, prixLabel, varLabel, volLabel, btnTrade);
        return row;
    }

    // ============================================================
    //  DIALOG TRADE (ACHETER / VENDRE)
    // ============================================================

    @FXML
    private void ouvrirDialogAcheter(ActionEvent event) {
        if (toutesActions.isEmpty()) { showInfo("Aucune action disponible."); return; }
        ouvrirDialogTrade(toutesActions.get(0)); // Ouvre sur la première par défaut
    }

    @FXML
    private void ouvrirDialogVendre(ActionEvent event) {
        if (portfolio.isEmpty()) { showInfo("Vous ne possédez aucune action à vendre."); return; }
        // Ouvre dialog avec mode vente
        ouvrirDialogTradeAvecMode(null, "VENDRE");
    }

    private void ouvrirDialogTrade(Action action) {
        ouvrirDialogTradeAvecMode(action, "ACHETER");
    }

    private void ouvrirDialogTradeAvecMode(Action actionPreselect, String modeInitial) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("💹 Trade — FINORA");

        ButtonType btnConfirm = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirm, btnCancel);
        dialog.getDialogPane().setMinWidth(500);

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));

        // === MODE TOGGLE ===
        ToggleGroup modeGroup = new ToggleGroup();
        ToggleButton btnAchat = new ToggleButton("🟢 Acheter");
        ToggleButton btnVente = new ToggleButton("🔴 Vendre");
        btnAchat.setToggleGroup(modeGroup);
        btnVente.setToggleGroup(modeGroup);

        btnAchat.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;");
        btnVente.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;");

        if ("VENDRE".equals(modeInitial)) btnVente.setSelected(true);
        else btnAchat.setSelected(true);

        HBox modeBox = new HBox(8, btnAchat, btnVente);

        // === SÉLECTION ACTION ===
        Label lblActionTitle = new Label("Action");
        lblActionTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        ComboBox<Action> cbAction = new ComboBox<>();
        cbAction.setItems(FXCollections.observableArrayList(toutesActions));
        cbAction.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Action a) {
                return a == null ? "" : a.getSymbole() + " — " + a.getNomEntreprise();
            }
            @Override public Action fromString(String s) { return null; }
        });
        cbAction.setPrefWidth(440);

        if (actionPreselect != null) cbAction.setValue(actionPreselect);
        else if (!toutesActions.isEmpty()) cbAction.setValue(toutesActions.get(0));

        // === PRIX ACTUEL ===
        Label lblPrixActuelTitle = new Label("Prix actuel");
        lblPrixActuelTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        Label lblPrixActuel = new Label();
        lblPrixActuel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00d4aa;");

        // Mettre à jour prix quand action change
        cbAction.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                lblPrixActuel.setText(String.format("%.2f %s",
                        n.getPrixUnitaire(),
                        n.getBourse() != null ? n.getBourse().getDevise() : "TND"));
            }
        });

        if (cbAction.getValue() != null) {
            Action sel = cbAction.getValue();
            lblPrixActuel.setText(String.format("%.2f %s",
                    sel.getPrixUnitaire(),
                    sel.getBourse() != null ? sel.getBourse().getDevise() : "TND"));
        }

        // === QUANTITÉ ===
        Label lblQteTitle = new Label("Quantité");
        lblQteTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        HBox qteBox = new HBox(10);
        qteBox.setAlignment(Pos.CENTER_LEFT);

        Button btnMinus = new Button("−");
        btnMinus.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 4 14; -fx-cursor: hand;");

        Spinner<Integer> spinnerQte = new Spinner<>(1, 99999, 1);
        spinnerQte.setEditable(true);
        spinnerQte.setPrefWidth(120);

        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 4 14; -fx-cursor: hand;");

        btnMinus.setOnAction(e -> spinnerQte.decrement());
        btnPlus.setOnAction(e -> spinnerQte.increment());

        qteBox.getChildren().addAll(btnMinus, spinnerQte, btnPlus);

        // === TOTAL ESTIMÉ ===
        Label lblTotalTitle = new Label("Total estimé");
        lblTotalTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        Label lblTotal = new Label("0.00 TND");
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Calculer total dynamiquement
        Runnable calcTotal = () -> {
            if (cbAction.getValue() != null) {
                double total = cbAction.getValue().getPrixUnitaire() * spinnerQte.getValue();
                String devise = cbAction.getValue().getBourse() != null
                        ? cbAction.getValue().getBourse().getDevise() : "TND";
                lblTotal.setText(String.format("%.2f %s", total, devise));
            }
        };

        spinnerQte.valueProperty().addListener((obs, o, n) -> calcTotal.run());
        cbAction.valueProperty().addListener((obs, o, n) -> calcTotal.run());
        calcTotal.run();

        content.getChildren().addAll(
                modeBox,
                new Separator(),
                lblActionTitle, cbAction,
                lblPrixActuelTitle, lblPrixActuel,
                lblQteTitle, qteBox,
                new Separator(),
                lblTotalTitle, lblTotal
        );

        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnConfirm) {
                Action actionSelectionnee = cbAction.getValue();
                int quantite = spinnerQte.getValue();
                boolean estAchat = btnAchat.isSelected();

                if (actionSelectionnee == null) {
                    showError("Sélectionnez une action !");
                    return;
                }

                if (estAchat) {
                    executerAchat(actionSelectionnee, quantite);
                } else {
                    executerVente(actionSelectionnee, quantite);
                }
            }
        });
    }

    // ============================================================
    //  LOGIQUE ACHAT / VENTE
    // ============================================================

    private void executerAchat(Action action, int quantite) {
        int idAction = action.getIdAction();
        portfolio.merge(idAction, quantite, Integer::sum);
        prixAchat.put(idAction, action.getPrixUnitaire());

        double total = action.getPrixUnitaire() * quantite;
        String devise = action.getBourse() != null ? action.getBourse().getDevise() : "TND";

        showSuccess(String.format(
                "✅ Achat Confirmé !\n\n" +
                        "📈 %s (%s)\n" +
                        "📦 Quantité : %d\n" +
                        "💰 Prix unitaire : %.2f %s\n" +
                        "💵 Total : %.2f %s",
                action.getSymbole(), action.getNomEntreprise(),
                quantite,
                action.getPrixUnitaire(), devise,
                total, devise
        ));

        calculerPortfolio();
        rafraichirWatchlist();
    }

    private void executerVente(Action action, int quantite) {
        int idAction = action.getIdAction();
        int possede = portfolio.getOrDefault(idAction, 0);

        if (possede <= 0) {
            showError("Vous ne possédez aucune action " + action.getSymbole() + " à vendre !");
            return;
        }

        if (quantite > possede) {
            showError(String.format(
                    "Vous ne possédez que %d action(s) %s.\n" +
                            "Vous ne pouvez pas en vendre %d.",
                    possede, action.getSymbole(), quantite
            ));
            return;
        }

        int nouvQte = possede - quantite;
        if (nouvQte == 0) portfolio.remove(idAction);
        else portfolio.put(idAction, nouvQte);

        double total = action.getPrixUnitaire() * quantite;
        double prixMoyen = prixAchat.getOrDefault(idAction, action.getPrixUnitaire());
        double plusvalue = (action.getPrixUnitaire() - prixMoyen) * quantite;
        String devise = action.getBourse() != null ? action.getBourse().getDevise() : "TND";

        showSuccess(String.format(
                "✅ Vente Confirmée !\n\n" +
                        "📉 %s (%s)\n" +
                        "📦 Quantité vendue : %d  |  Reste : %d\n" +
                        "💰 Prix unitaire : %.2f %s\n" +
                        "💵 Total : %.2f %s\n" +
                        "%s Plus-value : %.2f %s",
                action.getSymbole(), action.getNomEntreprise(),
                quantite, nouvQte,
                action.getPrixUnitaire(), devise,
                total, devise,
                plusvalue >= 0 ? "📈" : "📉",
                plusvalue, devise
        ));

        calculerPortfolio();
        rafraichirWatchlist();
    }

    // ============================================================
    //  WATCHLIST
    // ============================================================

    @FXML
    private void ouvrirDialogWatchlist(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("⭐ Gérer la Watchlist");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinWidth(400);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        Label title = new Label("Choisissez les actions à suivre :");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        content.getChildren().add(title);

        for (Action action : toutesActions) {
            HBox ligne = new HBox(12);
            ligne.setAlignment(Pos.CENTER_LEFT);

            CheckBox cb = new CheckBox(action.getSymbole() + " — " + action.getNomEntreprise());
            cb.setSelected(watchlistIds.contains(action.getIdAction()));
            cb.setStyle("-fx-font-size: 13px;");

            HBox.setHgrow(cb, Priority.ALWAYS);

            Label prixLabel = new Label(String.format("%.2f", action.getPrixUnitaire()));
            prixLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00d4aa;");

            cb.selectedProperty().addListener((obs, o, selected) -> {
                if (selected) watchlistIds.add(action.getIdAction());
                else watchlistIds.remove(action.getIdAction());
            });

            ligne.getChildren().addAll(cb, prixLabel);
            content.getChildren().add(ligne);
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();

        rafraichirWatchlist();
        lblWatchlistCount.setText("⭐ " + watchlistIds.size() + " en watchlist");
    }

    private void rafraichirWatchlist() {
        watchlistContainer.getChildren().clear();

        if (watchlistIds.isEmpty()) {
            Label empty = new Label("Aucune action en watchlist.\nCliquez sur ⭐ Watchlist pour en ajouter.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-text-alignment: CENTER;");
            empty.setWrapText(true);
            watchlistContainer.getChildren().add(empty);
            return;
        }

        for (Action action : toutesActions) {
            if (!watchlistIds.contains(action.getIdAction())) continue;

            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(10));
            item.setStyle("-fx-background-color: #0f111777; -fx-background-radius: 8;");

            // Symbole
            Label sym = new Label(action.getSymbole());
            sym.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Prix + variation
            double var = (random.nextDouble() * 4) - 2;
            VBox pricesBox = new VBox(2);
            pricesBox.setAlignment(Pos.CENTER_RIGHT);

            Label prixLbl = new Label(String.format("%.2f", action.getPrixUnitaire()));
            prixLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label varLbl = new Label(String.format("%+.2f%%", var));
            varLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (var >= 0 ? "#00d4aa" : "#ef4444") + ";");

            pricesBox.getChildren().addAll(prixLbl, varLbl);

            item.getChildren().addAll(sym, spacer, pricesBox);
            watchlistContainer.getChildren().add(item);
        }
    }

    // ============================================================
    //  PORTFOLIO
    // ============================================================

    private void calculerPortfolio() {
        double valeurTotale = 0;
        for (Map.Entry<Integer, Integer> entry : portfolio.entrySet()) {
            Optional<Action> actionOpt = toutesActions.stream()
                    .filter(a -> a.getIdAction() == entry.getKey())
                    .findFirst();
            if (actionOpt.isPresent()) {
                valeurTotale += actionOpt.get().getPrixUnitaire() * entry.getValue();
            }
        }

        final double valeurFinal = valeurTotale;
        Platform.runLater(() -> {
            if (valeurFinal > 0) {
                lblPortfolio.setText(String.format("%.2f TND", valeurFinal));
                lblPortfolioChange.setText("📊 " + portfolio.size() + " action(s) en portefeuille");
            } else {
                lblPortfolio.setText("0.00 TND");
                lblPortfolioChange.setText("Aucune position ouverte");
            }
        });
    }

    // ============================================================
    //  GRAPHIQUE
    // ============================================================

    private void configureChart() {
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setStyle("-fx-background-color: transparent;");
    }

    private void remplirGraphique() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Performance");

        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        double val = 100;
        for (String jour : jours) {
            val += (random.nextDouble() * 20) - 8;
            series.getData().add(new XYChart.Data<>(jour, val));
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    // ============================================================
    //  NEWS
    // ============================================================

    private void chargerNews() {
        String[][] news = {
                {"Inflation US : marché attentiste, tech en hausse.", "1 min"},
                {"Résultats trimestriels : volatilité forte sur le Nasdaq.", "15 min"},
                {"EUR/USD : léger rebond après annonces BCE.", "1h"},
                {"Les matières premières en forte hausse ce matin.", "3h"},
                {"Bourse de Paris : le CAC 40 franchit un seuil.", "6h"}
        };

        for (String[] item : news) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(12));
            card.setStyle("-fx-background-color: #0f111766; -fx-background-radius: 8; -fx-cursor: hand;");

            Label txt = new Label(item[0]);
            txt.setWrapText(true);
            txt.setStyle("-fx-font-size: 12px; -fx-text-fill: #d1d5db;");

            HBox footer = new HBox();
            Label time = new Label("🕐 il y a " + item[1]);
            time.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
            Label aiTag = new Label("+1AI%");
            aiTag.setStyle("-fx-font-size: 10px; -fx-text-fill: #00d4aa;");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            footer.getChildren().addAll(time, sp, aiTag);

            card.getChildren().addAll(txt, footer);
            newsContainer.getChildren().add(card);
        }
    }

    // ============================================================
    //  REFRESH
    // ============================================================

    @FXML
    private void handleActualiser(ActionEvent event) {
        chargerDonnees();
    }

    private void rafraichirVariations() {
        afficherTableauMarche(toutesActions);
        rafraichirWatchlist();
    }

    @FXML
    private void navDashboard(ActionEvent event) {
        // Navigation interne
    }

    // ============================================================
    //  UTILS
    // ============================================================

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}