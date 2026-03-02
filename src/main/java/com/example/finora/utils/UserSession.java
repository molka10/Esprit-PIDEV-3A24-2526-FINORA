package com.example.finora.utils;

import com.example.finora.entities.User;

public class UserSession {

    public enum AppRole {
        ADMIN,
        INVESTISSEUR,
        ENTREPRISE
    }

    private static AppRole currentRole;

    public static void setRole(AppRole role) {
        currentRole = role;
    }

    public static AppRole getRole() {
        if (currentRole == null && Session.isLoggedIn()) {
            String roleStr = Session.getCurrentUser().getRole();
            if ("ADMIN".equalsIgnoreCase(roleStr)) {
                currentRole = AppRole.ADMIN;
            } else if ("ENTREPRISE".equalsIgnoreCase(roleStr)) {
                currentRole = AppRole.ENTREPRISE;
            } else {
                currentRole = AppRole.INVESTISSEUR;
            }
        }
        return currentRole;
    }

    public static boolean isAdmin() {
        return getRole() == AppRole.ADMIN;
    }

    public static boolean isInvestisseur() {
        return getRole() == AppRole.INVESTISSEUR;
    }

    public static boolean isEntreprise() {
        return getRole() == AppRole.ENTREPRISE;
    }
}
