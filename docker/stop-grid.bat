@echo off
echo =====================================
echo Stopping Selenium Grid...
echo =====================================

docker compose -f docker-compose-v3.yml down

echo.
echo Grid Stopped Successfully!
echo.

pause