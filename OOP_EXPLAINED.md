# OOP Principles - Simple Explanations with Examples

## 1️⃣ ENCAPSULATION (Data Hiding)

**What?** Keep your data private and only allow changes through specific methods.

**Real-world analogy**: Your house has a front door (public) but your bedroom is private. People don't just walk into your bedroom - they knock, you control access.

### ❌ BAD Example:
```java
public class Bed {
    public String roomNumber;      // ❌ Anyone can change this!
    public boolean occupied;        // ❌ Anyone can change this!
    
    public void doSomething() {
        // Someone changed roomNumber outside! It's now invalid!
    }
}

// Usage:
Bed bed = new Bed();
bed.roomNumber = null;              // ❌ DANGER! Broke the object
bed.occupied = "maybe";             // ❌ DANGER! Wrong type
```

### ✅ GOOD Example (Our Code):
```java
public class Bed extends BaseEntity {
    private final String roomNumber;    // ✅ PRIVATE - can't change
    private final boolean occupied;     // ✅ PRIVATE - can't change
    
    public Bed(long id, String roomNumber, boolean occupied) {
        // Data is set once, safely
        this.roomNumber = roomNumber;
        this.occupied = occupied;
    }
    
    public String getRoomNumber() {     // ✅ Only getter - read-only
        return roomNumber;
    }
    
    public boolean isOccupied() {       // ✅ Only getter - read-only
        return occupied;
    }
    // ✅ NO SETTER - can't be changed after creation!
}

// Usage:
Bed bed = new Bed(1, "A-101", false);
System.out.println(bed.getRoomNumber());  // ✅ OK - read
bed.roomNumber = null;                     // ❌ COMPILE ERROR!
bed.occupied = "maybe";                    // ❌ COMPILE ERROR!
```

**Benefits**:
- Prevents invalid states
- Easier to maintain code
- Can change internal implementation without breaking other code

---

## 2️⃣ ABSTRACTION (Hiding Complexity)

**What?** Show only what's needed, hide the how. You don't need to know how a car engine works to drive it.

### ❌ BAD Example:
```java
// Everything mixed together - complex!
public class UserAuthentication {
    public void loginUser(String username, String password) {
        // Complex database code
        Connection conn = DriverManager.getConnection("jdbc:mysql://...");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username='" + username + "'");
        
        // Complex password checking
        String storedHash = rs.getString("password_hash");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageDigest = md.digest(password.getBytes());
        StringBuffer sb = new StringBuffer();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        String passwordHash = sb.toString();
        if (!passwordHash.equals(storedHash)) {
            throw new Exception("Wrong password");
        }
        
        // Complex role checking
        if (rs.getInt("role_id") == 1) {
            System.out.println("Admin");
        } else if (rs.getInt("role_id") == 2) {
            System.out.println("Proctor");
        } else if (rs.getInt("role_id") == 3) {
            System.out.println("Student");
        }
    }
}
// ❌ Hard to understand, hard to modify
```

### ✅ GOOD Example (Our Code):
```java
public class AuthService {
    private final UserDao userDao;              // Hides database details
    private final PasswordHasher passwordHasher; // Hides hashing details
    
    public User login(String username, String password) {
        // Simple, clear code - complex details are hidden!
        User user = userDao.getByUsername(username);
        
        if (!passwordHasher.matches(password, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        
        return user;  // Return the user - that's all the caller needs!
    }
}

// Usage - very simple!
AuthService auth = new AuthService(...);
User user = auth.login("alice", "password123");
System.out.println(user.role());  // "ADMIN" - simple!
```

**Benefits**:
- Easier to use (simple interface)
- Can change implementation without changing the interface
- Code is more readable

---

## 3️⃣ INHERITANCE (Hierarchy / Is-a Relationship)

**What?** Child classes inherit behavior from parent classes. Avoid repeating code.

**Real-world analogy**: 
- All cars have wheels, engine, steering wheel
- Truck, Car, Motorcycle all inherit from Vehicle
- But each is specialized

### ❌ BAD Example (Code Duplication):
```java
public class Student {
    private String username;           // ❌ Duplicated
    private String passwordHash;       // ❌ Duplicated
    private User.Role role;            // ❌ Duplicated
    private boolean active;            // ❌ Duplicated
    private Instant createdAt;         // ❌ Duplicated
    
    public String username() { return username; }  // ❌ Duplicated
    public String passwordHash() { return passwordHash; }  // ❌ Duplicated
    public boolean active() { return active; }  // ❌ Duplicated
    
    // Student-specific fields
    private String fullName;
    private String aauId;
    private String department;
}

public class Admin {
    private String username;           // ❌ Duplicated
    private String passwordHash;       // ❌ Duplicated
    private User.Role role;            // ❌ Duplicated
    private boolean active;            // ❌ Duplicated
    private Instant createdAt;         // ❌ Duplicated
    
    public String username() { return username; }  // ❌ Duplicated
    public String passwordHash() { return passwordHash; }  // ❌ Duplicated
    public boolean active() { return active; }  // ❌ Duplicated
}
// ❌ Same code in multiple places - BAD!
```

