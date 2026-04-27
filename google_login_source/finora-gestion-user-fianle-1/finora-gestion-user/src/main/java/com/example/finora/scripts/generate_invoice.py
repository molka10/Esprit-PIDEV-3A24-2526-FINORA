#!/usr/bin/env python3
"""
📄 Générateur de Factures FINORA
Crée des factures PDF professionnelles pour les transactions boursières
"""

import argparse
import os
from datetime import datetime

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import cm
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER


def to_float(s: str) -> float:
    """Convertit '872,50' ou '872.50' en float."""
    return float(str(s).strip().replace(" ", "").replace(",", "."))


def generer_facture(args):
    """Génère une facture PDF professionnelle"""

    # Créer le dossier de sortie si nécessaire
    out_dir = os.path.dirname(args.output)
    if out_dir:
        os.makedirs(out_dir, exist_ok=True)

    # Configuration du document
    doc = SimpleDocTemplate(
        args.output,
        pagesize=A4,
        rightMargin=2 * cm,
        leftMargin=2 * cm,
        topMargin=2 * cm,
        bottomMargin=2 * cm
    )

    # Styles
    styles = getSampleStyleSheet()

    style_titre = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=24,
        textColor=colors.HexColor('#6366f1'),
        spaceAfter=12,
        alignment=TA_CENTER,
        fontName='Helvetica-Bold'
    )

    style_sous_titre = ParagraphStyle(
        'SubTitle',
        parent=styles['Normal'],
        fontSize=11,
        textColor=colors.grey,
        alignment=TA_CENTER,
        spaceAfter=30
    )

    style_section = ParagraphStyle(
        'Section',
        parent=styles['Heading2'],
        fontSize=14,
        textColor=colors.HexColor('#1f2937'),
        spaceAfter=12,
        spaceBefore=20,
        fontName='Helvetica-Bold'
    )

    style_footer = ParagraphStyle(
        'Footer',
        parent=styles['Normal'],
        fontSize=9,
        textColor=colors.grey,
        alignment=TA_CENTER,
        leading=12
    )

    # Contenu du PDF
    story = []

    # ═══════════════════════════════════════════════════════
    # HEADER - Titre
    # ═══════════════════════════════════════════════════════
    titre_type = "CONFIRMATION D'ACHAT" if args.type == "ACHAT" else "CONFIRMATION DE VENTE"

    story.append(Paragraph("📊 FINORA", style_titre))
    story.append(Paragraph("Plateforme de Trading Boursier", style_sous_titre))
    story.append(Spacer(1, 0.5 * cm))

    # Badge type transaction (Table -> stable, texte toujours visible)
    type_color = colors.HexColor('#10b981') if args.type == "ACHAT" else colors.HexColor('#ef4444')

    badge = Table([[titre_type]], colWidths=[16 * cm])
    badge.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), type_color),
        ('TEXTCOLOR', (0, 0), (-1, -1), colors.white),
        ('FONTNAME', (0, 0), (-1, -1), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 14),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('TOPPADDING', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
        ('LEFTPADDING', (0, 0), (-1, -1), 0),
        ('RIGHTPADDING', (0, 0), (-1, -1), 0),
    ]))

    story.append(badge)
    story.append(Spacer(1, 1 * cm))

    # ═══════════════════════════════════════════════════════
    # INFORMATIONS TRANSACTION
    # ═══════════════════════════════════════════════════════
    story.append(Paragraph("Détails de la Transaction", style_section))

    data_info = [
        ['Numéro de Transaction', f'#{args.id}'],
        ['Date et Heure', args.date],
        ["Type d'Opération", args.type],
        ['', '']
    ]

    table_info = Table(data_info, colWidths=[8 * cm, 9 * cm])
    table_info.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (0, -1), colors.HexColor('#f3f4f6')),
        ('TEXTCOLOR', (0, 0), (0, -1), colors.HexColor('#6b7280')),
        ('TEXTCOLOR', (1, 0), (1, -1), colors.HexColor('#1f2937')),
        ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
        ('FONTNAME', (1, 0), (1, -1), 'Helvetica'),
        ('FONTSIZE', (0, 0), (-1, -1), 11),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
        ('TOPPADDING', (0, 0), (-1, -1), 12),
        ('GRID', (0, 0), (-1, -2), 0.5, colors.HexColor('#e5e7eb')),
    ]))

    story.append(table_info)
    story.append(Spacer(1, 0.8 * cm))

    # ═══════════════════════════════════════════════════════
    # DÉTAILS ACTION
    # ═══════════════════════════════════════════════════════
    story.append(Paragraph("Action Concernée", style_section))

    data_action = [
        ['Symbole', args.symbole],
        ["Nom de l'Entreprise", args.nom],
        ['Quantité', f'{args.quantite} actions'],
        ['Prix Unitaire', f'{args.prix} {args.devise}'],
        ['', '']
    ]

    table_action = Table(data_action, colWidths=[8 * cm, 9 * cm])
    table_action.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (0, -1), colors.HexColor('#f3f4f6')),
        ('TEXTCOLOR', (0, 0), (0, -1), colors.HexColor('#6b7280')),
        ('TEXTCOLOR', (1, 0), (1, -1), colors.HexColor('#1f2937')),
        ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
        ('FONTNAME', (1, 0), (1, -1), 'Helvetica'),
        ('FONTSIZE', (0, 0), (-1, -1), 11),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
        ('TOPPADDING', (0, 0), (-1, -1), 12),
        ('GRID', (0, 0), (-1, -2), 0.5, colors.HexColor('#e5e7eb')),
    ]))

    story.append(table_action)
    story.append(Spacer(1, 0.8 * cm))

    # ═══════════════════════════════════════════════════════
    # RÉCAPITULATIF FINANCIER
    # ═══════════════════════════════════════════════════════
    story.append(Paragraph("Récapitulatif Financier", style_section))

    total = to_float(args.total)
    commission = to_float(args.commission)
    montant_net = total + commission if args.type == "ACHAT" else total - commission

    data_financier = [
        ['Montant Total', f'{total:.2f} {args.devise}'],
        ['Commission FINORA (0.5%)', f'{commission:.2f} {args.devise}'],
        ['', ''],
        ['Montant Net à Payer' if args.type == "ACHAT" else 'Montant Net Reçu',
         f'{montant_net:.2f} {args.devise}']
    ]

    table_financier = Table(data_financier, colWidths=[12 * cm, 5 * cm])
    table_financier.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (0, 1), colors.HexColor('#f9fafb')),
        ('BACKGROUND', (0, 3), (-1, 3), colors.HexColor('#6366f1')),
        ('TEXTCOLOR', (0, 0), (-1, 2), colors.HexColor('#1f2937')),
        ('TEXTCOLOR', (0, 3), (-1, 3), colors.white),
        ('FONTNAME', (0, 0), (-1, 2), 'Helvetica'),
        ('FONTNAME', (0, 3), (-1, 3), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 2), 11),
        ('FONTSIZE', (0, 3), (-1, 3), 14),
        ('ALIGN', (1, 0), (1, -1), 'RIGHT'),
        ('ALIGN', (0, 0), (0, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
        ('TOPPADDING', (0, 0), (-1, -1), 12),
        ('LINEABOVE', (0, 3), (-1, 3), 2, colors.HexColor('#6366f1')),
        ('GRID', (0, 0), (-1, 1), 0.5, colors.HexColor('#e5e7eb')),
    ]))

    story.append(table_financier)
    story.append(Spacer(1, 1.5 * cm))

    # ═══════════════════════════════════════════════════════
    # FOOTER - Notes légales
    # ═══════════════════════════════════════════════════════
    story.append(Spacer(1, 1 * cm))
    story.append(Paragraph("─" * 80, style_footer))
    story.append(Spacer(1, 0.3 * cm))

    footer_text = (
        "<b>FINORA - Plateforme de Trading Boursier</b><br/>"
        "Ce document constitue une confirmation officielle de votre transaction.<br/>"
        "Conservez-le précieusement pour vos archives et déclarations fiscales.<br/><br/>"
        "Pour toute question : contact@finora.tn | +216 XX XXX XXX<br/>"
        "Document généré automatiquement le "
        + datetime.now().strftime("%d/%m/%Y à %H:%M")
    )

    story.append(Paragraph(footer_text, style_footer))

    # ═══════════════════════════════════════════════════════
    # GÉNÉRATION
    # ═══════════════════════════════════════════════════════
    doc.build(story)
    print(f"Facture générée : {args.output}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Générateur de factures FINORA')
    parser.add_argument('--output', required=True, help='Chemin du fichier PDF')
    parser.add_argument('--type', required=True, help='ACHAT ou VENTE')
    parser.add_argument('--symbole', required=True, help="Symbole de l'action")
    parser.add_argument('--nom', required=True, help="Nom de l'entreprise")
    parser.add_argument('--quantite', required=True, help='Quantité')
    parser.add_argument('--prix', required=True, help='Prix unitaire')
    parser.add_argument('--total', required=True, help='Montant total')
    parser.add_argument('--commission', required=True, help='Commission')
    parser.add_argument('--devise', required=True, help='Devise')
    parser.add_argument('--date', required=True, help='Date de la transaction')
    parser.add_argument('--id', required=True, help='ID de la transaction')

    args = parser.parse_args()
    generer_facture(args)
