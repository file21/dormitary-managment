# Dormitory Management System - Presentation Guide

## Project Overview
A JavaFX-based dormitory management system for Addis Ababa University demonstrating OOP and SOLID principles.

---

## 🎯 Key OOP Principles Demonstrated

### 1. **ENCAPSULATION** ✅
**Definition**: Data hiding - keeping fields private and providing controlled access through methods.

**Show in Code**:
- `User.java` - All fields are `private` with getters only
- `Bed.java` - Private fields, no setters (immutable)
- `DormApplication.java` - All fields are `private final` with getter methods

**What to Say**:
> "See how all fields are private? The outside world can't directly access or modify them. This prevents invalid states. For example, you can't accidentally set a bed's ID to an invalid value."

---

### 2. **ABSTRACTION** ✅
**Definition**: Hiding complex implementation details behind simple interfaces.

**Show in Code**:
- `User.java` - Abstract base class with `abstract` keyword
- `ScoringPolicy.java` - Interface defining contract for different scoring strategies
- `ApplicationService.java` - Uses `ApplicationDao`, `StudentDao`, `NotificationService` as abstractions

**What to Say**:
> "Look at the abstract User class. It defines common attributes for all users (username, role, etc) but doesn't care about specific implementations. Students, Admins, and Proctors extend this - they inherit shared behavior but can be unique."

---

### 3. **INHERITANCE** ✅
**Definition**: Creating a hierarchy where child classes inherit from parent classes.

**Show in Code**:
- `User` (abstract parent)
  - `→ Student` (extends User)
  - `→ Admin` (extends User)
  - `→ Proctor` (extends User)

- `BaseEntity` (parent)
  - `→ User` (extends BaseEntity)
  - `→ Bed` (extends BaseEntity)
  - `→ DormApplication` (extends BaseEntity)

**What to Say**:
> "Student, Admin, and Proctor all extend User. They inherit things like username, passwordHash, role, and the isActive() method. But each one is still unique - Student has department and yearOfStudy."

---

### 4. **POLYMORPHISM** ✅
**Definition**: Objects that can take many forms. The same method behaves differently in different classes.

**Show in Code**:
- `ScoringPolicy` interface with multiple implementations:
  - `DefaultScoringPolicy.score()` - Different behavior than custom policies
  
- `User user = new Student(...)` - User reference pointing to Student object
- `User user = new Admin(...)` - Same reference type, different object

**What to Say**:
> "Polymorphism means 'many forms'. We can have a User variable that points to a Student, Admin, or Proctor at runtime. Same variable, different behaviors!"

---

### 5. **SOLID PRINCIPLES** ✅

#### **S - Single Responsibility Principle**
- `ApplicationService` - Only handles application workflows
- `AuthService` - Only handles authentication
- `ScoringPolicy` - Only handles scoring logic

#### **O - Open/Closed Principle**
- Open for extension: Create new `ScoringPolicy` implementations without changing existing code
- Closed for modification: Don't need to edit `ApplicationService` to add new policies

#### **L - Liskov Substitution Principle**
- `ScoringPolicy` implementations can be swapped - all have `score()` method
- `Student`, `Admin`, `Proctor` can be used wherever `User` is expected

#### **I - Interface Segregation Principle**
- `ScoringPolicy` - Focused interface with one job
- `ApplicationDao` - Focused interface for application data access
- Don't create huge "monster" interfaces

#### **D - Dependency Inversion Principle**
- `ApplicationService` depends on `ApplicationDao` (interface), not `ApplicationDaoPg` (implementation)
- Can swap implementations without changing `ApplicationService`

---

## 📱 GUI Features to Demo

### **1. Login Screen** (LoginController)
```
✓ Username/Password validation
✓ Role-based authentication (ADMIN/STUDENT/PROCTOR)
✓ Error messages for invalid credentials
```

### **2. Admin Dashboard** (Future - AdminDashboardController)
```
✓ View pending applications
✓ Approve/Reject applications
✓ Assign dormitories to students
✓ Send notifications
```

### **3. Proctor Dashboard** (Future - ProctorDashboardController)
```
✓ Manage bed occupancy
✓ Check in/out students
✓ View campus statistics
✓ Verify student documents
```

