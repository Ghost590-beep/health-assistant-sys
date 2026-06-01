@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-25
if not exist "%JAVA_HOME%" set JAVA_HOME=C:\Program Files\Java\jdk-22

cd /d "%~dp0frontend"

echo Starting Frontend Application...
echo.

..\backend\mvnw.cmd javafx:run

pause