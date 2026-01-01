# PastCare SaaS - Minimal Budget-Friendly Deployment Plan

**Date**: 2025-12-29
**Status**: Production Ready
**Deployment Strategy**: Single VPS with automated deployment
**Target Budget**: $15-25/month

---

## 1. Infrastructure Overview

### Recommended Provider: **Hetzner Cloud** (Best Value)

**Primary Server Configuration:**
```
Server: Hetzner CPX21
- 3 vCPU cores
- 4 GB RAM
- 80 GB SSD storage
- 20 TB traffic
- Cost: €5.83/month (~$6.50/month)
- Location: Nuremberg, Germany (good for Africa/Europe)
```

**Why Hetzner?**
- 60% cheaper than DigitalOcean/Linode
- Excellent performance-to-price ratio
- 20TB bandwidth (vs 4TB on DigitalOcean)
- Free internal networking
- Built-in firewall
- Snapshots included

**Alternative Providers (if Hetzner unavailable):**
1. **Vultr High Frequency** - $12/month (2 vCPU, 4GB RAM, 3TB bandwidth)
2. **DigitalOcean Basic Droplet** - $24/month (2 vCPU, 4GB RAM, 4TB bandwidth)
3. **Linode Nanode** - $24/month (2 vCPU, 4GB RAM, 4TB bandwidth)

---

## 2. Tech Stack on Single VPS 

### Components (All on One Server)

```
┌─────────────────────────────────────────────┐
│         Hetzner VPS (4GB RAM, 3 vCPU)       │
├─────────────────────────────────────────────┤
│  Nginx (Reverse Proxy + Static Files)      │
│  - Frontend (Angular dist/)                 │
│  - SSL/TLS (Let's Encrypt)                  │
│  - Gzip compression                         │
├─────────────────────────────────────────────┤
│  Spring Boot (Java 17)                      │
│  - Port 8080 (internal)                     │
│  - RAM: ~1.5GB allocated                    │
├─────────────────────────────────────────────┤
│  MySQL 8.0                                  │
│  - RAM: ~512MB allocated                    │
│  - Storage: /var/lib/mysql                  │
├─────────────────────────────────────────────┤
│  File Storage (Local - see FILE_STORAGE_DECISION.md) │
│  - /var/pastcare/uploads (member photos)    │
│  - /var/pastcare/events (event images)      │
│  - /var/pastcare/documents (exports)        │
│  - Capacity: ~50GB (scales to 100 churches) │
│  - Migration to B2 when >40GB used          │
├─────────────────────────────────────────────┤
│  Monitoring                                 │
│  - Netdata (system metrics)                 │
│  - Application logs (/var/log/pastcare)     │
└─────────────────────────────────────────────┘
```

**Memory Allocation:**
- OS + System: ~500MB
- Nginx: ~50MB
- Spring Boot: ~1500MB (with -Xmx1536m -Xms512m)
- MySQL: ~512MB
- File cache + buffers: ~1438MB
- **Total Used: ~4000MB** (fits comfortably in 4GB)

---

## 3. Deployment Costs Breakdown

### Monthly Recurring Costs

| Service | Cost | Notes |
|---------|------|-------|
| **Hetzner VPS** | $6.50 | CPX21 server |
| **Domain Name** | $1.00 | .com domain (Namecheap/Cloudflare) |
| **Cloudflare CDN** | $0.00 | Free tier (caching + DDoS) |
| **SSL Certificate** | $0.00 | Let's Encrypt (free) |
| **Backups** | $1.50 | Hetzner automated backups (20% of server cost) |
| **Email (SendGrid)** | $0.00 | Free tier (100 emails/day) |
| **SMS (Hubtel Ghana)** | $0.00 | Pay-as-you-go (churches pay) |
| **Monitoring** | $0.00 | Netdata (self-hosted) |
| **Total** | **$9.00/month** | **~$108/year** |

### Optional Add-ons

