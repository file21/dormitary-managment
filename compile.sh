#!/bin/bash

# Simple compile script for Dormitory Management System
# Requires: JDK 21+ with JavaFX

echo "Compiling Dormitory Management System..."

# Create output directory
mkdir -p out

# Find JavaFX path (adjust this path based on your JavaFX installation)
# Common locations:
#   Linux: /usr/share/openjfx/lib
#   macOS: /opt/homebrew/opt/openjfx/libexec/lib
#   Windows: C:\javafx-sdk-21\lib

JAVAFX_PATH="/usr/share/openjfx/lib"

# Check if JavaFX path exists
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "JavaFX not found at $JAVAFX_PATH"
    echo "Please install JavaFX or update JAVAFX_PATH in this script"
    echo ""
    echo "To install on Ubuntu/Debian:"
    echo "  sudo apt-get install openjfx"
    echo ""
    exit 1
fi

# Compile all Java files
javac --module-path "$JAVAFX_PATH" \
      --add-modules javafx.controls,javafx.fxml \
      -d out \
      src/main/java/dorm/*.java \
      src/main/java/dorm/dao/*.java \
      src/main/java/dorm/model/*.java \
      src/main/java/dorm/service/*.java \
      src/main/java/dorm/ui/*.java \
      src/main/java/dorm/util/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Run with: ./run.sh"
else
    echo "Compilation failed!"
    exit 1
fi
