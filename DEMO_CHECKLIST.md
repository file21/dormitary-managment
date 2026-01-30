# Live Presentation Checklist

## ✅ Files to Show During Demo

### OOP Principles
```
📁 src/main/java/edu/aau/dorm/model/
  ├─ User.java                    ← Show INHERITANCE hierarchy
  ├─ Student.java                 ← Extends User
  ├─ Admin.java                   ← Extends User  
  ├─ Proctor.java                 ← Extends User
  ├─ Bed.java                     ← Show ENCAPSULATION (private fields)
  └─ DormApplication.java         ← Show ENCAPSULATION (immutable)

📁 src/main/java/edu/aau/dorm/service/
  ├─ ProctorService.java          ← Show SINGLE RESPONSIBILITY + DEPENDENCY INVERSION
  ├─ ApplicationService.java      ← Show dependency injection
  ├─ ScoringPolicy.java           ← Show interface (ABSTRACTION)
  └─ DefaultScoringPolicy.java    ← Show implementation (POLYMORPHISM)
```

---

## 🎯 What to Say for Each Principle

### 1. ENCAPSULATION
**Files**: `User.java`, `Bed.java`, `DormApplication.java`

**Script**:
> "Look at this class. All fields are PRIVATE - can't access them from outside. 
> Why? Because if someone sets username to null, the object is broken.
> Instead, we provide getter methods. You can READ the data, but not CHANGE it.
> This prevents bugs and invalid states."

**Show in code**:
- Point to `private final` keywords
- Point to `public` getter methods
- Point to missing setters

---

### 2. ABSTRACTION  
**Files**: `ScoringPolicy.java`, `DefaultScoringPolicy.java`, `AuthService.java`

**Script**:
> "See this interface? It defines WHAT to do (score an application).
> But it doesn't say HOW. The implementation is hidden.
> This is abstraction - you don't need to know the complex details.
> You just call score() and get a number back."

**Show in code**:
- Point to `public interface ScoringPolicy`
- Point to `int score(...)`
- Show how different implementations exist but interface stays same

---

### 3. INHERITANCE
**Files**: `User.java`, `Student.java`, `Admin.java`, `Proctor.java`

**Script**:
> "All users (Student, Admin, Proctor) have common stuff:
> username, passwordHash, role, active status.
> Instead of repeating this code 3 times, we create a parent class USER.
> Student, Admin, Proctor inherit from User.
> They get all common behavior for free, but each is still unique."

**Show in code**:
- Point to `public abstract class User extends BaseEntity`
- Point to `public class Student extends User`
- Point to `public class Admin extends User`
- Point to `public class Proctor extends User`

---

### 4. POLYMORPHISM
**Files**: `AuthService.java`, `LoginController.java`

**Script**:
> "Look at this code: User user = authService.login(...);
> This User variable can hold a Student, Admin, or Proctor.
> The variable is the same, but the actual object is different.
> This is polymorphism - one variable, many forms.
> It makes code flexible - same code works with different user types."

**Show in code**:
- Point to `User user = ...`
- Show how Student, Admin, Proctor can be assigned
- Show how you can check `instanceof` or use polymorphic methods

---

### 5. SOLID - SINGLE RESPONSIBILITY
**Files**: `AuthService.java`, `NotificationService.java`, `ProctorService.java`, `ApplicationService.java`

**Script**:
> "Each service has ONE job:
> - AuthService: Only handles login/logout
> - NotificationService: Only handles notifications
> - ProctorService: Only handles check-in/withdrawal
> - ApplicationService: Only handles application workflow
> 
> If you need to change how authentication works, you change ONE class.
> This makes code easier to maintain."

**Show in code**:
- Point to `ProctorService.checkIn()`
- Explain: only handles check-in, doesn't do notifications itself
- Show how it CALLS `notificationService` instead

---

### 6. SOLID - OPEN/CLOSED PRINCIPLE
**Files**: `ScoringPolicy.java`, `DefaultScoringPolicy.java`

**Script**:
> "See this interface? It's closed - you don't change it.
> But it's open for extension - you can create new implementations.
> Want different scoring logic? Create new ScoringPolicy class.
> The old code doesn't change at all."

**Show in code**:
- Point to `public interface ScoringPolicy`
- Point to `public class DefaultScoringPolicy implements ScoringPolicy`
- Explain: Can add new implementations without modifying this one

---

### 7. SOLID - DEPENDENCY INVERSION
**Files**: `ProctorService.java`

**Script**:
> "Look at the constructor. ProctorService receives INTERFACES:
> ApplicationDao, AllocationDao, NotificationService.
> Not concrete implementations like ApplicationDaoPg.
> 
> Why? Because if you want to switch from MySQL to PostgreSQL,
> you just change which implementation you pass in.
> The service code NEVER changes."

**Show in code**:
- Point to constructor parameters (interfaces)
- Point to `private final ApplicationDao applicationDao;`
- Point to comment: "Depends on INTERFACE not implementation"

---

## 🚀 Demo Script (15 minutes)

### Part 1: Code Walkthrough (8 mins)

