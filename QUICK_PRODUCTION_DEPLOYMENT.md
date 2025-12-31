# Quick Production Deployment Guide

**Target Server**: root@62.169.28.116
**Date**: December 31, 2025

## Prerequisites on Production Server

1. **Java 21** installed
2. **MySQL/PostgreSQL** database running
3. **Node.js & npm** installed (for frontend)
4. **Nginx** installed (for frontend serving)
5. **Git** installed

---

## Step 1: SSH into Production Server

```bash
ssh root@62.169.28.116
```

---

## Step 2: Set Up Backend

### 2.1 Navigate to Backend Directory (or clone if first time)

```bash
# If first time deployment:
cd /opt/pastcare
git clone root@62.169.28.116:/opt/pastcare/repos/past-care-spring.git backend
cd backend

# If already deployed:
cd /opt/pastcare/backend
```

### 2.2 Pull Latest Changes

```bash
git pull origin master
```

### 2.3 Set Up Environment Variables

```bash
# Create .env file with production values
nano /opt/pastcare/backend/.env
```

Add these variables (replace with your actual values):
```bash
# Database
DATABASE_URL=jdbc:mysql://localhost:3306/pastcare_production
DATABASE_USERNAME=pastcare_user
DATABASE_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_very_long_random_64_char_secret_key_here_make_it_strong

# Paystack (LIVE KEYS - NOT TEST KEYS!)
PAYSTACK_SECRET_KEY=sk_live_your_live_key_here
PAYSTACK_PUBLIC_KEY=pk_live_your_live_key_here
PAYSTACK_WEBHOOK_SECRET=your_webhook_secret

# QR Code
QR_SECRET_KEY=your16charkey123

# URLs
FRONTEND_URL=https://yourdomain.com
APP_DOMAIN=yourdomain.com

# Email (Gmail example)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-specific-password
EMAIL_FROM=noreply@yourdomain.com

# SMS (if using AfricasTalking)
AFRICASTALKING_API_KEY=your_api_key
AFRICASTALKING_USERNAME=your_username
AFRICASTALKING_SENDER_ID=PASTCARE

# Uploads
UPLOAD_DIR=/var/pastcare/uploads
```

Save and exit (Ctrl+X, Y, Enter).

### 2.4 Load Environment Variables

```bash
# Export the environment variables
export $(cat /opt/pastcare/backend/.env | grep -v '^#' | xargs)
```

### 2.5 Build Backend

```bash
./mvnw clean package -DskipTests
```

### 2.6 Create Systemd Service (First Time Only)

```bash
# Create service file
sudo nano /etc/systemd/system/pastcare-backend.service
```

Add this content:
```ini
[Unit]
Description=PastCare Spring Boot Backend
After=syslog.target network.target

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

[Install]
WantedBy=multi-user.target
```

Save and enable:
```bash
sudo systemctl daemon-reload
sudo systemctl enable pastcare-backend
```

### 2.7 Start/Restart Backend

```bash
# If first time:
sudo systemctl start pastcare-backend

# If updating:
sudo systemctl restart pastcare-backend

# Check status:
sudo systemctl status pastcare-backend

# View logs:
sudo journalctl -u pastcare-backend -f
```

---

## Step 3: Set Up Frontend

### 3.1 Navigate to Frontend Directory (or clone if first time)

```bash
# If first time deployment:
cd /opt/pastcare
git clone root@62.169.28.116:/opt/pastcare/repos/past-care-spring-frontend.git frontend
cd frontend

# If already deployed:
cd /opt/pastcare/frontend
```

### 3.2 Pull Latest Changes

```bash
git pull origin master
```

### 3.3 Install Dependencies & Build

```bash
# Install dependencies (only if package.json changed)
npm install

# Build for production
npm run build --configuration=production
# Or if that doesn't work:
ng build --configuration=production
```

### 3.4 Deploy Build to Nginx

```bash
# Create nginx web directory if doesn't exist
sudo mkdir -p /var/www/pastcare

# Copy build files
sudo cp -r dist/past-care-spring-frontend/* /var/www/pastcare/
```

### 3.5 Configure Nginx (First Time Only)

```bash
# Create nginx config
sudo nano /etc/nginx/sites-available/pastcare
```

Add this content:
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # Redirect HTTP to HTTPS (after SSL is set up)
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
}
```

Enable site and restart Nginx:
```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/pastcare /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Restart nginx
sudo systemctl restart nginx
```

---

## Step 4: Verify Deployment

### 4.1 Check Backend

```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

### 4.2 Check Frontend

```bash
# Check if frontend is being served
curl http://localhost/

# Should return HTML content
```

### 4.3 Check Database Migrations

```bash
# Connect to database
mysql -u $DATABASE_USERNAME -p$DATABASE_PASSWORD pastcare_production

# Check migrations
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 10;

exit;
```

### 4.4 Test Full Stack

Visit `http://your-server-ip` in browser and test:
- [ ] Landing page loads
- [ ] Login works
- [ ] Dashboard displays
- [ ] API calls work

---

