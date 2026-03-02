package com.example.finora.services.bourse;

import com.example.finora.entities.Commission;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 💰 ServiceCommission
 * Gère les taux de commission appliqués aux transactions
 *
 * ✅ Ajouts:
 * - Support commission spécifique par action via colonne commission.symbole
 * (nullable)
 * - Garde compatibilité avec l'existant (getCommissionActive(String) et
 * getTauxActif(String))
 */
public class Servicecommission {

    public Servicecommission() {
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────
    // NORMALISATION
    // ─────────────────────────────────────────────────────────

    private String normalizeType(String typeTransaction) {
        if (typeTransaction == null)
            return "LES_DEUX";
        String t = typeTransaction.trim().toUpperCase();
        if (t.startsWith("TYPE_"))
            t = t.substring(5);
        // On accepte uniquement les valeurs enum BD : ACHAT, VENTE, LES_DEUX
        return switch (t) {
            case "ACHAT" -> "ACHAT";
            case "VENTE" -> "VENTE";
            default -> "LES_DEUX";
        };
    }

    private String normalizeSymbole(String symbole) {
        if (symbole == null)
            return null;
        String s = symbole.trim().toUpperCase();
        return s.isBlank() ? null : s;
    }

    // ─────────────────────────────────────────────────────────
    // RÉCUPÉRER LA COMMISSION ACTIVE
    // ─────────────────────────────────────────────────────────

    /**
     * ✅ Ancienne méthode (compat)
     * Retourne la commission active globale (sans symbole)
     */
    public Commission getCommissionActive(String typeTransaction) {
        String type = normalizeType(typeTransaction);

        String sql = """
                    SELECT *
                    FROM commission
                    WHERE active = TRUE
                      AND (type_transaction = ? OR type_transaction = 'LES_DEUX')
                      AND symbole IS NULL
                    ORDER BY date_modification DESC
                    LIMIT 1
                """;

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, type);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return mapCommission(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération commission (globale) : " + e.getMessage());
            e.printStackTrace();
        }

        return new Commission("Commission par défaut", "LES_DEUX", 0.5);
    }

    /**
     * ✅ Nouvelle méthode (nécessaire)
     * Commission active par type + symbole :
     * - cherche d'abord une commission spécifique (symbole = ?)
     * - sinon fallback sur globale (symbole IS NULL)
     */
    public Commission getCommissionActive(String typeTransaction, String symbole) {
        String type = normalizeType(typeTransaction);
        String symb = normalizeSymbole(symbole);

        // Si pas de symbole => fallback direct sur globale
        if (symb == null)
            return getCommissionActive(type);

        String sql = """
                    SELECT *
                    FROM commission
                    WHERE active = TRUE
                      AND (type_transaction = ? OR type_transaction = 'LES_DEUX')
                      AND (symbole = ? OR symbole IS NULL)
                    ORDER BY
                      CASE WHEN symbole = ? THEN 0 ELSE 1 END,
                      date_modification DESC
                    LIMIT 1
                """;

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, type);
            pst.setString(2, symb);
            pst.setString(3, symb);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return mapCommission(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération commission (par symbole) : " + e.getMessage());
            e.printStackTrace();
        }

        return new Commission("Commission par défaut", "LES_DEUX", 0.5);
    }

    /**
     * ✅ Ancien raccourci (compat)
     */
    public double getTauxActif(String typeTransaction) {
        return getCommissionActive(typeTransaction).getTauxPourcentage();
    }

    /**
     * ✅ Nouveau raccourci (nécessaire)
     */
    public double getTauxActif(String typeTransaction, String symbole) {
        return getCommissionActive(typeTransaction, symbole).getTauxPourcentage();
    }

    // ─────────────────────────────────────────────────────────
    // CRUD STANDARD
    // ─────────────────────────────────────────────────────────

    public List<Commission> getAll() {
        List<Commission> list = new ArrayList<>();
        String sql = "SELECT * FROM commission ORDER BY date_modification DESC";

        try (Statement stm = getConnection().createStatement();
                ResultSet rs = stm.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapCommission(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture commissions : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public Commission getById(int id) {
        String sql = "SELECT * FROM commission WHERE id_commission = ?";

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return mapCommission(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void add(Commission c) {
        // ✅ si tu ajoutes symbole dans ton model Commission, tu peux l'enregistrer ici
        // sinon garde ce insert simple (globale)
        String sql = "INSERT INTO commission (nom, type_transaction, taux_pourcentage, active) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, c.getNom());
            pst.setString(2, normalizeType(c.getTypeTransaction()));
            pst.setDouble(3, c.getTauxPourcentage());
            pst.setBoolean(4, c.isActive());
            pst.executeUpdate();
            System.out.println("✅ Commission ajoutée : " + c.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout commission : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(Commission c) {
        String sql = "UPDATE commission SET nom = ?, type_transaction = ?, taux_pourcentage = ?, active = ? WHERE id_commission = ?";

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, c.getNom());
            pst.setString(2, normalizeType(c.getTypeTransaction()));
            pst.setDouble(3, c.getTauxPourcentage());
            pst.setBoolean(4, c.isActive());
            pst.setInt(5, c.getIdCommission());
            pst.executeUpdate();
            System.out.println("✅ Commission modifiée : " + c.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour commission : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(Commission c) {
        String sql = "DELETE FROM commission WHERE id_commission = ?";

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, c.getIdCommission());
            pst.executeUpdate();
            System.out.println("🗑️ Commission supprimée : " + c.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression commission : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────
    // STATISTIQUES ADMIN (table correcte)
    // ─────────────────────────────────────────────────────────

    public double getRevenusTotaux() {
        String sql = "SELECT COALESCE(SUM(commission), 0) FROM transaction_bourse";
        try (Statement stm = getConnection().createStatement();
                ResultSet rs = stm.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getRevenusMoisActuel() {
        String sql = """
                    SELECT COALESCE(SUM(commission), 0)
                    FROM transaction_bourse
                    WHERE MONTH(date_transaction) = MONTH(CURRENT_DATE)
                      AND YEAR(date_transaction) = YEAR(CURRENT_DATE)
                """;
        try (Statement stm = getConnection().createStatement();
                ResultSet rs = stm.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────
    // HELPER MAPPING
    // ─────────────────────────────────────────────────────────

    private Commission mapCommission(ResultSet rs) throws SQLException {
        Commission c = new Commission();
        c.setIdCommission(rs.getInt("id_commission"));
        c.setNom(rs.getString("nom"));
        c.setTypeTransaction(rs.getString("type_transaction"));
        c.setTauxPourcentage(rs.getDouble("taux_pourcentage"));
        c.setDateCreation(rs.getTimestamp("date_creation"));
        c.setDateModification(rs.getTimestamp("date_modification"));
        c.setActive(rs.getBoolean("active"));
        // ⚠️ si tu ajoutes "symbole" dans le model Commission, tu peux décommenter :
        // c.setSymbole(rs.getString("symbole"));
        return c;
    }
}