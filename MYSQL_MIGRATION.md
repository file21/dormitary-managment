# MySQL Database Migration Summary

## Changes Made

This document summarizes the complete migration to MySQL database structure.

### 1. Database Configuration Updated

**File**: `src/main/java/dorm/util/Db.java`

- Updated default MySQL connection to: `127.0.0.1:3310`
- Default credentials: `root / 30MB6-I67J4-3DN0T-L609U`
- Uses HikariCP connection pooling
- Can be overridden with environment variables: `DB_URL`, `DB_USER`, `DB_PASS`

### 2. Schema Simplified

**Files Changed**:
- Removed: `schema.sql` (PostgreSQL schema - not needed)
- Removed: `schema_mysql.sql` (complex schema - not needed)
- Removed: `test_data.sql` (complex test data - not needed)
- Kept: `schema.sql` (renamed from `simple_schema.sql`)

**Final Schema** (`src/main/resources/sql/schema.sql`):

Simple MySQL database with 7 tables:

1. **users** - All system users (id, username, password, role, display_name)
2. **students** - Student profiles (user_id, student_id, city, sponsorship_type, etc.)
3. **applications** - Dorm applications (id, student_id, status, admin_note)
4. **announcements** - System announcements (id, title, body, created_by)
5. **messages** - User messaging (id, from_user, to_user, content)
6. **building_assignments** - Proctor-to-building mapping (id, proctor_id, building_name)
7. **document_paths** - File storage paths (id, student_id, file_path, file_type)

### 3. Repository Pattern (Already MySQL-Ready)

All repository implementations use MySQL via JDBC:

- `MySqlUserRepository` ✓
- `MySqlStudentRepository` ✓
- `MySqlApplicationRepository` ✓
- `MySqlAnnouncementRepository` ✓
- `MySqlMessageRepository` ✓
- `MySqlBuildingAssignmentRepository` ✓

### 4. Setup Documentation

**New Files Created**:

- `DATABASE_SETUP.md` - Detailed database setup instructions
- `init-db.sh` - Automated database initialization script

**Updated Files**:

- `README.md` - Updated setup instructions to reference MySQL

### 5. Dependencies (Already Configured)

`pom.xml` already includes:
- MySQL Connector/J 8.0.33
- HikariCP 5.1.0 (connection pooling)
- JavaFX 21
- SLF4J (logging)

## Testing the Setup

### Option 1: Using the initialization script

```bash
./init-db.sh
```

### Option 2: Manual initialization

```bash
mysql -h 127.0.0.1 -P 3310 -u root -p30MB6-I67J4-3DN0T-L609U < src/main/resources/sql/schema.sql
```

### Option 3: Run from MySQL client

```sql
SOURCE /path/to/workspace/src/main/resources/sql/schema.sql;
```

## What Works

✓ All tables use simple MySQL data types (VARCHAR, TEXT, INT, TIMESTAMP, DATE)
✓ All foreign keys properly defined with CASCADE delete
✓ Indexes on frequently queried columns
✓ Sample test data included in schema
✓ Connection pooling via HikariCP
✓ All repositories use standard JDBC/MySQL syntax
✓ No PostgreSQL-specific syntax remaining

## Default Test Users

After running the schema:

- **Admin**: `admin / admin123`
- **Proctor**: `proctor1 / proctor123`
- **Owner**: `owner / owner123`
- **Student**: `student1 / student123`

## Notes for Junior Developers

- All SQL is basic and straightforward
- No advanced MySQL features used
- Standard JDBC PreparedStatement for all queries
- Foreign keys ensure data integrity automatically
- Timestamps handled by MySQL DEFAULT CURRENT_TIMESTAMP
- All tables use InnoDB engine (supports transactions and foreign keys)
- UTF8MB4 charset (supports all Unicode characters including emojis)

## Migration Complete

The application is now fully configured for MySQL and ready to use!
