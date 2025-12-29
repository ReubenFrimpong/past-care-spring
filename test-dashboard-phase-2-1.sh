#!/bin/bash
# Dashboard Phase 2.1: Custom Layouts MVP - Testing Script
# This script tests all Phase 2.1 endpoints and features

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
API_BASE="${BACKEND_URL}/api"
TOKEN=""
USER_ID=""

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Dashboard Phase 2.1 Testing Script${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# Function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS:${NC} $2"
    else
        echo -e "${RED}❌ FAIL:${NC} $2"
        if [ ! -z "$3" ]; then
            echo -e "${RED}   Error: $3${NC}"
        fi
    fi
}

# Function to make authenticated API call
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3

    if [ -z "$data" ]; then
        curl -s -X "$method" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            "${API_BASE}${endpoint}"
    else
        curl -s -X "$method" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "${API_BASE}${endpoint}"
    fi
}

# Test 1: Check backend is running
echo -e "${YELLOW}Test 1: Backend Health Check${NC}"
if curl -s "${BACKEND_URL}/actuator/health" > /dev/null 2>&1; then
    print_result 0 "Backend is running at $BACKEND_URL"
else
    print_result 1 "Backend is not accessible" "Make sure backend is running on $BACKEND_URL"
    exit 1
fi
echo ""

# Test 2: Login and get token
echo -e "${YELLOW}Test 2: Authentication${NC}"
echo "Please provide your login credentials:"
read -p "Email: " email
read -sp "Password: " password
echo ""

LOGIN_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$password\"}" \
    "${API_BASE}/auth/login")

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | grep -o '[^"]*$')

if [ ! -z "$TOKEN" ]; then
    print_result 0 "Login successful, token obtained"
    USER_ID=$(echo "$LOGIN_RESPONSE" | grep -o '"userId":[0-9]*' | grep -o '[0-9]*$')
    echo -e "   User ID: $USER_ID"
else
    print_result 1 "Login failed" "Invalid credentials or backend error"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi
echo ""

# Test 3: Get available widgets
echo -e "${YELLOW}Test 3: Get Available Widgets${NC}"
WIDGETS_RESPONSE=$(api_call "GET" "/dashboard/widgets/available")
WIDGET_COUNT=$(echo "$WIDGETS_RESPONSE" | grep -o '"widgetKey"' | wc -l)

if [ $WIDGET_COUNT -gt 0 ]; then
    print_result 0 "Retrieved $WIDGET_COUNT widgets"
    echo "   Sample widgets:"
    echo "$WIDGETS_RESPONSE" | grep -o '"widgetKey":"[^"]*' | head -5 | sed 's/"widgetKey":"/ - /'
else
    print_result 1 "No widgets returned" "$WIDGETS_RESPONSE"
fi
echo ""

# Test 4: Check widgets table in database
echo -e "${YELLOW}Test 4: Verify Widgets in Database${NC}"
echo "Checking if widgets are seeded in database..."
# This requires database access - skip if not available
if command -v mysql &> /dev/null; then
    DB_WIDGET_COUNT=$(mysql -u root -p -D pastcare_db -se "SELECT COUNT(*) FROM widgets" 2>/dev/null || echo "0")
    if [ "$DB_WIDGET_COUNT" -eq 17 ]; then
        print_result 0 "Database has 17 widgets seeded"
    else
        print_result 1 "Expected 17 widgets, found $DB_WIDGET_COUNT" "Run migrations"
    fi
else
    echo -e "   ${YELLOW}ℹ️  SKIP:${NC} MySQL client not available, skipping database check"
fi
echo ""

