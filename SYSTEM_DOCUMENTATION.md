# Dormitory Management System - Technical Documentation

## Overview

A comprehensive JavaFX-based dormitory management system for universities built with strict adherence to Object-Oriented Programming (OOP) principles and SOLID design patterns. The system manages three primary user roles: Administrators, Proctors, and Students across distributed campuses with multiple dormitory blocks.

---

## System Architecture

### Technology Stack

- **UI Framework**: JavaFX 25.0.2 with FXML layouts
- **Database**: MySQL 8.0+ (switched from PostgreSQL for requirements)
- **Connection Pooling**: HikariCP 6.3.3
- **Build Tool**: Maven 4.0.0
- **Java Version**: JDK 21

### Key Components

```
src/main/java/edu/aau/dorm/
├── model/           # Domain entities (User, Student, Admin, Proctor, etc.)
├── dao/             # Data Access Objects (MySQL implementation)
├── service/         # Business logic layer
├── ui/
│   ├── controller/  # UI Controllers (Admin, Proctor, Student dashboards)
│   └── SceneRouter  # Navigation and scene management
└── util/            # Utility classes (validation, hashing, DB configuration)
```

---

## OOP Principles Implementation

### 1. Encapsulation

**Definition**: Bundling data (state) and methods (behavior) into a single unit with restricted access.

**Implementation**:
- All fields in domain models are `private` with controlled access via getters
- Example: `User` class
  ```java
  public abstract class User extends BaseEntity {
      private final String username;
      private final String passwordHash;
      private final Role role;
      
      // Only getters exposed, no setters
      public String username() { return username; }
      public String passwordHash() { return passwordHash; }
  }
  ```

**Benefits**: 
- Prevents unauthorized modification of critical data
- Hides internal implementation details
- Enables validation in setters if needed

---

### 2. Abstraction

**Definition**: Hiding complex implementation details and showing only necessary features.

**Implementation**:
- Abstract base classes: `User` class provides abstract behavior for all user types
- Interfaces for DAO layer: `ApplicationDao`, `AllocationDao` define contracts
- Service layer abstracts business logic from controllers
  ```java
  public interface ApplicationDao {
      DormApplication getById(long id);
      void setStatus(long id, ApplicationStatus status);
      List<DormApplication> findByStatus(ApplicationStatus status);
  }
  ```

**Benefits**:
- Reduces system complexity
- Allows multiple implementations (MySQL, PostgreSQL, etc.)
- Makes testing easier with mock implementations

---

### 3. Inheritance

**Definition**: Classes inherit properties and methods from parent classes for code reuse.

**Implementation**:
- **User Hierarchy**: Base `User` class extended by `Admin`, `Proctor`, `Student`
  ```java
  public class Admin extends User { ... }
  public class Proctor extends User { ... }
  public class Student extends User {
      private final String fullName;
      private final String aauId;
      private final Category category;
  }
  ```
- **Entity Hierarchy**: `BaseEntity` provides common id field
- Each subclass adds specific attributes relevant to their role

**Benefits**:
- Avoids code duplication
- Establishes clear "is-a" relationships
- Facilitates polymorphism

---

### 4. Polymorphism

**Definition**: Objects of different types can be treated through the same interface.

**Implementation**:
- **Compile-time (Method Overriding)**: Service methods override behavior
  ```java
  // Base User class
  public boolean canManageUsers() { return false; }
  
  // Admin overrides
  public class Admin extends User {
      @Override
      public boolean canManageUsers() { return role == Role.ADMIN; }
  }
  ```

- **Runtime (Interface Implementation)**: DAOs implement common interface
  ```java
  ApplicationDao dao = new ApplicationDaoPg(); // Can swap implementations
  DormApplication app = dao.getById(1);
  ```

**Benefits**:
- Flexibility to change implementations without affecting clients
- Enables dependency injection and testing
- Reduces coupling between components

---

## SOLID Principles Implementation

### 1. Single Responsibility Principle (SRP)

**Definition**: A class should have only one reason to change.

**Implementation**:

| Class | Responsibility |
|-------|-----------------|
| `AdminService` | Admin application review & dormitory assignment |
| `ProctorManagementService` | Student check-in/checkout & capacity management |
| `StudentHousingService` | Housing availability & application management |
| `InputValidator` | All input validation (centralized) |
| `Db` | Database connection management only |

