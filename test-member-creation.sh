#!/bin/bash

# Test member creation with empty fellowshipIds

# First, get auth token
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@pastcare.com",
    "password": "admin123"
  }')

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
CHURCH_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.user.church.id')

echo "Token: $TOKEN"
echo "Church ID: $CHURCH_ID"

# Test 1: Create member with empty fellowshipIds array
echo -e "\n=== Test 1: Create member with empty fellowshipIds array ==="
curl -v -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"firstName\": \"John\",
    \"lastName\": \"Doe\",
    \"sex\": \"Male\",
    \"phoneNumber\": \"+233201234567\",
    \"maritalStatus\": \"Single\",
    \"churchId\": $CHURCH_ID,
    \"fellowshipIds\": []
  }" 2>&1 | tee /tmp/test-member-creation.log

echo -e "\n=== Test 2: Create member with null fellowshipIds ==="
curl -v -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"firstName\": \"Jane\",
    \"lastName\": \"Smith\",
    \"sex\": \"Female\",
    \"phoneNumber\": \"+233201234568\",
    \"maritalStatus\": \"Single\",
    \"churchId\": $CHURCH_ID,
    \"fellowshipIds\": null
  }" 2>&1
