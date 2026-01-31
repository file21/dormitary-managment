package dorm.dao;

/**
 * Factory class for creating DAO instances.
 */
public class DaoFactory {
    
    private static UserRepository userRepository;
    private static StudentRepository studentRepository;
    private static ApplicationRepository applicationRepository;
    private static AnnouncementRepository announcementRepository;
    private static MessageRepository messageRepository;
    
    public static UserRepository createUserRepository() {
        if (userRepository == null) {
            userRepository = new CsvUserRepository();
        }
        return userRepository;
    }
    
    public static StudentRepository createStudentRepository() {
        if (studentRepository == null) {
            studentRepository = new CsvStudentRepository();
        }
        return studentRepository;
    }
    
    public static ApplicationRepository createApplicationRepository() {
        if (applicationRepository == null) {
            applicationRepository = new CsvApplicationRepository(createStudentRepository());
        }
        return applicationRepository;
    }
    
    public static AnnouncementRepository createAnnouncementRepository() {
        if (announcementRepository == null) {
            announcementRepository = new CsvAnnouncementRepository();
        }
        return announcementRepository;
    }
    
    public static MessageRepository createMessageRepository() {
        if (messageRepository == null) {
            messageRepository = new CsvMessageRepository();
        }
        return messageRepository;
    }
}
