#!/bin/bash

# ========================================
# Environment Variables Verification Script
# ========================================
# Checks if all required environment variables are set for production

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

ERRORS=0
WARNINGS=0

check_required() {
    VAR_NAME=$1
    if [ -z "${!VAR_NAME}" ]; then
        echo -e "${RED}✗${NC} $VAR_NAME is NOT set"
        ERRORS=$((ERRORS + 1))
        return 1
    else
        echo -e "${GREEN}✓${NC} $VAR_NAME is set"
        return 0
    fi
}

check_optional() {
    VAR_NAME=$1
    if [ -z "${!VAR_NAME}" ]; then
        echo -e "${YELLOW}⚠${NC} $VAR_NAME is NOT set (optional)"
        WARNINGS=$((WARNINGS + 1))
        return 1
    else
        echo -e "${GREEN}✓${NC} $VAR_NAME is set"
        return 0
    fi
}

check_secure() {
    VAR_NAME=$1
    VAR_VALUE="${!VAR_NAME}"
    MIN_LENGTH=$2

    if [ ${#VAR_VALUE} -lt $MIN_LENGTH ]; then
        echo -e "${RED}✗${NC} $VAR_NAME is too short (minimum $MIN_LENGTH characters)"
        ERRORS=$((ERRORS + 1))
        return 1
    else
        echo -e "${GREEN}✓${NC} $VAR_NAME has sufficient length"
        return 0
    fi
}

echo "=========================================="
echo "  Environment Variables Verification"
echo "=========================================="
echo ""

echo "Database Configuration:"
check_required "DATABASE_URL"
check_required "DATABASE_USERNAME"
check_required "DATABASE_PASSWORD"
check_secure "DATABASE_PASSWORD" 12
echo ""

echo "JWT Configuration:"
check_required "JWT_SECRET"
check_secure "JWT_SECRET" 32
echo ""

echo "Paystack Configuration:"
check_required "PAYSTACK_SECRET_KEY"
check_required "PAYSTACK_PUBLIC_KEY"
check_required "PAYSTACK_WEBHOOK_SECRET"

# Check if using test keys in production
if [[ "$PAYSTACK_SECRET_KEY" == sk_test_* ]]; then
    echo -e "${RED}✗${NC} PAYSTACK_SECRET_KEY is a TEST key! Use LIVE key (sk_live_*)"
    ERRORS=$((ERRORS + 1))
fi

if [[ "$PAYSTACK_PUBLIC_KEY" == pk_test_* ]]; then
    echo -e "${RED}✗${NC} PAYSTACK_PUBLIC_KEY is a TEST key! Use LIVE key (pk_live_*)"
    ERRORS=$((ERRORS + 1))
fi
echo ""

echo "QR Code Configuration:"
check_required "QR_SECRET_KEY"
QR_LENGTH=${#QR_SECRET_KEY}
if [ $QR_LENGTH -ne 16 ]; then
    echo -e "${RED}✗${NC} QR_SECRET_KEY must be exactly 16 characters (current: $QR_LENGTH)"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✓${NC} QR_SECRET_KEY has correct length (16 chars)"
fi
echo ""

echo "Application URLs:"
check_required "FRONTEND_URL"
check_required "APP_DOMAIN"
echo ""

echo "Email Configuration:"
check_optional "SMTP_HOST"
check_optional "SMTP_USERNAME"
check_optional "SMTP_PASSWORD"
check_optional "EMAIL_FROM"
echo ""

echo "SMS Configuration:"
check_optional "AFRICASTALKING_API_KEY"
check_optional "AFRICASTALKING_USERNAME"
echo ""

echo "File Upload Configuration:"
check_optional "UPLOAD_DIR"
echo ""

echo "=========================================="
echo "  Summary"
echo "=========================================="

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo "Ready for production deployment."
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ $WARNINGS warning(s)${NC}"
    echo "Optional variables are missing but deployment can proceed."
    exit 0
else
    echo -e "${RED}✗ $ERRORS error(s), $WARNINGS warning(s)${NC}"
    echo "Fix all errors before deploying to production."
    exit 1
fi
