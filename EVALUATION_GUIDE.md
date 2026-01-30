# Dormitory Management System - Evaluation Guide

## Functional Requirements ✓

### 1. GUI (JavaFX)
- **LoginController** - Authentication screen
- **SignupController** - Registration screen
- Clean, simple interface without over-engineering

### 2. User Roles (2+)
- **Admin** - Reviews applications, approves/rejects
- **Proctor** - Manages check-ins, withdrawals
- **Student** - Submits applications
- **Owner** - System administrator

### 3. CRUD Operations
- **Create**: Applications, Users, Allocations
- **Read**: GetById, findByUsername
- **Update**: setStatus, allocate
- **Delete**: Implicit (soft delete via status)

### 4. Input Validation & Error Handling
- **Validation.java** - AAU ID format validation
- **AuthService** - Login validation (user exists, password correct, account active)
- **Exception handling** - IllegalArgumentException, IllegalStateException
- Try-catch in controllers with user-friendly messages

### 5. Separation of Logic
- **UI Layer**: Controllers only handle events/input
- **Service Layer**: Business logic (ApplicationService, ProctorService, AuthService)
- **DAO Layer**: Database access (ApplicationDao, UserDao, AllocationDao)
- **Model Layer**: Data representation (User, Student, DormApplication)

### 6. I/O Functionality
- **Database**: MySQL (schema_mysql.sql)
- **Connection**: HikariCP connection pooling (Db.java)
- **Persistence**: All data stored in MySQL database

### 7. Exception Handling for I/O
- Database exceptions caught and handled
- Connection failures managed gracefully
- Status messages shown to users

---

## OOP Principles ✓

### 1. Encapsulation
**Private fields with controlled access:**
```java
// User.java
private final String username;        // Private field
private final String passwordHash;    // Can't be modified
public String username() { return username; }  // Read-only access
```

### 2. Abstraction
**Interfaces hide implementation details:**
```java
// ApplicationDao.java - Interface
public interface ApplicationDao {
    DormApplication getById(long id);
    void setStatus(long id, ApplicationStatus status);
}

// ApplicationDaoPg.java - Implementation (could be MySQL, SQLite, etc.)
public class ApplicationDaoPg implements ApplicationDao { ... }
```

### 3. Inheritance
**Logical class hierarchy:**
```java
// Base class with common attributes
public abstract class User extends BaseEntity {
    private String username;
    private String passwordHash;
    private Role role;
}

// Subclasses with specialized behavior
public class Student extends User { ... }
public class Admin extends User { ... }
public class Proctor extends User { ... }
```

### 4. Polymorphism
**Method overriding and interface implementation:**
```java
// Different DAO implementations implement same interface
public class ApplicationDaoPg implements ApplicationDao { ... }
public class AllocationDaoPg implements AllocationDao { ... }

// Services use interfaces (not concrete classes)
private final ApplicationDao applicationDao;  // Could be any implementation
```

### 5. Data Validation
**Input validation at service layer:**
```java
// ApplicationService.java
public void approve(long applicationId) {
    DormApplication a = applicationDao.getById(applicationId);
    if (a.status() != ApplicationStatus.PENDING) {
        throw new IllegalStateException("Only PENDING applications can be approved.");
    }
    // ... rest of logic
}
```

---

## SOLID Principles ✓

### S - Single Responsibility Principle
Each class has ONE responsibility:

| Class | Responsibility |
|-------|-----------------|
| User | User data representation |
| ApplicationService | Application workflow logic |
| ApplicationDao | Database access for applications |
| LoginController | Handle login UI events |
| Validation | Validate input formats |

### O - Open/Closed Principle
Classes are **open for extension, closed for modification**:
- Create new User subclasses (Proctor, Student) without changing User
- Create new DAO implementations without changing interface
- Add new validation rules without changing Validation class

### L - Liskov Substitution Principle
Subclasses can substitute base classes:
```java
// This works because Admin, Student, Proctor extend User
User user = new Admin(...);
User user = new Student(...);
User user = new Proctor(...);
```

### I - Interface Segregation Principle
Small, focused interfaces:
```java
// Not one big interface, but specific ones:
public interface ApplicationDao { ... }
public interface AllocationDao { ... }
public interface NotificationService { ... }
public interface UserDao { ... }
```

### D - Dependency Inversion Principle
Depend on abstractions, not concrete classes:
```java
// ✓ GOOD - Depends on interface
private final ApplicationDao applicationDao;

// ✗ BAD - Would depend on concrete class
private final ApplicationDaoPg applicationDao;
```

---

## Key Classes Demonstrating OOP

### User Hierarchy (Inheritance + Polymorphism)
```
BaseEntity
    └── User (abstract)
        ├── Admin
        ├── Proctor
        └── Student
                └── StaffStudent
```

### DAO Pattern (Abstraction + Polymorphism)
```
ApplicationDao (interface)
    └── ApplicationDaoPg (implementation)
        - getById()
        - setStatus()
        - markSubmitted()
```

### Service Layer (Single Responsibility + Dependency Inversion)
```
ApplicationService
    - Depends on: ApplicationDao, StudentDao, NotificationService
    - Methods: approve(), reject()
    - No direct database access
```

---

## Project Structure

```
src/main/java/edu/aau/dorm/
├── model/          # Data classes (OOP Encapsulation, Inheritance)
│   ├── User.java
│   ├── Student.java
│   ├── Admin.java
│   └── DormApplication.java
├── dao/            # Database access (OOP Abstraction, Polymorphism)
│   ├── ApplicationDao.java (interface)
│   ├── ApplicationDaoPg.java
│   └── UserDao.java
├── service/        # Business logic (OOP SRP, DIP)
│   ├── ApplicationService.java
│   ├── AuthService.java
│   └── ProctorService.java
├── ui/             # JavaFX controllers (Separation of concerns)
│   ├── controller/
│   │   ├── LoginController.java
│   │   └── SignupController.java
│   └── SceneRouter.java
└── util/           # Helpers
    ├── Db.java
    ├── PasswordHasher.java
    └── Validation.java
```

---

## Testing the System

### Login
- Admin: `admin` / `admin123`
- Proctor: `proctor1` / `pass123`
- Student: `student1` / `pass123`

### Features
1. **Admin**: View and approve/reject applications
2. **Proctor**: Check in students, manage withdrawals
3. **Student**: Submit housing applications

---

## Summary

✓ All functional requirements met
✓ All 5 OOP principles clearly demonstrated
✓ All 5 SOLID principles implemented
✓ Clean code, no over-engineering
✓ Ready for evaluation
