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

    private final Connection connection;
    private final Servicecommission serviceCommission;

    // ✅ User statique pour le moment
    private static final String STATIC_USER = "USER_STATIC";

    public ServiceTransaction() {
        this.connection = Database.getInstance().getConnection();
        this.serviceCommission = new Servicecommission();
    }

    // ─────────────────────────────────────────────────────────
    //  ENREGISTRER une transaction (achat ou vente)
    // ─────────────────────────────────────────────────────────

    /**
     * Enregistre une transaction en base et retourne l'objet créé.
     * La commission est calculée dynamiquement depuis la table commission.
     */
    public Transaction enregistrer(int idAction, String type, int quantite, double prixUnitaire) {
        double montantTotal = prixUnitaire * quantite;

        // ✅ Récupérer le taux dynamique depuis la base
        double tauxPourcentage = serviceCommission.getTauxActif(type);
        double commission = Math.round(montantTotal * (tauxPourcentage / 100.0) * 100.0) / 100.0;

        String sql = "INSERT INTO transaction " +
                "(id_action, type_transaction, quantite, prix_unitaire, montant_total, commission) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, idAction);
            pst.setString(2, type);
            pst.setInt(3, quantite);
            pst.setDouble(4, prixUnitaire);
            pst.setDouble(5, montantTotal);
            pst.setDouble(6, commission);
            pst.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = pst.getGeneratedKeys()) {
                Transaction t = new Transaction(idAction, type, quantite, prixUnitaire, commission);
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
    //  LIRE toutes les transactions
    // ─────────────────────────────────────────────────────────

    public List<Transaction> getAll() {
        return requeteTransactions("ORDER BY t.date_transaction DESC");
    }

    public List<Transaction> getByType(String type) {
        return requeteTransactions("WHERE t.type_transaction = '" + type + "' ORDER BY t.date_transaction DESC");
    }

    public List<Transaction> getByAction(int idAction) {
        return requeteTransactions("WHERE t.id_action = " + idAction + " ORDER BY t.date_transaction DESC");
    }

    // ─────────────────────────────────────────────────────────
    //  STATISTIQUES
    // ─────────────────────────────────────────────────────────

    public double getTotalInvesti() {
        return somme("SELECT COALESCE(SUM(montant_total),0) FROM transaction WHERE type_transaction='ACHAT'");
    }

    public double getTotalVendu() {
        return somme("SELECT COALESCE(SUM(montant_total),0) FROM transaction WHERE type_transaction='VENTE'");
    }

    public double getTotalCommissions() {
        return somme("SELECT COALESCE(SUM(commission),0) FROM transaction");
    }

    public int getNombreTransactions() {
        return (int) somme("SELECT COUNT(*) FROM transaction");
    }

    // ─────────────────────────────────────────────────────────
    //  HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────

    private List<Transaction> requeteTransactions(String whereOrder) {
        List<Transaction> list = new ArrayList<>();

        String sql = "SELECT t.*, " +
                "a.id_action, a.symbole, a.nom_entreprise, a.secteur, " +
                "a.prix_unitaire AS prix_action, a.quantite_disponible, a.statut AS statut_action, " +
                "b.id_bourse, b.nom_bourse, b.pays, b.devise, b.statut AS statut_bourse " +
                "FROM transaction t " +
                "LEFT JOIN action a ON t.id_action = a.id_action " +
                "LEFT JOIN bourse b ON a.id_bourse = b.id_bourse " +
                whereOrder;

        try (Statement stm = connection.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setIdAction(rs.getInt("id_action"));
                t.setTypeTransaction(rs.getString("type_transaction"));
                t.setQuantite(rs.getInt("quantite"));
                t.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                t.setMontantTotal(rs.getDouble("montant_total"));
                t.setCommission(rs.getDouble("commission"));
                t.setDateTransaction(rs.getTimestamp("date_transaction"));

                // Action liée
                if (rs.getObject("a.id_action") != null || rs.getObject("symbole") != null) {
                    Action action = new Action();
                    action.setIdAction(rs.getInt("a.id_action"));
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

    // ─────────────────────────────────────────────────────────
    //  PORTFEUILLE (user statique)
    // ─────────────────────────────────────────────────────────

    public int getQuantitePossedee(String displayName, int idAction) {
        String sql = "SELECT quantite FROM portefeuille WHERE display_name = ? AND id_action = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, STATIC_USER);
            pst.setInt(2, idAction);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt("quantite") : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  ACHAT / VENTE
    // ─────────────────────────────────────────────────────────

    public Transaction acheter(int idAction, int quantite, String role, String displayName) throws Exception {
        return executerTrade(idAction, "ACHAT", quantite);
    }

    public Transaction vendre(int idAction, int quantite, String role, String displayName) throws Exception {
        return executerTrade(idAction, "VENTE", quantite);
    }

    /**
     * Exécute un trade avec transaction SQL + portefeuille.
     * ✅ ACHAT : décrémente stock + ajoute au portefeuille
     * ✅ VENTE : vérifie portefeuille (bloque si insuffisant) + remet au stock
     *
     * Important: on PROPAGE l'erreur (throw) au Controller pour afficher un message propre.
     */
    private Transaction executerTrade(int idAction, String type, int quantite) throws Exception {
        if (quantite <= 0) throw new IllegalArgumentException("Quantité invalide.");

        String sqlPrixStock = "SELECT prix_unitaire, quantite_disponible FROM action WHERE id_action = ?";

        String sqlUpdateStock = "UPDATE action SET quantite_disponible = ? WHERE id_action = ?";

        String sqlUpsertPortefeuille =
                "INSERT INTO portefeuille (display_name, id_action, quantite) " +
                        "VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE quantite = quantite + VALUES(quantite)";

        String sqlUpdatePortefeuilleVente =
                "UPDATE portefeuille SET quantite = quantite - ? " +
                        "WHERE display_name = ? AND id_action = ? AND quantite >= ?";

        String sqlCleanupPortefeuille =
                "DELETE FROM portefeuille WHERE display_name = ? AND id_action = ? AND quantite <= 0";

        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            double prixUnitaire;
            int stock;

            // 1) Lire prix + stock
            try (PreparedStatement pst = connection.prepareStatement(sqlPrixStock)) {
                pst.setInt(1, idAction);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Action introuvable (id=" + idAction + ")");
                    }
                    prixUnitaire = rs.getDouble("prix_unitaire");
                    stock = rs.getInt("quantite_disponible");
                }
            }

            int newStock;

            // 2) ACHAT / VENTE
            if ("ACHAT".equalsIgnoreCase(type)) {
                if (stock < quantite) {
                    throw new IllegalStateException("Stock insuffisant. Disponible=" + stock + ", demandé=" + quantite);
                }
                newStock = stock - quantite;

                // ✅ Ajouter au portefeuille
                try (PreparedStatement pst = connection.prepareStatement(sqlUpsertPortefeuille)) {
                    pst.setString(1, STATIC_USER);
                    pst.setInt(2, idAction);
                    pst.setInt(3, quantite);
                    pst.executeUpdate();
                }

            } else if ("VENTE".equalsIgnoreCase(type)) {

                // ✅ Bloquer la vente si pas assez dans portefeuille
                try (PreparedStatement pst = connection.prepareStatement(sqlUpdatePortefeuilleVente)) {
                    pst.setInt(1, quantite);
                    pst.setString(2, STATIC_USER);
                    pst.setInt(3, idAction);
                    pst.setInt(4, quantite);
                    int updated = pst.executeUpdate();
                    if (updated != 1) {
                        throw new IllegalStateException("Vente impossible : quantité insuffisante dans ton portefeuille.");
                    }
                }

                newStock = stock + quantite;

                // cleanup si 0
                try (PreparedStatement pst = connection.prepareStatement(sqlCleanupPortefeuille)) {
                    pst.setString(1, STATIC_USER);
                    pst.setInt(2, idAction);
                    pst.executeUpdate();
                }

            } else {
                throw new IllegalArgumentException("Type transaction invalide: " + type);
            }

            // 3) Update stock
            try (PreparedStatement pst = connection.prepareStatement(sqlUpdateStock)) {
                pst.setInt(1, newStock);
                pst.setInt(2, idAction);
                int updated = pst.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("Échec mise à jour stock action (id=" + idAction + ")");
                }
            }

            // 4) Enregistrer transaction (inchangé)
            Transaction t = enregistrer(idAction, type.toUpperCase(), quantite, prixUnitaire);
            if (t == null) {
                throw new SQLException("Transaction non enregistrée (retour null).");
            }

            connection.commit();
            return t;

        } catch (Exception ex) {
            try { connection.rollback(); } catch (SQLException ignore) {}
            System.err.println("❌ Erreur trade " + type + " : " + ex.getMessage());
            throw ex; // ✅ IMPORTANT : propager au controller

        } finally {
            try {
                connection.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignore) {}
        }
    }
}
