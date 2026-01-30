# Dormitory Management System

Simple JavaFX + MySQL app for dormitory allocation management.

## Setup

1. Create MySQL database:
```sql
CREATE DATABASE dormdb;
```

2. Load schema:
```bash
mysql -u root dormdb < src/main/resources/sql/schema_mysql.sql
```

3. Run app:
```bash
mvn clean javafx:run
```

## Login Credentials

- Admin: `admin` / `admin123`
- Proctor: `proctor1` / `pass123`
- Student: `student1` / `pass123`

## Project Structure

- `model/` - Data classes (User, Student, DormApplication, etc.)
- `dao/` - Database access layer
- `service/` - Business logic
- `ui/` - JavaFX controllers and views
- `util/` - Helpers (DB connection, password hashing)
