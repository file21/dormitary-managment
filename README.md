# Dormitory Management System

A JavaFX desktop application for managing dormitory applications, assignments, and resident tracking with CSV file-based data persistence.

## Features

### For Students
- Account registration and login
- Two-phase application system:
  - **Phase One**: All students submit initial application with documents
  - **Phase Two**: Self-sponsored students upload payment slip (4500 Birr)
- View application status and admin notes
- Send/receive messages
- Upload required documents

### For Admins
- Review and approve/decline applications (Phase One and Two)
- View uploaded documents and payment slips
- Post announcements
- Message students
- Assign buildings to approved students (with capacity tracking)
- Export student data to CSV

### For Buildings (Proctors)
- Buildings act as proctor accounts (login with building name, e.g., B501)
- View students assigned to the building
- Register student entry/withdrawal dates
- Track building occupancy in real-time
- Message students

### For Owners
- All admin capabilities
- Add and manage buildings with max capacity settings
- Update building capacity as needed
- Add and remove admin accounts
- View building occupancy statistics

## Application Flow

1. **Student Registration**: Students create an account with username, password, and basic info
2. **Phase One Application**: Students select sponsorship type (Government/Self-sponsored) and upload documents
3. **Admin Review**: Admins approve, decline, or request resubmission
4. **Phase Two (Self-sponsored only)**: After Phase One approval, self-sponsored students upload payment slip
5. **Building Assignment**: Admins assign students to buildings (capacity tracked)
6. **Entry Registration**: Building proctors register when students physically arrive
7. **Withdrawal Registration**: When students leave, proctors register withdrawal (frees up capacity)

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

| Role     | Username  | Password    |
|----------|-----------|-------------|
| Admin    | admin     | admin123    |
| Owner    | owner     | owner123    |
| Building | B501      | proctor501  |
| Building | B502      | proctor502  |
| Building | B503      | proctor503  |
| Student  | student1  | pass123     |

**Note:** Buildings act as proctor accounts. Login with the building name.

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
│   ├── Building.java        # Buildings with capacity
│   ├── Gender.java          # Enum
│   ├── SponsorshipType.java # Enum (GOVERNMENT, SELF_SPONSORED)
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
├── users.csv                # Admin and owner accounts
├── students.csv             # Student accounts and info
├── applications.csv         # Dorm applications
├── buildings.csv            # Building/proctor accounts with capacity
├── announcements.csv
└── messages.csv
```

## Data Storage

All data is stored in CSV files in the `data/` directory. The directory and files are created automatically when the application runs.

### Building Capacity

- Owner sets max capacity for each building (e.g., 100 students)
- Capacity decreases when students register entry
- Capacity increases when students withdraw
- Admins see remaining capacity when assigning buildings

## Troubleshooting

### "JavaFX not found" error
- Make sure JavaFX is installed
- Update `JAVAFX_PATH` in compile.sh/run.sh (or .bat files) to point to your JavaFX lib folder

### Common JavaFX paths:
- **Ubuntu/Debian:** `/usr/share/openjfx/lib`
- **macOS (Homebrew):** `/opt/homebrew/opt/openjfx/libexec/lib`
- **Windows:** `C:\javafx-sdk-21\lib`

### Student login doesn't work after restart
- Make sure students.csv exists in the data/ directory
- The system now properly checks students.csv for student authentication

## License

Educational project for AAU coursework.
