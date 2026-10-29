package controller;

import dao.AuditDAO;
import dao.EnrollmentDAO;
import dao.SectionDAO;
import dao.StudentDAO;
import dao.SubjectDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import model.Section;
import model.Student;
import model.Subject;
import model.User;
import util.AlertUtil;
import util.Session;

public class EnrollFormController {

    private static final int MAX_SEARCH_RESULTS = 20;

    @FXML private ComboBox<Student> studentCombo;
    @FXML private Label searchHintLabel;
    @FXML private TextField schoolYearField;
    @FXML private ComboBox<String> gradeLevelCombo;
    @FXML private ComboBox<Section> sectionCombo;
    @FXML private DatePicker enrollmentDatePicker;
    @FXML private ListView<Subject> subjectList;
    @FXML private Button saveButton;

    private final StudentDAO studentDAO = new StudentDAO();
    private final SectionDAO sectionDAO = new SectionDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    private final PauseTransition searchDelay =
            new PauseTransition(Duration.millis(300));

    private boolean updating = false;
    private boolean saved = false;

    @FXML
    private void initialize() {

        if (studentCombo == null
                || searchHintLabel == null
                || schoolYearField == null
                || gradeLevelCombo == null
                || sectionCombo == null
                || enrollmentDatePicker == null
                || subjectList == null
                || saveButton == null) {

            throw new IllegalStateException(
                    "Check the fx:id values in EnrollForm.fxml."
            );
        }

        setupStudentSearch();

        gradeLevelCombo.getItems().setAll(
                "1st Year",
                "2nd Year",
                "3rd Year",
                "4th Year"
        );

        subjectList.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);

        schoolYearField.setText(defaultSchoolYear());
        enrollmentDatePicker.setValue(LocalDate.now());

