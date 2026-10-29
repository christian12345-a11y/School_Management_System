package dao;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Subject;

public class SubjectDAO {

    public List<Subject> findActive() throws SQLException {
        String sql = "SELECT subject_id, subject_code, subject_name, units "
                   + "FROM subject WHERE status = 'active' ORDER BY subject_code";
        List<Subject> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Subject(
                    rs.getInt("subject_id"),
                    rs.getString("subject_code"),
                    rs.getString("subject_name"),
                    rs.getDouble("units")));
            }
        }
        return list;
    }
}