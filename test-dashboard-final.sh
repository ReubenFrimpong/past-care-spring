#!/bin/bash

# Comprehensive Test for Option C Location Feature

echo "========================================="
echo "Option C Location System - Final Test"
echo "========================================="
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASSED=0
FAILED=0

print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((FAILED++))
    fi
}

echo "=== Database Schema Tests ==="
echo ""

LOCATIONS_TABLE=$(mysql -u root -ppassword past-care-spring -se "SHOW TABLES LIKE 'locations';" 2>/dev/null)
[ "$LOCATIONS_TABLE" = "locations" ] && print_result 0 "Locations table exists" || print_result 1 "Locations table exists"

LOCATION_ID_COL=$(mysql -u root -ppassword past-care-spring -se "DESCRIBE member;" 2>/dev/null | grep location_id | wc -l)
[ "$LOCATION_ID_COL" -eq 1 ] && print_result 0 "Member.location_id exists" || print_result 1 "Member.location_id missing"

UNIQUE_CONSTRAINT=$(mysql -u root -ppassword past-care-spring -se "SHOW INDEX FROM locations WHERE Key_name LIKE '%coordinates%' AND Non_unique = 0;" 2>/dev/null | wc -l)
[ "$UNIQUE_CONSTRAINT" -gt 0 ] && print_result 0 "Unique constraint on coordinates" || print_result 1 "Missing unique constraint"

echo ""
echo "=== Backend API Tests ==="
echo ""

curl -s http://localhost:8080/actuator/health > /dev/null 2>&1 && print_result 0 "Backend running" || print_result 1 "Backend not running"

echo ""
echo "=== Summary ==="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
[ $FAILED -eq 0 ] && echo -e "${GREEN}✓ All tests passed!${NC}" || echo -e "${RED}✗ Some failures${NC}"
