package com.example.finora.utils;

import com.example.finora.entities.User;

public class Session {

    private static User currentUser;

    private Session() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return currentUser != null && ("ADMIN".equalsIgnoreCase(currentUser.getRole())
                || "ENTREPRISE".equalsIgnoreCase(currentUser.getRole()));
    }
}