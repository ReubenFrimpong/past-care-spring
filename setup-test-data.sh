#!/bin/bash

# Setup Test Data Script
# This script applies test seed data to the database

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_FILE="$SCRIPT_DIR/src/test/resources/test-seed-data.sql"

echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${BLUE}  PastCare Test Data Setup${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}\n"

# Check if seed file exists
if [ ! -f "$SEED_FILE" ]; then
    echo -e "${RED}Error: Test seed data file not found at:${NC}"
    echo -e "${RED}$SEED_FILE${NC}"
    exit 1
fi

echo -e "${YELLOW}This will add test data to the database:${NC}"
echo -e "  ${GREEN}✓${NC} Test Church: 'Test Church E2E' (ID: 999)"
echo -e "  ${GREEN}✓${NC} Portal Users:"
echo -e "      - approved.member@example.com / ApprovedPassword123!"
echo -e "      - unverified@example.com / UnverifiedPassword123!"
echo -e "      - pending@example.com / PendingPassword123!"
echo -e "  ${GREEN}✓${NC} Admin User: testuser@example.com / password123"
echo -e "  ${GREEN}✓${NC} Test Members: John Smith, Jane Smith, Child Smith"
echo -e "  ${GREEN}✓${NC} Test Household: Smith Family\n"

echo -e "${YELLOW}Note: Existing test data will be cleaned and reinserted.${NC}\n"

read -p "Continue? (Y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Nn]$ ]]; then
    echo -e "${YELLOW}Setup cancelled${NC}"
    exit 0
fi

echo -e "\n${YELLOW}Applying test seed data...${NC}"

# Apply seed data
if mysql -u root -ppassword past-care-spring < "$SEED_FILE" 2>&1 | grep -v "Warning"; then
    echo -e "\n${GREEN}═══════════════════════════════════════════${NC}"
    echo -e "${GREEN}✓ Test data applied successfully!${NC}"
    echo -e "${GREEN}═══════════════════════════════════════════${NC}\n"

    echo -e "${YELLOW}Test Credentials:${NC}\n"

    echo -e "${BLUE}Portal Users (for portal app):${NC}"
    echo -e "  Email: ${GREEN}approved.member@example.com${NC}"
    echo -e "  Password: ${GREEN}ApprovedPassword123!${NC}"
    echo -e "  URL: ${GREEN}http://localhost:4200/portal/login?churchId=999${NC}\n"

    echo -e "  Email: ${YELLOW}unverified@example.com${NC}"
    echo -e "  Password: ${YELLOW}UnverifiedPassword123!${NC}"
    echo -e "  Status: ${YELLOW}Pending Email Verification${NC}\n"

    echo -e "  Email: ${YELLOW}pending@example.com${NC}"
    echo -e "  Password: ${YELLOW}PendingPassword123!${NC}"
    echo -e "  Status: ${YELLOW}Pending Admin Approval${NC}\n"

    echo -e "${BLUE}Admin User (for main app):${NC}"
    echo -e "  Email: ${GREEN}testuser@example.com${NC}"
    echo -e "  Password: ${GREEN}password123${NC}"
    echo -e "  URL: ${GREEN}http://localhost:4200/login${NC}\n"

    echo -e "${YELLOW}Next Steps:${NC}"
    echo -e "  1. Start backend: ${BLUE}./mvnw spring-boot:run${NC}"
    echo -e "  2. Start frontend: ${BLUE}cd ../past-care-spring-frontend && npm start${NC}"
    echo -e "  3. Run tests: ${BLUE}./run-e2e-tests.sh${NC}\n"

    echo -e "${YELLOW}Or run everything automatically:${NC}"
    echo -e "  ${BLUE}./run-e2e-tests.sh${NC}\n"
else
    echo -e "\n${RED}═══════════════════════════════════════════${NC}"
    echo -e "${RED}✗ Failed to apply test data${NC}"
    echo -e "${RED}═══════════════════════════════════════════${NC}\n"

    echo -e "${YELLOW}Common Issues:${NC}"
    echo -e "  1. MySQL not running: ${BLUE}sudo systemctl start mysql${NC}"
    echo -e "  2. Wrong credentials: Check application.properties"
    echo -e "  3. Database doesn't exist: ${BLUE}mysql -u root -ppassword -e 'CREATE DATABASE past-care-spring;'${NC}\n"
    exit 1
fi
