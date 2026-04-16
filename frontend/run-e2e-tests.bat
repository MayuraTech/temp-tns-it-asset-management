@echo off
REM E2E Test Execution Script for User Management Module (Windows)
REM This script starts the necessary services and runs Cypress E2E tests

setlocal enabledelayedexpansion

echo =========================================
echo User Management E2E Test Execution
echo =========================================
echo.

REM Check if backend is running
echo Checking if backend is running on port 8080...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Backend is not running on port 8080
    echo Please start the backend server first:
    echo   cd backend
    echo   mvnw spring-boot:run
    exit /b 1
)
echo [OK] Backend is running
echo.

REM Check if frontend is running
echo Checking if frontend is running on port 4200...
curl -s http://localhost:4200 >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Frontend is not running on port 4200
    echo Please start the frontend server first:
    echo   cd frontend
    echo   npm start
    exit /b 1
)
echo [OK] Frontend is running
echo.

REM Check if Cypress is installed
echo Checking Cypress installation...
where npx >nul 2>&1
if errorlevel 1 (
    echo [ERROR] npx is not installed
    echo Please install Node.js and npm
    exit /b 1
)
echo [OK] Cypress is available
echo.

REM Parse command line arguments
set MODE=run
set SPEC=
set BROWSER=chrome

:parse_args
if "%~1"=="" goto run_tests
if "%~1"=="--interactive" (
    set MODE=open
    shift
    goto parse_args
)
if "%~1"=="--spec" (
    set SPEC=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--browser" (
    set BROWSER=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--help" (
    echo Usage: run-e2e-tests.bat [OPTIONS]
    echo.
    echo Options:
    echo   --interactive       Run tests in interactive mode (Cypress UI^)
    echo   --spec ^<file^>       Run specific test file
    echo   --browser ^<name^>    Specify browser (chrome, firefox, edge^)
    echo   --help              Show this help message
    echo.
    echo Examples:
    echo   run-e2e-tests.bat
    echo   run-e2e-tests.bat --interactive
    echo   run-e2e-tests.bat --spec cypress/e2e/user-management/authentication.cy.ts
    echo   run-e2e-tests.bat --browser firefox
    exit /b 0
)
echo [ERROR] Unknown option: %~1
echo Use --help for usage information
exit /b 1

:run_tests
echo =========================================
echo Running E2E Tests
echo =========================================
echo.

REM Run tests based on mode
if "%MODE%"=="open" (
    echo Opening Cypress in interactive mode...
    npx cypress open
) else (
    if not "%SPEC%"=="" (
        echo Running specific test: %SPEC%
        npx cypress run --spec "%SPEC%" --browser %BROWSER%
    ) else (
        echo Running all E2E tests...
        npx cypress run --browser %BROWSER%
    )
)

REM Check exit code
if errorlevel 1 (
    echo.
    echo =========================================
    echo [ERROR] Some E2E tests failed
    echo =========================================
    echo.
    echo Check the output above for details
    echo Screenshots of failures are saved in: cypress\screenshots
    exit /b 1
) else (
    echo.
    echo =========================================
    echo [OK] All E2E tests passed!
    echo =========================================
    exit /b 0
)
