package dao;

import config.DBConnection;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.DocumentRequest;

public class DocumentRequestDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String NAME_EXPR =
            "CONCAT_WS(' ', s.first_name, s.middle_name, s.last_name, s.suffix)";

    // status: "All", "pending", "approved" or "rejected"
    public List<DocumentRequest> findAll(String status, String keyword) throws SQLException {
        String sql = "SELECT dr.requested_id, s.student_no, " + NAME_EXPR + " AS student_name, "
                   + "dr.document_type, dr.purpose, dr.status, dr.requested_at, dr.processed_by "
                   + "FROM document_requests dr "
                   + "JOIN students s ON s.student_id = dr.student_id "
                   + "WHERE (? = 'All' OR dr.status = ?) "
                   + "AND (s.student_no LIKE ? OR dr.document_type LIKE ? OR " + NAME_EXPR + " LIKE ?) "
                   + "ORDER BY dr.requested_at DESC, dr.requested_id DESC";
        String like = "%" + keyword + "%";
        List<DocumentRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("requested_at");
                    list.add(new DocumentRequest(
                        rs.getInt("requested_id"),
                        rs.getString("student_no"),
                        rs.getString("student_name"),
                        rs.getString("document_type"),
                        nullToEmpty(rs.getString("purpose")),
                        rs.getString("status"),
                        ts == null ? "" : ts.toLocalDateTime().format(FMT),
                        nullToEmpty(rs.getString("processed_by"))));
                }
            }
        }
        return list;
    }

    // Returns the number of rows changed. 0 means it was no longer pending.
    public int updateStatus(int requestedId, String newStatus, String processedBy) throws SQLException {
        String sql = "UPDATE document_requests SET status = ?, processed_by = ? "
                   + "WHERE requested_id = ? AND status = 'pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, processedBy);
            ps.setInt(3, requestedId);
            return ps.executeUpdate();
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}