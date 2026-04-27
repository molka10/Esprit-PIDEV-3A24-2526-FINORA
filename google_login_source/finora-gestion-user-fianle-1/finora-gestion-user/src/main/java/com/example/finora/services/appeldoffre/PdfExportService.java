package com.example.finora.services.appeldoffre;

import com.example.finora.entities.AppelOffre;
import com.example.finora.entities.Candidature;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PdfExportService {

    private static final float MARGIN = 50f;
    private static final float HEADER_HEIGHT = 60f;

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final Color PURPLE = new Color(168, 85, 247);
    private static final Color LIGHT_GRAY = new Color(245, 245, 250);
    private static final Color DARK_TEXT = new Color(31, 41, 55);

    public void exportAppelOffreWithCandidatures(AppelOffre ao,
                                                 List<Candidature> cands,
                                                 File file) throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // ===== HEADER =====
            cs.setNonStrokingColor(PURPLE);
            cs.addRect(0, pageHeight - HEADER_HEIGHT, pageWidth, HEADER_HEIGHT);
            cs.fill();

            drawText(cs,
                    "FINORA - Export Appel d'Offre",
                    MARGIN,
                    pageHeight - 30,
                    FONT_BOLD,
                    16,
                    Color.WHITE);

            String now = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            drawText(cs,
                    "Generated on: " + now,
                    MARGIN,
                    pageHeight - 45,
                    FONT_REGULAR,
                    10,
                    Color.WHITE);

            float y = pageHeight - HEADER_HEIGHT - 30;

            // ===== SECTION: APPEL OFFRE =====
            drawSectionTitle(cs, "Appel d'Offre", y);
            y -= 20;

            y = drawKeyValue(cs, "Titre", ao.getTitre(), y);
            y = drawKeyValue(cs, "Categorie", ao.getCategorie(), y);
            y = drawKeyValue(cs, "Type", ao.getType(), y);
            y = drawKeyValue(cs, "Budget", formatBudget(ao), y);

            String dateLimite = (ao.getDateLimite() == null)
                    ? "-"
                    : ao.getDateLimite().format(DateTimeFormatter.ISO_DATE);

            y = drawKeyValue(cs, "Date limite", dateLimite, y);
            y = drawKeyValue(cs, "Statut", ao.getStatut(), y);

            y -= 10;
            drawSectionTitle(cs, "Description", y);
            y -= 20;

            y = drawWrappedText(cs, ao.getDescription(), MARGIN, y, 90);

            // ===== SECTION: CANDIDATURES =====
            y -= 30;
            drawSectionTitle(cs,
                    "Candidatures (" + cands.size() + ")",
                    y);
            y -= 25;

            for (Candidature c : cands) {

                if (y < 80) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - MARGIN;
                }

                cs.setNonStrokingColor(LIGHT_GRAY);
                cs.addRect(MARGIN, y - 55,
                        pageWidth - 2 * MARGIN,
                        50);
                cs.fill();

                y -= 15;
                y = drawKeyValue(cs, "Nom", c.getNomCandidat(), y);
                y = drawKeyValue(cs, "Email", c.getEmailCandidat(), y);

                String montant = c.getMontantPropose() == 0
                        ? "-"
                        : String.valueOf(c.getMontantPropose());

                y = drawKeyValue(cs, "Montant", montant, y);
                y = drawKeyValue(cs, "Statut", c.getStatut(), y);

                y -= 10;
            }

            cs.close();
            document.save(file);
        }
    }

    // ==============================
    // TEXT HELPERS
    // ==============================

    private void drawSectionTitle(PDPageContentStream cs,
                                  String text,
                                  float y) throws IOException {
        drawText(cs, text, MARGIN, y, FONT_BOLD, 13, DARK_TEXT);
    }

    private float drawKeyValue(PDPageContentStream cs,
                               String key,
                               String value,
                               float y) throws IOException {

        drawText(cs,
                key + ": " + sanitize(value),
                MARGIN,
                y,
                FONT_REGULAR,
                11,
                DARK_TEXT);

        return y - 15;
    }

    private float drawWrappedText(PDPageContentStream cs,
                                  String text,
                                  float x,
                                  float y,
                                  int maxChars) throws IOException {

        List<String> lines = wrap(sanitize(text), maxChars);

        for (String line : lines) {
            drawText(cs, line, x, y, FONT_REGULAR, 10, DARK_TEXT);
            y -= 12;
        }

        return y;
    }

    private void drawText(PDPageContentStream cs,
                          String text,
                          float x,
                          float y,
                          PDType1Font font,
                          int size,
                          Color color) throws IOException {

        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
    }

    // ==============================
    // SANITIZER (IMPORTANT)
    // ==============================

    private String sanitize(String s) {
        if (s == null) return "-";
        String t = s.trim();
        if (t.isEmpty()) return "-";

        t = t.replace('\u2019', '\'')
                .replace('\u2018', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .replace('\u2022', '-')
                .replace('\u2013', '-')
                .replace('\u2014', '-');

        // remove unsupported unicode (including emojis)
        t = t.replaceAll("[^\\x09\\x0A\\x0D\\x20-\\x7E\\xA0-\\xFF]", "");

        return t.isEmpty() ? "-" : t;
    }

    private List<String> wrap(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text == null) return List.of("-");

        String t = text;

        while (t.length() > maxChars) {
            int cut = t.lastIndexOf(' ', maxChars);
            if (cut <= 0) cut = maxChars;
            lines.add(t.substring(0, cut));
            t = t.substring(cut).trim();
        }

        if (!t.isEmpty()) lines.add(t);
        return lines;
    }

    private String formatBudget(AppelOffre ao) {
        String dev = ao.getDevise() == null ? "" : " " + ao.getDevise();
        String min = ao.getBudgetMin() == 0 ? "-" : String.valueOf(ao.getBudgetMin());
        String max = ao.getBudgetMax() == 0 ? "-" : String.valueOf(ao.getBudgetMax());
        return min + " - " + max + dev;
    }
}