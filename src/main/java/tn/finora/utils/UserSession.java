package tn.finora.utils;

public class UserSession {

    private static AppRole currentRole;

    public static void setRole(AppRole role) {
        currentRole = role;
    }

    public static AppRole getRole() {
        return currentRole;
    }

    public static boolean isAdmin() {
        return currentRole == AppRole.ADMIN;
    }

    public static boolean isInvestisseur() {
        return currentRole == AppRole.INVESTISSEUR;
    }
}