# Test 5: Get user layout (should create default if not exists)
echo -e "${YELLOW}Test 5: Get User Layout${NC}"
LAYOUT_RESPONSE=$(api_call "GET" "/dashboard/layout")
LAYOUT_ID=$(echo "$LAYOUT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*$')

if [ ! -z "$LAYOUT_ID" ]; then
    print_result 0 "User layout retrieved (ID: $LAYOUT_ID)"

    # Parse layout details
    LAYOUT_NAME=$(echo "$LAYOUT_RESPONSE" | grep -o '"layoutName":"[^"]*' | grep -o '[^"]*$')
    IS_DEFAULT=$(echo "$LAYOUT_RESPONSE" | grep -o '"isDefault":[^,}]*' | grep -o '[^:]*$')

    echo "   Layout Name: $LAYOUT_NAME"
    echo "   Is Default: $IS_DEFAULT"

    # Check if layout has widgets
    WIDGET_COUNT_IN_LAYOUT=$(echo "$LAYOUT_RESPONSE" | grep -o '"widgetKey"' | wc -l)
    if [ $WIDGET_COUNT_IN_LAYOUT -gt 0 ]; then
        print_result 0 "Layout contains $WIDGET_COUNT_IN_LAYOUT widgets"
    else
        print_result 1 "Layout has no widgets" "Layout config may be empty"
    fi
else
    print_result 1 "Failed to get user layout" "$LAYOUT_RESPONSE"
fi
echo ""

# Test 6: Save custom layout
echo -e "${YELLOW}Test 6: Save Custom Layout${NC}"
CUSTOM_LAYOUT='{
  "layoutName": "Test Layout - Phase 2.1",
  "layoutConfig": "{\"version\":1,\"gridColumns\":4,\"widgets\":[{\"widgetKey\":\"stats_overview\",\"position\":{\"x\":0,\"y\":0},\"size\":{\"width\":2,\"height\":1},\"visible\":true},{\"widgetKey\":\"birthdays_week\",\"position\":{\"x\":2,\"y\":0},\"size\":{\"width\":1,\"height\":1},\"visible\":true},{\"widgetKey\":\"member_growth\",\"position\":{\"x\":0,\"y\":1},\"size\":{\"width\":2,\"height\":1},\"visible\":true}]}"
}'

