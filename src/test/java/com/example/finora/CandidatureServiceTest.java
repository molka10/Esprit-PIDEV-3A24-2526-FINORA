package com.example.finora;

import com.example.finora.utils.DBConnection;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class CandidatureServiceTest {

    @Test
    void testInsertAndSelectCandidatureWithFK() throws Exception {

        Connection cnx = DBConnection.getInstance().getCnx();

        // 1) Insert parent: appel_offre
        long appelOffreId;
        String insertAppel = """
                INSERT INTO appel_offre
                (titre, description, categorie, type, budget_min, budget_max, devise, date_limite, statut)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(insertAppel, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Test Appel Offre JUnit");
            ps.setString(2, "Appel d'offre inséré depuis un test JUnit.");
            ps.setString(3, "Informatique");
            ps.setString(4, "achat");
            ps.setBigDecimal(5, new java.math.BigDecimal("1000.00"));
            ps.setBigDecimal(6, new java.math.BigDecimal("2000.00"));
            ps.setString(7, "TND");
            ps.setDate(8, Date.valueOf("2026-03-20"));
            ps.setString(9, "published");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key returned for appel_offre");
                appelOffreId = keys.getLong(1);
            }
        }

        // 2) Insert child: candidature linked to appelOffreId
        long candidatureId;
        String insertCand = """
                INSERT INTO candidature
                (appel_offre_id, nom_candidat, email_candidat, montant_propose, message, statut)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(insertCand, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, appelOffreId);
            ps.setString(2, "Candidat Test");
            ps.setString(3, "candidat.test@mail.com");
            ps.setBigDecimal(4, new java.math.BigDecimal("1500.00"));
            ps.setString(5, "Candidature créée via test JUnit.");
            ps.setString(6, "submitted");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), "No generated key returned for candidature");
                candidatureId = keys.getLong(1);
            }
        }

        // 3) Verify: select candidature by id
        String select = "SELECT * FROM candidature WHERE candidature_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(select)) {
            ps.setLong(1, candidatureId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Candidature not found in DB");
                assertEquals(appelOffreId, rs.getLong("appel_offre_id"));
                assertEquals("Candidat Test", rs.getString("nom_candidat"));
                assertEquals("submitted", rs.getString("statut"));
            }
        }

        // (Optional cleanup) - keeps DB clean for repeated tests
        // Delete child then parent
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM candidature WHERE candidature_id = ?")) {
            ps.setLong(1, candidatureId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM appel_offre WHERE appel_offre_id = ?")) {
            ps.setLong(1, appelOffreId);
            ps.executeUpdate();
        }
    }
}
