#!/bin/bash

# E2E Test Runner Script
# This script sets up test data, starts backend and frontend, runs e2e tests, and cleans up

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Directories
BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$(dirname "$BACKEND_DIR")/past-care-spring-frontend"

# Process IDs
BACKEND_PID=""
FRONTEND_PID=""

# Cleanup function
cleanup() {
    echo -e "\n${YELLOW}Cleaning up processes...${NC}"

    if [ ! -z "$BACKEND_PID" ]; then
        echo "Stopping backend (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null || true
    fi

    if [ ! -z "$FRONTEND_PID" ]; then
        echo "Stopping frontend (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID 2>/dev/null || true
    fi

    # Kill any remaining processes on ports
    lsof -ti:8080 2>/dev/null | xargs kill -9 2>/dev/null || true
    lsof -ti:4200 2>/dev/null | xargs kill -9 2>/dev/null || true

    echo -e "${GREEN}Cleanup complete${NC}"
}

# Register cleanup on script exit
trap cleanup EXIT INT TERM

echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${BLUE}  PastCare E2E Test Runner${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}\n"

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v mysql &> /dev/null; then
    echo -e "${RED}Error: MySQL not found${NC}"
    exit 1
fi

if ! command -v node &> /dev/null; then
    echo -e "${RED}Error: Node.js not found${NC}"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo -e "${RED}Error: npm not found${NC}"
    exit 1
fi

if [ ! -d "$FRONTEND_DIR" ]; then
    echo -e "${RED}Error: Frontend directory not found at $FRONTEND_DIR${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites met${NC}\n"

# Step 1: Apply test seed data
echo -e "${YELLOW}Step 1: Applying test seed data...${NC}"
if mysql -u root -ppassword past-care-spring < "$BACKEND_DIR/src/test/resources/test-seed-data.sql" 2>&1 | grep -v "Warning"; then
    echo -e "${GREEN}✓ Test data applied successfully${NC}\n"
else
    echo -e "${RED}Error: Failed to apply test data${NC}"
    exit 1
fi

# Step 2: Start backend
echo -e "${YELLOW}Step 2: Starting Spring Boot backend...${NC}"
cd "$BACKEND_DIR"

# Check if port 8080 is already in use
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo -e "${YELLOW}Port 8080 is already in use. Killing existing process...${NC}"
    lsof -ti:8080 | xargs kill -9 2>/dev/null || true
    sleep 2
fi

# Start backend in background
./mvnw spring-boot:run > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend started (PID: $BACKEND_PID)"

# Wait for backend to be ready
echo -n "Waiting for backend to start"
BACKEND_READY=0
for i in {1..60}; do
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        BACKEND_READY=1
        break
    fi
    echo -n "."
    sleep 2
done
echo ""

if [ $BACKEND_READY -eq 0 ]; then
    echo -e "${RED}Error: Backend failed to start within 120 seconds${NC}"
    echo -e "${YELLOW}Backend logs:${NC}"
    tail -50 /tmp/backend.log
    exit 1
fi

echo -e "${GREEN}✓ Backend is ready${NC}\n"

# Step 3: Start frontend
echo -e "${YELLOW}Step 3: Starting Angular frontend...${NC}"
cd "$FRONTEND_DIR"

# Check if port 4200 is already in use
if lsof -Pi :4200 -sTCP:LISTEN -t >/dev/null ; then
    echo -e "${YELLOW}Port 4200 is already in use. Killing existing process...${NC}"
    lsof -ti:4200 | xargs kill -9 2>/dev/null || true
    sleep 2
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

# Start frontend in background
npm start > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend started (PID: $FRONTEND_PID)"

# Wait for frontend to be ready
echo -n "Waiting for frontend to start"
FRONTEND_READY=0
for i in {1..60}; do
    if curl -f http://localhost:4200 >/dev/null 2>&1; then
        FRONTEND_READY=1
        break
    fi
    echo -n "."
    sleep 2
done
echo ""

if [ $FRONTEND_READY -eq 0 ]; then
    echo -e "${RED}Error: Frontend failed to start within 120 seconds${NC}"
    echo -e "${YELLOW}Frontend logs:${NC}"
    tail -50 /tmp/frontend.log
    exit 1
fi

echo -e "${GREEN}✓ Frontend is ready${NC}\n"

# Step 4: Run e2e tests
echo -e "${YELLOW}Step 4: Running E2E tests...${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}\n"

# Parse command line arguments
TEST_FILE="$1"
PLAYWRIGHT_ARGS="${@:2}"

if [ ! -z "$TEST_FILE" ]; then
    echo -e "${YELLOW}Running specific test: $TEST_FILE${NC}\n"
    npx playwright test "$TEST_FILE" $PLAYWRIGHT_ARGS
else
    echo -e "${YELLOW}Running all portal tests (fixed tests only)${NC}\n"
    npx playwright test e2e/portal-*.spec.ts $PLAYWRIGHT_ARGS
fi

TEST_EXIT_CODE=$?

echo -e "\n${BLUE}═══════════════════════════════════════════${NC}"

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
else
    echo -e "${RED}✗ Some tests failed${NC}"
    echo -e "${YELLOW}Tip: Run with --headed to see tests in browser${NC}"
    echo -e "${YELLOW}Tip: Run with --debug to step through tests${NC}"
fi

echo -e "${BLUE}═══════════════════════════════════════════${NC}\n"

# Cleanup happens automatically via trap

exit $TEST_EXIT_CODE
