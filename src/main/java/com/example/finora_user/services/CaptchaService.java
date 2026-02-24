package com.example.finora_user.services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

public class CaptchaService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RNG = new SecureRandom();

    private String currentCode;

    public String getCurrentCode() {
        return currentCode;
    }

    public Image generateCaptchaImage(int width, int height, int length) {
        currentCode = randomText(length);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // background
            g.setColor(new Color(246, 245, 255));
            g.fillRect(0, 0, width, height);

            // noise lines
            for (int i = 0; i < 10; i++) {
                g.setColor(new Color(rand(120, 200), rand(120, 200), rand(120, 220)));
                int x1 = rand(0, width);
                int y1 = rand(0, height);
                int x2 = rand(0, width);
                int y2 = rand(0, height);
                g.setStroke(new BasicStroke(1.2f));
                g.drawLine(x1, y1, x2, y2);
            }

            // text
            g.setFont(new Font("Arial", Font.BOLD, height - 18));

            int charSpace = width / (length + 1);
            for (int i = 0; i < currentCode.length(); i++) {
                char c = currentCode.charAt(i);

                g.setColor(new Color(rand(20, 80), rand(20, 80), rand(30, 100)));

                AffineTransform old = g.getTransform();
                double angle = (RNG.nextDouble() - 0.5) * 0.55;
                int x = charSpace * (i + 1) - 10;
                int y = height / 2 + rand(10, 16);

                g.rotate(angle, x, y);
                g.drawString(String.valueOf(c), x, y);
                g.setTransform(old);
            }

            // dots
            for (int i = 0; i < 140; i++) {
                g.setColor(new Color(rand(160, 230), rand(160, 230), rand(160, 240)));
                int x = rand(0, width);
                int y = rand(0, height);
                g.fillOval(x, y, 2, 2);
            }

        } finally {
            g.dispose();
        }

        return SwingFXUtils.toFXImage(img, null);
    }

    public boolean verify(String userInput) {
        if (userInput == null) return false;
        if (currentCode == null) return false;
        return currentCode.equalsIgnoreCase(userInput.trim());
    }

    private static String randomText(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(RNG.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private static int rand(int min, int max) {
        return min + RNG.nextInt(Math.max(1, (max - min + 1)));
    }
}