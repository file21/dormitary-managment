# Visual Diagrams for Presentation

## 1. Inheritance Hierarchy (INHERITANCE Principle)

```
                  BaseEntity
                      ↑
                      │
                    User (abstract)
                  │   │   │
        ┌─────────┼───┼───┴─────────┐
        │         │   │             │
     Student    Admin Proctor      (Other users)
        │         │   │
        └─ Common properties:
           • username
           • passwordHash
           • role
           • active
           • createdAt
        
        └─ Unique properties:
           Student: fullName, aauId, department, yearOfStudy
           Admin: (none - just core User properties)
           Proctor: (none - just core User properties)
```

**Key Point**: "All inherit username, password, role, etc. No duplication!"

---

## 2. Encapsulation Model (ENCAPSULATION Principle)

```
┌─────────────────────────────────────┐
│          Bed Class                  │
├─────────────────────────────────────┤
│ PRIVATE FIELDS (Hidden)             │
│ ────────────────────────────────    │
│ - blockId                           │
│ - bedLabel                          │
│ - occupied                          │
│                                     │
│ PUBLIC METHODS (Controlled Access)  │
│ ────────────────────────────────    │
│ + blockId() : long    [getter]      │
│ + bedLabel() : String [getter]      │
│ + isOccupied() : boolean [getter]   │
│                                     │
│ ✗ NO SETTERS (immutable)            │
└─────────────────────────────────────┘

        ↓ Safe Usage ↓

Code outside can:
✓ READ: bed.bedLabel()
✗ WRITE: bed.bedLabel = "X-01"  ← COMPILE ERROR!

Result: Object state always valid!
```

**Key Point**: "Private fields prevent invalid states"

---

## 3. SOLID - Dependency Inversion (D in SOLID)

```
WITHOUT Dependency Inversion (BAD):
═════════════════════════════════════

    ProctorService
         ↓
         ├─→ ApplicationDaoPg (concrete)
         ├─→ AllocationDaoPg (concrete)
         └─→ NotificationServiceDb (concrete)
    
    ✗ Hard-coupled to implementations
    ✗ Can't swap to PostgreSQL easily
    ✗ Hard to test (can't use mocks)


WITH Dependency Inversion (GOOD):
══════════════════════════════════════

    ProctorService
         ↓
         ├─→ ApplicationDao (interface)
         ├─→ AllocationDao (interface)
         └─→ NotificationService (interface)
         
         ↓ Actual implementations injected ↓
         
    MySQL Implementation:
    ├─ ApplicationDaoPg implements ApplicationDao
    ├─ AllocationDaoPg implements AllocationDao
    └─ NotificationServiceDb implements NotificationService
    
    PostgreSQL Implementation:
    ├─ ApplicationDaPostgres implements ApplicationDao
    ├─ AllocationDaPostgres implements AllocationDao
    └─ NotificationServicePostgres implements NotificationService
    
    Testing Implementation:
    ├─ MockApplicationDao implements ApplicationDao
    ├─ MockAllocationDao implements AllocationDao
    └─ MockNotificationService implements NotificationService
    
    ✓ Can swap implementations easily
    ✓ Easy to test with mocks
    ✓ ProctorService never changes!
```

**Key Point**: "Depend on interfaces, not concrete classes"

---

## 4. SOLID - Single Responsibility (S in SOLID)

```
One Class, One Job:
═══════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────┐
│ AuthService: ONLY authentication                    │
│ - login(username, password)                         │
│ - verify password                                   │
│ - return User                                       │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ ProctorService: ONLY check-in/check-out             │
│ - checkIn(applicationId, bedId)                     │
│ - withdraw(applicationId)                           │
│ - update bed occupancy                              │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ ApplicationService: ONLY application workflow        │
│ - submit(applicationId)                             │
│ - approve(applicationId)                            │
│ - reject(applicationId)                             │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ NotificationService: ONLY notifications             │
│ - notifyUser(userId, message)                       │
│ - sendEmail(email, message)                         │
└─────────────────────────────────────────────────────┘

                    ↓ Result ↓

✓ Each class has ONE reason to change
✓ Easy to locate code for modifications
✓ Changes in one class don't affect others
```

**Key Point**: "One class = one job = easier to maintain"

---

## 5. SOLID - Open/Closed Principle (O in SOLID)

