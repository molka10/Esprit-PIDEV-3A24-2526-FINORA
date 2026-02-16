package tn.finora.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private final Connection cnx;

    private DBConnection() {
        try {
            String url = "jdbc:mysql://localhost:3307/finora_db?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String pass = ""; // XAMPP default usually empty
            cnx = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connected to DB (Singleton OK)");
        } catch (SQLException e) {
            throw new RuntimeException("❌ DB connection failed: " + e.getMessage());
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
