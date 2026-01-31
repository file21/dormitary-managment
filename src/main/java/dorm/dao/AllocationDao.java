package dorm.dao;

public interface AllocationDao {
    void allocate(long applicationId, long bedId, long assignedByUserId, String roomNumber);
    void freeBedIfAllocated(long applicationId);
}
