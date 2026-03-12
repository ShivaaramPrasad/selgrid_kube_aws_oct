@echo off
REM ============================================================================
REM Kubernetes Selenium Grid Status
REM ============================================================================
REM This script displays the current status of the Kubernetes Selenium Grid
REM ============================================================================

echo.
echo ============================================================================
echo Kubernetes Selenium Grid Status
echo ============================================================================
echo.

echo [1] Pod Status:
echo ----
kubectl get pods
echo.

echo [2] Deployment Status:
echo ----
kubectl get deployments
echo.

echo [3] Service Status:
echo ----
kubectl get services
echo.

echo [4] Hub Pod Details (if running):
echo ----
for /f "tokens=1" %%a in ('kubectl get pods -l app=selenium-hub --no-headers 2^>nul ^| awk "{print $1}"') do (
    echo Getting logs for %%a...
    kubectl logs %%a --tail=20
    echo.
)

echo [5] Node Pod Details (if running):
echo ----
for /f "tokens=1" %%a in ('kubectl get pods -l app=selenium-node-chrome --no-headers 2^>nul ^| awk "{print $1}"') do (
    echo Getting logs for %%a...
    kubectl logs %%a --tail=10
    echo.
)

echo ============================================================================
echo Port-Forward Status:
echo ----
netstat -ano ^| find ":4444" >nul
if errorlevel 1 (
    echo Port 4444 is NOT forwarded. Run in a separate terminal:
    echo   kubectl port-forward svc/selenium-hub-service 4444:4444
) else (
    echo Port 4444 is forwarded and listening.
)
echo.

echo ============================================================================
echo Useful Commands:
echo ----
echo Start Grid:            start-kubectl-grid.bat
echo Stop Grid:             stop-kubectl-grid.bat
echo View Status:           kubectl get pods
echo View Hub Logs:         kubectl logs -l app=selenium-hub -f
echo View Node Logs:        kubectl logs -l app=selenium-node-chrome -f
echo Port Forward:          kubectl port-forward svc/selenium-hub-service 4444:4444
echo Access Grid UI:        http://localhost:4444
echo Run Tests:             mvn clean test
echo ============================================================================
echo.
pause
