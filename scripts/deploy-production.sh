#!/bin/bash

# ========================================
# PastCare Production Deployment Script
# ========================================
# This script safely deploys PastCare to production with all safety checks
# Usage: ./scripts/deploy-production.sh

set -e  # Exit on any error

# Configuration
APP_NAME="pastcare-spring"
APP_JAR="target/${APP_NAME}-0.0.1-SNAPSHOT.jar"
APP_PORT="${APP_PORT:-8080}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
PID_FILE="/var/run/${APP_NAME}.pid"
LOG_DIR="/var/log/pastcare"
LOG_FILE="$LOG_DIR/application.log"

# Database backup settings
BACKUP_DIR="${BACKUP_DIR:-/var/backups/pastcare}"
DB_NAME="${DB_NAME:-pastcare_production}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

confirm() {
    read -p "$1 (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "Deployment cancelled by user"
        exit 1
    fi
}

# Banner
echo ""
echo "=========================================="
echo "  PastCare Production Deployment"
echo "=========================================="
echo ""

# Step 1: Pre-deployment checks
log_step "Step 1: Running pre-deployment checks..."

# Check if running as correct user (not root)
if [ "$EUID" -eq 0 ]; then
    log_warn "Running as root is not recommended"
    confirm "Continue anyway?"
fi

# Check if JAR file exists
if [ ! -f "$APP_JAR" ]; then
    log_error "JAR file not found: $APP_JAR"
    log_info "Build the application first: ./mvnw clean package"
    exit 1
fi
log_info "JAR file found: $APP_JAR"

# Check JAR file age
JAR_AGE_MINUTES=$(( ($(date +%s) - $(stat -c %Y "$APP_JAR")) / 60 ))
if [ $JAR_AGE_MINUTES -gt 60 ]; then
    log_warn "JAR file is $JAR_AGE_MINUTES minutes old. Is this the latest build?"
    confirm "Continue with this JAR?"
fi

# Check environment variables
log_info "Checking required environment variables..."
MISSING_VARS=()
[ -z "$DATABASE_URL" ] && MISSING_VARS+=("DATABASE_URL")
[ -z "$DATABASE_PASSWORD" ] && MISSING_VARS+=("DATABASE_PASSWORD")
[ -z "$JWT_SECRET" ] && MISSING_VARS+=("JWT_SECRET")
[ -z "$PAYSTACK_SECRET_KEY" ] && MISSING_VARS+=("PAYSTACK_SECRET_KEY")

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    log_error "Missing required environment variables:"
    for var in "${MISSING_VARS[@]}"; do
        echo "  - $var"
    done
    exit 1
fi
log_info "All required environment variables are set"

# Create directories
mkdir -p "$LOG_DIR"
mkdir -p "$BACKUP_DIR"

# Step 2: Database backup
log_step "Step 2: Creating database backup..."

if [ -z "$DB_PASSWORD" ]; then
    log_warn "DB_PASSWORD not set, skipping backup"
    confirm "Deploy without backup? (NOT RECOMMENDED)"
else
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    BACKUP_FILE="$BACKUP_DIR/pre_deploy_backup_$TIMESTAMP.sql.gz"

    log_info "Backing up database to: $BACKUP_FILE"
    mysqldump \
        --user="$DB_USER" \
        --password="$DB_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        "$DB_NAME" | gzip > "$BACKUP_FILE"

    if [ $? -eq 0 ]; then
        BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
        log_info "Backup created successfully ($BACKUP_SIZE)"
    else
        log_error "Backup failed!"
        confirm "Continue deployment without backup? (NOT RECOMMENDED)"
    fi
fi

# Step 3: Stop current application
log_step "Step 3: Stopping current application..."

if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        log_info "Stopping application (PID: $OLD_PID)..."
        kill "$OLD_PID"

        # Wait for graceful shutdown (max 30 seconds)
        for i in {1..30}; do
            if ! ps -p "$OLD_PID" > /dev/null 2>&1; then
                log_info "Application stopped gracefully"
                break
            fi
            sleep 1
        done

        # Force kill if still running
        if ps -p "$OLD_PID" > /dev/null 2>&1; then
            log_warn "Force killing application..."
            kill -9 "$OLD_PID"
        fi
    fi
    rm -f "$PID_FILE"
fi

# Check if port is still in use
if lsof -Pi :$APP_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_warn "Port $APP_PORT is still in use"
    log_info "Killing process on port $APP_PORT..."
    lsof -ti:$APP_PORT | xargs kill -9 2>/dev/null || true
    sleep 2
fi

log_info "Port $APP_PORT is free"

# Step 4: Deploy new version
log_step "Step 4: Starting new application version..."

