@echo off
REM ============================================================================
REM Start Kubernetes Selenium Grid
REM ============================================================================
REM This script deploys the Selenium Hub and Chrome Nodes to Kubernetes
REM ============================================================================

echo.
echo ============================================================================
echo Starting Kubernetes Selenium Grid...
echo ============================================================================
echo.

REM Set working directory to current script location (kubectl folder)
cd /d "%~dp0"

echo [Step 1/2] Applying Selenium Hub Service and Deployment...
kubectl apply -f selenium-hub-service.yaml
if errorlevel 1 (
    echo ERROR: Failed to apply Hub service/deployment
    pause
    exit /b 1
)

echo.
echo [Step 2/2] Applying Selenium Node Chrome Deployment...
kubectl apply -f selenium-node-chrome-deployment.yaml
if errorlevel 1 (
    echo ERROR: Failed to apply Node deployment
    pause
    exit /b 1
)

echo.
echo ============================================================================
echo Waiting for pods to be ready...
echo ============================================================================
echo.

REM Wait for pods to start
timeout /t 5 /nobreak

REM Check pod status
echo.
echo Pod Status:
kubectl get pods
echo.

echo Checking if pods are ready...
for /L %%i in (1,1,30) do (
    for /f "tokens=3" %%a in ('kubectl get pods ^| find "Running" ^| find /c /v ""') do (
        if %%a geq 4 (
            echo All pods are running!
            goto pods_ready
        )
    )
    echo Waiting for pods to be ready... (attempt %%i/30)
    timeout /t 2 /nobreak
)

:pods_ready
echo.
echo ============================================================================
echo Kubernetes Selenium Grid Started Successfully!
echo ============================================================================
echo.
echo Next Steps:
echo 1. Run port-forward in a separate terminal:
echo    kubectl port-forward svc/selenium-hub-service 4444:4444
echo.
echo 2. Access Grid UI:
echo    http://localhost:4444
echo.
echo 3. Run tests with Maven:
echo    mvn clean test
echo.
echo To stop grid, run: stop-kubectl-grid.bat
echo ============================================================================
echo.
pause
