package dorm.dao;

/**
 * Factory class for creating DAO instances.
 * 
 * Demonstrates Factory Pattern and Single Responsibility Principle.
 * This class is responsible only for creating and configuring DAO objects.
 * All repositories use CSV files for data persistence.
 */
public class DaoFactory {
    
    // Singleton instances to ensure consistency across the application
    private static UserRepository userRepository;
    private static StudentRepository studentRepository;
    private static ApplicationRepository applicationRepository;
    private static AnnouncementRepository announcementRepository;
    private static MessageRepository messageRepository;
    private static BuildingRepository buildingRepository;
    
    /**
     * Creates and returns a configured UserRepository
     */
    public static UserRepository createUserRepository() {
        if (userRepository == null) {
            userRepository = new CsvUserRepository();
        }
        return userRepository;
    }
    
    /**
     * Creates and returns a configured StudentRepository
     */
    public static StudentRepository createStudentRepository() {
        if (studentRepository == null) {
            studentRepository = new CsvStudentRepository();
        }
        return studentRepository;
    }
    
    /**
     * Creates and returns a configured ApplicationRepository
     */
    public static ApplicationRepository createApplicationRepository() {
        if (applicationRepository == null) {
            applicationRepository = new CsvApplicationRepository(createStudentRepository());
        }
        return applicationRepository;
    }
    
    /**
     * Creates and returns a configured AnnouncementRepository
     */
    public static AnnouncementRepository createAnnouncementRepository() {
        if (announcementRepository == null) {
            announcementRepository = new CsvAnnouncementRepository();
        }
        return announcementRepository;
    }
    
    /**
     * Creates and returns a configured MessageRepository
     */
    public static MessageRepository createMessageRepository() {
        if (messageRepository == null) {
            messageRepository = new CsvMessageRepository();
        }
        return messageRepository;
    }
    
    /**
     * Creates and returns a configured BuildingRepository
     */
    public static BuildingRepository createBuildingRepository() {
        if (buildingRepository == null) {
            buildingRepository = new CsvBuildingRepository();
        }
        return buildingRepository;
    }
}
