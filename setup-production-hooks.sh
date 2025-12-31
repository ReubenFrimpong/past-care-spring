#!/bin/bash

# PastCare Production Git Hooks Setup Script
# Run this script ON your production server (root@62.169.28.116)
# Usage: bash setup-production-hooks.sh

set -e  # Exit on error

echo "==========================================="
echo "  PastCare Production Git Hooks Setup"
echo "==========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}Step 1: Creating directory structure...${NC}"
mkdir -p /opt/pastcare/{repos,backend,frontend,logs/{backend,frontend},backups}
echo -e "${GREEN}✓ Directories created${NC}"
echo ""

echo -e "${BLUE}Step 2: Setting up backend bare repository...${NC}"
cd /opt/pastcare/repos
git init --bare past-care-spring.git
echo -e "${GREEN}✓ Backend bare repository created${NC}"
echo ""

echo -e "${BLUE}Step 3: Creating backend post-receive hook...${NC}"
cat > /opt/pastcare/repos/past-care-spring.git/hooks/post-receive << 'BACKEND_HOOK'
#!/bin/bash

# Backend Auto-Deployment Hook
WORK_TREE="/opt/pastcare/backend"
GIT_DIR="/opt/pastcare/repos/past-care-spring.git"
LOG_FILE="/opt/pastcare/logs/backend/deploy.log"
BACKUP_DIR="/opt/pastcare/backups"

# Logging function
log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=========================================="
log "Backend Deployment Started"
log "=========================================="

# Backup current JAR if exists
if [ -f "$WORK_TREE/target/pastcare-spring-0.0.1-SNAPSHOT.jar" ]; then
    log "Creating backup of current JAR..."
    cp "$WORK_TREE/target/pastcare-spring-0.0.1-SNAPSHOT.jar" \
       "$BACKUP_DIR/pastcare-spring-$(date +%Y%m%d_%H%M%S).jar"
    log "✓ Backup created"
fi

# Checkout latest code
log "Checking out latest code from master branch..."
git --work-tree="$WORK_TREE" --git-dir="$GIT_DIR" checkout -f master
log "✓ Code checked out"

# Navigate to work tree
cd "$WORK_TREE"

# Load environment variables
if [ -f "$WORK_TREE/.env" ]; then
    log "Loading environment variables..."
    export $(cat "$WORK_TREE/.env" | grep -v '^#' | xargs)
    log "✓ Environment variables loaded"
else
    log "⚠ WARNING: .env file not found at $WORK_TREE/.env"
    log "Please create .env file with production configuration"
fi

# Build application
log "Building application with Maven..."
if ./mvnw clean package -DskipTests >> "$LOG_FILE" 2>&1; then
    log "✓ Build successful"
else
    log "✗ Build failed - check logs at $LOG_FILE"
    log "Rolling back to previous version..."

    # Restore from backup
    LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/pastcare-spring-*.jar 2>/dev/null | head -1)
    if [ -n "$LATEST_BACKUP" ]; then
        cp "$LATEST_BACKUP" "$WORK_TREE/target/pastcare-spring-0.0.1-SNAPSHOT.jar"
        log "✓ Restored from backup: $LATEST_BACKUP"
    fi

    exit 1
fi

# Restart backend service
log "Restarting backend service..."
if systemctl restart pastcare-backend; then
    log "✓ Backend service restarted"
else
    log "✗ Failed to restart backend service"
    exit 1
fi

# Wait for service to start
log "Waiting for backend to start..."
sleep 10

# Health check
log "Performing health check..."
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    log "✓ Backend is UP and healthy"
else
    log "⚠ WARNING: Backend health check failed"
    log "Check service status: systemctl status pastcare-backend"
fi

log "=========================================="
log "Backend Deployment Completed Successfully"
log "=========================================="
BACKEND_HOOK

chmod +x /opt/pastcare/repos/past-care-spring.git/hooks/post-receive
echo -e "${GREEN}✓ Backend post-receive hook created and made executable${NC}"
echo ""

echo -e "${BLUE}Step 4: Setting up frontend bare repository...${NC}"
cd /opt/pastcare/repos
git init --bare past-care-spring-frontend.git
echo -e "${GREEN}✓ Frontend bare repository created${NC}"
echo ""

