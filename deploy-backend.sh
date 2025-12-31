#!/bin/bash

# PastCare Backend Deployment Script with Live Logs
# This script pushes to production and streams application logs

set -e  # Exit on error

PRODUCTION_SERVER="root@62.169.28.116"
BACKEND_LOG_PATH="/opt/pastcare/logs/backend/application.log"
FALLBACK_LOG_PATH="/var/log/pastcare/backend.log"

echo "=========================================="
echo "  PastCare Backend Deployment"
echo "=========================================="
echo ""

# Step 1: Push to production remote
echo "📦 Pushing to production remote..."
git push production master

echo ""
echo "✅ Push complete!"
echo ""

# Step 2: Ask user if they want to stream logs
read -p "🔍 Do you want to stream application logs? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "=========================================="
    echo "  Streaming Backend Logs"
    echo "=========================================="
    echo "Press Ctrl+C to stop streaming"
    echo ""

    # Try primary log path first, fallback to secondary
    ssh $PRODUCTION_SERVER "tail -f $BACKEND_LOG_PATH 2>/dev/null || tail -f $FALLBACK_LOG_PATH 2>/dev/null || journalctl -u pastcare-backend -f"
fi

echo ""
echo "✅ Deployment complete!"