## Step 5: Set Up SSL (Recommended)

```bash
# Install certbot
sudo apt update
sudo apt install certbot python3-certbot-nginx

# Obtain SSL certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Test auto-renewal
sudo certbot renew --dry-run
```

---

## Quick Commands Reference

### Update Backend

```bash
ssh root@62.169.28.116
cd /opt/pastcare/backend
git pull origin master
./mvnw clean package -DskipTests
sudo systemctl restart pastcare-backend
sudo journalctl -u pastcare-backend -f
```

### Update Frontend

```bash
ssh root@62.169.28.116
cd /opt/pastcare/frontend
git pull origin master
npm run build --configuration=production
sudo cp -r dist/past-care-spring-frontend/* /var/www/pastcare/
sudo systemctl reload nginx
```

### Update Both (One Script)

```bash
ssh root@62.169.28.116 << 'EOF'
# Backend
cd /opt/pastcare/backend
git pull origin master
./mvnw clean package -DskipTests
sudo systemctl restart pastcare-backend

# Frontend
cd /opt/pastcare/frontend
git pull origin master
npm run build --configuration=production
sudo cp -r dist/past-care-spring-frontend/* /var/www/pastcare/
sudo systemctl reload nginx

echo "Deployment complete!"
EOF
```

### View Logs

```bash
# Backend logs (real-time)
sudo journalctl -u pastcare-backend -f

# Backend logs (last 100 lines)
sudo journalctl -u pastcare-backend -n 100

# Nginx access logs
sudo tail -f /var/log/nginx/access.log

# Nginx error logs
sudo tail -f /var/log/nginx/error.log
```

### Check Status

```bash
# Backend status
sudo systemctl status pastcare-backend

# Nginx status
sudo systemctl status nginx

# Check ports
sudo netstat -tlnp | grep -E ':(80|8080|443)'
```

---

## Troubleshooting

### Backend Won't Start

```bash
# Check logs for errors
sudo journalctl -u pastcare-backend -n 100 --no-pager

# Common issues:
# 1. Port 8080 already in use
sudo lsof -ti:8080 | xargs kill -9

# 2. Database connection failed
# - Check DATABASE_URL in .env
# - Verify database is running: sudo systemctl status mysql

# 3. Environment variables not loaded
export $(cat /opt/pastcare/backend/.env | grep -v '^#' | xargs)
```

### Frontend Not Loading

```bash
# Check if files exist
ls -la /var/www/pastcare/

# Check nginx config
sudo nginx -t

# Restart nginx
sudo systemctl restart nginx

# Check nginx error logs
sudo tail -f /var/log/nginx/error.log
```

### Database Issues

```bash
# Check if database exists
mysql -u root -p -e "SHOW DATABASES;" | grep pastcare

# Create database if missing
mysql -u root -p -e "CREATE DATABASE pastcare_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Check database size
mysql -u root -p pastcare_production -e "SELECT table_name, ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)' FROM information_schema.TABLES WHERE table_schema = 'pastcare_production' ORDER BY (data_length + index_length) DESC;"
```

---

## Automated Deployment Script

Save this as `/opt/pastcare/deploy.sh`:

```bash
#!/bin/bash
set -e

echo "🚀 Starting PastCare Deployment..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Backend
echo "📦 Deploying Backend..."
cd /opt/pastcare/backend
git pull origin master
./mvnw clean package -DskipTests
sudo systemctl restart pastcare-backend
echo -e "${GREEN}✓ Backend deployed${NC}"

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 10

# Frontend
echo "🎨 Deploying Frontend..."
cd /opt/pastcare/frontend
git pull origin master
npm install --production
npm run build --configuration=production
sudo cp -r dist/past-care-spring-frontend/* /var/www/pastcare/
sudo systemctl reload nginx
echo -e "${GREEN}✓ Frontend deployed${NC}"

# Verify
echo "🔍 Verifying deployment..."
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo -e "${GREEN}✓ Backend is UP${NC}"
else
    echo -e "${RED}✗ Backend is DOWN - Check logs!${NC}"
    sudo journalctl -u pastcare-backend -n 50
    exit 1
fi

echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
echo "📊 View backend logs: sudo journalctl -u pastcare-backend -f"
```

Make it executable:
```bash
chmod +x /opt/pastcare/deploy.sh
```

Use it:
```bash
/opt/pastcare/deploy.sh
```

---

## Security Checklist

- [ ] Changed default database password
- [ ] Using LIVE Paystack keys (not test keys)
- [ ] JWT_SECRET is strong (64+ characters)
- [ ] SSL certificate installed
- [ ] Firewall configured (only ports 80, 443, 22 open)
- [ ] Database user has limited permissions
- [ ] .env file has restricted permissions (600)
- [ ] Nginx headers configured (HSTS, XSS protection)
- [ ] Automated backups configured
- [ ] Log rotation configured

---

**Remember**: Always backup your database before deploying!

```bash
mysqldump -u root -p pastcare_production > /opt/pastcare/backups/backup_$(date +%Y%m%d_%H%M%S).sql
```
