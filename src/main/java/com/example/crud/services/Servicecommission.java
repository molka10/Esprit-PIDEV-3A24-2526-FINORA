package com.example.crud.services;

import com.example.crud.models.Commission;
import com.example.crud.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 💰 ServiceCommission
 * Gère les taux de commission appliqués aux transactions
 */
public class Servicecommission {

    private final Connection connection;

    public Servicecommission() {
        this.connection = Database.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────
    //  RÉCUPÉRER LA COMMISSION ACTIVE
    // ─────────────────────────────────────────────────────────

    /**
     * Retourne la commission active pour un type de transaction donné.
     * Utilisé par ServiceTransaction pour calculer les frais.
     */
    public Commission getCommissionActive(String typeTransaction) {
        String sql = "SELECT * FROM commission WHERE active = TRUE " +
                "AND (type_transaction = ? OR type_transaction = 'LES_DEUX') " +
                "ORDER BY date_modification DESC LIMIT 1";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, typeTransaction);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapCommission(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération commission : " + e.getMessage());
            e.printStackTrace();
        }

        // Par défaut : 0.5%
        return new Commission("Commission par défaut", "LES_DEUX", 0.5);
    }

    /**
     * Raccourci pour obtenir directement le taux en %
     */
    public double getTauxActif(String typeTransaction) {
        return getCommissionActive(typeTransaction).getTauxPourcentage();
    }

    // ─────────────────────────────────────────────────────────
    //  CRUD STANDARD
    // ─────────────────────────────────────────────────────────

    public List<Commission> getAll() {
        List<Commission> list = new ArrayList<>();
        String sql = "SELECT * FROM commission ORDER BY date_modification DESC";

        try (Statement stm = connection.createStatement();
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

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapCommission(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void add(Commission c) {
        String sql = "INSERT INTO commission (nom, type_transaction, taux_pourcentage, active) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, c.getNom());
            pst.setString(2, c.getTypeTransaction());
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
        String sql = "UPDATE commission SET nom = ?, type_transaction = ?, " +
                "taux_pourcentage = ?, active = ? WHERE id_commission = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, c.getNom());
            pst.setString(2, c.getTypeTransaction());
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

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, c.getIdCommission());
            pst.executeUpdate();
            System.out.println("🗑️ Commission supprimée : " + c.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression commission : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  STATISTIQUES ADMIN
    // ─────────────────────────────────────────────────────────

    /**
     * Revenus totaux générés par les commissions
     */
    public double getRevenusTotaux() {
        String sql = "SELECT COALESCE(SUM(commission), 0) FROM transaction";
        try (Statement stm = connection.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Revenus du mois en cours
     */
    public double getRevenusMoisActuel() {
        String sql = "SELECT COALESCE(SUM(commission), 0) FROM transaction " +
                "WHERE MONTH(date_transaction) = MONTH(CURRENT_DATE) " +
                "AND YEAR(date_transaction) = YEAR(CURRENT_DATE)";
        try (Statement stm = connection.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────
    //  HELPER MAPPING
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
        return c;
    }
}