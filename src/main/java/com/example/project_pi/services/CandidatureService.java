package com.example.project_pi.services;

import com.example.project_pi.entities.Candidature;
import com.example.project_pi.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatureService {

    private final Connection cnx = DBConnection.getInstance().getCnx();

    public void add(Candidature c) throws SQLException {
        String sql = """
                INSERT INTO candidature
                (appel_offre_id, nom_candidat, email_candidat, montant_propose, message, statut)
                VALUES (?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, c.getAppelOffreId());
            ps.setString(2, c.getNomCandidat());
            ps.setString(3, c.getEmailCandidat());

            if (c.getMontantPropose() == 0) ps.setNull(4, Types.DOUBLE);
            else ps.setDouble(4, c.getMontantPropose());

            ps.setString(5, c.getMessage());
            ps.setString(6, c.getStatut());
            ps.executeUpdate();
        }
    }

    public List<Candidature> getAll() throws SQLException {
        List<Candidature> list = new ArrayList<>();

        String sql = "SELECT * FROM candidature ORDER BY candidature_id DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Candidature> getByAppelOffreId(int appelOffreId) throws SQLException {
        List<Candidature> list = new ArrayList<>();

        String sql = "SELECT * FROM candidature WHERE appel_offre_id=? ORDER BY candidature_id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, appelOffreId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void update(Candidature c) throws SQLException {
        String sql = """
                UPDATE candidature
                SET appel_offre_id=?, nom_candidat=?, email_candidat=?,
                    montant_propose=?, message=?, statut=?
                WHERE candidature_id=?
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, c.getAppelOffreId());
            ps.setString(2, c.getNomCandidat());
            ps.setString(3, c.getEmailCandidat());

            if (c.getMontantPropose() == 0) ps.setNull(4, Types.DOUBLE);
            else ps.setDouble(4, c.getMontantPropose());

            ps.setString(5, c.getMessage());
            ps.setString(6, c.getStatut());

            ps.setInt(7, c.getCandidatureId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM candidature WHERE candidature_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Candidature map(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setCandidatureId(rs.getInt("candidature_id"));
        c.setAppelOffreId(rs.getInt("appel_offre_id"));
        c.setNomCandidat(rs.getString("nom_candidat"));
        c.setEmailCandidat(rs.getString("email_candidat"));

        double m = rs.getDouble("montant_propose");
        if (rs.wasNull()) m = 0;
        c.setMontantPropose(m);

        c.setMessage(rs.getString("message"));
        c.setStatut(rs.getString("statut"));

        // ✅ ADD THIS
        c.setCreatedAt(rs.getTimestamp("created_at"));

        return c;
    }
    public java.util.Map<Integer, Integer> getCountsByAppelOffreId() throws SQLException {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();

        String sql = "SELECT appel_offre_id, COUNT(*) AS cnt FROM candidature GROUP BY appel_offre_id";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getInt("appel_offre_id"), rs.getInt("cnt"));
            }
        }
        return map;
    }
}
