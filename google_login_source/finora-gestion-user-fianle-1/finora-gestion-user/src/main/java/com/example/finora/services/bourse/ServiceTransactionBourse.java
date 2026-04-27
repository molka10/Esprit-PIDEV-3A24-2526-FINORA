package com.example.finora.services.bourse;

import com.example.finora.entities.Action;
import com.example.finora.entities.Bourse;
import com.example.finora.entities.TransactionBourse;
import com.example.finora.services.WalletBridge;
import com.example.finora.utils.DBConnection;
import com.example.finora.utils.Session;

import java.sql.*;
import java.time.LocalDateTime; // ✅ AJOUT
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ ServiceTransactionBourse (ANCIEN / CRUD) - CORRIGÉ
 * - transaction_bourse
 * - portefeuille(user_id, id_action, quantite)
 * - acteur_role ENUM compatible (évite "Data truncated")
 * - ✅ Overloads pour supporter:
 * acheter(idAction, quantite, role, displayName) (anciens controllers)
 * acheter(userId, idAction, quantite, role, label) (nouveaux controllers)
 */
public class ServiceTransactionBourse {

    private final Servicecommission serviceCommission;

    public ServiceTransactionBourse() {
        this.serviceCommission = new Servicecommission();
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────
    // NORMALISATION ROLE (évite Data truncated acteur_role)
    // ─────────────────────────────────────────────────────────
    private String normalizeActeurRole(String role) {
        if (role == null)
            return "INVESTISSEUR";
        String r = role.trim().toUpperCase();
        if (r.startsWith("ROLE_"))
            r = r.substring(5);

        return switch (r) {
            case "ADMIN" -> "ADMIN";
            case "ENTREPRISE" -> "ENTREPRISE";
            case "INVESTISSEUR", "USER", "INVESTOR" -> "INVESTISSEUR";
            default -> "INVESTISSEUR";
        };
    }

    // ─────────────────────────────────────────────────────────
    // ENREGISTRER transaction_bourse
    // ─────────────────────────────────────────────────────────
    public TransactionBourse enregistrer(int userId, int idAction, String type, int quantite, double prixUnitaire,
                                         String role, String label) {

        double montantTotal = prixUnitaire * quantite;

        double tauxPourcentage = serviceCommission.getTauxActif(type);
        double commission = Math.round(montantTotal * (tauxPourcentage / 100.0) * 100.0) / 100.0;

        String sql = """
                    INSERT INTO transaction_bourse
                    (id_action, id_user, type_transaction, quantite, prix_unitaire, montant_total, commission, acteur_role, acteur_label)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String acteurRoleDb = normalizeActeurRole(role);
        String acteurLabelDb = (label == null || label.isBlank()) ? "USER_STATIC" : label;

        try (PreparedStatement pst = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, idAction);
            pst.setInt(2, userId);
            pst.setString(3, type);
            pst.setInt(4, quantite);
            pst.setDouble(5, prixUnitaire);
            pst.setDouble(6, montantTotal);
            pst.setDouble(7, commission);
            pst.setString(8, acteurRoleDb);
            pst.setString(9, acteurLabelDb);

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                TransactionBourse t = new TransactionBourse(idAction, type, quantite, prixUnitaire, commission);
                if (rs.next())
                    t.setIdTransaction(rs.getInt(1));
                t.setMontantTotal(montantTotal);
                t.setDateTransaction(new Timestamp(System.currentTimeMillis()));
                return t;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur enregistrement transaction_bourse : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────
    // PORTEFEUILLE
    public int getQuantitePossedee(int userId, int idAction) {
        String sql = """
        SELECT COALESCE(
            SUM(CASE
                WHEN type_transaction = 'ACHAT' THEN quantite
                WHEN type_transaction = 'VENTE' THEN -quantite
                ELSE 0
            END), 0
        ) AS possede
        FROM transaction_bourse
        WHERE id_user = ? AND id_action = ?
    """;

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, idAction);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt("possede") : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    // ─────────────────────────────────────────────────────────
    // ACHAT / VENTE (anciens controllers)
    // ─────────────────────────────────────────────────────────
    public TransactionBourse acheter(int idAction, int quantite, String role, String displayName) throws Exception {
        if (!Session.isLoggedIn())
            throw new IllegalStateException("Utilisateur non connecté.");
        return executerTrade(Session.getCurrentUser().getId(), idAction, "ACHAT", quantite, role, displayName);
    }

    public TransactionBourse vendre(int idAction, int quantite, String role, String displayName) throws Exception {
        if (!Session.isLoggedIn())
            throw new IllegalStateException("Utilisateur non connecté.");
        return executerTrade(Session.getCurrentUser().getId(), idAction, "VENTE", quantite, role, displayName);
    }

    // ─────────────────────────────────────────────────────────
    // ✅ ACHAT / VENTE (nouveaux controllers) => surcharge
    // ─────────────────────────────────────────────────────────
    public TransactionBourse acheter(int userId, int idAction, int quantite, String role, String label)
            throws Exception {
        return executerTrade(userId, idAction, "ACHAT", quantite, role, label);
    }

    public TransactionBourse vendre(int userId, int idAction, int quantite, String role, String label)
            throws Exception {
        return executerTrade(userId, idAction, "VENTE", quantite, role, label);
    }

    // ─────────────────────────────────────────────────────────
    // TRADE transactionnel : stock + portefeuille + historique
    // ─────────────────────────────────────────────────────────
    private TransactionBourse executerTrade(int userId, int idAction, String type, int quantite,
                                            String role, String label) throws Exception {

        if (userId <= 0)
            throw new IllegalArgumentException("Utilisateur invalide.");
        if (quantite <= 0)
            throw new IllegalArgumentException("Quantité invalide.");

        String sqlPrixStock = "SELECT prix_unitaire, quantite_disponible FROM action WHERE id_action = ?";
        String sqlUpdateStock = "UPDATE action SET quantite_disponible = ? WHERE id_action = ?";

        // ⚠️ si ta table portefeuille utilise user_id (pas id_user), garde user_id ici
        String sqlUpsertPortefeuille = """
                    INSERT INTO portefeuille (user_id, id_action, quantite)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE quantite = quantite + VALUES(quantite)
                """;

        String sqlUpdatePortefeuilleVente = """
                    UPDATE portefeuille SET quantite = quantite - ?
                    WHERE user_id = ? AND id_action = ? AND quantite >= ?
                """;

        String sqlCleanupPortefeuille = """
                    DELETE FROM portefeuille
                    WHERE user_id = ? AND id_action = ? AND quantite <= 0
                """;

        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            double prixUnitaire;
            int stock;

            // 1) Lire prix + stock
            try (PreparedStatement pst = conn.prepareStatement(sqlPrixStock)) {
                pst.setInt(1, idAction);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next())
                        throw new IllegalArgumentException("Action introuvable (id=" + idAction + ")");
                    prixUnitaire = rs.getDouble("prix_unitaire");
                    stock = rs.getInt("quantite_disponible");
                }
            }

            int newStock;

            // 2) Achat/Vente + portefeuille
            if ("ACHAT".equalsIgnoreCase(type)) {
                if (stock < quantite)
                    throw new IllegalStateException("Stock insuffisant. Disponible=" + stock + ", demandé=" + quantite);

                newStock = stock - quantite;

                try (PreparedStatement pst = conn.prepareStatement(sqlUpsertPortefeuille)) {
                    pst.setInt(1, userId);
                    pst.setInt(2, idAction);
                    pst.setInt(3, quantite);
                    pst.executeUpdate();
                }

            } else if ("VENTE".equalsIgnoreCase(type)) {

                try (PreparedStatement pst = conn.prepareStatement(sqlUpdatePortefeuilleVente)) {
                    pst.setInt(1, quantite);
                    pst.setInt(2, userId);
                    pst.setInt(3, idAction);
                    pst.setInt(4, quantite);
                    int updated = pst.executeUpdate();
                    if (updated != 1)
                        throw new IllegalStateException(
                                "Vente impossible : quantité insuffisante dans ton portefeuille.");
                }

                newStock = stock + quantite;

                try (PreparedStatement pst = conn.prepareStatement(sqlCleanupPortefeuille)) {
                    pst.setInt(1, userId);
                    pst.setInt(2, idAction);
                    pst.executeUpdate();
                }

            } else {
                throw new IllegalArgumentException("Type transaction invalide: " + type);
            }

            // 3) Update stock
            try (PreparedStatement pst = conn.prepareStatement(sqlUpdateStock)) {
                pst.setInt(1, newStock);
                pst.setInt(2, idAction);
                int updated = pst.executeUpdate();
                if (updated != 1)
                    throw new SQLException("Échec mise à jour stock action (id=" + idAction + ")");
            }

            // 4) Historique
            TransactionBourse t = enregistrer(userId, idAction, type.toUpperCase(), quantite, prixUnitaire, role,
                    label);
            if (t == null)
                throw new SQLException("Transaction non enregistrée (null).");

            // ✅ Commit DB
            conn.commit();

            // ✅ PUSH vers Power BI (après commit)
            pushPowerBIAfterCommit(t, idAction, label);

            // ✅ PUSH vers Wallet (après commit)
            try {
                String symbole = getSymboleAction(idAction);
                double total = t.getMontantTotal();
                if ("ACHAT".equalsIgnoreCase(type)) {
                    WalletBridge.recordBourseAchat(userId, symbole, total);
                } else {
                    WalletBridge.recordBourseVente(userId, symbole, total);
                }
            } catch (Exception walletEx) {
                System.err.println("⚠️ Wallet bridge failed (non-blocking): " + walletEx.getMessage());
            }

            return t;

        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignore) {
            }
            throw ex;

        } finally {
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignore) {
            }
        }
    }
    public List<TransactionBourse> getAllByUser(int userId) {
        return requeteTransactions("WHERE t.id_user = " + userId + " ORDER BY t.date_transaction DESC");
    }
    // ─────────────────────────────────────────────────────────
    // POWER BI INTEGRATION (non bloquant)
    // ─────────────────────────────────────────────────────────
    private void pushPowerBIAfterCommit(TransactionBourse t, int idAction, String label) {
        try {
            String symbole = getSymboleAction(idAction);

            LocalDateTime dt = (t.getDateTransaction() != null)
                    ? t.getDateTransaction().toLocalDateTime()
                    : LocalDateTime.now();

            ServicePowerBI.TransactionBI bi = new ServicePowerBI.TransactionBI(
                    t.getIdTransaction(),
                    symbole,
                    t.getTypeTransaction(),
                    t.getQuantite(),
                    t.getPrixUnitaire(),
                    t.getMontantTotal(),
                    t.getCommission(),
                    ServicePowerBI.toPowerBIDateTime(dt),
                    (label == null || label.isBlank()) ? "USER_STATIC" : label);

            System.out.println("🔥 PUSH POWER BI CALLED tx=" + t.getIdTransaction());
            ServicePowerBI.pushTransaction(bi);
            System.out.println("✅ Power BI push OK tx=" + t.getIdTransaction());

        } catch (Exception e) {
            System.err.println("⚠️ Power BI push échoué (sans bloquer): " + e.getMessage());
        }
    }

    private String getSymboleAction(int idAction) {
        String sql = "SELECT symbole FROM action WHERE id_action = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, idAction);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return rs.getString("symbole");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Impossible de récupérer symbole action: " + e.getMessage());
        }
        return "UNKNOWN";
    }

    // ─────────────────────────────────────────────────────────
    // LECTURE (optionnel, si tu en as besoin)
    // ─────────────────────────────────────────────────────────
    public List<TransactionBourse> getAll() {
        return requeteTransactions("ORDER BY t.date_transaction DESC");
    }

    private List<TransactionBourse> requeteTransactions(String extra) {
        List<TransactionBourse> list = new ArrayList<>();

        String sql = "SELECT t.*, " +
                "a.id_action AS a_id_action, a.symbole, a.nom_entreprise, a.secteur, " +
                "a.prix_unitaire AS prix_action, a.quantite_disponible, a.statut AS statut_action, " +
                "b.id_bourse, b.nom_bourse, b.pays, b.devise, b.statut AS statut_bourse " +
                "FROM transaction_bourse t " +
                "LEFT JOIN action a ON t.id_action = a.id_action " +
                "LEFT JOIN bourse b ON a.id_bourse = b.id_bourse " +
                extra;

        try (PreparedStatement pst = getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                TransactionBourse t = new TransactionBourse();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setIdAction(rs.getInt("id_action"));
                t.setTypeTransaction(rs.getString("type_transaction"));
                t.setQuantite(rs.getInt("quantite"));
                t.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                t.setMontantTotal(rs.getDouble("montant_total"));
                t.setCommission(rs.getDouble("commission"));
                t.setDateTransaction(rs.getTimestamp("date_transaction"));

                if (rs.getObject("a_id_action") != null || rs.getObject("symbole") != null) {
                    Action action = new Action();
                    action.setIdAction(rs.getInt("a_id_action"));
                    action.setSymbole(rs.getString("symbole"));
                    action.setNomEntreprise(rs.getString("nom_entreprise"));
                    action.setSecteur(rs.getString("secteur"));
                    action.setPrixUnitaire(rs.getDouble("prix_action"));
                    action.setQuantiteDisponible(rs.getInt("quantite_disponible"));
                    action.setStatut(rs.getString("statut_action"));

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
            e.printStackTrace();
        }

        return list;
    }

    // ─────────────────────────────────────────────────────────
    // STATS (si tu veux garder les méthodes)
    // ─────────────────────────────────────────────────────────
    public double getTotalInvesti() {
        return somme("SELECT COALESCE(SUM(montant_total),0) FROM transaction_bourse WHERE type_transaction='ACHAT'");
    }

    public double getTotalVendu() {
        return somme("SELECT COALESCE(SUM(montant_total),0) FROM transaction_bourse WHERE type_transaction='VENTE'");
    }

    public double getTotalCommissions() {
        return somme("SELECT COALESCE(SUM(commission),0) FROM transaction_bourse");
    }

    public int getNombreTransactions() {
        return (int) somme("SELECT COUNT(*) FROM transaction_bourse");
    }

    private double somme(String sql) {
        try (Statement stm = getConnection().createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getNombreActions(int userId) {
        String sql = "SELECT COUNT(DISTINCT id_action) FROM portefeuille WHERE user_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}