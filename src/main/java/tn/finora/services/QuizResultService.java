package tn.finora.services;

import tn.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizResultService {

    /**
     * Saves a quiz result to the database.
     */
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
            System.err.println("QuizResultService.save error: " + e.getMessage());
        }
    }

    /**
     * Returns all quiz results (for admin stats if needed later).
     */
    public List<String> getAll() {
        List<String> results = new ArrayList<>();
        String sql = "SELECT * FROM quiz_result ORDER BY taken_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                results.add(
                        rs.getString("student_name") + " | " +
                                rs.getString("lesson_title") + " | " +
                                rs.getInt("score") + "% | " +
                                rs.getBoolean("passed") + " | " +
                                rs.getTimestamp("taken_at")
                );
            }
        } catch (Exception e) {
            System.err.println("QuizResultService.getAll error: " + e.getMessage());
        }
        return results;
    }
}