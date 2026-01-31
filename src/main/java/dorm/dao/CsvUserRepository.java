package dorm.dao;

import dorm.model.Role;
import dorm.model.User;
import dorm.util.CsvHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CSV implementation of UserRepository.
 * Demonstrates Dependency Inversion Principle - concrete class depends on abstraction.
 * Demonstrates Single Responsibility Principle - only handles User CSV operations.
 */
public class CsvUserRepository implements UserRepository {
    
    private static final String FILENAME = "users.csv";
    private static final String HEADER = "id,username,password,role,display_name";
    
    @Override
    public Optional<User> findByUsername(String username) {
        return readAllUsers().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }
    
    @Override
    public List<User> findByRole(Role role) {
        List<User> result = new ArrayList<>();
        for (User user : readAllUsers()) {
            if (user.getRole() == role) {
                result.add(user);
            }
        }
        return result;
    }
    
    @Override
    public List<User> findAll() {
        return readAllUsers();
    }
    
    @Override
    public void save(User user) {
        String[] record = {
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getRole().name(),
            user.getDisplayName()
        };
        CsvHelper.append(FILENAME, HEADER, record);
    }
    
    @Override
    public void delete(User user) {
        List<User> users = readAllUsers();
        List<String[]> records = new ArrayList<>();
        
        for (User u : users) {
            if (!u.getId().equals(user.getId())) {
                records.add(userToRecord(u));
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, records);
    }
    
    /**
     * Helper method to read all users from CSV
     */
    private List<User> readAllUsers() {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        List<User> users = new ArrayList<>();
        
        for (String[] record : records) {
            if (record.length >= 5) {
                users.add(recordToUser(record));
            }
        }
        
        return users;
    }
    
    /**
     * Convert CSV record to User object
     */
    private User recordToUser(String[] record) {
        return new User(
            record[0],  // id
            record[1],  // username
            record[2],  // password
            Role.valueOf(record[3]),  // role
            record[4]   // display_name
        );
    }
    
    /**
     * Convert User to CSV record
     */
    private String[] userToRecord(User user) {
        return new String[] {
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getRole().name(),
            user.getDisplayName()
        };
    }
}