**Example - AdminService**:
```java
public class AdminService {
    // Responsibility: Manage admin-specific operations
    public void reviewApplication(...) { }
    public void assignDormitory(...) { }
    public void publishNews(...) { }
}
```

**Benefits**:
- Easier to understand and maintain
- Changes to one responsibility don't affect others
- Classes are more reusable and testable

---

### 2. Open/Closed Principle (OCP)

**Definition**: Classes should be open for extension, closed for modification.

**Implementation**:
- Abstract `User` class allows extension for new user roles without modification
- `ScoringPolicy` interface allows different scoring algorithms
  ```java
  public interface ScoringPolicy {
      int score(Student student, DormApplication application);
  }
  
  public class DefaultScoringPolicy implements ScoringPolicy {
      @Override
      public int score(Student student, DormApplication application) {
          // Implementation
      }
  }
  ```

**Benefits**:
- New functionality added by extending, not modifying
- Reduces risk of breaking existing code
- Encourages better design

---

### 3. Liskov Substitution Principle (LSP)

**Definition**: Subclasses must be substitutable for their parent classes without breaking the contract.

**Implementation**:
- All `User` subclasses (`Admin`, `Proctor`, `Student`) maintain contract:
  ```java
  User user = getUser(id); // Could be any subclass
  String username = user.username(); // Works for all
  ```

- Service methods validate state consistently:
  ```java
  public void checkInStudent(...) {
      DormApplication app = applicationDao.getById(applicationId);
      if (app.status() != ApplicationStatus.ACCEPTED) {
          throw new IllegalStateException(...);
      }
      // Consistent validation for all callers
  }
  ```

**Benefits**:
- Predictable behavior when using inheritance
- Prevents surprises from overridden methods
- Enables safe polymorphic usage

---

### 4. Interface Segregation Principle (ISP)

**Definition**: Clients should not depend on interfaces they don't use.

**Implementation**:
- Focused DAO interfaces instead of one monolithic interface
  ```java
  // Good: Segregated interfaces
  public interface ApplicationDao { /* 10 methods */ }
  public interface AllocationDao { /* 8 methods */ }
  public interface BlockDao { /* 6 methods */ }
  
  // Bad: Single fat interface (not done here)
  // public interface UniversalDao { /* 50 methods */ }
  ```

- Service receives only needed dependencies:
  ```java
  public AdminService(
      ApplicationDao applicationDao,        // Only needs this
      NotificationService notificationService // Only needs this
  ) { ... }
  ```

**Benefits**:
- Clients depend only on methods they use
- Easier to mock in tests
- Better cohesion and separation of concerns

---

### 5. Dependency Inversion Principle (DIP)

**Definition**: Depend on abstractions, not concrete implementations.

**Implementation**:
- All services depend on interfaces/abstractions:
  ```java
  public class AdminService {
      private final ApplicationDao applicationDao;      // Interface
      private final NotificationService notificationService; // Interface
      
      // Constructor: Dependencies injected
      public AdminService(
          ApplicationDao applicationDao,
          NotificationService notificationService
      ) {
          this.applicationDao = applicationDao;
          this.notificationService = notificationService;
      }
  }
  ```

- Controllers use constructor injection:
  ```java
  public class AdminDashboardController {
      private final AdminService adminService;
      
      public AdminDashboardController(AdminService adminService) {
          this.adminService = adminService;
      }
  }
  ```

**Benefits**:
- Easy to swap implementations (MySQL ↔ PostgreSQL)
- Simplified testing with mock objects
- Reduces coupling between classes
- Enables factory patterns and dependency injection containers

---

## Database Schema (MySQL)

### Core Tables

#### 1. `app_user` - Authentication & Authorization
- Stores all system users with role-based access
- Password stored as bcrypt hash
- Active flag for account management

#### 2. `student_profile` - Student Information
- Extended profile for students
- Gender tracking for dormitory assignment
- Year of study and category (normal vs. staff-privileged)

#### 3. `campus` - University Locations
- Multiple campuses supported
- Blocks organized by campus

#### 4. `block` - Dormitory Buildings
- Blocks within campuses
- Gender-designated (MALE/FEMALE)
- Tracks capacity and available beds

