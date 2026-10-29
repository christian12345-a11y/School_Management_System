package dao;

import config.DBConnection;
import java.sql.*;

public class DashboardDAO {

    public int countRequests(String status) throws SQLException {
        return scalar("SELECT COUNT(*) FROM document_requests WHERE status = ?", status);
    }

    public int countActiveStudents() throws SQLException {
        return scalar("SELECT COUNT(*) FROM students WHERE status = ?", "active");
    }

    private int scalar(String sql, String param) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}