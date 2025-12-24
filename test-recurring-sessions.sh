#!/bin/bash

# Recurring Sessions Test Script
# Tests the automatic recurring session generation feature

echo "=========================================="
echo "Recurring Sessions Test"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

API_URL="http://localhost:8080/api"

# Step 1: Login
echo -e "${YELLOW}Step 1: Authenticating...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${API_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "reuben@test.com",
    "password": "password"
  }' \
  -c /tmp/cookies.txt)

USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.user.id // empty')

if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
  echo -e "${RED}✗ Login failed${NC}"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Login successful (User ID: $USER_ID)${NC}"
echo ""

# Step 2: Create a recurring session template
echo -e "${YELLOW}Step 2: Creating recurring session template...${NC}"
TEMPLATE_NAME="Sunday Service - Weekly Template"
TEMPLATE_DATE=$(date +%Y-%m-%d)

CREATE_TEMPLATE_RESPONSE=$(curl -s -X POST "${API_URL}/attendance/sessions" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d "{
    \"sessionName\": \"$TEMPLATE_NAME\",
    \"sessionDate\": \"$TEMPLATE_DATE\",
    \"sessionTime\": \"10:00\",
    \"churchId\": 1,
    \"isRecurring\": true,
    \"recurrencePattern\": \"WEEKLY:SUNDAY\"
  }")

TEMPLATE_ID=$(echo $CREATE_TEMPLATE_RESPONSE | jq -r '.id // empty')

if [ -z "$TEMPLATE_ID" ] || [ "$TEMPLATE_ID" = "null" ]; then
  echo -e "${RED}✗ Failed to create template${NC}"
  echo "Response: $CREATE_TEMPLATE_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Template created with ID: $TEMPLATE_ID${NC}"
echo "   Pattern: WEEKLY:SUNDAY"
echo ""

# Step 3: Manually trigger session generation for this template
echo -e "${YELLOW}Step 3: Triggering session generation (7 days ahead)...${NC}"

GENERATE_RESPONSE=$(curl -s -X POST "${API_URL}/recurring-sessions/${TEMPLATE_ID}/generate?daysAhead=7" \
  -b /tmp/cookies.txt)

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ Session generation triggered${NC}"
  echo "   Response: $GENERATE_RESPONSE"
else
  echo -e "${RED}✗ Failed to trigger generation${NC}"
  echo "   Response: $GENERATE_RESPONSE"
fi
echo ""

# Step 4: Check how many sessions were generated
echo -e "${YELLOW}Step 4: Checking generated sessions...${NC}"

# Calculate date range (today + 7 days)
START_DATE=$(date +%Y-%m-%d)
END_DATE=$(date -d "+7 days" +%Y-%m-%d 2>/dev/null || date -v+7d +%Y-%m-%d 2>/dev/null)

SESSIONS_RESPONSE=$(curl -s -X GET "${API_URL}/attendance/sessions?churchId=1" \
  -b /tmp/cookies.txt)

# Count sessions with the template name (excluding the template itself)
GENERATED_COUNT=$(echo $SESSIONS_RESPONSE | jq "[.[] | select(.sessionName == \"$TEMPLATE_NAME\" and .isRecurring == false)] | length")

if [ "$GENERATED_COUNT" -gt 0 ]; then
  echo -e "${GREEN}✓ Found $GENERATED_COUNT generated session(s)${NC}"

  # Show details of generated sessions
  echo ""
  echo -e "${BLUE}Generated Sessions:${NC}"
  echo $SESSIONS_RESPONSE | jq -r ".[] | select(.sessionName == \"$TEMPLATE_NAME\" and .isRecurring == false) | \"  • \(.sessionDate) at \(.sessionTime // \"no time\") (ID: \(.id))\""
else
  echo -e "${YELLOW}⚠ No sessions generated yet${NC}"
  echo "   This could mean:"
  echo "   - No Sundays in the next 7 days"
  echo "   - Sessions already exist for those dates"
  echo "   - Template was just created (scheduler runs at midnight)"
fi
echo ""

# Step 5: Test with another pattern (Daily)
echo -e "${YELLOW}Step 5: Testing DAILY pattern...${NC}"

