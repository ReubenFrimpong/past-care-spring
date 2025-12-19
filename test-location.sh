#!/bin/bash

# Test script for Location functionality
# This script tests the Location entity and API endpoints

BASE_URL="http://localhost:8080/api"

echo "========================================="
echo "Location Feature Test Script"
echo "========================================="
echo ""

# Step 1: Login and get JWT token
echo "Step 1: Logging in to get JWT token..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Login failed. Please check credentials."
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo "✓ Login successful"
echo ""

# Step 2: Create a test member with location data
echo "Step 2: Creating member with Nominatim location data..."
CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/members" \
  -H "Content-Type: application/json" \
  -H "Cookie: access_token=${TOKEN}" \
  -d '{
    "firstName": "Test",
    "lastName": "Location",
    "sex": "Male",
    "phoneNumber": "0241234567",
    "maritalStatus": "Single",
    "areaOfResidence": "Dansoman, Accra",
    "coordinates": "5.5558,-0.2601",
    "nominatimAddress": {
      "suburb": "Dansoman",
      "city": "Accra",
      "state_district": "Accra Metropolitan",
      "state": "Greater Accra Region",
      "country": "Ghana"
    }
  }')

MEMBER_ID=$(echo $CREATE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$MEMBER_ID" ]; then
    echo "❌ Member creation failed"
    echo "Response: $CREATE_RESPONSE"
    exit 1
fi

echo "✓ Member created with ID: $MEMBER_ID"
echo ""

# Step 3: Retrieve the member and check location data
echo "Step 3: Retrieving member to verify location data..."
MEMBER_RESPONSE=$(curl -s -X GET "${BASE_URL}/members/${MEMBER_ID}" \
  -H "Cookie: access_token=${TOKEN}")

echo "Member Response:"
echo "$MEMBER_RESPONSE" | jq '.'
echo ""

# Check if location object exists
HAS_LOCATION=$(echo $MEMBER_RESPONSE | grep -o '"location":{' | wc -l)

if [ "$HAS_LOCATION" -eq "1" ]; then
    echo "✓ Location object found in response"

    # Extract location details
    LOCATION_DISPLAY=$(echo $MEMBER_RESPONSE | grep -o '"displayName":"[^"]*' | cut -d'"' -f4)
    LOCATION_CITY=$(echo $MEMBER_RESPONSE | grep -o '"city":"[^"]*' | cut -d'"' -f4)
    LOCATION_REGION=$(echo $MEMBER_RESPONSE | grep -o '"region":"[^"]*' | cut -d'"' -f4)

    echo "  Display Name: $LOCATION_DISPLAY"
    echo "  City: $LOCATION_CITY"
    echo "  Region: $LOCATION_REGION"
else
    echo "❌ Location object not found in response"
fi
echo ""

# Step 4: Create second member with same coordinates (test deduplication)
echo "Step 4: Creating second member with same coordinates to test deduplication..."
CREATE_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/members" \
  -H "Content-Type: application/json" \
  -H "Cookie: access_token=${TOKEN}" \
  -d '{
    "firstName": "Another",
    "lastName": "TestUser",
    "sex": "Female",
    "phoneNumber": "0247654321",
    "maritalStatus": "Married",
    "areaOfResidence": "Dansoman, Accra",
    "coordinates": "5.5558,-0.2601",
    "nominatimAddress": {
      "suburb": "Dansoman",
      "city": "Accra",
      "state_district": "Accra Metropolitan",
      "state": "Greater Accra Region",
      "country": "Ghana"
    }
  }')

MEMBER_ID_2=$(echo $CREATE_RESPONSE_2 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$MEMBER_ID_2" ]; then
    echo "❌ Second member creation failed"
else
    echo "✓ Second member created with ID: $MEMBER_ID_2"
fi
echo ""

# Step 5: Check database for location deduplication
echo "Step 5: Checking database for location records..."
LOCATION_COUNT=$(mysql -u root -ppassword past-care-spring -se "SELECT COUNT(*) FROM locations WHERE coordinates = '5.5558,-0.2601';")

echo "Number of location records with coordinates '5.5558,-0.2601': $LOCATION_COUNT"

if [ "$LOCATION_COUNT" -eq "1" ]; then
    echo "✓ Location deduplication working correctly (only 1 record)"
else
    echo "❌ Location deduplication not working (found $LOCATION_COUNT records)"
fi
echo ""

# Step 6: Test location statistics endpoint
echo "Step 6: Testing location statistics endpoint..."
STATS_RESPONSE=$(curl -s -X GET "${BASE_URL}/dashboard/location-stats" \
  -H "Cookie: access_token=${TOKEN}")

echo "Location Statistics Response:"
echo "$STATS_RESPONSE" | jq '.'
echo ""

# Step 7: Cleanup - Delete test members
echo "Step 7: Cleaning up test data..."
curl -s -X DELETE "${BASE_URL}/members/${MEMBER_ID}" \
  -H "Cookie: access_token=${TOKEN}" > /dev/null

if [ ! -z "$MEMBER_ID_2" ]; then
    curl -s -X DELETE "${BASE_URL}/members/${MEMBER_ID_2}" \
      -H "Cookie: access_token=${TOKEN}" > /dev/null
fi

echo "✓ Test members deleted"
echo ""

echo "========================================="
echo "Test completed!"
echo "========================================="
