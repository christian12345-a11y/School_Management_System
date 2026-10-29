package model;

public class Section {
    private final int sectionId;
    private final String sectionName;
    private final Integer capacity;

    public Section(int sectionId, String sectionName, Integer capacity) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.capacity = capacity;
    }

    public int getSectionId() { return sectionId; }
    public String getSectionName() { return sectionName; }
    public Integer getCapacity() { return capacity; }

    @Override
    public String toString() {
        return capacity == null ? sectionName : sectionName + " (capacity " + capacity + ")";
    }
}