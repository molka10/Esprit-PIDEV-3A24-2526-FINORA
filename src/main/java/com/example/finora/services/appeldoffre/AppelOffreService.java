package com.example.finora.services.appeldoffre;

import com.example.finora.entities.AppelOffre;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppelOffreService {

    private final Connection cnx;

    public AppelOffreService() {
        try {
            this.cnx = DBConnection.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }

    // =========================
    // CREATE
    // =========================
    public void add(AppelOffre a) throws SQLException {

        String sql = """
                INSERT INTO appel_offre
                (titre, description, categorie, type, budget_min, budget_max, devise, date_limite, statut)
                VALUES (?,?,?,?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getCategorie());
            ps.setString(4, a.getType());

            if (a.getBudgetMin() == 0) {
                ps.setNull(5, Types.DOUBLE);
            } else {
                ps.setDouble(5, a.getBudgetMin());
            }

            if (a.getBudgetMax() == 0) {
                ps.setNull(6, Types.DOUBLE);
            } else {
                ps.setDouble(6, a.getBudgetMax());
            }

            ps.setString(7, a.getDevise());

            if (a.getDateLimite() == null) {
                ps.setNull(8, Types.DATE);
            } else {
                ps.setDate(8, Date.valueOf(a.getDateLimite()));
            }

            ps.setString(9, a.getStatut());

            ps.executeUpdate();
        }
    }

    // =========================
    // READ ALL
    // =========================
    public List<AppelOffre> getAll() throws SQLException {

        List<AppelOffre> list = new ArrayList<>();

        String sql = "SELECT * FROM appel_offre ORDER BY appel_offre_id DESC";

        try (Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                AppelOffre a = new AppelOffre();

                a.setAppelOffreId(rs.getInt("appel_offre_id"));
                a.setTitre(rs.getString("titre"));
                a.setDescription(rs.getString("description"));
                a.setCategorie(rs.getString("categorie"));
                a.setType(rs.getString("type"));

                double min = rs.getDouble("budget_min");
                if (rs.wasNull())
                    min = 0;
                a.setBudgetMin(min);

                double max = rs.getDouble("budget_max");
                if (rs.wasNull())
                    max = 0;
                a.setBudgetMax(max);

                a.setDevise(rs.getString("devise"));

                Date d = rs.getDate("date_limite");
                a.setDateLimite(d != null ? d.toLocalDate() : null);

                a.setStatut(rs.getString("statut"));

                list.add(a);
            }
        }

        return list;
    }

    // =========================
    // UPDATE
    // =========================
    public void update(AppelOffre a) throws SQLException {

        String sql = """
                UPDATE appel_offre
                SET titre=?, description=?, categorie=?, type=?,
                    budget_min=?, budget_max=?, devise=?, date_limite=?, statut=?
                WHERE appel_offre_id=?
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getCategorie());
            ps.setString(4, a.getType());

            if (a.getBudgetMin() == 0) {
                ps.setNull(5, Types.DOUBLE);
            } else {
                ps.setDouble(5, a.getBudgetMin());
            }

            if (a.getBudgetMax() == 0) {
                ps.setNull(6, Types.DOUBLE);
            } else {
                ps.setDouble(6, a.getBudgetMax());
            }

            ps.setString(7, a.getDevise());

            if (a.getDateLimite() == null) {
                ps.setNull(8, Types.DATE);
            } else {
                ps.setDate(8, Date.valueOf(a.getDateLimite()));
            }

            ps.setString(9, a.getStatut());
            ps.setInt(10, a.getAppelOffreId());

            ps.executeUpdate();
        }
    }

    // =========================
    // DELETE
    // =========================
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM appel_offre WHERE appel_offre_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
