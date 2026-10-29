package controller;

import dao.AuditDAO;
import dao.StudentDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Student;
import model.User;
import util.AlertUtil;
import util.Session;

public class StudentsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label countLabel;
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> studentNoColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> genderColumn;
    @FXML private TableColumn<Student, String> contactColumn;
    @FXML private TableColumn<Student, String> addressColumn;
    @FXML private TableColumn<Student, String> statusColumn;

    private final StudentDAO studentDAO = new StudentDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    @FXML
    private void initialize() {
        studentNoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentNo()));
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        genderColumn.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().getGender())));
        contactColumn.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().getContactNo())));
        addressColumn.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().getAddress())));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        statusColumn.setCellFactory(col -> new TableCell<Student, String>() {
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
                    case "graduated" -> setStyle("-fx-text-fill: #1d4ed8; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        studentsTable.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openForm(row.getItem());
                }
            });
            return row;
        });

        statusCombo.getItems().addAll("All", "active", "dropped", "graduated");
        statusCombo.setValue("All");

        searchField.textProperty().addListener((obs, o, n) -> loadStudents());
        statusCombo.valueProperty().addListener((obs, o, n) -> loadStudents());

        loadStudents();
    }

    @FXML
    private void handleRefresh() {
        loadStudents();
    }

    @FXML
    private void handleAdd() {
        openForm(null);
    }

    @FXML
    private void handleEdit() {
        Student selected = requireSelection();
        if (selected != null) openForm(selected);
    }

    @FXML
    private void handleChangeStatus() {
        Student selected = requireSelection();
        if (selected == null) return;

        ChoiceDialog<String> dialog =
                new ChoiceDialog<>(selected.getStatus(), "active", "dropped", "graduated");
        dialog.setTitle("Change Status");
        dialog.setHeaderText(null);
        dialog.setContentText("New status for " + selected.getFullName() + ":");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().equals(selected.getStatus())) return;

        User user = Session.getCurrentUser();
        try {
            studentDAO.updateStatus(selected.getStudentId(), result.get());
            auditDAO.log(user.getUserId(), "UPDATE", "students", selected.getStudentId(), "success");
            loadStudents();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error", "Could not change the status.");
        }
    }

    private void openForm(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StudentForm.fxml"));
            Parent root = loader.load();
            StudentFormController form = loader.getController();
            form.setStudent(student);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(studentsTable.getScene().getWindow());
            stage.setTitle(student == null ? "Add Student" : "Edit Student");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (form.isSaved()) loadStudents();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Error", "Cannot open the student form.");
        }
    }

    private Student requireSelection() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.show(Alert.AlertType.WARNING, "Students", "Please select a student first.");
        }
        return selected;
    }

    private void loadStudents() {
        try {
            List<Student> list = studentDAO.findAll(statusCombo.getValue(), searchField.getText().trim());
            studentsTable.setItems(FXCollections.observableArrayList(list));
            countLabel.setText(list.size() + " student(s)");
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error", "Could not load the students.");
        }
    }

    private String orEmpty(String s) {
        return s == null ? "" : s;
    }
}