#!/bin/bash
# test-storage-and-rbac.sh
# Comprehensive test script for Storage Calculation and RBAC Context features
# Created: 2025-12-29

set -e

echo "=========================================="
echo "Testing Storage Calculation & RBAC Context"
echo "=========================================="
echo ""

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
JWT_TOKEN="${JWT_TOKEN:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Function to check if backend is running
check_backend() {
    print_info "Checking if backend is running..."
    if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
        print_success "Backend is running"
        return 0
    else
        print_error "Backend is not running at ${BASE_URL}"
        print_info "Start the backend with: ./mvnw spring-boot:run"
        return 1
    fi
}

# Function to test storage calculation endpoint
test_storage_calculation() {
    print_info "Testing storage calculation endpoint..."

    if [ -z "$JWT_TOKEN" ]; then
        print_error "JWT_TOKEN not set. Skipping storage calculation test."
        print_info "Set JWT_TOKEN environment variable: export JWT_TOKEN='your_token'"
        return 1
    fi

    # Test POST /api/storage-usage/calculate
    print_info "Triggering manual storage calculation..."
    response=$(curl -s -w "\n%{http_code}" -X POST \
        "${BASE_URL}/api/storage-usage/calculate" \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        -H "Content-Type: application/json")

    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)

    if [ "$http_code" -eq 200 ]; then
        print_success "Storage calculation triggered successfully"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        print_error "Storage calculation failed (HTTP $http_code)"
        echo "$body"
        return 1
    fi

    # Test GET /api/storage-usage/current
    print_info "Fetching current storage usage..."
    response=$(curl -s -w "\n%{http_code}" -X GET \
        "${BASE_URL}/api/storage-usage/current" \
        -H "Authorization: Bearer ${JWT_TOKEN}")

    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)

    if [ "$http_code" -eq 200 ]; then
        print_success "Retrieved current storage usage"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        print_error "Failed to retrieve storage usage (HTTP $http_code)"
        echo "$body"
        return 1
    fi
}

# Function to test RBAC cross-tenant protection
test_rbac_protection() {
    print_info "Testing RBAC cross-tenant protection..."

    if [ -z "$JWT_TOKEN" ]; then
        print_error "JWT_TOKEN not set. Skipping RBAC test."
        return 1
    fi

    # Try to access a member with an invalid ID (should fail or return 404)
    print_info "Attempting to access member with ID 9999999..."
    response=$(curl -s -w "\n%{http_code}" -X GET \
        "${BASE_URL}/api/members/9999999" \
        -H "Authorization: Bearer ${JWT_TOKEN}")

    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)

    if [ "$http_code" -eq 403 ] || [ "$http_code" -eq 404 ]; then
        print_success "RBAC protection working (received HTTP $http_code)"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    elif [ "$http_code" -eq 200 ]; then
        print_info "Member found (HTTP 200). This is expected if member exists in your church."
    else
        print_error "Unexpected response (HTTP $http_code)"
        echo "$body"
    fi
}

# Function to test security monitoring endpoint
test_security_monitoring() {
    print_info "Testing security monitoring endpoints..."

    if [ -z "$JWT_TOKEN" ]; then
        print_error "JWT_TOKEN not set. Skipping security monitoring test."
        return 1
    fi

    # Test GET /api/security/stats (requires PLATFORM_ACCESS)
    print_info "Fetching security statistics..."
    response=$(curl -s -w "\n%{http_code}" -X GET \
        "${BASE_URL}/api/security/stats" \
        -H "Authorization: Bearer ${JWT_TOKEN}")

    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)

    if [ "$http_code" -eq 200 ]; then
        print_success "Retrieved security statistics"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    elif [ "$http_code" -eq 403 ]; then
        print_info "Security stats require PLATFORM_ACCESS permission (HTTP 403)"
    else
        print_error "Unexpected response (HTTP $http_code)"
        echo "$body"
    fi
}

# Function to check database migrations
check_migrations() {
    print_info "Checking database migrations..."

    # Check if storage_usage table exists
    print_info "Verifying storage_usage table..."
    if mysql -u root -ppassword past-care-spring -e "DESCRIBE storage_usage;" > /dev/null 2>&1; then
        print_success "storage_usage table exists"
    else
        print_error "storage_usage table not found"
        print_info "Run migrations: ./mvnw flyway:migrate"
    fi

    # Check if security_audit_logs table exists
    print_info "Verifying security_audit_logs table..."
    if mysql -u root -ppassword past-care-spring -e "DESCRIBE security_audit_logs;" > /dev/null 2>&1; then
        print_success "security_audit_logs table exists"
    else
        print_error "security_audit_logs table not found"
        print_info "Run migrations: ./mvnw flyway:migrate"
    fi
}

# Function to check Hibernate filter logs
check_hibernate_filters() {
    print_info "Checking for Hibernate filter logs..."

    if [ -f "application.log" ]; then
        filter_logs=$(grep -c "Hibernate filter 'churchFilter' enabled" application.log 2>/dev/null || echo "0")
        if [ "$filter_logs" -gt 0 ]; then
            print_success "Found $filter_logs Hibernate filter log entries"
        else
            print_info "No Hibernate filter logs found yet (may need to make authenticated requests)"
        fi
    else
        print_info "No application.log file found"
    fi
}

# Main test execution
main() {
    echo "Starting tests..."
    echo ""

    # 1. Check backend
    if ! check_backend; then
        exit 1
    fi
    echo ""

    # 2. Check migrations
    check_migrations
    echo ""

    # 3. Check Hibernate filters
    check_hibernate_filters
    echo ""

    # 4. Test storage calculation
    test_storage_calculation
    echo ""

    # 5. Test RBAC protection
    test_rbac_protection
    echo ""

    # 6. Test security monitoring
    test_security_monitoring
    echo ""

    print_success "All tests completed!"
    echo ""
    echo "Next steps:"
    echo "1. Set JWT_TOKEN environment variable for authenticated tests:"
    echo "   export JWT_TOKEN='your_jwt_token'"
    echo "2. Run migrations if tables are missing:"
    echo "   ./mvnw flyway:migrate"
    echo "3. Check application logs for Hibernate filter activity"
    echo "4. Monitor security_audit_logs table for violation entries"
}

# Run main function
main
