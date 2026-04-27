package com.example.finora.services;

import java.util.List;

public record RiskResult(int score, Level level, List<String> reasons) {

    public enum Level {
        LOW, MEDIUM, HIGH
    }
}