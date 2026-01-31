package dorm.dao;

import dorm.model.Gender;
import dorm.model.SponsorshipType;
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
    private static final String HEADER = "id,username,password,display_name,student_id,city,gender,sponsorship_type,disability_info,document_paths,payment_slip_path,assigned_building,entry_date,withdrawal_date";
    
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
            if (record.length >= 7) {
                students.add(recordToStudent(record));
            }
        }
        
        return students;
    }
    
    /**
     * Convert CSV record to Student object
     */
    private Student recordToStudent(String[] record) {
        Gender gender = Gender.MALE;
        try {
            if (record.length > 6 && record[6] != null && !record[6].isEmpty()) {
                gender = Gender.valueOf(record[6]);
            }
        } catch (Exception e) {
            gender = Gender.MALE;
        }
        
        Student student = new Student(
            record[0],  // id
            record[1],  // username
            record[2],  // password
            record[3],  // display_name
            record[4],  // student_id
            record[5],  // city
            gender      // gender
        );
        
        // sponsorship_type
        if (record.length > 7 && record[7] != null && !record[7].isEmpty()) {
            try {
                student.setSponsorshipType(SponsorshipType.valueOf(record[7]));
            } catch (Exception e) {
                // ignore invalid value
            }
        }
        
        // disability_info
        if (record.length > 8) {
            student.setDisabilityInfo(CsvHelper.emptyToNull(record[8]));
        }
        
        // document_paths (semicolon-separated)
        if (record.length > 9 && record[9] != null && !record[9].isEmpty()) {
            String[] paths = record[9].split(";");
            for (String path : paths) {
                if (!path.trim().isEmpty()) {
                    student.addDocumentPath(path.trim());
                }
            }
        }
        
        // payment_slip_path
        if (record.length > 10) {
            student.setPaymentSlipPath(CsvHelper.emptyToNull(record[10]));
        }
        
        // assigned_building
        if (record.length > 11) {
            student.setAssignedBuilding(CsvHelper.emptyToNull(record[11]));
        }
        
        // entry_date
        if (record.length > 12) {
            student.setEntryDate(CsvHelper.emptyToNull(record[12]));
        }
        
        // withdrawal_date
        if (record.length > 13) {
            student.setWithdrawalDate(CsvHelper.emptyToNull(record[13]));
        }
        
        return student;
    }
    
    /**
     * Convert Student to CSV record
     */
    private String[] studentToRecord(Student student) {
        // Join document paths with semicolon
        String docPaths = String.join(";", student.getDocumentPaths());
        
        return new String[] {
            student.getId(),
            student.getUsername(),
            student.getPassword(),
            student.getDisplayName(),
            student.getStudentId(),
            CsvHelper.nullSafe(student.getCity()),
            student.getGender() != null ? student.getGender().name() : Gender.MALE.name(),
            student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "",
            CsvHelper.nullSafe(student.getDisabilityInfo()),
            docPaths,
            CsvHelper.nullSafe(student.getPaymentSlipPath()),
            CsvHelper.nullSafe(student.getAssignedBuilding()),
            CsvHelper.nullSafe(student.getEntryDate()),
            CsvHelper.nullSafe(student.getWithdrawalDate())
        };
    }
}
