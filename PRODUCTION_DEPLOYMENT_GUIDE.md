# Production Deployment Guide - PastCare

**Date**: December 31, 2025
**Status**: 🚨 **CRITICAL - READ BEFORE DEPLOYING**

---

## 🚨 CRITICAL: Why You Lost Data

### What Happened
Your church table was cleared when you restarted the backend because of this setting in `application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=update
```

### Why This Caused Data Loss

**`ddl-auto=update` tells Hibernate to:**
- Automatically modify database schema to match your Java entities
- Drop columns that are no longer in your code
- Potentially drop tables if entity relationships change
- **This can cause irreversible data loss**

### Additional Issues Found

1. **Flyway Disabled**: `spring.flyway.enabled=false` - No version control for database changes
2. **Hardcoded Credentials**: Database credentials in plain text
3. **Test Mode Keys**: Paystack test keys instead of production keys
4. **Development Logging**: Verbose SQL logging impacts performance
5. **Insecure Cookies**: `jwt.cookie.secure=false` - vulnerable to interception

---

## ✅ Solution: Production-Safe Configuration

### 1. Use Production Configuration File

**File Created**: `src/main/resources/application-prod.properties`

**Key Safety Features**:
```properties
# SAFE: Only validates schema, never modifies it
spring.jpa.hibernate.ddl-auto=validate

# SAFE: Flyway manages all database changes with version control
spring.flyway.enabled=true

# SAFE: Environment variables for sensitive data
spring.datasource.url=${DATABASE_URL}
spring.datasource.password=${DATABASE_PASSWORD}
jwt.secret=${JWT_SECRET}

# SAFE: Production security settings
jwt.cookie.secure=true
paystack.test-mode=false
```

---

## 📋 Pre-Deployment Checklist

### Step 1: Environment Variables Setup

Create a `.env` file or configure environment variables on your server:

```bash
# Database Configuration
export DATABASE_URL="jdbc:mysql://your-production-db-host:3306/pastcare_production"
export DATABASE_USERNAME="pastcare_user"
export DATABASE_PASSWORD="your-strong-password-here"

# JWT Configuration
export JWT_SECRET="your-very-long-random-secret-key-at-least-64-chars-long"

# Paystack Configuration (LIVE KEYS)
export PAYSTACK_SECRET_KEY="sk_live_your_live_secret_key"
export PAYSTACK_PUBLIC_KEY="pk_live_your_live_public_key"
export PAYSTACK_WEBHOOK_SECRET="your_webhook_secret"

# QR Code Encryption
export QR_SECRET_KEY="your-16-char-key1"

# Frontend URL
export FRONTEND_URL="https://yourdomain.com"

# Email Configuration
export SMTP_HOST="smtp.gmail.com"
export SMTP_PORT="587"
export SMTP_USERNAME="your-email@gmail.com"
export SMTP_PASSWORD="your-app-specific-password"
export EMAIL_FROM="noreply@yourdomain.com"

# SMS Configuration
export AFRICASTALKING_API_KEY="your_api_key"
export AFRICASTALKING_USERNAME="your_username"
export AFRICASTALKING_SENDER_ID="PASTCARE"

# Upload Directory
export UPLOAD_DIR="/var/pastcare/uploads"

# Application Domain
export APP_DOMAIN="yourdomain.com"
```

### Step 2: Database Backup BEFORE Deployment

**ALWAYS backup your database before deploying:**

```bash
# Create backup
mysqldump -u root -p pastcare_production > backup_$(date +%Y%m%d_%H%M%S).sql

# Verify backup
ls -lh backup_*.sql

# Store backup in safe location
cp backup_*.sql /path/to/safe/backup/location/
```

### Step 3: Flyway Baseline

**First-time production deployment only:**

```bash
# 1. Set Flyway baseline to current schema version
# Edit application-prod.properties:
spring.flyway.baseline-version=78  # Match your current highest migration

# 2. Run application with production profile
java -jar -Dspring.profiles.active=prod target/pastcare-spring.jar

# Flyway will:
# - Create flyway_schema_history table
# - Mark version 78 as baseline
# - Apply any migrations > 78
```

### Step 4: Deploy with Production Profile

```bash
# Build application
./mvnw clean package -DskipTests

# Run with production profile
java -jar -Dspring.profiles.active=prod target/pastcare-spring-0.0.1-SNAPSHOT.jar
```

### Step 5: Verify Deployment

```bash
# 1. Check application started successfully
curl http://localhost:8080/actuator/health

# Expected response:
{"status":"UP"}

# 2. Check Flyway migrations applied
mysql -u $DATABASE_USERNAME -p$DATABASE_PASSWORD -D pastcare_production \
  -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

# 3. Verify NO data loss
mysql -u $DATABASE_USERNAME -p$DATABASE_PASSWORD -D pastcare_production \
  -e "SELECT COUNT(*) as church_count FROM churches;"

# 4. Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"password"}'
```

---

## 🔒 Database Migration Strategy

### Development vs Production

