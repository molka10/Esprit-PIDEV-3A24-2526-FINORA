package com.example.crud.services;

import com.example.crud.models.Transaction;
import com.example.crud.utils.Database;

import java.sql.*;



public class ServiceTransaction {

    private final Connection cnx;

    public ServiceTransaction() {
        cnx = Database.getInstance().getConnection();
    }

    public void acheter(int idAction, int quantite, String acteurRole, String acteurLabel) {
        executerTrade("ACHAT", idAction, quantite, acteurRole, acteurLabel);
    }

    public void vendre(int idAction, int quantite, String acteurRole, String acteurLabel) {
        executerTrade("VENTE", idAction, quantite, acteurRole, acteurLabel);
    }

    private void executerTrade(String type, int idAction, int quantite, String acteurRole, String acteurLabel) {
        if (quantite <= 0) throw new RuntimeException("Quantité doit être > 0");

        String selectForUpdate = "SELECT quantite_disponible, prix_unitaire FROM action WHERE id_action=? FOR UPDATE";
        String updateQty = "UPDATE action SET quantite_disponible=? WHERE id_action=?";
        String insertTxn = "INSERT INTO transaction(type_transaction, quantite, prix_unitaire, montant_total, id_action, acteur_role, acteur_label) " +
                "VALUES (?,?,?,?,?,?,?)";

        boolean oldAutoCommit;
        try {
            oldAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            int qteDispo;
            double prix;

            // 1) lock row
            try (PreparedStatement ps = cnx.prepareStatement(selectForUpdate)) {
                ps.setInt(1, idAction);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Action introuvable (id=" + idAction + ")");
                    }
                    qteDispo = rs.getInt("quantite_disponible");
                    prix = rs.getDouble("prix_unitaire");
                }
            }

            // 2) calc new qty
            int newQty;
            if ("ACHAT".equals(type)) {
                if (qteDispo < quantite) {
                    throw new RuntimeException("Stock insuffisant. Dispo=" + qteDispo + ", demandé=" + quantite);
                }
                newQty = qteDispo - quantite;
            } else { // VENTE
                newQty = qteDispo + quantite;
            }

            // 3) update qty
            try (PreparedStatement ps = cnx.prepareStatement(updateQty)) {
                ps.setInt(1, newQty);
                ps.setInt(2, idAction);
                ps.executeUpdate();
            }

            // 4) insert transaction
            double total = quantite * prix;
            try (PreparedStatement ps = cnx.prepareStatement(insertTxn)) {
                ps.setString(1, type);
                ps.setInt(2, quantite);
                ps.setDouble(3, prix);
                ps.setDouble(4, total);
                ps.setInt(5, idAction);
                ps.setString(6, acteurRole);
                ps.setString(7, acteurLabel);
                ps.executeUpdate();
            }

            cnx.commit();
            cnx.setAutoCommit(oldAutoCommit);

        } catch (Exception e) {
            try { cnx.rollback(); } catch (SQLException ignored) {}
            try { cnx.setAutoCommit(true); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur trade (" + type + ") : " + e.getMessage(), e);
        }
    }
}
