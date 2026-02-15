package com.example.crud.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Database instance;
    private Connection connection;

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/finora?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // mets ton mot de passe si tu en as un

    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion MySQL réussie !");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur connexion MySQL");
            System.out.println(e.getMessage());
        }
        return connection;
    }
}
