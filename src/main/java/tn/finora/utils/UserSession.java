package tn.finora.utils;

public class UserSession {

    private static AppRole role = AppRole.ADMIN; // default

    public static AppRole getRole() {
        return role;
    }

    public static void setRole(AppRole role) {
        UserSession.role = role;
    }

    public static boolean isAdmin() {
        return role == AppRole.ADMIN;
    }

    public static boolean isUser() {
        return role == AppRole.USER;
    }
}