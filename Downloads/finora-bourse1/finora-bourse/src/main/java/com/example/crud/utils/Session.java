package com.example.crud.utils;

public final class Session {

    public enum Role {
        ADMIN,
        ENTREPRISE,
        INVESTISSEUR
    }

    private static Role currentRole = Role.INVESTISSEUR; // default
    private static String displayName = "Utilisateur";

    private Session() {}

    public static Role getRole() {
        return currentRole;
    }

    public static void setRole(Role role) {
        currentRole = role;
    }

    public static String getDisplayName() {
        return displayName;
    }

    public static void setDisplayName(String name) {
        displayName = name;
    }

    public static boolean isAdmin() { return currentRole == Role.ADMIN; }
    public static boolean isEntreprise() { return currentRole == Role.ENTREPRISE; }
    public static boolean isInvestisseur() { return currentRole == Role.INVESTISSEUR; }
}
