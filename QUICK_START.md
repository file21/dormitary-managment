# Dormitory Management System - Quick Start Guide

## Prerequisites

- **Java JDK 21+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)

---

## 1. Database Setup

### Step 1: Create Database Schema

```bash
# Connect to MySQL
mysql -u root -p

# Run schema script
source src/main/resources/sql/schema_mysql.sql

# Load test data (optional but recommended)
source src/main/resources/sql/test_data.sql

# Verify
SELECT * FROM app_user;
SELECT * FROM student_profile;
```

### Step 2: Verify Connection

```bash
mysql -u root -p dormdb -e "SELECT COUNT(*) FROM app_user;"
```

Expected output: Should show user count (minimum 2 if schema only, 8+ with test data)

---

## 2. Project Setup

### Step 1: Clone/Extract Project

```bash
cd dormitory-management
```

### Step 2: Update Database Configuration

**Option A: Environment Variables**
```bash
export DB_URL="jdbc:mysql://localhost:3306/dormdb?serverTimezone=UTC&useSSL=false"
export DB_USER="root"
export DB_PASS="your_mysql_password"
```

**Option B: Edit Db.java**
```java
// src/main/java/edu/aau/dorm/util/Db.java
cfg.setJdbcUrl("jdbc:mysql://localhost:3306/dormdb?serverTimezone=UTC&useSSL=false");
cfg.setUsername("root");
cfg.setPassword("your_mysql_password");
```

### Step 3: Build Project

```bash
mvn clean install
```

Expected: BUILD SUCCESS

---

## 3. Running the Application

### Option A: Maven (Recommended)

```bash
mvn javafx:run
```

### Option B: Command Line

```bash
# Build
mvn clean package

# Run
java -Djavafx.platform=linux --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp target/dorm-management-1.0.0.jar edu.aau.dorm.App
```

### Option C: IDE (IntelliJ)

1. Open project in IntelliJ IDEA
2. Configure JavaFX SDK in Project Settings
3. Run `App.java` as JavaFX application

---

## 4. Login Credentials

### Admin Account
- **Username**: `admin1`
- **Password**: (bcrypt hashed - use test data)

### Proctor Account
- **Username**: `proctor_main_male`
- **Password**: (bcrypt hashed - use test data)

### Student Account
- **Username**: `student001`
- **Password**: (bcrypt hashed - use test data)

> **Note**: To authenticate, passwords need to be implemented in LoginController. Currently, authentication is a placeholder.

---

## 5. Troubleshooting

### Issue: "No database connection"

**Solution**:
```bash
# Check MySQL is running
mysql -u root -p -e "SELECT VERSION();"

# Verify database exists
mysql -u root -p -e "SHOW DATABASES LIKE 'dormdb';"

# Check connection settings
grep "setJdbcUrl\|setUsername\|setPassword" src/main/java/edu/aau/dorm/util/Db.java
```

### Issue: "Module javafx not found"

**Solution**:
```bash
# Download JavaFX SDK
# Set JAVAFX_HOME environment variable
export JAVAFX_HOME=/path/to/javafx-sdk-25.0.2

# Or configure in IDE:
# Project Settings → Libraries → + JavaFX SDK
```

### Issue: "Connection pool initialization failed"

**Solution**:
```bash
# Verify MySQL credentials
mysql -u root -p dormdb -e "SELECT 1;"

# Check DB_URL environment variable
echo $DB_URL

# Increase HikariCP pool size in Db.java
cfg.setMaximumPoolSize(20);
```

---

## 6. Understanding the UI

### Admin Dashboard
1. **Applications Tab**: Review pending applications
   - Select application
   - Click "Review" to view details
   - Choose action: Approve, Reject, or Request Revision
   
2. **Assignment Tab**: Assign dormitories
   - Select approved student
   - Choose dormitory and bed
   - Enter room number
   - Click "Assign"

3. **News Tab**: Send publications
   - Enter title and message
   - Click "Publish"
   - All students notified

### Proctor Dashboard
1. **Check-in Tab**: Process check-ins
   - Select student
   - Verify documents
   - Assign bed and room
   - Click "Check In"

2. **Withdrawal Tab**: Process withdrawals
   - Select checked-in student
   - Enter reason
   - Click "Withdraw"

3. **Capacity Tab**: Manage capacity
   - Adjust total/male/female beds
   - Click "Update Capacity"

4. **Report Tab**: View statistics
   - Total allocated, checked in, withdrawn
   - Real-time occupancy

### Student Dashboard
1. **Available Housing**: Browse options
   - Filter by campus, gender, occupancy
   - View bed availability

2. **Application**: Submit housing request
   - Create new application
   - Update form details
   - Submit for review
   - Track status

3. **Status**: Monitor progress
   - Visual timeline
   - Current status message
   - Estimated processing time

