package dorm.dao;

import dorm.model.Student;
import dorm.util.CsvHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CSV implementation of StudentRepository.
 * Demonstrates SRP - only handles Student CSV operations.
 * Demonstrates DIP - implements abstraction, can be replaced with another implementation.
 */
public class CsvStudentRepository implements StudentRepository {
    
    private static final String FILENAME = "students.csv";
    private static final String HEADER = "id,username,password,display_name,student_id,city,sponsorship_type,disability_info,assigned_building,entry_date,withdrawal_date";
    
    @Override
    public Optional<Student> findByStudentId(String studentId) {
        return readAllStudents().stream()
                .filter(student -> student.getStudentId().equals(studentId))
                .findFirst();
    }
    
    @Override
    public List<Student> findAll() {
        return readAllStudents();
    }
    
    @Override
    public List<Student> findByBuilding(String buildingName) {
        List<Student> result = new ArrayList<>();
        for (Student student : readAllStudents()) {
            if (buildingName.equals(student.getAssignedBuilding())) {
                result.add(student);
            }
        }
        return result;
    }
    
    @Override
    public void save(Student student) {
        String[] record = studentToRecord(student);
        CsvHelper.append(FILENAME, HEADER, record);
    }
    
    @Override
    public void update(Student student) {
        List<Student> students = readAllStudents();
        List<String[]> records = new ArrayList<>();
        
        for (Student s : students) {
            if (s.getId().equals(student.getId())) {
                records.add(studentToRecord(student));
            } else {
                records.add(studentToRecord(s));
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, records);
    }
    
    /**
     * Helper method to read all students from CSV
     */
    private List<Student> readAllStudents() {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        List<Student> students = new ArrayList<>();
        
        for (String[] record : records) {
            if (record.length >= 6) {
                students.add(recordToStudent(record));
            }
        }
        
        return students;
    }
    
    /**
     * Convert CSV record to Student object
     */
    private Student recordToStudent(String[] record) {
        Student student = new Student(
            record[0],  // id
            record[1],  // username
            record[2],  // password
            record[3],  // display_name
            record[4],  // student_id
            record[5]   // city
        );
        
        if (record.length > 6) {
            student.setSponsorshipType(CsvHelper.emptyToNull(record[6]));
        }
        if (record.length > 7) {
            student.setDisabilityInfo(CsvHelper.emptyToNull(record[7]));
        }
        if (record.length > 8) {
            student.setAssignedBuilding(CsvHelper.emptyToNull(record[8]));
        }
        if (record.length > 9) {
            student.setEntryDate(CsvHelper.emptyToNull(record[9]));
        }
        if (record.length > 10) {
            student.setWithdrawalDate(CsvHelper.emptyToNull(record[10]));
        }
        
        return student;
    }
    
    /**
     * Convert Student to CSV record
     */
    private String[] studentToRecord(Student student) {
        return new String[] {
            student.getId(),
            student.getUsername(),
            student.getPassword(),
            student.getDisplayName(),
            student.getStudentId(),
            CsvHelper.nullSafe(student.getCity()),
            CsvHelper.nullSafe(student.getSponsorshipType()),
            CsvHelper.nullSafe(student.getDisabilityInfo()),
            CsvHelper.nullSafe(student.getAssignedBuilding()),
            CsvHelper.nullSafe(student.getEntryDate()),
            CsvHelper.nullSafe(student.getWithdrawalDate())
        };
    }
}