log_info "Starting application with profile: $SPRING_PROFILE"
log_info "Log file: $LOG_FILE"

# Start application in background
nohup java \
    -jar \
    -Dspring.profiles.active="$SPRING_PROFILE" \
    -Xms512m \
    -Xmx2g \
    "$APP_JAR" \
    > "$LOG_FILE" 2>&1 &

APP_PID=$!
echo $APP_PID > "$PID_FILE"

log_info "Application started (PID: $APP_PID)"

# Step 5: Wait for startup
log_step "Step 5: Waiting for application to start..."

MAX_WAIT=60
WAIT_COUNT=0

while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
    if curl -s "http://localhost:$APP_PORT/actuator/health" > /dev/null 2>&1; then
        log_info "Application is healthy!"
        break
    fi

    # Check if process is still running
    if ! ps -p $APP_PID > /dev/null 2>&1; then
        log_error "Application process died during startup!"
        log_error "Check logs: tail -f $LOG_FILE"
        exit 1
    fi

    sleep 1
    WAIT_COUNT=$((WAIT_COUNT + 1))

    if [ $((WAIT_COUNT % 10)) -eq 0 ]; then
        log_info "Still waiting... ($WAIT_COUNT/$MAX_WAIT seconds)"
    fi
done

if [ $WAIT_COUNT -ge $MAX_WAIT ]; then
    log_error "Application failed to start within $MAX_WAIT seconds"
    log_error "Check logs: tail -f $LOG_FILE"

    # Offer to rollback
    confirm "Attempt automatic rollback?"

    log_info "Killing failed deployment..."
    kill $APP_PID 2>/dev/null || true

    if [ -f "$BACKUP_FILE" ]; then
        log_info "Restoring database from backup..."
        gunzip -c "$BACKUP_FILE" | mysql --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME"
        log_info "Database restored"
    fi

    exit 1
fi

# Step 6: Verification
log_step "Step 6: Running post-deployment verification..."

# Check health endpoint
HEALTH_STATUS=$(curl -s "http://localhost:$APP_PORT/actuator/health" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
if [ "$HEALTH_STATUS" = "UP" ]; then
    log_info "Health check: PASSED"
else
    log_error "Health check: FAILED (Status: $HEALTH_STATUS)"
    exit 1
fi

# Check database connectivity
log_info "Verifying database connectivity..."
if mysql --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME" -e "SELECT 1;" > /dev/null 2>&1; then
    log_info "Database connectivity: PASSED"
else
    log_error "Database connectivity: FAILED"
    exit 1
fi

# Check critical tables exist
log_info "Verifying database schema..."
CHURCH_COUNT=$(mysql --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME" -N -e "SELECT COUNT(*) FROM churches;" 2>/dev/null || echo "0")
USER_COUNT=$(mysql --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME" -N -e "SELECT COUNT(*) FROM users;" 2>/dev/null || echo "0")
log_info "Churches in database: $CHURCH_COUNT"
log_info "Users in database: $USER_COUNT"

# Check Flyway migrations
log_info "Checking Flyway migration status..."
FLYWAY_COUNT=$(mysql --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME" -N -e "SELECT COUNT(*) FROM flyway_schema_history WHERE success=1;" 2>/dev/null || echo "0")
FLYWAY_LATEST=$(mysql --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME" -N -e "SELECT version FROM flyway_schema_history WHERE success=1 ORDER BY installed_rank DESC LIMIT 1;" 2>/dev/null || echo "N/A")
log_info "Successful migrations: $FLYWAY_COUNT"
log_info "Latest migration: V$FLYWAY_LATEST"

# Check for errors in logs
log_info "Checking for errors in startup logs..."
ERROR_COUNT=$(grep -c "ERROR" "$LOG_FILE" || echo "0")
if [ "$ERROR_COUNT" -gt 0 ]; then
    log_warn "Found $ERROR_COUNT errors in logs"
    log_warn "Review logs: grep ERROR $LOG_FILE"
else
    log_info "No errors found in startup logs"
fi

# Success
log_step "Deployment Summary"
echo "=========================================="
echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
echo "=========================================="
echo "Application: $APP_NAME"
echo "PID: $APP_PID"
echo "Profile: $SPRING_PROFILE"
echo "Port: $APP_PORT"
echo "Log file: $LOG_FILE"
echo "Health URL: http://localhost:$APP_PORT/actuator/health"
if [ -f "$BACKUP_FILE" ]; then
    echo "Backup: $BACKUP_FILE"
fi
echo "=========================================="
echo ""
log_info "Monitor logs: tail -f $LOG_FILE"
log_info "Check health: curl http://localhost:$APP_PORT/actuator/health"
log_info "Stop application: kill $APP_PID"
echo ""

exit 0
