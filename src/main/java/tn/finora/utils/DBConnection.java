package tn.finora.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private final Connection cnx;

    private DBConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/finora?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String pass = ""; // XAMPP default usually empty
            cnx = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connected to DB finora");
        } catch (SQLException e) {
            throw new RuntimeException("❌ DB connection failed: " + e.getMessage());
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) instance = new DBConnection();
        return instance;
    }

    private static final String URL      = "jdbc:mysql://localhost:3306/finora"; // ← your DB name
    private static final String USER     = "root";    // ← your MySQL username
    private static final String PASSWORD = "";        // ← your MySQL password

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public Connection getCnx() {
        return cnx;
    }
}
