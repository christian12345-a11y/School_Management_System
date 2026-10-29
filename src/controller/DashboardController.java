package controller;

import dao.AuditDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import model.User;
import util.AlertUtil;
import util.Navigator;
import util.Session;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private StackPane contentArea;
    @FXML private Button navHomeButton;
    @FXML private Button navStudentsButton;
    @FXML private Button navEnrollmentButton;
    @FXML private Button navRequestsButton;

    private final AuditDAO auditDAO = new AuditDAO();

    @FXML
    private void initialize() {
        User user = Session.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
        }
        showHome();
    }

    @FXML
    private void showHome() {
        loadView("Home.fxml", navHomeButton);
    }

    @FXML
    private void showStudents() {
        loadView("Students.fxml", navStudentsButton);
    }

    @FXML
    private void showEnrollment() {
        loadView("Enrollment.fxml", navEnrollmentButton);
    }

    @FXML
    private void showRequests() {
        loadView("DocumentRequests.fxml", navRequestsButton);
    }

    private void loadView(String fxmlFile, Button activeButton) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/view/" + fxmlFile));
            contentArea.getChildren().setAll(view);

            for (Button b : List.of(navHomeButton, navStudentsButton,
                                    navEnrollmentButton, navRequestsButton)) {
                b.getStyleClass().remove("nav-button-active");
            }
            activeButton.getStyleClass().add("nav-button-active");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Error", "Cannot open " + fxmlFile);
        }
    }

    @FXML
    private void handleLogout() {
        User user = Session.getCurrentUser();
        try {
            if (user != null) {
                auditDAO.log(user.getUserId(), "LOGOUT", "users", user.getUserId(), "success");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Session.clear();
        try {
            Navigator.switchTo(userNameLabel, "Login.fxml", "Registrar Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}