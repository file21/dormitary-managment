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

- Admin: `admin1` / `admin123`
- Proctor: `proctor_main_male` / `pass123`
- Student: `student001` / `pass123`

## Project Structure

- `model/` - Data classes (User, Student, DormApplication, etc.)
- `dao/` - Database access layer
- `service/` - Business logic
- `ui/` - JavaFX controllers and views
- `util/` - Helpers (DB connection, password hashing)
