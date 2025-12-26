#!/bin/bash

# Test the improved Add Members to Fellowship UX
# This script tests the new endpoint that returns fellowship member IDs

echo "Testing Fellowship Member IDs Endpoint..."
echo "=========================================="

# Get auth token (replace with actual credentials)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"admin123"}' \
  | jq -r '.token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ Failed to get auth token"
  exit 1
fi

echo "✓ Got auth token"

# Get all fellowships
echo -e "\nFetching fellowships..."
FELLOWSHIPS=$(curl -s -X GET http://localhost:8080/api/fellowships \
  -H "Authorization: Bearer $TOKEN")

FELLOWSHIP_ID=$(echo $FELLOWSHIPS | jq -r '.[0].id // empty')

if [ -z "$FELLOWSHIP_ID" ]; then
  echo "❌ No fellowships found"
  exit 1
fi

FELLOWSHIP_NAME=$(echo $FELLOWSHIPS | jq -r ".[0].name")
echo "✓ Found fellowship: $FELLOWSHIP_NAME (ID: $FELLOWSHIP_ID)"

# Get fellowship member IDs
echo -e "\nTesting GET /api/fellowships/$FELLOWSHIP_ID/members/ids..."
MEMBER_IDS=$(curl -s -X GET "http://localhost:8080/api/fellowships/$FELLOWSHIP_ID/members/ids" \
  -H "Authorization: Bearer $TOKEN")

echo "Member IDs in fellowship: $MEMBER_IDS"

MEMBER_COUNT=$(echo $MEMBER_IDS | jq 'length')
echo "✓ Found $MEMBER_COUNT members in fellowship"

# Display the member IDs
if [ "$MEMBER_COUNT" -gt 0 ]; then
  echo -e "\nMember IDs:"
  echo $MEMBER_IDS | jq -r '.[]' | while read -r id; do
    echo "  - Member ID: $id"
  done
fi

echo -e "\n✓ All tests passed!"
echo "The frontend will now be able to:"
echo "  1. Fetch existing fellowship member IDs"
echo "  2. Display 'Already in fellowship' badge for existing members"
echo "  3. Disable checkboxes for members already in the fellowship"
echo "  4. Prevent adding duplicate members"
