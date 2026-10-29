package dao;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Section;

public class SectionDAO {

    public List<Section> findActive() throws SQLException {
        String sql = "SELECT Section_id, section_name, capacity "
                   + "FROM section WHERE status = 'active' ORDER BY section_name";
        List<Section> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int cap = rs.getInt("capacity");
                list.add(new Section(
                    rs.getInt("Section_id"),
                    rs.getString("section_name"),
                    rs.wasNull() ? null : cap));
            }
        }
        return list;
    }

    // True if adding this student would go over the section's capacity
    public boolean isFull(int sectionId, int studentId, String schoolYear) throws SQLException {
        String sql = "SELECT sec.capacity, "
                   + "(SELECT COUNT(DISTINCT es.Student_id) FROM enrollment_subject es "
                   + " WHERE es.Section_id = sec.Section_id AND es.school_year = ? "
                   + " AND es.status = 'active' AND es.Student_id <> ?) AS taken "
                   + "FROM section sec WHERE sec.Section_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schoolYear);
            ps.setInt(2, studentId);
            ps.setInt(3, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                int cap = rs.getInt("capacity");
                if (rs.wasNull()) return false;   // no capacity set = no limit
                return rs.getInt("taken") >= cap;
            }
        }
    }
}