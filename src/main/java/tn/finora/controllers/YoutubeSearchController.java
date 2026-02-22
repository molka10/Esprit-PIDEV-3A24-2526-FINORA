package tn.finora.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.finora.entities.Lesson;
import tn.finora.services.LessonService;
import tn.finora.services.YouTubeService;
import tn.finora.services.YouTubeService.YouTubeVideo;

import java.sql.SQLException;
import java.util.List;

public class YoutubeSearchController {

    @FXML private TextField txtQuery;
    @FXML private Label lblStatus;
    @FXML private ListView<YouTubeVideo> listResults;

    private final YouTubeService yt = new YouTubeService();
    private final LessonService lessonService = new LessonService();

    private Lesson lesson;

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;

        // Auto-search by lesson title (Option A)
        if (txtQuery != null) txtQuery.setText(lesson.getTitre());
        Platform.runLater(this::onSearch);
    }

    @FXML
    private void onSearch() {
        String q = (txtQuery == null) ? "" : txtQuery.getText().trim();
        if (q.isBlank()) return;

        lblStatus.setText("⏳ Recherche YouTube...");
        listResults.getItems().clear();

        Thread t = new Thread(() -> {
            try {
                List<YouTubeVideo> res = yt.search(q, 6);
                Platform.runLater(() -> {
                    listResults.getItems().setAll(res);
                    lblStatus.setText(res.isEmpty() ? "Aucun résultat." : "Choisissez une vidéo puis Enregistrer.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("❌ " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onChoose() {
        if (lesson == null) return;

        YouTubeVideo selected = listResults.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez une vidéo d’abord.").showAndWait();
            return;
        }

        String url = selected.watchUrl();

        try {
            lessonService.updateVideoUrl(lesson.getId(), url);
            lesson.setVideoUrl(url); // update local object too

            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Vidéo enregistrée pour la leçon.\n" + url).showAndWait();
            onClose();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur DB: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onClose() {
        Stage st = (Stage) txtQuery.getScene().getWindow();
        st.close();
    }
}