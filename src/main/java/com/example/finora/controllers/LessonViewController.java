package com.example.finora.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.finora.entities.Formation;
import com.example.finora.entities.Lesson;
import com.example.finora.services.SpeechService;
import com.example.finora.utils.Session;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.*;

public class LessonViewController {

    @FXML
    private Label lblEyebrow;
    @FXML
    private Label lblTitre;
    @FXML
    private Label lblMeta;
    @FXML
    private Label lblIndex;

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label lblProgress;
    @FXML
    private Label lblProgressDetail;

    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;

    @FXML
    private Label lblRating;
    @FXML
    private Label lblInstructor;
    @FXML
    private Label lblTotalDuration;
    @FXML
    private ToggleButton btnComplete;

    @FXML
    private VBox sidebarBox;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane sidebarScroll;
    @FXML
    private ToggleButton btnFocus;

    @FXML
    private VBox contentWrap;
    @FXML
    private TextFlow contentFlow;
    @FXML
    private TextField txtFind;
    @FXML
    private Label lblFindInfo;

    @FXML
    private Button btnYoutube;
    @FXML
    private Button btnQuiz;
    @FXML
    private Button btnWatchVideo;

    // ✅ TTS buttons (must match fx:id in FXML)
    @FXML
    private Button btnReadLesson;
    @FXML
    private Button btnStopRead;

    private Lesson currentLesson;

    private Runnable onBack;
    private List<Lesson> lessons = new ArrayList<>();
    private int index = 0;
    private Formation formation;

    private final Set<Integer> completedLessonIds = new HashSet<>();
    private final Map<Integer, Node> sidebarNodeByLessonId = new HashMap<>();

    // ✅ paragraph mode
    private final List<Text> paragraphNodes = new ArrayList<>();
    private List<String> paragraphs = new ArrayList<>();

    private String currentContent = "";
    private String currentQuery = "";
    private int currentMatch = -1;
    private int fontSizePx = 16;
    private double lastDividerPos = 0.28;

    private final SpeechService speechService = new SpeechService();
    private int speechRate = 0;

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setLessons(List<Lesson> lessons, int index, Formation formation) {
        this.lessons = (lessons == null) ? new ArrayList<>() : new ArrayList<>(lessons);
        this.index = clamp(index, 0, Math.max(0, this.lessons.size() - 1));
        this.formation = formation;
        buildSidebar();
        render(false);
    }

    public void setData(Lesson lesson, Formation formation) {
        this.lessons = new ArrayList<>();
        if (lesson != null)
            this.lessons.add(lesson);
        this.index = 0;
        this.formation = formation;
        buildSidebar();
        render(false);
    }

    // ================= TTS =================

    @FXML
    private void onReadLesson() {
        if (currentContent == null || currentContent.isBlank()) {
            showWarn("Aucun contenu à lire.");
            return;
        }

        // If user is searching, switch to paragraph mode for reading (better highlight)
        if (txtFind != null)
            txtFind.setText("");
        currentQuery = "";
        renderReader(); // rebuild paragraphNodes

        if (btnReadLesson != null)
            btnReadLesson.setText("⏳ Lecture...");
        if (btnStopRead != null)
            btnStopRead.setText("⏸ Pause");

        if (paragraphs == null || paragraphs.isEmpty()) {
            paragraphs = splitParagraphs(currentContent);
        }

        speechService.setRate(speechRate);

        speechService.speakParagraphs(paragraphs, new SpeechService.ParagraphListener() {
            @Override
            public void onParagraphStart(int idx, int total) {
                Platform.runLater(() -> highlightParagraph(idx));
            }

            @Override
            public void onFinished() {
                Platform.runLater(() -> {
                    highlightParagraph(-1);
                    if (btnReadLesson != null)
                        btnReadLesson.setText("🔊 Lire la leçon");
                    if (btnStopRead != null)
                        btnStopRead.setText("⏸ Pause");
                });
            }
        });
    }

