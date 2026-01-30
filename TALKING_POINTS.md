# Quick Talking Points for Presentation

## When Evaluator Asks: "Explain Encapsulation"

**What to do**:
1. Open `User.java` or `Bed.java`
2. Point to `private final` keywords
3. Point to getter methods

**What to say**:
> "Encapsulation means data hiding. See how all fields are private?
> That means code outside this class CANNOT access them directly.
> They can only READ through these getter methods.
> This prevents bugs - for example, someone can't set the username to null."

**Example from code**:
```java
private final String username;  // ← Can't access directly
public String username() {       // ← Can only read through getter
    return username;
}
// ← NO SETTER - can't be changed!
```

---

## When Evaluator Asks: "Show me Abstraction"

**What to do**:
1. Open `ScoringPolicy.java`
2. Show interface definition
3. Show implementation in `DefaultScoringPolicy.java`

**What to say**:
> "Abstraction means hiding the 'how'.
> This interface ScoringPolicy just says 'give me a student and application, return an int score'.
> But HOW we calculate the score? That's hidden in the implementation.
> We could change the calculation completely and users don't care - they just call score()."

**Key point**:
> "The main benefit? You can swap implementations without changing other code.
> If you want different scoring, create a new class, no changes needed elsewhere."

---

## When Evaluator Asks: "What about Inheritance?"

**What to do**:
1. Open `User.java` (abstract class)
2. Show `Student.java`, `Admin.java`, `Proctor.java`
3. Point to `extends User`

**What to say**:
> "Instead of copying the same code three times, we use inheritance.
> User is the parent class with common stuff: username, password, role, etc.
> Student, Admin, and Proctor INHERIT from User.
> They get all that for free, but each one is unique.
> Student has fullName and aauId that others don't have."

**Avoid saying**: "Just copy-paste the User code into each class"
**Instead say**: "Inheritance avoids duplication"

---

## When Evaluator Asks: "How is this Polymorphism?"

**What to do**:
1. Open `LoginController.java` or any code using User
2. Show: `User user = authService.login(...);`
3. Explain that this variable can hold Student, Admin, or Proctor

**What to say**:
> "Polymorphism means 'many forms'.
> This variable is type User, but it could actually be a Student, Admin, or Proctor.
> Same variable, different runtime types.
> Makes code flexible - you write logic once but it works with different user types."

**Real example from our code**:
```java
User user = authService.login(username, password);
// user could be Student, Admin, or Proctor!
if (user.active()) {
    // Works with all three types
}
```

---

## When Evaluator Asks: "Explain SOLID - Single Responsibility"

**What to do**:
1. Show `AuthService.java` - only does authentication
2. Show `ProctorService.java` - only does check-in/check-out
3. Show `ApplicationService.java` - only handles applications

**What to say**:
> "Single Responsibility means each class has ONE job.
> AuthService? Only handles login/logout.
> ProctorService? Only handles check-in/check-out.
> ApplicationService? Only handles application workflow.
> 
> Why is this good? If something breaks or needs change,
> you know EXACTLY which class to go to. No hunting around in giant classes."

**Contrast**:
- ❌ BAD: UserManager class that does auth, notifications, scoring, reporting
- ✅ GOOD: AuthService (auth only), NotificationService (notifications only), etc.

---

## When Evaluator Asks: "Show Open/Closed Principle"

**What to do**:
1. Open `ScoringPolicy.java` (the interface)
2. Show `DefaultScoringPolicy.java` (one implementation)
3. Explain you could add another implementation

**What to say**:
> "Open/Closed means open for EXTENSION, closed for MODIFICATION.
> This interface ScoringPolicy is closed - you don't change it.
> But it's open for extension - you can create new implementations.
> Want different scoring logic? Create a new class implementing ScoringPolicy.
> Old code doesn't need to change at all."

**Concrete example**:
```java
// Want to add special scoring for disabled students?
// Just create:
public class DisabilityBonusScoringPolicy implements ScoringPolicy {
    @Override
    public int score(Student s, DormApplication app) {
        // Different logic
    }
}
// ← Original code unchanged!
```

---

## When Evaluator Asks: "What about Liskov Substitution?"

**What to do**:
1. Show that Student, Admin, Proctor extend User
2. Explain they can be used wherever User is expected

**What to say**:
> "Liskov Substitution means child classes can replace parent classes.
> Student, Admin, Proctor are all Users.
> So code that expects a User can work with any of them.
> This works because they implement all the same methods from User."

**Simple example**:
```java
public void printUserInfo(User user) {
    // This method accepts ANY User type
    System.out.println(user.username());  // Works for Student, Admin, Proctor
}

printUserInfo(new Student(...));  // ✓ Works
printUserInfo(new Admin(...));    // ✓ Works
printUserInfo(new Proctor(...));  // ✓ Works
```

---

## When Evaluator Asks: "Interface Segregation Principle?"

**What to do**:
1. Show `ApplicationDao.java` - focused interface
2. Show `AllocationDao.java` - different focused interface
3. Explain they're not mixed together

**What to say**:
> "Interface Segregation means don't create huge interfaces with everything.
> We have separate interfaces: ApplicationDao for applications, AllocationDao for allocations.
> Each service uses only what it needs.
> If we mixed everything in one interface, services would depend on things they don't use."

