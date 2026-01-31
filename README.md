# Dormitory Management System

A comprehensive JavaFX desktop application for managing dormitory applications, assignments, and resident tracking with MySQL database integration.

## Features

### For Students
- Account registration and login
- Submit dormitory applications
- View application status and admin notes
- Send/receive messages
- Upload required documents
- View assigned building and entry/withdrawal dates

### For Admins
- Review and update application statuses (Approve, Decline, Resubmit)
- Post announcements to all students
- Message students directly
- Search students by ID
- Assign buildings to approved students
- Export student data to CSV

### For Proctors
- View students assigned to their building
- Register student entry dates
- Register student withdrawal dates
- Message students in their building

### For Owners
- All admin capabilities
- Add and remove admin and proctor accounts
- Assign buildings to proctors
- Manage system-wide staff

## Technology Stack

- **Java 21** - Core programming language
- **JavaFX 21** - Desktop GUI framework
- **MySQL** - Database (via JDBC)
- **HikariCP** - Database connection pooling
- **Maven** - Build and dependency management

## OOP & SOLID Principles

This system demonstrates:
- **Encapsulation** - Private fields with controlled access
- **Abstraction** - Repository interfaces hiding implementation details
- **Inheritance** - Student extends User
- **Polymorphism** - Repository pattern with interchangeable implementations

SOLID Principles:
- **SRP** - Each class has a single responsibility
- **OCP** - Extensible through interfaces
- **LSP** - Student is substitutable for User
- **ISP** - Focused, specific repository interfaces
- **DIP** - Service depends on abstractions, not concrete classes

See [SOLID_PRINCIPLES.md](SOLID_PRINCIPLES.md) for detailed documentation.

## Setup

### 1. Database Setup

The application uses **MySQL database**. See [DATABASE_SETUP.md](DATABASE_SETUP.md) for detailed instructions.

**Quick Setup:**

```bash
# Initialize the database (creates dormdb and all tables)
./init-db.sh
```

Or manually:

```bash
mysql -h 127.0.0.1 -P 3310 -u root -p30MB6-I67J4-3DN0T-L609U < src/main/resources/sql/schema.sql
```

This creates:
- Database: `dormdb`
- 7 tables: users, students, applications, announcements, messages, building_assignments, document_paths
- Sample test users (admin, proctor, owner, student)

### 2. Database Configuration

Default connection (configured in `src/main/java/dorm/util/Db.java`):
- Host: `127.0.0.1:3310`
- Database: `dormdb`
- User: `root`
- Password: `30MB6-I67J4-3DN0T-L609U`

To override, set environment variables:

```bash
export DB_URL="jdbc:mysql://your-host:port/dormdb?serverTimezone=UTC&useSSL=false"
export DB_USER="your_username"
export DB_PASS="your_password"
```

### 3. Run the Application

```bash
mvn clean javafx:run
```

Or with custom JAVA_HOME:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn clean javafx:run
```

## Default Login Credentials

- **Admin**: username=`admin`, password=`admin123`
- **Proctor**: username=`proctor1`, password=`proctor123`
- **Owner**: username=`owner`, password=`owner123`
- **Student**: username=`student1`, password=`student123`

## Project Structure

```
src/main/java/dorm/
├── dao/                    # Data Access Objects (interfaces + MySQL implementations)
│   ├── *Repository.java    # DAO interfaces (ISP, DIP)
│   ├── MySql*.java         # MySQL implementations
│   └── DaoFactory.java     # Factory for creating DAOs
├── model/                  # Domain models
│   ├── User.java           # Base user class
│   ├── Student.java        # Student (inherits from User)
│   ├── Role.java           # User roles enum
│   ├── ApplicationStatus.java
│   └── ...
├── service/                # Business logic layer
│   └── DatabaseDormService.java
├── ui/                     # JavaFX UI components
│   ├── LoginViewDb.java
│   ├── StudentDashboardDb.java
│   ├── AdminDashboardDb.java
│   ├── ProctorDashboardDb.java
│   └── OwnerDashboardDb.java
├── util/                   # Utilities
│   ├── Db.java             # Database connection (HikariCP)
│   └── Validation.java
└── App.java                # Main application entry point
```

## File I/O Operations

1. **Database I/O** - All data persisted to MySQL via JDBC
2. **CSV Export** - Export student lists to CSV files
3. **File Upload** - FileChooser for selecting documents and payment slips

## Build & Test

```bash
# Clean and compile
mvn clean compile

# Run application
mvn javafx:run

# Package (optional)
mvn package
```

## License

Educational project for AAU coursework.
