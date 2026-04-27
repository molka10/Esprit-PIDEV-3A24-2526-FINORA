package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.services.BiometricService;
import com.example.finora.services.UserService;
import com.example.finora.utils.*;
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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

public class ProfileController {

    @FXML private Label avatarText;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;

    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker dobPicker;

    @FXML private Label statusLabel;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label pwdStatusLabel;

    @FXML private Label faceIdStatusLabel;
    @FXML private Button enrollFaceBtn;

    private UserService userService;
    private BiometricService biometricService = new BiometricService();

    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            updateFaceIdStatus();
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText("❌ Erreur de connexion au serveur.");
            }
            e.printStackTrace();
        }
        loadFromSession();
    }

    private void updateFaceIdStatus() {
        try {
            User u = Session.getCurrentUser();
            if (u != null && biometricService.hasFaceId(u.getId())) {
                faceIdStatusLabel.setText("✅ Activé");
                faceIdStatusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                enrollFaceBtn.setText("Reconfigurer");
            } else {
                faceIdStatusLabel.setText("Non configuré");
                faceIdStatusLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                enrollFaceBtn.setText("Configurer");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFromSession() {
        User u = Session.getCurrentUser();
        if (u == null) {
            statusLabel.setText("⚠️ Session expirée.");
            return;
        }
        avatarText.setText(getInitials(u.getUsername()));
        fullNameLabel.setText(u.getUsername());
        emailLabel.setText(u.getEmail());
        usernameField.setText(u.getUsername());
        phoneField.setText(u.getPhone() == null ? "" : u.getPhone());
        addressField.setText(u.getAddress() == null ? "" : u.getAddress());
        dobPicker.setValue(u.getDateOfBirth());
    }

    @FXML
    private void handleSave() {
        try {
            User current = Session.getCurrentUser();
            if (current == null) {
                statusLabel.setText("⚠️ Session expirée.");
                return;
            }
            System.out.println("[ProfileEdit] Before update: " + current);
            current.setUsername(usernameField.getText().trim());
            current.setPhone(phoneField.getText() == null ? "" : phoneField.getText().trim());
            current.setAddress(addressField.getText() == null ? "" : addressField.getText().trim());
            current.setDateOfBirth(dobPicker.getValue());
            System.out.println("[ProfileEdit] After update: " + current);

            boolean ok = userService.updateUser(current);
            System.out.println("[ProfileEdit] updateUser returned: " + ok);
            statusLabel.setText(ok ? "✅ Profil mis à jour." : "⚠️ Mise à jour échouée.");
            loadFromSession();
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangePassword() {
        try {
            User current = Session.getCurrentUser();
            if (current == null) {
                pwdStatusLabel.setText("⚠️ Session expirée.");
                return;
            }

            String oldPass = oldPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirm = confirmPasswordField.getText();

            System.out.println("[PasswordChange] Attempting password change for user: " + current.getEmail());

            if (oldPass == null || oldPass.isBlank()
                    || newPass == null || newPass.isBlank()
                    || confirm == null || confirm.isBlank()) {
                pwdStatusLabel.setText("⚠️ Remplissez tous les champs.");
                return;
            }
            if (newPass.length() < 8) {
                pwdStatusLabel.setText("⚠️ Nouveau mot de passe (min 8 caractères).");
                return;
            }
            if (!newPass.equals(confirm)) {
                pwdStatusLabel.setText("⚠️ Confirmation incorrecte.");
                return;
            }

            boolean ok = userService.changePassword(current.getId(), oldPass, newPass);
            System.out.println("[PasswordChange] changePassword returned: " + ok);
            if (ok) {
                pwdStatusLabel.setText("✅ Mot de passe mis à jour.");
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                pwdStatusLabel.setText("❌ Ancien mot de passe incorrect.");
            }
        } catch (SQLException e) {
            pwdStatusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnrollFace() {
        CameraService cameraService = new CameraService();
        FaceIdService faceIdService = new FaceIdService();
        User currentUser = Session.getCurrentUser();

        if (currentUser == null) return;

        if (cameraService.getWebcam() == null) {
            NotificationService.show("Erreur", "Aucune caméra détectée.",
                    NotificationService.NotificationType.ERROR);
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        ImageView preview = new ImageView();
        preview.setFitWidth(400);
        preview.setFitHeight(300);
        preview.imageProperty().bind(cameraService.imageProperty());

        Button captureBtn = new Button("Enregistrer mon visage");
        captureBtn.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 20;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20;");

        Label infoLabel = new Label("Positionnez votre visage dans le cadre");
        infoLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");

        VBox layout = new VBox(20, new Label("Activation Face ID") {{
            setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        }}, preview, infoLabel, captureBtn, cancelBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(
                "-fx-background-color: rgba(15, 23, 42, 0.95); -fx-padding: 30; -fx-background-radius: 20;");

        dialog.setScene(new Scene(layout));
        dialog.getScene().setFill(null);

        cancelBtn.setOnAction(e -> {
            cameraService.stop();
            dialog.close();
        });

        captureBtn.setOnAction(e -> {
            captureBtn.setDisable(true);
            captureBtn.setText("IA en cours...");
            infoLabel.setText("Analyse du visage...");

            Thread thread = new Thread(() -> {
                try {
                    // Use BufferedImage directly — no FX Image conversion needed
                    BufferedImage bimg = cameraService.captureSnapshot();

                    if (bimg == null) {
                        System.err.println("captureSnapshot() returned null — no frame yet");
                        Platform.runLater(() -> {
                            captureBtn.setDisable(false);
                            captureBtn.setText("Réessayer");
                            infoLabel.setText("❌ Aucune image capturée, réessayez.");
                        });
                        return;
                    }

                    System.out.println("BufferedImage size: " + bimg.getWidth() + "x" + bimg.getHeight()
                            + " type=" + bimg.getType());

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    boolean written = ImageIO.write(bimg, "jpg", out);

                    if (!written) {
                        System.err.println("ImageIO.write() failed — no writer found for jpg");
                        Platform.runLater(() -> {
                            captureBtn.setDisable(false);
                            captureBtn.setText("Réessayer");
                            infoLabel.setText("❌ Erreur encodage image.");
                        });
                        return;
                    }

                    byte[] imageBytes = out.toByteArray();
                    System.out.println("Captured image bytes length: " + imageBytes.length);

                    if (imageBytes.length < 100) {
                        System.err.println("Image bytes too small: " + imageBytes.length);
                        Platform.runLater(() -> {
                            captureBtn.setDisable(false);
                            captureBtn.setText("Réessayer");
                            infoLabel.setText("❌ Image invalide, réessayez.");
                        });
                        return;
                    }

                    double[] embedding = faceIdService.getFaceEmbedding(imageBytes);

                    if (embedding != null && embedding.length > 0) {
                        biometricService.saveBiometric(currentUser.getId(), embedding);
                        Platform.runLater(() -> {
                            cameraService.stop();
                            dialog.close();
                            updateFaceIdStatus();
                            NotificationService.show("Succès", "Face ID activé !",
                                    NotificationService.NotificationType.SUCCESS);
                        });
                    } else {
                        Platform.runLater(() -> {
                            captureBtn.setDisable(false);
                            captureBtn.setText("Réessayer");
                            infoLabel.setText("❌ Visage non détecté, réessayez.");
                        });
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        captureBtn.setDisable(false);
                        captureBtn.setText("Réessayer");
                        infoLabel.setText("❌ Erreur: " + ex.getMessage());
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        });

        // Start camera and give it 500ms to warm up before showing dialog
        cameraService.start();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        dialog.show();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String n = name.trim();
        if (n.length() == 1) return n.toUpperCase();
        return ("" + n.charAt(0) + n.charAt(n.length() - 1)).toUpperCase();
    }
}