package dao;

import config.DBConnection;
import java.sql.*;
import model.User;

public class UserDAO {

    // Returns the User if username + password hash match, otherwise null
    public User authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT user_id, role_id, username, full_name, email, status "
                   + "FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getInt("role_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("status"));
                }
            }
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        return exists("SELECT 1 FROM users WHERE username = ?", username);
    }

    public boolean emailExists(String email) throws SQLException {
        return exists("SELECT 1 FROM users WHERE email = ?", email);
    }

    public void create(int roleId, String username, String passwordHash,
                       String fullName, String email) throws SQLException {
        String sql = "INSERT INTO users (role_id, username, password_hash, full_name, email) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setString(2, username);
            ps.setString(3, passwordHash);
            ps.setString(4, fullName);
            ps.setString(5, email);
            ps.executeUpdate();
        }
    }

    private boolean exists(String sql, String value) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}