**Bad way vs Good way**:
```java
// ❌ BAD - everything in one interface
public interface MegaDao {
    DormApplication getApplication(...);
    void updateApplication(...);
    void allocateBed(...);
    void freeBed(...);
    // ... 20 more methods
}

// ✅ GOOD - focused interfaces
public interface ApplicationDao {
    DormApplication getById(...);
    void setStatus(...);
}

public interface AllocationDao {
    void allocate(...);
    void freeBedIfAllocated(...);
}
```

---

## When Evaluator Asks: "Dependency Inversion Principle?"

**What to do**:
1. Open `ProctorService.java`
2. Show constructor with interface parameters
3. Point to comment about depending on interfaces, not implementations

**What to say**:
> "Dependency Inversion means depend on interfaces, not concrete classes.
> Look at ProctorService - it receives ApplicationDao interface, not ApplicationDaoPg class.
> Why? Because if we want to switch databases from MySQL to PostgreSQL,
> we just pass a different implementation. The service code NEVER changes.
> This makes code flexible and testable."

**Code example**:
```java
// ✅ GOOD - depends on interface
public class ProctorService {
    private final ApplicationDao applicationDao;  // Interface!
    
    public ProctorService(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }
}

// Use with MySQL
new ProctorService(new ApplicationDaoPg());

// Or PostgreSQL (just swap implementation!)
new ProctorService(new ApplicationDaPostgres());

// Or mock for testing!
new ProctorService(new MockApplicationDao());

// ❌ BAD - depends on concrete class
public class ProctorService {
    private final ApplicationDaoPg applicationDao;  // Concrete class!
    // ← Can't swap implementations, hard to test
}
```

---

## When Evaluator Asks: "Why no God Classes?"

**What to do**:
1. Show the package structure - separate concerns
2. Show different services doing different things

**What to say**:
> "A God class is one class doing everything.
> We avoid that by splitting into focused classes.
> AuthService handles auth. ProctorService handles check-in.
> ApplicationService handles applications. Notification Service handles notifications.
> This is Single Responsibility from SOLID."

**Red flags for God classes**:
- ❌ 1000+ lines in one file
- ❌ 30+ public methods
- ❌ Handles UI, database, business logic, all mixed
- ❌ Hard to test because it does too much

---

## When Evaluator Asks: "Why private fields?"

**What to do**:
1. Open any model class - Bed, User, DormApplication
2. Show all fields are `private`
3. Show getter methods only

**What to say**:
> "Public fields are dangerous. Anyone can change them to invalid values.
> Private fields prevent that. You MUST use getter/setter methods.
> In those methods, we can validate and enforce business rules.
> This prevents bugs and invalid states."

**Example of the problem**:
```java
// ❌ BAD - public field
public class Bed {
    public String bedLabel;  // Anyone can set this!
}

Bed bed = new Bed();
bed.bedLabel = null;  // ✗ Now the object is broken!

// ✅ GOOD - private field
public class Bed {
    private final String bedLabel;  // Can't change
    
    public Bed(String bedLabel) {
        if (bedLabel == null) throw new IllegalArgumentException();
        this.bedLabel = bedLabel;  // Validated!
    }
    
    public String bedLabel() { return bedLabel; }  // Only getter
}
```

---

## When Evaluator Asks: "How would you add a new feature?"

**Example: Add a new user type "Owner"**

**What to say**:
> "Following OOP principles, here's how we'd add Owner:
> 
> 1. Create Owner extends User - inherits all user properties
> 2. Add Owner-specific fields if needed (e.g., managedBlocks)
> 3. Update UserDao to handle Owner
> 4. Add Owner to User.Role enum
> 5. Create OwnerService with owner-specific operations
> 6. Add OwnerDashboardController for the UI
> 
> See? Existing code doesn't break.
> We just ADD new classes following the same pattern."

---

## When Evaluator Asks: "How do you test this?"

**What to say**:
> "Because we use dependency injection and interfaces,
> testing is easy.
> 
> Instead of passing real ApplicationDaoPg,
> we pass MockApplicationDao.
> The service doesn't care - it just uses the interface.
> 
> No need for real database, no need for real file system.
> Just mock implementations that behave how we want."

**Example**:
```java
public void testCheckIn() {
    // Create mock instead of real DAO
    ApplicationDao mockDao = new MockApplicationDao();
    ProctorService service = new ProctorService(mockDao);
    
    // Test without real database!
    service.checkIn(1L, 1L, 1L, "A-101");
    
    // Verify behavior
    assertEquals(ApplicationStatus.CHECKED_IN, mockDao.getStatus(1L));
}
```

---

## General Principles to Emphasize

### "Everything has a reason"
> "Every class, every interface, every method serves a specific purpose.
> Nothing is there 'just because'. This makes code maintainable."

### "Code is read more than written"
> "Even if it takes longer to write proper classes,
> the payoff is code that's easy to understand and modify later."

### "Design prevents bugs"
> "By using encapsulation, abstraction, inheritance properly,
> we prevent whole categories of bugs before they happen."

### "Professional code"
> "This is how real companies write code.
> Understanding these principles now gives you a big advantage."

---

## What NOT to Say

❌ "Inheritance is just copying code differently"
✅ "Inheritance is code reuse - DRY principle"

❌ "We use interfaces because it's required"
✅ "We use interfaces for flexibility and testability"

❌ "Polymorphism is just assigning child to parent variable"
✅ "Polymorphism lets us write flexible code that works with multiple types"

❌ "Single Responsibility is about splitting files"
✅ "Single Responsibility means each class has one reason to change"

❌ "We're over-engineering for a school project"
✅ "These principles are industry standard - better to learn them now"

