@echo off
setlocal enabledelayedexpansion

:menu
cls
echo.
echo ======================================
echo   SELENIUM GRID MANAGEMENT CONSOLE
echo ======================================
echo.
echo Select an option:
echo.
echo   1. Start Grid (Basic)
echo   2. Start Grid with Video Recording
echo   3. Start Grid (Scaled)
echo   4. Stop Grid
echo   5. View Running Containers
echo   6. View Grid Console
echo   7. View Test Videos
echo   8. Clean Up (Remove all containers)
echo   9. Exit
echo.
set /p option="Enter your choice (1-9): "

if "%option%"=="1" goto start_basic
if "%option%"=="2" goto start_video
if "%option%"=="3" goto start_scaled
if "%option%"=="4" goto stop_grid
if "%option%"=="5" goto view_containers
if "%option%"=="6" goto view_console
if "%option%"=="7" goto view_videos
if "%option%"=="8" goto cleanup
if "%option%"=="9" goto exit_script

echo [ERROR] Invalid option!
timeout /t 2
goto menu

:start_basic
cls
echo Starting basic grid...
docker compose -f docker-compose-v3.yml up -d
if errorlevel 1 (
    echo [ERROR] Failed to start!
    pause
    goto menu
)
echo [SUCCESS] Grid started!
echo Access: http://localhost:4444
pause
goto menu

:start_video
cls
echo Starting grid with video recording...
if not exist testcaseVideos mkdir testcaseVideos
docker compose --profile video -f docker-compose-v3.yml up -d
if errorlevel 1 (
    echo [ERROR] Failed to start!
    pause
    goto menu
)
echo [SUCCESS] Grid started with video!
echo Grid: http://localhost:4444
echo Videos: http://localhost:8081
pause
goto menu

:start_scaled
cls
echo.
echo Scaling Configuration:
echo   1. 3 Chrome instances
echo   2. 2 Chrome + 2 Edge
echo   3. 2 Chrome + 2 Edge + 2 Firefox
set /p scale_opt="Select (1-3): "

if "%scale_opt%"=="1" (
    docker compose -f docker-compose-v3.yml up --scale chrome=3 -d
) else if "%scale_opt%"=="2" (
    docker compose -f docker-compose-v3.yml up --scale chrome=2 --scale edge=2 -d
) else if "%scale_opt%"=="3" (
    docker compose -f docker-compose-v3.yml up --scale chrome=2 --scale edge=2 --scale firefox=2 -d
) else (
    echo Invalid option!
    pause
    goto menu
)
echo [SUCCESS] Scaled grid started!
pause
goto menu

:stop_grid
cls
echo Stopping grid...
docker compose -f docker-compose-v3.yml down
echo [SUCCESS] Grid stopped!
pause
goto menu

:view_containers
cls
echo Running Containers:
echo.
docker compose -f docker-compose-v3.yml ps
echo.
pause
goto menu

:view_console
cls
echo Opening Selenium Grid Console...
start http://localhost:4444
timeout /t 2
goto menu

:view_videos
cls
echo Opening File Browser...
start http://localhost:8081
timeout /t 2
goto menu

:cleanup
cls
echo WARNING: This will remove all containers and networks!
set /p confirm="Are you sure? (yes/no): "
if /i "%confirm%"=="yes" (
    echo Cleaning up...
    docker compose -f docker-compose-v3.yml down -v
    echo [SUCCESS] Cleanup complete!
) else (
    echo Cleanup cancelled.
)
pause
goto menu

:exit_script
cls
echo Goodbye!
timeout /t 1
exit /b 0
