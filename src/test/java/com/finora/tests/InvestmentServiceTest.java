package com.finora.tests;

import org.junit.jupiter.api.Test;
import com.example.finora.entities.Investment;
import com.example.finora.services.InvestmentService;
import com.example.finora.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InvestmentServiceTest {


    private int insertRow() throws Exception {
        Connection cnx = DBConnection.getInstance().getCnx();

        String sql = "INSERT INTO investment (name, category, location, estimated_value, risk_level, image_url, description, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TEST_INV");
            ps.setString(2, "TEST_CAT");
            ps.setString(3, "TEST_LOC");
            ps.setBigDecimal(4, new BigDecimal("9999.99"));
            ps.setString(5, "MEDIUM");
            ps.setString(6, "https://example.com/test.jpg");
            ps.setString(7, "TEST_DESC");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), " No generated key returned!");
                return keys.getInt(1);
            }
        }
    }


    private void deleteRow(int id) throws Exception {
        Connection cnx = DBConnection.getInstance().getCnx();
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM investment WHERE investment_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }


    @Test
    void testAddInvestment() throws Exception {
        InvestmentService service = new InvestmentService();

        Investment inv = new Investment(
                "JUnit_Investment",
                "Category_Test",
                "Tunis",
                new BigDecimal("12345.50"),
                "LOW",
                "https://example.com/img.jpg",
                "Investment added by JUnit"
        );

        service.add(inv);

        Connection cnx = DBConnection.getInstance().getCnx();
        String sql = "SELECT * FROM investment ORDER BY investment_id DESC LIMIT 1";

        int insertedId;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            assertTrue(rs.next(), " No row found after add!");

            insertedId = rs.getInt("investment_id");

            assertEquals("JUnit_Investment", rs.getString("name"));
            assertEquals("Category_Test", rs.getString("category"));
            assertEquals("Tunis", rs.getString("location"));
            assertEquals(new BigDecimal("12345.50"), rs.getBigDecimal("estimated_value"));
            assertEquals("LOW", rs.getString("risk_level"));
        }

        // cleanup
        deleteRow(insertedId);
    }


    @Test
    void testGetAllInvestments() throws Exception {
        InvestmentService service = new InvestmentService();

        int insertedId = insertRow();

        List<Investment> list = service.getAll();
        assertNotNull(list);
        assertTrue(list.size() > 0, "❌ getAll returned empty list!");

        boolean found = list.stream().anyMatch(x -> x.getInvestmentId() == insertedId);
        assertTrue(found, "❌ Inserted row not found in getAll list!");

        // cleanup
        deleteRow(insertedId);
    }


    @Test
    void testUpdateInvestment() throws Exception {
        InvestmentService service = new InvestmentService();

        int insertedId = insertRow();

        Investment inv = new Investment();
        inv.setInvestmentId(insertedId);
        inv.setName("UPDATED_NAME");
        inv.setCategory("UPDATED_CAT");
        inv.setLocation("UPDATED_LOC");
        inv.setEstimatedValue(new BigDecimal("1111.11"));
        inv.setRiskLevel("HIGH");
        inv.setImageUrl("https://example.com/updated.jpg");
        inv.setDescription("UPDATED_DESC");

        service.update(inv);

        Connection cnx = DBConnection.getInstance().getCnx();
        String sql = "SELECT * FROM investment WHERE investment_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, insertedId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "❌ Row not found after update!");
                assertEquals("UPDATED_NAME", rs.getString("name"));
                assertEquals(new BigDecimal("1111.11"), rs.getBigDecimal("estimated_value"));
                assertEquals("HIGH", rs.getString("risk_level"));
            }
        }

        // cleanup
        deleteRow(insertedId);
    }

    @Test
    void testDeleteInvestment() throws Exception {
        InvestmentService service = new InvestmentService();

        int insertedId = insertRow();

        service.delete(insertedId);

        Connection cnx = DBConnection.getInstance().getCnx();
        String sql = "SELECT COUNT(*) FROM investment WHERE investment_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, insertedId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "❌ Row still exists after delete!");
            }
        }
    }
}
