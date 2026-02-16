package com.example.project_pi.main;

import com.example.project_pi.utils.DBConnection;
import java.sql.Connection;
public class TestDB {
    public static void main(String[] args) {
        DBConnection.getInstance().getCnx();
        System.out.println("✅ Test finished");
    }
}
