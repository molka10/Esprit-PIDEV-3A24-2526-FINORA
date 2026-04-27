package com.example.finora.controllers;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.example.finora.services.QuizResultService;
import com.example.finora.services.QuizResultService.QuizResult;
import com.example.finora.services.CertificateService;

import java.io.File;
import java.util.List;

public class QuizResultsController {

    @FXML private VBox cardsContainer;
    @FXML private Label lblTotal;
    @FXML private Label lblPassed;
    @FXML private Label lblAverage;
    @FXML private TextField txtSearch;

    private final QuizResultService service = new QuizResultService();
    private final CertificateService certificateService = new CertificateService();

    private List<QuizResult> allResults;

    @FXML
    public void initialize() {
        if (txtSearch != null) {
            txtSearch.setStyle("""
                -fx-background-radius: 999;
                -fx-border-radius: 999;
                -fx-padding: 10 14;
                -fx-background-color: white;
                -fx-border-color: rgba(124,58,237,0.25);
                -fx-border-width: 1.2;
                -fx-prompt-text-fill: #8B7BB5;
            """);
        }

        loadStats();
        loadResults();
    }

    private void loadStats() {
        int total   = service.countTotal();
        int passed  = service.countPassed();
        double avg  = service.averageScore();
        int rate    = total == 0 ? 0 : (int) Math.round((passed * 100.0) / total);

        if (lblTotal != null) {
            lblTotal.setText(String.valueOf(total));
            lblTotal.setStyle("""
                -fx-font-size: 44px;
                -fx-font-weight: 900;
                -fx-text-fill: linear-gradient(to right, #7C3AED, #A78BFA);
            """);
        }

        if (lblPassed != null) {
            lblPassed.setText(passed + "  (" + rate + "% taux de réussite)");
            lblPassed.setStyle("""
                -fx-font-size: 16px;
                -fx-font-weight: 800;
                -fx-text-fill: #1E0A3C;
            """);
        }

        if (lblAverage != null) {
            lblAverage.setText(String.format("%.1f%%", avg));
            lblAverage.setStyle("""
                -fx-font-size: 34px;
                -fx-font-weight: 900;
                -fx-text-fill: #1E0A3C;
            """);
        }
    }

    private void loadResults() {
        allResults = service.getAll();
        renderCards(allResults);
    }

    @FXML
    private void onSearch() {
        final String query = (txtSearch == null || txtSearch.getText() == null)
                ? ""
                : txtSearch.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            renderCards(allResults);
            return;
        }

        List<QuizResult> filtered = allResults.stream()
                .filter(r ->
                        safeLower(r.studentName).contains(query)
                                || safeLower(r.lessonTitle).contains(query)
                                || safeLower(r.formationTitle).contains(query)
                )
                .toList();

