package tn.finora.services;

import tn.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizResultService {

    private final Connection cnx = DBConnection.getInstance().getCnx();

    public void save(String studentName, int lessonId, String lessonTitle,
                     String formationTitle, int score) {
        String sql = """
            INSERT INTO quiz_result
              (student_name, lesson_id, lesson_title, formation_title, score, passed)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
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

    public List<QuizResult> getAll() {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_result ORDER BY taken_at DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            System.err.println("QuizResultService.getAll: " + e.getMessage());
        }
        return list;
    }

    public List<QuizResult> getAllByLesson(int lessonId) {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_result WHERE lesson_id=? ORDER BY taken_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            System.err.println("QuizResultService.getAllByLesson: " + e.getMessage());
        }
        return list;
    }

    public void delete(int id) {
        String sql = "DELETE FROM quiz_result WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("QuizResultService.delete: " + e.getMessage());
        }
    }

    public int countTotal() {
        return countInt("SELECT COUNT(*) FROM quiz_result");
    }

    public int countPassed() {
        return countInt("SELECT COUNT(*) FROM quiz_result WHERE passed = TRUE");
    }

    public double averageScore() {
        return avg("SELECT AVG(score) FROM quiz_result");
    }

    public int countTotalByLesson(int lessonId) {
        return countInt("SELECT COUNT(*) FROM quiz_result WHERE lesson_id=" + lessonId);
    }

    public int countPassedByLesson(int lessonId) {
        return countInt("SELECT COUNT(*) FROM quiz_result WHERE lesson_id=" + lessonId + " AND passed=TRUE");
    }

    public double averageScoreByLesson(int lessonId) {
        return avg("SELECT AVG(score) FROM quiz_result WHERE lesson_id=" + lessonId);
    }

    private int countInt(String sql) {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("QuizResultService.count: " + e.getMessage());
        }
        return 0;
    }

    private double avg(String sql) {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            System.err.println("QuizResultService.avg: " + e.getMessage());
        }
        return 0.0;
    }

    private QuizResult map(ResultSet rs) throws SQLException {
        return new QuizResult(
                rs.getInt("id"),
                rs.getString("student_name"),
                rs.getInt("lesson_id"),
                rs.getString("lesson_title"),
                rs.getString("formation_title"),
                rs.getInt("score"),
                rs.getBoolean("passed"),
                rs.getTimestamp("taken_at").toString()
        );
    }

    public static class QuizResult {
        public final int id;
        public final String studentName;
        public final int lessonId;
        public final String lessonTitle;
        public final String formationTitle;
        public final int score;
        public final boolean passed;
        public final String takenAt;

        public QuizResult(int id, String studentName, int lessonId, String lessonTitle,
                          String formationTitle, int score,
                          boolean passed, String takenAt) {
            this.id             = id;
            this.studentName    = studentName;
            this.lessonId       = lessonId;
            this.lessonTitle    = lessonTitle;
            this.formationTitle = formationTitle;
            this.score          = score;
            this.passed         = passed;
            this.takenAt        = takenAt;
        }
    }
}