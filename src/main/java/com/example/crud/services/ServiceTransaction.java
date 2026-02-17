package com.example.crud.services;

import com.example.crud.models.Action;
import com.example.crud.models.Bourse;
import com.example.crud.models.Transaction;
import com.example.crud.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 📜 ServiceTransaction
 * Gère la persistance de l'historique des transactions
 */
public class ServiceTransaction {

    private static final double TAUX_COMMISSION = 0.005; // 0.5%

    private final Connection connection;

    public ServiceTransaction() {
        this.connection = Database.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────
    //  ENREGISTRER une transaction (achat ou vente)
    // ─────────────────────────────────────────────────────────

    /**
     * Version compatible avec ta table `transaction` (sans colonne commission).
     */
    public Transaction enregistrer(int idAction, String type, int quantite, double prixUnitaire,
                                   String acteurRole, String acteurLabel) {

        double montantTotal = prixUnitaire * quantite;
        double commissionCalculee = Math.round(montantTotal * TAUX_COMMISSION * 100.0) / 100.0; // calcul UI/log

        String sql = "INSERT INTO transaction " +
                "(id_action, type_transaction, quantite, prix_unitaire, montant_total, date_transaction, acteur_role, acteur_label) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, idAction);
            pst.setString(2, type);
            pst.setInt(3, quantite);
            pst.setDouble(4, prixUnitaire);
            pst.setDouble(5, montantTotal);
            pst.setString(6, acteurRole);
            pst.setString(7, acteurLabel);

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                Transaction t = new Transaction(idAction, type, quantite, prixUnitaire, commissionCalculee);
                if (rs.next()) t.setIdTransaction(rs.getInt(1));
                System.out.println("✅ Transaction enregistrée : " + t);
                return t;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur enregistrement transaction : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  ACHETER / VENDRE (update action + insert transaction)
    // ─────────────────────────────────────────────────────────

    public void acheter(int idAction, int quantite, String role, String acteur) throws SQLException {
        if (quantite <= 0) throw new IllegalArgumentException("Quantité invalide");

        String select = "SELECT prix_unitaire, quantite_disponible FROM action WHERE id_action = ?";
        String update = "UPDATE action SET quantite_disponible = quantite_disponible - ? WHERE id_action = ?";

        connection.setAutoCommit(false);
        try (PreparedStatement psSel = connection.prepareStatement(select);
             PreparedStatement psUpd = connection.prepareStatement(update)) {

            psSel.setInt(1, idAction);
            try (ResultSet rs = psSel.executeQuery()) {
                if (!rs.next()) throw new SQLException("Action introuvable (id=" + idAction + ")");

                double prix = rs.getDouble("prix_unitaire");
                int dispo = rs.getInt("quantite_disponible");

                if (dispo < quantite) {
                    throw new SQLException("Stock insuffisant. Disponible: " + dispo);
                }

                psUpd.setInt(1, quantite);
                psUpd.setInt(2, idAction);
                psUpd.executeUpdate();

                // insert transaction (table `transaction`)
                enregistrer(idAction, "ACHAT", quantite, prix, role, acteur);
            }

            connection.commit();
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void vendre(int idAction, int quantite, String role, String acteur) throws SQLException {
        if (quantite <= 0) throw new IllegalArgumentException("Quantité invalide");

        String select = "SELECT prix_unitaire FROM action WHERE id_action = ?";
        String update = "UPDATE action SET quantite_disponible = quantite_disponible + ? WHERE id_action = ?";

        connection.setAutoCommit(false);
        try (PreparedStatement psSel = connection.prepareStatement(select);
             PreparedStatement psUpd = connection.prepareStatement(update)) {

            psSel.setInt(1, idAction);
            try (ResultSet rs = psSel.executeQuery()) {
                if (!rs.next()) throw new SQLException("Action introuvable (id=" + idAction + ")");

                double prix = rs.getDouble("prix_unitaire");

                psUpd.setInt(1, quantite);
                psUpd.setInt(2, idAction);
                psUpd.executeUpdate();

                enregistrer(idAction, "VENTE", quantite, prix, role, acteur);
            }

            connection.commit();
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  LIRE transactions
    // ─────────────────────────────────────────────────────────

    public List<Transaction> getAll() {
        return requeteTransactions("", "ORDER BY t.date_transaction DESC");
    }

    public List<Transaction> getByType(String type) {
        return requeteTransactions("WHERE t.type_transaction = ?", "ORDER BY t.date_transaction DESC", type);
    }

    public List<Transaction> getByAction(int idAction) {
        return requeteTransactions("WHERE t.id_action = ?", "ORDER BY t.date_transaction DESC", idAction);
    }

    // ─────────────────────────────────────────────────────────
    //  STATISTIQUES (commission = 0 car colonne inexistante)
    // ─────────────────────────────────────────────────────────

    public double getTotalInvesti() {
        return somme("SELECT COALESCE(SUM(montant_total),0) FROM transaction WHERE type_transaction='ACHAT'");
    }

    public double getTotalVendu() {
        return somme("SELECT COALESCE(SUM(montant_total),0) FROM transaction WHERE type_transaction='VENTE'");
    }

    public double getTotalCommissions() {
        // DB n'a pas commission → on peut calculer depuis montant_total
        return somme("SELECT COALESCE(SUM(montant_total)*" + TAUX_COMMISSION + ",0) FROM transaction");
    }

    public int getNombreTransactions() {
        return (int) somme("SELECT COUNT(*) FROM transaction");
    }

    // ─────────────────────────────────────────────────────────
    //  HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────

    private List<Transaction> requeteTransactions(String where, String orderBy, Object... params) {
        List<Transaction> list = new ArrayList<>();

        String sql =
                "SELECT t.id_transaction, t.id_action, t.type_transaction, t.quantite, t.prix_unitaire, t.montant_total, " +
                        "t.date_transaction, t.acteur_role, t.acteur_label, " +
                        "a.symbole, a.nom_entreprise, a.secteur, a.prix_unitaire AS prix_action, a.quantite_disponible, a.statut AS statut_action, " +
                        "b.id_bourse, b.nom_bourse, b.pays, b.devise, b.statut AS statut_bourse " +
                        "FROM transaction t " +
                        "LEFT JOIN action a ON t.id_action = a.id_action " +
                        "LEFT JOIN bourse b ON a.id_bourse = b.id_bourse " +
                        " " + where + " " + orderBy;

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setIdTransaction(rs.getInt("id_transaction"));
                    t.setIdAction(rs.getInt("id_action"));
                    t.setTypeTransaction(rs.getString("type_transaction"));
                    t.setQuantite(rs.getInt("quantite"));
                    t.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                    t.setMontantTotal(rs.getDouble("montant_total"));

                    // commission non existante en DB → on calcule
                    double commissionCalc = Math.round(t.getMontantTotal() * TAUX_COMMISSION * 100.0) / 100.0;
                    t.setCommission(commissionCalc);

                    t.setDateTransaction(rs.getTimestamp("date_transaction"));

                    // Action liée
                    if (rs.getObject("symbole") != null) {
                        Action action = new Action();
                        action.setIdAction(rs.getInt("id_action"));
                        action.setSymbole(rs.getString("symbole"));
                        action.setNomEntreprise(rs.getString("nom_entreprise"));
                        action.setSecteur(rs.getString("secteur"));
                        action.setPrixUnitaire(rs.getDouble("prix_action"));
                        action.setQuantiteDisponible(rs.getInt("quantite_disponible"));
                        action.setStatut(rs.getString("statut_action"));

                        // Bourse liée
                        if (rs.getObject("id_bourse") != null) {
                            Bourse bourse = new Bourse();
                            bourse.setIdBourse(rs.getInt("id_bourse"));
                            bourse.setNomBourse(rs.getString("nom_bourse"));
                            bourse.setPays(rs.getString("pays"));
                            bourse.setDevise(rs.getString("devise"));
                            bourse.setStatut(rs.getString("statut_bourse"));
                            action.setBourse(bourse);
                        }

                        t.setAction(action);
                    }

                    list.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture transactions : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    private double somme(String sql) {
        try (Statement stm = connection.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