| Service | Cost | When Needed |
|---------|------|-------------|
| Object Storage (Backblaze B2) | $0.005/GB | When file storage > 50GB |
| CDN Bandwidth (Cloudflare Pro) | $20/month | When traffic > 100k visits/month |
| Premium Support | $0 | Community support initially |

### One-Time Setup Costs

| Item | Cost |
|------|------|
| Domain Registration (1 year) | $12 |
| Development Time | $0 (in-house) |
| **Total Setup** | **$12** |

---

## 4. Server Setup Script

### Automated Deployment Script

```bash
#!/bin/bash
# PastCare Production Server Setup
# Run as root on fresh Ubuntu 22.04 LTS

set -e

echo "=== PastCare Production Setup ==="

# 1. Update system
apt update && apt upgrade -y

# 2. Install Java 17
apt install -y openjdk-17-jdk-headless

# 3. Install MySQL 8.0
apt install -y mysql-server
mysql_secure_installation

# 4. Install Nginx
apt install -y nginx

# 5. Install certbot for SSL
apt install -y certbot python3-certbot-nginx

# 6. Create application user
useradd -m -s /bin/bash pastcare
mkdir -p /opt/pastcare
mkdir -p /var/pastcare/{uploads,events,backups}
chown -R pastcare:pastcare /var/pastcare

# 7. Configure MySQL
mysql -u root <<EOF
CREATE DATABASE pastcare CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pastcare'@'localhost' IDENTIFIED BY 'CHANGE_THIS_PASSWORD';
GRANT ALL PRIVILEGES ON pastcare.* TO 'pastcare'@'localhost';
FLUSH PRIVILEGES;
EOF

# 8. Configure MySQL for production
cat > /etc/mysql/mysql.conf.d/pastcare.cnf <<EOF
[mysqld]
innodb_buffer_pool_size = 512M
max_connections = 100
query_cache_size = 0
query_cache_type = 0
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
EOF

systemctl restart mysql

# 9. Install Netdata for monitoring
bash <(curl -Ss https://my-netdata.io/kickstart.sh) --dont-wait

# 10. Configure firewall
ufw allow 22/tcp   # SSH
ufw allow 80/tcp   # HTTP
ufw allow 443/tcp  # HTTPS
ufw --force enable

echo "=== Setup Complete ==="
echo "Next steps:"
echo "1. Deploy Spring Boot JAR to /opt/pastcare/"
echo "2. Deploy Angular dist/ to /var/www/pastcare/"
echo "3. Configure Nginx (see deployment guide)"
echo "4. Set up SSL with: certbot --nginx -d yourdomain.com"
```

---

## 5. Application Deployment

### Spring Boot Service Configuration

**File: `/etc/systemd/system/pastcare.service`**

```ini
[Unit]
Description=PastCare Spring Boot Application
After=syslog.target mysql.service

[Service]
User=pastcare
ExecStart=/usr/bin/java \
  -Xms512m \
  -Xmx1536m \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  -jar /opt/pastcare/pastcare.jar
SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=pastcare

# Environment variables
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/pastcare?useSSL=false&serverTimezone=UTC"
Environment="SPRING_DATASOURCE_USERNAME=pastcare"
Environment="SPRING_DATASOURCE_PASSWORD=CHANGE_THIS_PASSWORD"
Environment="JWT_SECRET=CHANGE_THIS_SECRET_KEY_MIN_256_BITS"
Environment="PAYSTACK_SECRET_KEY=YOUR_PAYSTACK_SECRET_KEY"
Environment="HUBTEL_CLIENT_ID=YOUR_HUBTEL_CLIENT_ID"
Environment="HUBTEL_CLIENT_SECRET=YOUR_HUBTEL_CLIENT_SECRET"
Environment="FILE_UPLOAD_DIR=/var/pastcare/uploads"

[Install]
WantedBy=multi-user.target
```

**Enable and start service:**
```bash
systemctl daemon-reload
systemctl enable pastcare
systemctl start pastcare
systemctl status pastcare
```

---

## 6. Nginx Configuration

