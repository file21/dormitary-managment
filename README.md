# Dormitory Management System

A comprehensive JavaFX desktop application for managing dormitory applications, assignments, and resident tracking with CSV file-based data persistence.

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
- **CSV Files** - Data persistence (stored in `data/` directory)
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

### 1. Prerequisites

- Java 21 or later
- Maven 3.8+

### 2. Build the Application

```bash
mvn clean compile
```

### 3. Run the Application

```bash
mvn javafx:run
```

Or export JAVA_HOME first if needed:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn javafx:run
```

## Data Storage

All data is stored in CSV files in the `data/` directory:
- `users.csv` - Admin, proctor, and owner accounts
- `students.csv` - Student accounts and information
- `applications.csv` - Dormitory applications
- `announcements.csv` - System announcements
- `messages.csv` - User messages
- `building_assignments.csv` - Proctor building assignments

The `data/` directory is created automatically if it doesn't exist, and sample data is included.

## Default Login Credentials

- **Admin**: username=`admin`, password=`admin123`
- **Proctor**: username=`proctor1`, password=`pass123`
- **Owner**: username=`owner`, password=`owner123`
- **Student**: username=`student1`, password=`pass123`

## Project Structure

```
src/main/java/dorm/
├── dao/                    # Data Access Objects (interfaces + CSV implementations)
│   ├── *Repository.java    # DAO interfaces (ISP, DIP)
│   ├── Csv*.java           # CSV file implementations
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
│   ├── CsvHelper.java      # CSV file operations utility
│   └── Validation.java
└── App.java                # Main application entry point

data/                       # CSV data files
├── users.csv
├── students.csv
├── applications.csv
├── announcements.csv
├── messages.csv
└── building_assignments.csv
```

## File I/O Operations

1. **CSV File I/O** - All data persisted to CSV files
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
