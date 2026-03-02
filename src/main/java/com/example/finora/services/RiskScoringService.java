package com.example.finora.services;

import com.example.finora.entities.User;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RiskScoringService {

    // quick disposable list (you can extend later)
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "tempmail.com", "10minutemail.com", "guerrillamail.com", "mailinator.com",
            "yopmail.com", "yopmail.fr", "yopmail.net", "getnada.com", "trashmail.com");

    public RiskScoringService() {
        // No need to store connection; fetch fresh each time
    }

    // Helper to get a fresh connection
    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    public RiskResult compute(User user) throws SQLException {
        if (user == null)
            return new RiskResult(0, RiskResult.Level.LOW, List.of());

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1) Disposable email
        if (isDisposableEmail(user.getEmail())) {
            score += 35;
            reasons.add("Email jetable détecté");
        }

        // 2) Profile completeness
        if (isBlank(user.getPhone())) {
            score += 10;
            reasons.add("Téléphone manquant");
        }
        if (isBlank(user.getAddress())) {
            score += 8;
            reasons.add("Adresse manquante");
        }
        if (user.getDateOfBirth() == null) {
            score += 8;
            reasons.add("Date de naissance manquante");
        } else {
            // if under 18 (should be blocked by validation, but still a risk signal)
            long age = ChronoUnit.YEARS.between(user.getDateOfBirth(), LocalDate.now());
            if (age < 18) {
                score += 40;
                reasons.add("Âge < 18 (incohérent)");
            }
        }

        // 3) Account age (new accounts are riskier)
        int accountAgeDays = getAccountAgeDays(user.getId());
        if (accountAgeDays >= 0 && accountAgeDays < 3) {
            score += 15;
            reasons.add("Compte très récent (< 3 jours)");
        } else if (accountAgeDays >= 0 && accountAgeDays < 14) {
            score += 8;
            reasons.add("Compte récent (< 14 jours)");
        }

        // 4) Password reset activity (last 30 days)
        int resets30 = countPasswordResets(user.getId(), 30);
        if (resets30 >= 5) {
            score += 25;
            reasons.add("Trop de réinitialisations (≥ 5 / 30 jours)");
        } else if (resets30 >= 2) {
            score += 10;
            reasons.add("Plusieurs réinitialisations (≥ 2 / 30 jours)");
        }

        // 5) Role impact (admin accounts are sensitive)
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            score += 10;
            reasons.add("Compte ADMIN (sensibilité élevée)");
        }

        // clamp score 0..100
        score = Math.max(0, Math.min(100, score));

        RiskResult.Level level;
        if (score >= 60)
            level = RiskResult.Level.HIGH;
        else if (score >= 30)
            level = RiskResult.Level.MEDIUM;
        else
            level = RiskResult.Level.LOW;

        return new RiskResult(score, level, reasons);
    }

    // -------------------- helpers --------------------

    private boolean isDisposableEmail(String email) {
        if (email == null)
            return false;
        String e = email.trim().toLowerCase();
        int at = e.lastIndexOf('@');
        if (at < 0)
            return false;
        String domain = e.substring(at + 1);
        return DISPOSABLE_DOMAINS.contains(domain);
    }

    private int countPasswordResets(int userId, int lastDays) throws SQLException {
        String sql = """
                    SELECT COUNT(*) AS c
                    FROM password_resets
                    WHERE user_id = ?
                      AND created_at >= (NOW() - INTERVAL ? DAY)
                """;
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, lastDays);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("c");
            }
        }
        return 0;
    }

    private int getAccountAgeDays(int userId) throws SQLException {
        String sql = "SELECT created_at FROM users WHERE id=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return -1;
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts == null)
                    return -1;

                long days = ChronoUnit.DAYS.between(ts.toInstant(), java.time.Instant.now());
                return (int) Math.max(0, days);
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}