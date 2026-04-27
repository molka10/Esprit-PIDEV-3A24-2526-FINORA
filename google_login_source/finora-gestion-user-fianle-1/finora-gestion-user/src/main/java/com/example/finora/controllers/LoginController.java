package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.services.CaptchaService;
import com.example.finora.services.GoogleAuthService;
import com.example.finora.services.UserService;
import com.example.finora.utils.InputValidator;
import com.example.finora.utils.Navigator;
import com.example.finora.utils.Session;
import com.example.finora.utils.CameraService;
import com.example.finora.utils.FaceIdService;
import com.example.finora.utils.NotificationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import com.example.finora.utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    // CAPTCHA
    @FXML
    private ImageView captchaImage;
    @FXML
    private TextField captchaField;

    private UserService userService;
    private final CaptchaService captchaService = new CaptchaService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();

    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            refreshCaptcha();
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText("❌ Erreur de connexion à la base de données.");
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshCaptcha() {
        if (captchaImage != null) {
            captchaImage.setImage(captchaService.generateCaptchaImage(220, 60, 6));
        }
        if (captchaField != null)
            captchaField.clear();
    }

    @FXML
    private void handleLogin() {
        try {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String pass = passwordField.getText() == null ? "" : passwordField.getText();

            String err = InputValidator.validateLogin(email, pass);
            if (err != null) {
                statusLabel.setText("⚠️ " + err);
                return;
            }

            // CAPTCHA check
            if (captchaField == null || !captchaService.verify(captchaField.getText())) {
                statusLabel.setText("⚠️ CAPTCHA incorrect. Réessayez.");
                refreshCaptcha();
                return;
            }

            User u = userService.login(email, pass);

            if (u == null) {
                statusLabel.setText("❌ Email ou mot de passe incorrect.");
                refreshCaptcha();
                return;
            }

            loginSuccess(u);

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            refreshCaptcha();
        }
    }

    // ─────────────────────────────────────────────
    // ✅ GOOGLE LOGIN
    // ─────────────────────────────────────────────
    @FXML
    private void handleGoogleLogin() {
        statusLabel.setText("🔄 Ouverture de Google...");

        // Run on a background thread so the UI doesn't freeze
        Thread thread = new Thread(() -> {
            try {
                GoogleAuthService.GoogleUser googleUser = googleAuthService.authenticate();

                User u = userService.findOrCreateGoogleUser(googleUser.name(), googleUser.email());

                // Back to JavaFX thread
                Platform.runLater(() -> {
                    if (u == null) {
                        statusLabel.setText("❌ Impossible de créer le compte Google.");
                        return;
                    }
                    loginSuccess(u);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Erreur Google: " + e.getMessage());
                    System.err.println("[GoogleLogin] Error: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ─────────────────────────────────────────────
    // Shared login success logic
    // ─────────────────────────────────────────────
    private void loginSuccess(User u) {
        Session.setCurrentUser(u);
        Stage stage = (Stage) emailField.getScene().getWindow();

        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            Navigator.goTo(stage, "admin-shell.fxml", "Dashboard Admin");
        } else if ("ENTREPRISE".equalsIgnoreCase(u.getRole())) {
            Navigator.goTo(stage, "entreprise-shell.fxml", "Portail Entreprise");
        } else {
            Navigator.goTo(stage, "user-shell.fxml", "Dashboard");
        }
    }

    @FXML
    private void handleFaceIdLogin() {
        statusLabel.setText("🔄 Initialisation de la caméra...");

        CameraService cameraService = new CameraService();
        FaceIdService faceIdService = new FaceIdService();

        if (cameraService.getWebcam() == null) {
            statusLabel.setText("❌ Aucune caméra détectée.");
            return;
        }

        // Create a custom glassmorphism modal for Face ID
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle("Face ID Scan");

        ImageView cameraPreview = new ImageView();
        cameraPreview.setFitWidth(400);
        cameraPreview.setFitHeight(300);
        cameraPreview.setPreserveRatio(true);
        cameraPreview.imageProperty().bind(cameraService.imageProperty());

        Button captureBtn = new Button("Scanner le visage");
        captureBtn.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20;");

        VBox layout = new VBox(20,
                new Label("Scan Face ID") {
                    {
                        setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
                    }
                },
                cameraPreview,
                captureBtn,
                cancelBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(
                "-fx-background-color: rgba(15, 23, 42, 0.9); -fx-padding: 30; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-radius: 20;");

        Scene scene = new Scene(layout);
        scene.setFill(null);
        dialog.setScene(scene);

        cancelBtn.setOnAction(e -> {
            cameraService.stop();
            dialog.close();
            statusLabel.setText("");
        });

        captureBtn.setOnAction(e -> {
            captureBtn.setDisable(true);
            captureBtn.setText("Analyse en cours...");

            Thread thread = new Thread(() -> {
                try {
                    BufferedImage bimg = cameraService.captureSnapshot();
                    if (bimg == null) {
                        Platform.runLater(() -> statusLabel.setText("❌ Échec de capture."));
                        return;
                    }

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(bimg, "jpg", out);
                    byte[] bytes = out.toByteArray();

                    // Get embedding from HF
                    double[] currentEmbedding = faceIdService.getFaceEmbedding(bytes);
                    if (currentEmbedding == null) {
                        Platform.runLater(() -> {
                            captureBtn.setDisable(false);
                            captureBtn.setText("Réessayer");
                            statusLabel.setText("❌ Erreur IA: Image non traitée.");
                        });
                        return;
                    }

                    // Find match in DB
                    User matchedUser = findBestMatch(currentEmbedding, faceIdService);

                    Platform.runLater(() -> {
                        if (matchedUser != null) {
                            cameraService.stop();
                            dialog.close();
                            loginSuccess(matchedUser);
                            NotificationService.show("Bienvenue", "Connexion Face ID réussie !",
                                    NotificationService.NotificationType.SUCCESS);
                        } else {
                            captureBtn.setDisable(false);
                            captureBtn.setText("Réessayer");
                            statusLabel.setText("❌ Visage non reconnu.");
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        captureBtn.setDisable(false);
                        statusLabel.setText("❌ Erreur technique.");
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        });

        cameraService.start();
        dialog.show();
    }

    private User findBestMatch(double[] currentEmbedding, FaceIdService faceIdService) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String sql = "SELECT u.*, b.face_embedding FROM users u JOIN user_biometrics b ON u.id = b.user_id WHERE b.is_active = TRUE";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                double bestSimilarity = -1;
                User bestUser = null;

                while (rs.next()) {
                    String storedStr = rs.getString("face_embedding");
                    double[] storedVector = stringToEmbedding(storedStr);

                    double similarity = faceIdService.calculateSimilarity(currentEmbedding, storedVector);
                    if (similarity > 0.85 && similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                        bestUser = mapUser(rs);
                    }
                }
                return bestUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private double[] stringToEmbedding(String str) {
        if (str == null || str.isEmpty())
            return null;
        String[] parts = str.split(",");
        double[] embedding = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Double.parseDouble(parts[i]);
        }
        return embedding;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role"));
        return u;
    }

    @FXML
    private void goToSignup() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Navigator.goTo(stage, "signup-view.fxml", "Inscription");
    }

    @FXML
    private void goForgotPassword() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Navigator.goTo(stage, "forgot-password-view.fxml", "Mot de passe oublié");
    }
}