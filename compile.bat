@echo off
REM Simple compile script for Dormitory Management System (Windows)
REM Requires: JDK 21+ with JavaFX

echo Compiling Dormitory Management System...

REM Create output directory
if not exist out mkdir out

REM Set JavaFX path (update this to your JavaFX installation path)
set JAVAFX_PATH=C:\javafx-sdk-21\lib

REM Check if JavaFX path exists
if not exist "%JAVAFX_PATH%" (
    echo JavaFX not found at %JAVAFX_PATH%
    echo Please download JavaFX from https://openjfx.io/ and update JAVAFX_PATH in this script
    exit /b 1
)

REM Compile all Java files
javac --module-path "%JAVAFX_PATH%" ^
      --add-modules javafx.controls,javafx.fxml ^
      -d out ^
      src\main\java\dorm\*.java ^
      src\main\java\dorm\dao\*.java ^
      src\main\java\dorm\model\*.java ^
      src\main\java\dorm\service\*.java ^
      src\main\java\dorm\ui\*.java ^
      src\main\java\dorm\util\*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Run with: run.bat
) else (
    echo Compilation failed!
    exit /b 1
)
