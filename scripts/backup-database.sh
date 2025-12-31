#!/bin/bash

# ========================================
# PastCare Database Backup Script
# ========================================
# This script creates a timestamped backup of the production database
# Add to cron for automated daily backups: 0 2 * * * /path/to/backup-database.sh

set -e  # Exit on any error

# Configuration (update these for your environment)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-pastcare_production}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD}"  # Set via environment variable for security
BACKUP_DIR="${BACKUP_DIR:-/var/backups/pastcare}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"  # Keep backups for 30 days

# Derived variables
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/pastcare_backup_$TIMESTAMP.sql"
BACKUP_FILE_COMPRESSED="$BACKUP_FILE.gz"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Validate prerequisites
if [ -z "$DB_PASSWORD" ]; then
    log_error "DB_PASSWORD environment variable not set"
    exit 1
fi

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Start backup
log_info "Starting database backup..."
log_info "Database: $DB_NAME"
log_info "Timestamp: $TIMESTAMP"

# Perform backup
log_info "Creating SQL dump..."
mysqldump \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USER" \
    --password="$DB_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --hex-blob \
    "$DB_NAME" > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    log_info "SQL dump created successfully: $BACKUP_FILE"
else
    log_error "SQL dump failed!"
    exit 1
fi

# Compress backup
log_info "Compressing backup..."
gzip "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    log_info "Backup compressed: $BACKUP_FILE_COMPRESSED"
else
    log_error "Compression failed!"
    exit 1
fi

# Get backup size
BACKUP_SIZE=$(du -h "$BACKUP_FILE_COMPRESSED" | cut -f1)
log_info "Backup size: $BACKUP_SIZE"

# Verify backup
log_info "Verifying backup integrity..."
gunzip -t "$BACKUP_FILE_COMPRESSED"

if [ $? -eq 0 ]; then
    log_info "Backup verified successfully"
else
    log_error "Backup verification failed!"
    exit 1
fi

# Clean up old backups
log_info "Cleaning up backups older than $RETENTION_DAYS days..."
find "$BACKUP_DIR" -name "pastcare_backup_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
DELETED_COUNT=$(find "$BACKUP_DIR" -name "pastcare_backup_*.sql.gz" -type f -mtime +$RETENTION_DAYS | wc -l)
log_info "Deleted $DELETED_COUNT old backup(s)"

# List recent backups
log_info "Recent backups:"
ls -lh "$BACKUP_DIR"/pastcare_backup_*.sql.gz | tail -5

# Summary
log_info "========================================="
log_info "Backup completed successfully!"
log_info "File: $BACKUP_FILE_COMPRESSED"
log_info "Size: $BACKUP_SIZE"
log_info "========================================="

# Send notification (optional - uncomment and configure)
# echo "Database backup completed: $BACKUP_FILE_COMPRESSED ($BACKUP_SIZE)" | \
#   mail -s "PastCare Backup Success - $TIMESTAMP" admin@yourdomain.com

exit 0
