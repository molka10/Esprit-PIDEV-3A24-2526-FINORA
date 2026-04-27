package com.example.finora.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.finora.entities.Lesson;
import com.example.finora.services.LessonService;
import com.example.finora.services.YouTubeService;
import com.example.finora.services.YouTubeService.YouTubeVideo;
import com.example.finora.utils.Session;

import java.awt.*;
import java.net.URI;
import java.util.List;

public class YoutubeSearchController {

    @FXML
    private Label lblLessonTitle;

    @FXML
    private HBox currentVideoRow;
    @FXML
    private Label lblCurrentVideo;

    @FXML
    private TextField txtQuery;
    @FXML
    private ComboBox<String> cmbSort;

    @FXML
    private ProgressIndicator loader;
    @FXML
    private Label lblStatus;

    @FXML
    private VBox resultsBox;

    private final YouTubeService yt = new YouTubeService();
    private final LessonService lessonService;

    public YoutubeSearchController() {
        this.lessonService = new LessonService();
    }

    private Lesson lesson;

    @FXML
    public void initialize() {
        if (cmbSort != null) {
            cmbSort.getItems().setAll("Pertinence", "Date", "Vues", "Note", "Titre");
            cmbSort.getSelectionModel().select(0);
        }
        setLoading(false);
        setStatus("");
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;

        if (lblLessonTitle != null) {
            lblLessonTitle.setText("Leçon: " + safe(lesson.getTitre(), "..."));
        }

        refreshCurrentVideoRow();

        // Auto-search by lesson title (Option A)
        if (txtQuery != null)
            txtQuery.setText(safe(lesson.getTitre(), ""));
        Platform.runLater(this::onSearch);
    }

    private void refreshCurrentVideoRow() {
        if (lesson == null)
            return;

        String url = safe(lesson.getVideoUrl(), "").trim();
        boolean has = !url.isBlank();

        if (currentVideoRow != null) {
            currentVideoRow.setVisible(has);
            currentVideoRow.setManaged(has);
        }
        if (lblCurrentVideo != null) {
            lblCurrentVideo.setText(has ? ("Vidéo liée: " + url) : "");
        }
    }

    @FXML
    private void onSearch() {
        if (lesson == null)
            return;

        String q = (txtQuery == null) ? "" : txtQuery.getText().trim();
        if (q.isBlank()) {
            setStatus("Tapez un mot-clé pour chercher sur YouTube.");
            return;
        }

        setLoading(true);
        setStatus("⏳ Recherche YouTube...");
        resultsBox.getChildren().clear();

        String order = mapOrder(cmbSort == null ? "Pertinence" : cmbSort.getValue());

        Thread t = new Thread(() -> {
            try {
                List<YouTubeVideo> res = yt.search(q, 8, order);

                Platform.runLater(() -> {
                    resultsBox.getChildren().clear();

                    if (res.isEmpty()) {
                        resultsBox.getChildren().add(emptyState("Aucun résultat trouvé."));
                        setStatus("Aucun résultat.");
                    } else {
                        for (YouTubeVideo v : res) {
                            resultsBox.getChildren().add(buildCard(v));
                        }
                        setStatus("✅ Choisissez une vidéo pour l’associer à la leçon.");
                    }

                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultsBox.getChildren().clear();
                    resultsBox.getChildren().add(emptyState("❌ " + e.getMessage()));
                    setStatus("❌ " + e.getMessage());
                    setLoading(false);
                });
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private Node buildCard(YouTubeVideo v) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-color: #EDE9FE;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(109,40,217,0.07), 12, 0, 0, 3);");

        ImageView thumb = new ImageView();
        thumb.setFitWidth(160);
        thumb.setFitHeight(90);
        thumb.setPreserveRatio(false);
        thumb.setSmooth(true);
        thumb.setStyle("-fx-background-radius: 10px; -fx-border-radius: 10px;");

        // safe thumbnail loading
        String turl = safe(v.thumbnailUrl, "").trim();
        if (!turl.isBlank()) {
            try {
                // backgroundLoading=true
                Image img = new Image(turl, true);
                thumb.setImage(img);
            } catch (Exception ignored) {
            }
        }

        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(safe(v.title, "(Sans titre)"));
        title.setWrapText(true);
        title.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-family: 'Georgia', serif;" +
                        "-fx-text-fill: #1E0A3C;");

        Label channel = new Label("📺 " + safe(v.channelTitle, ""));
        channel.setStyle("-fx-text-fill: #6B5A8A; -fx-font-size: 12px;");

        Label url = new Label(v.watchUrl());
        url.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        info.getChildren().addAll(title, channel, url);

        VBox actions = new VBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnPreview = new Button("▶ Ouvrir");
        btnPreview.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #7C3AED;" +
                        "-fx-border-color: #C4B5FD;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-padding: 8 14 8 14;" +
                        "-fx-cursor: hand;");
        btnPreview.setOnAction(e -> openInBrowser(v.watchUrl()));

        Button btnLink = new Button("✅ Associer");
        btnLink.setStyle(
                "-fx-background-color: #7C3AED;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 8 14 8 14;" +
                        "-fx-cursor: hand;");
        btnLink.setOnAction(e -> linkVideo(v.watchUrl()));

        actions.getChildren().addAll(btnPreview, btnLink);

        card.getChildren().addAll(thumb, info, actions);
        return card;
    }

    private Node emptyState(String msg) {
        Label lab = new Label(msg);
        lab.setWrapText(true);
        lab.setStyle("-fx-text-fill: #6B5A8A; -fx-font-size: 14px; -fx-padding: 30;");
        return lab;
    }

    private void linkVideo(String url) {
        if (lesson == null)
            return;

        try {
            lessonService.updateVideoUrl(lesson.getId(), url);
            lesson.setVideoUrl(url);

            refreshCurrentVideoRow();
            new Alert(Alert.AlertType.INFORMATION, "✅ Vidéo associée à la leçon.\n" + url).showAndWait();

            onClose();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur DB: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onRemoveVideo() {
        if (lesson == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la vidéo liée à cette leçon ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    lessonService.updateVideoUrl(lesson.getId(), null);
                    lesson.setVideoUrl(null);
                    refreshCurrentVideoRow();
                    setStatus("✅ Lien supprimé.");
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur DB: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void onClose() {
        Stage st = (Stage) txtQuery.getScene().getWindow();
        st.close();
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le navigateur: " + e.getMessage()).showAndWait();
        }
    }

    private String mapOrder(String uiValue) {
        if (uiValue == null)
            return "relevance";
        return switch (uiValue) {
            case "Date" -> "date";
            case "Vues" -> "viewCount";
            case "Note" -> "rating";
            case "Titre" -> "title";
            default -> "relevance";
        };
    }

    private void setLoading(boolean on) {
        if (loader != null) {
            loader.setVisible(on);
            loader.setManaged(on);
        }
    }

    private void setStatus(String s) {
        if (lblStatus != null)
            lblStatus.setText(s == null ? "" : s);
    }

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }
}