# Dormitory Management System

A JavaFX desktop application for managing dormitory applications, assignments, and resident tracking with CSV file-based data persistence.

## Features

### For Students
- Account registration and login
- Submit dormitory applications
- View application status and admin notes
- Send/receive messages
- Upload required documents

### For Admins
- Review and update application statuses
- Post announcements
- Message students
- Assign buildings to approved students
- Export student data to CSV

### For Proctors
- View students assigned to their building
- Register student entry/withdrawal dates
- Message students

### For Owners
- All admin capabilities
- Add and remove admin and proctor accounts
- Assign buildings to proctors

## Requirements

- **Java JDK 21** or later
- **JavaFX 21** (OpenJFX)

## Installation

### Step 1: Install Java JDK 21

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install openjdk-21-jdk
```

**Windows/macOS:**
Download from https://adoptium.net/

### Step 2: Install JavaFX

**Ubuntu/Debian:**
```bash
sudo apt-get install openjfx
```

**Windows/macOS:**
1. Download JavaFX SDK from https://openjfx.io/
2. Extract to a folder (e.g., `C:\javafx-sdk-21` on Windows)
3. Update the `JAVAFX_PATH` in compile.sh/compile.bat

## How to Compile and Run

### Linux/macOS

```bash
# Make scripts executable (first time only)
chmod +x compile.sh run.sh

# Compile
./compile.sh

# Run
./run.sh
```

### Windows

```cmd
# Compile
compile.bat

# Run
run.bat
```

## Default Login Credentials

| Role    | Username  | Password  |
|---------|-----------|-----------|
| Admin   | admin     | admin123  |
| Proctor | proctor1  | pass123   |
| Owner   | owner     | owner123  |
| Student | student1  | pass123   |

## Project Structure

```
src/main/java/dorm/
├── App.java                 # Main entry point
├── dao/                     # Data Access Objects
│   ├── *Repository.java     # Interfaces
│   └── Csv*.java            # CSV implementations
├── model/                   # Data models
│   ├── User.java
│   ├── Student.java
│   └── ...
├── service/                 # Business logic
│   └── DatabaseDormService.java
├── ui/                      # JavaFX UI classes
│   ├── LoginViewDb.java
│   ├── StudentDashboardDb.java
│   ├── AdminDashboardDb.java
│   ├── ProctorDashboardDb.java
│   └── OwnerDashboardDb.java
└── util/                    # Utilities
    ├── CsvHelper.java
    └── Validation.java

data/                        # CSV data files (auto-created)
├── users.csv
├── students.csv
├── applications.csv
├── announcements.csv
├── messages.csv
└── building_assignments.csv
```

## Data Storage

All data is stored in CSV files in the `data/` directory. The directory and files are created automatically when the application runs.

## Troubleshooting

### "JavaFX not found" error
- Make sure JavaFX is installed
- Update `JAVAFX_PATH` in compile.sh/run.sh (or .bat files) to point to your JavaFX lib folder

### Common JavaFX paths:
- **Ubuntu/Debian:** `/usr/share/openjfx/lib`
- **macOS (Homebrew):** `/opt/homebrew/opt/openjfx/libexec/lib`
- **Windows:** `C:\javafx-sdk-21\lib`

## License

Educational project for AAU coursework.