### **4. Student Dashboard** (Future - StudentDashboardController)
```
✓ View available dormitories
✓ Apply for housing
✓ Track application status
✓ View assigned room details
```

---

## 🔄 Data Flow Example

```
LoginController (UI)
    ↓
AuthService (Business Logic)
    ↓
UserDao (Data Access)
    ↓
Database
```

**Explanation**: UI doesn't talk directly to Database. There's a clean separation:
- **Controller**: Handles user interaction
- **Service**: Contains business rules
- **DAO**: Handles database operations

---

## 💾 Database Schema (Simplified)

```sql
-- Users table
users (id, username, password_hash, role, active)

-- Student extends User
students (user_id, full_name, aau_id, department, year_of_study)

-- Dormitory structure
blocks (id, campus, block_name, capacity)
beds (id, block_id, bed_label, active)

-- Applications
dorm_applications (id, student_id, block_id, status, submitted_at)
```

---

## ✨ Code Quality Features

✅ **No God Classes** - Each class has one job
✅ **No Public Fields** - Everything is private with getters
✅ **Meaningful Names** - `ApplicationStatus`, `SponsorshipType`, not `a`, `x`
✅ **Comments** - Explaining why, not what (code explains what)
✅ **Proper Package Structure**:
- `model/` - Data models
- `service/` - Business logic
- `dao/` - Database access
- `ui/controller/` - UI controllers

---

## 🎓 How to Run the Demo

### **Prerequisites**
```bash
Java 17+
JavaFX 21+
MySQL Database
Maven
```

### **Setup**
```bash
1. Clone repository
2. mvn clean install
3. Set up MySQL database
4. Configure DB credentials in Db.java
5. mvn javafx:run
```

### **Demo Flow**
```
1. Show login with different roles (ADMIN/STUDENT/PROCTOR)
2. Explain User inheritance hierarchy
3. Show how different users see different features
4. Walk through code structure (packages, classes)
5. Explain how services use DAOs and interfaces
```

---

## 🚀 Points to Emphasize During Presentation

1. **"Why private fields matter?"**
   - Prevents invalid states
   - Forces using business logic (methods) for changes
   - Makes code safer and more maintainable

2. **"Why inheritance hierarchy?"**
   - Student, Admin, Proctor share common User behavior
   - Less code duplication
   - Easier to add new user types

3. **"Why interfaces/abstractions?"**
   - Can swap implementations (MySQL ↔ PostgreSQL)
   - Easy to test with mock objects
   - Reduces coupling between classes

4. **"Why service layer?"**
   - Business logic separate from UI
   - Same service can be used by desktop or API
   - Easier to change logic without breaking UI

5. **"Single Responsibility"**
   - `ApplicationService` only handles applications
   - `AuthService` only handles authentication
   - Easier to understand and modify

---

## 📝 Evaluation Checklist

- [x] OOP Principles (Encapsulation, Abstraction, Inheritance, Polymorphism)
- [x] SOLID Principles (All 5 demonstrated)
- [x] No god classes (each class has one job)
- [x] No public fields (everything private)
- [x] Clean inheritance hierarchy
- [x] Interface-based design
- [x] Good package structure
- [x] Meaningful names and comments
- [x] Functional GUI
- [x] Database integration

---

## 🎬 Live Demo Script

### **Part 1: Show the Code** (5 mins)
1. Open `User.java` - Show abstract base class
2. Open `Student.java`, `Admin.java`, `Proctor.java` - Show inheritance
3. Open `ApplicationService.java` - Show dependency injection
4. Explain how these concepts work together

### **Part 2: Run the Application** (5 mins)
1. Start app with `mvn javafx:run`
2. Login as student - show student dashboard
3. Logout and login as admin - show admin features
4. Show how same database supports different roles

### **Part 3: Q&A** (5 mins)
- Be ready to explain why certain decisions were made
- Show how to add a new feature (e.g., new ScoringPolicy)
- Explain trade-offs between flexibility and simplicity

---

## 📚 References

- **OOP Concepts**: https://www.oracle.com/java/technologies/
- **SOLID Principles**: https://en.wikipedia.org/wiki/SOLID
- **JavaFX**: https://gluonhq.com/products/javafx/
- **Design Patterns**: https://refactoring.guru/design-patterns
