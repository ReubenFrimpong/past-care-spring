#!/bin/bash

# QR Code Workflow Test Script
# This script tests the complete QR code check-in workflow

echo "=========================================="
echo "QR Code Workflow Test"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

API_URL="http://localhost:8080/api"
FRONTEND_URL="http://localhost:4200"

# Step 1: Login to get JWT token
echo -e "${YELLOW}Step 1: Authenticating...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${API_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "reuben@test.com",
    "password": "password"
  }' \
  -c /tmp/cookies.txt)

# Check if login was successful by verifying user data exists
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.user.id // empty')

if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
  echo -e "${RED}✗ Login failed${NC}"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Login successful (User ID: $USER_ID)${NC}"
echo "   Note: Using cookie-based authentication"
echo ""

# Step 2: Create a test attendance session
echo -e "${YELLOW}Step 2: Creating test attendance session...${NC}"
SESSION_NAME="QR Code Test - $(date +%H:%M:%S)"
SESSION_DATE=$(date +%Y-%m-%d)

CREATE_SESSION_RESPONSE=$(curl -s -X POST "${API_URL}/attendance/sessions" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d "{
    \"sessionName\": \"$SESSION_NAME\",
    \"sessionDate\": \"$SESSION_DATE\",
    \"sessionTime\": \"10:00\",
    \"churchId\": 1
  }")

SESSION_ID=$(echo $CREATE_SESSION_RESPONSE | jq -r '.id // empty')

if [ -z "$SESSION_ID" ] || [ "$SESSION_ID" = "null" ]; then
  echo -e "${RED}✗ Failed to create session${NC}"
  echo "Response: $CREATE_SESSION_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Session created with ID: $SESSION_ID${NC}"
echo ""

# Step 3: Generate QR code for the session
echo -e "${YELLOW}Step 3: Generating QR code...${NC}"
QR_RESPONSE=$(curl -s -X POST "${API_URL}/attendance/sessions/${SESSION_ID}/qr-code" \
  -b /tmp/cookies.txt)

QR_DATA=$(echo $QR_RESPONSE | jq -r '.qrCodeData // empty')
QR_MESSAGE=$(echo $QR_RESPONSE | jq -r '.message // empty')

if [ -z "$QR_DATA" ] || [ "$QR_DATA" = "null" ]; then
  echo -e "${RED}✗ Failed to generate QR code${NC}"
  echo "Response: $QR_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ QR code generated${NC}"
echo "   Message: $QR_MESSAGE"
echo ""

# Step 4: Check if QR code message contains the check-in URL
echo -e "${YELLOW}Step 4: Verifying QR code contains check-in URL...${NC}"

if echo "$QR_MESSAGE" | grep -q "${FRONTEND_URL}/check-in?qr="; then
  echo -e "${GREEN}✓ QR code message contains correct check-in URL${NC}"

  # Extract the URL from the message
  CHECK_IN_URL=$(echo "$QR_MESSAGE" | grep -o "http://[^ ]*")
  echo "   Check-in URL: $CHECK_IN_URL"
else
  echo -e "${RED}✗ QR code message does not contain expected check-in URL${NC}"
  echo "   Expected URL pattern: ${FRONTEND_URL}/check-in?qr="
  echo "   Actual message: $QR_MESSAGE"
  exit 1
fi
echo ""

# Step 5: Verify check-in page is accessible
echo -e "${YELLOW}Step 5: Verifying check-in page is accessible...${NC}"
CHECK_IN_PAGE_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "${FRONTEND_URL}/check-in")

if [ "$CHECK_IN_PAGE_RESPONSE" = "200" ]; then
  echo -e "${GREEN}✓ Check-in page is accessible (HTTP $CHECK_IN_PAGE_RESPONSE)${NC}"
else
  echo -e "${RED}✗ Check-in page is not accessible (HTTP $CHECK_IN_PAGE_RESPONSE)${NC}"
  exit 1
fi
echo ""

