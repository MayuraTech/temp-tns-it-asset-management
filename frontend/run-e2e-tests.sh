#!/bin/bash

# E2E Test Execution Script for User Management Module
# This script starts the necessary services and runs Cypress E2E tests

set -e

echo "========================================="
echo "User Management E2E Test Execution"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if backend is running
echo "Checking if backend is running on port 8080..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${RED}❌ Backend is not running on port 8080${NC}"
    echo -e "${YELLOW}Please start the backend server first:${NC}"
    echo "  cd backend"
    echo "  ./mvnw spring-boot:run"
    exit 1
fi
echo -e "${GREEN}✓ Backend is running${NC}"
echo ""

# Check if frontend is running
echo "Checking if frontend is running on port 4200..."
if ! curl -s http://localhost:4200 > /dev/null 2>&1; then
    echo -e "${RED}❌ Frontend is not running on port 4200${NC}"
    echo -e "${YELLOW}Please start the frontend server first:${NC}"
    echo "  cd frontend"
    echo "  npm start"
    exit 1
fi
echo -e "${GREEN}✓ Frontend is running${NC}"
echo ""

# Check if Cypress is installed
echo "Checking Cypress installation..."
if ! command -v npx &> /dev/null; then
    echo -e "${RED}❌ npx is not installed${NC}"
    echo "Please install Node.js and npm"
    exit 1
fi
echo -e "${GREEN}✓ Cypress is available${NC}"
echo ""

# Run E2E tests
echo "========================================="
echo "Running E2E Tests"
echo "========================================="
echo ""

# Parse command line arguments
MODE="run"
SPEC=""
BROWSER="chrome"

while [[ $# -gt 0 ]]; do
    case $1 in
        --interactive)
            MODE="open"
            shift
            ;;
        --spec)
            SPEC="$2"
            shift 2
            ;;
        --browser)
            BROWSER="$2"
            shift 2
            ;;
        --help)
            echo "Usage: ./run-e2e-tests.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --interactive       Run tests in interactive mode (Cypress UI)"
            echo "  --spec <file>       Run specific test file"
            echo "  --browser <name>    Specify browser (chrome, firefox, edge)"
            echo "  --help              Show this help message"
            echo ""
            echo "Examples:"
            echo "  ./run-e2e-tests.sh"
            echo "  ./run-e2e-tests.sh --interactive"
            echo "  ./run-e2e-tests.sh --spec cypress/e2e/user-management/authentication.cy.ts"
            echo "  ./run-e2e-tests.sh --browser firefox"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Run tests based on mode
if [ "$MODE" = "open" ]; then
    echo "Opening Cypress in interactive mode..."
    npx cypress open
else
    if [ -n "$SPEC" ]; then
        echo "Running specific test: $SPEC"
        npx cypress run --spec "$SPEC" --browser "$BROWSER"
    else
        echo "Running all E2E tests..."
        npx cypress run --browser "$BROWSER"
    fi
fi

# Check exit code
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo -e "${GREEN}✓ All E2E tests passed!${NC}"
    echo "========================================="
    exit 0
else
    echo ""
    echo "========================================="
    echo -e "${RED}❌ Some E2E tests failed${NC}"
    echo "========================================="
    echo ""
    echo "Check the output above for details"
    echo "Screenshots of failures are saved in: cypress/screenshots"
    exit 1
fi