| Environment | ddl-auto | Flyway | Purpose |
|-------------|----------|--------|---------|
| **Development** | `update` | disabled | Fast iteration, schema changes automatic |
| **Production** | `validate` | enabled | Safety, version-controlled changes only |

### How to Make Schema Changes in Production

**NEVER directly modify entities in production. Use Flyway migrations:**

#### Example: Adding a new column

1. **Create migration file**: `src/main/resources/db/migration/V79__add_phone_to_churches.sql`

```sql
-- V79__add_phone_to_churches.sql
ALTER TABLE churches
ADD COLUMN phone_number VARCHAR(20);
```

2. **Test in development**:
```bash
# Switch to Flyway temporarily in dev
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate

./mvnw spring-boot:run
```

3. **Deploy to production**:
```bash
# Backup first!
mysqldump -u root -p pastcare_production > backup_before_v79.sql

# Deploy (Flyway will auto-apply V79)
java -jar -Dspring.profiles.active=prod target/pastcare-spring.jar
```

4. **Verify**:
```bash
mysql -u root -p pastcare_production -e "DESCRIBE churches;"
# Should show phone_number column

mysql -u root -p pastcare_production \
  -e "SELECT version, description, success FROM flyway_schema_history WHERE version='79';"
# Should show V79 as successful
```

---

## 🚨 Emergency Rollback Procedure

### If Deployment Goes Wrong

**1. Stop the application immediately:**
```bash
# Find process
ps aux | grep pastcare-spring

# Kill it
kill -9 <PID>
```

**2. Restore database from backup:**
```bash
# Drop the damaged database (CAREFUL!)
mysql -u root -p -e "DROP DATABASE pastcare_production;"

# Recreate database
mysql -u root -p -e "CREATE DATABASE pastcare_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Restore from backup
mysql -u root -p pastcare_production < backup_YYYYMMDD_HHMMSS.sql

# Verify restoration
mysql -u root -p pastcare_production -e "SELECT COUNT(*) FROM churches;"
```

**3. Investigate issue before redeploying:**
```bash
# Check application logs
tail -f /var/log/pastcare/application.log

# Check Flyway errors
mysql -u root -p pastcare_production \
  -e "SELECT * FROM flyway_schema_history WHERE success=0;"
```

---

## 📁 File Structure for Production

```
pastcare-spring/
├── src/main/resources/
│   ├── application.properties              # Development only
│   ├── application-prod.properties         # Production (NEW)
│   └── db/migration/
│       ├── V1__initial_schema.sql
│       ├── V2__add_users.sql
│       ├── ...
│       └── V78__Create_Event_Images_Table.sql
│
├── .env.production                         # Environment variables (DO NOT COMMIT)
├── PRODUCTION_DEPLOYMENT_GUIDE.md          # This file
└── scripts/
    ├── backup-database.sh                  # Automated backup script
    └── deploy-production.sh                # Deployment script
```

---

## 🔐 Security Best Practices

### 1. Never Commit Secrets
```bash
# Add to .gitignore
echo ".env" >> .gitignore
echo ".env.production" >> .gitignore
echo "application-prod.properties" >> .gitignore  # If you put secrets in it
```

### 2. Use Strong Passwords
```bash
# Generate strong JWT secret (64 characters minimum)
openssl rand -base64 64

# Generate strong database password
openssl rand -base64 32
```

### 3. Paystack Production Keys
- **NEVER** use test keys (`sk_test_*`) in production
- Get live keys from Paystack Dashboard → Settings → API Keys & Webhooks
- Use live keys (`sk_live_*`) in production environment variables

### 4. SSL/TLS Configuration
```properties
# In application-prod.properties
jwt.cookie.secure=true              # Cookies only over HTTPS
server.ssl.enabled=true             # Enable SSL
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}
```

---

## 📊 Monitoring Production

### 1. Database Health Checks

```bash
# Daily automated backup (add to cron)
0 2 * * * /path/to/backup-database.sh

# Monitor table sizes
mysql -u root -p pastcare_production -e "
SELECT
  table_name,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'pastcare_production'
ORDER BY (data_length + index_length) DESC;
"
```

### 2. Application Logs

```bash
# Real-time log monitoring
tail -f /var/log/pastcare/application.log

# Search for errors
grep -i "error" /var/log/pastcare/application.log | tail -20

# Monitor Flyway migrations
grep -i "flyway" /var/log/pastcare/application.log
```

### 3. Health Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Application metrics
curl http://localhost:8080/actuator/metrics

# Database connection pool status
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

---

## 🎯 What Changed to Fix Data Loss Issue

### Before (DANGEROUS - application.properties)
```properties
spring.jpa.hibernate.ddl-auto=update          # ❌ Modifies schema automatically
spring.flyway.enabled=false                   # ❌ No migration version control
spring.datasource.password=password           # ❌ Hardcoded credentials
paystack.test-mode=true                       # ❌ Test mode in production
jwt.cookie.secure=false                       # ❌ Insecure cookies
```