```
                    ┌─────────────────────┐
                    │  ScoringPolicy      │
                    │    (Interface)      │
                    │                     │
                    │ + score() : int     │
                    └────────┬────────────┘
                             │
                             │ implements
                    ┌────────┴────────┐
                    │                 │
       ┌────────────▼──────────┐  ┌────────────▼──────────┐
       │ DefaultScoringPolicy  │  │ NewScoringPolicy      │
       │                       │  │                       │
       │ + score() : int       │  │ + score() : int       │
       │   - Base logic        │  │   - Different logic   │
       │   - Bonus math        │  │   - Machine learning? │
       └───────────────────────┘  └───────────────────────┘

        ↓ Want different scoring? ↓

NEW FEATURE (without changing existing code):

       ┌────────────┬──────────────────────┐
       │            │                      │
       │  Create    │ implements           │
       │  Special   │ ScoringPolicy        │
       │  Scoring   │                      │
       │  Policy    │ + score() : int      │
       │            │   - Special logic    │
       │            │                      │
       └────────────┴──────────────────────┘

✓ OPEN for extension (add new implementations)
✓ CLOSED for modification (existing code untouched)
```

**Key Point**: "Add new features without breaking old code"

---

## 6. Architecture Layers (Separation of Concerns)

```
┌──────────────────────────────────────────────────────┐
│                   UI LAYER                           │
│  ┌─────────────┬──────────────┬──────────────┐      │
│  │   Login     │   Student    │    Admin     │      │
│  │ Controller  │ Dashboard    │  Dashboard   │      │
│  └─────────────┴──────────────┴──────────────┘      │
│                         ↓                           │
├──────────────────────────────────────────────────────┤
│                 SERVICE LAYER                        │
│  ┌─────────────┬──────────────┬──────────────┐      │
│  │   Auth      │  Application │   Proctor    │      │
│  │ Service     │  Service     │  Service     │      │
│  │             │              │              │      │
│  │ Business    │ Business     │ Business     │      │
│  │ Logic       │ Logic        │ Logic        │      │
│  └─────────────┴──────────────┴──────────────┘      │
│                         ↓                           │
├──────────────────────────────────────────────────────┤
│              DATA ACCESS LAYER (DAO)                 │
│  ┌─────────────┬──────────────┬──────────────┐      │
│  │   User      │ Application  │ Allocation   │      │
│  │   DAO       │   DAO        │   DAO        │      │
│  │             │              │              │      │
│  │ Interfaces  │ Interfaces   │ Interfaces   │      │
│  └─────────────┴──────────────┴──────────────┘      │
│                         ↓                           │
├──────────────────────────────────────────────────────┤
│           DATABASE IMPLEMENTATIONS                   │
│  ┌─────────────┬──────────────┬──────────────┐      │
│  │   User      │ Application  │ Allocation   │      │
│  │  DaoPg      │   DaoPg      │   DaoPg      │      │
│  │             │              │              │      │
│  │ PostgreSQL  │ PostgreSQL   │ PostgreSQL   │      │
│  └─────────────┴──────────────┴──────────────┘      │
│                         ↓                           │
├──────────────────────────────────────────────────────┤
│                   DATABASE                          │
│        PostgreSQL / MySQL Server                     │
└──────────────────────────────────────────────────────┘

Benefits of this layering:
✓ UI doesn't know about database
✓ Service logic independent of UI
✓ Easy to change database without affecting UI
✓ Easy to test with mock DAO
✓ Clear responsibilities
```

**Key Point**: "Clear separation = easy maintenance"

---

## 7. Polymorphism in Action

```
┌─────────────────────────────────────┐
│  User user = login(username, pwd)   │
│                                     │
│  user could be:                     │
│  ├─ Student(...) ✓                  │
│  ├─ Admin(...) ✓                    │
│  ├─ Proctor(...) ✓                  │
│                                     │
│  Same variable, MANY FORMS!         │
└─────────────────────────────────────┘
       
       ↓ Usage ↓

if (user.canManageUsers()) {
    showAdminPanel();
}

// Same code, different behavior for different user types!
// This is POLYMORPHISM

User u1 = new Student(...);  → user.canManageUsers() = false
User u2 = new Admin(...);    → user.canManageUsers() = true
User u3 = new Proctor(...);  → user.canManageUsers() = false

// Same method call, different results!
```

**Key Point**: "One variable, many forms = flexible code"

---

## 8. Data Flow Example: Login

