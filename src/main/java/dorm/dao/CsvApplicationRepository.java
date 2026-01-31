package dorm.dao;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.model.Student;
import dorm.util.CsvHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CSV implementation of ApplicationRepository.
 * Demonstrates SRP - focused on application CSV operations only.
 */
public class CsvApplicationRepository implements ApplicationRepository {
    
    private static final String FILENAME = "applications.csv";
    private static final String HEADER = "id,student_id,status,admin_note";
    
    private final StudentRepository studentRepository;
    
    public CsvApplicationRepository(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    
    @Override
    public Optional<DormApplication> findByStudent(Student student) {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        
        for (String[] record : records) {
            if (record.length >= 3 && record[1].equals(student.getId())) {
                return Optional.of(recordToApplication(record, student));
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<DormApplication> findAll() {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        List<DormApplication> applications = new ArrayList<>();
        
        for (String[] record : records) {
            if (record.length >= 3) {
                String studentUserId = record[1];
                
                // Find the student by looking up their user_id match
                for (Student student : studentRepository.findAll()) {
                    if (student.getId().equals(studentUserId)) {
                        applications.add(recordToApplication(record, student));
                        break;
                    }
                }
            }
        }
        
        return applications;
    }
    
    @Override
    public void save(DormApplication application) {
        String[] record = applicationToRecord(application);
        CsvHelper.append(FILENAME, HEADER, record);
    }
    
    @Override
    public void update(DormApplication application) {
        List<String[]> allRecords = CsvHelper.readAll(FILENAME, true);
        List<String[]> updatedRecords = new ArrayList<>();
        
        for (String[] record : allRecords) {
            if (record.length >= 3 && record[0].equals(application.getId())) {
                updatedRecords.add(applicationToRecord(application));
            } else {
                updatedRecords.add(record);
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, updatedRecords);
    }
    
    @Override
    public void delete(DormApplication application) {
        List<String[]> allRecords = CsvHelper.readAll(FILENAME, true);
        List<String[]> updatedRecords = new ArrayList<>();
        
        for (String[] record : allRecords) {
            if (record.length >= 1 && !record[0].equals(application.getId())) {
                updatedRecords.add(record);
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, updatedRecords);
    }
    
    /**
     * Convert CSV record to DormApplication object
     */
    private DormApplication recordToApplication(String[] record, Student student) {
        DormApplication app = new DormApplication(record[0], student);
        app.setStatus(ApplicationStatus.valueOf(record[2]));
        
        if (record.length > 3) {
            app.setAdminNote(CsvHelper.emptyToNull(record[3]));
        }
        
        return app;
    }
    
    /**
     * Convert DormApplication to CSV record
     */
    private String[] applicationToRecord(DormApplication application) {
        return new String[] {
            application.getId(),
            application.getStudent().getId(),
            application.getStatus().name(),
            CsvHelper.nullSafe(application.getAdminNote())
        };
    }
}
