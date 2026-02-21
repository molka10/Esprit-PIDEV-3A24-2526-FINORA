package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.finora.services.QuizResultService;
import tn.finora.services.QuizResultService.QuizResult;

import java.util.List;

public class QuizResultsController {

    @FXML private VBox cardsContainer;
    @FXML private Label lblTotal;
    @FXML private Label lblPassed;
    @FXML private Label lblAverage;
    @FXML private TextField txtSearch;

    private final QuizResultService service = new QuizResultService();
    private List<QuizResult> allResults;

    @FXML
    public void initialize() {
        loadStats();
        loadResults();
    }

    private void loadStats() {
        int total   = service.countTotal();
        int passed  = service.countPassed();
        double avg  = service.averageScore();
        int rate    = total == 0 ? 0 : (int) Math.round((passed * 100.0) / total);

        if (lblTotal   != null) lblTotal.setText(String.valueOf(total));
        if (lblPassed  != null) lblPassed.setText(passed + "  (" + rate + "% taux de réussite)");
        if (lblAverage != null) lblAverage.setText(String.format("%.1f%%", avg));
    }

    private void loadResults() {
        allResults = service.getAll();
        renderCards(allResults);
    }

    @FXML
    private void onSearch() {
        String q = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            renderCards(allResults);
            return;
        }
        List<QuizResult> filtered = allResults.stream()
                .filter(r -> r.studentName.toLowerCase().contains(q)
                        || r.lessonTitle.toLowerCase().contains(q)
                        || r.formationTitle.toLowerCase().contains(q))
                .toList();
        renderCards(filtered);
    }

    private void renderCards(List<QuizResult> results) {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();

        if (results == null || results.isEmpty()) {
            Label empty = new Label("Aucun résultat de quiz pour le moment.");
            empty.setStyle("-fx-text-fill: #6B5A8A; -fx-font-size: 14px; -fx-padding: 30;");
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (QuizResult r : results) {
            cardsContainer.getChildren().add(buildResultCard(r));
        }
    }

    private HBox buildResultCard(QuizResult r) {
        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-color: #EDE9FE;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(109,40,217,0.07), 14, 0, 0, 3);"
        );

        VBox accent = new VBox();
        accent.setPrefWidth(6);
        accent.setMinWidth(6);
        accent.setStyle(
                "-fx-background-color: " + (r.passed ? "#10B981" : "#EF4444") + ";" +
                        "-fx-background-radius: 14 0 0 14;"
        );

        VBox content = new VBox(6);
        content.setPadding(new Insets(14, 18, 14, 18));
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("👤  " + r.studentName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-font-family: 'Georgia', serif; -fx-text-fill: #1E0A3C;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label scoreBadge = new Label(r.score + "%");
        scoreBadge.setStyle(
                "-fx-background-color: " + (r.passed ? "#D1FAE5" : "#FEE2E2") + ";" +
                        "-fx-text-fill: " + (r.passed ? "#065F46" : "#991B1B") + ";" +
                        "-fx-border-color: " + (r.passed ? "#6EE7B7" : "#FCA5A5") + ";" +
                        "-fx-border-width: 1.5px; -fx-background-radius: 999px; -fx-border-radius: 999px;" +
                        "-fx-padding: 4 14 4 14; -fx-font-size: 13px; -fx-font-weight: 900;"
        );

        Label passedBadge = new Label(r.passed ? "✅ Réussi" : "❌ Échoué");
        passedBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + (r.passed ? "#065F46" : "#991B1B") + ";");

        topRow.getChildren().addAll(nameLabel, scoreBadge, passedBadge);

        Label lessonLabel = new Label("📖  " + r.lessonTitle + "   •   🎓  " + r.formationTitle);
        lessonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B5A8A; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        Label dateLabel = new Label("🕐  " + r.takenAt);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        content.getChildren().addAll(topRow, lessonLabel, dateLabel);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 8 14 8 14;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce résultat ?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    service.delete(r.id);
                    loadStats();
                    loadResults();
                }
            });
        });

        card.getChildren().addAll(accent, content, deleteBtn);
        return card;
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