@echo off
title HealthAssist - Launcher

echo ============================================
echo    HEALTH ASSISTANCE SYSTEM
echo ============================================
echo.

:: Find JavaFX
if exist "C:\javafx-sdk-26\lib" (
    set JAVAFX=C:\javafx-sdk-26\lib
) else if exist "C:\javafx-sdk\lib" (
    set JAVAFX=C:\javafx-sdk\lib
) else (
    echo WARNING: JavaFX SDK not found at C:\javafx-sdk-26\lib
    echo Please download JavaFX SDK 26 from https://gluonhq.com/products/javafx/
    echo and extract to C:\javafx-sdk-26
    echo.
)

:: Set Java
set JAVA_HOME=C:\Program Files\Java\jdk-22
if not exist "%JAVA_HOME%" set JAVA_HOME=C:\Program Files\Java\jdk-25
if not exist "%JAVA_HOME%" (
    echo Java JDK not found! Please install JDK 22+
    pause
    exit
)

echo JAVA_HOME: %JAVA_HOME%
echo.

:: Start Backend
echo Starting Backend Server...
cd /d "%~dp0backend"
start "HealthAssist Backend" cmd /k "set JAVA_HOME=%JAVA_HOME% && mvnw.cmd spring-boot:run"

echo Backend starting on http://localhost:8080
echo.

:: Wait then start Frontend
timeout /t 12 /nobreak >nul

echo Starting Frontend...
cd /d "%~dp0frontend"
start "HealthAssist Frontend" cmd /k "set JAVA_HOME=%JAVA_HOME% && mvnw.cmd javafx:run"

echo.
echo ============================================
echo    SYSTEM RUNNING!
echo.
echo    Backend:  http://localhost:8080
echo    Swagger:  http://localhost:8080/swagger-ui.html
echo    Frontend: JavaFX window opening...
echo ============================================
echo.
echo Close this window. Keep the other windows open.
pause