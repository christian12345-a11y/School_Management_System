package controller;

import dao.UserDAO;
import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Role;
import util.AlertUtil;
import util.Navigator;
import util.PasswordUtil;

public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField middleNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField suffixField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister() {
        String first = firstNameField.getText().trim();
        String middle = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String suffix = suffixField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (first.isEmpty() || last.isEmpty() || email.isEmpty()
                || username.isEmpty() || password.isEmpty()) {
            warn("Please fill in all required fields (*).");
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            warn("Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            warn("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            warn("Passwords do not match.");
            return;
        }

        // full_name = first [middle] last [suffix]
        StringBuilder fullName = new StringBuilder(first);
        if (!middle.isEmpty()) fullName.append(" ").append(middle);
        fullName.append(" ").append(last);
        if (!suffix.isEmpty()) fullName.append(" ").append(suffix);

        try {
            if (userDAO.usernameExists(username)) {
                warn("That username is already taken.");
                return;
            }
            if (userDAO.emailExists(email)) {
                warn("That email is already registered.");
                return;
            }

            userDAO.create(Role.REGISTRAR, username, PasswordUtil.hash(password),
                    fullName.toString(), email);

            AlertUtil.show(Alert.AlertType.INFORMATION, "Registration",
                    "Account created. You can now sign in.");
            handleBack();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error",
                    "Could not create the account. Is MySQL running?");
        }
    }

    @FXML
    private void handleBack() {
        try {
            Navigator.switchTo(registerButton, "Login.fxml", "Registrar Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void warn(String message) {
        AlertUtil.show(Alert.AlertType.WARNING, "Registration", message);
    }
}