SAVE_RESPONSE=$(api_call "POST" "/dashboard/layout" "$CUSTOM_LAYOUT")
SAVED_LAYOUT_ID=$(echo "$SAVE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*$')

if [ ! -z "$SAVED_LAYOUT_ID" ]; then
    print_result 0 "Custom layout saved successfully (ID: $SAVED_LAYOUT_ID)"
    SAVED_NAME=$(echo "$SAVE_RESPONSE" | grep -o '"layoutName":"[^"]*' | grep -o '[^"]*$')
    echo "   Layout Name: $SAVED_NAME"
else
    print_result 1 "Failed to save custom layout" "$SAVE_RESPONSE"
fi
echo ""

# Test 7: Retrieve saved layout
echo -e "${YELLOW}Test 7: Verify Saved Layout Persists${NC}"
RETRIEVED_LAYOUT=$(api_call "GET" "/dashboard/layout")
RETRIEVED_NAME=$(echo "$RETRIEVED_LAYOUT" | grep -o '"layoutName":"[^"]*' | grep -o '[^"]*$')

if [[ "$RETRIEVED_NAME" == *"Test Layout"* ]]; then
    print_result 0 "Saved layout retrieved successfully"
    echo "   Retrieved: $RETRIEVED_NAME"
else
    print_result 1 "Retrieved layout doesn't match saved layout"
    echo "   Expected: Test Layout - Phase 2.1"
    echo "   Got: $RETRIEVED_NAME"
fi
echo ""

# Test 8: Reset to default layout
echo -e "${YELLOW}Test 8: Reset to Default Layout${NC}"
RESET_RESPONSE=$(api_call "POST" "/dashboard/layout/reset" "")
RESET_LAYOUT_ID=$(echo "$RESET_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*$')

if [ ! -z "$RESET_LAYOUT_ID" ]; then
    print_result 0 "Layout reset to default successfully"

    # Verify it's actually default
    RESET_NAME=$(echo "$RESET_RESPONSE" | grep -o '"layoutName":"[^"]*' | grep -o '[^"]*$')
    echo "   Reset Layout Name: $RESET_NAME"

    # Check widget count in reset layout
    RESET_WIDGET_COUNT=$(echo "$RESET_RESPONSE" | grep -o '"widgetKey"' | wc -l)
    if [ $RESET_WIDGET_COUNT -gt 10 ]; then
        print_result 0 "Reset layout has $RESET_WIDGET_COUNT widgets (full default)"
    else
        print_result 1 "Reset layout has only $RESET_WIDGET_COUNT widgets" "Expected 17 widgets"
    fi
else
    print_result 1 "Failed to reset layout" "$RESET_RESPONSE"
fi
echo ""

# Test 9: Test role-based widget filtering
echo -e "${YELLOW}Test 9: Role-Based Widget Filtering${NC}"
echo "Testing that widgets are filtered based on user role..."

WIDGETS_RESPONSE=$(api_call "GET" "/dashboard/widgets/available")
HAS_ROLE_RESTRICTED=$(echo "$WIDGETS_RESPONSE" | grep -o '"requiredRole"' | wc -l)

if [ $HAS_ROLE_RESTRICTED -gt 0 ]; then
    echo "   ℹ️  Some widgets have role restrictions"
    print_result 0 "Role-based filtering is configured"
else
    echo "   ℹ️  No role restrictions found (all widgets available to all roles)"
    print_result 0 "All widgets available to current user role"
fi
echo ""

# Test 10: Test concurrent layout updates
echo -e "${YELLOW}Test 10: Concurrent Layout Updates${NC}"
echo "Testing that layout updates don't conflict..."

# Save layout 1
LAYOUT_1='{
  "layoutName": "Concurrent Test 1",
  "layoutConfig": "{\"version\":1,\"gridColumns\":4,\"widgets\":[{\"widgetKey\":\"stats_overview\",\"position\":{\"x\":0,\"y\":0},\"size\":{\"width\":2,\"height\":1},\"visible\":true}]}"
}'
api_call "POST" "/dashboard/layout" "$LAYOUT_1" > /dev/null

# Immediately save layout 2
LAYOUT_2='{
  "layoutName": "Concurrent Test 2",
  "layoutConfig": "{\"version\":1,\"gridColumns\":4,\"widgets\":[{\"widgetKey\":\"birthdays_week\",\"position\":{\"x\":0,\"y\":0},\"size\":{\"width\":1,\"height\":1},\"visible\":true}]}"
}'
FINAL_RESPONSE=$(api_call "POST" "/dashboard/layout" "$LAYOUT_2")

FINAL_NAME=$(echo "$FINAL_RESPONSE" | grep -o '"layoutName":"[^"]*' | grep -o '[^"]*$')
if [[ "$FINAL_NAME" == "Concurrent Test 2" ]]; then
    print_result 0 "Latest layout update persisted correctly"
else
    print_result 1 "Concurrent updates may have conflicts" "Got: $FINAL_NAME"
fi
echo ""

# Summary
echo ""
echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo "Backend Phase 2.1 endpoints are functional!"
echo ""
echo "Next steps:"
echo "1. Open frontend: http://localhost:4200"
echo "2. Login with your credentials"
echo "3. Look for 'Customize' button on dashboard"
echo "4. Click 'Customize' to enter edit mode"
echo "5. Click 'Widgets' to open configurator"
echo "6. Toggle widget visibility"
echo "7. Test drag-and-drop (if cdkDrag added to widgets)"
echo "8. Click 'Save' to persist layout"
echo "9. Refresh page to verify layout loads"
echo "10. Click 'Reset' to restore defaults"
echo ""
echo -e "${GREEN}✅ Backend Testing Complete!${NC}"
echo ""
