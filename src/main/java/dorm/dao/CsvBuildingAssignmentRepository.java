package dorm.dao;

import dorm.model.BuildingAssignment;
import dorm.model.User;
import dorm.util.CsvHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CSV implementation of BuildingAssignmentRepository.
 * Demonstrates SRP - handles only building assignment CSV operations.
 */
public class CsvBuildingAssignmentRepository implements BuildingAssignmentRepository {
    
    private static final String FILENAME = "building_assignments.csv";
    private static final String HEADER = "proctor_id,building_name";
    
    private final UserRepository userRepository;
    
    public CsvBuildingAssignmentRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public List<BuildingAssignment> findAll() {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        List<BuildingAssignment> assignments = new ArrayList<>();
        
        for (String[] record : records) {
            if (record.length >= 2) {
                String proctorId = record[0];
                String buildingName = record[1];
                
                // Find the proctor user by id
                for (User user : userRepository.findAll()) {
                    if (user.getId().equals(proctorId)) {
                        assignments.add(new BuildingAssignment(user, buildingName));
                        break;
                    }
                }
            }
        }
        
        return assignments;
    }
    
    @Override
    public Optional<BuildingAssignment> findByProctor(User proctor) {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        
        for (String[] record : records) {
            if (record.length >= 2 && record[0].equals(proctor.getId())) {
                return Optional.of(new BuildingAssignment(proctor, record[1]));
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public void save(User proctor, String buildingName) {
        Optional<BuildingAssignment> existing = findByProctor(proctor);
        
        if (existing.isPresent()) {
            // Update existing record
            List<String[]> allRecords = CsvHelper.readAll(FILENAME, true);
            List<String[]> updatedRecords = new ArrayList<>();
            
            for (String[] record : allRecords) {
                if (record.length >= 2 && record[0].equals(proctor.getId())) {
                    updatedRecords.add(new String[] { proctor.getId(), buildingName });
                } else {
                    updatedRecords.add(record);
                }
            }
            
            CsvHelper.writeAll(FILENAME, HEADER, updatedRecords);
        } else {
            // Insert new record
            String[] record = { proctor.getId(), buildingName };
            CsvHelper.append(FILENAME, HEADER, record);
        }
    }
    
    @Override
    public void deleteByProctor(User proctor) {
        List<String[]> allRecords = CsvHelper.readAll(FILENAME, true);
        List<String[]> updatedRecords = new ArrayList<>();
        
        for (String[] record : allRecords) {
            if (record.length >= 1 && !record[0].equals(proctor.getId())) {
                updatedRecords.add(record);
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, updatedRecords);
    }
}
