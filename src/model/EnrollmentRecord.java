package model;

public class EnrollmentRecord {
    private final int enrollmentId;
    private final String studentNo;
    private final String studentName;
    private final String subject;
    private final String sectionName;
    private final String schoolYear;
    private final String gradeLevel;
    private final String enrollmentDate;
    private final String status;

    public EnrollmentRecord(int enrollmentId, String studentNo, String studentName, String subject,
                            String sectionName, String schoolYear, String gradeLevel,
                            String enrollmentDate, String status) {
        this.enrollmentId = enrollmentId;
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.subject = subject;
        this.sectionName = sectionName;
        this.schoolYear = schoolYear;
        this.gradeLevel = gradeLevel;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }

    public int getEnrollmentId() { return enrollmentId; }
    public String getStudentNo() { return studentNo; }
    public String getStudentName() { return studentName; }
    public String getSubject() { return subject; }
    public String getSectionName() { return sectionName; }
    public String getSchoolYear() { return schoolYear; }
    public String getGradeLevel() { return gradeLevel; }
    public String getEnrollmentDate() { return enrollmentDate; }
    public String getStatus() { return status; }
}