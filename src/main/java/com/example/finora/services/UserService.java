package com.example.finora.services;

import com.example.finora.entities.User;
import com.example.finora.utils.DBConnection;
import com.example.finora.utils.PasswordUtils;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public UserService() {
        // No need to store connection; fetch fresh each time
    }

    // Helper to get a fresh connection
    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    // -------------------- CRUD --------------------

    public int addUserReturnId(User u) throws SQLException {
        String sql = """
                INSERT INTO users (username, email, mot_de_passe, role, phone, address, date_of_birth)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());

            // Store hashed password if not already hashed
            String pwd = u.getMotDePasse();
            if (pwd == null)
                pwd = "";
            ps.setString(3, PasswordUtils.isHashed(pwd) ? pwd : PasswordUtils.hash(pwd));

            ps.setString(4, u.getRole());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getAddress());

            if (u.getDateOfBirth() != null) {
                ps.setDate(7, Date.valueOf(u.getDateOfBirth()));
            } else {
                ps.setNull(7, Types.DATE);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    u.setId(id);
                    return id;
                }
            }
        }

        return -1;
    }

    public boolean updateUser(User u) throws SQLException {
        String sql = """
                UPDATE users
                SET username=?, email=?, role=?, phone=?, address=?, date_of_birth=?
                WHERE id=?
                """;

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getAddress());

            if (u.getDateOfBirth() != null) {
                ps.setDate(6, Date.valueOf(u.getDateOfBirth()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setInt(7, u.getId());

            System.out.println("[UserService.updateUser] Executing: " + sql);
            System.out.println("[UserService.updateUser] Params: username=" + u.getUsername() + " email=" + u.getEmail() + " role=" + u.getRole() + " phone=" + u.getPhone() + " address=" + u.getAddress() + " dob=" + u.getDateOfBirth() + " id=" + u.getId());
            int rows = ps.executeUpdate();
            System.out.println("[UserService.updateUser] Rows updated: " + rows);
            return rows > 0;
        }
    }

    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> list = new ArrayList<>();

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    // -------------------- SEARCH --------------------

    public List<User> searchText(String q) throws SQLException {
        String sql = """
                SELECT * FROM users
                WHERE username LIKE ? OR email LIKE ?
                ORDER BY created_at DESC
                """;

        List<User> list = new ArrayList<>();
        String like = "%" + (q == null ? "" : q.trim()) + "%";

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }

        return list;
    }

    public List<User> searchByRole(String role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role=? ORDER BY created_at DESC";
        List<User> list = new ArrayList<>();

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, role);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }

        return list;
    }

    // -------------------- FINDERS --------------------

    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public User login(String email, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE email=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;

                User u = map(rs);

                String stored = u.getMotDePasse();
                if (stored == null)
                    return null;

                // If stored is bcrypt hash (normal)
                if (PasswordUtils.isHashed(stored)) {
                    return PasswordUtils.verify(plainPassword, stored) ? u : null;
                }

                // If old users stored plain text, allow login and upgrade to bcrypt once
                if (stored.equals(plainPassword)) {
                    updatePassword(u.getId(), PasswordUtils.hash(plainPassword));
                    // refresh user data (optional)
                    return getUserById(u.getId());
                }

                return null;
            }
        }
    }

    // -------------------- PASSWORD --------------------

    public boolean updatePassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET mot_de_passe=? WHERE id=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            System.out.println("[UserService.updatePassword] Updated password for userId " + userId + ", rows affected: " + rows);
            return rows > 0;
        }
    }

    /**
     * Change password after verifying old password.
     * Sends email after success (non-blocking).
     */
    public boolean changePassword(int userId, String oldPlain, String newPlain) throws SQLException {
        System.out.println("[UserService.changePassword] Starting password change for userId: " + userId);
        String sql = "SELECT email, username, mot_de_passe FROM users WHERE id=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[UserService.changePassword] User not found: " + userId);
                    return false;
                }

                String email = rs.getString("email");
                String username = rs.getString("username");
                String stored = rs.getString("mot_de_passe");
                
                System.out.println("[UserService.changePassword] Found user: " + email + ", stored password is hashed: " + PasswordUtils.isHashed(stored));

                // One-time upgrade if old accounts were plain text
                if (!PasswordUtils.isHashed(stored)) {
                    System.out.println("[UserService.changePassword] Using plain text password upgrade");
                    if (stored != null && stored.equals(oldPlain)) {
                        boolean ok = updatePassword(userId, PasswordUtils.hash(newPlain));
                        if (ok)
                            safeSendChangedEmail(email, username);
                        System.out.println("[UserService.changePassword] Plain text upgrade result: " + ok);
                        return ok;
                    }
                    return false;
                }

                // Normal bcrypt verification
                if (!PasswordUtils.verify(oldPlain, stored)) {
                    System.out.println("[UserService.changePassword] Old password verification failed");
                    return false;
                }

                boolean ok = updatePassword(userId, PasswordUtils.hash(newPlain));
                if (ok)
                    safeSendChangedEmail(email, username);
                
                System.out.println("[UserService.changePassword] Password change result: " + ok);
                return ok;
            }
        }
    }

    private void safeSendChangedEmail(String toEmail, String username) {
        try {
            EmailService emailService = new EmailService();
            emailService.sendPasswordChangedEmail(toEmail, username);
        } catch (Exception ex) {
            // Don’t fail password change if email fails
            System.out.println("[FINORA] Email failed (password changed): " + ex.getMessage());
        }
    }

    // -------------------- MAPPER --------------------

    private User map(ResultSet rs) throws SQLException {
        User u = new User();

        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            u.setDateOfBirth(dob.toLocalDate());
        } else {
            u.setDateOfBirth((LocalDate) null);
        }

        return u;
    }

    // -------------------- GOOGLE LOGIN --------------------

    /**
     * Find an existing user by Google email, or auto-create a new one.
     * Google users get role "USER" and a marker password so normal
     * password login is blocked for them.
     */
    public User findOrCreateGoogleUser(String name, String email) throws SQLException {
        User existing = getUserByEmail(email);
        if (existing != null)
            return existing;

        // First-time Google login → create account automatically
        User newUser = new User();
        newUser.setUsername(name);
        newUser.setEmail(email);
        newUser.setMotDePasse("GOOGLE_OAUTH_USER"); // marker, not a real password
        newUser.setRole("USER");

        int id = addUserReturnId(newUser);
        newUser.setId(id);
        System.out.println("\u2705 [GoogleLogin] Auto-created user: " + email);
        return newUser;
    }
}