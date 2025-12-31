# Production Deployment - Quick Start

**⚠️ CRITICAL**: Read [PRODUCTION_DEPLOYMENT_GUIDE.md](PRODUCTION_DEPLOYMENT_GUIDE.md) for complete details.

---

## 🚀 Quick Deploy (5 Steps)

### 1. Create Environment File
```bash
cp .env.production.template .env.production
nano .env.production  # Fill in all values
```

**Required Variables**:
- `DATABASE_URL` - Production database URL
- `DATABASE_PASSWORD` - Strong password
- `JWT_SECRET` - Generate: `openssl rand -base64 64`
- `PAYSTACK_SECRET_KEY` - **sk_live_*** (NOT test key!)
- `PAYSTACK_PUBLIC_KEY` - **pk_live_*** (NOT test key!)
- `FRONTEND_URL` - https://yourdomain.com

### 2. Verify Environment
```bash
source .env.production
./scripts/verify-env.sh
```
Expected: ✅ **All checks passed!**

### 3. Build
```bash
./mvnw clean package -DskipTests
```

### 4. Deploy
```bash
./scripts/deploy-production.sh
```
Script automatically:
- Backs up database ✅
- Stops old app ✅
- Starts new app ✅
- Verifies deployment ✅

### 5. Verify
```bash
curl http://localhost:8080/actuator/health
```
Expected: `{"status":"UP"}`

---

## 🚨 Emergency Rollback

```bash
# 1. Stop app
kill $(cat /var/run/pastcare-spring.pid)

# 2. Find latest backup
ls -lt /var/backups/pastcare/ | head -2

# 3. Restore
gunzip -c /var/backups/pastcare/pre_deploy_backup_*.sql.gz | \
  mysql -u root -p pastcare_production

# 4. Restart old version
java -jar -Dspring.profiles.active=prod target/old-version.jar
```

---

## 📋 Pre-Deployment Checklist

- [ ] Created `.env.production` from template
- [ ] All environment variables filled in (run `./scripts/verify-env.sh`)
- [ ] Using **LIVE** Paystack keys (sk_live_*, pk_live_*)
- [ ] `JWT_SECRET` is 64+ characters
- [ ] Database password is strong (12+ chars)
- [ ] Tested in staging environment
- [ ] Database backup exists
- [ ] Built application: `./mvnw clean package`

---

## ⚙️ Configuration Differences

| Setting | Development | Production |
|---------|------------|-----------|
| **Config File** | `application.properties` | `application-prod.properties` |
| **Profile** | default | `prod` |
| **ddl-auto** | `update` ❌ | `validate` ✅ |
| **Flyway** | disabled | enabled ✅ |
| **Secrets** | hardcoded | env vars ✅ |
| **Paystack** | test keys | live keys ✅ |
| **Cookies** | insecure | secure ✅ |

---

## 📊 Monitoring

```bash
# View logs
tail -f /var/log/pastcare/application.log

# Check health
curl http://localhost:8080/actuator/health

# Check database
mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME \
  -e "SELECT COUNT(*) FROM churches;"

# Check Flyway
mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME \
  -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"
```

---

## 🔄 Database Migrations (Flyway)

### Adding a Column (Example)

1. **Create migration**:
   ```bash
   touch src/main/resources/db/migration/V79__add_phone_to_churches.sql
   ```

2. **Write SQL**:
   ```sql
   ALTER TABLE churches ADD COLUMN phone_number VARCHAR(20);
   ```

3. **Deploy** (Flyway auto-applies):
   ```bash
   ./mvnw clean package
   ./scripts/deploy-production.sh
   ```

---

## 💾 Backups

### Automated Daily Backup
```bash
# Add to crontab
crontab -e

# Daily at 2:00 AM
0 2 * * * export DB_PASSWORD="your_password" && \
  /path/to/scripts/backup-database.sh >> /var/log/pastcare/backup.log 2>&1
```

### Manual Backup
```bash
export DB_PASSWORD="your_password"
./scripts/backup-database.sh
```

---

## 🔐 Security

### Generate Secrets
```bash
# JWT Secret (64+ chars)
openssl rand -base64 64

# Database Password (32 chars)
openssl rand -base64 32

# QR Code Key (exactly 16 chars)
openssl rand -base64 12 | cut -c1-16
```

---

## ❗ Common Issues

### "Port 8080 already in use"
```bash
lsof -ti:8080 | xargs kill -9
```

### "Flyway validation failed"
- Migration files modified after being applied
- Solution: Never modify existing migrations. Create new ones.

### "Table doesn't exist"
- Flyway not enabled or baseline not set
- Check: `spring.flyway.enabled=true` in application-prod.properties

### "Access denied for user"
- Check DATABASE_USERNAME and DATABASE_PASSWORD in .env.production
- Verify database user permissions

---

## 📞 Help

- **Full Guide**: [PRODUCTION_DEPLOYMENT_GUIDE.md](PRODUCTION_DEPLOYMENT_GUIDE.md)
- **Scripts**: `scripts/` directory
- **Config**: `src/main/resources/application-prod.properties`

---

**⚠️ NEVER use `ddl-auto=update` in production!**

**⚠️ ALWAYS backup before deploying!**

**⚠️ ALWAYS use production profile: `-Dspring.profiles.active=prod`**
