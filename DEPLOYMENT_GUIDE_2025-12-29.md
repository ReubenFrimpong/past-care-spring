## Deployment Guide: Storage Calculation & RBAC Context

**Date:** 2025-12-29
**Features:** Database Storage Calculation + Multi-Layer RBAC Context
**Status:** ✅ Ready for Deployment

---

## 📋 Pre-Deployment Checklist

### 1. Code Status
- ✅ All code compiled successfully (510 source files)
- ✅ No build errors
- ✅ Security features implemented
- ✅ Monitoring infrastructure added

### 2. Database Requirements
- ✅ V55: `storage_usage` table
- ✅ V56: Performance indexes for tenant isolation
- ✅ V57: `security_audit_logs` table
- ⚠️ **ACTION REQUIRED:** Run database migrations

### 3. Configuration Changes
- ✅ Flyway enabled in application.properties
- ✅ SQL logging enabled for testing
- ✅ Hibernate filter logging enabled
- ⚠️ **ACTION REQUIRED:** Review logging levels for production

---

## 🚀 Deployment Steps

### Step 1: Database Migration

```bash
# Option A: Using Maven
./mvnw flyway:migrate

# Option B: Migrations run automatically on application startup
# (since spring.flyway.enabled=true)
```

**Expected Result:**
```
Flyway: Migrating schema to version 55 - Create storage usage table
Flyway: Migrating schema to version 56 - Add tenant isolation indexes
Flyway: Migrating schema to version 57 - Create security audit logs table
Flyway: Successfully applied 3 migrations
```

**Verify Migrations:**
```bash
# Check storage_usage table
mysql -u root -p past-care-spring -e "DESCRIBE storage_usage;"

# Check security_audit_logs table
mysql -u root -p past-care-spring -e "DESCRIBE security_audit_logs;"

# Check indexes
mysql -u root -p past-care-spring -e "SHOW INDEX FROM members WHERE Key_name LIKE 'idx_%';"
```

### Step 2: Start Application

```bash
# Clean build and start
./mvnw clean spring-boot:run

# Or build JAR and run
./mvnw clean package -DskipTests
java -jar target/pastcare-spring-0.0.1-SNAPSHOT.jar
```

**Watch for startup logs:**
```
✓ Flyway migrations successful
✓ Hibernate filters configured
✓ SecurityMonitoringService initialized
✓ StorageCalculationService scheduled job registered
✓ Application started on port 8080
```

### Step 3: Verify Deployment

Run the automated test script:

```bash
# Without authentication (basic checks)
./test-storage-and-rbac.sh

# With authentication (full tests)
export JWT_TOKEN='your_jwt_token'
./test-storage-and-rbac.sh
```

**Manual Verification:**

```bash
# 1. Test Storage Calculation
curl -X POST http://localhost:8080/api/storage-usage/calculate \
  -H "Authorization: Bearer $JWT_TOKEN"

# 2. Get Current Usage
curl -X GET http://localhost:8080/api/storage-usage/current \
  -H "Authorization: Bearer $JWT_TOKEN"

# 3. Test RBAC Protection (should return 403 or 404)
curl -X GET http://localhost:8080/api/members/99999999 \
  -H "Authorization: Bearer $JWT_TOKEN"

# 4. Security Stats (requires PLATFORM_ACCESS)
curl -X GET http://localhost:8080/api/security/stats \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 🔍 Monitoring & Verification

### Check Hibernate Filters

Enable SQL logging and make a query:

```bash
# Check application logs for:
grep "Hibernate filter 'churchFilter' enabled" logs/application.log

# Should see:
# "Hibernate filter 'churchFilter' enabled for church ID: X"
```

### Verify Tenant Isolation

```bash
# Enable SQL trace logging
tail -f logs/application.log | grep "SELECT.*WHERE.*church_id"

# Make requests and verify queries include:
# WHERE ... AND church_id = ?
```

### Monitor Security Violations

```bash
# Check database for logged violations
mysql -u root -p past-care-spring -e \
  "SELECT * FROM security_audit_logs ORDER BY timestamp DESC LIMIT 10;"

# Query security stats via API
curl -X GET http://localhost:8080/api/security/stats \
  -H "Authorization: Bearer $PLATFORM_ADMIN_TOKEN" | jq
```

### Check Storage Calculations

```bash
# View storage usage records
mysql -u root -p past-care-spring -e \
  "SELECT church_id, file_storage_mb, database_storage_mb, total_storage_mb, calculated_at
   FROM storage_usage ORDER BY calculated_at DESC LIMIT 10;"

# Verify scheduled job runs (daily at 2 AM)
grep "Calculating storage for all churches" logs/application.log
```

---

## ⚙️ Configuration for Production

### 1. Adjust Logging Levels

**For Production:**

```properties
# Reduce SQL logging verbosity
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# Keep security logging at INFO
logging.level.com.reuben.pastcare_spring.security=INFO
logging.level.com.reuben.pastcare_spring.services.SecurityMonitoringService=INFO
logging.level.com.reuben.pastcare_spring.config.HibernateFilterInterceptor=INFO
```

**For Development/Staging (current settings):**
```properties
# Verbose logging for testing
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### 2. Storage Calculation Schedule

