package tn.finora.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;

import java.util.*;

public class LessonViewController {

    // Header
    @FXML private Label lblEyebrow;
    @FXML private Label lblTitre;
    @FXML private Label lblMeta;
    @FXML private Label lblIndex;

    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;
    @FXML private Label lblProgressDetail;

    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    @FXML private Label lblRating;
    @FXML private Label lblInstructor;
    @FXML private Label lblTotalDuration;
    @FXML private ToggleButton btnComplete;

    // Sidebar / Layout
    @FXML private VBox sidebarBox;
    @FXML private SplitPane splitPane;
    @FXML private ScrollPane sidebarScroll;
    @FXML private ToggleButton btnFocus;

    // Reader
    @FXML private VBox contentWrap;
    @FXML private TextFlow contentFlow;
    @FXML private TextField txtFind;
    @FXML private Label lblFindInfo;

    // State
    private Runnable onBack;
    private List<Lesson> lessons = new ArrayList<>();
    private int index = 0;
    private Formation formation;

    // completion tracking (session)
    private final Set<Integer> completedLessonIds = new HashSet<>();
    private final Map<Integer, Node> sidebarNodeByLessonId = new HashMap<>();

    // reader state
    private String currentContent = "";
    private String currentQuery = "";
    private int currentMatch = -1;
    private int fontSizePx = 16;

    // focus mode state
    private boolean focusMode = false;
    private double lastDividerPos = 0.28;

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
        if (lesson != null) this.lessons.add(lesson);
        this.index = 0;
        this.formation = formation;

