@echo off
REM ============================================================================
REM Kubernetes Port Forward to Selenium Hub
REM ============================================================================
REM This script establishes port-forward connection from localhost:4444
REM to the Selenium Hub Service in Kubernetes
REM Keep this terminal open while running tests
REM ============================================================================

echo.
echo ============================================================================
echo Kubernetes Port Forward Setup
echo ============================================================================
echo.

REM Set working directory to current script location (kubectl folder)
cd /d "%~dp0"

echo Checking if Kubernetes is running...
kubectl cluster-info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Kubernetes cluster is not running or not accessible
    echo Please ensure Docker Desktop Kubernetes is running
    pause
    exit /b 1
)

echo ✓ Kubernetes is running
echo.

echo Checking if Selenium Hub Service exists...
kubectl get service selenium-hub-service >nul 2>&1
if errorlevel 1 (
    echo ERROR: Selenium Hub Service not found
    echo Please run start-kubectl-grid.bat first
    pause
    exit /b 1
)

echo ✓ Selenium Hub Service found
echo.

echo ============================================================================
echo Starting Port Forward: localhost:4444 ^-^> svc/selenium-hub-service:4444
echo ============================================================================
echo.
echo This terminal will remain open for the port-forward connection.
echo While this is running, you can:
echo   1. Access Grid UI: http://localhost:4444
echo   2. Run tests: mvn clean test (in another terminal)
echo.
echo To stop port-forward: Press Ctrl+C
echo ============================================================================
echo.

kubectl port-forward svc/selenium-hub-service 4444:4444

echo.
echo Port forward stopped.
echo.
pause
