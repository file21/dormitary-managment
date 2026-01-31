package dorm.dao;

/**
 * Factory class for creating DAO instances.
 * 
 * Demonstrates Factory Pattern and Single Responsibility Principle.
 * This class is responsible only for creating and configuring DAO objects.
 */
public class DaoFactory {
    
    /**
     * Creates and returns a configured UserRepository
     */
    public static UserRepository createUserRepository() {
        return new MySqlUserRepository();
    }
    
    /**
     * Creates and returns a configured StudentRepository
     */
    public static StudentRepository createStudentRepository() {
        return new MySqlStudentRepository();
    }
    
    /**
     * Creates and returns a configured ApplicationRepository
     */
    public static ApplicationRepository createApplicationRepository() {
        return new MySqlApplicationRepository(createStudentRepository());
    }
    
    /**
     * Creates and returns a configured AnnouncementRepository
     */
    public static AnnouncementRepository createAnnouncementRepository() {
        return new MySqlAnnouncementRepository();
    }
    
    /**
     * Creates and returns a configured MessageRepository
     */
    public static MessageRepository createMessageRepository() {
        return new MySqlMessageRepository();
    }
    
    /**
     * Creates and returns a configured BuildingAssignmentRepository
     */
    public static BuildingAssignmentRepository createBuildingAssignmentRepository() {
        return new MySqlBuildingAssignmentRepository(createUserRepository());
    }
}
