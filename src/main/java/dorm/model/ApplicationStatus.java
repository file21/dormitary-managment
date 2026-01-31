package dorm.model;

public enum ApplicationStatus {
    // Phase One statuses
    PHASE_ONE_PENDING,      // Initial application submitted, waiting for review
    PHASE_ONE_APPROVED,     // Phase one approved (self-sponsored need to do phase two)
    PHASE_ONE_DECLINED,     // Application declined
    PHASE_ONE_RESUBMIT,     // Asked to resubmit with corrections
    
    // Phase Two statuses (only for self-sponsored students)
    PHASE_TWO_PENDING,      // Payment slip submitted, waiting for verification
    PHASE_TWO_APPROVED,     // Payment verified, ready for building assignment
    PHASE_TWO_DECLINED,     // Payment rejected
    
    // Final status
    ASSIGNED                // Building assigned, application complete
}
