package tn.finora.services;

import tn.finora.entities.Lesson;
import tn.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LessonService {

    private final Connection cnx = DBConnection.getInstance().getCnx();

    public void add(Lesson l) throws SQLException {
        String sql = "INSERT INTO lesson(formation_id, titre, contenu, video_url, ordre, duree_minutes) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, l.getFormationId());
        ps.setString(2, l.getTitre());
        ps.setString(3, l.getContenu());
        ps.setString(4, l.getVideoUrl());
        ps.setInt(5, l.getOrdre());
        ps.setInt(6, l.getDureeMinutes());
        ps.executeUpdate();
    }

    public List<Lesson> getAll() throws SQLException {
        List<Lesson> list = new ArrayList<>();
        String sql = "SELECT * FROM lesson ORDER BY id DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Lesson l = map(rs);
            list.add(l);
        }
        return list;
    }

    public List<Lesson> getByFormation(int formationId) throws SQLException {
        List<Lesson> list = new ArrayList<>();
        String sql = "SELECT * FROM lesson WHERE formation_id=? ORDER BY ordre ASC, id ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, formationId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Lesson l = map(rs);
            list.add(l);
        }
        return list;
    }

    public void update(Lesson l) throws SQLException {
        String sql = "UPDATE lesson SET formation_id=?, titre=?, contenu=?, video_url=?, ordre=?, duree_minutes=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, l.getFormationId());
        ps.setString(2, l.getTitre());
        ps.setString(3, l.getContenu());
        ps.setString(4, l.getVideoUrl());
        ps.setInt(5, l.getOrdre());
        ps.setInt(6, l.getDureeMinutes());
        ps.setInt(7, l.getId());

        int rows = ps.executeUpdate();
        if (rows == 0) System.out.println("⚠️ Aucun lesson trouvé avec id=" + l.getId());
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM lesson WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        int rows = ps.executeUpdate();
        if (rows == 0) System.out.println("⚠️ Aucun lesson trouvé avec id=" + id);
    }

    private Lesson map(ResultSet rs) throws SQLException {
        Lesson l = new Lesson();
        l.setId(rs.getInt("id"));
        l.setFormationId(rs.getInt("formation_id"));
        l.setTitre(rs.getString("titre"));
        l.setContenu(rs.getString("contenu"));
        // ✅ NEW (null-safe)
        try { l.setVideoUrl(rs.getString("video_url")); } catch (SQLException ignored) { l.setVideoUrl(null); }
        l.setOrdre(rs.getInt("ordre"));
        l.setDureeMinutes(rs.getInt("duree_minutes"));
        return l;
    }
    public void updateVideoUrl(int lessonId, String videoUrl) throws SQLException {
        String sql = "UPDATE lesson SET video_url=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, videoUrl);
        ps.setInt(2, lessonId);
        ps.executeUpdate();
    }
}