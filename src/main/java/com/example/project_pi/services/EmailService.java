package com.example.project_pi.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String host = env("SMTP_HOST", "smtp.gmail.com");
    private final int port = Integer.parseInt(env("SMTP_PORT", "587"));
    private final String user = env("SMTP_USER", "");
    private final String pass = env("SMTP_PASS", "");
    private final String from = env("SMTP_FROM", user);

    public void send(String to, String subject, String body) throws MessagingException {
        if (to == null || to.isBlank()) return;
        if (user.isBlank() || pass.isBlank()) {
            throw new MessagingException("SMTP_USER / SMTP_PASS missing (env).");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject);
        msg.setText(body);

        Transport.send(msg);
    }

    private String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }
}