```
User clicks Login
      ↓
   [LoginController]
      ↓
   authService.login(username, password)
      ↓
   [AuthService - Business Logic]
   ├─ userDao.findByUsername(username)
   │  └─→ [UserDao - Abstract Interface]
   │      └─→ [UserDaoPg - PostgreSQL Implementation]
   │          └─→ [Database Query]
   │              └─→ Returns User object
   │
   └─ PasswordHasher.verify(password, hash)
      └─→ [PasswordHasher - Utility]
          └─→ Hash password and compare
              └─→ Returns boolean
   
   ↓ If valid ↓
   
   Return User object
      ↓
   [LoginController receives User]
      ↓
   Route to appropriate dashboard
      ↓
   [Display correct UI based on role]

LAYERING IN ACTION:
✓ UI (Controller) doesn't know database details
✓ Service handles business logic
✓ DAO handles database access
✓ Can swap implementations easily
```

**Key Point**: "Clear flow = easy to follow and modify"

---

## 9. Interface Segregation (I in SOLID)

```
GOOD - Focused Interfaces:
══════════════════════════════════════════

ProctorService needs:
┌───────────────────────────────────────┐
│ ApplicationDao                        │
│ - getById(id)                         │
│ - setStatus(id, status)               │
└───────────────────────────────────────┘

┌───────────────────────────────────────┐
│ AllocationDao                         │
│ - allocate(...)                       │
│ - freeBedIfAllocated(...)             │
└───────────────────────────────────────┘

✓ Only depends on what it needs
✓ Small, focused interfaces
✓ Easy to understand and implement


BAD - Monster Interface:
══════════════════════════════════════════

┌─────────────────────────────────────────────┐
│ MegaDao (everything in one interface)      │
│ - getApplication(id)                        │
│ - setApplicationStatus(id, status)          │
│ - getAllocations(...)                       │
│ - saveAllocation(...)                       │
│ - getBlock(id)                              │
│ - saveBlock(block)                          │
│ - getBed(id)                                │
│ - saveBed(bed)                              │
│ - getAllNotifications(...)                  │
│ - sendNotification(...)                     │
│ - ... 20+ more methods                      │
└─────────────────────────────────────────────┘

✗ ProctorService depends on things it doesn't use
✗ Hard to understand what each method does
✗ Hard to implement (must implement everything)
✗ Tight coupling
```

**Key Point**: "Small focused interfaces = better design"

---

## 10. Class Responsibilities Map

```
┌──────────────────────────────────────────────────────┐
│                  What Each Class Does                │
├──────────────────────────────────────────────────────┤
│                                                      │
│  User (abstract)                                     │
│  └─ Define common properties for all users           │
│                                                      │
│  Student, Admin, Proctor                             │
│  └─ Specific user types (inherit from User)          │
│                                                      │
│  Bed, DormApplication, Block                         │
│  └─ Data models (just hold data safely)              │
│                                                      │
│  AuthService                                         │
│  └─ Handle login/logout                              │
│                                                      │
│  ProctorService                                      │
│  └─ Handle check-in, check-out                       │
│                                                      │
│  ApplicationService                                  │
│  └─ Handle application workflow                      │
│                                                      │
│  ScoringPolicy (interface)                           │
│  └─ Define HOW to score applications                 │
│                                                      │
│  DefaultScoringPolicy                                │
│  └─ One way to score (pluggable)                     │
│                                                      │
│  UserDao, ApplicationDao, AllocationDao              │
│  └─ Define database operations (interfaces)          │
│                                                      │
│  UserDaoPg, ApplicationDaoPg, AllocationDaoPg        │
│  └─ Actually implement database operations (MySQL)   │
│                                                      │
└──────────────────────────────────────────────────────┘

SINGLE RESPONSIBILITY CHECK:
✓ Can describe each class in ONE sentence
✓ Each class has ONE reason to change
✓ No overlapping responsibilities
```

**Key Point**: "Each class knows exactly what it's supposed to do"

---

## Summary: How These Principles Work Together

```
ENCAPSULATION
    ↓
    Makes objects safe and predictable
    ↓
INHERITANCE
    ↓
    Avoids duplication, creates hierarchies
    ↓
ABSTRACTION
    ↓
    Hides complexity, provides clean interfaces
    ↓
POLYMORPHISM
    ↓
    Makes code flexible, works with multiple types
    ↓
SOLID PRINCIPLES
    ↓
    Organize code into clean, maintainable packages
    ↓
RESULT: Clean, Maintainable, Extensible Code! 🎉
```

