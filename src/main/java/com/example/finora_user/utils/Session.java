package com.example.finora_user.utils;

import com.example.finora_user.entities.User;

/**
 * Simple in-memory session for the currently authenticated user.
 *
 * Note: This is enough for a desktop app; it resets when the app restarts.
 */
public final class Session {

    private static User currentUser;

    private Session() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
