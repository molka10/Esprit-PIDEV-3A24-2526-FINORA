package com.example.finora.main;

import com.example.finora.utils.DBConnection;

public class TestDB {
    public static void main(String[] args) {
        DBConnection.getInstance().getCnx();
        System.out.println("✅ Test finished");
    }
}