### After (SAFE - application-prod.properties)
```properties
spring.jpa.hibernate.ddl-auto=validate        # ✅ Only validates, never modifies
spring.flyway.enabled=true                    # ✅ Version-controlled migrations
spring.datasource.password=${DATABASE_PASSWORD} # ✅ Environment variable
paystack.test-mode=false                      # ✅ Production mode
jwt.cookie.secure=true                        # ✅ Secure cookies
```

---

## 📝 Deployment Workflow Summary

```
┌─────────────────────────────────────────────────────────────┐
│ BEFORE EVERY DEPLOYMENT                                     │
├─────────────────────────────────────────────────────────────┤
│ 1. ✅ Backup database                                        │
│ 2. ✅ Test in staging environment                            │
│ 3. ✅ Review all Flyway migrations                           │
│ 4. ✅ Verify environment variables set                       │
│ 5. ✅ Build application: ./mvnw clean package                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ DEPLOYMENT                                                  │
├─────────────────────────────────────────────────────────────┤
│ 1. Stop current application (if running)                   │
│ 2. Deploy new JAR file                                     │
│ 3. Start with production profile:                          │
│    java -jar -Dspring.profiles.active=prod app.jar         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ VERIFICATION                                                │
├─────────────────────────────────────────────────────────────┤
│ 1. ✅ Check health endpoint                                  │
│ 2. ✅ Verify Flyway migrations applied                       │
│ 3. ✅ Confirm NO data loss (row counts)                      │
│ 4. ✅ Test critical features (login, subscriptions)          │
│ 5. ✅ Monitor logs for errors                                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ IF ANYTHING GOES WRONG                                      │
├─────────────────────────────────────────────────────────────┤
│ 1. 🚨 STOP APPLICATION IMMEDIATELY                           │
│ 2. 🚨 RESTORE FROM BACKUP                                    │
│ 3. 🚨 INVESTIGATE ISSUE                                      │
│ 4. 🚨 FIX AND RE-TEST IN STAGING                            │
└─────────────────────────────────────────────────────────────┘
```

---

## ❓ FAQ

### Q: Can I use ddl-auto=update in production?
**A: ABSOLUTELY NOT.** This is what caused your data loss. Always use `validate` or `none` in production.

### Q: What if I need to change the database schema in production?
**A: Create a Flyway migration.** See "How to Make Schema Changes in Production" section above.

### Q: How do I know if Flyway is working?
**A: Check the `flyway_schema_history` table:**
```sql
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 10;
```

### Q: What happens if a Flyway migration fails?
**A: The application won't start.** Flyway will mark the migration as failed. You must fix the migration SQL and re-deploy.

### Q: Can I skip Flyway and just use ddl-auto=none?
**A: Not recommended.** Without Flyway, you have no version control for database changes. How will you track what changed and when?

### Q: What if I accidentally deploy with development config?
**A: Your data is at risk.** Immediately stop the application and verify no data was lost. Always use `-Dspring.profiles.active=prod`.

---

## 🎓 Learn More

### Recommended Reading
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Hibernate ddl-auto Explained](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.using-hibernate)
- [Paystack Production Checklist](https://paystack.com/docs/guides/production-checklist/)

### Related Documentation
- [PRICING_MODEL_REVISED.md](PRICING_MODEL_REVISED.md) - Subscription pricing
- [SESSION_2025-12-31_COMPREHENSIVE_UPDATES.md](SESSION_2025-12-31_COMPREHENSIVE_UPDATES.md) - Recent updates
- [BILLING_SYSTEM_COMPLETE.md](BILLING_SYSTEM_COMPLETE.md) - Billing system overview

---

## ✅ Final Checklist Before Production

- [ ] Created `.env.production` with all required environment variables
- [ ] Changed `DATABASE_URL` to production database
- [ ] Changed `DATABASE_PASSWORD` to strong password
- [ ] Generated strong `JWT_SECRET` (64+ characters)
- [ ] Obtained Paystack **LIVE** keys (sk_live_*, pk_live_*)
- [ ] Set `FRONTEND_URL` to production domain
- [ ] Configured SMTP credentials for production emails
- [ ] Set `APP_DOMAIN` to production domain
- [ ] Created upload directory: `/var/pastcare/uploads`
- [ ] Set file permissions on upload directory (writable by app)
- [ ] Verified `application-prod.properties` has correct settings
- [ ] Tested deployment in staging environment first
- [ ] Created database backup
- [ ] Stored backup in safe, off-server location
- [ ] Tested rollback procedure
- [ ] Set up automated daily backups (cron job)
- [ ] Configured log rotation
- [ ] Set up monitoring/alerting for application errors
- [ ] Documented deployment process for team
- [ ] Created runbook for common production issues
- [ ] Tested health endpoints work
- [ ] Verified SSL/TLS certificates installed
- [ ] Configured firewall rules (only necessary ports open)
- [ ] Set up database connection pooling (HikariCP configured)
- [ ] Performance tested with production-like load
- [ ] Security audit completed
- [ ] Compliance requirements met (data protection, GDPR, etc.)

---

**Document Version**: 1.0
**Last Updated**: December 31, 2025
**Author**: PastCare DevOps Team

---

**🚨 REMEMBER: Always backup before deploying. Always test in staging first. Always use production profile.**
