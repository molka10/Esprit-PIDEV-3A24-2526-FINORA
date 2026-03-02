package com.example.finora.utils;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationService {

    private static VBox container;

    public enum NotificationType {
        SUCCESS, WARNING, ERROR, INFO
    }

    public static void setContainer(VBox toastContainer) {
        container = toastContainer;
    }

    public static void show(String title, String message, NotificationType type) {
        if (container == null) {
            System.err.println("Notification container not initialized!");
            return;
        }

        Platform.runLater(() -> {
            VBox toast = createToast(title, message, type);
            container.getChildren().add(0, toast); // Add at the top

            // Animation: Slide-in and Fade-in
            toast.setOpacity(0);
            toast.setTranslateX(100);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
            fadeIn.setToValue(1.0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
            slideIn.setToX(0);

            fadeIn.play();
            slideIn.play();

            // Auto-dismiss after 4 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                    Platform.runLater(() -> dismissToast(toast));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private static VBox createToast(String titleStr, String messageStr, NotificationType type) {
        VBox toast = new VBox(4);
        toast.getStyleClass().add("toast-item");

        try {
            String css = NotificationService.class.getResource("/com/example/finora/theme.css").toExternalForm();
            toast.getStylesheets().add(css);
        } catch (Exception e) {
            // Fallback if resource not found
        }

        Label title = new Label(titleStr);
        title.getStyleClass().add("toast-title");

        Label msg = new Label(messageStr);
        msg.getStyleClass().add("toast-msg");
        msg.setWrapText(true);

        toast.getChildren().addAll(title, msg);

        // Styling based on type
        switch (type) {
            case SUCCESS:
                toast.getStyleClass().add("toast-success");
                break;
            case WARNING:
                toast.getStyleClass().add("toast-warning");
                break;
            case ERROR:
                toast.getStyleClass().add("toast-error");
                break;
            case INFO:
                toast.getStyleClass().add("toast-info");
                break;
        }

        return toast;
    }

    private static void dismissToast(VBox toast) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> container.getChildren().remove(toast));
        fadeOut.play();
    }
}
