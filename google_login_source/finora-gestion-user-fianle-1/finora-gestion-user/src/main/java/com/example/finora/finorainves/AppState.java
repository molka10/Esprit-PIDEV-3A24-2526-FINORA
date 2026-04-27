package com.example.finora.finorainves;

import com.example.finora.entities.Investment;
import com.example.finora.entities.InvestmentManagement;

public class AppState {
    private static Investment selectedInvestment;
    private static InvestmentManagement selectedManagement;

    public static Investment getSelectedInvestment() {
        return selectedInvestment;
    }

    public static void setSelectedInvestment(Investment investment) {
        selectedInvestment = investment;
    }

    public static InvestmentManagement getSelectedManagement() {
        return selectedManagement;
    }

    public static void setSelectedManagement(InvestmentManagement management) {
        selectedManagement = management;
    }
}
