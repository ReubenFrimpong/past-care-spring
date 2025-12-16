#!/bin/bash

# Dashboard API Testing Script
# Tests all dashboard endpoints with cookie-based authentication

BASE_URL="http://localhost:8080/api"
COOKIE_JAR="/tmp/pastcare_cookies.txt"

echo "=== Dashboard API Test ==="
echo ""

# Step 1: Login to get authentication cookie
echo "1. Logging in to get authentication cookie..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "rememberMe": false
  }' \
  -c "$COOKIE_JAR")

# Check if login was successful by checking the user field
USER=$(echo $LOGIN_RESPONSE | jq -r '.user')

if [ -z "$USER" ] || [ "$USER" = "null" ]; then
  echo "❌ Login failed. Response:"
  echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"
  exit 1
fi

echo "✅ Login successful!"
echo "User: $USER"
echo "Cookies saved to $COOKIE_JAR"
echo ""

# Step 2: Test complete dashboard endpoint
echo "2. Testing GET /api/dashboard (Complete Dashboard)..."
DASHBOARD_RESPONSE=$(curl -s -X GET "${BASE_URL}/dashboard" \
  -b "$COOKIE_JAR")

echo "Response:"
echo "$DASHBOARD_RESPONSE" | jq '.' 2>/dev/null || echo "$DASHBOARD_RESPONSE"
echo ""

# Step 3: Test stats endpoint
echo "3. Testing GET /api/dashboard/stats (Statistics Only)..."
STATS_RESPONSE=$(curl -s -X GET "${BASE_URL}/dashboard/stats" \
  -b "$COOKIE_JAR")

echo "Response:"
echo "$STATS_RESPONSE" | jq '.' 2>/dev/null || echo "$STATS_RESPONSE"
echo ""

# Step 4: Test pastoral care endpoint
echo "4. Testing GET /api/dashboard/pastoral-care (Pastoral Care Needs)..."
CARE_RESPONSE=$(curl -s -X GET "${BASE_URL}/dashboard/pastoral-care" \
  -b "$COOKIE_JAR")

echo "Response:"
echo "$CARE_RESPONSE" | jq '.' 2>/dev/null || echo "$CARE_RESPONSE"
echo ""

# Step 5: Test events endpoint
echo "5. Testing GET /api/dashboard/events (Upcoming Events)..."
EVENTS_RESPONSE=$(curl -s -X GET "${BASE_URL}/dashboard/events" \
  -b "$COOKIE_JAR")

echo "Response:"
echo "$EVENTS_RESPONSE" | jq '.' 2>/dev/null || echo "$EVENTS_RESPONSE"
echo ""

# Step 6: Test activities endpoint
echo "6. Testing GET /api/dashboard/activities (Recent Activities)..."
ACTIVITIES_RESPONSE=$(curl -s -X GET "${BASE_URL}/dashboard/activities" \
  -b "$COOKIE_JAR")

echo "Response:"
echo "$ACTIVITIES_RESPONSE" | jq '.' 2>/dev/null || echo "$ACTIVITIES_RESPONSE"
echo ""

echo "=== Test Complete ==="
