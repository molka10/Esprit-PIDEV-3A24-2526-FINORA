package com.example.finora.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.finora.entities.Formation;
import com.example.finora.entities.Lesson;
import com.example.finora.services.GeminiService;
import com.example.finora.services.GeminiService.QuizQuestion;
import com.example.finora.services.QuizResultService;
import com.example.finora.services.QuizFraudService;

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

    private final QuizFraudService fraudService = new QuizFraudService();

    private final List<QuizQuestion> questions = new ArrayList<>();
    private final Map<Integer, Integer> userAnswers = new HashMap<>();

    private int currentIndex = 0;
    private boolean quizReady = false;

    private final ToggleGroup group = new ToggleGroup();

    private final GeminiService geminiService = new GeminiService();
    private final QuizResultService resultService = new QuizResultService();

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

        // 🔹 Detect window focus loss
        Platform.runLater(() -> {
            Stage stage = (Stage) lblQuestion.getScene().getWindow();
            stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    fraudService.registerFocusLoss();
                }
            });
        });
    }

    public void setData(Lesson lesson, Formation formation) {
        this.lesson = lesson;
        this.formation = formation;
        generateQuiz();
    }

    // ================================
    // QUIZ GENERATION
    // ================================

    private void generateQuiz() {

        quizReady = false;
        btnNext.setDisable(true);
        btnPrevious.setDisable(true);
        group.selectToggle(null);

        lblStatus.setText("⏳ Generating quiz...");
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

                    for (QuizQuestion q : generated) {
                        if (q.options != null && q.options.size() >= 2) {
                            questions.add(q);
                        }
                    }

                    if (questions.isEmpty()) {
                        lblStatus.setText("❌ Failed to generate valid questions.");
                        return;
                    }

                    currentIndex = 0;
                    userAnswers.clear();
                    quizReady = true;

                    // 🔹 Start fraud tracking AFTER quiz is ready
                    fraudService.startQuiz();
                    fraudService.startQuestion(currentIndex);

                    showQuestion();

                    lblStatus.setText("📝 Answer the questions");
                    btnNext.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        lblStatus.setText("❌ Error: " + e.getMessage()));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    // ================================
    // DISPLAY QUESTION
    // ================================

    private void showQuestion() {

        if (!quizReady || questions.isEmpty()) return;

        QuizQuestion q = questions.get(currentIndex);

        lblQuestion.setText(q.question);
        lblProgress.setText("Question " + (currentIndex + 1) + "/" + questions.size());

        List<RadioButton> buttons = List.of(rb1, rb2, rb3, rb4);

        group.selectToggle(null);

        for (int i = 0; i < 4; i++) {
            RadioButton rb = buttons.get(i);

            if (i < q.options.size()) {
                rb.setText(q.options.get(i));
                rb.setVisible(true);
                rb.setDisable(false);
            } else {
                rb.setText("");
                rb.setVisible(false);
                rb.setDisable(true);
            }

            rb.setSelected(false);
        }

        if (userAnswers.containsKey(currentIndex)) {
            int saved = userAnswers.get(currentIndex);
            if (saved >= 0 && saved < q.options.size()) {
                buttons.get(saved).setSelected(true);
            }
        }

        btnPrevious.setDisable(currentIndex == 0);
        btnNext.setText(currentIndex == questions.size() - 1 ? "Finish ✅" : "Next ➡");

        fraudService.startQuestion(currentIndex);
    }

    // ================================
    // NEXT
    // ================================

    @FXML
    private void onNext() {

        if (!quizReady) return;

        Toggle selected = group.getSelectedToggle();

        if (selected == null) {
            showAlert("Attention", "Please choose an answer.");
            return;
        }

        int selectedIndex = (int) selected.getUserData();
        userAnswers.put(currentIndex, selectedIndex);

        // 🔹 record timing
        fraudService.finishQuestion(currentIndex);

        if (currentIndex == questions.size() - 1) {
            calculateResult();
            return;
        }

        currentIndex++;
        showQuestion();
    }

    // ================================
    // RESULT
    // ================================

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

        boolean suspicious = fraudService.isFraudSuspicious();

        // 🔒 STRICT MODE: auto-fail if suspicious
        if (suspicious) {
            percent = 0;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Quiz Result");

        VBox box = new VBox(15);
        box.setStyle("-fx-padding: 30; -fx-alignment: center;");

        Label lblScore = new Label(percent + "%");
        lblScore.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #7C3AED;");

        Label lblDetails = new Label("Correct answers: " + correct + " / " + questions.size());

        box.getChildren().addAll(lblScore, lblDetails);

        // 🔒 Fraud handling
        if (suspicious) {

            Label fraudLabel = new Label("🚫 Quiz invalid due to suspicious activity.");
            fraudLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

            box.getChildren().add(fraudLabel);

        } else {

            Label cleanLabel = new Label("✅ No suspicious activity detected.");
            cleanLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

            box.getChildren().add(cleanLabel);

            // Certificate ONLY if clean AND >=80%
            if (percent >= 80) {
                Button btnCertificate = new Button("🏆 Download Certificate");
                btnCertificate.setStyle("-fx-background-color:#7C3AED; -fx-text-fill:white;");
                box.getChildren().add(btnCertificate);
            }
        }

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();

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
    @FXML
    private void onPrevious() {

        if (!quizReady) return;

        if (currentIndex > 0) {
            currentIndex--;
            showQuestion();
        }
    }
}
//

