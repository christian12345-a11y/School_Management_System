package controller;

import dao.DashboardDAO;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML private Label pendingLabel;
    @FXML private Label approvedLabel;
    @FXML private Label rejectedLabel;
    @FXML private Label studentsLabel;

    @FXML
    private void initialize() {
        DashboardDAO dao = new DashboardDAO();
        try {
            pendingLabel.setText(String.valueOf(dao.countRequests("pending")));
            approvedLabel.setText(String.valueOf(dao.countRequests("approved")));
            rejectedLabel.setText(String.valueOf(dao.countRequests("rejected")));
            studentsLabel.setText(String.valueOf(dao.countActiveStudents()));
        } catch (SQLException e) {
            e.printStackTrace();
            pendingLabel.setText("-");
            approvedLabel.setText("-");
            rejectedLabel.setText("-");
            studentsLabel.setText("-");
        }
    }
}