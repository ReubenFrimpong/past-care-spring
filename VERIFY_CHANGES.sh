#!/bin/bash

echo "=========================================="
echo "Form Standardization - Verification Script"
echo "=========================================="
echo ""

FRONTEND_DIR="/home/reuben/Documents/workspace/past-care-spring-frontend"

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} File exists: $1"
        return 0
    else
        echo -e "${RED}✗${NC} File missing: $1"
        return 1
    fi
}

check_content() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} Found in $1: $2"
        return 0
    else
        echo -e "${RED}✗${NC} Not found in $1: $2"
        return 1
    fi
}

echo -e "${BLUE}1. Checking Member Detail Page Files...${NC}"
check_file "$FRONTEND_DIR/src/app/pages/member-detail-page/member-detail-page.ts"
check_file "$FRONTEND_DIR/src/app/pages/member-detail-page/member-detail-page.html"
check_file "$FRONTEND_DIR/src/app/pages/member-detail-page/member-detail-page.css"
echo ""

echo -e "${BLUE}2. Checking Shared CSS Framework...${NC}"
check_file "$FRONTEND_DIR/src/styles/form-standards.css"
check_content "$FRONTEND_DIR/src/styles/form-standards.css" "form-section-header"
check_content "$FRONTEND_DIR/src/styles/form-standards.css" "form-row"
check_content "$FRONTEND_DIR/src/styles/form-standards.css" "form-group"
check_content "$FRONTEND_DIR/src/styles/form-standards.css" "dialog-header"
check_content "$FRONTEND_DIR/src/styles/form-standards.css" "dialog-footer"
echo ""

echo -e "${BLUE}3. Checking Member Detail Page Route...${NC}"
check_content "$FRONTEND_DIR/src/app/app.routes.ts" "members/:id"
check_content "$FRONTEND_DIR/src/app/app.routes.ts" "MemberDetailPage"
echo ""

echo -e "${BLUE}4. Verifying Embedded Components Removed from Member Form...${NC}"
if ! grep -q "app-lifecycle-events" "$FRONTEND_DIR/src/app/components/member-form/member-form.component.html" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} app-lifecycle-events removed from member form HTML"
else
    echo -e "${RED}✗${NC} app-lifecycle-events still in member form HTML"
fi

if ! grep -q "app-communication-logs" "$FRONTEND_DIR/src/app/components/member-form/member-form.component.html" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} app-communication-logs removed from member form HTML"
else
    echo -e "${RED}✗${NC} app-communication-logs still in member form HTML"
fi

if ! grep -q "app-confidential-notes" "$FRONTEND_DIR/src/app/components/member-form/member-form.component.html" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} app-confidential-notes removed from member form HTML"
else
    echo -e "${RED}✗${NC} app-confidential-notes still in member form HTML"
fi

if ! grep -q "LifecycleEventsComponent" "$FRONTEND_DIR/src/app/components/member-form/member-form.component.ts" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} LifecycleEventsComponent import removed from member form TS"
else
    echo -e "${RED}✗${NC} LifecycleEventsComponent import still in member form TS"
fi
echo ""

echo -e "${BLUE}5. Checking Lifecycle Events Standardization...${NC}"
check_content "$FRONTEND_DIR/src/app/components/lifecycle-events/lifecycle-events.component.html" "form-section-header"
check_content "$FRONTEND_DIR/src/app/components/lifecycle-events/lifecycle-events.component.html" "dialog-header"
check_content "$FRONTEND_DIR/src/app/components/lifecycle-events/lifecycle-events.component.html" "form-row"
check_content "$FRONTEND_DIR/src/app/components/lifecycle-events/lifecycle-events.component.html" "form-group"
check_content "$FRONTEND_DIR/src/app/components/lifecycle-events/lifecycle-events.component.css" "@import.*form-standards.css"
echo ""

echo -e "${BLUE}6. Checking Communication Logs Standardization...${NC}"
check_content "$FRONTEND_DIR/src/app/components/communication-logs/communication-logs.component.html" "form-section-header"
check_content "$FRONTEND_DIR/src/app/components/communication-logs/communication-logs.component.css" "@import.*form-standards.css"
echo ""

echo -e "${BLUE}7. Checking Confidential Notes Standardization...${NC}"
check_content "$FRONTEND_DIR/src/app/components/confidential-notes/confidential-notes.component.html" "form-section-header"
check_content "$FRONTEND_DIR/src/app/components/confidential-notes/confidential-notes.component.css" "@import.*form-standards.css"
# Check that red theme was replaced (should NOT find old red color)
if ! grep -q "#dc2626" "$FRONTEND_DIR/src/app/components/confidential-notes/confidential-notes.component.css" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} Red theme color (#dc2626) removed from confidential notes"
else
    echo -e "${RED}✗${NC} Red theme color (#dc2626) still in confidential notes"
fi
# Check for warning orange theme instead
check_content "$FRONTEND_DIR/src/app/components/confidential-notes/confidential-notes.component.css" "#f59e0b"
echo ""

echo -e "${BLUE}8. Checking Members Page Navigation Update...${NC}"
check_content "$FRONTEND_DIR/src/app/members-page/members-page.ts" "router.navigate.*members.*member.id"
echo ""

echo -e "${BLUE}9. Verifying TypeScript Compilation...${NC}"
cd "$FRONTEND_DIR"
if npx tsc --noEmit 2>&1 | grep -q "error TS"; then
    echo -e "${RED}✗${NC} TypeScript compilation has errors"
else
    echo -e "${GREEN}✓${NC} TypeScript compilation successful (no errors)"
fi
echo ""

echo "=========================================="
echo "Verification Complete!"
echo "=========================================="
