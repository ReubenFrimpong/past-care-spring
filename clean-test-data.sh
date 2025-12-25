#!/bin/bash

# Clean Test Data Script
# This script removes all test data from the database

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${BLUE}  PastCare Test Data Cleanup${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}\n"

echo -e "${YELLOW}This will remove all test data from the database.${NC}"
echo -e "${YELLOW}Test data includes:${NC}"
echo -e "  - Portal users with @example.com emails"
echo -e "  - Admin user: testuser@example.com"
echo -e "  - Test Church E2E (ID: 999)"
echo -e "  - All members belonging to church ID 999"
echo -e "  - All households belonging to church ID 999\n"

read -p "Are you sure you want to continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Cleanup cancelled${NC}"
    exit 0
fi

echo -e "\n${YELLOW}Removing test data...${NC}"

# SQL cleanup commands
mysql -u root -ppassword past-care-spring <<EOF 2>&1 | grep -v "Warning"
-- Clean up in correct order to handle foreign key constraints
DELETE FROM portal_users WHERE email LIKE '%@example.com';
DELETE FROM user WHERE email = 'testuser@example.com';
DELETE FROM households WHERE church_id = 999;
DELETE FROM member WHERE church_id = 999;
DELETE FROM church WHERE name = 'Test Church E2E';

-- Show what was deleted
SELECT 'Test data cleanup complete' AS status;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Test data removed successfully${NC}\n"
else
    echo -e "${RED}Error: Failed to remove test data${NC}"
    exit 1
fi

echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}Cleanup complete!${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}\n"

echo -e "${YELLOW}Note: To re-add test data, run:${NC}"
echo -e "  ${BLUE}mysql -u root -ppassword past-care-spring < src/test/resources/test-seed-data.sql${NC}\n"