DAILY_TEMPLATE_RESPONSE=$(curl -s -X POST "${API_URL}/attendance/sessions" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d "{
    \"sessionName\": \"Daily Devotion\",
    \"sessionDate\": \"$TEMPLATE_DATE\",
    \"sessionTime\": \"06:00\",
    \"churchId\": 1,
    \"isRecurring\": true,
    \"recurrencePattern\": \"DAILY\"
  }")

DAILY_TEMPLATE_ID=$(echo $DAILY_TEMPLATE_RESPONSE | jq -r '.id // empty')

if [ -z "$DAILY_TEMPLATE_ID" ] || [ "$DAILY_TEMPLATE_ID" = "null" ]; then
  echo -e "${RED}✗ Failed to create daily template${NC}"
else
  echo -e "${GREEN}✓ Daily template created (ID: $DAILY_TEMPLATE_ID)${NC}"

  # Generate sessions
  DAILY_GENERATE=$(curl -s -X POST "${API_URL}/recurring-sessions/${DAILY_TEMPLATE_ID}/generate?daysAhead=7" \
    -b /tmp/cookies.txt)

  echo "   Response: $DAILY_GENERATE"
fi
echo ""

# Step 6: Test with monthly pattern
echo -e "${YELLOW}Step 6: Testing MONTHLY pattern...${NC}"

MONTHLY_TEMPLATE_RESPONSE=$(curl -s -X POST "${API_URL}/attendance/sessions" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d "{
    \"sessionName\": \"Monthly Leadership Meeting\",
    \"sessionDate\": \"$TEMPLATE_DATE\",
    \"sessionTime\": \"19:00\",
    \"churchId\": 1,
    \"isRecurring\": true,
    \"recurrencePattern\": \"MONTHLY:1\"
  }")

MONTHLY_TEMPLATE_ID=$(echo $MONTHLY_TEMPLATE_RESPONSE | jq -r '.id // empty')

if [ -z "$MONTHLY_TEMPLATE_ID" ] || [ "$MONTHLY_TEMPLATE_ID" = "null" ]; then
  echo -e "${RED}✗ Failed to create monthly template${NC}"
else
  echo -e "${GREEN}✓ Monthly template created (ID: $MONTHLY_TEMPLATE_ID)${NC}"

  # Generate sessions (use 31 days to ensure we get at least one month)
  MONTHLY_GENERATE=$(curl -s -X POST "${API_URL}/recurring-sessions/${MONTHLY_TEMPLATE_ID}/generate?daysAhead=31" \
    -b /tmp/cookies.txt)

  echo "   Response: $MONTHLY_GENERATE"
fi
echo ""

# Step 7: Trigger generation for all templates
echo -e "${YELLOW}Step 7: Triggering generation for ALL templates...${NC}"

ALL_GENERATE_RESPONSE=$(curl -s -X POST "${API_URL}/recurring-sessions/generate-all" \
  -b /tmp/cookies.txt)

echo -e "${GREEN}✓ Generation triggered for all templates${NC}"
echo "   Response: $ALL_GENERATE_RESPONSE"
echo ""

# Summary
echo "=========================================="
echo -e "${GREEN}Recurring Sessions Test Complete!${NC}"
echo "=========================================="
echo ""
echo "Summary:"
echo "  • Weekly template created: $TEMPLATE_NAME (ID: $TEMPLATE_ID)"
echo "  • Daily template created: Daily Devotion (ID: ${DAILY_TEMPLATE_ID:-N/A})"
echo "  • Monthly template created: Monthly Leadership Meeting (ID: ${MONTHLY_TEMPLATE_ID:-N/A})"
echo "  • Generated sessions: ${GENERATED_COUNT:-0}"
echo ""
echo "Next steps:"
echo "  1. Check the sessions list in the UI"
echo "  2. Verify recurring templates show up"
echo "  3. Verify generated sessions appear for upcoming dates"
echo "  4. The scheduler will run automatically at midnight"
echo ""
echo "To manually trigger generation again:"
echo "  curl -X POST '$API_URL/recurring-sessions/generate-all' -b /tmp/cookies.txt"
echo ""

# Cleanup
rm -f /tmp/cookies.txt
