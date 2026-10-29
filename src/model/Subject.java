package model;

public class Subject {
    private final int subjectId;
    private final String subjectCode;
    private final String subjectName;
    private final double units;

    public Subject(int subjectId, String subjectCode, String subjectName, double units) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.units = units;
    }

    public int getSubjectId() { return subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public double getUnits() { return units; }

    @Override
    public String toString() {
        return subjectCode + " - " + subjectName + " (" + units + " units)";
    }
}