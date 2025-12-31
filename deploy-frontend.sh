#!/bin/bash

# PastCare Frontend Deployment Script with Live Logs
# This script pushes to production and streams application logs

set -e  # Exit on error

PRODUCTION_SERVER="root@62.169.28.116"
FRONTEND_LOG_PATH="/opt/pastcare/logs/frontend/access.log"
NGINX_ACCESS_LOG="/var/log/nginx/pastcare-access.log"
NGINX_ERROR_LOG="/var/log/nginx/pastcare-error.log"

echo "=========================================="
echo "  PastCare Frontend Deployment"
echo "=========================================="
echo ""

# Navigate to frontend directory
cd past-care-spring-frontend

# Step 1: Push to production remote
echo "📦 Pushing to production remote..."
git push production master

echo ""
echo "✅ Push complete!"
echo ""

# Step 2: Ask user which logs to stream
echo "Which logs would you like to stream?"
echo "1) Nginx Access Logs (HTTP requests)"
echo "2) Nginx Error Logs (errors only)"
echo "3) Both (split view)"
echo "4) Skip log streaming"
echo ""

read -p "Select option (1-4): " -n 1 -r LOG_CHOICE
echo ""
echo ""

case $LOG_CHOICE in
    1)
        echo "=========================================="
        echo "  Streaming Nginx Access Logs"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        ssh $PRODUCTION_SERVER "tail -f $NGINX_ACCESS_LOG 2>/dev/null || tail -f /var/log/nginx/access.log"
        ;;
    2)
        echo "=========================================="
        echo "  Streaming Nginx Error Logs"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        ssh $PRODUCTION_SERVER "tail -f $NGINX_ERROR_LOG 2>/dev/null || tail -f /var/log/nginx/error.log"
        ;;
    3)
        echo "=========================================="
        echo "  Streaming Both Access & Error Logs"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        # Use multitail if available, otherwise use tail with process substitution
        ssh $PRODUCTION_SERVER "
            if command -v multitail &> /dev/null; then
                multitail $NGINX_ACCESS_LOG $NGINX_ERROR_LOG
            else
                tail -f $NGINX_ACCESS_LOG $NGINX_ERROR_LOG
            fi
        "
        ;;
    4)
        echo "⏭️  Skipping log streaming"
        ;;
    *)
        echo "Invalid option. Skipping log streaming."
        ;;
esac

echo ""
echo "✅ Deployment complete!"