**File: `/etc/nginx/sites-available/pastcare`**

```nginx
# Frontend (Angular)
server {
    listen 80;
    server_name pastcare.app www.pastcare.app;

    # Let's Encrypt challenge
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # Redirect to HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name pastcare.app www.pastcare.app;

    # SSL Configuration (managed by certbot)
    ssl_certificate /etc/letsencrypt/live/pastcare.app/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/pastcare.app/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/json application/xml+rss application/rss+xml font/truetype font/opentype application/vnd.ms-fontobject image/svg+xml;

    # Frontend static files
    root /var/www/pastcare;
    index index.html;

    # API proxy
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # File uploads
    location /uploads/ {
        alias /var/pastcare/uploads/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Angular routing (SPA)
    location / {
        try_files $uri $uri/ /index.html;
        expires 1h;
        add_header Cache-Control "public, must-revalidate";
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Max upload size (for member photos, event images)
    client_max_body_size 10M;
}
```

**Enable site:**
```bash
ln -s /etc/nginx/sites-available/pastcare /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx
```

---

## 7. Database Migration & Seeding

### Initial Database Setup

```bash
# 1. Copy migration files to server
scp -r src/main/resources/db/migration/ pastcare@server:/tmp/

# 2. Application will auto-run Flyway migrations on first start
# Check logs: journalctl -u pastcare -f

# 3. Seed initial subscription plans
mysql -u pastcare -p pastcare <<EOF
-- Insert FREE STARTER plan
INSERT INTO subscription_plans (name, display_name, description, price, storage_limit_mb, user_limit, is_free, is_active, display_order)
VALUES ('STARTER', 'Free Starter', 'Perfect for small churches getting started', 0.00, 1024, 5, TRUE, TRUE, 1);

-- Insert STANDARD paid plan
INSERT INTO subscription_plans (name, display_name, description, price, storage_limit_mb, user_limit, is_free, is_active, display_order)
VALUES ('STANDARD', 'PastCare Standard', 'Everything you need to manage your church', 9.99, 2048, NULL, FALSE, TRUE, 2);

-- Create superadmin user (CHANGE PASSWORD!)
-- Password is hashed 'admin123' - MUST CHANGE after first login
INSERT INTO users (username, email, password, role, is_active, is_superadmin)
VALUES ('admin', 'admin@pastcare.app', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'SUPERADMIN', TRUE, TRUE);

-- Create initial church for testing
INSERT INTO churches (name, email, phone, address, city, state, country, timezone)
VALUES ('Demo Church', 'demo@pastcare.app', '+233123456789', '123 Main St', 'Accra', 'Greater Accra', 'Ghana', 'Africa/Accra');

-- Create free subscription for demo church
INSERT INTO church_subscriptions (church_id, plan_id, status, auto_renew)
SELECT c.id, sp.id, 'ACTIVE', FALSE
FROM churches c, subscription_plans sp
WHERE c.name = 'Demo Church' AND sp.name = 'STARTER';
EOF
```

---

## 8. Deployment Workflow (CI/CD)

### Manual Deployment (Initial)

```bash
# 1. Build backend
cd pastcare-spring
./mvnw clean package -DskipTests

# 2. Build frontend
cd ../past-care-spring-frontend
npm run build

# 3. Deploy to server
scp target/pastcare-spring-0.0.1-SNAPSHOT.jar pastcare@server:/opt/pastcare/pastcare.jar
scp -r dist/past-care-spring-frontend/* pastcare@server:/var/www/pastcare/

# 4. Restart application
ssh pastcare@server "sudo systemctl restart pastcare"

# 5. Verify deployment
curl -I https://pastcare.app
```

### GitHub Actions (Automated - Future)

