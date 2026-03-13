@echo off
REM ============================================================
REM Stop Healenium Backend Services
REM ============================================================

echo.
echo ============================================
echo   Stopping Healenium Backend Services
echo ============================================
echo.

cd /d "%~dp0"

docker-compose -f docker-compose-healenium.yml down

echo.
echo ============================================
echo   Healenium Backend Services Stopped
echo ============================================
echo.
pause