### ✅ GOOD Example (Our Code - Inheritance):
```java
// Parent class - common behavior
public abstract class User extends BaseEntity {
    private final String username;              // ✅ Defined once
    private final String passwordHash;          // ✅ Defined once
    private final Role role;                    // ✅ Defined once
    private final boolean active;               // ✅ Defined once
    private final Instant createdAt;            // ✅ Defined once
    
    // Common methods
    public String username() { return username; }         // ✅ Defined once
    public String passwordHash() { return passwordHash; } // ✅ Defined once
    public boolean active() { return active; }            // ✅ Defined once
}

// Child class - inherits common behavior
public class Student extends User {
    // Inherits: username, passwordHash, role, active, createdAt, methods
    
    // Student-specific fields
    private final String fullName;
    private final String aauId;
    private final String department;
    private final int yearOfStudy;
}

// Another child class
public class Admin extends User {
    // Inherits: username, passwordHash, role, active, createdAt, methods
    // No duplicate code!
}

// Another child class
public class Proctor extends User {
    // Inherits: username, passwordHash, role, active, createdAt, methods
    // No duplicate code!
}
```

**Benefits**:
- No code duplication
- Common behavior in one place
- Easy to add new types of users
- Easier to maintain

**Hierarchy in our code**:
```
BaseEntity
    ↓
    User (abstract)
    ├── Student
    ├── Admin
    └── Proctor
    
    Bed
    
    DormApplication
```

---

## 4️⃣ POLYMORPHISM (Many Forms)

**What?** Same method name, different behavior. One variable can hold different types.

**Real-world analogy**: The action "start()" works on both a car and a motorcycle, but does different things.

### ❌ BAD Example (No Polymorphism):
```java
public void printUserInfo(User user) {
    if (user instanceof Student) {
        Student s = (Student) user;
        System.out.println("Student: " + s.fullName() + " (" + s.aauId() + ")");
    } else if (user instanceof Admin) {
        System.out.println("Admin: " + user.username());
    } else if (user instanceof Proctor) {
        System.out.println("Proctor: " + user.username());
    }
}
// ❌ Lots of instanceof checks - bad design!
```

### ✅ GOOD Example (Polymorphism):
```java
// We can define a method in User class that child classes override
public abstract class User extends BaseEntity {
    public abstract String getDisplayName();  // Different for each type
}

public class Student extends User {
    @Override
    public String getDisplayName() {
        return fullName + " (" + aauId + ")";
    }
}

public class Admin extends User {
    @Override
    public String getDisplayName() {
        return "Admin: " + username();
    }
}

public class Proctor extends User {
    @Override
    public String getDisplayName() {
        return "Proctor: " + username();
    }
}

// Usage - POLYMORPHISM!
public void printUserInfo(User user) {
    // Same code works for Student, Admin, Proctor!
    // Each type calls its own version of getDisplayName()
    System.out.println(user.getDisplayName());
}

User student = new Student(...);
User admin = new Admin(...);
User proctor = new Proctor(...);

printUserInfo(student);  // Calls Student.getDisplayName()
printUserInfo(admin);    // Calls Admin.getDisplayName()
printUserInfo(proctor);  // Calls Proctor.getDisplayName()
// ✅ Same method, different behavior!
```

**Benefits**:
- Less code duplication
- Easier to extend with new types
- More flexible and maintainable

---

## 5️⃣ SOLID PRINCIPLES

### **S - Single Responsibility Principle**

**What?** Each class should have only ONE reason to change.

✅ GOOD:
```java
public class AuthService {
    // Only handles authentication
    public User login(String username, String password) { ... }
    public void logout(User user) { ... }
}

public class NotificationService {
    // Only handles notifications
    public void notifyUser(long userId, String message) { ... }
}

public class ApplicationService {
    // Only handles application workflow
    public void submit(long applicationId) { ... }
    public void approve(long applicationId) { ... }
}
```

❌ BAD:
```java
public class UserManager {
    // Too many responsibilities!
    public User login(String username, String password) { ... }  // Auth
    public void sendEmail(String email, String message) { ... }  // Email
    public void storeInDatabase(User user) { ... }               // Database
    public void generateReport() { ... }                         // Reporting
    public void calculateScore(User user) { ... }                // Scoring
}
// ❌ One class doing everything - hard to modify!
```

---

### **O - Open/Closed Principle**

**What?** Open for extension, closed for modification.

