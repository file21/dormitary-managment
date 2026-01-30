package edu.aau.dorm.dao;

import edu.aau.dorm.model.Bed;
import java.util.List;

/**
 * BedDao: Data Access Object for Bed operations
 * 
 * Responsibilities:
 * - CRUD operations for beds
 * - Query bed availability and occupancy
 * - Update bed status (occupied/available)
 * - Query beds by block, gender, or status
 * 
 * Design: Interface-based for flexibility (DIP)
 * Implementations: MySQL, PostgreSQL, etc.
 */
public class BedDao {

    /**
     * Get bed by ID
     * 
     * @param id Bed ID
     * @return Bed object or null if not found
     */
    public Bed getById(long id) {
        // TODO: Implement MySQL query
        return null;
    }

    /**
     * Get all beds in a block
     * 
     * @param blockId Block ID
     * @return List of beds in block
     */
    public List<Bed> getByBlockId(long blockId) {
        // TODO: Implement MySQL query
        return List.of();
    }

    /**
     * Count available beds in block by gender
     * 
     * @param blockId Block ID
     * @param gender Gender (MALE, FEMALE)
     * @return Count of available beds
     */
    public int countAvailableByBlockAndGender(long blockId, String gender) {
        // TODO: Implement MySQL query
        return 0;
    }

    /**
     * Mark bed as occupied or unoccupied
     * 
     * @param bedId Bed ID
     * @param occupied true to mark occupied, false to mark available
     */
    public void markOccupied(long bedId, boolean occupied) {
        // TODO: Implement MySQL update
    }

    /**
     * Get block ID for a bed
     * 
     * @param bedId Bed ID
     * @return Block ID
     */
    public long getBlockIdByBedId(long bedId) {
        // TODO: Implement MySQL query
        return 0;
    }

    /**
     * Get count of available beds in block
     * 
     * @param blockId Block ID
     * @return Count of available beds
     */
    public int getAvailableBedCount(long blockId) {
        // TODO: Implement MySQL query
        return 0;
    }

    /**
     * Create new bed
     * 
     * @param blockId Block ID
     * @param bedLabel Bed label/identifier
     * @return Created bed ID
     */
    public long create(long blockId, String bedLabel) {
        // TODO: Implement MySQL insert
        return 0;
    }

    /**
     * Get all available beds in block
     * 
     * @param blockId Block ID
     * @return List of available beds
     */
    public List<Bed> getAvailableBeds(long blockId) {
        // TODO: Implement MySQL query
        return List.of();
    }

    /**
     * Check if bed is available
     * 
     * @param bedId Bed ID
     * @return true if available, false otherwise
     */
    public boolean isAvailable(long bedId) {
        // TODO: Implement MySQL query
        return false;
    }
}
