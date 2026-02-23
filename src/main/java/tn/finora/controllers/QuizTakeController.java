package tn.finora.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

    private boolean quizReady = false;

    @FXML
    public void initialize() {

        rb1.setToggleGroup(group);
        rb2.setToggleGroup(group);
        rb3.setToggleGroup(group);
        rb4.setToggleGroup(group);

        rb1.setUserData(0);
        rb2.setUserData(1);
        rb3.setUserData(2);
        rb4.setUserData(3);

        btnPrevious.setDisable(true);
        btnNext.setDisable(true);
    }

    public void setData(Lesson lesson, Formation formation) {
        this.lesson = lesson;
        this.formation = formation;
        generateQuiz();
    }

    // ================= QUIZ GENERATION =================

    private void generateQuiz() {

        quizReady = false;
        btnNext.setDisable(true);
        btnPrevious.setDisable(true);
        group.selectToggle(null);

        lblStatus.setText("⏳ Génération du quiz...");
        lblQuestion.setText("");
        lblProgress.setText("");

        Thread thread = new Thread(() -> {
            try {

                List<QuizQuestion> generated =
                        geminiService.generateQuiz(
                                lesson.getTitre(),
                                lesson.getContenu());

                Platform.runLater(() -> {

                    questions.clear();
                    questions.addAll(generated);

                    currentIndex = 0;
                    userAnswers.clear();
                    quizReady = true;

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

    // ================= DISPLAY QUESTION =================

    private void showQuestion() {

        if (!quizReady || questions.isEmpty()) return;

        currentIndex = Math.max(0, Math.min(currentIndex, questions.size() - 1));

        QuizQuestion q = questions.get(currentIndex);

        lblQuestion.setText(q.question);
        lblProgress.setText("Question " + (currentIndex + 1) + "/" + questions.size());

        List<RadioButton> buttons = List.of(rb1, rb2, rb3, rb4);

        group.selectToggle(null);

        for (int i = 0; i < 4; i++) {

            RadioButton rb = buttons.get(i);

            if (i < q.options.size()) {
                rb.setText(q.options.get(i));
                rb.setDisable(false);
            } else {
                rb.setText("Option indisponible");
                rb.setDisable(true);
            }

            rb.setSelected(false);
        }

        // Restore saved answer
        if (userAnswers.containsKey(currentIndex)) {
            int saved = userAnswers.get(currentIndex);
            if (saved >= 0 && saved < 4) {
                buttons.get(saved).setSelected(true);
            }
        }

        btnPrevious.setDisable(currentIndex == 0);
        btnNext.setText(currentIndex == questions.size() - 1 ? "Finish ✅" : "Next ➡");
    }

    // ================= NEXT =================

    @FXML
    private void onNext() {

        if (!quizReady) return;

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

    // ================= PREVIOUS =================

    @FXML
    private void onPrevious() {

        if (!quizReady) return;

        if (currentIndex > 0) {
            currentIndex--;
            showQuestion();
        }
    }

    // ================= RESULT =================

    private void calculateResult() {

        if (questions.isEmpty()) return;

        int correct = 0;

        for (int i = 0; i < questions.size(); i++) {
            Integer answer = userAnswers.get(i);
            if (answer != null &&
                    answer == questions.get(i).correctIndex) {
                correct++;
            }
        }

        int percent = (correct * 100) / questions.size();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Quiz Result");

        dialog.initOwner(lblQuestion.getScene().getWindow());

        VBox box = new VBox(15);
        box.setStyle("-fx-padding: 30; -fx-alignment: center;");

        Label lblTitle = new Label("🎉 Quiz Completed!");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label lblScore = new Label(percent + "%");
        lblScore.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #7C3AED;");

        Label lblDetails = new Label("Correct answers: " + correct + " / " + questions.size());
        lblDetails.setStyle("-fx-font-size: 14px;");

        Label lblMessage = new Label(getPerformanceMessage(percent));
        lblMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        Button btnCertificate = new Button("🏆 Download Certificate");
        btnCertificate.setStyle(
                "-fx-background-color: #7C3AED;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 8;"
        );

        btnCertificate.setOnAction(e ->
                System.out.println("Certificate button clicked ✅"));

        box.getChildren().addAll(lblTitle, lblScore, lblDetails, lblMessage, btnCertificate);

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();

        Stage stage = (Stage) lblQuestion.getScene().getWindow();
        stage.close();
    }

    // ================= UTIL =================

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

    private String getPerformanceMessage(int percent) {
        if (percent >= 90) return "🔥 Excellent performance!";
        if (percent >= 70) return "👏 Great job!";
        if (percent >= 50) return "🙂 Not bad, keep improving!";
        return "📚 Keep studying and try again!";
    }
}