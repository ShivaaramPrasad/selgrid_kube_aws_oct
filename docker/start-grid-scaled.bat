@echo off
setlocal enabledelayedexpansion
echo =====================================
echo Starting Scaled Selenium Grid...
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

REM Display scaling options
echo Scaling Configuration:
echo ===================================
echo 1. 3 Chrome instances (default)
echo 2. 2 Chrome + 2 Edge instances
echo 3. 2 Chrome + 2 Edge + 2 Firefox (custom)
echo 4. Enter custom configuration
echo.

set /p choice="Select configuration (1-4): "

if "%choice%"=="1" (
    echo [INFO] Starting with 3 Chrome instances...
    docker compose -f docker-compose-v3.yml up --scale chrome=3 -d
) else if "%choice%"=="2" (
    echo [INFO] Starting with 2 Chrome + 2 Edge instances...
    docker compose -f docker-compose-v3.yml up --scale chrome=2 --scale edge=2 -d
) else if "%choice%"=="3" (
    echo [INFO] Starting with 2 Chrome + 2 Edge + 2 Firefox instances...
    docker compose -f docker-compose-v3.yml up --scale chrome=2 --scale edge=2 --scale firefox=2 -d
) else if "%choice%"=="4" (
    set /p chrome="Number of Chrome instances (default 1): "
    set /p edge="Number of Edge instances (default 1): "
    set /p firefox="Number of Firefox instances (default 1): "
    
    if not defined chrome set chrome=1
    if not defined edge set edge=1
    if not defined firefox set firefox=1
    
    echo [INFO] Starting with custom configuration...
    echo   - Chrome: !chrome! instances
    echo   - Edge: !edge! instances
    echo   - Firefox: !firefox! instances
    docker compose -f docker-compose-v3.yml up --scale chrome=!chrome! --scale edge=!edge! --scale firefox=!firefox! -d
) else (
    echo [ERROR] Invalid selection!
    pause
    exit /b 1
)

if errorlevel 1 (
    echo [ERROR] Failed to start containers!
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Scaled Grid Started Successfully!
echo.
echo Grid Information:
  - Selenium Grid: http://localhost:4444
  - Hub Console: http://localhost:4444/ui
echo.
echo Check running instances:
  docker compose -f docker-compose-v3.yml ps
echo.
echo Tip: Maximum 5 concurrent sessions (hub limit)
echo.

pause
