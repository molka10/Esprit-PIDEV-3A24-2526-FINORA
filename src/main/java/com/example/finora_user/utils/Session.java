package com.example.finora_user.utils;

import com.example.finora_user.entities.User;

public class Session {

    private static User currentUser;

    private Session() {}

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
}