package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonViewController {

    @FXML private Label lblTitre;
    @FXML private Label lblMeta;
    @FXML private TextArea txtContenu;

    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    private Runnable onBack;

    private List<Lesson> lessons = new ArrayList<>();
    private int index = 0;

    private Formation formation; // optional (nice display)

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    /**
     * Lessons list + current index (for next/prev navigation)
     */
    public void setLessons(List<Lesson> lessons, int index, Formation formation) {
        this.lessons = (lessons == null) ? new ArrayList<>() : new ArrayList<>(lessons);
        this.index = Math.max(0, Math.min(index, this.lessons.size() - 1));
        this.formation = formation;

        render();
    }

    /**
     * Fallback if you only pass one lesson (no next/prev)
     */
    public void setData(Lesson lesson, Formation formation) {
        this.lessons = new ArrayList<>();
        if (lesson != null) this.lessons.add(lesson);
        this.index = 0;
        this.formation = formation;

        render();
    }

    private void render() {
        if (lessons.isEmpty()) return;

        Lesson lesson = lessons.get(index);

        lblTitre.setText(safe(lesson.getTitre(), "(Sans titre)"));

        String formationText = (formation == null)
                ? ("Formation #" + lesson.getFormationId())
                : formation.getTitre();

        lblMeta.setText(formationText
                + "   •   Ordre: " + lesson.getOrdre()
                + "   •   " + lesson.getDureeMinutes() + " min");

        txtContenu.setText(safe(lesson.getContenu(), ""));

        updateProgress();
        updateButtons();
    }

    private void updateProgress() {
        int total = Math.max(1, lessons.size());
        // progress = (index + 1) / total
        double p = (index + 1) / (double) total;

        progressBar.setProgress(p);

        int percent = (int) Math.round(p * 100);
        lblProgress.setText(percent + "% (" + (index + 1) + "/" + total + ")");
    }

    private void updateButtons() {
        btnPrev.setDisable(index <= 0);
        btnNext.setDisable(index >= lessons.size() - 1);
    }

    @FXML
    private void onPrev() {
        if (index > 0) {
            index--;
            render();
        }
    }

    @FXML
    private void onNext() {
        if (index < lessons.size() - 1) {
            index++;
            render();
        }
    }

    @FXML
    private void onBack() {
        if (onBack != null) onBack.run();
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }
}
