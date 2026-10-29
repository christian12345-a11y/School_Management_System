package controller;

import dao.AuditDAO;
import dao.StudentDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Student;
import model.User;
import util.AlertUtil;
import util.Session;

public class StudentFormController {

    @FXML private Label titleLabel;
    @FXML private TextField studentNoField;
    @FXML private TextField firstNameField;
    @FXML private TextField middleNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField suffixField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField contactNoField;
    @FXML private TextField addressField;
    @FXML private Button saveButton;

    private final StudentDAO studentDAO = new StudentDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    private Student editing;   // null = Add mode
    private boolean saved = false;

    @FXML
    private void initialize() {
        genderCombo.getItems().addAll("Male", "Female");
    }

    // Called by StudentsController. Pass null for Add mode.
    public void setStudent(Student s) {
        editing = s;
        if (s == null) {
            titleLabel.setText("Add Student");
            return;
        }
        titleLabel.setText("Edit Student");
        studentNoField.setText(s.getStudentNo());
        studentNoField.setDisable(true);   // locked when editing
        firstNameField.setText(s.getFirstName());
        middleNameField.setText(nullToEmpty(s.getMiddleName()));
        lastNameField.setText(s.getLastName());
        suffixField.setText(nullToEmpty(s.getSuffix()));
        birthDatePicker.setValue(s.getBirthDate());
        genderCombo.setValue(s.getGender());
        contactNoField.setText(nullToEmpty(s.getContactNo()));
        addressField.setText(nullToEmpty(s.getAddress()));
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String studentNo = studentNoField.getText().trim();
        String first = firstNameField.getText().trim();
        String middle = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String suffix = suffixField.getText().trim();
        LocalDate birth = birthDatePicker.getValue();
        String gender = genderCombo.getValue();
        String contact = contactNoField.getText().trim();
        String address = addressField.getText().trim();

        if (studentNo.isEmpty() || first.isEmpty() || last.isEmpty()) {
            warn("Please fill in Student No., First Name, and Last Name.");
            return;
        }
        if (birth != null && birth.isAfter(LocalDate.now())) {
            warn("Birth date cannot be in the future.");
            return;
        }
        if (!contact.isEmpty() && !contact.matches("^09\\d{9}$")) {
            warn("Contact No. must be 11 digits and start with 09.");
            return;
        }

        User user = Session.getCurrentUser();
        try {
            if (editing == null) {
                if (studentDAO.studentNoExists(studentNo)) {
                    warn("Student No. " + studentNo + " already exists.");
                    return;
                }
                Student s = new Student(0, studentNo, first, middle, last, suffix,
                        birth, gender, address, contact, "active");
                int newId = studentDAO.insert(s);
                auditDAO.log(user.getUserId(), "CREATE", "students", newId, "success");
            } else {
                Student s = new Student(editing.getStudentId(), editing.getStudentNo(), first,
                        middle, last, suffix, birth, gender, address, contact, editing.getStatus());
                studentDAO.update(s);
                auditDAO.log(user.getUserId(), "UPDATE", "students", editing.getStudentId(), "success");
            }
            saved = true;
            close();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error", "Could not save the student.");
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    private void warn(String message) {
        AlertUtil.show(Alert.AlertType.WARNING, "Student", message);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}