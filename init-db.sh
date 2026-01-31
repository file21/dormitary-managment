#!/bin/bash
# Simple database initialization script for MySQL

echo "=========================================="
echo "Dormitory Management System - Database Setup"
echo "=========================================="
echo ""

# Database connection settings
DB_HOST="127.0.0.1"
DB_PORT="3310"
DB_USER="root"
DB_PASS="30MB6-I67J4-3DN0T-L609U"
SCHEMA_FILE="src/main/resources/sql/schema.sql"

echo "Connecting to MySQL server at ${DB_HOST}:${DB_PORT}..."
echo ""

# Check if schema file exists
if [ ! -f "$SCHEMA_FILE" ]; then
    echo "Error: Schema file not found at $SCHEMA_FILE"
    exit 1
fi

# Execute the schema
echo "Creating database and tables..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" < "$SCHEMA_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✓ Database initialized successfully!"
    echo "=========================================="
    echo ""
    echo "Database: dormdb"
    echo "Tables created:"
    echo "  - users"
    echo "  - students"
    echo "  - applications"
    echo "  - announcements"
    echo "  - messages"
    echo "  - building_assignments"
    echo "  - document_paths"
    echo ""
    echo "Default test users created:"
    echo "  - admin / admin123 (ADMIN)"
    echo "  - proctor1 / proctor123 (PROCTOR)"
    echo "  - owner / owner123 (OWNER)"
    echo "  - student1 / student123 (STUDENT)"
    echo ""
    echo "You can now run the JavaFX application!"
else
    echo ""
    echo "=========================================="
    echo "✗ Database initialization failed!"
    echo "=========================================="
    echo "Please check your MySQL connection settings."
    exit 1
fi
