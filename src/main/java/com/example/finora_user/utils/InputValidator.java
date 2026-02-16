package com.example.finora_user.utils;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // username: 3..20, letters/numbers/_ only
    private static final Pattern USERNAME =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    // password: min 6 chars (simple for validation day)
    public static String validateSignup(String username, String email, String pass, String role) {
        if (isBlank(username) || isBlank(email) || isBlank(pass) || isBlank(role))
            return "Tous les champs sont obligatoires.";

        if (!USERNAME.matcher(username).matches())
            return "Username invalide (3-20 caractères, lettres/chiffres/_).";

        if (!EMAIL.matcher(email).matches())
            return "Email invalide. Exemple: user@mail.com";

        if (pass.length() < 6)
            return "Mot de passe trop court (minimum 6 caractères).";

        if (!isRoleValid(role))
            return "Role invalide (ADMIN / ENTREPRISE / USER).";

        return null; // OK
    }

    public static String validateLogin(String email, String pass) {
        if (isBlank(email) || isBlank(pass))
            return "Veuillez remplir email et mot de passe.";

        if (!EMAIL.matcher(email).matches())
            return "Email invalide.";

        if (pass.length() < 6)
            return "Mot de passe invalide.";

        return null; // OK
    }

    public static String validateAddUser(String username, String email, String pass, String role) {
        // same as signup (admin add)
        return validateSignup(username, email, pass, role);
    }

    public static String validateUpdateUser(String username, String email, String role) {
        if (isBlank(username) || isBlank(email) || isBlank(role))
            return "Username, email et role sont obligatoires.";

        if (!USERNAME.matcher(username).matches())
            return "Username invalide (3-20 caractères, lettres/chiffres/_).";

        if (!EMAIL.matcher(email).matches())
            return "Email invalide.";

        if (!isRoleValid(role))
            return "Role invalide (ADMIN / ENTREPRISE / USER).";

        return null;
    }

    private static boolean isRoleValid(String role) {
        return "ADMIN".equals(role) || "ENTREPRISE".equals(role) || "USER".equals(role);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
