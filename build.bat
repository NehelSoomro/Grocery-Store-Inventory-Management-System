@echo off
setlocal
REM Compile and launch the GUI (Windows)
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\inventory\*.java
if errorlevel 1 (
    echo.
    echo Build failed. Check the compiler errors above.
    pause
    exit /b 1
)
start "Grocery Store Inventory" javaw -cp out inventory.Main
if errorlevel 1 (
    echo Could not launch the application. Make sure Java is installed and available on PATH.
    pause
    exit /b 1
)
echo Application launched successfully.
exit /b 0
