package tn.finora.services;

import tn.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizResultService {

    // ── Save a result ────────────────────────────────────────────────
    public void save(String studentName, int lessonId, String lessonTitle,
                     String formationTitle, int score) {
        String sql = """
            INSERT INTO quiz_result
              (student_name, lesson_id, lesson_title, formation_title, score, passed)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentName);
            ps.setInt(2, lessonId);
            ps.setString(3, lessonTitle);
            ps.setString(4, formationTitle);
            ps.setInt(5, score);
            ps.setBoolean(6, score >= 80);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("QuizResultService.save: " + e.getMessage());
        }
    }

    // ── Get all results (for admin table) ────────────────────────────
    public List<QuizResult> getAll() {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_result ORDER BY taken_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new QuizResult(
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getString("lesson_title"),
                        rs.getString("formation_title"),
                        rs.getInt("score"),
                        rs.getBoolean("passed"),
                        rs.getTimestamp("taken_at").toString()
                ));
            }
        } catch (Exception e) {
            System.err.println("QuizResultService.getAll: " + e.getMessage());
        }
        return list;
    }

    // ── Delete a result by id ────────────────────────────────────────
    public void delete(int id) {
        String sql = "DELETE FROM quiz_result WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("QuizResultService.delete: " + e.getMessage());
        }
    }

    // ── Count total results ──────────────────────────────────────────
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM quiz_result";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("QuizResultService.countTotal: " + e.getMessage());
        }
        return 0;
    }

    // ── Count passed results ─────────────────────────────────────────
    public int countPassed() {
        String sql = "SELECT COUNT(*) FROM quiz_result WHERE passed = TRUE";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("QuizResultService.countPassed: " + e.getMessage());
        }
        return 0;
    }

    // ── Average score ────────────────────────────────────────────────
    public double averageScore() {
        String sql = "SELECT AVG(score) FROM quiz_result";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            System.err.println("QuizResultService.averageScore: " + e.getMessage());
        }
        return 0.0;
    }

    // ── Inner model ──────────────────────────────────────────────────
    public static class QuizResult {
        public final int id;
        public final String studentName;
        public final String lessonTitle;
        public final String formationTitle;
        public final int score;
        public final boolean passed;
        public final String takenAt;

        public QuizResult(int id, String studentName, String lessonTitle,
                          String formationTitle, int score,
                          boolean passed, String takenAt) {
            this.id             = id;
            this.studentName    = studentName;
            this.lessonTitle    = lessonTitle;
            this.formationTitle = formationTitle;
            this.score          = score;
            this.passed         = passed;
            this.takenAt        = takenAt;
        }
    }
}