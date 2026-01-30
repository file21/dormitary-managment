package edu.aau.dorm.service;

import edu.aau.dorm.dao.UserDao;
import edu.aau.dorm.dao.UserDaoPg;
import edu.aau.dorm.model.User;
import edu.aau.dorm.util.PasswordHasher;

/**
 * OOP & SOLID DEMO: Authentication Service
 * 
 * SINGLE RESPONSIBILITY PRINCIPLE:
 * - This class ONLY handles login/authentication
 * - NOT handling UI, NOT handling databases directly, NOT handling notifications
 * - If authentication logic changes, only this class needs updating
 * 
 * ENCAPSULATION:
 * - userDao is private final - can't be changed from outside
 * - Only one public method: login()
 * 
 * ABSTRACTION:
 * - Uses PasswordHasher and UserDao - hides complex implementation details
 * - Caller doesn't need to know HOW password is verified
 * 
 * DEPENDENCY INVERSION:
 * - Depends on UserDao interface (but could be better - see note below)
 */
public final class AuthService {
    private final UserDao userDao;  // NOTE: Could be injected via constructor (better practice)

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    // Default constructor for backward compatibility
    public AuthService() {
        this(new UserDaoPg());
    }

    /**
     * Authenticate user by username and password.
     * 
     * Business logic:
     * 1. Find user by username
     * 2. Check if account is active
     * 3. Verify password matches hash
     * 
     * @param username User's username
     * @param password User's plaintext password (will be hashed for verification)
     * @return User object if authentication successful
     * @throws IllegalArgumentException if username/password invalid
     * @throws IllegalStateException if account is disabled
     */
    public User login(String username, String password) {
        // Step 1: Find user by username (uses UserDao abstraction)
        User u = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // Step 2: Check account is active (encapsulation - using getter method)
        if (!u.active()) 
            throw new IllegalStateException("Account is disabled");
        
        // Step 3: Verify password (uses PasswordHasher - hides complexity)
        if (!PasswordHasher.verify(password, u.passwordHash()))
            throw new IllegalArgumentException("Invalid username or password");

        return u;  // Success - return authenticated user
    }
}
