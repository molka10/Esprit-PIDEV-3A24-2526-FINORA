package com.example.finora.utils;

import javafx.scene.Scene;

public class ThemeManager {

    public enum Theme {
        LIGHT, DARK
    }

    private static Theme current = Theme.LIGHT;

    private static final String BASE_CSS = "/ui/styles/app.css";
    private static final String LIGHT_CSS = "/ui/styles/theme-light.css";
    private static final String DARK_CSS = "/ui/styles/theme-dark.css";

    public static Theme getCurrent() {
        return current;
    }

    public static Theme getTheme() {
        return current;
    }

    public static String getStylesheet() {
        return (current == Theme.DARK) ? DARK_CSS : LIGHT_CSS;
    }

    public static void setTheme(Theme theme) {
        current = theme;
    }

    public static void toggle() {
        current = (current == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
    }

    public static void apply(Scene scene) {
        scene.getStylesheets().clear();
        // Layer 1: Base layouts and classes
        scene.getStylesheets().add(ThemeManager.class.getResource(BASE_CSS).toExternalForm());
        // Layer 2: Theme-specific colors (overrides Layer 1)
        scene.getStylesheets().add(ThemeManager.class.getResource(getStylesheet()).toExternalForm());
    }
}
