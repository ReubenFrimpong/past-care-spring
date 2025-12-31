#!/bin/bash

#############################################
# PastCare Spring - E2E Test Runner
#
# Runs only the frontend Playwright E2E tests
# All tests are idempotent (isolated test data + cleanup)
#############################################

set -e

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  Running Frontend E2E Tests (200+ tests)                  ║"
echo "║  All tests are idempotent - safe to run multiple times    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

cd past-care-spring-frontend

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
    echo ""
fi

echo "Test modules covered:"
echo "  ✓ Authentication & Users (30 tests + 10 validation tests)"
echo "  ✓ Billing (25 tests)"
echo "  ✓ Members (30 tests + 12 validation tests)"
echo "  ✓ Attendance (20 tests + 8 validation tests)"
echo "  ✓ Fellowship (18 tests + 8 validation tests)"
echo "  ✓ Giving (22 tests + 10 validation tests)"
echo "  ✓ Pastoral Care (25 tests + 13 validation tests)"
echo "  ✓ Events (20 tests + 10 validation tests)"
echo "  ✓ Communications (10 tests + 6 validation tests)"
echo "  ✓ Cross-Module Integration (10 tests)"
echo ""

START_TIME=$(date +%s)

npx playwright test

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

echo ""
echo "✓ E2E Tests Completed Successfully!"
echo "Duration: ${MINUTES}m ${SECONDS}s"
echo ""
echo "View detailed test report:"
echo "  npx playwright show-report"
echo ""

cd ..
