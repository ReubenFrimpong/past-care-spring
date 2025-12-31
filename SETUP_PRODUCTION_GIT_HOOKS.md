# Setting Up Production Git Hooks for Auto-Deployment

This guide sets up automatic deployment on your production server using Git hooks. When you push to the production remote, it will automatically pull and deploy your code.

---

## Architecture Overview

```
Your Local Machine                Production Server (62.169.28.116)
==================                ================================

Backend Repo                      Bare Git Repo (receives push)
└─> git push production master    /opt/pastcare/repos/past-care-spring.git
                                  │
                                  └─> post-receive hook triggers
                                      │
                                      └─> Working Directory (actual app)
                                          /opt/pastcare/backend
                                          - git pull from bare repo
                                          - mvnw clean package
                                          - systemctl restart backend

Frontend Repo                     Bare Git Repo (receives push)
└─> git push production master    /opt/pastcare/repos/past-care-spring-frontend.git
                                  │
                                  └─> post-receive hook triggers
                                      │
                                      └─> Working Directory (actual app)
                                          /opt/pastcare/frontend
                                          - git pull from bare repo
                                          - npm run build
                                          - copy to /var/www/pastcare
```

---

## Step 1: SSH into Production Server

```bash
ssh root@62.169.28.116
```

---

## Step 2: Create Directory Structure

```bash
# Create directories
mkdir -p /opt/pastcare/{repos,backend,frontend,logs,backups}
mkdir -p /var/pastcare/uploads
mkdir -p /var/www/pastcare

# Set permissions
chmod 755 /opt/pastcare
chmod 755 /var/pastcare/uploads
```

---

## Step 3: Set Up Backend Bare Repository

### 3.1 Initialize Bare Repository

```bash
cd /opt/pastcare/repos
git init --bare past-care-spring.git
```

### 3.2 Create post-receive Hook

```bash
nano /opt/pastcare/repos/past-care-spring.git/hooks/post-receive
```

Paste this content:

```bash
#!/bin/bash
#
# Backend Auto-Deployment Hook
# Triggered when you push to: git push production master
#

# Configuration
WORK_TREE="/opt/pastcare/backend"
GIT_DIR="/opt/pastcare/repos/past-care-spring.git"
LOG_FILE="/opt/pastcare/logs/backend-deploy.log"
ENV_FILE="/opt/pastcare/backend/.env"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Error handling
set -e
trap 'log "❌ Deployment failed at line $LINENO"' ERR

log "=========================================="
log "🚀 Starting Backend Deployment"
log "=========================================="

# Read the branch being pushed
while read oldrev newrev refname; do
    branch=$(git rev-parse --symbolic --abbrev-ref $refname)

    if [ "$branch" == "master" ]; then
        log "📥 Received push to master branch"

        # 1. Backup current JAR (if exists)
        if [ -f "$WORK_TREE/target/pastcare-spring-0.0.1-SNAPSHOT.jar" ]; then
            log "💾 Backing up current JAR..."
            cp "$WORK_TREE/target/pastcare-spring-0.0.1-SNAPSHOT.jar" \
               "$WORK_TREE/target/pastcare-spring-backup-$(date +%Y%m%d_%H%M%S).jar"
        fi

        # 2. Checkout code to working directory
        log "📦 Checking out code to $WORK_TREE..."
        mkdir -p "$WORK_TREE"
        git --work-tree="$WORK_TREE" --git-dir="$GIT_DIR" checkout -f master

        # 3. Load environment variables
        if [ -f "$ENV_FILE" ]; then
            log "🔐 Loading environment variables..."
            export $(cat "$ENV_FILE" | grep -v '^#' | xargs)
        else
            log "⚠️  Warning: $ENV_FILE not found!"
        fi

        # 4. Build application
        log "🔨 Building application..."
        cd "$WORK_TREE"
        ./mvnw clean package -DskipTests 2>&1 | tee -a "$LOG_FILE"

        if [ ${PIPESTATUS[0]} -eq 0 ]; then
            log "✅ Build successful"
        else
            log "❌ Build failed!"
            exit 1
        fi

        # 5. Restart backend service
        log "🔄 Restarting backend service..."
        systemctl restart pastcare-backend

        # 6. Wait for service to start
        log "⏳ Waiting for service to start..."
        sleep 5

        # 7. Verify service is running
        if systemctl is-active --quiet pastcare-backend; then
            log "✅ Backend service is running"

            # Check health endpoint
            if curl -sf http://localhost:8080/actuator/health > /dev/null; then
                log "✅ Health check passed"
            else
                log "⚠️  Warning: Health check failed (service may still be starting)"
            fi
        else
            log "❌ Backend service failed to start!"
            log "📋 Last 20 lines of logs:"
            journalctl -u pastcare-backend -n 20 --no-pager | tee -a "$LOG_FILE"
            exit 1
        fi

        # 8. Cleanup old backups (keep last 5)
        log "🧹 Cleaning up old backups..."
        cd "$WORK_TREE/target"
        ls -t pastcare-spring-backup-*.jar 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null || true

        log "=========================================="
        log "✅ Backend Deployment Complete!"
        log "=========================================="
        log "📊 View logs: sudo journalctl -u pastcare-backend -f"

    else
        log "⏭️  Skipping deployment for branch: $branch"
    fi
done
```

