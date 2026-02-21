package com.example.project_pi.services;

import com.example.project_pi.entities.AppelOffre;
import com.example.project_pi.entities.Candidature;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PdfExportService {

    private static final float MARGIN = 48f;
    private static final float LEADING = 14f;

    // PDFBox 3.x fonts (no more PDType1Font.HELVETICA_BOLD constants)
    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    /**
     * Export 1 AppelOffre + its candidatures into a PDF file.
     * No JOIN needed: candidatures list is passed in.
     */
    public void exportAppelOffreWithCandidatures(AppelOffre ao, List<Candidature> cands, File outFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            buildDocumentRobust(doc, ao, cands);
            doc.save(outFile);
        }
    }

    // ---------------- Robust builder (handles paging cleanly) ----------------

    private void buildDocumentRobust(PDDocument doc, AppelOffre ao, List<Candidature> cands) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float y = page.getMediaBox().getHeight() - MARGIN;

        // Header
        y = writeLine(cs, "Gestion Appel d’Offre - Export PDF", FONT_BOLD, 16, y);
        y = writeLine(cs, "------------------------------------------------------------", FONT_REGULAR, 11, y);

        // AppelOffre section
        y = gap(y, 8);
        y = writeLine(cs, "Appel d’Offre", FONT_BOLD, 14, y);

        y = writeKeyValue(cs, "Titre", safe(ao.getTitre()), y);
        y = writeKeyValue(cs, "Catégorie", safe(ao.getCategorie()), y);
        y = writeKeyValue(cs, "Type", safe(ao.getType()), y);
        y = writeKeyValue(cs, "Budget", formatBudget(ao), y);

        String dateLimite = (ao.getDateLimite() == null)
                ? "-"
                : ao.getDateLimite().format(DateTimeFormatter.ISO_DATE);
        y = writeKeyValue(cs, "Date limite", dateLimite, y);

        y = writeKeyValue(cs, "Statut", safe(ao.getStatut()), y);

        y = gap(y, 6);
        y = writeLine(cs, "Description:", FONT_BOLD, 12, y);
        y = writeWrapped(cs, safe(ao.getDescription()), FONT_REGULAR, 11, y, 95);

        // Candidatures section
        y = gap(y, 12);
        y = writeLine(cs, "Candidatures (" + cands.size() + ")", FONT_BOLD, 14, y);
        y = writeLine(cs, "------------------------------------------------------------", FONT_REGULAR, 11, y);

        if (cands.isEmpty()) {
            y = gap(y, 6);
            writeLine(cs, "Aucune candidature pour cet appel d’offre.", FONT_REGULAR, 11, y);
            cs.close();
            return;
        }

        int i = 1;
        for (Candidature c : cands) {
            // Need space for a candidature block
            if (y < 120) {
                cs.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                y = page.getMediaBox().getHeight() - MARGIN;

                y = writeLine(cs, "Suite - Candidatures", FONT_BOLD, 13, y);
                y = writeLine(cs, "------------------------------------------------------------", FONT_REGULAR, 11, y);
            }

            y = writeCandidatureBlock(cs, c, i, y);
            i++;
            y = gap(y, 6);
        }

        cs.close();
    }

    private float writeCandidatureBlock(PDPageContentStream cs, Candidature c, int index, float y) throws IOException {
        y = gap(y, 6);
        y = writeLine(cs, "Candidature #" + index, FONT_BOLD, 12, y);

        y = writeKeyValue(cs, "Nom", safe(c.getNomCandidat()), y);
        y = writeKeyValue(cs, "Email", safe(c.getEmailCandidat()), y);

        String montant = (c.getMontantPropose() == 0) ? "-" : String.valueOf(c.getMontantPropose());
        y = writeKeyValue(cs, "Montant proposé", montant, y);

        y = writeKeyValue(cs, "Statut", safe(c.getStatut()), y);

        y = writeLine(cs, "Message:", FONT_BOLD, 11, y);
        y = writeWrapped(cs, safe(c.getMessage()), FONT_REGULAR, 11, y, 95);

        return y;
    }

    // ---------------- Text helpers ----------------

    private float writeKeyValue(PDPageContentStream cs, String k, String v, float y) throws IOException {
        return writeLine(cs, k + " : " + v, FONT_REGULAR, 11, y);
    }

    private float writeLine(PDPageContentStream cs, String text, PDType1Font font, int size, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(text);
        cs.endText();
        return y - LEADING;
    }

    private float writeWrapped(PDPageContentStream cs, String text, PDType1Font font, int size, float y, int maxCharsPerLine) throws IOException {
        List<String> lines = wrap(text, maxCharsPerLine);
        for (String line : lines) {
            y = writeLine(cs, line, font, size, y);
        }
        return y;
    }

    private List<String> wrap(String text, int maxChars) {
        String t = (text == null) ? "" : text.trim();
        if (t.isEmpty()) return List.of("-");

        List<String> out = new ArrayList<>();
        while (t.length() > maxChars) {
            int cut = t.lastIndexOf(' ', maxChars);
            if (cut <= 0) cut = maxChars;
            out.add(t.substring(0, cut).trim());
            t = t.substring(cut).trim();
        }
        if (!t.isEmpty()) out.add(t);
        return out;
    }

    private float gap(float y, float px) {
        return y - px;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String formatBudget(AppelOffre ao) {
        String dev = (ao.getDevise() == null || ao.getDevise().isBlank()) ? "" : " " + ao.getDevise();
        String min = (ao.getBudgetMin() == 0) ? "-" : String.valueOf(ao.getBudgetMin());
        String max = (ao.getBudgetMax() == 0) ? "-" : String.valueOf(ao.getBudgetMax());
        return min + " - " + max + dev;
    }
}