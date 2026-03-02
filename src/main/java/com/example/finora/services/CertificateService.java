package com.example.finora.services;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CertificateService {

    /**
     * Generates a PDF certificate and saves it to the given path.
     *
     * @param studentName   Name typed by the student
     * @param lessonTitle   Title of the completed lesson
     * @param formationName Name of the formation
     * @param score         Score percentage (e.g. 80, 90, 100)
     * @param outputPath    Full path where PDF will be saved (e.g. "C:/cert.pdf")
     */
    public void generateCertificate(
            String studentName,
            String lessonTitle,
            String formationName,
            int score,
            String outputPath) throws Exception {

        Document doc = new Document(PageSize.A4.rotate()); // Landscape
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        PdfContentByte canvas = writer.getDirectContent();

        // ── Background color (soft lavender) ──────────────────────
        canvas.setColorFill(new BaseColor(245, 240, 255));
        canvas.rectangle(0, 0, doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
        canvas.fill();

        // ── Outer purple border ────────────────────────────────────
        canvas.setColorStroke(new BaseColor(124, 58, 237));
        canvas.setLineWidth(6f);
        canvas.rectangle(20, 20,
                doc.getPageSize().getWidth() - 40,
                doc.getPageSize().getHeight() - 40);
        canvas.stroke();

        // ── Inner thin border ──────────────────────────────────────
        canvas.setColorStroke(new BaseColor(196, 181, 253));
        canvas.setLineWidth(1.5f);
        canvas.rectangle(30, 30,
                doc.getPageSize().getWidth() - 60,
                doc.getPageSize().getHeight() - 60);
        canvas.stroke();

        float centerX = doc.getPageSize().getWidth() / 2;
        float pageH   = doc.getPageSize().getHeight();

        // ── FINORA brand ───────────────────────────────────────────
        Font brandFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD,
                new BaseColor(124, 58, 237));
        addCenteredText(canvas, "FINORA — GESTION FORMATION", brandFont, centerX, pageH - 70);

        // ── Decorative line ────────────────────────────────────────
        canvas.setColorStroke(new BaseColor(167, 139, 250));
        canvas.setLineWidth(1f);
        canvas.moveTo(centerX - 160, pageH - 85);
        canvas.lineTo(centerX + 160, pageH - 85);
        canvas.stroke();

        // ── "CERTIFICAT DE RÉUSSITE" ───────────────────────────────
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.BOLD,
                new BaseColor(30, 10, 60));
        addCenteredText(canvas, "CERTIFICAT DE RÉUSSITE", titleFont, centerX, pageH - 130);

        // ── "Décerné à" ────────────────────────────────────────────
        Font subtitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.ITALIC,
                new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Décerné à", subtitleFont, centerX, pageH - 175);

        // ── Student name ───────────────────────────────────────────
        Font nameFont = new Font(Font.FontFamily.TIMES_ROMAN, 42, Font.BOLDITALIC,
                new BaseColor(124, 58, 237));
        addCenteredText(canvas, studentName, nameFont, centerX, pageH - 230);

        // ── Underline under name ───────────────────────────────────
        canvas.setColorStroke(new BaseColor(167, 139, 250));
        canvas.setLineWidth(1.2f);
        canvas.moveTo(centerX - 200, pageH - 242);
        canvas.lineTo(centerX + 200, pageH - 242);
        canvas.stroke();

        // ── "pour avoir complété avec succès" ─────────────────────
        Font bodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL,
                new BaseColor(55, 65, 81));
        addCenteredText(canvas, "pour avoir complété avec succès la leçon :", bodyFont, centerX, pageH - 270);

        // ── Lesson title ───────────────────────────────────────────
        Font lessonFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD,
                new BaseColor(30, 10, 60));
        addCenteredText(canvas, "« " + lessonTitle + " »", lessonFont, centerX, pageH - 305);

        // ── Formation name ─────────────────────────────────────────
        Font formationFont = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.ITALIC,
                new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Formation : " + formationName, formationFont, centerX, pageH - 330);

        // ── Score badge area ───────────────────────────────────────
        // Draw score circle
        canvas.setColorFill(new BaseColor(124, 58, 237));
        canvas.circle(centerX, pageH - 375, 38);
        canvas.fill();

        Font scoreFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD, BaseColor.WHITE);
        addCenteredText(canvas, score + "%", scoreFont, centerX, pageH - 380);

        Font scoreLabelFont = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.NORMAL,
                new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Score obtenu", scoreLabelFont, centerX, pageH - 425);

        // ── Date ───────────────────────────────────────────────────
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy",
                java.util.Locale.FRENCH));
        Font dateFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL,
                new BaseColor(107, 90, 138));
        addCenteredText(canvas, "Délivré le " + date, dateFont, centerX, pageH - 455);

        // ── Bottom decorative line ─────────────────────────────────
        canvas.setColorStroke(new BaseColor(167, 139, 250));
        canvas.setLineWidth(1f);
        canvas.moveTo(centerX - 160, pageH - 470);
        canvas.lineTo(centerX + 160, pageH - 470);
        canvas.stroke();

        // ── Footer ─────────────────────────────────────────────────
        Font footerFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.ITALIC,
                new BaseColor(156, 163, 175));
        addCenteredText(canvas, "Finora · Plateforme de Formation en Ligne",
                footerFont, centerX, pageH - 490);

        doc.close();
    }

    private void addCenteredText(PdfContentByte canvas, String text,
                                 Font font, float x, float y) throws Exception {
        canvas.beginText();
        canvas.setFontAndSize(font.getBaseFont() != null
                        ? font.getBaseFont()
                        : BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, false),
                font.getSize());
        canvas.setColorFill(font.getColor() != null ? font.getColor() : BaseColor.BLACK);

        // Calculate text width for centering
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, false);
        float textWidth = bf.getWidthPoint(text, font.getSize());
        canvas.setTextMatrix(x - textWidth / 2, y);
        canvas.showText(text);
        canvas.endText();
    }
}