Make it executable:
```bash
chmod +x /opt/pastcare/repos/past-care-spring.git/hooks/post-receive
```

---

## Step 4: Set Up Frontend Bare Repository

### 4.1 Initialize Bare Repository

```bash
cd /opt/pastcare/repos
git init --bare past-care-spring-frontend.git
```

### 4.2 Create post-receive Hook

```bash
nano /opt/pastcare/repos/past-care-spring-frontend.git/hooks/post-receive
```

Paste this content:

```bash
#!/bin/bash
#
# Frontend Auto-Deployment Hook
# Triggered when you push to: git push production master
#

# Configuration
WORK_TREE="/opt/pastcare/frontend"
GIT_DIR="/opt/pastcare/repos/past-care-spring-frontend.git"
DEPLOY_DIR="/var/www/pastcare"
LOG_FILE="/opt/pastcare/logs/frontend-deploy.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Error handling
set -e
trap 'log "❌ Deployment failed at line $LINENO"' ERR

log "=========================================="
log "🚀 Starting Frontend Deployment"
log "=========================================="

# Read the branch being pushed
while read oldrev newrev refname; do
    branch=$(git rev-parse --symbolic --abbrev-ref $refname)

    if [ "$branch" == "master" ]; then
        log "📥 Received push to master branch"

        # 1. Backup current deployment
        if [ -d "$DEPLOY_DIR" ] && [ "$(ls -A $DEPLOY_DIR)" ]; then
            log "💾 Backing up current deployment..."
            tar -czf "/opt/pastcare/backups/frontend-backup-$(date +%Y%m%d_%H%M%S).tar.gz" \
                -C "$DEPLOY_DIR" . 2>/dev/null || true
        fi

        # 2. Checkout code to working directory
        log "📦 Checking out code to $WORK_TREE..."
        mkdir -p "$WORK_TREE"
        git --work-tree="$WORK_TREE" --git-dir="$GIT_DIR" checkout -f master

        # 3. Install dependencies (only if package.json changed)
        cd "$WORK_TREE"
        if git diff --name-only $oldrev $newrev | grep -q "package.json"; then
            log "📦 Installing dependencies..."
            npm install --production 2>&1 | tee -a "$LOG_FILE"
        else
            log "⏭️  Skipping npm install (package.json unchanged)"
        fi

        # 4. Build for production
        log "🔨 Building frontend..."
        npm run build --configuration=production 2>&1 | tee -a "$LOG_FILE"

        if [ ${PIPESTATUS[0]} -eq 0 ]; then
            log "✅ Build successful"
        else
            log "❌ Build failed!"
            exit 1
        fi

        # 5. Deploy to nginx directory
        log "🚀 Deploying to $DEPLOY_DIR..."

        # Create deploy directory if it doesn't exist
        mkdir -p "$DEPLOY_DIR"

        # Remove old files (except hidden files)
        rm -rf "$DEPLOY_DIR"/* 2>/dev/null || true

        # Copy new build
        if [ -d "$WORK_TREE/dist/past-care-spring-frontend" ]; then
            cp -r "$WORK_TREE/dist/past-care-spring-frontend/"* "$DEPLOY_DIR/"
            log "✅ Files deployed successfully"
        else
            log "❌ Build directory not found!"
            exit 1
        fi

        # 6. Set correct permissions
        log "🔐 Setting permissions..."
        chown -R www-data:www-data "$DEPLOY_DIR"
        chmod -R 755 "$DEPLOY_DIR"

        # 7. Reload nginx
        log "🔄 Reloading nginx..."
        nginx -t 2>&1 | tee -a "$LOG_FILE"

        if [ ${PIPESTATUS[0]} -eq 0 ]; then
            systemctl reload nginx
            log "✅ Nginx reloaded successfully"
        else
            log "❌ Nginx configuration test failed!"
            exit 1
        fi

        # 8. Cleanup old backups (keep last 5)
        log "🧹 Cleaning up old backups..."
        cd /opt/pastcare/backups
        ls -t frontend-backup-*.tar.gz 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null || true

        log "=========================================="
        log "✅ Frontend Deployment Complete!"
        log "=========================================="
        log "🌐 Visit: http://your-domain.com"

    else
        log "⏭️  Skipping deployment for branch: $branch"
    fi
done
```

