package com.example.project_pi;

import com.example.project_pi.entities.Candidature;
import com.example.project_pi.services.CandidatureService;
import com.example.project_pi.utils.DBConnection;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CandidatureCRUDServiceTest {

    @Test
    void testServiceAddAndGetAll() throws Exception {

        Connection cnx = DBConnection.getInstance().getCnx();

        // 1) Insert a parent appel_offre (needed because FK)
        long appelOffreId;
        String insertAppel = """
                INSERT INTO appel_offre
                (titre, description, categorie, type, budget_min, budget_max, devise, date_limite, statut)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(insertAppel, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Appel Offre for Service Test");
            ps.setString(2, "Inserted to test CandidatureService.");
            ps.setString(3, "Informatique");
            ps.setString(4, "achat");
            ps.setBigDecimal(5, new java.math.BigDecimal("1000.00"));
            ps.setBigDecimal(6, new java.math.BigDecimal("2000.00"));
            ps.setString(7, "TND");
            ps.setDate(8, Date.valueOf("2026-03-22"));
            ps.setString(9, "published");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next());
                appelOffreId = keys.getLong(1);
            }
        }

        // 2) Use YOUR service to add candidature
        CandidatureService service = new CandidatureService();

        Candidature c = new Candidature(
                (int) appelOffreId,
                "Test Service User",
                "service.user@mail.com",
                1600.0,
                "Created via CandidatureService test",
                "submitted"
        );

        service.add(c);

        // 3) Verify with service getAll()
        List<Candidature> all = service.getAll();
        assertNotNull(all);
        assertTrue(all.size() > 0);

        boolean found = all.stream().anyMatch(x ->
                x.getAppelOffreId() == (int) appelOffreId
                        && "Test Service User".equals(x.getNomCandidat())
                        && "service.user@mail.com".equals(x.getEmailCandidat())
        );

        assertTrue(found, "Inserted candidature not found in service.getAll()");

        // 4) Cleanup: delete inserted rows
        // Delete candidatures linked to this appel_offre
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM candidature WHERE appel_offre_id = ?")) {
            ps.setLong(1, appelOffreId);
            ps.executeUpdate();
        }
        // Delete the parent
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM appel_offre WHERE appel_offre_id = ?")) {
            ps.setLong(1, appelOffreId);
            ps.executeUpdate();
        }
    }
}
