package com.example.finora.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {

    private static DBConnection instance;
    private Connection cnx;

    private final String URL = "jdbc:mysql://localhost:3306/finora";
    private final String USER = "root";
    private final String PASSWORD = "";


    private database() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(" Connexion BD établie");
        } catch (SQLException e) {
            System.out.println(" Erreur connexion DB");
            e.printStackTrace();
        }
    }


    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return cnx;
    }
}
