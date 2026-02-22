package com.example.finora_user.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String from;

    public EmailService() {
        host = mustGet("SMTP_HOST");
        port = Integer.parseInt(mustGet("SMTP_PORT"));
        username = mustGet("SMTP_USERNAME");
        password = mustGet("SMTP_PASSWORD");
        from = mustGet("SMTP_FROM");
    }

    public void sendPasswordChangedEmail(String toEmail, String usernameDisplay) {
        String subject = "Finora - Mot de passe modifié";
        String body = """
                Bonjour %s,

                Votre mot de passe Finora a été modifié avec succès.

                Si vous n’êtes pas à l’origine de ce changement, veuillez réinitialiser votre mot de passe immédiatement
                ou contacter le support.

                -- Finora
                """.formatted(usernameDisplay == null ? "" : usernameDisplay);

        sendText(toEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String usernameDisplay) {
        String subject = "Finora - Mot de passe réinitialisé";
        String body = """
                Bonjour %s,

                Votre mot de passe Finora a été réinitialisé avec succès.

                Si vous n’êtes pas à l’origine de cette action, veuillez contacter le support.

                -- Finora
                """.formatted(usernameDisplay == null ? "" : usernameDisplay);

        sendText(toEmail, subject, body);
    }

    public void sendText(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Email destinataire vide.");
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject(subject);

            // ✅ Correct UTF-8 content for Jakarta Mail
            msg.setContent(body, "text/plain; charset=UTF-8");

            Transport.send(msg);

        } catch (MessagingException e) {
            throw new RuntimeException("Échec envoi email: " + e.getMessage(), e);
        }
    }

    private static String mustGet(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Variable d'environnement manquante: " + key);
        }
        return v.trim();
    }
}