    // Toggle Pause/Resume on the same button
    @FXML
    private void onStopReading() {
        if (speechService.isPlaying()) {
            speechService.pause();
            if (btnStopRead != null)
                btnStopRead.setText("▶ Reprendre");
        } else if (speechService.isPaused()) {
            speechService.resume();
            if (btnStopRead != null)
                btnStopRead.setText("⏸ Pause");
        } else {
            // nothing playing
            if (btnStopRead != null)
                btnStopRead.setText("⏸ Pause");
            if (btnReadLesson != null)
                btnReadLesson.setText("🔊 Lire la leçon");
        }
    }

    private void stopTtsFully() {
        speechService.stop();
        highlightParagraph(-1);
        if (btnReadLesson != null)
            btnReadLesson.setText("🔊 Lire la leçon");
        if (btnStopRead != null)
            btnStopRead.setText("⏸ Pause");
    }

    @FXML
    private void onSlower() {
        speechRate = Math.max(-10, speechRate - 1);
        speechService.setRate(speechRate);
    }

    @FXML
    private void onFaster() {
        speechRate = Math.min(10, speechRate + 1);
        speechService.setRate(speechRate);
    }

    // ================= BACK =================

    @FXML
    private void onBack() {
        stopTtsFully();
        if (onBack != null) {
            onBack.run();
        } else {
            // Default fallback if no specific onBack logic is provided
            Stage stage = (Stage) lblTitre.getScene().getWindow();
            if (stage.getScene() != null
                    && stage.getScene().lookup("#contentArea") instanceof javafx.scene.layout.StackPane) {
                // Do nothing or handle appropriately for shell
            } else {
                stage.close();
            }
        }
    }

    // ================= SIDEBAR =================

    private void buildSidebar() {
        if (sidebarBox == null)
            return;

        sidebarBox.getChildren().clear();
        sidebarNodeByLessonId.clear();

        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);

            Button item = new Button();
            item.getStyleClass().add("curr-item");
            item.setMaxWidth(Double.MAX_VALUE);

            Label left = new Label(String.format("%02d", i + 1));
            left.getStyleClass().add("curr-num");

            Label mid = new Label(safe(l.getTitre(), "(Sans titre)"));
            mid.getStyleClass().add("curr-title");
            mid.setWrapText(true);

            Label right = new Label(isCompleted(l) ? "✓" : "");
            right.getStyleClass().add("curr-check");

            HBox row = new HBox(10, left, mid);
            row.getStyleClass().add("curr-row");
            HBox.setHgrow(mid, Priority.ALWAYS);
            row.getChildren().add(right);

            item.setGraphic(row);
            item.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            final int targetIndex = i;
            item.setOnAction(e -> {
                if (targetIndex == index)
                    return;
                stopTtsFully(); // ✅ real stop when switching lesson
                index = targetIndex;
                render(true);
            });

