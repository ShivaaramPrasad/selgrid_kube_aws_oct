@echo off
echo =====================================
echo Starting Selenium Grid with Docker...
echo =====================================

docker compose -f docker-compose-v3.yml up -d

echo.
echo Grid Started Successfully!
echo Open: http://localhost:4444
echo.

pause