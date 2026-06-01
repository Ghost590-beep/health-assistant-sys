@echo off
title HealthAssist - Launching...

echo ============================================
echo    HEALTH ASSISTANCE SYSTEM
echo ============================================
echo.

echo [1/2] Starting Backend Server on port 8080...
start "HealthAssist Backend" cmd /k "%~dp0start_backend.bat"

echo Waiting for backend to initialize (15 seconds)...
timeout /t 15 /nobreak >nul

echo [2/2] Starting Frontend...
start "HealthAssist Frontend" cmd /k "%~dp0start_frontend.bat"

echo.
echo ============================================
echo    SYSTEM STARTED!
echo.
echo    Backend:  http://localhost:8080
echo    Swagger:  http://localhost:8080/swagger-ui.html
echo ============================================
pause