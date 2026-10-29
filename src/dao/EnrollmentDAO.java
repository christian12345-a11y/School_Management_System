package dao;

import config.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.EnrollmentRecord;

public class EnrollmentDAO {

    private static final String NAME_EXPR =
            "CONCAT_WS(' ', s.first_name, s.middle_name, s.last_name, s.suffix)";

    // schoolYear / status: "All" means no filter
    public List<EnrollmentRecord> findAll(String schoolYear, String status, String keyword)
            throws SQLException {
        String sql = "SELECT es.Enrollment_subject_id, s.student_no, " + NAME_EXPR + " AS student_name, "
                   + "sub.subject_code, sub.subject_name, sec.section_name, "
                   + "es.school_year, es.grade_level, es.enrollment_date, es.status "
                   + "FROM enrollment_subject es "
                   + "JOIN students s ON s.student_id = es.Student_id "
                   + "JOIN subject sub ON sub.subject_id = es.Subject_id "
                   + "LEFT JOIN section sec ON sec.Section_id = es.Section_id "
                   + "WHERE (? = 'All' OR es.school_year = ?) "
                   + "AND (? = 'All' OR es.status = ?) "
                   + "AND (s.student_no LIKE ? OR " + NAME_EXPR + " LIKE ? "
                   + "     OR sub.subject_code LIKE ? OR sub.subject_name LIKE ?) "
                   + "ORDER BY es.school_year DESC, s.last_name, s.first_name, sub.subject_code";
        String like = "%" + keyword + "%";
        List<EnrollmentRecord> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schoolYear);
            ps.setString(2, schoolYear);
            ps.setString(3, status);
            ps.setString(4, status);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);
            ps.setString(8, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("enrollment_date");
                    String sectionName = rs.getString("section_name");
                    list.add(new EnrollmentRecord(
                        rs.getInt("Enrollment_subject_id"),
                        rs.getString("student_no"),
                        rs.getString("student_name"),
                        rs.getString("subject_code") + " - " + rs.getString("subject_name"),
                        sectionName == null ? "" : sectionName,
                        rs.getString("school_year"),
                        nullToEmpty(rs.getString("grade_level")),
                        d == null ? "" : d.toString(),
                        rs.getString("status")));
                }
            }
        }
        return list;
    }

    public List<String> findSchoolYears() throws SQLException {
        List<String> years = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT school_year FROM enrollment_subject ORDER BY school_year DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) years.add(rs.getString(1));
        }
        return years;
    }

    // Dropped rows do not count as enrolled
    public boolean isEnrolled(int studentId, int subjectId, String schoolYear) throws SQLException {
        String sql = "SELECT 1 FROM enrollment_subject "
                   + "WHERE Student_id = ? AND Subject_id = ? AND school_year = ? AND status <> 'dropped'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setString(3, schoolYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // All subjects are saved together. If one fails, none are saved.
    // Returns the new Enrollment_subject_id values.
    public List<Integer> enroll(int studentId, List<Integer> subjectIds, String schoolYear,
                                String gradeLevel, int sectionId, LocalDate date) throws SQLException {
        String sql = "INSERT INTO enrollment_subject "
                   + "(Student_id, Subject_id, school_year, grade_level, enrollment_date, status, Section_id) "
                   + "VALUES (?, ?, ?, ?, ?, 'active', ?)";
        List<Integer> ids = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (int subjectId : subjectIds) {
                    ps.setInt(1, studentId);
                    ps.setInt(2, subjectId);
                    ps.setString(3, schoolYear);
                    ps.setString(4, gradeLevel);
                    ps.setDate(5, Date.valueOf(date));
                    ps.setInt(6, sectionId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) ids.add(keys.getInt(1));
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return ids;
    }

    // Returns rows changed. 0 means it was no longer active.
    public int drop(int enrollmentId) throws SQLException {
        String sql = "UPDATE enrollment_subject SET status = 'dropped' "
                   + "WHERE Enrollment_subject_id = ? AND status = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            return ps.executeUpdate();
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}