            sidebarBox.getChildren().add(item);
            sidebarNodeByLessonId.put(l.getId(), item);
        }

        updateSidebarStyles();
    }

    private void updateSidebarStyles() {
        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);
            Node n = sidebarNodeByLessonId.get(l.getId());
            if (n == null)
                continue;

            n.getStyleClass().removeAll("curr-item-active", "curr-item-done");
            if (i == index)
                n.getStyleClass().add("curr-item-active");
            if (isCompleted(l))
                n.getStyleClass().add("curr-item-done");

            if (n instanceof Button b && b.getGraphic() instanceof HBox row) {
                for (Node child : row.getChildren()) {
                    if (child instanceof Label lab && lab.getStyleClass().contains("curr-check")) {
                        lab.setText(isCompleted(l) ? "✓" : "");
                    }
                }
            }
        }
    }

    private boolean isCompleted(Lesson l) {
        return l != null && completedLessonIds.contains(l.getId());
    }

    // ================= RENDER =================

    private void render(boolean animate) {
        if (lessons.isEmpty())
            return;

        Lesson lesson = lessons.get(index);
        currentLesson = lesson;

        if (btnQuiz != null) {
            btnQuiz.setVisible(true);
            btnQuiz.setManaged(true);
        }

        if (btnYoutube != null) {
            boolean admin = Session.isAdmin();
            btnYoutube.setVisible(admin);
            btnYoutube.setManaged(admin);
        }

        if (lblEyebrow != null) {
            String name = (formation != null && formation.getTitre() != null && !formation.getTitre().isBlank())
                    ? formation.getTitre().toUpperCase()
                    : "FORMATION #" + lesson.getFormationId();
            lblEyebrow.setText(name);
        }

        if (lblTitre != null)
            lblTitre.setText(safe(lesson.getTitre(), "(Sans titre)"));
        if (lblMeta != null)
            lblMeta.setText("Ordre " + lesson.getOrdre() + "   •   " + lesson.getDureeMinutes() + " min");
        if (lblIndex != null)
            lblIndex.setText(String.format("%02d / %02d", index + 1, lessons.size()));

        if (lblInstructor != null)
            lblInstructor.setText("Instructeur : Finora Academy");
        if (lblTotalDuration != null)
            lblTotalDuration.setText("⏱ " + totalDurationMinutes() + " min total");
        if (lblRating != null)
            lblRating.setText(starsFor(lesson) + "  " + ratingFor(lesson));

        if (btnComplete != null) {
            boolean done = isCompleted(lesson);
            btnComplete.setSelected(done);
            btnComplete.setText(done ? "✓ Terminé" : "Marquer comme terminé");
        }

        if (btnWatchVideo != null) {
            String url = safeRaw(lesson.getVideoUrl()).trim();
            boolean has = !url.isBlank();
            btnWatchVideo.setVisible(has);
            btnWatchVideo.setManaged(has);
        }

        Runnable applyContent = () -> {
            currentContent = safe(lesson.getContenu(), "");
            currentMatch = -1;
            highlightParagraph(-1);
            paragraphs = splitParagraphs(currentContent);
            renderReader();
        };

        if (animate && contentWrap != null)
            fadeSwap(contentWrap, applyContent);
        else
            applyContent.run();

        updateProgressAnimated();
        updateButtons();
        updateSidebarStyles();

        if (btnReadLesson != null)
            btnReadLesson.setText("🔊 Lire la leçon");
        if (btnStopRead != null)
            btnStopRead.setText("⏸ Pause");
    }

    // ================= READER =================

    private void renderReader() {
        if (contentFlow == null)
            return;

        contentFlow.getChildren().clear();
        paragraphNodes.clear();

        String q = (txtFind == null) ? "" : safe(txtFind.getText(), "").trim();
        currentQuery = q;

        // ✅ If search is empty -> paragraph rendering (supports TTS highlight)
        if (q.isBlank()) {
            if (paragraphs == null || paragraphs.isEmpty())
                paragraphs = splitParagraphs(currentContent);

            for (int i = 0; i < paragraphs.size(); i++) {
                Text t = new Text(paragraphs.get(i) + "\n\n");
                t.getStyleClass().add("reader-text");
                t.setStyle("-fx-font-size: " + fontSizePx + "px;");
                contentFlow.getChildren().add(t);
                paragraphNodes.add(t);
            }

            if (lblFindInfo != null)
                lblFindInfo.setText("");
            return;
        }

        // ✅ Otherwise keep your existing search highlighting behavior
        List<int[]> matches = findAllMatches(currentContent, q);
        if (matches.isEmpty()) {
            contentFlow.getChildren().add(makeText(currentContent, false, false));
            currentMatch = -1;
            if (lblFindInfo != null)
                lblFindInfo.setText("Aucun résultat");
            return;
        }

        if (currentMatch < 0)
            currentMatch = 0;
        if (currentMatch >= matches.size())
            currentMatch = matches.size() - 1;

        int cursor = 0;
        for (int i = 0; i < matches.size(); i++) {
            int start = matches.get(i)[0];
            int end = matches.get(i)[1];

            if (start > cursor)
                contentFlow.getChildren().add(makeText(currentContent.substring(cursor, start), false, false));

            boolean active = (i == currentMatch);
            contentFlow.getChildren().add(makeText(currentContent.substring(start, end), true, active));

            cursor = end;
        }

        if (cursor < currentContent.length()) {
            contentFlow.getChildren().add(makeText(currentContent.substring(cursor), false, false));
        }

        if (lblFindInfo != null)
            lblFindInfo.setText((currentMatch + 1) + " / " + matches.size());
    }

    private List<String> splitParagraphs(String text) {
        if (text == null)
            return List.of();
        String cleaned = text.trim().replace("\r", "");
        String[] parts = cleaned.split("\\n\\s*\\n+");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim().replaceAll("\\s+", " ");
            if (!s.isBlank())
                out.add(s);
        }
        return out;
    }

    private void highlightParagraph(int idx) {
        for (int i = 0; i < paragraphNodes.size(); i++) {
            Text t = paragraphNodes.get(i);
            t.getStyleClass().remove("reader-speaking");
            if (i == idx)
                t.getStyleClass().add("reader-speaking");
        }
    }

    private List<int[]> findAllMatches(String text, String query) {
        List<int[]> out = new ArrayList<>();
        if (text == null || query == null)
            return out;

        String t = text.toLowerCase(Locale.ROOT);
        String q = query.toLowerCase(Locale.ROOT);

        int from = 0;
        while (from < t.length()) {
            int idx = t.indexOf(q, from);
            if (idx < 0)
                break;
            out.add(new int[] { idx, idx + q.length() });
            from = idx + q.length();
        }
        return out;
    }

    private Text makeText(String s, boolean highlight, boolean active) {
        Text t = new Text(s);
        t.getStyleClass().add("reader-text");
        t.setStyle("-fx-font-size: " + fontSizePx + "px;");
        if (highlight)
            t.getStyleClass().add("reader-highlight");
        if (active)
            t.getStyleClass().add("reader-highlight-active");
        return t;
    }

    // ================= PROGRESS =================

    private void updateProgressAnimated() {
        if (progressBar == null)
            return;

        int total = Math.max(1, lessons.size());
        int done = (int) lessons.stream().filter(this::isCompleted).count();
        double target = done / (double) total;

        if (lblProgress != null)
            lblProgress.setText((int) Math.round(target * 100) + "%");
        if (lblProgressDetail != null)
            lblProgressDetail.setText(done + " / " + total + " terminées");

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(260),
                        new KeyValue(progressBar.progressProperty(), target, Interpolator.EASE_BOTH)));
        tl.play();
    }

    private void updateButtons() {
        if (btnPrev != null)
            btnPrev.setDisable(index <= 0);
        if (btnNext != null)
            btnNext.setDisable(index >= lessons.size() - 1);
    }

    // ================= NAV =================

    @FXML
    private void onPrev() {
        if (index > 0) {
            stopTtsFully();
            index--;
            render(true);
        }
    }

    @FXML
    private void onNext() {
        if (index < lessons.size() - 1) {
            stopTtsFully();
            index++;
            render(true);
        }
    }

    @FXML
    private void onFindChanged() {
        currentMatch = -1;
        renderReader();
    }

    @FXML
    private void onFindNext() {
        if (currentQuery == null || currentQuery.isBlank()) {
            renderReader();
            return;
        }
        List<int[]> matches = findAllMatches(currentContent, currentQuery);
        if (matches.isEmpty()) {
            renderReader();
            return;
        }
        currentMatch = (currentMatch < 0) ? 0 : (currentMatch + 1) % matches.size();
        renderReader();
    }

    @FXML
    private void onFindPrev() {
        if (currentQuery == null || currentQuery.isBlank()) {
            renderReader();
            return;
        }
        List<int[]> matches = findAllMatches(currentContent, currentQuery);
        if (matches.isEmpty()) {
            renderReader();
            return;
        }
        currentMatch = (currentMatch < 0) ? (matches.size() - 1) : (currentMatch - 1 + matches.size()) % matches.size();
        renderReader();
    }

    @FXML
    private void onCopy() {
        ClipboardContent cc = new ClipboardContent();
        cc.putString(currentContent == null ? "" : currentContent);
        Clipboard.getSystemClipboard().setContent(cc);
    }

    @FXML
    private void onFontMinus() {
        fontSizePx = Math.max(13, fontSizePx - 1);
        renderReader();
    }

    @FXML
    private void onFontPlus() {
        fontSizePx = Math.min(22, fontSizePx + 1);
        renderReader();
    }

    @FXML
    private void onToggleComplete() {
        if (lessons.isEmpty())
            return;
        Lesson cur = lessons.get(index);
        if (cur == null)
            return;

        if (completedLessonIds.contains(cur.getId()))
            completedLessonIds.remove(cur.getId());
        else
            completedLessonIds.add(cur.getId());

        render(false);
    }

    // ================= FOCUS =================

    @FXML
    private void onToggleFocus() {
        boolean focus = btnFocus != null && btnFocus.isSelected();
        if (splitPane == null || sidebarScroll == null)
            return;

        if (focus) {
            if (!splitPane.getDividers().isEmpty())
                lastDividerPos = splitPane.getDividers().get(0).getPosition();
            sidebarScroll.setVisible(false);
            sidebarScroll.setManaged(false);
            splitPane.setDividerPositions(0.0);
        } else {
            sidebarScroll.setManaged(true);
            sidebarScroll.setVisible(true);
            splitPane.setDividerPositions(lastDividerPos <= 0 ? 0.28 : lastDividerPos);
        }
    }

    // ================= VIDEO =================

    @FXML
    private void onWatchVideo() {
        if (lessons.isEmpty())
            return;

        Lesson lesson = lessons.get(index);
        String url = safeRaw(lesson.getVideoUrl()).trim();
        if (url.isBlank()) {
            showWarn("Aucune vidéo pour cette leçon.");
            return;
        }

        if (looksLikeMp4(url)) {
            try {
                openMp4Player(url);
                return;
            } catch (Exception ex) {
                showWarn("Lecture interne impossible. Ouverture dans le navigateur...");
                openInBrowser(url);
                return;
            }
        }
        openInBrowser(url);
    }

    private boolean looksLikeMp4(String url) {
        String u = url.toLowerCase(Locale.ROOT).trim();
        int q = u.indexOf('?');
        if (q > 0)
            u = u.substring(0, q);
        return u.endsWith(".mp4");
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showError("Desktop non supporté pour ouvrir le navigateur.");
            }
        } catch (Exception e) {
            showError("Impossible d'ouvrir le lien: " + e.getMessage());
        }
    }

    private void openMp4Player(String url) {
        Media media = new Media(url);
        MediaPlayer player = new MediaPlayer(media);
        MediaView view = new MediaView(player);

        view.setPreserveRatio(true);
        view.setFitWidth(900);

        Button btnPlayPause = new Button("⏯");
        btnPlayPause.getStyleClass().add("reader-toolbtn");

        Slider time = new Slider(0, 100, 0);
        time.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(time, Priority.ALWAYS);

        Label lblTime = new Label("00:00 / 00:00");
        lblTime.getStyleClass().add("viewer-meta");

        Button btnExternal = new Button("↗ Ouvrir");
        btnExternal.getStyleClass().add("reader-toolbtn");
        btnExternal.setOnAction(e -> openInBrowser(url));

        btnPlayPause.setOnAction(e -> {
            MediaPlayer.Status st = player.getStatus();
            if (st == MediaPlayer.Status.PLAYING)
                player.pause();
            else
                player.play();
        });

        player.currentTimeProperty().addListener((obs, oldV, newV) -> {
            if (!time.isValueChanging() && player.getTotalDuration() != null
                    && !player.getTotalDuration().isUnknown()) {
                double p = newV.toMillis() / player.getTotalDuration().toMillis();
                time.setValue(p * 100.0);
            }
            lblTime.setText(fmt(player.getCurrentTime().toMillis()) + " / " + fmtTotal(player));
        });

        time.valueChangingProperty().addListener((obs, was, is) -> {
            if (!is && player.getTotalDuration() != null && !player.getTotalDuration().isUnknown()) {
                double target = time.getValue() / 100.0 * player.getTotalDuration().toMillis();
                player.seek(Duration.millis(target));
            }
        });

        HBox controls = new HBox(10, btnPlayPause, time, lblTime, btnExternal);
        controls.setPadding(new javafx.geometry.Insets(12));
        controls.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox root = new VBox(10, view, controls);
        root.setPadding(new javafx.geometry.Insets(16));
        root.getStyleClass().add("card");

        Stage stage = new Stage();
        stage.setTitle("Vidéo - " + safe(lessons.get(index).getTitre(), "Lesson"));
        Scene scene = new Scene(root, 980, 620);

        scene.getStylesheets().add(getClass().getResource("/formation/style.css").toExternalForm());
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> {
            try {
                player.stop();
            } catch (Exception ignored) {
            }
            try {
                player.dispose();
            } catch (Exception ignored) {
            }
        });

        stage.show();
        player.play();
    }

    private String fmt(double ms) {
        int total = (int) Math.floor(ms / 1000.0);
        int m = total / 60;
        int s = total % 60;
        return String.format("%02d:%02d", m, s);
    }

    private String fmtTotal(MediaPlayer p) {
        if (p.getTotalDuration() == null || p.getTotalDuration().isUnknown())
            return "00:00";
        return fmt(p.getTotalDuration().toMillis());
    }

    private void fadeSwap(Node node, Runnable apply) {
        FadeTransition out = new FadeTransition(Duration.millis(140), node);
        out.setToValue(0);
        FadeTransition in = new FadeTransition(Duration.millis(160), node);
        in.setToValue(1);
        out.setOnFinished(e -> {
            apply.run();
            in.play();
        });
        out.play();
    }

    private int totalDurationMinutes() {
        int sum = 0;
        for (Lesson l : lessons)
            if (l != null)
                sum += Math.max(0, l.getDureeMinutes());
        return sum;
    }

    private String ratingFor(Lesson l) {
        int h = Math.abs(Objects.hash(l.getId(), safe(l.getTitre(), "")));
        double r = 4.2 + (h % 50) / 100.0;
        return String.format(Locale.US, "%.2f", r);
    }

    private String starsFor(Lesson l) {
        double r = Double.parseDouble(ratingFor(l));
        int full = (int) Math.floor(r);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < full; i++)
            sb.append("★");
        while (sb.length() < 5)
            sb.append("☆");
        return sb.toString();
    }

    // ==========================================================
    // ✅ ADDED FUNCTIONS (to match FXML onAction handlers)
    // ==========================================================

    @FXML
    private void onQuiz() {
        if (currentLesson == null)
            return;

        try {
            if (Session.isAdmin()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation/quiz_results.fxml"));
                Scene scene = new Scene(loader.load(), 1100, 720);
                scene.getStylesheets()
                        .add(Objects.requireNonNull(getClass().getResource("/formation/style.css")).toExternalForm());

                Stage stage = new Stage();
                stage.setTitle("Résultats Quiz (Admin)");
                stage.setScene(scene);
                stage.show();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation/quiz_take.fxml"));
            Scene scene = new Scene(loader.load(), 700, 620);
            scene.getStylesheets()
                    .add(Objects.requireNonNull(getClass().getResource("/formation/style.css")).toExternalForm());

            QuizTakeController ctrl = loader.getController();
            ctrl.setData(currentLesson, formation);

            Stage stage = new Stage();
            stage.setTitle("Quiz — " + currentLesson.getTitre());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            showError("Erreur ouverture quiz: " + e.getMessage());
        }
    }

    @FXML
    private void onYoutubeSuggestions() {
        if (currentLesson == null)
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation/youtube_search.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            scene.getStylesheets()
                    .add(Objects.requireNonNull(getClass().getResource("/formation/style.css")).toExternalForm());

            YoutubeSearchController ctrl = loader.getController();
            ctrl.setLesson(currentLesson);

            Stage stage = new Stage();
            stage.setTitle("YouTube — " + currentLesson.getTitre());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }

    // ================= HELPERS =================

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }

    private String safeRaw(String s) {
        return s == null ? "" : s;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}