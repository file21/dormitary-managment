# Dormitory Management System - Educational Demo

## 🎓 Project Purpose

This is a **learning project** to demonstrate **Object-Oriented Programming (OOP)** and **SOLID principles** through a real-world JavaFX application.

**Goal**: Show how to write clean, maintainable code using proper OOP design.

---

## 📚 OOP Principles Demonstrated

### ✅ Encapsulation
- **What**: Keep data private, expose only through methods
- **Where**: `User.java`, `Bed.java`, `DormApplication.java`
- **Why**: Prevents invalid states, ensures data integrity

### ✅ Abstraction
- **What**: Hide complex details behind simple interfaces
- **Where**: `ScoringPolicy.java`, `ApplicationDao.java`
- **Why**: Makes code easier to use and modify

### ✅ Inheritance
- **What**: Child classes inherit from parent classes
- **Where**: `Student` → `User`, `Admin` → `User`, `Proctor` → `User`
- **Why**: Avoids code duplication

### ✅ Polymorphism
- **What**: Same method behaves differently in different classes
- **Where**: User variable can hold Student/Admin/Proctor
- **Why**: Flexible code that works with multiple types

---

## 🏗️ SOLID Principles Applied

| Principle | Example | Benefit |
|-----------|---------|---------|
| **S**ingle Responsibility | AuthService only does authentication | Easy to modify, understand |
| **O**pen/Closed | ScoringPolicy interface → multiple implementations | Add features without changing existing code |
| **L**iskov Substitution | Student, Admin, Proctor used as User | Flexible, extensible code |
| **I**nterface Segregation | Separate ApplicationDao, AllocationDao | Clean, focused interfaces |
| **D**ependency Inversion | Service uses interfaces, not concrete classes | Loosely coupled, testable |

---

## 📁 Project Structure

```
src/main/java/edu/aau/dorm/
├── model/               ← Data models (User, Student, Bed, etc.)
│   ├── User.java       ← Abstract base class (inheritance demo)
│   ├── Student.java    ← Extends User
│   ├── Admin.java      ← Extends User
│   ├── Proctor.java    ← Extends User
│   ├── Bed.java        ← Encapsulation demo
│   ├── DormApplication.java  ← Immutable model
│   └── ...
│
├── service/            ← Business logic (SOLID principles)
│   ├── AuthService.java        ← Single responsibility: authentication only
│   ├── ApplicationService.java  ← Single responsibility: applications only
│   ├── ProctorService.java      ← Single responsibility: check-in/check-out
│   ├── ScoringPolicy.java       ← Interface (abstraction)
│   ├── DefaultScoringPolicy.java ← Implementation (polymorphism)
│   └── ...
│
├── dao/                ← Data access (dependency inversion)
│   ├── UserDao.java    ← Interface
│   ├── UserDaoPg.java  ← PostgreSQL implementation
│   ├── ApplicationDao.java     ← Interface
│   ├── ApplicationDaoPg.java   ← PostgreSQL implementation
│   └── ...
│
├── ui/
│   ├── controller/     ← JavaFX controllers (UI layer)
│   │   ├── LoginController.java
│   │   ├── StudentDashboardController.java
│   │   ├── AdminDashboardController.java
│   │   └── ...
│   └── ...
│
├── util/              ← Utilities
│   ├── Db.java        ← Database configuration
│   ├── PasswordHasher.java
│   └── ...
│
└── App.java          ← Entry point
```

---

## 🎯 Key Files to Study

### For Encapsulation
- `src/main/java/edu/aau/dorm/model/User.java` - Base class with private fields
- `src/main/java/edu/aau/dorm/model/Bed.java` - Immutable with getters only
- `src/main/java/edu/aau/dorm/model/DormApplication.java` - Private final fields

### For Inheritance
- `src/main/java/edu/aau/dorm/model/User.java` - Abstract parent
- `src/main/java/edu/aau/dorm/model/Student.java` - Child class
- `src/main/java/edu/aau/dorm/model/Admin.java` - Child class
- `src/main/java/edu/aau/dorm/model/Proctor.java` - Child class

### For Abstraction & Polymorphism
- `src/main/java/edu/aau/dorm/service/ScoringPolicy.java` - Interface
- `src/main/java/edu/aau/dorm/service/DefaultScoringPolicy.java` - Implementation

### For SOLID Principles
- `src/main/java/edu/aau/dorm/service/ProctorService.java` - Single responsibility + dependency inversion
- `src/main/java/edu/aau/dorm/service/ApplicationService.java` - Single responsibility
- `src/main/java/edu/aau/dorm/service/AuthService.java` - Single responsibility

---

## 🚀 Quick Start

### Prerequisites
```
- Java 17+
- JavaFX 21+
- MySQL 8+
- Maven 3.6+
```

### Setup
```bash
# 1. Clone the repository
git clone <repo-url>
cd dormitory-managment

# 2. Install dependencies
mvn clean install

# 3. Set up database
# Create MySQL database
CREATE DATABASE dormdb;

# Import schema
mysql -u root -p dormdb < src/main/resources/sql/schema.sql

# 4. Configure database connection (if needed)
# Edit: src/main/java/edu/aau/dorm/util/Db.java
# Update DB_URL, DB_USER, DB_PASS

# 5. Run the application
mvn javafx:run
```

### Test Credentials
```
Role     Username   Password
------   --------   ---------
Student  student1   password123
Admin    admin1     password123
Proctor  proctor1   password123
```

---

## 💻 Demo Flow

### Part 1: Show the Code (8 minutes)
1. **Encapsulation** - Show `User.java`, explain private fields
2. **Inheritance** - Show `Student.java` extending `User`, explain hierarchy
3. **SOLID** - Show `ProctorService.java`, explain dependency injection
4. **Abstraction** - Show `ScoringPolicy.java` interface

