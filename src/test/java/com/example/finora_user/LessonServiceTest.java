package tn.finora.services;

import com.example.finora.services.LessonService;
import org.junit.jupiter.api.*;
import com.example.finora.entities.Lesson;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LessonServiceTest {

    private static Connection cnx;
    private static LessonService lessonService;

    // We will insert a Formation row ONLY for tests, so FK works.
    private static int testFormationId;
    private static int insertedLessonId;

    @BeforeAll
    static void setupAll() throws SQLException {
        cnx = DBConnection.getInstance().getCnx();
        lessonService = new LessonService();

        // 1) Create a formation for tests (minimal insert).
        // Adjust column names if your formation table differs.
        String insertFormation = "INSERT INTO formation (titre, description, categorie, niveau, is_published) VALUES (?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(insertFormation, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, "JUnit Formation");
        ps.setString(2, "Formation created for tests");
        ps.setString(3, "Test");
        ps.setString(4, "debutant");
        ps.setInt(5, 1);
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        assertTrue(keys.next(), "Formation ID not generated");
        testFormationId = keys.getInt(1);
    }

    @AfterAll
    static void cleanupAll() throws SQLException {
        // Clean lessons then formation (FK)
        PreparedStatement delLessons = cnx.prepareStatement("DELETE FROM lesson WHERE formation_id=?");
        delLessons.setInt(1, testFormationId);
        delLessons.executeUpdate();

        PreparedStatement delFormation = cnx.prepareStatement("DELETE FROM formation WHERE id=?");
        delFormation.setInt(1, testFormationId);
        delFormation.executeUpdate();
    }

    @Test
    @Order(1)
    void testAddLesson() {
        Lesson l = new Lesson(testFormationId, "JUnit Lesson", "Content", 1, 10);

        assertDoesNotThrow(() -> lessonService.add(l));

        // Verify it exists in DB and capture inserted ID (best for update/delete tests)
        try {
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT id FROM lesson WHERE formation_id=? AND titre=? ORDER BY id DESC LIMIT 1"
            );
            ps.setInt(1, testFormationId);
            ps.setString(2, "JUnit Lesson");
            ResultSet rs = ps.executeQuery();

            assertTrue(rs.next(), "Lesson not found after insert");
            insertedLessonId = rs.getInt("id");
            assertTrue(insertedLessonId > 0, "Inserted lesson ID should be > 0");
        } catch (SQLException e) {
            fail("SQL error while verifying insert: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testGetByFormation() {
        assertTrue(testFormationId > 0, "testFormationId must be set");

        assertDoesNotThrow(() -> {
            List<Lesson> lessons = lessonService.getByFormation(testFormationId);
            assertNotNull(lessons);
            assertTrue(lessons.size() >= 1, "Should return at least 1 lesson for this formation");
        });
    }

    @Test
    @Order(3)
    void testUpdateLesson() {
        assertTrue(insertedLessonId > 0, "insertedLessonId must be set by testAddLesson");

        Lesson updated = new Lesson(testFormationId, "JUnit Lesson Updated", "New content", 2, 20);
        updated.setId(insertedLessonId);

        assertDoesNotThrow(() -> lessonService.update(updated));

        // Verify updated values in DB
        try {
            PreparedStatement ps = cnx.prepareStatement("SELECT titre, ordre, duree_minutes FROM lesson WHERE id=?");
            ps.setInt(1, insertedLessonId);
            ResultSet rs = ps.executeQuery();

            assertTrue(rs.next(), "Updated lesson not found");
            assertEquals("JUnit Lesson Updated", rs.getString("titre"));
            assertEquals(2, rs.getInt("ordre"));
            assertEquals(20, rs.getInt("duree_minutes"));
        } catch (SQLException e) {
            fail("SQL error while verifying update: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void testDeleteLesson() {
        assertTrue(insertedLessonId > 0, "insertedLessonId must be set");

        assertDoesNotThrow(() -> lessonService.delete(insertedLessonId));

        // Verify deletion
        try {
            PreparedStatement ps = cnx.prepareStatement("SELECT id FROM lesson WHERE id=?");
            ps.setInt(1, insertedLessonId);
            ResultSet rs = ps.executeQuery();
            assertFalse(rs.next(), "Lesson should be deleted");
        } catch (SQLException e) {
            fail("SQL error while verifying delete: " + e.getMessage());
        }
    }
}
