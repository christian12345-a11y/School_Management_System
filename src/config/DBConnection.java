package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // XAMPP defaults: user "root", empty password
    private static final String URL =
            "jdbc:mysql://localhost:3306/school_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver jar is missing from the classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}