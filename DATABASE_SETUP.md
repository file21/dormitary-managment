# MySQL Database Setup

## Simple Setup Instructions

This application uses MySQL database. Follow these easy steps to set it up.

## Database Configuration

The application connects to MySQL using these settings (configured in `src/main/java/dorm/util/Db.java`):

- **Host**: 127.0.0.1:3310
- **Database**: dormdb
- **Username**: root
- **Password**: 30MB6-I67J4-3DN0T-L609U

You can override these with environment variables:
- `DB_URL` - full JDBC URL
- `DB_USER` - database username
- `DB_PASS` - database password

## Initialize Database

### Step 1: Make sure MySQL is running

Your MySQL server should be running on `127.0.0.1:3310`

### Step 2: Run the schema script

Execute the schema file to create the database and tables:

```bash
mysql -h 127.0.0.1 -P 3310 -u root -p30MB6-I67J4-3DN0T-L609U < src/main/resources/sql/schema.sql
```

Or if you prefer, copy the contents of `src/main/resources/sql/schema.sql` and run it in your MySQL client.

### Step 3: Verify the setup

The schema creates:
1. Database named `dormdb`
2. 7 simple tables:
   - `users` - All system users (admin, proctor, student, owner)
   - `students` - Student profile information
   - `applications` - Dorm applications
   - `announcements` - System announcements
   - `messages` - User messaging
   - `building_assignments` - Proctor assignments
   - `document_paths` - File storage paths

3. Sample test data:
   - Admin user: `admin / admin123`
   - Proctor user: `proctor1 / proctor123`
   - Owner user: `owner / owner123`
   - Student user: `student1 / student123`

## Database Structure

All tables use simple MySQL data types:
- `VARCHAR` for text
- `TEXT` for long text
- `INT` for numbers
- `TIMESTAMP` for dates/times
- `DATE` for dates only

Foreign keys ensure data integrity:
- Students link to users
- Applications link to users
- Building assignments link to users

That's it! The database is ready to use with the JavaFX application.
