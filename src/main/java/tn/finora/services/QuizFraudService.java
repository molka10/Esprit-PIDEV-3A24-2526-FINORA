package tn.finora.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class QuizFraudService {

    private LocalDateTime quizStartTime;
    private LocalDateTime questionStartTime;

    private int focusLossCount = 0;
    private int suspiciousFastAnswers = 0;

    private final Map<Integer, Long> questionTimes = new HashMap<>();

    // Minimum seconds expected per question
    private static final int MIN_SECONDS_PER_QUESTION = 5;

    public void startQuiz() {
        quizStartTime = LocalDateTime.now();
    }

    public void startQuestion(int questionIndex) {
        questionStartTime = LocalDateTime.now();
    }

    public void finishQuestion(int questionIndex) {
        if (questionStartTime == null) return;

        long seconds = Duration.between(questionStartTime, LocalDateTime.now()).toSeconds();
        questionTimes.put(questionIndex, seconds);

        if (seconds < MIN_SECONDS_PER_QUESTION) {
            suspiciousFastAnswers++;
        }
    }

    public void registerFocusLoss() {
        focusLossCount++;
    }

    public int getFocusLossCount() {
        return focusLossCount;
    }

    public int getSuspiciousFastAnswers() {
        return suspiciousFastAnswers;
    }

    public boolean isFraudSuspicious() {

        // Rules
        if (focusLossCount >= 3) return true;
        if (suspiciousFastAnswers >= 3) return true;

        return false;
    }

    public String getFraudReport() {
        return "Focus losses: " + focusLossCount +
                " | Fast answers: " + suspiciousFastAnswers;
    }
}