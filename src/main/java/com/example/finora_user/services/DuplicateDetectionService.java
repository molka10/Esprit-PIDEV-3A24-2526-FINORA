package com.example.finora_user.services;

import com.example.finora_user.entities.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DuplicateDetectionService {

    private final UserService userService;

    // thresholds (tune later)
    private static final double USERNAME_SIM_THRESHOLD = 0.85;
    private static final double EMAIL_SIM_THRESHOLD = 0.90;

    public DuplicateDetectionService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Find possible duplicates using:
     * - Exact email match (case-insensitive)
     * - Normalized email match (Gmail normalization)
     * - Similar username (Levenshtein similarity)
     * - Similar email (optional similarity)
     *
     * @param username username to check
     * @param email email to check
     * @param excludeUserId if updating, pass current user id; for add pass -1
     */
    public List<DuplicateMatch> findDuplicates(String username, String email, int excludeUserId) throws SQLException {
        String uInput = safeLower(username);
        String eInput = safeLower(email);

        String eNormInput = normalizeEmail(eInput);

        List<User> all = userService.getAllUsers();
        List<DuplicateMatch> matches = new ArrayList<>();

        for (User u : all) {
            if (u == null) continue;
            if (excludeUserId > 0 && u.getId() == excludeUserId) continue;

            String uDb = safeLower(u.getUsername());
            String eDb = safeLower(u.getEmail());
            String eNormDb = normalizeEmail(eDb);

            List<String> reasons = new ArrayList<>();
            double bestSim = 0.0;

            // 1) Email exact match
            if (!eInput.isBlank() && eInput.equals(eDb)) {
                reasons.add("Email identique (exact)");
                bestSim = Math.max(bestSim, 1.0);
            }

            // 2) Email normalized match
            if (!eNormInput.isBlank() && eNormInput.equals(eNormDb)) {
                reasons.add("Email identique (normalisé)");
                bestSim = Math.max(bestSim, 1.0);
            }

            // 3) Username similarity
            if (!uInput.isBlank() && !uDb.isBlank()) {
                double s = similarity(uInput, uDb);
                if (s >= USERNAME_SIM_THRESHOLD) {
                    reasons.add("Username similaire (" + percent(s) + ")");
                    bestSim = Math.max(bestSim, s);
                }
            }

            // 4) Email similarity (fallback if not exact)
            if (!eInput.isBlank() && !eDb.isBlank()) {
                double s = similarity(eInput, eDb);
                if (s >= EMAIL_SIM_THRESHOLD && !eInput.equals(eDb)) {
                    reasons.add("Email similaire (" + percent(s) + ")");
                    bestSim = Math.max(bestSim, s);
                }
            }

            if (!reasons.isEmpty()) {
                matches.add(new DuplicateMatch(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        bestSim,
                        reasons
                ));
            }
        }

        matches.sort(Comparator
                .comparingDouble(DuplicateMatch::similarity).reversed()
                .thenComparing(DuplicateMatch::userId));

        // Keep top 8 to avoid noisy UI
        if (matches.size() > 8) {
            return matches.subList(0, 8);
        }
        return matches;
    }

    // -------------------- helpers --------------------

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /**
     * Email normalization:
     * - Lowercase + trim
     * - Gmail: remove dots in local-part + remove +tag
     */
    private static String normalizeEmail(String email) {
        if (email == null) return "";
        String e = email.trim().toLowerCase();
        int at = e.lastIndexOf('@');
        if (at < 0) return e;

        String local = e.substring(0, at);
        String domain = e.substring(at + 1);

        // remove "+tag"
        int plus = local.indexOf('+');
        if (plus >= 0) local = local.substring(0, plus);

        // Gmail normalization
        if (domain.equals("gmail.com") || domain.equals("googlemail.com")) {
            local = local.replace(".", "");
            domain = "gmail.com";
        }

        return local + "@" + domain;
    }

    /**
     * Levenshtein similarity in [0..1]
     */
    private static double similarity(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        if (a.equals(b)) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        int dist = levenshtein(a, b);
        int maxLen = Math.max(a.length(), b.length());
        return 1.0 - ((double) dist / (double) maxLen);
    }

    private static int levenshtein(String s1, String s2) {
        int n = s1.length();
        int m = s2.length();

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            char c1 = s1.charAt(i - 1);

            for (int j = 1; j <= m; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;

                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }

            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }

        return prev[m];
    }

    private static String percent(double v) {
        int p = (int) Math.round(v * 100.0);
        return p + "%";
    }
}