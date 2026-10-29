package controller;

import dao.AuditDAO;
import dao.DocumentRequestDAO;
import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.DocumentRequest;
import model.User;
import util.AlertUtil;
import util.Session;

public class DocumentRequestsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label countLabel;
    @FXML private TableView<DocumentRequest> requestsTable;
    @FXML private TableColumn<DocumentRequest, Integer> idColumn;
    @FXML private TableColumn<DocumentRequest, String> studentNoColumn;
    @FXML private TableColumn<DocumentRequest, String> studentNameColumn;
    @FXML private TableColumn<DocumentRequest, String> documentColumn;
    @FXML private TableColumn<DocumentRequest, String> purposeColumn;
    @FXML private TableColumn<DocumentRequest, String> requestedAtColumn;
    @FXML private TableColumn<DocumentRequest, String> statusColumn;
    @FXML private TableColumn<DocumentRequest, String> processedByColumn;

    private final DocumentRequestDAO requestDAO = new DocumentRequestDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRequestedId()));
        studentNoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentNo()));
        studentNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        documentColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDocumentType()));
        purposeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPurpose()));
        requestedAtColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRequestedAt()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        processedByColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProcessedBy()));

        statusColumn.setCellFactory(col -> new TableCell<DocumentRequest, String>() {
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
                    case "approved" -> setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                    case "rejected" -> setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                    case "pending"  -> setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        statusCombo.getItems().addAll("All", "pending", "approved", "rejected");
        statusCombo.setValue("All");

        searchField.textProperty().addListener((obs, oldV, newV) -> loadRequests());
        statusCombo.valueProperty().addListener((obs, oldV, newV) -> loadRequests());

        loadRequests();
    }

    @FXML
    private void handleRefresh() {
        loadRequests();
    }

    @FXML
    private void handleApprove() {
        process("approved");
    }

    @FXML
    private void handleReject() {
        process("rejected");
    }

    private void process(String newStatus) {
        DocumentRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.show(Alert.AlertType.WARNING, "Document Requests", "Please select a request first.");
            return;
        }
        if (!"pending".equals(selected.getStatus())) {
            AlertUtil.show(Alert.AlertType.WARNING, "Document Requests",
                    "Only pending requests can be processed.");
            return;
        }

        String verb = newStatus.equals("approved") ? "approve" : "reject";
        boolean ok = AlertUtil.confirm("Confirm",
                "Do you want to " + verb + " the request of " + selected.getStudentName()
                + " for " + selected.getDocumentType() + "?");
        if (!ok) return;

        User user = Session.getCurrentUser();
        try {
            int rows = requestDAO.updateStatus(selected.getRequestedId(), newStatus, user.getFullName());
            if (rows == 0) {
                AlertUtil.show(Alert.AlertType.WARNING, "Document Requests",
                        "This request was already processed by someone else.");
            } else {
                auditDAO.log(user.getUserId(), "UPDATE", "document_requests",
                        selected.getRequestedId(), "success");
            }
            loadRequests();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error", "Could not update the request.");
        }
    }

    private void loadRequests() {
        try {
            List<DocumentRequest> list =
                    requestDAO.findAll(statusCombo.getValue(), searchField.getText().trim());
            requestsTable.setItems(FXCollections.observableArrayList(list));
            countLabel.setText(list.size() + " request(s)");
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.show(Alert.AlertType.ERROR, "Database Error", "Could not load the requests.");
        }
    }
}