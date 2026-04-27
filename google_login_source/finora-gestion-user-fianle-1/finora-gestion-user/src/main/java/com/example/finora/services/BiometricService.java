package com.example.finora.services;

import com.example.finora.utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BiometricService {

    /**
     * Saves the face embedding vector for a user.
     */
    public void saveBiometric(int userId, double[] embedding) throws SQLException {
        String query = "INSERT INTO user_biometrics (user_id, face_embedding) VALUES (?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, embeddingToString(embedding));
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves the active face embedding vector for a user.
     */
    public double[] getBiometric(int userId) throws SQLException {
        String query = "SELECT face_embedding FROM user_biometrics WHERE user_id = ? AND is_active = TRUE LIMIT 1";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return stringToEmbedding(rs.getString("face_embedding"));
                }
            }
        }
        return null;
    }

    /**
     * Checks if a user has enrolled for Face ID.
     */
    public boolean hasFaceId(int userId) throws SQLException {
        String query = "SELECT 1 FROM user_biometrics WHERE user_id = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String embeddingToString(double[] embedding) {
        if (embedding == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1)
                sb.append(",");
        }
        return sb.toString();
    }

    private double[] stringToEmbedding(String str) {
        if (str == null || str.isEmpty())
            return null;
        String[] parts = str.split(",");
        double[] embedding = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Double.parseDouble(parts[i]);
        }
        return embedding;
    }
}
