package controller;

import dao.AuditDAO;
import dao.EnrollmentDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.EnrollmentRecord;
import model.User;
import util.AlertUtil;
import util.Session;

public class EnrollmentController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> schoolYearCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label countLabel;
    @FXML private TableView<EnrollmentRecord> enrollmentTable;
    @FXML private TableColumn<EnrollmentRecord, String> studentNoColumn;
    @FXML private TableColumn<EnrollmentRecord, String> studentNameColumn;
    @FXML private TableColumn<EnrollmentRecord, String> subjectColumn;
    @FXML private TableColumn<EnrollmentRecord, String> sectionColumn;
    @FXML private TableColumn<EnrollmentRecord, String> schoolYearColumn;
    @FXML private TableColumn<EnrollmentRecord, String> gradeLevelColumn;
    @FXML private TableColumn<EnrollmentRecord, String> dateColumn;
    @FXML private TableColumn<EnrollmentRecord, String> statusColumn;

    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    private boolean loadingFilters = false;

    @FXML
    private void initialize() {
        studentNoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentNo()));
        studentNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        subjectColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubject()));
        sectionColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSectionName()));
        schoolYearColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSchoolYear()));
        gradeLevelColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGradeLevel()));
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEnrollmentDate()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        statusColumn.setCellFactory(col -> new TableCell<EnrollmentRecord, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status.toUpperCase());
                switch (status) {
                    case "active"    -> setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                    case "dropped"   -> setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                    case "completed" -> setStyle("-fx-text-fill: #1d4ed8; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        statusCombo.getItems().addAll("All", "active", "completed", "dropped");
        statusCombo.setValue("All");
        loadSchoolYears("All");

        searchField.textProperty().addListener((obs, o, n) -> loadEnrollments());
        schoolYearCombo.valueProperty().addListener((obs, o, n) -> loadEnrollments());
        statusCombo.valueProperty().addListener((obs, o, n) -> loadEnrollments());

        loadEnrollments();
    }

    @FXML
    private void handleRefresh() {
        loadSchoolYears(schoolYearCombo.getValue());
        loadEnrollments();
    }

    @FXML
    private void handleEnroll() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EnrollForm.fxml"));
            Parent root = loader.load();
            EnrollFormController form = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(enrollmentTable.getScene().getWindow());
            stage.setTitle("Enroll Student");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (form.isSaved()) {
                loadSchoolYears(schoolYearCombo.getValue());
                loadEnrollments();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Error", "Cannot open the enrollment form.");
        }
    }

    @FXML
    private void handleDrop() {
        EnrollmentRecord selected = enrollmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.show(Alert.AlertType.WARNING, "Enrollment", "Please select an enrollment first.");
            return;
        }
        if (!"active".equals(selected.getStatus())) {
            AlertUtil.show(Alert.AlertType.WARNING, "Enrollment", "Only active enrollments can be dropped.");
            return;
        }

        boolean ok = AlertUtil.confirm("Drop Subject",
                "Drop " + selected.getSubject() + " for " + selected.getStudentName()
                + " (" + selected.getSchoolYear() + ")?");
        if (!ok) return;

        User user = Session.getCurrentUser();
        try {
            int rows = enrollmentDAO.drop(selected.getEnrollmentId());
            if (rows == 0) {
                AlertUtil.show(Alert.AlertType.WARNING, "Enrollment",
                        "This enrollment is no longer active.");
            } else {
                auditDAO.log(user.getUserId(), "UPDATE", "enrollment_subject",
                        selected.getEnrollmentId(), "success");
            }
            loadEnrollments();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error", "Could not drop the subject.");
        }
    }

    // Fills the school year filter without triggering a reload for every change
    private void loadSchoolYears(String keepSelection) {
        loadingFilters = true;
        try {
            List<String> years = enrollmentDAO.findSchoolYears();
            schoolYearCombo.getItems().setAll("All");
            schoolYearCombo.getItems().addAll(years);
            schoolYearCombo.setValue(
                    keepSelection != null && schoolYearCombo.getItems().contains(keepSelection)
                            ? keepSelection : "All");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            loadingFilters = false;
        }
    }

    private void loadEnrollments() {
        if (loadingFilters || schoolYearCombo.getValue() == null || statusCombo.getValue() == null) return;
        try {
            List<EnrollmentRecord> list = enrollmentDAO.findAll(
                    schoolYearCombo.getValue(), statusCombo.getValue(), searchField.getText().trim());
            enrollmentTable.setItems(FXCollections.observableArrayList(list));
            countLabel.setText(list.size() + " record(s)");
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error", "Could not load the enrollments.");
        }
    }
}