---

## 7. Database Queries

### View All Applications

```sql
SELECT id, student_user_id, status, score, submitted_at 
FROM dorm_application 
ORDER BY submitted_at DESC;
```

### Check Block Occupancy

```sql
SELECT 
    b.block_code,
    c.name as campus,
    b.total_beds,
    (b.total_beds - b.available_beds) as occupied,
    CONCAT(ROUND(((b.total_beds - b.available_beds) * 100 / b.total_beds), 1), '%') as occupancy
FROM block b
JOIN campus c ON b.campus_id = c.id
ORDER BY occupancy DESC;
```

### View Student Applications

```sql
SELECT 
    sp.full_name,
    da.status,
    da.sponsorship,
    da.score,
    da.submitted_at
FROM dorm_application da
JOIN student_profile sp ON da.student_user_id = sp.user_id
WHERE da.status IN ('SUBMITTED', 'UNDER_REVIEW')
ORDER BY da.score DESC;
```

### Audit Trail

```sql
SELECT 
    u.username,
    al.action,
    al.entity_type,
    al.entity_id,
    al.created_at
FROM audit_log al
LEFT JOIN app_user u ON al.actor_user_id = u.id
ORDER BY al.created_at DESC
LIMIT 20;
```

---

## 8. Project Structure Overview

```
dormitory-management/
├── src/main/java/edu/aau/dorm/
│   ├── App.java                           # Entry point
│   ├── model/                             # Domain entities
│   ├── dao/                               # Data access
│   ├── service/                           # Business logic
│   │   ├── AdminService.java              # Admin operations
│   │   ├── ProctorManagementService.java  # Proctor operations
│   │   └── StudentHousingService.java     # Student operations
│   ├── ui/controller/                     # UI Controllers
│   │   ├── AdminDashboardController.java
│   │   ├── ProctorDashboardController.java
│   │   └── StudentDashboardController.java
│   └── util/                              # Utilities
│       ├── Db.java                        # Connection pool
│       ├── InputValidator.java            # Validation
│       └── PasswordHasher.java            # Security
├── src/main/resources/
│   ├── sql/
│   │   ├── schema_mysql.sql               # Database schema
│   │   └── test_data.sql                  # Test data
│   └── ui/
│       └── *.fxml                         # UI layouts
├── pom.xml                                # Maven configuration
└── *.md                                   # Documentation
```

---

## 9. Key Classes

| Class | Purpose |
|-------|---------|
| `App.java` | Application entry point |
| `AdminService` | Admin business logic |
| `ProctorManagementService` | Proctor business logic |
| `StudentHousingService` | Student business logic |
| `AdminDashboardController` | Admin UI |
| `ProctorDashboardController` | Proctor UI |
| `StudentDashboardController` | Student UI |
| `InputValidator` | Input validation |
| `Db.java` | Database connection |

---

## 10. Development Tips

### Add New Feature

1. **Create Service Method** in appropriate service class
   - Follow existing patterns
   - Add input validation
   - Handle exceptions

2. **Add Controller Handler** in corresponding controller
   - Wire service method
   - Update UI with results
   - Show user feedback

3. **Update Database** if needed
   - Add tables/fields to schema
   - Create migration script
   - Update DAOs

### Debug Issues

```bash
# Enable detailed logging
mvn clean javafx:run -X

# Check database directly
mysql -u root -p dormdb

# View application logs
tail -f logs/application.log
```

### Performance Tips

- Use connection pooling (already configured)
- Add database indexes for queries
- Implement pagination for large lists
- Cache frequently accessed data

---

## 11. Deployment Checklist

- [ ] Database configured and tested
- [ ] Application builds successfully
- [ ] All three dashboards work
- [ ] Test data loads correctly
- [ ] Error handling works
- [ ] Validation messages display
- [ ] Notifications send properly
- [ ] Audit logs record actions
- [ ] Performance acceptable
- [ ] Security measures in place

---

## 12. Support & Help

### Documentation
- **SYSTEM_DOCUMENTATION.md** - Detailed architecture and design
- **PROJECT_SUMMARY.md** - Complete feature overview
- Source code comments - Implementation details

### Common Issues
1. Database connection → Check Db.java configuration
2. JavaFX error → Verify JavaFX SDK path
3. Build failure → Run `mvn clean install`
4. UI not loading → Check FXML files in resources

### Next Steps
1. Implement authentication in LoginController
2. Add more features (email notifications, reports)
3. Deploy to production MySQL
4. Create user documentation
5. Set up backup and monitoring

---

## License

Addis Ababa University - Dormitory Management System
Created for academic and administrative purposes.

---

**Happy Coding!** 🚀

Questions? Refer to the comprehensive documentation files or review the source code comments.
