package tn.finora.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;
import tn.finora.services.CertificateService;
import tn.finora.services.GeminiService;
import tn.finora.services.GeminiService.QuizQuestion;
import tn.finora.services.QuizResultService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QuizTakeController {

    @FXML private VBox mainContainer;
    @FXML private Label lblStatus;
    @FXML private VBox quizArea;
    @FXML private Button btnSubmit;
    @FXML private Button btnClose;

    private Lesson lesson;
    private Formation formation;

    private List<QuizQuestion> questions = new ArrayList<>();
    private final List<ToggleGroup> toggleGroups = new ArrayList<>();

    private final GeminiService geminiService     = new GeminiService();
    private final CertificateService certService  = new CertificateService();
    private final QuizResultService resultService = new QuizResultService();

    public void setData(Lesson lesson, Formation formation) {
        this.lesson = lesson;
        this.formation = formation;
        startQuizGeneration();
    }

    private void startQuizGeneration() {
        lblStatus.setText("⏳ Génération du quiz en cours...");
        lblStatus.setStyle("-fx-text-fill: #7C3AED; -fx-font-size: 14px; -fx-font-weight: 700;");
        btnSubmit.setDisable(true);
        quizArea.getChildren().clear();

        Thread thread = new Thread(() -> {
            try {
                List<QuizQuestion> generated = geminiService.generateQuiz(
                        lesson.getTitre(), lesson.getContenu());

                Platform.runLater(() -> {
                    this.questions = generated;
                    renderQuestions();
                    lblStatus.setText("📝 Répondez aux " + questions.size() + " questions :");
                    lblStatus.setStyle("-fx-text-fill: #1E0A3C; -fx-font-size: 14px; -fx-font-weight: 700;");
                    btnSubmit.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("❌ Erreur: " + e.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #B91C1C; -fx-font-size: 13px;");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void renderQuestions() {
        quizArea.getChildren().clear();
        toggleGroups.clear();

        for (int i = 0; i < questions.size(); i++) {
            quizArea.getChildren().add(buildQuestionBox(i + 1, questions.get(i)));
        }
    }

    private VBox buildQuestionBox(int number, QuizQuestion q) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(18, 20, 18, 20));
        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-color: #EDE9FE;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(109,40,217,0.07), 12, 0, 0, 3);"
        );

        Label questionLabel = new Label("Q" + number + ".  " + q.question);
        questionLabel.setWrapText(true);
        questionLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-family: 'Georgia', serif;" +
                        "-fx-text-fill: #1E0A3C;"
        );

        ToggleGroup group = new ToggleGroup();
        toggleGroups.add(group);

        VBox optionsBox = new VBox(8);
        optionsBox.setPadding(new Insets(6, 0, 0, 16));

        String[] letters = {"A", "B", "C", "D"};
        for (int i = 0; i < q.options.size(); i++) {
            RadioButton rb = new RadioButton(letters[i] + ".  " + q.options.get(i));
            rb.setToggleGroup(group);
            rb.setUserData(i);
            rb.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
                            "-fx-text-fill: #374151;" +
                            "-fx-cursor: hand;"
            );
            optionsBox.getChildren().add(rb);
        }

        box.getChildren().addAll(questionLabel, optionsBox);
        return box;
    }

    @FXML
    private void onSubmit() {
        for (int i = 0; i < toggleGroups.size(); i++) {
            if (toggleGroups.get(i).getSelectedToggle() == null) {
                showAlert("❗ Attention", "Veuillez répondre à toutes les questions avant de soumettre.");
                return;
            }
        }

        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            int chosen = (int) toggleGroups.get(i).getSelectedToggle().getUserData();
            if (chosen == questions.get(i).correctIndex) correct++;
        }

        int percent = (correct * 100) / questions.size();
        showResult(correct, questions.size(), percent);
    }

    private void showResult(int correct, int total, int percent) {
        quizArea.getChildren().clear();
        btnSubmit.setDisable(true);

        boolean passed = percent >= 80;

        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(40));

        Label scoreCircle = new Label(percent + "%");
        scoreCircle.setStyle(
                "-fx-background-color: " + (passed ? "#7C3AED" : "#EF4444") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 36px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-family: 'Georgia', serif;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-min-width: 130px;" +
                        "-fx-min-height: 130px;" +
                        "-fx-alignment: center;"
        );

        Label msgLabel = new Label(passed
                ? "🎉 Félicitations ! Vous avez réussi !"
                : "😔 Pas tout à fait... Réessayez après avoir relu la leçon.");
        msgLabel.setWrapText(true);
        msgLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-family: 'Georgia', serif;" +
                        "-fx-text-fill: " + (passed ? "#1E0A3C" : "#B91C1C") + ";" +
                        "-fx-text-alignment: center;"
        );

        Label detailLabel = new Label(correct + " / " + total + " réponses correctes");
        detailLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #6B5A8A;" +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );

        resultBox.getChildren().addAll(scoreCircle, msgLabel, detailLabel);

        // ✅ Save one result (don’t save twice!)
        String formationTitle = (formation != null) ? formation.getTitre() : "Formation inconnue";
        resultService.save("Étudiant", lesson.getId(), lesson.getTitre(), formationTitle, percent);

        if (passed) {
            lblStatus.setText("✅ Score ≥ 80% — Téléchargez votre certificat !");
            lblStatus.setStyle("-fx-text-fill: #065F46; -fx-font-size: 13px; -fx-font-weight: 700;");

            Button certBtn = new Button("🏆  Télécharger mon Certificat PDF");
            certBtn.setStyle(
                    "-fx-background-color: #7C3AED;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: 900;" +
                            "-fx-background-radius: 12px;" +
                            "-fx-padding: 14 28 14 28;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(124,58,237,0.40), 14, 0, 0, 4);"
            );
            certBtn.setOnAction(e -> generateCertificate(percent));
            resultBox.getChildren().add(certBtn);

        } else {
            lblStatus.setText("Score: " + percent + "% — Minimum requis: 80%");
            lblStatus.setStyle("-fx-text-fill: #B91C1C; -fx-font-size: 13px; -fx-font-weight: 700;");

            Button retryBtn = new Button("🔄  Réessayer le Quiz");
            retryBtn.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #7C3AED;" +
                            "-fx-border-color: #C4B5FD;" +
                            "-fx-border-width: 1.5px;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-padding: 10 22 10 22;" +
                            "-fx-cursor: hand;"
            );
            retryBtn.setOnAction(e -> startQuizGeneration());
            resultBox.getChildren().add(retryBtn);
        }

        quizArea.getChildren().add(resultBox);
    }

    private void generateCertificate(int score) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Certificat");
        dialog.setHeaderText("Entrez votre nom complet");
        dialog.setContentText("Nom :");

        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                showAlert("Erreur", "Veuillez entrer votre nom.");
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer le certificat");
            fc.setInitialFileName("certificat_" + name.trim().replace(" ", "_") + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fc.showSaveDialog(mainContainer.getScene().getWindow());
            if (file == null) return;

            try {
                String formationTitle = (formation != null) ? formation.getTitre() : "Formation";
                certService.generateCertificate(name.trim(), lesson.getTitre(), formationTitle, score, file.getAbsolutePath());
                showAlert("✅ Certificat généré !", "Sauvegardé ici :\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                showAlert("❌ Erreur", "Impossible de générer le PDF : " + ex.getMessage());
            }
        });
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}