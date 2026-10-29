package dao;

import config.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Student;

public class StudentDAO {

    private static final String COLUMNS =
            "student_id, student_no, first_name, middle_name, last_name, suffix, "
          + "birth_date, gender, address, contact_no, status";

    private static final String NAME_EXPR =
            "CONCAT_WS(' ', first_name, middle_name, last_name, suffix)";

    // status: "All", "active", "dropped" or "graduated"
    public List<Student> findAll(String status, String keyword) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM students "
                   + "WHERE (? = 'All' OR status = ?) "
                   + "AND (student_no LIKE ? OR " + NAME_EXPR + " LIKE ?) "
                   + "ORDER BY last_name, first_name";
        String like = "%" + keyword + "%";
        List<Student> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // For the student picker: active students only, limited results
    public List<Student> searchActive(String keyword, int limit) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM students "
                   + "WHERE status = 'active' "
                   + "AND (student_no LIKE ? OR " + NAME_EXPR + " LIKE ?) "
                   + "ORDER BY last_name, first_name "
                   + "LIMIT ?";
        String like = "%" + keyword + "%";
        List<Student> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean studentNoExists(String studentNo) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM students WHERE student_no = ?")) {
            ps.setString(1, studentNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Returns the new student_id
    public int insert(Student s) throws SQLException {
        String sql = "INSERT INTO students (student_no, first_name, middle_name, last_name, suffix, "
                   + "birth_date, gender, address, contact_no) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getStudentNo());
            ps.setString(2, s.getFirstName());
            setNullable(ps, 3, s.getMiddleName());
            ps.setString(4, s.getLastName());
            setNullable(ps, 5, s.getSuffix());
            setDate(ps, 6, s.getBirthDate());
            setNullable(ps, 7, s.getGender());
            setNullable(ps, 8, s.getAddress());
            setNullable(ps, 9, s.getContactNo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    // student_no and status are NOT changed here
    public void update(Student s) throws SQLException {
        String sql = "UPDATE students SET first_name = ?, middle_name = ?, last_name = ?, suffix = ?, "
                   + "birth_date = ?, gender = ?, address = ?, contact_no = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            setNullable(ps, 2, s.getMiddleName());
            ps.setString(3, s.getLastName());
            setNullable(ps, 4, s.getSuffix());
            setDate(ps, 5, s.getBirthDate());
            setNullable(ps, 6, s.getGender());
            setNullable(ps, 7, s.getAddress());
            setNullable(ps, 8, s.getContactNo());
            ps.setInt(9, s.getStudentId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int studentId, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE students SET status = ? WHERE student_id = ?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    private Student map(ResultSet rs) throws SQLException {
        Date bd = rs.getDate("birth_date");
        LocalDate birth = bd == null ? null : bd.toLocalDate();
        return new Student(
            rs.getInt("student_id"),
            rs.getString("student_no"),
            rs.getString("first_name"),
            rs.getString("middle_name"),
            rs.getString("last_name"),
            rs.getString("suffix"),
            birth,
            rs.getString("gender"),
            rs.getString("address"),
            rs.getString("contact_no"),
            rs.getString("status"));
    }

    // Empty text is saved as NULL
    private void setNullable(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }
}