Make it executable:
```bash
chmod +x /opt/pastcare/repos/past-care-spring-frontend.git/hooks/post-receive
```

---

## Step 5: Clone Repositories to Working Directories

Now clone from the bare repos to create working directories:

```bash
# Backend
cd /opt/pastcare
git clone /opt/pastcare/repos/past-care-spring.git backend

# Frontend
git clone /opt/pastcare/repos/past-care-spring-frontend.git frontend
```

---

## Step 6: Set Up Environment Variables (Backend)

Create the `.env` file:

```bash
nano /opt/pastcare/backend/.env
```

Add your environment variables:
```bash
# Database
DATABASE_URL=jdbc:mysql://localhost:3306/pastcare_production
DATABASE_USERNAME=pastcare_user
DATABASE_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_very_long_random_64_char_secret_key_here

# Paystack (LIVE KEYS!)
PAYSTACK_SECRET_KEY=sk_live_your_key
PAYSTACK_PUBLIC_KEY=pk_live_your_key
PAYSTACK_WEBHOOK_SECRET=your_webhook_secret

# QR Code
QR_SECRET_KEY=your16charkey123

# URLs
FRONTEND_URL=https://yourdomain.com
APP_DOMAIN=yourdomain.com

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
EMAIL_FROM=noreply@yourdomain.com

# SMS
AFRICASTALKING_API_KEY=your_api_key
AFRICASTALKING_USERNAME=your_username
AFRICASTALKING_SENDER_ID=PASTCARE

# Uploads
UPLOAD_DIR=/var/pastcare/uploads
```

Secure the file:
```bash
chmod 600 /opt/pastcare/backend/.env
```

---

## Step 7: Create Systemd Service for Backend

```bash
nano /etc/systemd/system/pastcare-backend.service
```

Add this content:
```ini
[Unit]
Description=PastCare Spring Boot Backend
After=syslog.target network.target mysql.service

[Service]
User=root
Type=simple
WorkingDirectory=/opt/pastcare/backend
EnvironmentFile=/opt/pastcare/backend/.env
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/pastcare/backend/target/pastcare-spring-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=pastcare-backend

# Security settings
NoNewPrivileges=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
systemctl daemon-reload
systemctl enable pastcare-backend
systemctl start pastcare-backend
systemctl status pastcare-backend
```

---

## Step 8: Configure Nginx

```bash
nano /etc/nginx/sites-available/pastcare
```

