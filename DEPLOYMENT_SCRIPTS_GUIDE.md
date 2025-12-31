# PastCare Deployment Scripts Guide

## Overview

This project includes automated deployment scripts that push code to production and allow you to stream application logs in real-time.

## Available Scripts

### 1. `deploy-backend.sh` - Backend Only Deployment
Deploys the Spring Boot backend application.

**Usage:**
```bash
./deploy-backend.sh
```

**What it does:**
1. Pushes backend code to production remote (`root@62.169.28.116`)
2. Asks if you want to stream application logs
3. If yes, tails the backend application logs via SSH

**Logs streamed:**
- Spring Boot application logs from `/opt/pastcare/logs/backend/application.log`
- Falls back to `journalctl -u pastcare-backend -f` if log file not found

---

### 2. `deploy-frontend.sh` - Frontend Only Deployment
Deploys the Angular frontend application.

**Usage:**
```bash
./deploy-frontend.sh
```

**What it does:**
1. Navigates to `past-care-spring-frontend/` directory
2. Pushes frontend code to production remote
3. Presents log streaming options:
   - Option 1: Nginx access logs (all HTTP requests)
   - Option 2: Nginx error logs (errors only)
   - Option 3: Both logs (combined view)
   - Option 4: Skip log streaming

**Logs streamed:**
- Nginx access logs: `/var/log/nginx/pastcare-access.log`
- Nginx error logs: `/var/log/nginx/pastcare-error.log`

---

### 3. `deploy-all.sh` - Full Stack Deployment
Deploys both backend and frontend together.

**Usage:**
```bash
./deploy-all.sh
```

**What it does:**
1. Pushes backend to production
2. Pushes frontend to production
3. Presents comprehensive log streaming options:
   - Option 1: Backend application logs only
   - Option 2: Frontend access logs only
   - Option 3: Frontend error logs only
   - Option 4: Combined backend + frontend errors
   - Option 5: All logs (backend + nginx access + nginx error)
   - Option 6: Skip log streaming

---

## Prerequisites

1. **SSH Access**: You must have SSH access to the production server:
   ```bash
   ssh root@62.169.28.116
   ```

2. **Git Remotes**: Production remotes must be configured (already done):
   ```bash
   git remote -v
   # Should show:
   # production  root@62.169.28.116:/opt/pastcare/repos/past-care-spring.git
   ```

3. **Executable Permissions**: Scripts must be executable:
   ```bash
   chmod +x deploy-*.sh
   ```

---

## Log Streaming Details

### Backend Logs
Backend logs show Spring Boot application output including:
- Application startup/shutdown
- HTTP requests and responses
- Database queries
- Exception stack traces
- Custom log messages from your code

**Example output:**
```
2025-12-30 14:32:15.123 INFO  [http-nio-8080-exec-5] AuthController : User login successful: superadmin@pastcare.app
2025-12-30 14:32:16.456 DEBUG [http-nio-8080-exec-7] BillingService : Processing subscription renewal for church ID: 42
```

### Frontend Logs (Nginx)

**Access Logs** show all HTTP requests:
```
62.169.28.116 - - [30/Dec/2025:14:32:15 +0000] "GET /api/auth/me HTTP/1.1" 200 512 "https://pastcare.app/dashboard"
62.169.28.116 - - [30/Dec/2025:14:32:16 +0000] "POST /api/billing/subscription HTTP/1.1" 201 1024
```

**Error Logs** show Nginx-level errors:
```
2025/12/30 14:32:17 [error] 1234#1234: *5678 upstream prematurely closed connection while reading response header from upstream
2025/12/30 14:32:18 [warn] 1234#1234: *5679 could not build optimal types_hash
```

---

## Common Workflows

### Quick Backend Fix
```bash
# Make changes to backend code
# Test locally
./mvnw test

# Deploy and watch logs
./deploy-backend.sh
# Press 'y' when prompted to stream logs
# Watch for any errors
# Press Ctrl+C to stop streaming
```

### Frontend Deployment with Error Monitoring
```bash
# Make changes to frontend code
cd past-care-spring-frontend
ng build --configuration=production

# Deploy and monitor errors
cd ..
./deploy-frontend.sh
# Select option 2 (error logs only)
# Watch for any 404s or 500 errors
# Press Ctrl+C to stop
```

### Full Deployment with Comprehensive Monitoring
```bash
# Deploy both applications
./deploy-all.sh
# Select option 4 or 5 to see all errors
# Monitor for a few minutes
# Press Ctrl+C when satisfied
```

---

## Stopping Log Streaming

Press `Ctrl+C` at any time to stop streaming logs and return to your terminal.

---

## Troubleshooting

### SSH Connection Issues
**Problem**: `Permission denied (publickey)`

**Solution**:
1. Ensure your SSH key is added to the production server
2. Test connection: `ssh root@62.169.28.116`

### Log File Not Found
**Problem**: `tail: cannot open '/opt/pastcare/logs/backend/application.log'`

**Solutions**:
- Scripts will automatically fallback to alternative log sources
- Check production server log configuration
- Verify application is running: `ssh root@62.169.28.116 'systemctl status pastcare-backend'`

### Git Push Rejected
**Problem**: `! [rejected] master -> master (non-fast-forward)`

**Solution**:
```bash
# Pull changes first
git pull production master

# Resolve any conflicts
# Then deploy again
./deploy-all.sh
```

---

## Advanced Usage

### Stream Logs Without Deployment

**Backend logs:**
```bash
ssh root@62.169.28.116 'tail -f /opt/pastcare/logs/backend/application.log'
```

**Frontend access logs:**
```bash
ssh root@62.169.28.116 'tail -f /var/log/nginx/pastcare-access.log'
```

**Filtered logs (errors only):**
```bash
ssh root@62.169.28.116 'tail -f /opt/pastcare/logs/backend/application.log | grep -i "error\|exception"'
```

### Custom Log Paths

If your production server uses different log paths, edit the scripts:

```bash
# Edit at the top of each script
BACKEND_LOG_PATH="/your/custom/path/application.log"
NGINX_ACCESS_LOG="/your/custom/path/access.log"
```

---

## Security Notes

- Scripts use root SSH access - ensure your SSH keys are secure
- Logs may contain sensitive data - do not share log output publicly
- Consider using a non-root user with sudo access for production

---

## Quick Reference

| Script | Use Case | Time |
|--------|----------|------|
| `deploy-backend.sh` | Backend code changes only | ~30s |
| `deploy-frontend.sh` | Frontend/UI changes only | ~30s |
| `deploy-all.sh` | Full stack changes | ~1min |

**Production Server**: `root@62.169.28.116`

**Git Remotes**: Already configured as `production`

**Stop Logs**: `Ctrl+C`
