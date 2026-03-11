@echo off
echo =====================================
echo Stopping Selenium Grid...
echo =====================================
echo.

REM Check if Docker is running
docker ps >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running.
    pause
    exit /b 1
)

REM Stop containers
echo [INFO] Stopping all containers...
docker compose -f docker-compose-v3.yml down

if errorlevel 1 (
    echo [ERROR] Failed to stop containers!
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Grid Stopped Successfully!
echo.
echo Tip: To remove all containers and volumes, run:
  docker compose -f docker-compose-v3.yml down -v
echo.

pause