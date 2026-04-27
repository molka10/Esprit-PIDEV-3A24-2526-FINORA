package com.example.finora.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class QuizFraudService {

    private LocalDateTime quizStartTime;
    private LocalDateTime questionStartTime;

    private int focusLossCount = 0;
    private int exitFullscreenCount = 0;
    private int suspiciousFastAnswers = 0;

    private final Map<Integer, Long> questionTimes = new HashMap<>();

    // Minimum seconds expected per question
    private static final int MIN_SECONDS_PER_QUESTION = 5;

    // ✅ Score based
    private int fraudScore = 0;

    public void startQuiz() {
        quizStartTime = LocalDateTime.now();
        fraudScore = 0;
        focusLossCount = 0;
        exitFullscreenCount = 0;
        suspiciousFastAnswers = 0;
        questionTimes.clear();
        questionStartTime = null;
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
            fraudScore += 10;
        }
    }

    public void registerFocusLoss() {
        focusLossCount++;
        fraudScore += 30;
    }

    public void registerExitFullscreen() {
        exitFullscreenCount++;
        fraudScore += 25;
    }

    public int getFocusLossCount() { return focusLossCount; }
    public int getExitFullscreenCount() { return exitFullscreenCount; }
    public int getSuspiciousFastAnswers() { return suspiciousFastAnswers; }
    public int getFraudScore() { return fraudScore; }

    public boolean isFraudSuspicious() {
        // ✅ strong rules
        if (exitFullscreenCount >= 1) return true;
        if (focusLossCount >= 3) return true;
        if (suspiciousFastAnswers >= 3) return true;

        // ✅ score rule
        return fraudScore >= 60;
    }

    public String getFraudReport() {
        return "Focus losses: " + focusLossCount +
                " | Fullscreen exits: " + exitFullscreenCount +
                " | Fast answers: " + suspiciousFastAnswers +
                " | Score: " + fraudScore;
    }
}