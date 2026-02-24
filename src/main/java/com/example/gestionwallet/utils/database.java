//package
package com.example.gestionwallet.utils;
//import JDBC
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    //principe Singleton
    private static database instance;
    //connexion
    private Connection cnx;
    //connexion MySQL
    private final String URL = "jdbc:mysql://localhost:3306/finora";
    private final String USER = "root";
    private final String PASSWORD = "";

    //pour Singleton
    private database() {
        try {
            //Connexion à la base
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(" Connexion BD établie");
        } catch (SQLException e) {
            System.out.println("Erreur connexion BD : " + e.getMessage());
            e.printStackTrace();
        }
    }

    //logique Singleton Si instance vide : crée - Sinon : retourne même instance
    public static database getInstance() {
        if (instance == null) {
            instance = new database();
        }
        return instance;
    }
    //récupérer connexion
    public Connection getConnection() {
        return cnx;
    }
}
