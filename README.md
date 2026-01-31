# Dormitory Management System

A JavaFX desktop application for managing dormitory applications and assignments with CSV file-based data persistence.

## Features

### For Students
- Account registration and login
- Two-phase application system:
  - **Phase One**: Sponsorship type, residency, address (city, subcity, woreda)
  - **Phase Two**: Mother's info, emergency contact, transaction ID (unlocked after Phase One approval)
- View application status

### For Admins
- Review applications with checkbox selection
- Bulk approve/decline/request resubmit
- Assign buildings to approved students
- Export selected students to CSV
- Post announcements
- Message students

### For Owners
- All admin capabilities
- Manage admin staff accounts

## Application Flow

1. **Registration**: Students create account with name, ID, gender, college (8+ char password)
2. **Phase One**: Fill sponsorship, residency, address info
3. **Admin Review**: Approve, decline, or request resubmit
4. **Phase Two**: (After Phase One approval) Fill mother's info, emergency contact, transaction ID
5. **Building Assignment**: Admin assigns building to approved students

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
2. Extract to a folder
3. Update `JAVAFX_PATH` in compile.sh/compile.bat

## How to Compile and Run

### Linux/macOS

```bash
chmod +x compile.sh run.sh
./compile.sh
./run.sh
```

### Windows

```cmd
compile.bat
run.bat
```

## Default Login Credentials

| Role    | Username | Password  |
|---------|----------|-----------|
| Admin   | admin    | admin123  |
| Owner   | owner    | owner123  |
| Student | student1 | pass1234  |

**Note:** New student passwords must be at least 8 characters.

## Project Structure

```
src/main/java/dorm/
├── App.java                 # Main entry point
├── dao/                     # Data Access Objects
├── model/                   # Data models
│   ├── Student.java
│   ├── Gender.java
│   ├── College.java         # 9 AAU colleges with full name and acronym
│   ├── Residency.java       # ADDIS_ABABA, SHEGER_CITY, REGIONAL
│   ├── SponsorshipType.java # GOVERNMENT, SELF_SPONSORED
│   └── ...
├── service/
│   └── DatabaseDormService.java
├── ui/
│   ├── LoginViewDb.java
│   ├── StudentDashboardDb.java
│   ├── AdminDashboardDb.java
│   └── OwnerDashboardDb.java
└── util/
    └── CsvHelper.java

data/                        # CSV data files
├── users.csv
├── students.csv
├── applications.csv
├── announcements.csv
└── messages.csv
```

## Troubleshooting

### JavaFX paths:
- **Ubuntu/Debian:** `/usr/share/openjfx/lib`
- **macOS (Homebrew):** `/opt/homebrew/opt/openjfx/libexec/lib`
- **Windows:** `C:\javafx-sdk-21\lib`

## License

Educational project for AAU coursework.
