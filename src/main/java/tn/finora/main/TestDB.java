package tn.finora.main;

import tn.finora.utils.DBConnection;

public class TestDB {
    public static void main(String[] args) {
        DBConnection.getInstance().getCnx();
        System.out.println("✅ Test finished");
    }
}
