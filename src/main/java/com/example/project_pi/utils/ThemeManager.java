package com.example.project_pi.utils;

import javafx.scene.Scene;

public class ThemeManager {

    public enum Theme { LIGHT, DARK }

    private static Theme current = Theme.DARK; // default you want

    private static final String LIGHT_CSS = "/com/example/project_pi/ui/styles/theme-light.css";
    private static final String DARK_CSS  = "/com/example/project_pi/ui/styles/theme-dark.css";

    public static Theme getCurrent() {
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
        scene.getStylesheets().add(ThemeManager.class.getResource(getStylesheet()).toExternalForm());
    }
}
