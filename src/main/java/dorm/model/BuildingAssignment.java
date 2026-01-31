package dorm.model;

public class BuildingAssignment {
    private final User proctor;
    private String buildingName;

    public BuildingAssignment(User proctor, String buildingName) {
        this.proctor = proctor;
        this.buildingName = buildingName;
    }

    public User getProctor() {
        return proctor;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
}
