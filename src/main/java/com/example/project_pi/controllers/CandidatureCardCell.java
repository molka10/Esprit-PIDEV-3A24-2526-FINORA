package com.example.project_pi.controllers;

import com.example.project_pi.entities.Candidature;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CandidatureCardCell extends ListCell<Candidature> {

    private final Map<Integer, String> appelOffreTitles;

    private final VBox root = new VBox(6);
    private final Label titleLine = new Label();
    private final Label meta1 = new Label();
    private final Label meta2 = new Label();

    public CandidatureCardCell(Map<Integer, String> appelOffreTitles) {
        this.appelOffreTitles = appelOffreTitles;

        root.getStyleClass().add("ao-card");
        root.setPadding(new Insets(12));

        titleLine.getStyleClass().add("ao-title");
        meta1.getStyleClass().add("ao-meta");
        meta2.getStyleClass().add("ao-meta");

        root.getChildren().addAll(titleLine, meta1, meta2);
    }

    @Override
    protected void updateItem(Candidature c, boolean empty) {
        super.updateItem(c, empty);

        if (empty || c == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        String aoTitle = appelOffreTitles.getOrDefault(
                c.getAppelOffreId(),
                "Appel d’Offre #" + c.getAppelOffreId()
        );

        titleLine.setText("📌 " + aoTitle);

        meta1.setText(
                "👤 " + safe(c.getNomCandidat()) +
                        " • ✉ " + safe(c.getEmailCandidat()) +
                        " • 📊 " + safe(c.getStatut())
        );

        String date = (c.getCreatedAt() == null)
                ? "-"
                : c.getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        meta2.setText("💰 " + c.getMontantPropose() + " • 🗓 " + date);

        setText(null);
        setGraphic(root);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}