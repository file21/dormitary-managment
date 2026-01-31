package dorm.dao;

import dorm.model.Gender;
import dorm.model.Residency;
import dorm.model.SponsorshipType;
import dorm.model.Student;
import dorm.util.CsvHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvStudentRepository implements StudentRepository {
    
    private static final String FILENAME = "students.csv";
    private static final String HEADER = "id,username,password,display_name,student_id,gender,residency,city,subcity,woreda,sponsorship_type,disability_info,emergency_contact_name,emergency_contact_phone,transaction_id,assigned_building";
    
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
    
    private List<Student> readAllStudents() {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        List<Student> students = new ArrayList<>();
        
        for (String[] record : records) {
            if (record.length >= 5) {
                students.add(recordToStudent(record));
            }
        }
        
        return students;
    }
    
    private Student recordToStudent(String[] record) {
        Gender gender = Gender.MALE;
        try {
            if (record.length > 5 && record[5] != null && !record[5].isEmpty()) {
                gender = Gender.valueOf(record[5]);
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
            gender      // gender
        );
        
        // residency
        if (record.length > 6 && record[6] != null && !record[6].isEmpty()) {
            try {
                student.setResidency(Residency.valueOf(record[6]));
            } catch (Exception e) { }
        }
        
        // city
        if (record.length > 7) {
            student.setCity(CsvHelper.emptyToNull(record[7]));
        }
        
        // subcity
        if (record.length > 8) {
            student.setSubcity(CsvHelper.emptyToNull(record[8]));
        }
        
        // woreda
        if (record.length > 9) {
            student.setWoreda(CsvHelper.emptyToNull(record[9]));
        }
        
        // sponsorship_type
        if (record.length > 10 && record[10] != null && !record[10].isEmpty()) {
            try {
                student.setSponsorshipType(SponsorshipType.valueOf(record[10]));
            } catch (Exception e) { }
        }
        
        // disability_info
        if (record.length > 11) {
            student.setDisabilityInfo(CsvHelper.emptyToNull(record[11]));
        }
        
        // emergency_contact_name
        if (record.length > 12) {
            student.setEmergencyContactName(CsvHelper.emptyToNull(record[12]));
        }
        
        // emergency_contact_phone
        if (record.length > 13) {
            student.setEmergencyContactPhone(CsvHelper.emptyToNull(record[13]));
        }
        
        // transaction_id
        if (record.length > 14) {
            student.setTransactionId(CsvHelper.emptyToNull(record[14]));
        }
        
        // assigned_building
        if (record.length > 15) {
            String building = CsvHelper.emptyToNull(record[15]);
            if (building != null) {
                student.setAssignedBuilding(building);
            }
        }
        
        return student;
    }
    
    private String[] studentToRecord(Student student) {
        return new String[] {
            student.getId(),
            student.getUsername(),
            student.getPassword(),
            student.getDisplayName(),
            student.getStudentId(),
            student.getGender() != null ? student.getGender().name() : Gender.MALE.name(),
            student.getResidency() != null ? student.getResidency().name() : "",
            CsvHelper.nullSafe(student.getCity()),
            CsvHelper.nullSafe(student.getSubcity()),
            CsvHelper.nullSafe(student.getWoreda()),
            student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "",
            CsvHelper.nullSafe(student.getDisabilityInfo()),
            CsvHelper.nullSafe(student.getEmergencyContactName()),
            CsvHelper.nullSafe(student.getEmergencyContactPhone()),
            CsvHelper.nullSafe(student.getTransactionId()),
            CsvHelper.nullSafe(student.getAssignedBuilding())
        };
    }
}
