package com.example.finora.finorainves;

import tn.finora.entities.Investment;
import tn.finora.entities.InvestmentManagement;

public class AppState {
    private static Investment selectedInvestment;
    private static Investment editingInvestment;

    private static InvestmentManagement selectedManagement;
    private static InvestmentManagement editingManagement;

    public static Investment getSelectedInvestment() { return selectedInvestment; }
    public static void setSelectedInvestment(Investment inv) { selectedInvestment = inv; }

    public static Investment getEditingInvestment() { return editingInvestment; }
    public static void setEditingInvestment(Investment inv) { editingInvestment = inv; }

    public static InvestmentManagement getSelectedManagement() { return selectedManagement; }
    public static void setSelectedManagement(InvestmentManagement m) { selectedManagement = m; }

    public static InvestmentManagement getEditingManagement() { return editingManagement; }
    public static void setEditingManagement(InvestmentManagement m) { editingManagement = m; }
}
