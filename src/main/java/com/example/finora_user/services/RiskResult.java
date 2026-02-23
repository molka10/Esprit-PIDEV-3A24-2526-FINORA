package com.example.finora_user.services;

import java.util.List;

public record RiskResult(int score, Level level, List<String> reasons) {

    public enum Level {
        LOW, MEDIUM, HIGH
    }
}