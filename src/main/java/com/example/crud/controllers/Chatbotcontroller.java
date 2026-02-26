package com.example.crud.controllers;

import com.example.crud.services.ServiceChatbot;
import javafx.application.Platform;
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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 🤖 ChatbotController
 * Interface de chat avec l'assistant financier IA
 */
public class Chatbotcontroller implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox chatContainer;
    @FXML private TextArea inputField;
    @FXML private Button btnEnvoyer;
    @FXML private HBox loadingIndicator;

    private final ServiceChatbot serviceChatbot = new ServiceChatbot();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r); t.setDaemon(true); return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vérifier que l'API est configurée
        if (!serviceChatbot.estConfigure()) {
            afficherErreurConfiguration();
        }

        // Enter pour envoyer
        inputField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER") && !event.isShiftDown()) {
                event.consume();
                envoyerMessage(null);
            }
        });

        // Auto-scroll
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    // ─────────────────────────────────────────────────────────
    //  ENVOYER MESSAGE
    // ─────────────────────────────────────────────────────────

    @FXML
    private void envoyerMessage(ActionEvent event) {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        // Afficher le message de l'utilisateur
        ajouterMessageUtilisateur(message);
        inputField.clear();

        // Désactiver l'input pendant le chargement
        inputField.setDisable(true);
        btnEnvoyer.setDisable(true);
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);

        // Appeler l'API en arrière-plan
        executor.submit(() -> {
            String reponse = serviceChatbot.envoyerMessage(message);

            Platform.runLater(() -> {
                ajouterMessageAssistant(reponse);
                inputField.setDisable(false);
                btnEnvoyer.setDisable(false);
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
                inputField.requestFocus();
            });
        });
    }

    @FXML
    private void envoyerSuggestion(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String suggestion = btn.getText();
        inputField.setText(suggestion);
        envoyerMessage(null);
    }

    // ─────────────────────────────────────────────────────────
    //  AFFICHAGE DES MESSAGES
    // ─────────────────────────────────────────────────────────

    private void ajouterMessageUtilisateur(String message) {
        VBox messageBox = new VBox(8);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setMaxWidth(600);
        messageBox.setPadding(new Insets(0, 0, 0, 100));

        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_RIGHT);

        VBox bubble = new VBox(4);
        bubble.setStyle(
                "-fx-background-color: #6366f1;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 16;"
        );
        bubble.setMaxWidth(500);

        Label textLabel = new Label(message);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        bubble.getChildren().add(textLabel);

        Label emoji = new Label("👤");
        emoji.setStyle("-fx-font-size: 24px;");

        container.getChildren().addAll(bubble, emoji);
        messageBox.getChildren().add(container);

        chatContainer.getChildren().add(messageBox);
    }

    private void ajouterMessageAssistant(String message) {
        VBox messageBox = new VBox(8);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setMaxWidth(600);

        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_LEFT);

        Label emoji = new Label("🤖");
        emoji.setStyle("-fx-font-size: 24px;");

        VBox contentBox = new VBox(4);

        Label nameLabel = new Label("Assistant FINORA");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: bold;");

        VBox bubble = new VBox();
        bubble.setStyle(
                "-fx-background-color: #1a1d27;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 16;" +
                        "-fx-border-color: #6366f144;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );
        bubble.setMaxWidth(500);

        Label textLabel = new Label(message);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d1d5db;");

        bubble.getChildren().add(textLabel);
        contentBox.getChildren().addAll(nameLabel, bubble);

        container.getChildren().addAll(emoji, contentBox);
        messageBox.getChildren().add(container);

        chatContainer.getChildren().add(messageBox);
    }

    // ─────────────────────────────────────────────────────────
    //  NOUVELLE CONVERSATION
    // ─────────────────────────────────────────────────────────

    @FXML
    private void nouvelleConversation(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Nouvelle Conversation");
        confirm.setHeaderText("Effacer l'historique ?");
        confirm.setContentText("Cela réinitialisera toute la conversation.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            serviceChatbot.reinitialiserConversation();

            // Garder seulement le message d'accueil
            while (chatContainer.getChildren().size() > 1) {
                chatContainer.getChildren().remove(1);
            }

            showSuccess("✅ Conversation réinitialisée !");
        }
    }

    // ─────────────────────────────────────────────────────────
    //  ERREUR CONFIGURATION
    // ─────────────────────────────────────────────────────────

    private void afficherErreurConfiguration() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Configuration requise");
        alert.setHeaderText("Clé API Anthropic manquante");
        alert.setContentText(
                "Pour utiliser le chatbot, vous devez configurer votre clé API Anthropic.\n\n" +
                        "1. Obtenez une clé sur : https://console.anthropic.com/\n" +
                        "2. Modifiez ServiceChatbot.java\n" +
                        "3. Remplacez 'VOTRE_CLE_API_ICI' par votre clé\n\n" +
                        "Le chatbot sera désactivé jusqu'à la configuration."
        );
        alert.showAndWait();

        // Désactiver l'interface
        inputField.setDisable(true);
        btnEnvoyer.setDisable(true);
    }

    // ─────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────

    @FXML
    private void retourDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/crud/dashboard-investisseur-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FINORA - Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  UTILS
    // ─────────────────────────────────────────────────────────

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}