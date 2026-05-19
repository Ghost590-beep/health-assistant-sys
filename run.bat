@echo off
title HealthAssist - Setup

echo ============================================
echo    HEALTH ASSISTANCE SYSTEM - SETUP
echo ============================================
echo.

:: Check Java
echo [1/3] Checking Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed!
    echo Download from: https://jdk.java.net/
    pause
    exit
)
echo Java found!

:: Check MySQL
echo.
echo [2/3] Setting up database...
echo Please enter your MySQL root password:
set /p mysqlpass=

mysql -u root -p%mysqlpass% -e "source database\schema.sql; source database\seed.sql;" 2>nul
if %errorlevel% neq 0 (
    echo Database setup may have failed. Run schema.sql and seed.sql manually in MySQL Workbench.
)

:: Copy .env to backend
echo.
echo [3/3] Configuring backend...
echo DB_HOST=localhost> backend\.env
echo DB_PORT=3306>> backend\.env
echo DB_NAME=health_assistance_system>> backend\.env
echo DB_USERNAME=root>> backend\.env
echo DB_PASSWORD=%mysqlpass%>> backend\.env
echo JWT_SECRET=HealthAssistSystem2024SuperSecretKeyForJWT!!>> backend\.env
echo JWT_EXPIRATION=86400000>> backend\.env
echo SERVER_PORT=8080>> backend\.env
echo .env created!

echo.
echo ============================================
echo    SETUP COMPLETE!
echo.
echo    Run launcher.bat to start the system
echo ============================================
pause