```
1. "Let me show you the class hierarchy" (2 mins)
   → Open User.java
   → Open Student.java, Admin.java, Proctor.java
   → Explain inheritance

2. "Look at encapsulation" (2 mins)
   → Open Bed.java
   → Show private fields, public getters only
   → Explain why this is safe

3. "Here's how services use abstractions" (2 mins)
   → Open ProctorService.java
   → Show constructor with interface parameters
   → Explain dependency injection

4. "And here's an interface with implementations" (2 mins)
   → Open ScoringPolicy.java
   → Open DefaultScoringPolicy.java
   → Explain polymorphism
```

### Part 2: Run the Application (5 mins)

```
1. "Let's start the application" (1 min)
   → mvn javafx:run

2. "Login as different roles" (3 mins)
   → Login as student (show student dashboard)
   → Logout and login as admin (show admin features)
   → Logout and login as proctor (show proctor features)

3. "Notice how same database supports all roles" (1 min)
   → Explain role-based access control
```

### Part 3: Q&A (2 mins)

**Prepare for these questions**:

1. "Why is inheritance important?"
   > "Avoids code duplication. All users share common behavior (username, password).
   > Instead of repeating that 3 times, we inherit from User."

2. "Why use interfaces?"
   > "Loose coupling. We can change database from MySQL to PostgreSQL
   > without changing any service code. Just swap the implementation."

3. "How would you add a new feature?"
   > "Example: Add new user type (Owner). 
   > Create Owner extends User.
   > Single Responsibility - each thing has one job.
   > Services don't care about specific user types, just the interface."

4. "Why private fields?"
   > "Encapsulation. Prevents invalid states. You can't set a field to an invalid value.
   > All changes go through methods where we can enforce business rules."

5. "What's the benefit of all this design?"
   > "Maintainability. Code is easier to understand, modify, and test.
   > You know exactly where to go to change something.
   > Changes in one place don't break other parts."

---

## 📝 Key Talking Points

### "Less is More"
> "We kept this project focused. Not trying to build everything.
> Just enough to show OOP and SOLID principles clearly.
> This makes it easier to understand and demonstrate."

### "Design Decisions"
> "Every class, interface, and method has a purpose.
> No 'god classes' doing everything.
> No public fields that can be misused.
> Clear separation between UI, business logic, and data access."

### "Easy to Extend"
> "Want to add a new user type? Add it, it works.
> Want different scoring? Implement the interface, swap it in.
> Want to change database? Create new Dao implementation, swap it.
> No existing code changes."

### "Testing"
> "Because we use interfaces and dependency injection,
> we can easily test with mock objects.
> No need for real database during testing."

---

## ⚠️ Potential Issues & Fixes

### Issue: "It doesn't compile"
**Fix**: 
```bash
mvn clean install
# Check that all dependencies are in pom.xml
# Check MySQL is running
```

### Issue: "Database connection failed"
**Fix**:
1. Check MySQL is running: `mysql -u root -p`
2. Create database: `CREATE DATABASE dormdb;`
3. Update connection string in `Db.java`
4. Run migrations from `schema_mysql.sql`

### Issue: "Login fails"
**Fix**:
1. Check test data is loaded: `SELECT * FROM users;`
2. Test credentials: username=`student1`, password=`password123`
3. Check that passwords are hashed (should be long strings)

---

## 💡 Pro Tips

1. **Slow Down**: Go line by line when explaining code
2. **Use Analogies**: "Private fields are like a locked box"
3. **Relate to Real Life**: "Inheritance is like real families - traits passed down"
4. **Show Consequences**: "If we made all fields public, someone could break the object"
5. **Emphasize Benefits**: "This design makes the code maintainable for years"

---

## 🎓 What Evaluators Want to Hear

✅ "This class has ENCAPSULATION because fields are private and accessed via getters"

✅ "We use INHERITANCE to avoid duplicating User properties in Student, Admin, Proctor"

✅ "This interface provides ABSTRACTION - hides the complex implementation details"

✅ "ProctorService follows SINGLE RESPONSIBILITY - it only handles check-in/check-out"

✅ "We depend on the interface (DEPENDENCY INVERSION), not the concrete class"

✅ "If we wanted to add new scoring logic, we just create a new class implementing ScoringPolicy (OPEN/CLOSED)"

---

## 📊 Evaluation Coverage

| Criteria | Where to Show | What to Say |
|----------|---------------|------------|
| **Encapsulation** | Bed.java, User.java | "Private fields, public getters, no setters" |
| **Abstraction** | ScoringPolicy.java | "Interface hides implementation complexity" |
| **Inheritance** | User → Student/Admin/Proctor | "All inherit from User, avoid duplication" |
| **Polymorphism** | Scoring, AuthService | "Different implementations of same interface" |
| **SOLID - S** | ProctorService only handles check-in | "One class, one responsibility" |
| **SOLID - O** | ScoringPolicy implementations | "Open for extension without modification" |
| **SOLID - L** | User can be Student/Admin/Proctor | "Substitutable for parent type" |
| **SOLID - I** | Focused interfaces (ApplicationDao, etc) | "Interfaces are focused, not bloated" |
| **SOLID - D** | ProctorService dependencies | "Depends on interfaces, not implementations" |
| **No God Classes** | Show all services (separate responsibilities) | "Each class has one specific job" |
| **No Public Fields** | Show private final in models | "All fields are private with controlled access" |
| **Good Names** | Point out descriptive names | "ApplicationStatus, SponsorshipType, etc" |
| **Clean GUI** | Run the app | "Clean, logical interface" |
| **Works** | Complete a full workflow | "Login, apply, review, approve works end-to-end" |

