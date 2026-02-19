package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonViewController {

    // ── FXML fields ────────────────────────────────────────────────
    @FXML private Label lblEyebrow;   // "FORMATION NAME"
    @FXML private Label lblTitre;
    @FXML private Label lblMeta;
    @FXML private Label lblIndex;     // "03 / 12" pill
    @FXML private TextArea txtContenu;

    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    // ── State ───────────────────────────────────────────────────────
    private Runnable onBack;
    private List<Lesson> lessons = new ArrayList<>();
    private int index = 0;
    private Formation formation;

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    /**
     * Main entry point — full list with current index for Next/Prev navigation.
     */
    public void setLessons(List<Lesson> lessons, int index, Formation formation) {
        this.lessons   = (lessons == null) ? new ArrayList<>() : new ArrayList<>(lessons);
        this.index     = Math.max(0, Math.min(index, this.lessons.size() - 1));
        this.formation = formation;
        render();
    }

    /**
     * Fallback — single lesson, no navigation.
     */
    public void setData(Lesson lesson, Formation formation) {
        this.lessons = new ArrayList<>();
        if (lesson != null) this.lessons.add(lesson);
        this.index   = 0;
        this.formation = formation;
        render();
    }

    // ── Render ──────────────────────────────────────────────────────
    private void render() {
        if (lessons.isEmpty()) return;

        Lesson lesson = lessons.get(index);

        // Eyebrow: formation name in uppercase
        if (lblEyebrow != null) {
            String name = (formation != null)
                    ? formation.getTitre().toUpperCase()
                    : "FORMATION #" + lesson.getFormationId();
            lblEyebrow.setText(name);
        }

        // Title
        lblTitre.setText(safe(lesson.getTitre(), "(Sans titre)"));

        // Meta
        lblMeta.setText(
                "Ordre " + lesson.getOrdre()
                        + "   •   " + lesson.getDureeMinutes() + " min"
        );

        // Content
        txtContenu.setText(safe(lesson.getContenu(), ""));

        updateProgress();
        updateButtons();
    }

    private void updateProgress() {
        int total = Math.max(1, lessons.size());
        double p  = (index + 1) / (double) total;

        progressBar.setProgress(p);
        lblProgress.setText((int) Math.round(p * 100) + "%");

        if (lblIndex != null) {
            lblIndex.setText(String.format("%02d / %02d", index + 1, lessons.size()));
        }
    }

    private void updateButtons() {
        btnPrev.setDisable(index <= 0);
        btnNext.setDisable(index >= lessons.size() - 1);
    }

    // ── Navigation ──────────────────────────────────────────────────
    @FXML
    private void onPrev() {
        if (index > 0) { index--; render(); }
    }

    @FXML
    private void onNext() {
        if (index < lessons.size() - 1) { index++; render(); }
    }

    @FXML
    private void onBack() {
        if (onBack != null) onBack.run();
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }

    // ── Helper ──────────────────────────────────────────────────────
    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }
}