**File: `.github/workflows/deploy.yml`** (when ready)

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up Java 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temus'

      - name: Build backend
        run: ./mvnw clean package -DskipTests

      - name: Build frontend
        run: |
          cd past-care-spring-frontend
          npm ci
          npm run build

      - name: Deploy to server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: pastcare
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          source: "target/*.jar,past-care-spring-frontend/dist/*"
          target: "/tmp/deploy"

      - name: Restart application
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: pastcare
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            sudo cp /tmp/deploy/target/*.jar /opt/pastcare/pastcare.jar
            sudo cp -r /tmp/deploy/past-care-spring-frontend/dist/* /var/www/pastcare/
            sudo systemctl restart pastcare
```

---

## 9. Backup Strategy

### Automated Daily Backups

**File: `/usr/local/bin/pastcare-backup.sh`**

```bash
#!/bin/bash
# Daily backup script for PastCare

BACKUP_DIR="/var/pastcare/backups"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# 1. Database backup
mysqldump -u pastcare -p'CHANGE_THIS_PASSWORD' pastcare | gzip > "$BACKUP_DIR/db_$DATE.sql.gz"

# 2. Files backup
tar -czf "$BACKUP_DIR/files_$DATE.tar.gz" /var/pastcare/uploads /var/pastcare/events

# 3. Cleanup old backups (keep last 30 days)
find "$BACKUP_DIR" -type f -name "*.gz" -mtime +$RETENTION_DAYS -delete

# 4. Optional: Upload to Backblaze B2 (when storage grows)
# b2 sync "$BACKUP_DIR" b2://pastcare-backups/

echo "Backup completed: $DATE"
```

**Cron job (daily at 2 AM):**
```bash
crontab -e
0 2 * * * /usr/local/bin/pastcare-backup.sh >> /var/log/pastcare-backup.log 2>&1
```

**Hetzner Automated Backups:**
- Enable in Hetzner Cloud Console
- Creates full server snapshots daily
- Keeps last 7 snapshots rotating
- Cost: 20% of server price (~$1.30/month)

---

## 10. Monitoring & Alerts

### Netdata (Free, Self-Hosted)

**Access**: `http://your-server-ip:19999`

**Metrics Monitored:**
- CPU usage
- Memory usage
- Disk I/O
- Network traffic
- MySQL queries/second
- Java JVM metrics
- Nginx requests/second

**Alert Configuration** (`/etc/netdata/health.d/pastcare.conf`):

```bash
# CPU usage alert
alarm: high_cpu_usage
   on: system.cpu
lookup: average -3m percentage of user,system
 every: 1m
  warn: $this > 70
  crit: $this > 90
  info: CPU usage is high

# Memory alert
alarm: high_memory_usage
   on: system.ram
lookup: average -3m percentage of used
 every: 1m
  warn: $this > 80
  crit: $this > 95
  info: Memory usage is high

# Disk space alert
alarm: low_disk_space
   on: disk.space
lookup: average -10m percentage of used
 every: 5m
  warn: $this > 80
  crit: $this > 90
  info: Disk space is low

# MySQL connection alert
alarm: mysql_connections
   on: mysql.connections
lookup: average -5m
 every: 1m
  warn: $this > 80
  crit: $this > 95
  info: MySQL connection limit approaching
```

### Application Logs

**Log locations:**
- Application: `journalctl -u pastcare -f`
- Nginx access: `/var/log/nginx/access.log`
- Nginx error: `/var/log/nginx/error.log`
- MySQL error: `/var/log/mysql/error.log`

**Log rotation** (automatic via systemd/logrotate):
```bash
# /etc/logrotate.d/pastcare
/var/log/nginx/*.log {
    daily
    rotate 14
    compress
    delaycompress
    missingok
    notifempty
    create 0640 www-data adm
}
```

---

## 11. Scaling Plan (When Needed)

### Growth Thresholds

| Metric | Current Capacity | Upgrade Trigger | Next Step |
|--------|------------------|-----------------|-----------|
| Churches | 100-200 | 150+ churches | Upgrade to CPX31 (8GB RAM) |
| Storage | 50GB | 40GB used | Add Backblaze B2 object storage |
| Traffic | 50k req/day | 40k req/day | Enable Cloudflare Pro CDN |
| Database | 5GB | 4GB | Separate MySQL to dedicated server |

### Scaling Steps

**Level 1: Vertical Scaling** (Month 3-6)
- Upgrade to Hetzner CPX31: 4 vCPU, 8GB RAM, 160GB SSD
- Cost: €11.90/month (~$13/month)
- No architecture changes needed

**Level 2: Object Storage** (Month 6-12)
- Move uploads to Backblaze B2
- 50GB storage: $0.25/month
- CDN bandwidth: $0.01/GB
- Estimated cost: $2-5/month

**Level 3: Database Separation** (Month 12+)
- Separate MySQL server (Hetzner CX21)
- Cost: +€4.51/month (~$5/month)
- Better performance isolation

**Level 4: Load Balancing** (Year 2+)
- 2x application servers behind load balancer
- 1x dedicated MySQL server
- Object storage for all files
- Total cost: ~$40/month

---

## 12. Security Checklist

### Pre-Deployment Security

- [ ] Change all default passwords (MySQL, admin user)
- [ ] Generate strong JWT secret (256+ bits)
- [ ] Configure Paystack webhook signatures
- [ ] Set up SSL/TLS with Let's Encrypt
- [ ] Configure firewall (UFW): only ports 22, 80, 443
- [ ] Disable MySQL remote access (bind to localhost)
- [ ] Create limited database user (not root)
- [ ] Set up SSH key authentication (disable password login)
- [ ] Configure fail2ban for SSH brute force protection
- [ ] Enable automatic security updates

### Post-Deployment Security

- [ ] Configure CORS properly (only production domain)
- [ ] Set up rate limiting (Nginx + Spring)
- [ ] Enable database query logging (slow queries)
- [ ] Set up security headers (CSP, HSTS, X-Frame-Options)
- [ ] Configure session timeout (30 minutes)
- [ ] Implement request size limits (10MB max)
- [ ] Set up intrusion detection (fail2ban)
- [ ] Regular security audit logs review
- [ ] Keep dependencies updated (monthly)

---

## 13. Pre-Launch Checklist

### Technical

- [ ] Database migrations tested
- [ ] Seed data loaded (plans, superadmin)
- [ ] Environment variables configured
- [ ] SSL certificate active
- [ ] Backups scheduled and tested
- [ ] Monitoring alerts configured
- [ ] Domain DNS configured (A record + www CNAME)
- [ ] Email service configured (SendGrid/SMTP)
- [ ] SMS service configured (Hubtel)
- [ ] Payment gateway tested (Paystack sandbox → production)

### Application

- [ ] Landing page accessible
- [ ] Registration flow works
- [ ] Login/authentication works
- [ ] Payment initialization works
- [ ] Payment verification works
- [ ] Dashboard loads for authenticated users
- [ ] File uploads work (member photos)
- [ ] SMS sending works (test credits)
- [ ] Superadmin panel accessible
- [ ] Subscription management works

### Performance

- [ ] Frontend build optimized (prod mode)
- [ ] Backend JVM tuned (-Xmx1536m -XX:+UseG1GC)
- [ ] MySQL query cache disabled (InnoDB buffer pool 512MB)
- [ ] Nginx gzip enabled
- [ ] Static assets cached (1 year)
- [ ] API response times < 500ms
- [ ] Page load times < 3 seconds

---

## 14. Deployment Timeline

### Week 1: Server Setup
- **Day 1**: Provision Hetzner VPS, configure DNS
- **Day 2**: Run server setup script, install dependencies
- **Day 3**: Configure Nginx, get SSL certificate
- **Day 4**: Set up MySQL, run migrations
- **Day 5**: Deploy application, test basic functionality
- **Day 6**: Configure monitoring, backups
- **Day 7**: Security hardening, final testing

### Week 2: Testing & Launch
- **Day 8-10**: End-to-end testing (auth, payments, features)
- **Day 11-12**: Load testing with simulated users
- **Day 13**: Final security audit
- **Day 14**: **PRODUCTION LAUNCH** 🚀

---

## 15. Post-Launch Support

### First 30 Days

**Daily:**
- Check error logs (`journalctl -u pastcare --since "1 hour ago"`)
- Monitor server metrics (Netdata)
- Verify backups completed

**Weekly:**
- Review security audit logs
- Check disk space usage
- Update dependencies (if security patches)
- Review user feedback

**Monthly:**
- Performance review (response times, uptime)
- Cost analysis (actual vs projected)
- Feature usage analytics
- Plan capacity upgrades if needed

---

## 16. Cost Projection (First Year)

### Monthly Breakdown

| Month | Churches | Storage | VPS | Add-ons | Total |
|-------|----------|---------|-----|---------|-------|
| 1-3 | 1-20 | 5GB | $6.50 | $2.50 | **$9.00** |
| 4-6 | 20-50 | 15GB | $6.50 | $2.50 | **$9.00** |
| 7-9 | 50-100 | 30GB | $13.00 | $5.00 | **$18.00** |
| 10-12 | 100-150 | 45GB | $13.00 | $7.00 | **$20.00** |

**Year 1 Total Cost**: ~$162
**Revenue Target (100 churches @ $9.99)**: $999/month by Month 12
**Profit Margin**: ~98% ($979 profit on $999 revenue)

---

## 17. Quick Reference

### Essential Commands

```bash
# View application logs
journalctl -u pastcare -f

# Restart application
sudo systemctl restart pastcare

# Check application status
sudo systemctl status pastcare

# Reload Nginx config
sudo nginx -t && sudo systemctl reload nginx

# View database
mysql -u pastcare -p pastcare

# Manual backup
/usr/local/bin/pastcare-backup.sh

# Check disk space
df -h

# Check memory usage
free -h

# View active connections
netstat -tulpn | grep LISTEN
```

### Important Files

| File | Purpose |
|------|---------|
| `/opt/pastcare/pastcare.jar` | Spring Boot application |
| `/var/www/pastcare/` | Frontend (Angular) |
| `/etc/systemd/system/pastcare.service` | Service configuration |
| `/etc/nginx/sites-available/pastcare` | Nginx configuration |
| `/var/pastcare/uploads/` | File storage |
| `/var/pastcare/backups/` | Database backups |
| `/etc/letsencrypt/` | SSL certificates |

---

## 18. Troubleshooting

### Application Won't Start

```bash
# Check logs
journalctl -u pastcare -n 100 --no-pager

# Common issues:
# 1. Database connection failed → Check MySQL running, credentials
# 2. Port 8080 in use → Check for other processes
# 3. Out of memory → Check JVM settings, server RAM
```

### High Memory Usage

```bash
# Check Java heap usage
jcmd $(pgrep -f pastcare.jar) GC.heap_info

# Reduce heap size if needed
# Edit /etc/systemd/system/pastcare.service
# Change -Xmx1536m to -Xmx1024m
```

### Database Performance Issues

```bash
# Check slow queries
mysql -u pastcare -p -e "SHOW PROCESSLIST;"

# Optimize tables
mysql -u pastcare -p pastcare -e "OPTIMIZE TABLE church_subscriptions, payments;"
```

---

## 19. Summary

### Total Setup Cost
- **Server**: $6.50/month
- **Domain**: $1/month
- **Backups**: $1.50/month
- **Total**: **$9/month** (~$108/year)

### What You Get
✅ Full production environment
✅ 4GB RAM, 3 vCPU, 80GB storage
✅ Automated daily backups
✅ SSL/HTTPS enabled
✅ Monitoring with Netdata
✅ 99.9% uptime SLA (Hetzner)
✅ Room for 100-200 churches
✅ 20TB bandwidth included

### Deployment Time
- **Automated setup**: 2 hours
- **Testing & verification**: 1 day
- **Total to production**: 7-14 days

---

**Document Version**: 1.0
**Last Updated**: 2025-12-29
**Status**: Ready for Deployment 🚀
