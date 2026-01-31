package dorm.dao;

import dorm.model.Building;
import dorm.util.CsvHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CSV implementation of BuildingRepository.
 */
public class CsvBuildingRepository implements BuildingRepository {
    
    private static final String FILENAME = "buildings.csv";
    private static final String HEADER = "id,name,password,max_capacity";
    
    @Override
    public Optional<Building> findByName(String name) {
        return readAllBuildings().stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst();
    }
    
    @Override
    public List<Building> findAll() {
        return readAllBuildings();
    }
    
    @Override
    public void save(Building building) {
        String[] record = buildingToRecord(building);
        CsvHelper.append(FILENAME, HEADER, record);
    }
    
    @Override
    public void update(Building building) {
        List<Building> buildings = readAllBuildings();
        List<String[]> records = new ArrayList<>();
        
        for (Building b : buildings) {
            if (b.getId().equals(building.getId())) {
                records.add(buildingToRecord(building));
            } else {
                records.add(buildingToRecord(b));
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, records);
    }
    
    @Override
    public void delete(Building building) {
        List<Building> buildings = readAllBuildings();
        List<String[]> records = new ArrayList<>();
        
        for (Building b : buildings) {
            if (!b.getId().equals(building.getId())) {
                records.add(buildingToRecord(b));
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, records);
    }
    
    private List<Building> readAllBuildings() {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        List<Building> buildings = new ArrayList<>();
        
        for (String[] record : records) {
            if (record.length >= 4) {
                buildings.add(recordToBuilding(record));
            }
        }
        
        return buildings;
    }
    
    private Building recordToBuilding(String[] record) {
        int maxCapacity = 100; // default
        try {
            maxCapacity = Integer.parseInt(record[3]);
        } catch (NumberFormatException e) {
            // use default
        }
        
        return new Building(
            record[0],  // id
            record[1],  // name
            record[2],  // password
            maxCapacity // max_capacity
        );
    }
    
    private String[] buildingToRecord(Building building) {
        return new String[] {
            building.getId(),
            building.getName(),
            building.getPassword(),
            String.valueOf(building.getMaxCapacity())
        };
    }
}