# Step 6: Test member check-in by phone
echo -e "${YELLOW}Step 6: Testing member check-in...${NC}"

# First, get a member from the database
MEMBERS_RESPONSE=$(curl -s -X GET "${API_URL}/members?page=0&size=1" \
  -b /tmp/cookies.txt)

MEMBER_PHONE=$(echo $MEMBERS_RESPONSE | jq -r '.content[0].phoneNumber // empty')
MEMBER_NAME=$(echo $MEMBERS_RESPONSE | jq -r '.content[0].firstName // empty')

if [ -z "$MEMBER_PHONE" ] || [ "$MEMBER_PHONE" = "null" ]; then
  echo -e "${YELLOW}⚠ No members found in database. Skipping member check-in test.${NC}"
else
  echo "   Using member: $MEMBER_NAME (Phone: $MEMBER_PHONE)"

  CHECK_IN_RESPONSE=$(curl -s -X POST "${API_URL}/check-in/by-phone" \
    -H "Content-Type: application/json" \
    -d "{
      \"sessionId\": $SESSION_ID,
      \"phoneNumber\": \"$MEMBER_PHONE\",
      \"qrCodeData\": \"$QR_DATA\",
      \"deviceInfo\": \"Test Script\"
    }")

  CHECK_IN_STATUS=$(echo $CHECK_IN_RESPONSE | jq -r '.status // empty')
  CHECK_IN_MESSAGE=$(echo $CHECK_IN_RESPONSE | jq -r '.message // empty')

  if [ "$CHECK_IN_STATUS" = "PRESENT" ]; then
    echo -e "${GREEN}✓ Member check-in successful${NC}"
    echo "   Message: $CHECK_IN_MESSAGE"
  else
    echo -e "${RED}✗ Member check-in failed${NC}"
    echo "   Response: $CHECK_IN_RESPONSE"
  fi
fi
echo ""

# Step 7: Test visitor check-in
echo -e "${YELLOW}Step 7: Testing visitor check-in...${NC}"

VISITOR_CHECK_IN_RESPONSE=$(curl -s -X POST "${API_URL}/check-in/visitor" \
  -H "Content-Type: application/json" \
  -d "{
    \"sessionId\": $SESSION_ID,
    \"firstName\": \"Test\",
    \"lastName\": \"Visitor\",
    \"phoneNumber\": \"1234567890\",
    \"email\": \"test@visitor.com\",
    \"qrCodeData\": \"$QR_DATA\",
    \"deviceInfo\": \"Test Script\"
  }")

VISITOR_STATUS=$(echo $VISITOR_CHECK_IN_RESPONSE | jq -r '.status // empty')
VISITOR_MESSAGE=$(echo $VISITOR_CHECK_IN_RESPONSE | jq -r '.message // empty')

if [ "$VISITOR_STATUS" = "PRESENT" ]; then
  echo -e "${GREEN}✓ Visitor check-in successful${NC}"
  echo "   Message: $VISITOR_MESSAGE"
else
  echo -e "${RED}✗ Visitor check-in failed${NC}"
  echo "   Response: $VISITOR_CHECK_IN_RESPONSE"
fi
echo ""

# Summary
echo "=========================================="
echo -e "${GREEN}QR Code Workflow Test Complete!${NC}"
echo "=========================================="
echo ""
echo "Summary:"
echo "  • Session created: $SESSION_NAME (ID: $SESSION_ID)"
echo "  • QR code generated with check-in URL"
echo "  • Check-in page accessible"
echo "  • Member check-in: ${MEMBER_PHONE:-'N/A'}"
echo "  • Visitor check-in: Test Visitor"
echo ""
echo "Next steps:"
echo "  1. Test QR code by scanning with your phone"
echo "  2. Navigate to: $CHECK_IN_URL"
echo "  3. Verify the check-in page displays session information"
echo "  4. Try both member and visitor check-in flows"
echo ""

# Cleanup
rm -f /tmp/cookies.txt