        try {
            sectionCombo.setItems(
                    FXCollections.observableArrayList(
                            sectionDAO.findActive()
                    )
            );

            subjectList.setItems(
                    FXCollections.observableArrayList(
                            subjectDAO.findActive()
                    )
            );

        } catch (SQLException e) {
            e.printStackTrace();

            showError(
                    "Database Error",
                    "Could not load sections and subjects."
            );
        }
    }

    // --------------------------------------------------
    // STUDENT POWER SEARCH
    // --------------------------------------------------

    private void setupStudentSearch() {

        studentCombo.setEditable(true);
        studentCombo.setVisibleRowCount(10);

        studentCombo.setConverter(new StringConverter<Student>() {

            @Override
            public String toString(Student student) {
                return student == null ? "" : display(student);
            }

            @Override
            public Student fromString(String text) {

                if (text == null) {
                    return null;
                }

                String keyword = text.trim();

                for (Student student : studentCombo.getItems()) {
                    if (display(student).equalsIgnoreCase(keyword)) {
                        return student;
                    }
                }

                return null;
            }
        });

        searchDelay.setOnFinished(event -> searchStudents());

        studentCombo.getEditor().textProperty().addListener(
                (observable, oldText, newText) -> {

                    if (updating) {
                        return;
                    }

                    Student selected = studentCombo.getValue();

                    // Keep the selection when JavaFX displays
                    // the selected student's name in the editor.
                    if (selected != null
                            && display(selected).equals(
                                    newText == null ? "" : newText.trim())) {
                        return;
                    }

                    // The user has started a new search.
                    studentCombo.setValue(null);

                    searchDelay.playFromStart();
                }
        );

        // Clicking the dropdown arrow loads active students
        // when the search field is empty.
        studentCombo.setOnShowing(event -> {

            String keyword = studentCombo.getEditor().getText();

            if (keyword == null || keyword.trim().isEmpty()) {
                loadAllStudents();
            }
        });

        searchHintLabel.setText(
                "Type a student name or student number to search."
        );
    }

    private void loadAllStudents() {

        String editorText = studentCombo.getEditor().getText();

        if (editorText == null) {
            editorText = "";
        }

        int caret = studentCombo.getEditor().getCaretPosition();

        try {
            List<Student> students =
                    studentDAO.searchActive("", MAX_SEARCH_RESULTS);

            updateStudentItems(students, editorText, caret);

            searchHintLabel.setText(
                    students.isEmpty()
                    ? "No active students found."
                    : "Showing up to " + MAX_SEARCH_RESULTS
                      + " active students."
            );

        } catch (SQLException e) {
            e.printStackTrace();

            searchHintLabel.setText(
                    "Could not load active students."
            );
        }
    }

    private void searchStudents() {

        String editorText = studentCombo.getEditor().getText();

        if (editorText == null) {
            editorText = "";
        }

        String keyword = editorText.trim();
        int caret = studentCombo.getEditor().getCaretPosition();

        if (keyword.isEmpty()) {
            loadAllStudents();
            return;
        }

        try {
            List<Student> results =
                    studentDAO.searchActive(
                            keyword,
                            MAX_SEARCH_RESULTS
                    );

            updateStudentItems(results, editorText, caret);

            if (results.isEmpty()) {

                studentCombo.hide();

                searchHintLabel.setText(
                        "No active student found for \""
                        + keyword + "\"."
                );

            } else {

                searchHintLabel.setText(
                        results.size() == MAX_SEARCH_RESULTS
                        ? "Showing up to " + MAX_SEARCH_RESULTS
                          + " matches. Type more to narrow the search."
                        : results.size() + " match(es) found."
                );

                // Display matching results while typing.
                if (!studentCombo.isShowing()) {
                    studentCombo.show();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

            searchHintLabel.setText(
                    "Could not search students."
            );
        }
    }

    private void updateStudentItems(
            List<Student> students,
            String text,
            int caret) {

        TextField editor = studentCombo.getEditor();

        updating = true;

        try {
            studentCombo.getItems().setAll(students);

            // Do not erase what the user is typing.
            if (!text.equals(editor.getText())) {
                editor.setText(text);
                editor.positionCaret(
                        Math.min(caret, text.length())
                );
            }

        } finally {
            updating = false;
        }
    }

    // --------------------------------------------------
    // SAVE ENROLLMENT
    // --------------------------------------------------

    @FXML
    private void handleSave() {

        Student student = studentCombo.getValue();

        String editorText = studentCombo.getEditor().getText();

        String schoolYear = schoolYearField.getText() == null
                ? ""
                : schoolYearField.getText().trim();

        String gradeLevel = gradeLevelCombo.getValue();
        Section section = sectionCombo.getValue();
        LocalDate date = enrollmentDatePicker.getValue();

        List<Subject> subjects = new ArrayList<>(
                subjectList.getSelectionModel().getSelectedItems()
        );

        if (student == null
                || editorText == null
                || !display(student).equals(editorText.trim())) {

            warn("Please search and select a student from the dropdown.");
            return;
        }

        if (gradeLevel == null || section == null || date == null) {
            warn("Please select the grade level, section, and date.");
            return;
        }

        if (!isValidSchoolYear(schoolYear)) {
            warn("School Year must look like 2026-2027.");
            return;
        }

        if (subjects.isEmpty()) {
            warn("Please select at least one subject.");
            return;
        }

        User user = Session.getCurrentUser();

        if (user == null) {
            warn("Your session has expired. Please log in again.");
            return;
        }

        try {
            // Check for duplicate enrollment.
            List<String> duplicates = new ArrayList<>();

            for (Subject subject : subjects) {

                if (enrollmentDAO.isEnrolled(
                        student.getStudentId(),
                        subject.getSubjectId(),
                        schoolYear)) {

                    duplicates.add(subject.getSubjectCode());
                }
            }

            if (!duplicates.isEmpty()) {
                warn(
                        "Already enrolled in " + schoolYear
                        + ": " + String.join(", ", duplicates)
                );
                return;
            }

            // Check section capacity.
            if (sectionDAO.isFull(
                    section.getSectionId(),
                    student.getStudentId(),
                    schoolYear)) {

                warn(
                        "Section " + section.getSectionName()
                        + " is already full for " + schoolYear + "."
                );
                return;
            }

            List<Integer> subjectIds = new ArrayList<>();

            for (Subject subject : subjects) {
                subjectIds.add(subject.getSubjectId());
            }

            // Save enrollment.
            List<Integer> newIds = enrollmentDAO.enroll(
                    student.getStudentId(),
                    subjectIds,
                    schoolYear,
                    gradeLevel,
                    section.getSectionId(),
                    date
            );

            // Record successful enrollment actions.
            for (int id : newIds) {
                auditDAO.log(
                        user.getUserId(),
                        "CREATE",
                        "enrollment_subject",
                        id,
                        "success"
                );
            }

            AlertUtil.show(
                    Alert.AlertType.INFORMATION,
                    "Enrollment",
                    student.getFullName()
                    + " was enrolled in "
                    + newIds.size()
                    + " subject(s)."
            );

            saved = true;
            close();

        } catch (SQLException e) {
            e.printStackTrace();

            showError(
                    "Database Error",
                    "Could not save the enrollment."
            );
        }
    }

    // --------------------------------------------------
    // CANCEL AND HELPER METHODS
    // --------------------------------------------------

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {

        if (saveButton != null
                && saveButton.getScene() != null
                && saveButton.getScene().getWindow() != null) {

            ((Stage) saveButton.getScene()
                    .getWindow()).close();
        }
    }

    private void warn(String message) {

        AlertUtil.show(
                Alert.AlertType.WARNING,
                "Enrollment",
                message
        );
    }

    private void showError(String title, String message) {

        AlertUtil.show(
                Alert.AlertType.ERROR,
                title,
                message
        );
    }

    private String display(Student student) {
        return student.getStudentNo()
                + " - "
                + student.getFullName();
    }

    private boolean isValidSchoolYear(String schoolYear) {

        if (schoolYear == null
                || !schoolYear.matches("^\\d{4}-\\d{4}$")) {
            return false;
        }

        try {
            int first = Integer.parseInt(
                    schoolYear.substring(0, 4)
            );

            int second = Integer.parseInt(
                    schoolYear.substring(5)
            );

            return second == first + 1;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String defaultSchoolYear() {

        LocalDate today = LocalDate.now();

        int start = today.getMonthValue() >= 6
                ? today.getYear()
                : today.getYear() - 1;

        return start + "-" + (start + 1);
    }

    public boolean isSaved() {
        return saved;
    }
}