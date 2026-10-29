package controller;

import dao.AuditDAO;
import dao.UserDAO;
import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Role;
import model.User;
import util.AlertUtil;
import util.Navigator;
import util.PasswordUtil;
import util.Session;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private final UserDAO userDAO = new UserDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.show(Alert.AlertType.WARNING, "Login", "Please enter your username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(username, PasswordUtil.hash(password));

            if (user == null) {
                auditDAO.log(null, "LOGIN", "users", null, "failed");
                AlertUtil.show(Alert.AlertType.ERROR, "Login", "Invalid username or password.");
                return;
            }

            if (!"active".equals(user.getStatus()) || user.getRoleId() != Role.REGISTRAR) {
                auditDAO.log(user.getUserId(), "LOGIN", "users", user.getUserId(), "failed");
                AlertUtil.show(Alert.AlertType.ERROR, "Login",
                        "This account cannot access the Registrar system.");
                return;
            }

            auditDAO.log(user.getUserId(), "LOGIN", "users", user.getUserId(), "success");
            Session.setCurrentUser(user);
            Navigator.switchTo(loginButton, "Dashboard.fxml", "Registrar Dashboard");

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error",
                    "Cannot connect to the database. Is MySQL running?");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Error", "Cannot open the dashboard.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            Navigator.switchTo(loginButton, "Register.fxml", "Registrar Registration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}