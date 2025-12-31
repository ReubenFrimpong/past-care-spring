#!/bin/bash

# PastCare Full Stack Deployment Script with Live Logs
# Deploys both backend and frontend with optional log streaming

set -e  # Exit on error

PRODUCTION_SERVER="root@62.169.28.116"
BACKEND_LOG_PATH="/opt/pastcare/logs/backend/application.log"
NGINX_ACCESS_LOG="/var/log/nginx/pastcare-access.log"
NGINX_ERROR_LOG="/var/log/nginx/pastcare-error.log"

echo "=========================================="
echo "  PastCare Full Stack Deployment"
echo "=========================================="
echo ""

# Step 1: Deploy Backend
echo "📦 [1/2] Deploying Backend..."
echo "----------------------------------------"
git push production master
echo "✅ Backend pushed successfully!"
echo ""

# Step 2: Deploy Frontend
echo "📦 [2/2] Deploying Frontend..."
echo "----------------------------------------"
cd past-care-spring-frontend
git push production master
echo "✅ Frontend pushed successfully!"
cd ..
echo ""

echo "=========================================="
echo "  Deployment Complete!"
echo "=========================================="
echo ""

# Step 3: Offer log streaming options
echo "Which logs would you like to stream?"
echo "1) Backend application logs (Spring Boot)"
echo "2) Frontend access logs (Nginx)"
echo "3) Frontend error logs (Nginx)"
echo "4) Backend + Frontend errors (combined)"
echo "5) All logs (multi-tail)"
echo "6) Skip log streaming"
echo ""

read -p "Select option (1-6): " -n 1 -r LOG_CHOICE
echo ""
echo ""

case $LOG_CHOICE in
    1)
        echo "=========================================="
        echo "  Streaming Backend Application Logs"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        ssh $PRODUCTION_SERVER "tail -f $BACKEND_LOG_PATH 2>/dev/null || journalctl -u pastcare-backend -f"
        ;;
    2)
        echo "=========================================="
        echo "  Streaming Frontend Access Logs"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        ssh $PRODUCTION_SERVER "tail -f $NGINX_ACCESS_LOG 2>/dev/null || tail -f /var/log/nginx/access.log"
        ;;
    3)
        echo "=========================================="
        echo "  Streaming Frontend Error Logs"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        ssh $PRODUCTION_SERVER "tail -f $NGINX_ERROR_LOG 2>/dev/null || tail -f /var/log/nginx/error.log"
        ;;
    4)
        echo "=========================================="
        echo "  Streaming Backend + Frontend Errors"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        ssh $PRODUCTION_SERVER "tail -f $BACKEND_LOG_PATH $NGINX_ERROR_LOG 2>/dev/null | grep -i 'error\|exception\|failed'"
        ;;
    5)
        echo "=========================================="
        echo "  Streaming All Logs"
        echo "=========================================="
        echo "Press Ctrl+C to stop streaming"
        echo ""
        ssh $PRODUCTION_SERVER "tail -f $BACKEND_LOG_PATH $NGINX_ACCESS_LOG $NGINX_ERROR_LOG 2>/dev/null"
        ;;
    6)
        echo "⏭️  Skipping log streaming"
        ;;
    *)
        echo "Invalid option. Skipping log streaming."
        ;;
esac

echo ""
echo "✅ All done!"
