package com.example.finora.utils;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{8,15}$");

    // ---------------- LOGIN ----------------
    public static String validateLogin(String email, String pass) {
        if (email == null || email.trim().isEmpty()) return "Email requis.";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Format email invalide.";
        if (pass == null || pass.isEmpty()) return "Mot de passe requis.";
        return null;
    }

    // ---------------- USERS ADD (Gestion users) ----------------
    public static String validateAddUser(String username, String email, String pass, String role) {
        if (username == null || username.trim().isEmpty()) return "Username requis.";
        if (username.trim().length() < 3) return "Username min 3 caractères.";
        if (email == null || email.trim().isEmpty()) return "Email requis.";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Format email invalide.";
        if (pass == null || pass.isEmpty()) return "Mot de passe requis.";
        if (pass.length() < 6) return "Mot de passe min 6 caractères.";
        if (role == null || role.trim().isEmpty()) return "Role requis.";
        return null;
    }

    // ---------------- USERS UPDATE (Gestion users) ----------------
    public static String validateUpdateUser(String username, String email, String role) {
        if (username == null || username.trim().isEmpty()) return "Username requis.";
        if (username.trim().length() < 3) return "Username min 3 caractères.";
        if (email == null || email.trim().isEmpty()) return "Email requis.";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Format email invalide.";
        if (role == null || role.trim().isEmpty()) return "Role requis.";
        return null;
    }

    // ---------------- SIGNUP (NEW) ----------------
    public static String validateSignup(
            String username,
            String email,
            String password,
            String confirmPassword,
            String phone,
            String address,
            LocalDate dob,
            boolean termsAccepted
    ) {
        if (username == null || username.trim().isEmpty())
            return "Username requis.";
        if (username.trim().length() < 3)
            return "Username min 3 caractères.";

        if (email == null || email.trim().isEmpty())
            return "Email requis.";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            return "Format email invalide.";

        if (password == null || password.isEmpty())
            return "Mot de passe requis.";
        if (password.length() < 6)
            return "Mot de passe min 6 caractères.";

        if (confirmPassword == null || confirmPassword.isEmpty())
            return "Confirmation mot de passe requise.";
        if (!password.equals(confirmPassword))
            return "Les mots de passe ne correspondent pas.";

        if (phone == null || phone.trim().isEmpty())
            return "Téléphone requis.";
        if (!PHONE_PATTERN.matcher(phone.trim()).matches())
            return "Téléphone invalide (8-15 chiffres).";

        if (address == null || address.trim().isEmpty())
            return "Adresse requise.";
        if (address.trim().length() < 3)
            return "Adresse trop courte.";

        if (dob == null)
            return "Date de naissance requise.";

        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 18)
            return "Vous devez avoir au moins 18 ans.";

        if (!termsAccepted)
            return "Vous devez accepter les Conditions générales.";

        return null; // ✅ OK
    }
}
