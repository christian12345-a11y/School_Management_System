package model;

public class DocumentRequest {
    private final int requestedId;
    private final String studentNo;
    private final String studentName;
    private final String documentType;
    private final String purpose;
    private final String status;
    private final String requestedAt;
    private final String processedBy;

    public DocumentRequest(int requestedId, String studentNo, String studentName,
                           String documentType, String purpose, String status,
                           String requestedAt, String processedBy) {
        this.requestedId = requestedId;
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.documentType = documentType;
        this.purpose = purpose;
        this.status = status;
        this.requestedAt = requestedAt;
        this.processedBy = processedBy;
    }

    public int getRequestedId() { return requestedId; }
    public String getStudentNo() { return studentNo; }
    public String getStudentName() { return studentName; }
    public String getDocumentType() { return documentType; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public String getRequestedAt() { return requestedAt; }
    public String getProcessedBy() { return processedBy; }
}