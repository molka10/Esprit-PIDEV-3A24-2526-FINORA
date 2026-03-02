package com.example.finora.controllers.bourse;

import com.example.finora.entities.Action;
import com.example.finora.entities.TransactionBourse;
import com.example.finora.services.bourse.ServiceAction;
import com.example.finora.services.bourse.ServiceAlphaVantage;
import com.example.finora.services.bourse.ServiceAlphaVantage.DonneesAction;
import com.example.finora.services.bourse.ServiceTransactionBourse;
import com.example.finora.utils.Navigator;
import com.example.finora.utils.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🏦 DashboardInvestisseurController
 * ✅ Vente/achat utilisent DB (portefeuille) au lieu du Map mémoire
 * ✅ Fix FXML: ouvrirDialogWatchlist + searchWatchlist injected
 * ✅ Trade dialog: affiche Prix unitaire + Montant total
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

    // Sidebar user (si existe dans FXML)
    @FXML private Label lblSidebarUser;
    @FXML private Label lblSidebarId;

    // Conteneurs
    @FXML private VBox watchlistContainer;
    @FXML private VBox newsContainer;
    @FXML private VBox marcheContainer;

    // Filtres
    @FXML private ComboBox<String> cbFiltreMarche;
    @FXML private TextField searchField;

    // ✅ existe dans ton FXML
    @FXML private TextField searchWatchlist;

    private final ServiceAction serviceAction = new ServiceAction();
    private final ServiceTransactionBourse serviceTransaction = new ServiceTransactionBourse();

    // Watchlist en mémoire
    private final Set<Integer> watchlistIds = new HashSet<>();

    // Prix d'achat moyen (optionnel)
    private final Map<Integer, Double> prixAchat = new HashMap<>();

    private final ServiceAlphaVantage alphaVantage = new ServiceAlphaVantage();

    // Cache UI : symbole -> données AV
    private final Map<String, DonneesAction> avData = new ConcurrentHashMap<>();
    private List<Action> toutesActions = new ArrayList<>();
    private final Random random = new Random();

    // Flag pour éviter alertes multiples
    private boolean alerteDejaAffichee = false;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private int currentUserIdSafe() {
        return (Session.isLoggedIn() && Session.getCurrentUser() != null)
                ? Session.getCurrentUser().getId()
                : -1;
    }

    private String currentUserRoleSafe() {
        return (Session.isLoggedIn() && Session.getCurrentUser() != null)
                ? Session.getCurrentUser().getRole()
                : "INVESTISSEUR";
    }

    private String currentUserLabelSafe() {
        return (Session.isLoggedIn() && Session.getCurrentUser() != null)
                ? Session.getCurrentUser().getUsername()
                : "USER_STATIC";
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        if (lblDateTime != null) {
            lblDateTime.setText(LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.FRENCH)));
        }

        int uid = currentUserIdSafe();
        if (uid > 0) {
            if (lblSidebarUser != null) lblSidebarUser.setText(Session.getCurrentUser().getUsername());
            if (lblSidebarId != null) lblSidebarId.setText("ID: " + uid);
        }

        configureChart();
        chargerDonnees();
        chargerNews();

        demarrerSurveillanceAlerte();

        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::rafraichirVariations),
                4, 4, TimeUnit.SECONDS);
    }

    // ============================================================
    // CHARGEMENT DES DONNÉES
    // ============================================================

    private void chargerDonnees() {
        Task<List<Action>> task = new Task<>() {
            @Override
            protected List<Action> call() {
                return serviceAction.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            toutesActions = task.getValue();

            // Remplir filtre bourses
            if (cbFiltreMarche != null) {
                List<String> nomsBourses = new ArrayList<>();
                nomsBourses.add("Toutes les bourses");
                toutesActions.stream()
                        .map(a -> a.getBourse() != null ? a.getBourse().getNomBourse() : "")
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .forEach(nomsBourses::add);
                cbFiltreMarche.setItems(FXCollections.observableArrayList(nomsBourses));
            }

            if (lblMesActions != null) lblMesActions.setText(toutesActions.size() + " actions");

            afficherTableauMarche(toutesActions);
            rafraichirWatchlist();
            remplirGraphique();

            calculerPortfolioValeur(); // ✅ portfolio valeur user only

            if (lblWatchlistCount != null) {
                lblWatchlistCount.setText("⭐ " + watchlistIds.size() + " en watchlist");
            }
        });

        task.setOnFailed(e -> System.err.println("Erreur : " + task.getException()));
        new Thread(task).start();
    }

    @FXML
    private void chargerPrixReels() {
        if (!alphaVantage.estConfigure()) {
            showError("Clé Alpha Vantage non configurée !");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<Action> actions = serviceAction.getAll();
                for (Action action : actions) {
                    DonneesAction d = alphaVantage.getDonneesAction(action.getSymbole());
                    if (d != null) {
                        avData.put(action.getSymbole(), d);
                        action.setPrixUnitaire(d.prix);
                    }
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            afficherTableauMarche(toutesActions);
            rafraichirWatchlist();
            calculerPortfolioValeur();
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Erreur Alpha Vantage : " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // ============================================================
    // TABLEAU MARCHÉ
    // ============================================================

    private void afficherTableauMarche(List<Action> actions) {
        if (marcheContainer == null) return;
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

        String[] colors = { "#6366f1", "#f59e0b", "#10b981", "#ef4444", "#3b82f6", "#8b5cf6" };
        String color = colors[Math.abs(action.getSymbole().hashCode()) % colors.length];

        Label badge = new Label(action.getSymbole().length() > 4
                ? action.getSymbole().substring(0, 4)
                : action.getSymbole());

        badge.setStyle(
                "-fx-background-color: " + color + "33;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-font-weight: bold; -fx-font-size: 11px;" +
                        "-fx-background-radius: 6; -fx-padding: 4 8;" +
                        "-fx-pref-width: 50; -fx-alignment: CENTER;");

        HBox symbolBox = new HBox(8, badge);
        symbolBox.setAlignment(Pos.CENTER_LEFT);
        symbolBox.setPrefWidth(120);

        Label nomLabel = new Label(action.getNomEntreprise());
        nomLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e5e7eb;");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);

        Label prixLabel = new Label(String.format("%.2f", action.getPrixUnitaire()));
        prixLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        double var = calculerVariationSimulee(action);
        Label varLabel = new Label(String.format("%+.2f%%", var));
        varLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-text-fill: " + (var >= 0 ? "#00d4aa" : "#ef4444") + ";" +
                "-fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        Label volLabel = new Label(NumberFormat.getInstance(Locale.US).format((long) action.getQuantiteDisponible() * 1000));
        volLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-pref-width: 100; -fx-alignment: CENTER_RIGHT;");

        Button btnTrade = new Button("Trade");
        btnTrade.setStyle(
                "-fx-background-color: #00d4aa22; -fx-text-fill: #00d4aa;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;" +
                        "-fx-border-color: #00d4aa44; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-pref-width: 80;");
        btnTrade.setOnAction(e -> ouvrirDialogTrade(action));

        row.getChildren().addAll(symbolBox, nomLabel, prixLabel, varLabel, volLabel, btnTrade);
        return row;
    }

    // ============================================================
    // DIALOG TRADE
    // ============================================================

    @FXML
    private void ouvrirDialogAcheter(ActionEvent event) {
        if (toutesActions.isEmpty()) {
            showInfo("Aucune action disponible.");
            return;
        }
        ouvrirDialogTrade(toutesActions.get(0));
    }

    @FXML
    private void ouvrirDialogVendre(ActionEvent event) {
        int userId = currentUserIdSafe();
        if (userId <= 0) {
            showInfo("Utilisateur non connecté.");
            return;
        }

        // ✅ vérifier DB portefeuille
        int nb = serviceTransaction.getNombreActions(userId);
        if (nb <= 0) {
            showInfo("Vous ne possédez aucune action à vendre.");
            return;
        }

        ouvrirDialogTradeAvecMode(null, "VENDRE");
    }

    private void ouvrirDialogTrade(Action action) {
        ouvrirDialogTradeAvecMode(action, "ACHETER");
    }

    /**
     * ✅ ICI: Ajout affichage PRIX + TOTAL
     */
    private void ouvrirDialogTradeAvecMode(Action actionPreselect, String modeInitial) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("💹 Trade — FINORA");

        ButtonType btnConfirm = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirm, btnCancel);
        dialog.getDialogPane().setMinWidth(480);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));

        ToggleButton btnAchat = new ToggleButton("🟢 Acheter");
        ToggleButton btnVente = new ToggleButton("🔴 Vendre");
        ToggleGroup modeGroup = new ToggleGroup();
        btnAchat.setToggleGroup(modeGroup);
        btnVente.setToggleGroup(modeGroup);

        if ("VENDRE".equalsIgnoreCase(modeInitial)) btnVente.setSelected(true);
        else btnAchat.setSelected(true);

        HBox modeBox = new HBox(8, btnAchat, btnVente);

        ComboBox<Action> cbAction = new ComboBox<>();
        cbAction.setItems(FXCollections.observableArrayList(toutesActions));
        cbAction.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Action a) {
                return a == null ? "" : a.getSymbole() + " — " + a.getNomEntreprise();
            }
            @Override public Action fromString(String s) { return null; }
        });

        if (actionPreselect != null) cbAction.setValue(actionPreselect);
        else if (!toutesActions.isEmpty()) cbAction.setValue(toutesActions.get(0));

        Spinner<Integer> spinnerQte = new Spinner<>(1, 99999, 1);
        spinnerQte.setEditable(true);

        // ✅ AJOUT: Prix + Total
        Label lblPrix = new Label("Prix unitaire : —");
        lblPrix.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        Label lblTotal = new Label("Montant total : —");
        lblTotal.setStyle("-fx-font-size: 13px; -fx-text-fill: #111827; -fx-font-weight: bold;");

        Runnable refreshPrix = () -> {
            Action sel = cbAction.getValue();
            if (sel == null) {
                lblPrix.setText("Prix unitaire : —");
                lblTotal.setText("Montant total : —");
                return;
            }

            double prix = sel.getPrixUnitaire();
            int qte = spinnerQte.getValue();

            lblPrix.setText(String.format("Prix unitaire : %.2f TND", prix));
            lblTotal.setText(String.format("Montant total : %.2f TND", prix * qte));
        };

        cbAction.valueProperty().addListener((obs, oldV, newV) -> refreshPrix.run());
        spinnerQte.valueProperty().addListener((obs, oldV, newV) -> refreshPrix.run());
        refreshPrix.run();

        content.getChildren().addAll(
                modeBox,
                new Separator(),
                new Label("Action :"), cbAction,
                new Label("Quantité :"), spinnerQte,
                lblPrix,
                lblTotal
        );

        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnConfirm) {
                Action sel = cbAction.getValue();
                int quantite = spinnerQte.getValue();
                if (sel == null) {
                    showError("Sélectionnez une action !");
                    return;
                }
                if (btnAchat.isSelected()) executerAchat(sel, quantite);
                else executerVente(sel, quantite);
            }
        });
    }

    // ============================================================
    // ✅ WATCHLIST
    // ============================================================

    @FXML
    private void ouvrirDialogWatchlist(ActionEvent event) {
        if (toutesActions == null || toutesActions.isEmpty()) {
            showInfo("Aucune action disponible.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("⭐ Gérer la Watchlist");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinWidth(420);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().add(new Label("Choisissez les actions à suivre :"));

        for (Action action : toutesActions) {
            HBox ligne = new HBox(12);
            ligne.setAlignment(Pos.CENTER_LEFT);

            CheckBox cb = new CheckBox(action.getSymbole() + " — " + action.getNomEntreprise());
            cb.setSelected(watchlistIds.contains(action.getIdAction()));
            HBox.setHgrow(cb, Priority.ALWAYS);

            Label prixLabel = new Label(String.format("%.2f", action.getPrixUnitaire()));
            prixLabel.setStyle("-fx-text-fill: #00d4aa;");

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
        if (lblWatchlistCount != null) {
            lblWatchlistCount.setText("⭐ " + watchlistIds.size() + " en watchlist");
        }
    }

    // ============================================================
    // ACHAT / VENTE (DB)
    // ============================================================

    private void executerAchat(Action action, int quantite) {
        int userId = currentUserIdSafe();
        if (userId <= 0) {
            showError("Utilisateur non connecté.");
            return;
        }

        try {
            serviceTransaction.acheter(
                    userId,
                    action.getIdAction(),
                    quantite,
                    currentUserRoleSafe(),
                    currentUserLabelSafe()
            );

            prixAchat.put(action.getIdAction(), action.getPrixUnitaire());

            showSuccess("✅ Achat confirmé !");
            chargerDonnees();

        } catch (Exception ex) {
            showError("Achat impossible : " + ex.getMessage());
        }
    }

    private void executerVente(Action action, int quantite) {
        int userId = currentUserIdSafe();
        if (userId <= 0) {
            showError("Utilisateur non connecté.");
            return;
        }

        int idAction = action.getIdAction();
        int possede = serviceTransaction.getQuantitePossedee(userId, idAction);

        if (possede <= 0) {
            showError("Vous ne possédez aucune action " + action.getSymbole() + " !");
            return;
        }
        if (quantite > possede) {
            showError("Vous possédez seulement " + possede + " action(s) " + action.getSymbole() + ".");
            return;
        }

        try {
            serviceTransaction.vendre(
                    userId,
                    idAction,
                    quantite,
                    currentUserRoleSafe(),
                    currentUserLabelSafe()
            );

            showSuccess("✅ Vente confirmée !");
            chargerDonnees();

        } catch (Exception ex) {
            showError("Vente impossible : " + ex.getMessage());
        }
    }

    // ============================================================
    // PORTFOLIO VALUE (user only)
    // ============================================================

    private void calculerPortfolioValeur() {
        int userId = currentUserIdSafe();
        if (userId <= 0) {
            if (lblPortfolio != null) lblPortfolio.setText("0.00 TND");
            if (lblPortfolioChange != null) lblPortfolioChange.setText("Non connecté");
            return;
        }

        Task<Double> task = new Task<>() {
            @Override
            protected Double call() {
                List<TransactionBourse> transactions = serviceTransaction.getAllByUser(userId);

                double totalAchats = 0;
                double totalVentes = 0;

                for (TransactionBourse t : transactions) {
                    if ("ACHAT".equalsIgnoreCase(t.getTypeTransaction())) totalAchats += t.getMontantTotal();
                    else if ("VENTE".equalsIgnoreCase(t.getTypeTransaction())) totalVentes += t.getMontantTotal();
                }
                return totalAchats - totalVentes;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            double valeurPortfolio = task.getValue();
            if (lblPortfolio != null) lblPortfolio.setText(String.format("%.2f TND", valeurPortfolio));
            if (lblPortfolioChange != null) {
                lblPortfolioChange.setText(valeurPortfolio > 0 ? "📊 Positions ouvertes" : "Aucune position ouverte");
            }
        }));

        task.setOnFailed(e -> System.err.println("Erreur calcul portfolio: " + task.getException()));
        new Thread(task).start();
    }

    // ============================================================
    // GRAPH / NEWS / WATCHLIST / REFRESH
    // ============================================================

    private void configureChart() {
        if (lineChart == null) return;
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setStyle("-fx-background-color: transparent;");
    }

    private void remplirGraphique() {
        if (lineChart == null) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] jours = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };
        double val = 100;
        for (String j : jours) {
            val += (random.nextDouble() * 20) - 8;
            series.getData().add(new XYChart.Data<>(j, val));
        }
        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private double calculerVariationSimulee(Action action) {
        long seed = action.getSymbole().hashCode() + (System.currentTimeMillis() / 86400000);
        Random rand = new Random(seed);
        double base = (rand.nextDouble() * 10) - 5;

        long microSeed = System.currentTimeMillis() / 4000;
        Random microRand = new Random(seed + microSeed);
        double micro = (microRand.nextDouble() * 0.4) - 0.2;

        return Math.round((base + micro) * 100.0) / 100.0;
    }

    private void chargerNews() {
        if (newsContainer == null) return;

        String[][] news = {
                { "Inflation US : marché attentiste, tech en hausse.", "1 min" },
                { "Résultats trimestriels : volatilité forte sur le Nasdaq.", "15 min" },
                { "EUR/USD : léger rebond après annonces BCE.", "1h" },
                { "Les matières premières en forte hausse ce matin.", "3h" },
                { "Bourse de Paris : le CAC 40 franchit un seuil clé.", "6h" }
        };

        newsContainer.getChildren().clear();
        for (String[] item : news) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(12));
            card.setStyle("-fx-background-color: #0f111766; -fx-background-radius: 8;");

            Label txt = new Label(item[0]);
            txt.setWrapText(true);
            txt.setStyle("-fx-font-size: 12px; -fx-text-fill: #d1d5db;");

            HBox footer = new HBox();
            Label time = new Label("🕐 il y a " + item[1]);
            time.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label tag = new Label("+IA");
            tag.setStyle("-fx-font-size: 10px; -fx-text-fill: #00d4aa;");
            footer.getChildren().addAll(time, sp, tag);

            card.getChildren().addAll(txt, footer);
            newsContainer.getChildren().add(card);
        }
    }

    private void rafraichirWatchlist() {
        if (watchlistContainer == null) return;

        watchlistContainer.getChildren().clear();
        if (watchlistIds.isEmpty()) {
            Label empty = new Label("Cliquez sur ⭐ Watchlist\npour ajouter des actions.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
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

            Label sym = new Label(action.getSymbole());
            sym.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

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

    @FXML
    private void handleActualiser(ActionEvent event) {
        chargerDonnees();
    }

    private void rafraichirVariations() {
        afficherTableauMarche(toutesActions);
        rafraichirWatchlist();
    }

    @FXML
    private void ouvrirChatbot(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.goTo(stage, "/bourse/Chatbot view.fxml", "FINORA - Chatbot IA");
    }

    @FXML
    private void ouvrirPrediction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.goTo(stage, "/bourse/prediction-view.fxml", "FINORA - Prédiction IA");
    }

    @FXML
    private void ouvrirHistorique(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Historiquecontroller ctrl = Navigator.goTo(stage, "/bourse/Historique view.fxml",
                "FINORA - Historique des Transactions");
        if (ctrl != null) ctrl.setFxmlRetour("/bourse/dashboard-investisseur-view.fxml");
    }

    @FXML
    private void retourProfil(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.returnToDashboard(stage);
    }

    // ============================================================
    // ALERTE
    // ============================================================

    private void demarrerSurveillanceAlerte() {
        scheduler.scheduleAtFixedRate(this::verifierSeuilInvestissement, 10, 30, TimeUnit.SECONDS);
    }

    private void verifierSeuilInvestissement() {
        if (alerteDejaAffichee) return;

        int userId = currentUserIdSafe();
        if (userId <= 0) return;

        Task<Double> task = new Task<>() {
            @Override
            protected Double call() {
                List<TransactionBourse> transactions = serviceTransaction.getAllByUser(userId);
                double totalAchats = 0;
                for (TransactionBourse t : transactions) {
                    if ("ACHAT".equalsIgnoreCase(t.getTypeTransaction())) totalAchats += t.getMontantTotal();
                }
                return totalAchats;
            }
        };

        task.setOnSucceeded(e -> {
            Double montantTotal = task.getValue();
            if (montantTotal > 50000) {
                alerteDejaAffichee = true;
                Platform.runLater(() -> declencherAlerteInvestissement(montantTotal));
            }
        });

        new Thread(task).start();
    }

    private void declencherAlerteInvestissement(double montant) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🚨 ALERTE INVESTISSEMENT");
        alert.setHeaderText("Seuil critique dépassé !");
        alert.setContentText("💰 Montant total investi : " + String.format("%.2f TND", montant));
        alert.showAndWait();
    }

    // ============================================================
    // UTILS
    // ============================================================

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

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}