✅ GOOD (Our Code):
```java
public interface ScoringPolicy {
    int score(Student student, DormApplication app);
}

public class DefaultScoringPolicy implements ScoringPolicy {
    @Override
    public int score(Student student, DormApplication app) {
        // Original scoring logic
        return baseScore + bonusForDisability(student) + bonusForDistance(app);
    }
}

// Want to add new scoring logic? Create new class, don't modify existing!
public class NewScoringPolicy implements ScoringPolicy {
    @Override
    public int score(Student student, DormApplication app) {
        // Different logic - existing code unchanged!
        return newScore;
    }
}
```

---

### **L - Liskov Substitution Principle**

**What?** Child classes must be usable wherever parent classes are used.

✅ GOOD:
```java
public class LoginController {
    private AuthService authService;
    
    public void handleLogin(String username, String password) {
        User user = authService.login(username, password);
        
        // This works with Student, Admin, or Proctor!
        // They're all Users, so they're substitutable
        if (user instanceof Student) {
            showStudentDashboard((Student) user);
        } else if (user instanceof Admin) {
            showAdminDashboard((Admin) user);
        }
    }
}
```

---

### **I - Interface Segregation Principle**

**What?** Don't create huge interfaces. Clients shouldn't depend on methods they don't use.

✅ GOOD (Focused interfaces):
```java
public interface ApplicationDao {
    DormApplication getById(long id);
    void setStatus(long applicationId, ApplicationStatus status);
    void markSubmitted(long applicationId, int score, Instant now);
}

public interface AllocationDao {
    void allocate(long applicationId, long bedId, long proctorId, String roomNumber);
    void freeBedIfAllocated(long applicationId);
}
```

❌ BAD (Monster interface):
```java
public interface MegaDao {
    // Too many methods, clients only use what they need
    DormApplication getApplicationById(long id);
    void setApplicationStatus(long applicationId, ApplicationStatus status);
    void allocateBed(long applicationId, long bedId, long proctorId, String roomNumber);
    void freeBed(long applicationId);
    Bed getBedById(long bedId);
    void saveBed(Bed bed);
    Block getBlockById(long blockId);
    void saveBlock(Block block);
    // ... 20 more methods
}
```

---

### **D - Dependency Inversion Principle**

**What?** Depend on abstractions (interfaces), not concrete implementations.

✅ GOOD (Our Code):
```java
public class ProctorService {
    // Depends on INTERFACES, not implementations
    private final ApplicationDao applicationDao;     // Interface!
    private final AllocationDao allocationDao;       // Interface!
    private final NotificationService notificationService;  // Interface!
    
    public ProctorService(
            ApplicationDao applicationDao,           // Inject interface
            AllocationDao allocationDao,             // Inject interface
            NotificationService notificationService  // Inject interface
    ) {
        this.applicationDao = applicationDao;
        this.allocationDao = allocationDao;
        this.notificationService = notificationService;
    }
}

// Can use with MySQL implementation
ProctorService service = new ProctorService(
    new ApplicationDaoPg(),
    new AllocationDaoPg(),
    new NotificationServiceDb()
);

// Or test with mock implementations - same code!
ProctorService testService = new ProctorService(
    new MockApplicationDao(),
    new MockAllocationDao(),
    new MockNotificationService()
);
```

❌ BAD (Depends on concrete classes):
```java
public class ProctorService {
    // ❌ Depends on CONCRETE implementations
    private final ApplicationDaoPg applicationDao;      // Concrete class!
    private final AllocationDaoPg allocationDao;        // Concrete class!
    private final NotificationServiceDb notificationService;  // Concrete class!
    
    // ❌ Hard to change database from MySQL to PostgreSQL
    // ❌ Hard to test with mock objects
}
```

---

## Summary Table

| Principle | What | Why | Example |
|-----------|------|-----|---------|
| **Encapsulation** | Private fields, public methods | Data safety, prevent invalid states | Bed has private fields, only getters |
| **Abstraction** | Hide complexity | Easier to use, easier to modify | Don't know how notification works |
| **Inheritance** | Parent-child hierarchy | Avoid code duplication | User → Student, Admin, Proctor |
| **Polymorphism** | Same method, different behavior | Flexible code, less duplication | Override getDisplayName() in each class |
| **Single Resp.** | One class, one job | Easy to modify, maintain | AuthService only does auth |
| **Open/Closed** | Extend without modifying | Add new features without breaking old code | Create new ScoringPolicy implementations |
| **Liskov Sub.** | Child ≈ Parent | Code flexibility | Student, Admin, Proctor are all Users |
| **Interface Seg.** | Focused interfaces | Don't force unused dependencies | Separate ApplicationDao from AllocationDao |
| **Dep. Inversion** | Depend on interfaces | Loosely coupled, testable | Service uses interfaces, not implementations |

