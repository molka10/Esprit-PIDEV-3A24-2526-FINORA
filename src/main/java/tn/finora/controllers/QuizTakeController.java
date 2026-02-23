package tn.finora.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;
import tn.finora.services.GeminiService;
import tn.finora.services.GeminiService.QuizQuestion;
import tn.finora.services.QuizResultService;

import java.util.*;

public class QuizTakeController {

    @FXML private Label lblStatus;
    @FXML private Label lblProgress;
    @FXML private Label lblQuestion;

    @FXML private RadioButton rb1;
    @FXML private RadioButton rb2;
    @FXML private RadioButton rb3;
    @FXML private RadioButton rb4;

    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    @FXML private Button btnClose;

    private Lesson lesson;
    private Formation formation;

    private List<QuizQuestion> questions = new ArrayList<>();
    private int currentIndex = 0;

    private final ToggleGroup group = new ToggleGroup();
    private final Map<Integer, Integer> userAnswers = new HashMap<>();

    private final GeminiService geminiService = new GeminiService();
    private final QuizResultService resultService = new QuizResultService();

    @FXML
    public void initialize() {

        rb1.setToggleGroup(group);
        rb2.setToggleGroup(group);
        rb3.setToggleGroup(group);
        rb4.setToggleGroup(group);

        // VERY IMPORTANT: assign userData
        rb1.setUserData(0);
        rb2.setUserData(1);
        rb3.setUserData(2);
        rb4.setUserData(3);

        btnPrevious.setDisable(true);
    }

    public void setData(Lesson lesson, Formation formation) {
        this.lesson = lesson;
        this.formation = formation;
        generateQuiz();
    }

    private void generateQuiz() {
        lblStatus.setText("⏳ Génération du quiz...");
        btnNext.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                List<QuizQuestion> generated =
                        geminiService.generateQuiz(
                                lesson.getTitre(),
                                lesson.getContenu());

                Platform.runLater(() -> {
                    questions = generated;
                    currentIndex = 0;
                    showQuestion();
                    lblStatus.setText("📝 Répondez aux questions");
                    btnNext.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        lblStatus.setText("❌ Erreur: " + e.getMessage()));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void showQuestion() {

        if (questions.isEmpty()) return;

        QuizQuestion q = questions.get(currentIndex);

        lblQuestion.setText(q.question);
        lblProgress.setText("Question " + (currentIndex + 1) + "/" + questions.size());

        List<RadioButton> buttons = List.of(rb1, rb2, rb3, rb4);

        // Reset buttons cleanly
        group.selectToggle(null);

        for (RadioButton rb : buttons) {
            rb.setVisible(false);
            rb.setManaged(false);
        }

        // Show only available options
        for (int i = 0; i < q.options.size() && i < 4; i++) {
            RadioButton rb = buttons.get(i);
            rb.setText(q.options.get(i));
            rb.setVisible(true);
            rb.setManaged(true);
        }

        // Restore answer
        Integer saved = userAnswers.get(currentIndex);
        if (saved != null) {
            for (RadioButton rb : buttons) {
                if (rb.getUserData().equals(saved)) {
                    rb.setSelected(true);
                    break;
                }
            }
        }

        btnPrevious.setDisable(currentIndex == 0);

        if (currentIndex == questions.size() - 1) {
            btnNext.setText("Finish ✅");
        } else {
            btnNext.setText("Next ➡");
        }
    }

    @FXML
    private void onNext() {

        Toggle selected = group.getSelectedToggle();

        if (selected == null) {
            showAlert("Attention", "Veuillez choisir une réponse.");
            return;
        }

        int selectedIndex = (int) selected.getUserData();
        userAnswers.put(currentIndex, selectedIndex);

        if (currentIndex == questions.size() - 1) {
            calculateResult();
            return;
        }

        currentIndex++;
        showQuestion();
    }

    @FXML
    private void onPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            showQuestion();
        }
    }

    private void calculateResult() {

        int correct = 0;

        for (int i = 0; i < questions.size(); i++) {
            Integer answer = userAnswers.get(i);
            if (answer != null &&
                    answer == questions.get(i).correctIndex) {
                correct++;
            }
        }

        int percent = (correct * 100) / questions.size();

        String formationTitle =
                (formation != null) ? formation.getTitre() : "Formation";

        resultService.save(
                "Étudiant",
                lesson.getId(),
                lesson.getTitre(),
                formationTitle,
                percent
        );

        showAlert("Résultat",
                "Score: " + percent + "% (" + correct + "/" + questions.size() + ")");

        Stage stage = (Stage) lblQuestion.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) lblQuestion.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}