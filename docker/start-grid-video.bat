@echo off
setlocal enabledelayedexpansion
echo =====================================
echo Starting Selenium Grid with Video Recording...
echo =====================================
echo.

REM Check if Docker is running
docker ps >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

REM Check if compose file exists
if not exist docker-compose-v3.yml (
    echo [ERROR] docker-compose-v3.yml not found!
    pause
    exit /b 1
)

REM Create video directory if it doesn't exist
if not exist testcaseVideos (
    echo [INFO] Creating testcaseVideos directory...
    mkdir testcaseVideos
)

REM Start containers with video profile
echo [INFO] Starting containers with video recording...
docker compose --profile video -f docker-compose-v3.yml up -d

if errorlevel 1 (
    echo [ERROR] Failed to start containers!
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Grid Started with Video Recording!
echo.
echo Services:
  - Selenium Grid: http://localhost:4444
  - File Browser: http://localhost:8081
echo.
echo Video Recording:
  - Videos will be saved to: testcaseVideos/
  - Access via File Browser at http://localhost:8081
echo.
echo Waiting for grid to be ready (30 seconds)...
timeout /t 5 /nobreak

pause
