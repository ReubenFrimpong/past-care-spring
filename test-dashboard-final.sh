#!/bin/bash

echo "=== Dashboard API Test with Cookies ==="
echo ""

# Login and save cookies
echo "1. Login..."
curl -s -c /tmp/cookies.txt http://localhost:8080/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","rememberMe":false}' | jq '.'

echo ""
echo "2. Cookies saved:"
cat /tmp/cookies.txt
echo ""

# Test dashboard stats
echo "3. Test /api/dashboard/stats with cookies..."
curl -s -b /tmp/cookies.txt http://localhost:8080/api/dashboard/stats | jq '.'
echo ""

# Test full dashboard
echo "4. Test /api/dashboard with cookies..."
curl -s -b /tmp/cookies.txt http://localhost:8080/api/dashboard | jq '.'
echo ""

# Test pastoral care
echo "5. Test /api/dashboard/pastoral-care with cookies..."
curl -s -b /tmp/cookies.txt http://localhost:8080/api/dashboard/pastoral-care | jq '.'
echo ""

# Test events
echo "6. Test /api/dashboard/events with cookies..."
curl -s -b /tmp/cookies.txt http://localhost:8080/api/dashboard/events | jq '.'
echo ""

# Test activities
echo "7. Test /api/dashboard/activities with cookies..."
curl -s -b /tmp/cookies.txt http://localhost:8080/api/dashboard/activities | jq '.'

echo ""
echo "=== Test Complete ==="
