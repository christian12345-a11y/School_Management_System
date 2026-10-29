package dao;

import config.DBConnection;
import java.sql.*;

public class AuditDAO {

    // userId and recordId can be null (e.g. failed login with unknown username)
    public void log(Integer userId, String action, String tableName,
                    Integer recordId, String status) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, table_name, record_id, status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setString(2, action);
            ps.setString(3, tableName);
            ps.setObject(4, recordId);
            ps.setString(5, status);
            ps.executeUpdate();
        }
    }
}