        buildSidebar();
        render(false);
    }

    // Sidebar
    private void buildSidebar() {
        if (sidebarBox == null) return;

        sidebarBox.getChildren().clear();
        sidebarNodeByLessonId.clear();

        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);

            Button item = new Button();
            item.getStyleClass().add("curr-item");
            item.setMaxWidth(Double.MAX_VALUE);

            String num = String.format("%02d", i + 1);
            String title = safe(l.getTitre(), "(Sans titre)");

            Label left = new Label(num);
            left.getStyleClass().add("curr-num");

            Label mid = new Label(title);
            mid.getStyleClass().add("curr-title");
            mid.setWrapText(true);

            Label right = new Label(isCompleted(l) ? "✓" : "");
            right.getStyleClass().add("curr-check");

            HBox row = new HBox(10, left, mid);
            row.getStyleClass().add("curr-row");
            HBox.setHgrow(mid, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().add(right);

            item.setGraphic(row);
            item.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            final int targetIndex = i;
            item.setOnAction(e -> {
                if (targetIndex == index) return;
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
            if (n == null) continue;

            n.getStyleClass().removeAll("curr-item-active", "curr-item-done");
            if (i == index) n.getStyleClass().add("curr-item-active");
            if (isCompleted(l)) n.getStyleClass().add("curr-item-done");

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

    // Render
    private void render(boolean animate) {
        if (lessons.isEmpty()) return;

        Lesson lesson = lessons.get(index);

        if (lblEyebrow != null) {
            String name = (formation != null && formation.getTitre() != null && !formation.getTitre().isBlank())
                    ? formation.getTitre().toUpperCase()
                    : "FORMATION #" + lesson.getFormationId();
            lblEyebrow.setText(name);
        }

        if (lblTitre != null) lblTitre.setText(safe(lesson.getTitre(), "(Sans titre)"));
        if (lblMeta != null) lblMeta.setText("Ordre " + lesson.getOrdre() + "   •   " + lesson.getDureeMinutes() + " min");
        if (lblIndex != null) lblIndex.setText(String.format("%02d / %02d", index + 1, lessons.size()));

        if (lblInstructor != null) lblInstructor.setText("Instructeur : Finora Academy");
        if (lblTotalDuration != null) lblTotalDuration.setText("⏱ " + totalDurationMinutes() + " min total");
        if (lblRating != null) lblRating.setText(starsFor(lesson) + "  " + ratingFor(lesson));

        if (btnComplete != null) {
            boolean done = isCompleted(lesson);
            btnComplete.setSelected(done);
            btnComplete.setText(done ? "✓ Terminé" : "Marquer comme terminé");
        }

        Runnable applyContent = () -> {
            currentContent = safe(lesson.getContenu(), "");
            currentMatch = -1;
            renderReader();
        };

        if (animate && contentWrap != null) fadeSwap(contentWrap, applyContent);
        else applyContent.run();

        updateProgressAnimated();
        updateButtons();
        updateSidebarStyles();
    }

    // Reader render (guaranteed font size)
    private void renderReader() {
        if (contentFlow == null) return;

        contentFlow.getChildren().clear();

        String q = (txtFind == null) ? "" : safe(txtFind.getText(), "").trim();
        currentQuery = q;

        if (q.isBlank()) {
            contentFlow.getChildren().add(makeText(currentContent, false, false));
            if (lblFindInfo != null) lblFindInfo.setText("");
            return;
        }

        List<int[]> matches = findAllMatches(currentContent, q);
        if (matches.isEmpty()) {
            contentFlow.getChildren().add(makeText(currentContent, false, false));
            currentMatch = -1;
            if (lblFindInfo != null) lblFindInfo.setText("Aucun résultat");
            return;
        }

        if (currentMatch < 0) currentMatch = 0;
        if (currentMatch >= matches.size()) currentMatch = matches.size() - 1;

        int cursor = 0;
        for (int i = 0; i < matches.size(); i++) {
            int start = matches.get(i)[0];
            int end = matches.get(i)[1];

            if (start > cursor) contentFlow.getChildren().add(makeText(currentContent.substring(cursor, start), false, false));

            boolean active = (i == currentMatch);
            contentFlow.getChildren().add(makeText(currentContent.substring(start, end), true, active));

            cursor = end;
        }

        if (cursor < currentContent.length()) {
            contentFlow.getChildren().add(makeText(currentContent.substring(cursor), false, false));
        }

        if (lblFindInfo != null) lblFindInfo.setText((currentMatch + 1) + " / " + matches.size());
    }

    private List<int[]> findAllMatches(String text, String query) {
        List<int[]> out = new ArrayList<>();
        if (text == null || query == null) return out;

        String t = text.toLowerCase(Locale.ROOT);
        String q = query.toLowerCase(Locale.ROOT);

        int from = 0;
        while (from < t.length()) {
            int idx = t.indexOf(q, from);
            if (idx < 0) break;
            out.add(new int[]{idx, idx + q.length()});
            from = idx + q.length();
        }
        return out;
    }

    private Text makeText(String s, boolean highlight, boolean active) {
        Text t = new Text(s);
        t.getStyleClass().add("reader-text");

        // ✅ GUARANTEED font sizing (no CSS selector dependency)
        t.setStyle("-fx-font-size: " + fontSizePx + "px;");

        if (highlight) t.getStyleClass().add("reader-highlight");
        if (active) t.getStyleClass().add("reader-highlight-active");
        return t;
    }

    // Progress
    private void updateProgressAnimated() {
        if (progressBar == null) return;

        int total = Math.max(1, lessons.size());
        int done = (int) lessons.stream().filter(this::isCompleted).count();
        double target = done / (double) total;

        if (lblProgress != null) lblProgress.setText((int) Math.round(target * 100) + "%");
        if (lblProgressDetail != null) lblProgressDetail.setText(done + " / " + total + " terminées");

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(260),
                        new KeyValue(progressBar.progressProperty(), target, Interpolator.EASE_BOTH))
        );
        tl.play();
    }

    private void updateButtons() {
        if (btnPrev != null) btnPrev.setDisable(index <= 0);
        if (btnNext != null) btnNext.setDisable(index >= lessons.size() - 1);
    }

    // Actions
    @FXML private void onPrev() { if (index > 0) { index--; render(true); } }
    @FXML private void onNext() { if (index < lessons.size() - 1) { index++; render(true); } }

    @FXML
    private void onToggleComplete() {
        if (lessons.isEmpty()) return;
        Lesson cur = lessons.get(index);
        if (cur == null) return;

        if (completedLessonIds.contains(cur.getId())) completedLessonIds.remove(cur.getId());
        else completedLessonIds.add(cur.getId());

        render(false);
    }

    @FXML
    private void onFindChanged() {
        currentMatch = -1;
        renderReader();
    }

    @FXML
    private void onFindNext() {
        if (currentQuery == null || currentQuery.isBlank()) { renderReader(); return; }
        List<int[]> matches = findAllMatches(currentContent, currentQuery);
        if (matches.isEmpty()) { renderReader(); return; }
        currentMatch = (currentMatch < 0) ? 0 : (currentMatch + 1) % matches.size();
        renderReader();
    }

    @FXML
    private void onFindPrev() {
        if (currentQuery == null || currentQuery.isBlank()) { renderReader(); return; }
        List<int[]> matches = findAllMatches(currentContent, currentQuery);
        if (matches.isEmpty()) { renderReader(); return; }
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

    // ✅ Focus Mode
    @FXML
    private void onToggleFocus() {
        focusMode = btnFocus != null && btnFocus.isSelected();

        if (splitPane == null || sidebarScroll == null) return;

        if (focusMode) {
            // remember last divider
            if (splitPane.getDividers().size() > 0) {
                lastDividerPos = splitPane.getDividers().get(0).getPosition();
            }
            sidebarScroll.setVisible(false);
            sidebarScroll.setManaged(false);

            // push divider to 0 (reader full width)
            splitPane.setDividerPositions(0.0);
        } else {
            sidebarScroll.setManaged(true);
            sidebarScroll.setVisible(true);

            // restore divider
            splitPane.setDividerPositions(lastDividerPos <= 0 ? 0.28 : lastDividerPos);
        }
    }

    @FXML
    private void onBack() {
        if (onBack != null) onBack.run();
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }

    // Animation helper
    private void fadeSwap(Node node, Runnable apply) {
        FadeTransition out = new FadeTransition(Duration.millis(140), node);
        out.setToValue(0);
        FadeTransition in = new FadeTransition(Duration.millis(160), node);
        in.setToValue(1);
        out.setOnFinished(e -> { apply.run(); in.play(); });
        out.play();
    }

    private int totalDurationMinutes() {
        int sum = 0;
        for (Lesson l : lessons) if (l != null) sum += Math.max(0, l.getDureeMinutes());
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
        for (int i = 0; i < full; i++) sb.append("★");
        while (sb.length() < 5) sb.append("☆");
        return sb.toString();
    }

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }

    private int clamp(int v, int min, int max) {
        if (v < min) return min;
        return Math.min(v, max);
    }
}