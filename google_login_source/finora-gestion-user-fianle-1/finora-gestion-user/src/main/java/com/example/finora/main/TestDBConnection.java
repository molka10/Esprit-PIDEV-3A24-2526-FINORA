package com.example.finora.main;

import com.example.finora.utils.DBConnection;

import java.sql.Connection;

public class TestDBConnection {

    public static void main(String[] args) {
        try {
            Connection cn = DBConnection.getInstance().getConnection();

            if (cn != null && !cn.isClosed()) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Database connection failed.");
            }

        } catch (Exception e) {
            System.out.println("❌ Error connecting to database:");
            e.printStackTrace();
        }
    }
}
