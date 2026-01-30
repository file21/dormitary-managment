package edu.aau.dorm.dao;

import java.util.List;

/**
 * BlockExtensionDao: Extension methods for Block operations
 * 
 * Provides additional DAO methods for blocks that may not be in base BlockDao
 * Responsibilities:
 * - Query blocks with availability information
 * - Update capacity information
 * - Manage occupancy tracking
 * - Campus-level queries
 */
public class BlockExtensionDao {

    /**
     * Get all blocks with availability information
     * Returns blocks with current occupancy and available beds
     * 
     * @return List of block data with availability info
     */
    public List<?> getAllWithAvailability() {
        // TODO: Implement MySQL query with JOIN to get availability
        return List.of();
    }

    /**
     * Get available beds count in block
     * 
     * @param blockId Block ID
     * @return Count of available beds
     */
    public int getAvailableBeds(long blockId) {
        // TODO: Implement MySQL query
        return 0;
    }

    /**
     * Get total beds in block
     * 
     * @param blockId Block ID
     * @return Total bed capacity
     */
    public int getTotalBeds(long blockId) {
        // TODO: Implement MySQL query
        return 0;
    }

    /**
     * Update block capacity information
     * 
     * @param blockId Block ID
     * @param totalBeds Total bed capacity
     * @param maleBeds Beds designated for males
     * @param femaleBeds Beds designated for females
     */
    public void updateCapacity(long blockId, int totalBeds, int maleBeds, int femaleBeds) {
        // TODO: Implement MySQL update
    }

    /**
     * Decrement available beds count
     * Called when bed is occupied
     * 
     * @param blockId Block ID
     */
    public void decrementAvailableBeds(long blockId) {
        // TODO: Implement MySQL update
    }

    /**
     * Increment available beds count
     * Called when bed is freed (student withdrawal)
     * 
     * @param blockId Block ID
     */
    public void incrementAvailableBeds(long blockId) {
        // TODO: Implement MySQL update
    }

    /**
     * Get blocks by campus
     * 
     * @param campusId Campus ID
     * @return List of blocks in campus
     */
    public List<?> getByCampus(long campusId) {
        // TODO: Implement MySQL query
        return List.of();
    }

    /**
     * Get occupancy percentage
     * 
     * @param blockId Block ID
     * @return Percentage of beds occupied (0-100)
     */
    public double getOccupancyPercentage(long blockId) {
        // TODO: Implement calculation from database
        return 0.0;
    }

    /**
     * Check if block has available beds
     * 
     * @param blockId Block ID
     * @return true if available beds > 0
     */
    public boolean hasAvailableBeds(long blockId) {
        return getAvailableBeds(blockId) > 0;
    }

    /**
     * Get available beds for specific gender
     * 
     * @param blockId Block ID
     * @param gender Gender (MALE or FEMALE)
     * @return Count of available beds for gender
     */
    public int getAvailableBedsForGender(long blockId, String gender) {
        // TODO: Implement MySQL query
        return 0;
    }
}
