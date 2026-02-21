package com.example.project_pi.controllers;

import com.example.project_pi.entities.AppelOffre;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class AppelOffreCardCell extends ListCell<AppelOffre> {

    private final VBox root = new VBox(6);
    private final Label titleLine = new Label();
    private final Label meta1 = new Label();
    private final Label meta2 = new Label();

    public AppelOffreCardCell() {
        root.getStyleClass().add("ao-card");
        root.setPadding(new Insets(12));

        titleLine.getStyleClass().add("ao-title");
        meta1.getStyleClass().add("ao-meta");
        meta2.getStyleClass().add("ao-meta");

        root.getChildren().addAll(titleLine, meta1, meta2);
    }

    @Override
    protected void updateItem(AppelOffre a, boolean empty) {
        super.updateItem(a, empty);

        if (empty || a == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        titleLine.setText("📌 " + safe(a.getTitre()));

        meta1.setText(
                "🏷 " + safe(a.getCategorie()) +
                        " • 🧩 " + safe(a.getType()) +
                        " • 📊 " + safe(a.getStatut())
        );

        String dev = (a.getDevise() == null || a.getDevise().isBlank()) ? "" : " " + a.getDevise();
        String min = (a.getBudgetMin() == 0) ? "-" : String.valueOf(a.getBudgetMin());
        String max = (a.getBudgetMax() == 0) ? "-" : String.valueOf(a.getBudgetMax());

        String date = (a.getDateLimite() == null)
                ? "-"
                : a.getDateLimite().format(DateTimeFormatter.ISO_DATE);

        meta2.setText("💰 " + min + " - " + max + dev + " • 🗓 " + date);

        setText(null);
        setGraphic(root);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}