Add this content:
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # SSL redirect (after setting up SSL)
    # return 301 https://$server_name$request_uri;

    root /var/www/pastcare;
    index index.html;

    # Logging
    access_log /var/log/nginx/pastcare-access.log;
    error_log /var/log/nginx/pastcare-error.log;

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

        # Timeouts
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
    }

    # Increase upload size
    client_max_body_size 50M;
}
```

Enable and test:
```bash
ln -s /etc/nginx/sites-available/pastcare /etc/nginx/sites-enabled/
nginx -t
systemctl restart nginx
```

---

## Step 9: Update Git Remotes on Your Local Machine

Now you need to update your local repositories to push to production:

### Backend

```bash
cd /home/reuben/Documents/workspace/pastcare-spring
git remote add production root@62.169.28.116:/opt/pastcare/repos/past-care-spring.git
```

### Frontend

```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend
git remote add production root@62.169.28.116:/opt/pastcare/repos/past-care-spring-frontend.git
```

---

## Step 10: Deploy! 🚀

Now you can deploy by simply pushing:

### Deploy Backend

```bash
cd /home/reuben/Documents/workspace/pastcare-spring
git push production master
```

You'll see output like:
```
📥 Received push to master branch
💾 Backing up current JAR...
📦 Checking out code...
🔨 Building application...
✅ Build successful
🔄 Restarting backend service...
✅ Backend service is running
✅ Health check passed
✅ Backend Deployment Complete!
```

### Deploy Frontend

```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend
git push production master
```

You'll see output like:
```
📥 Received push to master branch
💾 Backing up current deployment...
📦 Checking out code...
🔨 Building frontend...
✅ Build successful
🚀 Deploying to /var/www/pastcare...
✅ Files deployed successfully
🔄 Reloading nginx...
✅ Frontend Deployment Complete!
```

---

## Monitoring Deployments

### View Deployment Logs

```bash
# Backend deployment log
ssh root@62.169.28.116 "tail -f /opt/pastcare/logs/backend-deploy.log"

# Frontend deployment log
ssh root@62.169.28.116 "tail -f /opt/pastcare/logs/frontend-deploy.log"

# Backend application log
ssh root@62.169.28.116 "journalctl -u pastcare-backend -f"
```

### View All Recent Deployments

```bash
ssh root@62.169.28.116 << 'EOF'
echo "=== Backend Deployments ==="
grep "Deployment Complete" /opt/pastcare/logs/backend-deploy.log | tail -5

echo ""
echo "=== Frontend Deployments ==="
grep "Deployment Complete" /opt/pastcare/logs/frontend-deploy.log | tail -5
EOF
```

---

## Rollback Procedure

If deployment fails, the hooks automatically keep backups.

### Rollback Backend

```bash
ssh root@62.169.28.116 << 'EOF'
# Find latest backup
LATEST_BACKUP=$(ls -t /opt/pastcare/backend/target/pastcare-spring-backup-*.jar | head -1)

# Stop service
systemctl stop pastcare-backend

# Restore backup
cp "$LATEST_BACKUP" /opt/pastcare/backend/target/pastcare-spring-0.0.1-SNAPSHOT.jar

# Start service
systemctl start pastcare-backend

echo "Rollback complete"
EOF
```

### Rollback Frontend

```bash
ssh root@62.169.28.116 << 'EOF'
# Find latest backup
LATEST_BACKUP=$(ls -t /opt/pastcare/backups/frontend-backup-*.tar.gz | head -1)

# Extract backup
rm -rf /var/www/pastcare/*
tar -xzf "$LATEST_BACKUP" -C /var/www/pastcare

# Reload nginx
systemctl reload nginx

echo "Rollback complete"
EOF
```

---

## Troubleshooting

### Hook Not Executing

```bash
# Check hook permissions
ssh root@62.169.28.116 "ls -la /opt/pastcare/repos/past-care-spring.git/hooks/"

# Should be executable (755)
ssh root@62.169.28.116 "chmod +x /opt/pastcare/repos/past-care-spring.git/hooks/post-receive"
```

### Build Failures

```bash
# Check deployment logs
ssh root@62.169.28.116 "tail -50 /opt/pastcare/logs/backend-deploy.log"
ssh root@62.169.28.116 "tail -50 /opt/pastcare/logs/frontend-deploy.log"
```

### Permission Errors

```bash
# Fix ownership
ssh root@62.169.28.116 << 'EOF'
chown -R root:root /opt/pastcare/backend
chown -R root:root /opt/pastcare/frontend
chown -R www-data:www-data /var/www/pastcare
EOF
```

---

## Complete Deployment Workflow

```bash
# 1. Make changes locally
git add .
git commit -m "Your commit message"

# 2. Push to GitHub (backup)
git push origin master

# 3. Deploy to production
git push production master

# 4. Monitor deployment
ssh root@62.169.28.116 "tail -f /opt/pastcare/logs/backend-deploy.log"

# 5. Verify
curl https://yourdomain.com
```

---

**That's it! Now every time you `git push production master`, your code will automatically deploy! 🎉**
