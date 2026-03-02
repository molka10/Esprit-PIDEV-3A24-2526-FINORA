package com.example.finora.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.finora.entities.Formation;
import com.example.finora.entities.Lesson;
import com.example.finora.services.GeminiService;
import com.example.finora.services.GeminiService.QuizQuestion;
import com.example.finora.services.QuizAiCommentService;
import com.example.finora.services.QuizFraudService;
import com.example.finora.services.QuizResultService;
import com.example.finora.utils.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizTakeController {

    @FXML
    private Label lblStatus;
    @FXML
    private Label lblProgress;
    @FXML
    private Label lblQuestion;

    @FXML
    private RadioButton rb1;
    @FXML
    private RadioButton rb2;
    @FXML
    private RadioButton rb3;
    @FXML
    private RadioButton rb4;

    @FXML
    private Button btnPrevious;
    @FXML
    private Button btnNext;
    @FXML
    private Button btnClose;

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label lblCheatHint;

    private Lesson lesson;
    private Formation formation;

    private final QuizFraudService fraudService = new QuizFraudService();
    private final List<QuizQuestion> questions = new ArrayList<>();
    private final Map<Integer, Integer> userAnswers = new HashMap<>();

    private int currentIndex = 0;
    private boolean quizReady = false;

    private final ToggleGroup group = new ToggleGroup();

    private final GeminiService geminiService = new GeminiService();
    private final QuizAiCommentService aiCommentService = new QuizAiCommentService();
    private final QuizResultService resultService;

    public QuizTakeController() {
        this.resultService = new QuizResultService();
    }

    private Stage stage;
    private boolean fullscreenEnforced = false;

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

        if (progressBar != null)
            progressBar.setProgress(0);
        if (lblCheatHint != null)
            lblCheatHint.setText("");

        Platform.runLater(() -> {
            if (lblQuestion.getScene() != null) {
                stage = (Stage) lblQuestion.getScene().getWindow();
                stage.setOnCloseRequest(e -> releaseFullscreen());
                stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (quizReady && !newVal) {
                        fraudService.registerFocusLoss();
                        warnIfCheating();
                    }
                });
                stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
                    if (!quizReady)
                        return;
                    if (fullscreenEnforced && oldVal && !newVal) {
                        fraudService.registerExitFullscreen();
                        warnIfCheating();
                    }
                });
            }
        });
    }

    public void setData(Lesson lesson, Formation formation) {
        this.lesson = lesson;
        this.formation = formation;
        generateQuiz();
    }

    private void generateQuiz() {
        quizReady = false;
        fullscreenEnforced = false;
        btnNext.setDisable(true);
        btnPrevious.setDisable(true);
        group.selectToggle(null);
        userAnswers.clear();
        questions.clear();

        if (lblCheatHint != null)
            lblCheatHint.setText("");
        if (progressBar != null)
            progressBar.setProgress(0);

        lblStatus.setText("⏳ Generating quiz...");
        lblQuestion.setText("");
        lblProgress.setText("");

        Thread thread = new Thread(() -> {
            try {
                List<QuizQuestion> generated = geminiService.generateQuiz(
                        lesson.getTitre(),
                        lesson.getContenu());

                Platform.runLater(() -> {
                    for (QuizQuestion q : generated) {
                        if (q != null && q.options != null && q.options.size() >= 2) {
                            questions.add(q);
                        }
                    }
                    if (questions.isEmpty()) {
                        lblStatus.setText("❌ Failed to generate valid questions.");
                        return;
                    }
                    currentIndex = 0;
                    quizReady = true;
                    fraudService.startQuiz();
                    fraudService.startQuestion(currentIndex);
                    if (stage != null) {
                        stage.setFullScreenExitHint("");
                        stage.setFullScreen(true);
                        fullscreenEnforced = true;
                    }
                    showQuestion();
                    lblStatus.setText("📝 Answer the questions");
                    btnNext.setDisable(false);
                    warnIfCheating();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("❌ Error: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void showQuestion() {
        if (!quizReady || questions.isEmpty())
            return;
        QuizQuestion q = questions.get(currentIndex);
        lblQuestion.setText(q.question == null ? "" : q.question);
        lblProgress.setText("Question " + (currentIndex + 1) + " / " + questions.size());
        updateProgressBar();

        List<RadioButton> buttons = List.of(rb1, rb2, rb3, rb4);
        group.selectToggle(null);

        for (int i = 0; i < 4; i++) {
            RadioButton rb = buttons.get(i);
            if (i < q.options.size()) {
                rb.setText(q.options.get(i));
                rb.setVisible(true);
                rb.setManaged(true);
                rb.setDisable(false);
            } else {
                rb.setText("");
                rb.setVisible(false);
                rb.setManaged(false);
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
        warnIfCheating();
    }

    @FXML
    private void onNext() {
        if (!quizReady)
            return;
        Toggle selected = group.getSelectedToggle();
        if (selected == null) {
            showAlert("Attention", "Please choose an answer.");
            return;
        }
        int selectedIndex = (int) selected.getUserData();
        userAnswers.put(currentIndex, selectedIndex);
        fraudService.finishQuestion(currentIndex);
        warnIfCheating();
        if (currentIndex == questions.size() - 1) {
            calculateResult();
            return;
        }
        currentIndex++;
        showQuestion();
    }

    @FXML
    private void onPrevious() {
        if (!quizReady)
            return;
        if (currentIndex > 0) {
            currentIndex--;
            showQuestion();
        }
    }

    private void calculateResult() {
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer answer = userAnswers.get(i);
            int correctIdx = questions.get(i).correctIndex;
            if (answer != null && answer == correctIdx)
                correct++;
        }
        int percent = (correct * 100) / Math.max(1, questions.size());
        boolean suspicious = fraudService.isFraudSuspicious();
        int shownPercent = suspicious ? 0 : percent;

        final Stage quizStage = (Stage) lblQuestion.getScene().getWindow();
        Platform.runLater(() -> {
            try {
                fullscreenEnforced = false;
                quizStage.setFullScreen(false);
            } catch (Exception ignored) {
            }
        });

        Stage resultStage = new Stage();
        resultStage.setTitle("Quiz Result");
        resultStage.initOwner(quizStage);
        resultStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        resultStage.setResizable(false);

        VBox root = new VBox(22);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("result-card");
        root.setStyle("-fx-background-color: #f8fafc;");

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 650, suspicious ? 640 : 440);

        Label title = new Label("📊 Quiz Result");
        title.getStyleClass().add("item-title");

        Label bigScore = new Label(shownPercent + "%");
        bigScore.getStyleClass().add("stat-number");

        ProgressBar pb = new ProgressBar(shownPercent / 100.0);
        pb.setPrefWidth(450);
        pb.getStyleClass().add("viewer-progress-bar");

        Label details = new Label("Correct answers: " + correct + " / " + questions.size());
        details.getStyleClass().add("viewer-meta");

        root.getChildren().addAll(title, bigScore, pb, details);

        if (suspicious) {
            VBox fraudBox = new VBox(10);
            fraudBox.setAlignment(Pos.CENTER);
            fraudBox.getStyleClass().add("failed-card");
            fraudBox.setStyle(
                    "-fx-background-color: #fff1f2; -fx-padding: 20; -fx-border-color: #fecaca; -fx-background-radius: 15; -fx-border-radius: 15;");

            Label warnIcon = new Label("🚫 Quiz Invalidated");
            warnIcon.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-font-size: 18px;");

            Label warnMsg = new Label("Suspicious behavior detected....");
            warnMsg.setStyle("-fx-text-fill: #991b1b;");

            Label quote = new Label("من غشنا فليس منا");
            quote.setStyle(
                    "-fx-text-fill: #b45309; -fx-font-size: 26px; -fx-font-weight: 900; -fx-font-family: 'Georgia';");

            fraudBox.getChildren().addAll(warnIcon, warnMsg, quote);
            root.getChildren().add(fraudBox);

            VBox aiBox = new VBox(8);
            aiBox.setAlignment(Pos.CENTER_LEFT);
            aiBox.setStyle(
                    "-fx-background-color: #f5f3ff; -fx-padding: 15; -fx-background-radius: 12; -fx-border-color: #ddd6fe; -fx-border-radius: 12;");
            aiBox.setMaxWidth(500);

            Label aiLabel = new Label("AI says:");
            aiLabel.setStyle("-fx-text-fill: #6d28d9; -fx-font-weight: bold;");

            Label aiComment = new Label("⏳ Analyzing behavior...");
            aiComment.setWrapText(true);
            aiComment.setStyle("-fx-text-fill: #4c1d95;");

            aiBox.getChildren().addAll(aiLabel, aiComment);
            root.getChildren().add(aiBox);

            // Fetch AI commentary in background
            String report = fraudService.getFraudReport() + " | Score: " + percent + "%";
            new Thread(() -> {
                String comment = aiCommentService.analyzeFraud(report);
                Platform.runLater(() -> aiComment.setText(comment));
            }).start();

        } else {
            String username = Session.getCurrentUser().getUsername();

            resultService.save(
                    username,
                    lesson.getId(),
                    lesson.getTitre(),
                    formation == null ? "" : formation.getTitre(),
                    shownPercent
            );
        }

        Button btnDismiss = new Button("Close ✖");
        btnDismiss.getStyleClass().add("btn-primary");
        btnDismiss.setOnAction(e -> resultStage.close());
        root.getChildren().add(btnDismiss);

        resultStage.setOnHidden(e -> Platform.runLater(quizStage::close));
        scene.getStylesheets().add(getClass().getResource("/formation/style.css").toExternalForm());
        resultStage.setScene(scene);
        resultStage.show();
    }

    @FXML
    private void onClose() {
        releaseFullscreen();
        Stage s = (Stage) lblQuestion.getScene().getWindow();
        s.close();
    }

    private void updateProgressBar() {
        if (progressBar == null)
            return;
        int total = Math.max(1, questions.size());
        double p = (currentIndex + 1) / (double) total;
        progressBar.setProgress(p);
    }

    private void releaseFullscreen() {
        try {
            if (stage != null && stage.isFullScreen()) {
                fullscreenEnforced = false;
                stage.setFullScreen(false);
            }
        } catch (Exception ignored) {
        }
    }

    private void warnIfCheating() {
        if (!quizReady)
            return;
        int score = fraudService.getFraudScore();
        if (lblCheatHint != null) {
            if (score >= 60)
                lblCheatHint.setText("🚫 Suspicious behavior detected");
            else if (score >= 40)
                lblCheatHint.setText("⚠ Please stay focused");
            else
                lblCheatHint.setText("");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}