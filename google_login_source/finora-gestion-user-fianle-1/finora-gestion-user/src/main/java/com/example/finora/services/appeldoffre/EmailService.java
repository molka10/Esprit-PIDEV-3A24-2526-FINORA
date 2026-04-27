package com.example.finora.services.appeldoffre;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    // ✅ Gmail SMTP (TLS)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    private static final String SMTP_USER = System.getenv("SMTP_USER");
    private static final String SMTP_PASS = System.getenv("SMTP_PASS");

    private static final String FROM_EMAIL = SMTP_USER;

    public void send(String to, String subject, String body) throws MessagingException {
        if (to == null || to.isBlank()) {
            throw new MessagingException("Recipient email is empty.");
        }
        if (SMTP_USER == null || SMTP_USER.isBlank() || SMTP_PASS == null || SMTP_PASS.isBlank()) {
            throw new MessagingException("SMTP credentials are missing in EmailService.java");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // helps on some machines

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM_EMAIL));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(subject == null ? "" : subject, StandardCharsets.UTF_8.name());
        msg.setText(body == null ? "" : body, StandardCharsets.UTF_8.name());

        Transport.send(msg);
    }

}