echo -e "${BLUE}Step 5: Creating frontend post-receive hook...${NC}"
cat > /opt/pastcare/repos/past-care-spring-frontend.git/hooks/post-receive << 'FRONTEND_HOOK'
#!/bin/bash

# Frontend Auto-Deployment Hook
WORK_TREE="/opt/pastcare/frontend"
GIT_DIR="/opt/pastcare/repos/past-care-spring-frontend.git"
DEPLOY_DIR="/var/www/pastcare"
LOG_FILE="/opt/pastcare/logs/frontend/deploy.log"
BACKUP_DIR="/opt/pastcare/backups"

# Logging function
log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=========================================="
log "Frontend Deployment Started"
log "=========================================="

# Backup current deployment
if [ -d "$DEPLOY_DIR" ] && [ "$(ls -A $DEPLOY_DIR)" ]; then
    log "Creating backup of current deployment..."
    tar -czf "$BACKUP_DIR/frontend-$(date +%Y%m%d_%H%M%S).tar.gz" -C "$DEPLOY_DIR" . 2>/dev/null || true
    log "✓ Backup created"
fi

# Checkout latest code
log "Checking out latest code from master branch..."
git --work-tree="$WORK_TREE" --git-dir="$GIT_DIR" checkout -f master
log "✓ Code checked out"

# Navigate to work tree
cd "$WORK_TREE"

# Install dependencies (only if package.json changed)
log "Checking for package.json changes..."
if git diff-tree --name-only HEAD HEAD~1 | grep -q "package.json"; then
    log "package.json changed - installing dependencies..."
    if npm install >> "$LOG_FILE" 2>&1; then
        log "✓ Dependencies installed"
    else
        log "✗ npm install failed - check logs"
        exit 1
    fi
else
    log "No package.json changes detected"
fi

# Build application
log "Building frontend for production..."
if npm run build --configuration=production >> "$LOG_FILE" 2>&1; then
    log "✓ Build successful"
