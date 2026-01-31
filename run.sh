#!/bin/bash

# Simple run script for Dormitory Management System
# Requires: JDK 21+ with JavaFX

echo "Starting Dormitory Management System..."

# Find JavaFX path (adjust this path based on your JavaFX installation)
JAVAFX_PATH="/usr/share/openjfx/lib"

# Check if JavaFX path exists
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "JavaFX not found at $JAVAFX_PATH"
    echo "Please install JavaFX or update JAVAFX_PATH in this script"
    exit 1
fi

# Check if compiled classes exist
if [ ! -d "out" ]; then
    echo "No compiled classes found. Run ./compile.sh first"
    exit 1
fi

# Run the application
java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls,javafx.fxml \
     -cp out \
     dorm.App
