package model;

import java.time.LocalDate;

public class Student {
    private final int studentId;
    private final String studentNo;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String suffix;
    private final LocalDate birthDate;
    private final String gender;
    private final String address;
    private final String contactNo;
    private final String status;

    public Student(int studentId, String studentNo, String firstName, String middleName,
                   String lastName, String suffix, LocalDate birthDate, String gender,
                   String address, String contactNo, String status) {
        this.studentId = studentId;
        this.studentNo = studentNo;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.suffix = suffix;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.contactNo = contactNo;
        this.status = status;
    }

    public int getStudentId() { return studentId; }
    public String getStudentNo() { return studentNo; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getSuffix() { return suffix; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public String getContactNo() { return contactNo; }
    public String getStatus() { return status; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (middleName != null && !middleName.isEmpty()) sb.append(" ").append(middleName);
        sb.append(" ").append(lastName);
        if (suffix != null && !suffix.isEmpty()) sb.append(" ").append(suffix);
        return sb.toString();
    }
}