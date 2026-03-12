@echo off
setlocal enabledelayedexpansion

:menu
cls
echo.
echo ============================================================================
echo     KUBERNETES SELENIUM GRID MENU
echo ============================================================================
echo.
echo Select an option:
echo.
echo 1. Start Kubernetes Grid (Deploy Hub + Nodes)
echo 2. Stop Kubernetes Grid (Remove Hub + Nodes)
echo 3. Check Grid Status
echo 4. View Hub Logs (Live)
echo 5. View Node Logs (Live)
echo 6. Port Forward to Hub (4444)
echo 7. Access Grid UI (http://localhost:4444)
echo 8. Run Tests with Maven
echo 9. Delete All and Clean Up
echo 0. Exit
echo.
echo ============================================================================
echo.

set /p choice="Enter your choice (0-9): "

if "%choice%"=="1" goto start_grid
if "%choice%"=="2" goto stop_grid
if "%choice%"=="3" goto check_status
if "%choice%"=="4" goto hub_logs
if "%choice%"=="5" goto node_logs
if "%choice%"=="6" goto port_forward
if "%choice%"=="7" goto access_ui
if "%choice%"=="8" goto run_tests
if "%choice%"=="9" goto cleanup
if "%choice%"=="0" goto exit_menu

echo Invalid choice. Please try again.
timeout /t 2 /nobreak
goto menu

:start_grid
echo.
echo Starting Kubernetes Grid...
echo.
call "%~dp0start-kubectl-grid.bat"
goto menu

:stop_grid
echo.
echo Stopping Kubernetes Grid...
echo.
call "%~dp0stop-kubectl-grid.bat"
goto menu

:check_status
echo.
echo Checking Grid Status...
echo.
call "%~dp0kubectl-status.bat"
goto menu

:hub_logs
echo.
echo Hub Pod Logs (Press Ctrl+C to stop):
echo.
cd /d "%~dp0..\kubectl"
kubectl logs -l app=selenium-hub -f
goto menu

:node_logs
echo.
echo Node Pod Logs (Press Ctrl+C to stop):
echo.
cd /d "%~dp0..\kubectl"
kubectl logs -l app=selenium-node-chrome -f
goto menu

:port_forward
echo.
echo Starting Port Forward: localhost:4444 ^-^> svc/selenium-hub-service:4444
echo (This terminal will stay open for the port-forward connection)
echo (Press Ctrl+C to stop)
echo.
cd /d "%~dp0..\kubectl"
kubectl port-forward svc/selenium-hub-service 4444:4444
goto menu

:access_ui
echo.
echo Opening Grid UI in default browser: http://localhost:4444
echo.
start "" "http://localhost:4444"
timeout /t 2 /nobreak
goto menu

:run_tests
echo.
echo Running Maven Tests...
echo.
cd /d "%~dp0.."
call mvn clean test
echo.
pause
goto menu

:cleanup
echo.
echo WARNING: This will delete all Kubernetes resources and clean up.
echo.
set /p confirm="Are you sure? (y/n): "
if /i "%confirm%"=="y" (
    echo Cleaning up all resources...
    cd /d "%~dp0..\kubectl"
    kubectl delete deployment --all
    kubectl delete service --all
    echo.
    echo Cleanup complete!
    timeout /t 2 /nobreak
) else (
    echo Cleanup cancelled.
)
goto menu

:exit_menu
echo.
echo Exiting Kubernetes Grid Menu...
echo.
exit /b 0
