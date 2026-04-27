package com.example.finora.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CertificateService {

    /**
     * Generates a PDF certificate and saves it to outputPath.
     */
    public File generateCertificate(
            String userName,
            String lessonTitle,
            String formationName,
            int score,
            String outputPath
    ) throws Exception {

        // ✅ fallback changed: "Utilisateur" instead of "Student"
        String safeUser = safe(userName, "Utilisateur");
        String safeLesson = safe(lessonTitle, "");
        String safeFormation = safe(formationName, "");

        File outFile = new File(outputPath);

        // Ensure directory exists
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        Document doc = new Document(PageSize.A4.rotate()); // Landscape
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outFile));
        doc.open();

        PdfContentByte canvas = writer.getDirectContent();

        float pageW = doc.getPageSize().getWidth();
        float pageH = doc.getPageSize().getHeight();
        float centerX = pageW / 2f;

        // ── Background color (soft lavender)
        canvas.setColorFill(new BaseColor(245, 240, 255));
        canvas.rectangle(0, 0, pageW, pageH);
        canvas.fill();

        // ── Outer purple border
        canvas.setColorStroke(new BaseColor(124, 58, 237));
        canvas.setLineWidth(6f);
        canvas.rectangle(20, 20, pageW - 40, pageH - 40);
        canvas.stroke();

        // ── Inner thin border
        canvas.setColorStroke(new BaseColor(196, 181, 253));
        canvas.setLineWidth(1.5f);
        canvas.rectangle(30, 30, pageW - 60, pageH - 60);
        canvas.stroke();

        // ── FINORA brand
        Font brandFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, new BaseColor(124, 58, 237));
        addCenteredText(canvas, "FINORA — GESTION FORMATION", brandFont, centerX, pageH - 70);

        // ── Decorative line
        canvas.setColorStroke(new BaseColor(167, 139, 250));
        canvas.setLineWidth(1f);
        canvas.moveTo(centerX - 160, pageH - 85);
        canvas.lineTo(centerX + 160, pageH - 85);
        canvas.stroke();

        // ── Title
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.BOLD, new BaseColor(30, 10, 60));
        addCenteredText(canvas, "CERTIFICAT DE RÉUSSITE", titleFont, centerX, pageH - 130);

        // ── Subtitle
        Font subtitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.ITALIC, new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Décerné à", subtitleFont, centerX, pageH - 175);

        // ── User name (was Student)
        Font nameFont = new Font(Font.FontFamily.TIMES_ROMAN, 42, Font.BOLDITALIC, new BaseColor(124, 58, 237));
        addCenteredText(canvas, safeUser, nameFont, centerX, pageH - 230);

        // ── Underline
        canvas.setColorStroke(new BaseColor(167, 139, 250));
        canvas.setLineWidth(1.2f);
        canvas.moveTo(centerX - 200, pageH - 242);
        canvas.lineTo(centerX + 200, pageH - 242);
        canvas.stroke();

        // ── Body
        Font bodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL, new BaseColor(55, 65, 81));
        addCenteredText(canvas, "pour avoir complété avec succès la leçon :", bodyFont, centerX, pageH - 270);

        // ── Lesson title
        Font lessonFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD, new BaseColor(30, 10, 60));
        addCenteredText(canvas, "« " + safeLesson + " »", lessonFont, centerX, pageH - 305);

        // ── Formation name
        Font formationFont = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.ITALIC, new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Formation : " + safeFormation, formationFont, centerX, pageH - 330);

        // ── Score circle
        canvas.setColorFill(new BaseColor(124, 58, 237));
        canvas.circle(centerX, pageH - 375, 38);
        canvas.fill();

        Font scoreFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD, BaseColor.WHITE);
        addCenteredText(canvas, score + "%", scoreFont, centerX, pageH - 380);

        Font scoreLabelFont = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.NORMAL, new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Score obtenu", scoreLabelFont, centerX, pageH - 425);

        // ── Date
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        Font dateFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Délivré le " + date, dateFont, centerX, pageH - 455);

        // ── Bottom decorative line
        canvas.setColorStroke(new BaseColor(167, 139, 250));
        canvas.setLineWidth(1f);
        canvas.moveTo(centerX - 160, pageH - 470);
        canvas.lineTo(centerX + 160, pageH - 470);
        canvas.stroke();

        // ── Footer
        Font footerFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.ITALIC, new BaseColor(156, 163, 175));
        addCenteredText(canvas, "Finora · Plateforme de Formation en Ligne", footerFont, centerX, pageH - 490);

        doc.close();

        return outFile;
    }

    public static void openFile(File file) {
        try {
            if (file != null && file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {}
    }

    private static String safe(String s, String fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        return t.isEmpty() ? fallback : t;
    }

    /**
     * uses the SAME base font to measure width and draw text
     */
    private void addCenteredText(PdfContentByte canvas, String text, Font font, float x, float y) throws Exception {
        if (text == null) text = "";

        BaseFont bf = font.getBaseFont();
        if (bf == null) {
            bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, false);
        }

        canvas.beginText();
        canvas.setFontAndSize(bf, font.getSize());
        canvas.setColorFill(font.getColor() != null ? font.getColor() : BaseColor.BLACK);

        float textWidth = bf.getWidthPoint(text, font.getSize());
        canvas.setTextMatrix(x - textWidth / 2f, y);
        canvas.showText(text);
        canvas.endText();
    }
}