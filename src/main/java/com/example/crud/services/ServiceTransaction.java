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
            pst.setInt   (1, idAction);
            pst.setString(2, type);
            pst.setInt   (3, quantite);
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
             ResultSet rs  = stm.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction  (rs.getInt   ("id_transaction"));
                t.setIdAction       (rs.getInt   ("id_action"));
                t.setTypeTransaction(rs.getString("type_transaction"));
                t.setQuantite       (rs.getInt   ("quantite"));
                t.setPrixUnitaire   (rs.getDouble ("prix_unitaire"));
                t.setMontantTotal   (rs.getDouble ("montant_total"));
                t.setCommission     (rs.getDouble ("commission"));
                t.setDateTransaction(rs.getTimestamp("date_transaction"));

                // Action liée
                if (rs.getObject("a.id_action") != null || rs.getObject("symbole") != null) {
                    Action action = new Action();
                    action.setIdAction         (rs.getInt   ("a.id_action"));
                    action.setSymbole          (rs.getString("symbole"));
                    action.setNomEntreprise    (rs.getString("nom_entreprise"));
                    action.setSecteur          (rs.getString("secteur"));
                    action.setPrixUnitaire     (rs.getDouble ("prix_action"));
                    action.setQuantiteDisponible(rs.getInt  ("quantite_disponible"));
                    action.setStatut           (rs.getString("statut_action"));

                    // Bourse liée
                    if (rs.getObject("id_bourse") != null) {
                        Bourse bourse = new Bourse();
                        bourse.setIdBourse  (rs.getInt   ("id_bourse"));
                        bourse.setNomBourse (rs.getString("nom_bourse"));
                        bourse.setPays      (rs.getString("pays"));
                        bourse.setDevise    (rs.getString("devise"));
                        bourse.setStatut    (rs.getString("statut_bourse"));
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
             ResultSet rs  = stm.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────
//  ACHAT / VENTE (méthodes attendues par TradingController)
// ─────────────────────────────────────────────────────────

    /**
     * Achète une quantité d'une action :
     * - vérifie stock disponible
     * - met à jour la quantité_disponible
     * - enregistre la transaction (ACHAT) avec commission
     */
    public Transaction acheter(int idAction, int quantite, String role, String displayName) {
        // role/displayName pas encore stockés dans ta table -> on les garde pour plus tard
        return executerTrade(idAction, "ACHAT", quantite);
    }

    /**
     * Vend une quantité d'une action :
     * - augmente quantité_disponible
     * - enregistre la transaction (VENTE) avec commission
     */
    public Transaction vendre(int idAction, int quantite, String role, String displayName) {
        // role/displayName pas encore stockés dans ta table -> on les garde pour plus tard
        return executerTrade(idAction, "VENTE", quantite);
    }

    /**
     * Exécute un trade avec transaction SQL.
     */
    private Transaction executerTrade(int idAction, String type, int quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("Quantité invalide.");

        String sqlPrixStock = "SELECT prix_unitaire, quantite_disponible FROM action WHERE id_action = ?";
        String sqlUpdateStock = "UPDATE action SET quantite_disponible = ? WHERE id_action = ?";

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

            // 2) Calcul du nouveau stock selon type
            int newStock;
            if ("ACHAT".equalsIgnoreCase(type)) {
                if (stock < quantite) {
                    throw new IllegalStateException("Stock insuffisant. Disponible=" + stock + ", demandé=" + quantite);
                }
                newStock = stock - quantite;
            } else if ("VENTE".equalsIgnoreCase(type)) {
                // Si tu veux empêcher la vente sans portefeuille, il faut vérifier un portefeuille (table holdings).
                // Ici on autorise et on remet en stock.
                newStock = stock + quantite;
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

            // 4) Enregistrer transaction (utilise déjà commission dynamique)
            Transaction t = enregistrer(idAction, type.toUpperCase(), quantite, prixUnitaire);
            if (t == null) {
                throw new SQLException("Transaction non enregistrée (retour null).");
            }

            // 5) Commit
            connection.commit();
            return t;

        } catch (Exception ex) {
            try { connection.rollback(); } catch (SQLException ignore) {}
            System.err.println("❌ Erreur trade " + type + " : " + ex.getMessage());
            ex.printStackTrace();
            return null;

        } finally {
            try {
                connection.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignore) {}
        }
    }

}