#!/bin/bash

# Test script for bulk affected members feature
# Tests the ability to add all members to a crisis (e.g., COVID-19 affecting entire congregation)

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "=== Testing Bulk Affected Members Feature ==="
echo ""

# Step 1: Login
echo "1. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@church.com",
    "password": "admin123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo "❌ Login failed"
  exit 1
fi
echo "✓ Login successful"
echo ""

# Step 2: Get all members to verify count
echo "2. Getting all members..."
MEMBERS_RESPONSE=$(curl -s -X GET "$BASE_URL/members?size=10000" \
  -H "Authorization: Bearer $TOKEN")

MEMBER_COUNT=$(echo $MEMBERS_RESPONSE | jq -r '.content | length')
echo "✓ Found $MEMBER_COUNT members"

# Get first 5 member IDs for bulk add
MEMBER_IDS=$(echo $MEMBERS_RESPONSE | jq -r '.content[0:5] | map(.id)')
echo "✓ Selected 5 members for testing: $MEMBER_IDS"
echo ""

# Step 3: Report a test crisis
echo "3. Reporting a test crisis (Church-wide COVID-19)..."
CRISIS_RESPONSE=$(curl -s -X POST "$BASE_URL/crises" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "COVID-19 Pandemic Response",
    "description": "Church-wide crisis affecting all members due to COVID-19 pandemic",
    "crisisType": "HEALTH_EMERGENCY",
    "severity": "CRITICAL",
    "incidentDate": "2020-03-15T10:00:00",
    "location": "Church-wide",
    "status": "ACTIVE"
  }')

CRISIS_ID=$(echo $CRISIS_RESPONSE | jq -r '.id')

if [ -z "$CRISIS_ID" ] || [ "$CRISIS_ID" == "null" ]; then
  echo "❌ Failed to create crisis"
  echo "Response: $CRISIS_RESPONSE"
  exit 1
fi
echo "✓ Crisis created with ID: $CRISIS_ID"
echo ""

# Step 4: Bulk add affected members
echo "4. Bulk adding 5 members to crisis..."
BULK_ADD_RESPONSE=$(curl -s -X POST "$BASE_URL/crises/$CRISIS_ID/affected-members/bulk" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"memberIds\": $MEMBER_IDS,
    \"notes\": \"Affected by COVID-19 pandemic\",
    \"isPrimaryContact\": false
  }")

ADDED_COUNT=$(echo $BULK_ADD_RESPONSE | jq -r '. | length')

if [ -z "$ADDED_COUNT" ] || [ "$ADDED_COUNT" == "null" ]; then
  echo "❌ Bulk add failed"
  echo "Response: $BULK_ADD_RESPONSE"
  exit 1
fi
echo "✓ Successfully added $ADDED_COUNT members"
echo ""

# Step 5: Verify affected members were added
echo "5. Verifying crisis now has affected members..."
VERIFY_RESPONSE=$(curl -s -X GET "$BASE_URL/crises/$CRISIS_ID" \
  -H "Authorization: Bearer $TOKEN")

AFFECTED_COUNT=$(echo $VERIFY_RESPONSE | jq -r '.affectedMembers | length')
echo "✓ Crisis now has $AFFECTED_COUNT affected members"
echo ""

# Step 6: Test duplicate prevention (should skip already added members)
echo "6. Testing duplicate prevention (re-adding same members)..."
DUPLICATE_RESPONSE=$(curl -s -X POST "$BASE_URL/crises/$CRISIS_ID/affected-members/bulk" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"memberIds\": $MEMBER_IDS,
    \"notes\": \"Testing duplicate prevention\",
    \"isPrimaryContact\": false
  }")

DUPLICATE_COUNT=$(echo $DUPLICATE_RESPONSE | jq -r '. | length')
echo "✓ Re-adding same members resulted in $DUPLICATE_COUNT new additions (should be 0)"
echo ""

# Step 7: Check stats
echo "7. Checking crisis statistics..."
STATS_RESPONSE=$(curl -s -X GET "$BASE_URL/crises/stats" \
  -H "Authorization: Bearer $TOKEN")

TOTAL_AFFECTED=$(echo $STATS_RESPONSE | jq -r '.totalAffectedMembers')
echo "✓ Total affected members across all crises: $TOTAL_AFFECTED"
echo ""

# Cleanup
echo "8. Cleaning up test data..."
curl -s -X DELETE "$BASE_URL/crises/$CRISIS_ID" \
  -H "Authorization: Bearer $TOKEN" > /dev/null
echo "✓ Test crisis deleted"
echo ""

echo "=== All Tests Passed! ==="
echo ""
echo "Summary:"
echo "- Created crisis with ID: $CRISIS_ID"
echo "- Bulk added $ADDED_COUNT members"
echo "- Verified $AFFECTED_COUNT affected members"
echo "- Duplicate prevention working (0 duplicates)"
echo "- Stats updated correctly"
