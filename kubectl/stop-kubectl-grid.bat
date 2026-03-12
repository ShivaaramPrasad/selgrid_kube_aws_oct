@echo off
REM ============================================================================
REM Stop Kubernetes Selenium Grid
REM ============================================================================
REM This script removes the Selenium Hub and Chrome Nodes from Kubernetes
REM ============================================================================

echo.
echo ============================================================================
echo Stopping Kubernetes Selenium Grid...
echo ============================================================================
echo.

REM Set working directory to current script location (kubectl folder)
cd /d "%~dp0"

echo [Step 1/3] Deleting Selenium Node Chrome Deployment...
kubectl delete deployment selenium-node-chrome-deployment
if errorlevel 1 (
    echo WARNING: Node deployment not found or already deleted
)

echo.
echo [Step 2/3] Deleting Selenium Hub Deployment...
kubectl delete deployment selenium-hub-deployment
if errorlevel 1 (
    echo WARNING: Hub deployment not found or already deleted
)

echo.
echo [Step 3/3] Deleting Selenium Hub Service...
kubectl delete service selenium-hub-service
if errorlevel 1 (
    echo WARNING: Hub service not found or already deleted
)

echo.
echo Waiting for resources to be cleaned up...
timeout /t 3 /nobreak

echo.
echo Current Resources:
kubectl get pods
echo.
kubectl get deployments
echo.
kubectl get services
echo.

echo ============================================================================
echo Kubernetes Selenium Grid Stopped Successfully!
echo ============================================================================
echo.
pause
