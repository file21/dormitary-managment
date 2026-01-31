package dorm.dao;

import dorm.model.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
}
