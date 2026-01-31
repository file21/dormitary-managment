package dorm.dao;

import dorm.model.Building;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Building operations.
 */
public interface BuildingRepository {
    /**
     * Find a building by name (used for authentication)
     */
    Optional<Building> findByName(String name);
    
    /**
     * Get all buildings
     */
    List<Building> findAll();
    
    /**
     * Save a new building
     */
    void save(Building building);
    
    /**
     * Update a building
     */
    void update(Building building);
    
    /**
     * Delete a building
     */
    void delete(Building building);
}
