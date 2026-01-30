package edu.aau.dorm.service;

import edu.aau.dorm.dao.UserDao;
import edu.aau.dorm.dao.UserDaoPg;
import edu.aau.dorm.model.User;
import edu.aau.dorm.util.PasswordHasher;

public final class AuthService {
    private final UserDao userDao;

    public AuthService() {
        this(new UserDaoPg());
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String password) {
        User u = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!u.active()) 
            throw new IllegalStateException("Account is disabled");
        
        if (!PasswordHasher.verify(password, u.passwordHash()))
            throw new IllegalArgumentException("Invalid username or password");

        return u;
    }
}
