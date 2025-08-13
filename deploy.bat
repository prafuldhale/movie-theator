@echo off
setlocal enabledelayedexpansion

REM Movie Booking Application Docker Deployment Script for Windows
REM Author: Movie Booking Team
REM Version: 1.0

echo 🎬 Movie Booking Application - Docker Deployment
echo ================================================

REM Check if Docker is running
echo [INFO] Checking Docker installation...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running or not installed. Please start Docker and try again.
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo [SUCCESS] Docker is running

REM Clean up old containers
echo [INFO] Cleaning up old containers...
docker ps -a --filter "name=moviebooking" --format "{{.Names}}" | findstr /r "." >nul 2>&1
if not errorlevel 1 (
    for /f "tokens=*" %%i in ('docker ps -a --filter "name=moviebooking" --format "{{.Names}}"') do (
        docker stop %%i >nul 2>&1
        docker rm %%i >nul 2>&1
    )
    echo [SUCCESS] Old containers cleaned up
)

REM Build the application
echo [INFO] Building the application...
echo [INFO] Running tests...
C:\apache-maven-3.9.11\bin\mvn.cmd test
if errorlevel 1 (
    echo [ERROR] Tests failed. Please fix the issues before deploying.
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo [SUCCESS] Tests passed

echo [INFO] Building with Maven...
C:\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Maven build failed.
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo [SUCCESS] Maven build completed

REM Build Docker image
echo [INFO] Building Docker image...
docker build -t moviebooking-backend:latest .
if errorlevel 1 (
    echo [ERROR] Docker build failed
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo [SUCCESS] Docker image built successfully

REM Deploy with docker-compose
echo [INFO] Deploying with docker-compose...
if not exist logs mkdir logs

docker-compose up -d
if errorlevel 1 (
    echo [ERROR] Docker-compose deployment failed
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo [SUCCESS] Application deployed successfully

REM Check application health
echo [INFO] Checking application health...
echo [INFO] Waiting for application to start (30 seconds)...
timeout /t 30 /nobreak >nul

REM Check if application is responding
curl -f http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Application health check failed. Checking logs...
    docker-compose logs moviebooking-app
    echo [WARNING] Please check the logs above for any issues
    echo.
    echo Press any key to continue...
    pause >nul
) else (
    echo [SUCCESS] Application is healthy and responding
)

REM Show deployment information
echo.
echo 🎉 Deployment Complete!
echo ======================
echo Application URL: http://localhost:8080
echo H2 Console: http://localhost:8080/h2-console
echo API Documentation: http://localhost:8080/api/v1.0/moviebooking
echo.
echo Useful Commands:
echo   View logs: docker-compose logs -f moviebooking-app
echo   Stop app: docker-compose down
echo   Restart app: docker-compose restart
echo   View containers: docker ps
echo.

echo Press any key to exit...
pause >nul
