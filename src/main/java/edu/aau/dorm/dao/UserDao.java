package edu.aau.dorm.dao;

import edu.aau.dorm.model.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
}
