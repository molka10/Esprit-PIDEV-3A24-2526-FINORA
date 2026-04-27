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
import com.example.finora.entities.User;
import com.example.finora.services.CertificateService;
import com.example.finora.services.GeminiService;
import com.example.finora.services.GeminiService.QuizQuestion;
import com.example.finora.services.QuizAiCommentService;
import com.example.finora.services.QuizFraudService;
import com.example.finora.services.QuizResultService;
import com.example.finora.utils.Session;

import java.io.File;
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

    // optional UI
    @FXML private ProgressBar progressBar;
    @FXML private Label lblCheatHint;

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

    // certificate generator (iText)
    private final CertificateService certificateService = new CertificateService();

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

        if (progressBar != null) progressBar.setProgress(0);
        if (lblCheatHint != null) lblCheatHint.setText("");

        Platform.runLater(() -> {
            stage = (Stage) lblQuestion.getScene().getWindow();

            // If user closes window with X, still release fullscreen
            stage.setOnCloseRequest(e -> releaseFullscreen());

            // Focus loss detection (only during quiz)
            stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (quizReady && !newVal) {
                    fraudService.registerFocusLoss();
                    warnIfCheating();
                }
            });

            // Fullscreen exit detection (only if we enforced fullscreen)
            stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
                if (!quizReady) return;
                if (fullscreenEnforced && oldVal && !newVal) {
                    fraudService.registerExitFullscreen();
                    warnIfCheating();
                }
            });
        });
    }

    public void setData(Lesson lesson, Formation formation) {
        this.lesson = lesson;
        this.formation = formation;
        generateQuiz();
    }

    private String getCurrentUserName() {
        try {
            User u = Session.getCurrentUser();
            if (u != null && u.getUsername() != null && !u.getUsername().isBlank()) {
                return u.getUsername().trim();
            }
        } catch (Exception ignored) {}
        return "Utilisateur";
    }

    private void generateQuiz() {

        quizReady = false;
        fullscreenEnforced = false;

        btnNext.setDisable(true);
        btnPrevious.setDisable(true);
        group.selectToggle(null);

        userAnswers.clear();
        questions.clear();

        if (lblCheatHint != null) lblCheatHint.setText("");
        if (progressBar != null) progressBar.setProgress(0);

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

        if (!quizReady || questions.isEmpty()) return;

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

        if (!quizReady) return;

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
        if (!quizReady) return;
        if (currentIndex > 0) {
            currentIndex--;
            showQuestion();
        }
    }

    private void calculateResult() {

        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer answer = userAnswers.get(i);
            if (answer != null && answer == questions.get(i).correctIndex) correct++;
        }

        int percent = (correct * 100) / Math.max(1, questions.size());
        boolean suspicious = fraudService.isFraudSuspicious();
        int shownPercent = suspicious ? 0 : percent;

        final String currentUserName = getCurrentUserName();

        // get quiz window reference once
        final Stage quizStage = (Stage) lblQuestion.getScene().getWindow();

        // release fullscreen BEFORE opening result
        Platform.runLater(() -> {
            try {
                fullscreenEnforced = false;
                quizStage.setFullScreen(false);
            } catch (Exception ignored) {}
        });

        // Build Result Stage
        Stage resultStage = new Stage();
        resultStage.setTitle("Quiz Result");
        resultStage.initOwner(quizStage);
        resultStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        resultStage.setResizable(false);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setStyle("""
            -fx-padding: 35;
            -fx-background-color: rgba(255,255,255,0.25);
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-color: rgba(255,255,255,0.45);
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 25, 0.35, 0, 10);
        """);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 640, suspicious ? 560 : 460);
        scene.setFill(javafx.scene.paint.Color.web("#00000000"));

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ignored) {}

        Label title = new Label("📊 Quiz Result");
        title.setStyle("""
            -fx-font-size: 20px;
            -fx-font-weight: 900;
            -fx-text-fill: #1E0A3C;
        """);

        Label bigScore = new Label(shownPercent + "%");
        bigScore.setStyle("""
            -fx-font-size: 64px;
            -fx-font-weight: 900;
            -fx-text-fill: linear-gradient(to right, #7C3AED, #A78BFA);
        """);

        ProgressBar pb = new ProgressBar(shownPercent / 100.0);
        pb.setPrefWidth(420);
        pb.setStyle("""
            -fx-accent: #7C3AED;
            -fx-background-radius: 999;
            -fx-border-radius: 999;
        """);

        Label details = new Label("Correct answers: " + correct + " / " + questions.size());
        details.setStyle("-fx-font-size: 14px; -fx-text-fill: #444;");

        root.getChildren().addAll(title, bigScore, pb, details);

        if (suspicious) {

            VBox warningBox = new VBox(10);
            warningBox.setAlignment(Pos.CENTER);
            warningBox.setStyle("""
                -fx-background-color: rgba(220,38,38,0.10);
                -fx-padding: 18;
                -fx-background-radius: 16;
                -fx-border-radius: 16;
                -fx-border-color: rgba(220,38,38,0.35);
            """);

            Label warningTitle = new Label("🚫 Quiz Invalidated");
            warningTitle.setStyle("""
                -fx-font-size: 15px;
                -fx-font-weight: 900;
                -fx-text-fill: #B91C1C;
            """);

            Label explanation = new Label("Suspicious behavior detected.\nPlease respect quiz rules and remain focused.");
            explanation.setWrapText(true);
            explanation.setStyle("-fx-text-fill: #7F1D1D; -fx-font-size: 13px;");

            Label arabicPhrase = new Label("من غشنا فليس منا");
            arabicPhrase.setAlignment(Pos.CENTER);
            arabicPhrase.setMaxWidth(Double.MAX_VALUE);
            arabicPhrase.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            arabicPhrase.setStyle("""
                -fx-font-size: 22px;
                -fx-font-weight: 900;
                -fx-text-fill: linear-gradient(to right, #a55c06, #dcb211);
                -fx-text-alignment: center;
            """);

            warningBox.getChildren().addAll(warningTitle, explanation, arabicPhrase);
            root.getChildren().add(warningBox);

            Label aiLabel = new Label("AI analysis loading...");
            aiLabel.setWrapText(true);
            aiLabel.setMaxWidth(520);
            aiLabel.setMinHeight(Region.USE_PREF_SIZE);
            aiLabel.setStyle("""
                -fx-background-color: rgba(124,58,237,0.12);
                -fx-padding: 18;
                -fx-background-radius: 16;
                -fx-text-fill: #4C1D95;
                -fx-font-size: 13px;
                -fx-line-spacing: 2px;
            """);

            ScrollPane aiScroll = new ScrollPane(aiLabel);
            aiScroll.setFitToWidth(true);
            aiScroll.setPrefViewportHeight(140);
            aiScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            aiScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            aiScroll.setStyle("""
                -fx-background-color: transparent;
                -fx-background: transparent;
                -fx-padding: 0;
                -fx-border-color: rgba(124,58,237,0.20);
                -fx-border-radius: 16;
                -fx-background-radius: 16;
            """);

            root.getChildren().add(aiScroll);

            new Thread(() -> {
                String aiText;
                try {
                    QuizAiCommentService ai = new QuizAiCommentService();
                    aiText = ai.analyzeFraud(fraudService.getFraudReport());
                } catch (Exception e) {
                    aiText = "AI unavailable.";
                }
                final String finalAiText = aiText;
                Platform.runLater(() -> aiLabel.setText("AI says:\n" + finalAiText));
            }).start();

        } else {

            VBox successBox = new VBox(8);
            successBox.setAlignment(Pos.CENTER);
            successBox.setStyle("""
                -fx-background-color: rgba(34,197,94,0.10);
                -fx-padding: 18;
                -fx-background-radius: 16;
            """);

            Label successText = new Label(
                    shownPercent >= 80
                            ? "Excellent work! Certificate unlocked."
                            : "Quiz completed successfully."
            );
            successText.setStyle("""
                -fx-font-size: 14px;
                -fx-font-weight: 800;
                -fx-text-fill: #166534;
            """);

            successBox.getChildren().add(successText);
            root.getChildren().add(successBox);

            // ✅ Save only if clean (USE REAL USERNAME)
            resultService.save(
                    currentUserName,
                    lesson.getId(),
                    lesson.getTitre(),
                    formation == null ? "" : formation.getTitre(),
                    shownPercent
            );

            // ✅ CERTIFICATE BUTTON (only if >= 80)
            if (shownPercent >= 80) {

                Button btnCert = new Button("🎓 Générer certificat");
                btnCert.setStyle("""
                    -fx-background-color: #7C3AED;
                    -fx-text-fill: white;
                    -fx-font-weight: 900;
                    -fx-background-radius: 12;
                    -fx-padding: 10 18;
                """);

                btnCert.setOnAction(ev -> {
                    btnCert.setDisable(true);
                    btnCert.setText("⏳ Génération...");

                    new Thread(() -> {
                        try {
                            String home = System.getProperty("user.home");
                            String folder = home + File.separator + "FINORA-Certificates";
                            new File(folder).mkdirs();

                            // safe filename (remove forbidden chars)
                            String safeUser = currentUserName.replaceAll("[\\\\/:*?\"<>|]", "_");

                            String path = folder + File.separator
                                    + "Certificate_" + safeUser
                                    + "_Lesson" + lesson.getId()
                                    + "_Score" + shownPercent + ".pdf";

                            // ✅ USE REAL USERNAME IN PDF
                            certificateService.generateCertificate(
                                    currentUserName,
                                    lesson.getTitre(),
                                    formation == null ? "" : formation.getTitre(),
                                    shownPercent,
                                    path
                            );

                            File pdf = new File(path);

                            Platform.runLater(() -> {
                                try {
                                    // If you have a static openFile(File) helper in CertificateService:
                                    CertificateService.openFile(pdf);
                                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                                    a.setTitle("Certificat");
                                    a.setHeaderText("✅ Certificat généré");
                                    a.setContentText("Le certificat a été généré et ouvert.\n\n" + pdf.getAbsolutePath());
                                    a.show();
                                } catch (Exception ex) {
                                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                                    a.setTitle("Certificat");
                                    a.setHeaderText("✅ Certificat généré");
                                    a.setContentText("PDF généré.\nOuvre-le manuellement:\n\n" + pdf.getAbsolutePath());
                                    a.show();
                                }
                                btnCert.setDisable(false);
                                btnCert.setText("🎓 Générer certificat");
                            });

                        } catch (Exception ex) {
                            Platform.runLater(() -> {
                                Alert a = new Alert(Alert.AlertType.ERROR);
                                a.setTitle("Certificat");
                                a.setHeaderText("❌ Erreur génération certificat");
                                a.setContentText(ex.getMessage());
                                a.show();
                                btnCert.setDisable(false);
                                btnCert.setText("🎓 Générer certificat");
                            });
                        }
                    }).start();
                });

                root.getChildren().add(btnCert);
            }
        }

        Button btnDismiss = new Button("Close ✖");
        btnDismiss.setStyle("""
            -fx-background-color: #7C3AED;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 12;
            -fx-padding: 10 18;
        """);

        btnDismiss.setOnAction(e -> resultStage.close());
        root.getChildren().add(btnDismiss);

        resultStage.setOnCloseRequest(e -> Platform.runLater(quizStage::close));
        resultStage.setOnHidden(e -> Platform.runLater(quizStage::close));

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
        if (progressBar == null) return;
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
        } catch (Exception ignored) {}
    }

    private void warnIfCheating() {
        if (!quizReady) return;

        int score = fraudService.getFraudScore();

        if (lblCheatHint != null) {
            if (score >= 60) lblCheatHint.setText("🚫 Suspicious behavior detected (quiz may be invalid)");
            else if (score >= 40) lblCheatHint.setText("⚠ Please stay focused + keep fullscreen");
            else lblCheatHint.setText("");
        }

        if (score >= 60) lblStatus.setText("🚫 Suspicious behavior detected. Quiz may be invalid.");
        else if (score >= 40) lblStatus.setText("⚠ Stay focused and keep fullscreen.");
        else lblStatus.setText("📝 Answer the questions");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}