        renderCards(filtered);
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private void renderCards(List<QuizResult> results) {
        if (cardsContainer == null) return;

        cardsContainer.getChildren().clear();

        if (results == null || results.isEmpty()) {
            Label empty = new Label("Aucun résultat de quiz pour le moment.");
            empty.setStyle("""
                -fx-text-fill: #6B5A8A;
                -fx-font-size: 14px;
                -fx-padding: 30;
            """);
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (QuizResult r : results) {
            cardsContainer.getChildren().add(buildResultCard(r));
        }
    }

    private HBox buildResultCard(QuizResult r) {
        final QuizResult result = r;
        boolean passed = r != null && r.passed;

        // ===== Card Container =====
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));

        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.75);
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: rgba(124,58,237,0.20);
            -fx-border-width: 1.2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 20, 0.25, 0, 8);
        """);

        // ===== Accent Bar (left) =====
        Region accent = new Region();
        accent.setPrefWidth(6);
        accent.setMinWidth(6);
        accent.setMaxWidth(6);

        accent.setStyle("""
            -fx-background-radius: 18;
            -fx-background-color: %s;
        """.formatted(passed ? "#22C55E" : "#EF4444"));

        // ===== Content =====
        VBox content = new VBox(8);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Top Row
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("👤  " + safe(r.studentName, "Étudiant"));
        nameLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: 900;
            -fx-text-fill: #1E0A3C;
        """);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label scoreBadge = pill(
                (r == null ? 0 : r.score) + "%",
                passed ? "#DCFCE7" : "#FEE2E2",
                passed ? "#166534" : "#991B1B",
                passed ? "#86EFAC" : "#FCA5A5"
        );

        Label passedBadge = new Label(passed ? "✅ Réussi" : "❌ Échoué");
        passedBadge.setStyle("""
            -fx-font-size: 12px;
            -fx-font-weight: 800;
            -fx-text-fill: %s;
        """.formatted(passed ? "#166534" : "#991B1B"));

        top.getChildren().addAll(nameLabel, scoreBadge, passedBadge);

        // Middle Row
        Label lessonLabel = new Label("📖  " + safe(r.lessonTitle, "-") + "   •   🎓  " + safe(r.formationTitle, "-"));
        lessonLabel.setWrapText(true);
        lessonLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #6B5A8A;
        """);

        // Date
        Label dateLabel = new Label("🕐  " + safe(r.takenAt, ""));
        dateLabel.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #9CA3AF;
        """);

        content.getChildren().addAll(top, lessonLabel, dateLabel);

        // ===== Actions (Certificate + Delete) =====
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // 🎓 Certificate button (ONLY if passed)
        Button certBtn = new Button("🎓");
        certBtn.setCursor(Cursor.HAND);
        certBtn.setTooltip(new Tooltip("Générer certificat"));
        certBtn.setStyle("""
            -fx-background-color: rgba(124,58,237,0.12);
            -fx-text-fill: #7C3AED;
            -fx-font-size: 16px;
            -fx-background-radius: 12;
            -fx-padding: 10 12;
        """);

        if (!passed) {
            certBtn.setVisible(false);
            certBtn.setManaged(false);
        } else {
            certBtn.setOnAction(e -> generateCertificateFor(result, certBtn));
        }

        // 🗑 Delete button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setCursor(Cursor.HAND);
        deleteBtn.setStyle("""
            -fx-background-color: rgba(239,68,68,0.08);
            -fx-text-fill: #EF4444;
            -fx-font-size: 16px;
            -fx-background-radius: 12;
            -fx-padding: 10 12;
        """);

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Supprimer ce résultat ?",
                    ButtonType.YES, ButtonType.NO
            );
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    service.delete(result.id);
                    loadStats();
                    loadResults();
                }
            });
        });

        actions.getChildren().addAll(certBtn, deleteBtn);

        addHoverScale(card);

        card.getChildren().addAll(accent, content, actions);
        return card;
    }

    private void generateCertificateFor(QuizResult r, Button certBtn) {
        if (r == null) return;

        String oldText = certBtn.getText();
        certBtn.setDisable(true);
        certBtn.setText("⏳");

        new Thread(() -> {
            try {
                String home = System.getProperty("user.home");
                String dirPath = home + "/FINORA-Certificates/";
                String fileName = "Certificate_" + r.id + "_" + safeFileName(r.studentName) + ".pdf";
                String outputPath = dirPath + fileName;

                File pdf = certificateService.generateCertificate(
                        safe(r.studentName, "Student"),
                        safe(r.lessonTitle, ""),
                        safe(r.formationTitle, ""),
                        r.score,
                        outputPath
                );

                Platform.runLater(() -> {
                    certBtn.setDisable(false);
                    certBtn.setText(oldText);

                    try {
                        CertificateService.openFile(pdf);
                    } catch (Exception ignored) {}

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Certificat");
                    a.setHeaderText("✅ Certificat généré !");
                    a.setContentText("Fichier : " + pdf.getAbsolutePath());
                    a.showAndWait();
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    certBtn.setDisable(false);
                    certBtn.setText(oldText);

                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setTitle("Certificat");
                    a.setHeaderText("❌ Erreur génération certificat");
                    a.setContentText(ex.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }

    private String safeFileName(String s) {
        if (s == null) return "student";
        String t = s.trim();
        if (t.isEmpty()) return "student";
        return t.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private void addHoverScale(Region node) {
        ScaleTransition stIn = new ScaleTransition(Duration.millis(140), node);
        stIn.setToX(1.015);
        stIn.setToY(1.015);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(140), node);
        stOut.setToX(1.0);
        stOut.setToY(1.0);

        node.setOnMouseEntered(e -> stIn.playFromStart());
        node.setOnMouseExited(e -> stOut.playFromStart());
    }

    private Label pill(String text, String bg, String fg, String border) {
        Label l = new Label(text);
        l.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-border-color: %s;
            -fx-border-width: 1.2;
            -fx-background-radius: 999;
            -fx-border-radius: 999;
            -fx-padding: 5 14;
            -fx-font-size: 13px;
            -fx-font-weight: 900;
        """.formatted(bg, fg, border));
        return l;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }

    @FXML
    private void onRefresh() {
        loadStats();
        loadResults();
        if (txtSearch != null) txtSearch.clear();
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        stage.close();
    }
}