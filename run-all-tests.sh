#!/bin/bash

#############################################
# PastCare Spring - Comprehensive Test Runner
#
# This script runs all tests (API + E2E) in the correct order
# All tests are idempotent and can be run multiple times
#############################################

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print header
echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  PastCare Spring - Comprehensive Test Suite              ║${NC}"
echo -e "${BLUE}║  All tests are idempotent and can run multiple times     ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to print section headers
print_section() {
    echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW} $1${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Track start time
START_TIME=$(date +%s)

#############################################
# 1. Backend API Integration Tests
#############################################

print_section "Step 1/2: Running Backend API Integration Tests (250+ tests)"

echo "Running Maven tests with 'all-tests' profile..."
echo "This includes:"
echo "  - Unit tests (if any)"
echo "  - API integration tests (Authentication, Members, Attendance, Fellowship, Giving, etc.)"
echo ""

if mvn clean verify -P all-tests; then
    print_success "Backend API tests completed successfully"
else
    print_error "Backend API tests failed"
    exit 1
fi

#############################################
# 2. Frontend E2E Tests
#############################################

print_section "Step 2/2: Running Frontend E2E Tests (200+ tests)"

echo "Installing Playwright browsers (if not already installed)..."
cd past-care-spring-frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

# Install Playwright browsers
npx playwright install --with-deps chromium firefox webkit

echo ""
echo "Running Playwright E2E tests..."
echo "This includes:"
echo "  - Authentication & Users (30 tests)"
echo "  - Billing (25 tests)"
echo "  - Members (30 tests)"
echo "  - Attendance (20 tests)"
echo "  - Fellowship (18 tests)"
echo "  - Giving (22 tests)"
echo "  - Pastoral Care (25 tests)"
echo "  - Events (20 tests)"
echo "  - Communications (10 tests)"
echo "  - Form validation tests (77 tests)"
echo ""

if npx playwright test; then
    print_success "Frontend E2E tests completed successfully"
else
    print_error "Frontend E2E tests failed"
    cd ..
    exit 1
fi

cd ..

#############################################
# Summary
#############################################

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

echo ""
echo -e "${GREEN}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                  ALL TESTS PASSED! ✓                      ║${NC}"
echo -e "${GREEN}╠═══════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  Backend API Tests: ✓ PASSED                              ║${NC}"
echo -e "${GREEN}║  Frontend E2E Tests: ✓ PASSED                             ║${NC}"
echo -e "${GREEN}╠═══════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  Total Duration: ${MINUTES}m ${SECONDS}s                                   ║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}View E2E Test Report:${NC} npx playwright show-report"
echo -e "${BLUE}View Backend Test Results:${NC} target/surefire-reports/ and target/failsafe-reports/"
echo ""
