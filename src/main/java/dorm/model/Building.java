package dorm.model;

/**
 * Represents a dormitory building.
 * Buildings act as proctor accounts - proctors login using building credentials.
 */
public class Building {
    private final String id;
    private final String name;        // Used as login username (e.g., B501)
    private String password;
    private int maxCapacity;
    
    public Building(String id, String name, String password, int maxCapacity) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.maxCapacity = maxCapacity;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