else
    log "✗ Build failed - check logs at $LOG_FILE"
    log "Rolling back to previous version..."

    # Restore from backup
    LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/frontend-*.tar.gz 2>/dev/null | head -1)
    if [ -n "$LATEST_BACKUP" ]; then
        rm -rf "$DEPLOY_DIR"/*
        tar -xzf "$LATEST_BACKUP" -C "$DEPLOY_DIR"
        log "✓ Restored from backup: $LATEST_BACKUP"
    fi

    exit 1
fi

# Deploy to Nginx directory
log "Deploying to Nginx directory..."
mkdir -p "$DEPLOY_DIR"
rm -rf "$DEPLOY_DIR"/*
cp -r "$WORK_TREE/dist/past-care-spring-frontend/"* "$DEPLOY_DIR/"
log "✓ Files copied to $DEPLOY_DIR"

# Set proper permissions
chown -R www-data:www-data "$DEPLOY_DIR"
log "✓ Permissions set"

# Test Nginx configuration
log "Testing Nginx configuration..."
if nginx -t >> "$LOG_FILE" 2>&1; then
    log "✓ Nginx configuration valid"
else
    log "✗ Nginx configuration test failed"
    exit 1
fi

# Reload Nginx
log "Reloading Nginx..."
if systemctl reload nginx; then
    log "✓ Nginx reloaded"
else
    log "✗ Failed to reload Nginx"
    exit 1
fi

log "=========================================="
log "Frontend Deployment Completed Successfully"
log "=========================================="
FRONTEND_HOOK

chmod +x /opt/pastcare/repos/past-care-spring-frontend.git/hooks/post-receive
echo -e "${GREEN}✓ Frontend post-receive hook created and made executable${NC}"
echo ""

echo -e "${BLUE}Step 6: Setting up environment variables template...${NC}"
cat > /opt/pastcare/backend/.env.template << 'ENV_TEMPLATE'
# Database Configuration
DATABASE_URL=jdbc:mysql://localhost:3306/pastcare_production
DATABASE_USERNAME=pastcare_user
DATABASE_PASSWORD=your_secure_password_here

# JWT Configuration
JWT_SECRET=your_very_long_random_secret_key_at_least_64_chars_long

# Paystack Configuration (LIVE KEYS - NOT TEST KEYS!)
PAYSTACK_SECRET_KEY=sk_live_your_live_secret_key
PAYSTACK_PUBLIC_KEY=pk_live_your_live_public_key
PAYSTACK_WEBHOOK_SECRET=your_webhook_secret

# QR Code Encryption
QR_SECRET_KEY=your16charkey123

# Frontend URL
FRONTEND_URL=https://yourdomain.com

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-specific-password
EMAIL_FROM=noreply@yourdomain.com

# SMS Configuration (AfricasTalking)
AFRICASTALKING_API_KEY=your_api_key
AFRICASTALKING_USERNAME=your_username
AFRICASTALKING_SENDER_ID=PASTCARE

# Upload Directory
UPLOAD_DIR=/var/pastcare/uploads

# Application Domain
APP_DOMAIN=yourdomain.com
ENV_TEMPLATE

echo -e "${GREEN}✓ Environment template created at /opt/pastcare/backend/.env.template${NC}"
echo -e "${YELLOW}⚠ IMPORTANT: Copy .env.template to .env and fill in your production values!${NC}"
echo ""

echo -e "${BLUE}Step 7: Creating systemd service for backend...${NC}"
cat > /etc/systemd/system/pastcare-backend.service << 'SYSTEMD_SERVICE'
[Unit]
Description=PastCare Spring Boot Backend
After=syslog.target network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/pastcare/backend
EnvironmentFile=/opt/pastcare/backend/.env
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/pastcare/backend/target/pastcare-spring-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=pastcare-backend

# Security
PrivateTmp=true
NoNewPrivileges=true

[Install]
WantedBy=multi-user.target
SYSTEMD_SERVICE

systemctl daemon-reload
systemctl enable pastcare-backend
echo -e "${GREEN}✓ Systemd service created and enabled${NC}"
echo ""

echo -e "${BLUE}Step 8: Creating Nginx configuration...${NC}"
cat > /etc/nginx/sites-available/pastcare << 'NGINX_CONFIG'
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # Redirect HTTP to HTTPS (uncomment after SSL setup)
    # return 301 https://$server_name$request_uri;

    root /var/www/pastcare;
    index index.html;

    # Frontend (Angular SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Backend API proxy
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;

        # Increase timeouts for long requests
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
    }

    # Increase upload size limit
    client_max_body_size 50M;

    # Logging
    access_log /var/log/nginx/pastcare-access.log;
    error_log /var/log/nginx/pastcare-error.log;
}
NGINX_CONFIG

# Enable site
ln -sf /etc/nginx/sites-available/pastcare /etc/nginx/sites-enabled/
nginx -t
echo -e "${GREEN}✓ Nginx configuration created${NC}"
echo -e "${YELLOW}⚠ Remember to update server_name with your actual domain${NC}"
echo ""

echo "==========================================="
echo -e "${GREEN}Production Git Hooks Setup Complete!${NC}"
echo "==========================================="
echo ""
echo "Next Steps:"
echo ""
echo "1. Configure environment variables:"
echo -e "   ${BLUE}cd /opt/pastcare/backend${NC}"
echo -e "   ${BLUE}cp .env.template .env${NC}"
echo -e "   ${BLUE}nano .env${NC} (fill in production values)"
echo ""
echo "2. On your LOCAL machine, add production remotes:"
echo -e "   ${BLUE}# Backend${NC}"
echo -e "   ${BLUE}cd /home/reuben/Documents/workspace/pastcare-spring${NC}"
echo -e "   ${BLUE}git remote add production root@62.169.28.116:/opt/pastcare/repos/past-care-spring.git${NC}"
echo ""
echo -e "   ${BLUE}# Frontend${NC}"
echo -e "   ${BLUE}cd /home/reuben/Documents/workspace/past-care-spring-frontend${NC}"
echo -e "   ${BLUE}git remote add production root@62.169.28.116:/opt/pastcare/repos/past-care-spring-frontend.git${NC}"
echo ""
echo "3. Deploy to production:"
echo -e "   ${BLUE}git push production master${NC}"
echo ""
echo "4. View deployment logs:"
echo -e "   ${BLUE}tail -f /opt/pastcare/logs/backend/deploy.log${NC}"
echo -e "   ${BLUE}tail -f /opt/pastcare/logs/frontend/deploy.log${NC}"
echo ""
echo "5. Check service status:"
echo -e "   ${BLUE}systemctl status pastcare-backend${NC}"
echo -e "   ${BLUE}systemctl status nginx${NC}"
echo ""
echo -e "${GREEN}Happy Deploying! 🚀${NC}"
