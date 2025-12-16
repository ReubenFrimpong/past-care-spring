#!/bin/bash

# Simple dashboard test using HttpOnly cookies

COOKIE_JAR="/tmp/pastcare_cookies.txt"

# Step 1: Login
echo "Step 1: Login..."
curl -s http://localhost:8080/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","rememberMe":false}' \
  -c "$COOKIE_JAR" \
  > /tmp/login.json

cat /tmp/login.json | jq '.'

# Check if login was successful by checking the response
USER=$(cat /tmp/login.json | jq -r '.user')

if [ -z "$USER" ] || [ "$USER" = "null" ]; then
  echo "Login failed!"
  exit 1
fi

echo ""
echo "Login successful! Cookies saved to $COOKIE_JAR"
echo ""

# Step 2: Test stats endpoint
echo "Step 2: Test /api/dashboard/stats..."
curl -s http://localhost:8080/api/dashboard/stats \
  -b "$COOKIE_JAR" | jq '.'

echo ""

# Step 3: Test complete dashboard
echo "Step 3: Test /api/dashboard..."
curl -s http://localhost:8080/api/dashboard \
  -b "$COOKIE_JAR" | jq '.'