Current: Daily at 2 AM
Customizable in `StorageCalculationService.java`:

```java
@Scheduled(cron = "0 0 2 * * *")  // 2 AM daily
// Or customize:
// @Scheduled(cron = "0 0 */6 * * *")  // Every 6 hours
// @Scheduled(cron = "0 0 3 * * SUN")  // 3 AM every Sunday
```

### 3. Security Alert Thresholds

Current: 5 violations in 24 hours triggers alert
Customizable in `SecurityMonitoringService.java`:

```java
if (recentViolations >= 5) {  // Adjust threshold
    log.warn("SECURITY ALERT: User {} has {} violations", userId, recentViolations);
    // TODO: Send email alert
    // TODO: Auto-suspend account
}
```

---

## 📊 Performance Optimization

### Database Indexes

All required indexes created in V56 migration:

```sql
-- Tenant filtering (composite indexes)
idx_members_id_church_id
idx_donations_id_church_id
idx_events_id_church_id
... (30+ indexes total)

-- Storage usage
idx_storage_usage_church_calculated

-- Security audit
idx_user_timestamp
idx_church_timestamp
```

**Verify Index Usage:**

```sql
-- Check query execution plan
EXPLAIN SELECT * FROM members WHERE id = 123 AND church_id = 5;

-- Should use: idx_members_id_church_id (composite index)
```

### Performance Monitoring

```bash
# Monitor slow queries
mysql -u root -p -e "SHOW PROCESSLIST;"

# Check index statistics
mysql -u root -p past-care-spring -e \
  "SELECT * FROM information_schema.STATISTICS
   WHERE table_schema = 'past-care-spring'
   AND index_name LIKE 'idx_%';"
```

---

## 🔐 Security Features Summary

### Multi-Layer Defense

| Layer | Component | Protection |
|-------|-----------|------------|
| **Layer 1** | Hibernate Filters | Auto-adds `WHERE church_id = ?` to ALL queries |
| **Layer 2** | Service Validation | Explicit checks in 55+ methods across 10 services |
| **Layer 3** | Exception Handling | Logs violations, returns 403, alerts on patterns |

### Security Endpoints

```
GET  /api/security/stats                    - Platform admin only
GET  /api/security/violations/recent        - Platform admin only
GET  /api/security/violations/user/{userId} - Platform admin only
GET  /api/security/violations/church/{id}   - Church admin or platform admin
```

### Storage Endpoints

```
GET  /api/storage-usage/current             - Current church usage
GET  /api/storage-usage/history             - Historical usage data
POST /api/storage-usage/calculate           - Manual calculation trigger
```

---

## 🐛 Troubleshooting

### Issue: Migrations Fail

**Error:** `Table 'storage_usage' already exists`

**Solution:**
```bash
# Check Flyway schema history
mysql -u root -p past-care-spring -e "SELECT * FROM flyway_schema_history;"

# If needed, repair
./mvnw flyway:repair
./mvnw flyway:migrate
```

### Issue: Hibernate Filter Not Applied

**Symptoms:** Queries don't include `WHERE church_id = ?`

**Checks:**
```bash
# 1. Verify interceptor is registered
grep "HibernateFilterInterceptor" logs/application.log

# 2. Check tenant context is set
grep "TenantContext.setCurrentChurchId" logs/application.log

# 3. Verify filter enabled
grep "Hibernate filter 'churchFilter' enabled" logs/application.log
```

**Solution:**
- Ensure request is authenticated
- Check JWT contains church ID
- Verify interceptor path patterns match

### Issue: Storage Calculation Returns 0

**Symptoms:** All storage values are 0.00 MB

**Checks:**
```bash
# 1. Check upload directories exist
ls -la ~/pastcare-uploads/churches/*/

# 2. Verify database has data
mysql -u root -p past-care-spring -e "SELECT COUNT(*) FROM members;"

# 3. Check calculation logs
grep "Calculating storage for church" logs/application.log
```

**Solution:**
- Create upload directories if missing
- Verify data exists in database
- Trigger manual calculation: `POST /api/storage-usage/calculate`

### Issue: Security Violations Not Logged

**Symptoms:** No entries in `security_audit_logs` table

**Checks:**
```bash
# 1. Trigger a test violation
curl -X GET http://localhost:8080/api/members/999999999 \
  -H "Authorization: Bearer $JWT_TOKEN"

# 2. Check exception handler logs
grep "SECURITY VIOLATION" logs/application.log

# 3. Verify table exists
mysql -u root -p past-care-spring -e "DESC security_audit_logs;"
```

**Solution:**
- Run V57 migration
- Verify GlobalExceptionHandler is calling SecurityMonitoringService
- Check database connection

