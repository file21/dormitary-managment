@echo off
REM Simple run script for Dormitory Management System (Windows)
REM Requires: JDK 21+ with JavaFX

echo Starting Dormitory Management System...

REM Set JavaFX path (update this to your JavaFX installation path)
set JAVAFX_PATH=C:\javafx-sdk-21\lib

REM Check if JavaFX path exists
if not exist "%JAVAFX_PATH%" (
    echo JavaFX not found at %JAVAFX_PATH%
    echo Please download JavaFX from https://openjfx.io/ and update JAVAFX_PATH in this script
    exit /b 1
)

REM Check if compiled classes exist
if not exist out (
    echo No compiled classes found. Run compile.bat first
    exit /b 1
)

REM Run the application
java --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp out ^
     dorm.App