#### 5. `bed` - Individual Bed Units
- Each bed in a block
- Occupancy status tracking
- Active/inactive flag for maintenance

#### 6. `dorm_application` - Student Applications
- Application status tracking (DRAFT → SUBMITTED → ACCEPTED/REJECTED → CHECKED_IN)
- Priority scoring based on distance, disability, sponsorship
- Tracks submission history

#### 7. `allocation` - Bed Allocations
- Links students to specific beds
- Tracks check-in/check-out timestamps
- Records assigning admin

#### 8. `notification` - System Messages
- Notifications sent to users
- Read/unread tracking
- Audit trail of communications

#### 9. `audit_log` - System Actions
- Records all significant system actions
- Who did what and when
- Supports compliance and debugging

---

## Service Layer Architecture

### AdminService
**Responsibilities**:
- Review student applications
- Update application status (ACCEPTED/REJECTED/NEEDS_EDIT)
- Assign dormitories to approved students
- Send publications/news to all students

**Key Methods**:
```java
public void reviewApplication(long applicationId, long adminUserId, ApplicationStatus newStatus, String comment)
public void assignDormitory(long applicationId, long bedId, long adminUserId, String roomNumber)
public void publishNews(long adminUserId, String title, String message)
public List<DormApplication> getPendingApplications()
```

### ProctorManagementService
**Responsibilities**:
- Check in students (update status, mark bed occupied)
- Withdraw students (free beds, update status)
- Verify documents
- Update dormitory capacity
- Monitor occupancy

**Key Methods**:
```java
public void checkInStudent(long applicationId, long bedId, long proctorUserId, String roomNumber, Instant checkInTime)
public void withdrawStudent(long applicationId, long proctorUserId, String withdrawalReason, Instant withdrawalTime)
public void verifyDocuments(long applicationId, long proctorUserId, String verificationNotes)
public void updateBlockCapacity(long blockId, long proctorUserId, int totalBeds, int maleBeds, int femaleBeds)
```

### StudentHousingService
**Responsibilities**:
- Display available housing with occupancy
- Create/update housing applications
- Submit applications for review
- Track application status

**Key Methods**:
```java
public List<?> getAvailableHousing()
public int getAvailableBedsByGender(long blockId, String gender)
public long createApplication(...)
public void updateApplication(long applicationId, String campusPreference, double distance, String notes)
public void submitApplication(long applicationId, long studentUserId, Instant submissionTime)
```

---

## UI Layer Architecture

### Controller Pattern (MVVM)

**AdminDashboardController**:
- Delegates business logic to `AdminService`
- Manages table data with `ObservableList`
- Handles user interactions (button clicks, selections)
- Updates UI based on service responses

**Design Pattern**:
```
User Input → Controller → Service → DAO → Database
   ↓                ↓          ↓
  UI Update ← Data Display ← Business Logic
```

### Input Validation Strategy

**Layered Validation**:
1. **UI Layer**: Real-time validation with feedback
2. **Service Layer**: Business rule validation
3. **DAO Layer**: Database constraint validation

**InputValidator Utility**:
```java
// Centralized validation
InputValidator.validateUsername(username);
InputValidator.validatePassword(password);
InputValidator.validateBedCapacity(total, male, female);
InputValidator.validateBatch(
    () -> InputValidator.validateNonEmpty(name, "Name"),
    () -> InputValidator.validatePhoneNumber(phone),
    () -> InputValidator.validateAauId(aauId)
);
```

---

## Data Flow Example: Application Submission

```
Student fills form
    ↓
StudentDashboardController.onSubmitApplication()
    ↓
StudentHousingService.submitApplication(applicationId, studentUserId, timestamp)
    ├─ Validate application state (DRAFT/NEEDS_EDIT)
    ├─ ApplicationDao.markSubmitted(applicationId, score, timestamp)
    └─ NotificationService.notifyUser(..., "Application submitted")
    ↓
Database updated
    ↓
Student notified
```

---

## Test Data

Test data includes:
- **3 Campuses**: Main, North, South
- **8 Dormitory Blocks**: Mix of male/female
- **60+ Beds**: With occupancy status
- **6 Test Students**: Various statuses (DRAFT, SUBMITTED, ACCEPTED, CHECKED_IN, REJECTED, UNDER_REVIEW)
- **Application History**: Reviews, allocations, notifications
- **Audit Trail**: System actions logged