### Part 2: Run the Application (5 minutes)
1. Start app: `mvn javafx:run`
2. Login as student → show student dashboard
3. Logout, login as admin → show admin features
4. Logout, login as proctor → show proctor features

### Part 3: Q&A (2 minutes)
Answer questions about OOP principles and design decisions

---

## 📖 Documentation Files

| File | Purpose |
|------|---------|
| `PRESENTATION_GUIDE.md` | Complete presentation outline with what to show |
| `DEMO_CHECKLIST.md` | Step-by-step demo checklist with code locations |
| `TALKING_POINTS.md` | What to say for each principle |
| `OOP_EXPLAINED.md` | Simple explanations with good/bad examples |
| `README_FOR_DEMO.md` | This file |

---

## ✨ Code Quality Features

✅ **No God Classes** - Each class has one job
✅ **No Public Fields** - Everything is private with controlled access
✅ **Meaningful Names** - `ApplicationStatus`, `SponsorshipType`, not `a`, `x`
✅ **Comments Explain Why** - Not what (code already shows that)
✅ **Clean Package Structure** - model, service, dao, ui clearly separated
✅ **Dependency Injection** - Services receive dependencies, not create them
✅ **Interface-Based Design** - Depend on interfaces, not concrete classes
✅ **Error Handling** - Proper exceptions with meaningful messages

---

## 🔧 Common Issues & Solutions

### Issue: "Cannot find symbol" errors
**Solution**:
```bash
mvn clean install
```

### Issue: Database connection fails
**Solution**:
1. Ensure MySQL is running
2. Create database: `CREATE DATABASE dormdb;`
3. Check `Db.java` connection string
4. Verify username/password in `Db.java`

### Issue: Login doesn't work
**Solution**:
1. Verify test data exists: `SELECT * FROM users;`
2. Check password hashing is working
3. Try default credentials: `student1` / `password123`

### Issue: Application won't start
**Solution**:
```bash
# Clear cache and rebuild
mvn clean install

# Check Java version
java -version  # Should be 17+

# Check JavaFX is in pom.xml
grep -A2 "javafx" pom.xml
```

---

## 🎓 Learning Outcomes

After studying this project, you should understand:

1. **Encapsulation**
   - Why fields should be private
   - How getters/setters provide controlled access
   - Benefits of immutability

2. **Inheritance**
   - How to create class hierarchies
   - When to use abstract classes
   - Avoiding code duplication

3. **Abstraction**
   - Creating and using interfaces
   - How interfaces hide complexity
   - Benefits of abstraction

4. **Polymorphism**
   - Runtime polymorphism with interfaces
   - Method overriding in inheritance hierarchies
   - Flexible code design

5. **SOLID Principles**
   - Single Responsibility - one class, one job
   - Open/Closed - extend without modifying
   - Liskov Substitution - child ≈ parent
   - Interface Segregation - focused interfaces
   - Dependency Inversion - depend on abstractions

---

## 📊 Evaluation Checklist

- [ ] Encapsulation demonstrated (private fields, getters)
- [ ] Abstraction shown (interfaces, hiding complexity)
- [ ] Inheritance applied (User hierarchy)
- [ ] Polymorphism explained (different implementations)
- [ ] SOLID principles visible in code
- [ ] No god classes (each class has one job)
- [ ] No public fields (all private with access control)
- [ ] Clean package structure (model, service, dao, ui)
- [ ] Good naming (descriptive class/method names)
- [ ] Application runs without crashing
- [ ] All features work correctly
- [ ] GUI is clean and intuitive

---

## 🎬 Presentation Tips

### Do
✅ Go line by line when explaining code
✅ Use analogies ("Private fields are like a locked box")
✅ Show both good and bad examples
✅ Emphasize benefits ("This design makes maintenance easy")
✅ Point to actual code in the IDE
✅ Answer "Why?" not just "What?"

### Don't
❌ Rush through the code
❌ Assume everyone knows OOP
❌ Overcomplicate explanations
❌ Make it sound magical ("Just use interfaces bro")
❌ Skip the practical benefits

---

## 🚀 Next Steps for Learning

1. **Study the code** - Read through each package
2. **Run the application** - See it in action
3. **Modify the code** - Add a new feature using same patterns
4. **Write tests** - Test the services
5. **Add new user type** - Create Owner extends User
6. **Change database** - Implement different DAO with PostgreSQL

---

## 📚 References

- Java Documentation: https://docs.oracle.com/en/java/
- SOLID Principles: https://en.wikipedia.org/wiki/SOLID
- JavaFX: https://gluonhq.com/products/javafx/
- Design Patterns: https://refactoring.guru/design-patterns

---

## ❓ FAQ

**Q: Why so much separation of concerns?**
A: Makes code maintainable. If authentication breaks, you go to AuthService. If database fails, you go to DAO. Clear separation means clear responsibility.

**Q: Isn't this overengineered for this project?**
A: This is actually the MINIMUM good design. Real projects are much more complex.

**Q: Can I simplify it?**
A: This is already simplified for learning. Real enterprise systems are much more complex.

**Q: What if I wanted to add a new user type?**
A: Create a new class extending User. All existing code works unchanged - that's the power of OOP design!

---

## 📝 Notes

This is a **teaching project**. The goal is to demonstrate OOP and SOLID principles clearly, not to build a production system. The code is intentionally straightforward so you can understand the principles without getting lost in complexity.

**Key Design Decision**: We keep it simple enough to understand in 15 minutes, but complex enough to show real patterns.

---

## 📧 Questions?

If something isn't clear, go to the `TALKING_POINTS.md` or `OOP_EXPLAINED.md` files for more detailed explanations.

Good luck with your presentation! 🎓
