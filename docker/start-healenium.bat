@echo off
REM ============================================================
REM Start Healenium Backend Services
REM ============================================================
REM Starts PostgreSQL, Healenium Backend, and Selector Imitator
REM Required for Healenium self-healing to work during tests
REM ============================================================

echo.
echo ============================================
echo   Starting Healenium Backend Services
echo ============================================
echo.

cd /d "%~dp0"

echo [1/3] Checking Docker...
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

echo [2/3] Starting Healenium services...
docker-compose -f docker-compose-healenium.yml up -d

echo.
echo [3/3] Waiting for services to be ready...
timeout /t 15 /nobreak >nul

echo.
echo Checking service status...
docker-compose -f docker-compose-healenium.yml ps

echo.
echo ============================================
echo   Healenium Backend Services Started!
echo ============================================
echo   PostgreSQL:          localhost:5432
echo   Healenium Backend:   localhost:7878
echo   Selector Imitator:   localhost:8000
echo ============================================
echo.
echo You can now run your Selenium tests with self-healing enabled.
echo.
pause