**Load test data**:
```sql
mysql -u root -p dormdb < src/main/resources/sql/test_data.sql
```

---

## Environment Setup

### MySQL Setup

```bash
# Create database
mysql -u root -p < src/main/resources/sql/schema_mysql.sql

# Load test data
mysql -u root -p dormdb < src/main/resources/sql/test_data.sql
```

### Configuration

Set environment variables:
```bash
export DB_URL="jdbc:mysql://localhost:3306/dormdb?serverTimezone=UTC&useSSL=false"
export DB_USER="root"
export DB_PASS="yourpassword"
```

### Running the Application

```bash
# Build with Maven
mvn clean javafx:run

# Or compile and run
mvn clean package
java -Djavax.net.debug=ssl --module-path /path/to/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml -jar target/dorm-management-1.0.0.jar
```

---

## Project Structure

```
dormitory-management/
├── src/main/
│   ├── java/edu/aau/dorm/
│   │   ├── App.java                          # Entry point
│   │   ├── model/                            # Domain entities
│   │   │   ├── User.java                     # Abstract base user
│   │   │   ├── Admin.java
│   │   │   ├── Proctor.java
│   │   │   ├── Student.java
│   │   │   ├── DormApplication.java
│   │   │   ├── Allocation.java
│   │   │   └── ... other entities
│   │   ├── dao/                              # Data access layer
│   │   │   ├── ApplicationDao.java           # Interface
│   │   │   ├── ApplicationDaoPg.java         # MySQL implementation
│   │   │   ├── AllocationDao.java
│   │   │   ├── BlockDao.java
│   │   │   └── ... other DAOs
│   │   ├── service/                          # Business logic
│   │   │   ├── AdminService.java
│   │   │   ├── ProctorManagementService.java
│   │   │   ├── StudentHousingService.java
│   │   │   └── ... other services
│   │   ├── ui/
│   │   │   ├── SceneRouter.java              # Navigation
│   │   │   └── controller/
│   │   │       ├── AdminDashboardController.java
│   │   │       ├── ProctorDashboardController.java
│   │   │       ├── StudentDashboardController.java
│   │   │       └── ... other controllers
│   │   └── util/
│   │       ├── Db.java                       # Connection pool
│   │       ├── InputValidator.java           # Validation utility
│   │       └── PasswordHasher.java           # Security
│   └── resources/
│       ├── sql/
│       │   ├── schema_mysql.sql              # Database schema
│       │   └── test_data.sql                 # Sample data
│       └── edu/aau/dorm/ui/
│           ├── admin_dashboard.fxml
│           ├── proctor_dashboard.fxml
│           ├── student_dashboard.fxml
│           └── ... other FXML files
├── pom.xml                                   # Maven configuration
└── SYSTEM_DOCUMENTATION.md                   # This file
```

---

## Future Enhancements

1. **Authentication & Authorization**
   - Implement JWT token-based authentication
   - Role-based access control (RBAC)
   - Multi-factor authentication

2. **Advanced Features**
   - Room preferences and matching algorithm
   - Automated occupancy reports
   - Email notifications integration
   - Mobile app (React Native/Flutter)

3. **Performance**
   - Query optimization and indexing
   - Caching layer (Redis)
   - Pagination for large result sets

4. **Security**
   - SQL injection prevention (parameterized queries)
   - Input sanitization
   - Audit logging enhancements

5. **Testing**
   - Unit tests (JUnit 5)
   - Integration tests
   - UI tests (TestFX)

---

## Conclusion

This Dormitory Management System demonstrates a well-architected JavaFX application with strict adherence to OOP and SOLID principles. The design is scalable, maintainable, and testable, providing a solid foundation for a production university dormitory management solution.

**Key Achievements**:
- ✅ Three distinct user roles (Admin, Proctor, Student)
- ✅ Complete CRUD operations
- ✅ Comprehensive input validation
- ✅ OOP principles (Encapsulation, Abstraction, Inheritance, Polymorphism)
- ✅ SOLID design patterns (SRP, OCP, LSP, ISP, DIP)
- ✅ MySQL database with proper schema
- ✅ Real database I/O with connection pooling
- ✅ Exception handling and error management
- ✅ Audit trail and logging
- ✅ Separation of concerns (UI ↔ Business ↔ Data)