---

## 📈 Monitoring Dashboards

### Recommended Metrics

**Storage Usage:**
- Total storage per church
- Growth rate (MB/day)
- Churches approaching limits (>80%)
- File vs database storage ratio

**Security:**
- Violations per day
- Top violating users
- Violations by church
- Unreviewed high-severity violations

**Performance:**
- Query execution time with filters
- Index hit rate
- Slow query count

### Sample Queries for Dashboards

```sql
-- Churches approaching storage limit
SELECT c.name, su.total_storage_mb,
       (su.total_storage_mb / 2048.0 * 100) as usage_percent
FROM storage_usage su
JOIN church c ON su.church_id = c.id
WHERE su.calculated_at = (
    SELECT MAX(calculated_at) FROM storage_usage WHERE church_id = su.church_id
)
AND su.total_storage_mb > 1638  -- >80% of 2GB
ORDER BY usage_percent DESC;

-- Security violations last 24 hours
SELECT COUNT(*) as violation_count,
       DATE_FORMAT(timestamp, '%Y-%m-%d %H:00:00') as hour
FROM security_audit_logs
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY hour
ORDER BY hour;

-- Top violating users
SELECT user_id, COUNT(*) as violations,
       MAX(timestamp) as last_violation
FROM security_audit_logs
WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY user_id
HAVING violations > 3
ORDER BY violations DESC;
```

---

## 🔄 Rollback Plan

If issues occur, rollback steps:

### 1. Code Rollback

```bash
# Revert to previous commit
git log --oneline | head -5  # Find previous commit
git revert <commit-hash>
./mvnw clean package
```

### 2. Database Rollback

```bash
# Drop new tables (data loss!)
mysql -u root -p past-care-spring << EOF
DROP TABLE IF EXISTS security_audit_logs;
DROP TABLE IF EXISTS storage_usage;
EOF

# Drop indexes (optional, improves rollback safety)
mysql -u root -p past-care-spring << EOF
DROP INDEX idx_members_church_id ON members;
DROP INDEX idx_donations_church_id ON donations;
-- ... repeat for all indexes
EOF
```

### 3. Configuration Rollback

```properties
# Disable Flyway
spring.flyway.enabled=false

# Reduce logging
logging.level.org.hibernate.SQL=WARN
logging.level.com.reuben.pastcare_spring=INFO
```

---

## ✅ Post-Deployment Validation

### Day 1 (Deployment Day)

- [ ] All migrations applied successfully
- [ ] Application starts without errors
- [ ] Storage calculation runs and returns data
- [ ] Hibernate filters appear in query logs
- [ ] No security violations in legitimate requests

### Week 1

- [ ] Scheduled storage job runs at 2 AM daily
- [ ] Storage data shows accurate file + DB sizes
- [ ] Security audit logs capture violations (if any)
- [ ] No performance degradation observed
- [ ] Indexes showing usage in EXPLAIN queries

### Month 1

- [ ] Storage trends visible (90-day history)
- [ ] Security patterns identified
- [ ] Performance metrics stable
- [ ] No cross-tenant data leaks reported
- [ ] Churches viewing storage in UI (if implemented)

---

## 📚 Documentation References

- **Storage Calculation:** See `STORAGE_CALCULATION_IMPLEMENTATION_COMPLETE.md`
- **RBAC Context:** See `RBAC_CONTEXT_IMPLEMENTATION_COMPLETE.md`
- **Session Summary:** See `SESSION_2025-12-29_RBAC_CONTEXT_AND_STORAGE_COMPLETE.md`

---

## 🎯 Next Steps

### Immediate (Next 1-2 Days)

1. **Run Deployment Steps** (above)
2. **Verify All Features** using test script
3. **Monitor Logs** for first 24 hours
4. **Adjust Logging** for production if needed

### Short Term (Next 1-2 Weeks)

1. **Frontend Integration:**
   - Display storage usage in church settings
   - Show storage breakdown charts
   - Add upgrade prompts when approaching limits

2. **Security Dashboard:**
   - Create admin panel for security stats
   - Add violation review interface
   - Implement email alerts for threshold breaches

3. **Testing:**
   - Load testing with multiple churches
   - Cross-tenant access penetration testing
   - Storage calculation accuracy validation

### Long Term (Next 1-3 Months)

1. **Enhanced Monitoring:**
   - Integrate with monitoring tools (Prometheus, Grafana)
   - Set up automated alerts
   - Create executive dashboards

2. **Billing Integration:**
   - Connect storage usage to billing system
   - Automated overage charges
   - Storage upgrade workflows

3. **Performance Optimization:**
   - Profile query performance with large datasets
   - Optimize index usage based on actual patterns
   - Consider archiving old security logs

---

**Deployment Owner:** Development Team
**Approved By:** _________________
**Deployment Date:** _________________
**Rollback Contact:** _________________

---

✅ **Ready for Production Deployment**
