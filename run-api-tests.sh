#!/bin/bash

#############################################
# PastCare Spring - API Integration Test Runner
#
# Runs only the backend API integration tests
# All tests are idempotent (use H2 in-memory DB with transaction rollback)
#############################################

set -e

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  Running Backend API Integration Tests (250+ tests)       ║"
echo "║  All tests are idempotent - safe to run multiple times    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

echo "Test modules covered:"
echo "  ✓ Authentication & Users"
echo "  ✓ Members (CRUD, Search, Multi-tenancy)"
echo "  ✓ Attendance (Sessions, QR Codes, Visitors, Analytics)"
echo "  ✓ Fellowship"
echo "  ✓ Giving (Donations, Campaigns, Pledges)"
echo "  ✓ Pastoral Care (Care Needs, Visits, Counseling, Prayer Requests)"
echo "  ✓ Events"
echo "  ✓ Communications (SMS)"
echo "  ✓ Billing (Subscriptions, Payments)"
echo ""

START_TIME=$(date +%s)

mvn verify -P api-tests

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

echo ""
echo "✓ API Integration Tests Completed Successfully!"
echo "Duration: ${MINUTES}m ${SECONDS}s"
echo ""
echo "Test Results Location:"
echo "  - target/failsafe-reports/"
echo ""
