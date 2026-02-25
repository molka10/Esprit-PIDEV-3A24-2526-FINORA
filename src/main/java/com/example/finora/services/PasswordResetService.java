package com.example.finora.services;

import com.example.finora.utils.DBConnection;
import com.example.finora.utils.PasswordUtils;

import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PasswordResetService {

    private final Connection cn;
    private final SecureRandom rng = new SecureRandom();

    public PasswordResetService() throws SQLException {
        cn = DBConnection.getInstance().getConnection();
    }

    // 6-digit OTP, expires in 5 minutes
    public String createOtpForUser(int userId) throws SQLException {
        String code = String.format("%06d", rng.nextInt(1_000_000));
        Timestamp expires = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));

        String sql = "INSERT INTO password_resets (user_id, code, expires_at, used) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, code);
            ps.setTimestamp(3, expires);
            ps.executeUpdate();
        }

        return code;
    }

    public boolean verifyOtp(int userId, String code) throws SQLException {
        String sql = """
            SELECT id, expires_at, used
            FROM password_resets
            WHERE user_id=? AND code=?
            ORDER BY id DESC
            LIMIT 1
        """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;

                int resetId = rs.getInt("id");
                Timestamp expiresAt = rs.getTimestamp("expires_at");
                boolean used = rs.getInt("used") == 1;

                if (used) return false;
                if (expiresAt == null || expiresAt.before(new Timestamp(System.currentTimeMillis()))) return false;

                // mark used
                try (PreparedStatement ups = cn.prepareStatement("UPDATE password_resets SET used=1 WHERE id=?")) {
                    ups.setInt(1, resetId);
                    ups.executeUpdate();
                }

                return true;
            }
        }
    }

    /**
     * Reset password (bcrypt) + send email (non-blocking).
     */
    public boolean resetPassword(int userId, String newPlainPassword, UserService userService) throws SQLException {
        if (newPlainPassword == null || newPlainPassword.length() < 8) return false;

        boolean ok = userService.updatePassword(userId, PasswordUtils.hash(newPlainPassword));
        if (!ok) return false;

        // Send email confirmation (don’t fail reset if email fails)
        try {
            var u = userService.getUserById(userId);
            if (u != null) {
                EmailService emailService = new EmailService();
                emailService.sendPasswordResetEmail(u.getEmail(), u.getUsername());
            }
        } catch (Exception ex) {
            System.out.println("[FINORA] Email failed (password reset): " + ex.getMessage());
        }

        return true;
    }
}