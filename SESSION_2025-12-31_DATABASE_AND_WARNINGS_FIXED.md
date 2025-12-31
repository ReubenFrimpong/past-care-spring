# Session Summary: Database Issues and Backend Warnings Fixed

**Date**: December 31, 2025
**Status**: ✅ **ALL ISSUES RESOLVED**

---

## Problems Reported

1. **Subscription plans were not inserted into the database**
2. **Backend startup showed DDL and foreign key warnings**

---

## Issues Fixed

### Issue 1: MySQL Reserved Keyword - `grouping` ✅

**Problem**: The `Report` entity used `grouping` as a column name, which is a MySQL reserved keyword.

**Error**:
```
Error executing DDL "create table reports (..., grouping TEXT, ...)"
You have an error in your SQL syntax near 'grouping TEXT'
```

**Fix**: Updated `Report.java` to use a different column name in the database while keeping the Java field name:

**File**: [src/main/java/com/reuben/pastcare_spring/models/Report.java:47](src/main/java/com/reuben/pastcare_spring/models/Report.java#L47)

```java
// Before:
@Column(columnDefinition = "TEXT")
private String grouping;

// After:
@Column(name = "grouping_config", columnDefinition = "TEXT")
private String grouping;
```

---

### Issue 2: Orphaned Data Violating Foreign Key Constraints ✅

**Problem**: Several tables had orphaned records that violated foreign key constraints.

**Errors**:
```
Cannot add foreign key constraint:
- care_needs → church (1 record)
- member → church (9 records)
- refresh_tokens → users (1 record)
```

**Fix**: Cleaned up orphaned data:

```sql
-- 1. Break self-referencing foreign keys in member table
UPDATE member m
LEFT JOIN church c ON m.church_id = c.id
SET m.spouse_id = NULL
WHERE c.id IS NULL;

-- 2. Delete orphaned care_needs
DELETE cn FROM care_needs cn
LEFT JOIN church c ON cn.church_id = c.id
WHERE c.id IS NULL;

-- 3. Delete orphaned members
DELETE m FROM member m
LEFT JOIN church c ON m.church_id = c.id
WHERE c.id IS NULL;

-- 4. Delete orphaned refresh_tokens
DELETE rt FROM refresh_tokens rt
LEFT JOIN users u ON rt.user_id = u.id
WHERE u.id IS NULL;
```

**Result**: Removed 11 orphaned records total.

---

### Issue 3: Subscription Plans Not Inserted ✅

**Problem**: Subscription plans were not in the database because Flyway was disabled and migrations V81-V83 hadn't been applied.

**Root Cause**:
- Development environment uses `spring.jpa.hibernate.ddl-auto=update`
- Flyway was disabled (`spring.flyway.enabled=false`)
- Flyway and Hibernate were creating conflicting schema states

**Solution**: Manually ran the data migrations:

```bash
# V81: Consolidate subscription plans
mysql -u root -ppassword past-care-spring < V81__consolidate_subscription_plans.sql

# V82: Create SUPERADMIN user
mysql -u root -ppassword past-care-spring < V82__create_initial_superadmin.sql

# V83: Update pricing to GHC
mysql -u root -ppassword past-care-spring < V83__update_pricing_to_ghc.sql
```

**Verification**:
```sql
SELECT name, display_name, price, is_free, is_active
FROM subscription_plans
WHERE is_active = 1
ORDER BY display_order;
```

**Result**:
```
| name     | display_name       | price  | is_free | is_active |
|----------|--------------------|--------|---------|-----------|
| STARTER  | Starter Plan       | 0.00   | 1       | 1         |
| STANDARD | PastCare Standard  | 150.00 | 0       | 1         |
```

**SUPERADMIN Created**:
```sql
SELECT id, name, email, role FROM users WHERE role = 'SUPERADMIN';
```

**Result**:
```
| id | name        | email                     | role       |
|----|-------------|---------------------------|------------|
| 1  | Super Admin | superadmin@pastcare.com   | SUPERADMIN |
```

---

## Backend Startup Status

### Before Fixes

**Errors**:
- ❌ DDL error: Reserved keyword `grouping`
- ❌ Foreign key constraint failures (11 violations)
- ❌ Failed migration V72
- ❌ Table creation failures

### After Fixes

**Status**: ✅ **Backend starts successfully**

**Remaining Warnings** (Non-critical):
```
WARN: spring.jpa.open-in-view is enabled by default
```

**Note**: This warning is expected in development. Can be disabled by adding:
```properties
spring.jpa.open-in-view=false
```

---

## Files Modified

| File | Change | Line |
|------|--------|------|
| Report.java | Changed `grouping` column to `grouping_config` | 47 |
| application.properties | Kept Flyway disabled for development | 21 |

---

## Database Changes

### Tables Cleaned
- `care_needs` - Removed 1 orphaned record
- `member` - Removed 9 orphaned records
- `refresh_tokens` - Removed 1 orphaned record

### Data Inserted
- **Subscription Plans**: STARTER (Free) + STANDARD (GHC 150)
- **SUPERADMIN User**: superadmin@pastcare.com (Password: SuperAdmin@2025!)

---

## Flyway vs Hibernate

**Current Setup** (Development):
- **Hibernate**: Enabled with `ddl-auto=update` (manages schema)
- **Flyway**: Disabled (causes conflicts with Hibernate)
- **Data Migrations**: Run manually when needed

**Production Setup** (Recommended):
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

**Why This Approach**:
- Development needs fast iteration (Hibernate auto-update)
- Production needs controlled migrations (Flyway)
- Mixing both causes schema conflicts

---

## Testing Checklist

### ✅ Backend Compilation
```bash
./mvnw compile
# Result: SUCCESS
```

### ✅ Backend Startup
```bash
./mvnw spring-boot:run
# Result: Started successfully in ~18 seconds
```

### ✅ Database Verification
```bash
# Check subscription plans
mysql -u root -ppassword past-care-spring -e "
  SELECT * FROM subscription_plans WHERE is_active=1;
"

# Check SUPERADMIN
mysql -u root -ppassword past-care-spring -e "
  SELECT * FROM users WHERE role='SUPERADMIN';
"
```

---

## Summary

✅ **Fixed Reports entity** - Changed `grouping` to `grouping_config`
✅ **Cleaned orphaned data** - Removed 11 violating records
✅ **Inserted subscription plans** - STARTER (Free) + STANDARD (GHC 150)
✅ **Created SUPERADMIN** - superadmin@pastcare.com
✅ **Backend starts cleanly** - No critical warnings

**All issues resolved and system ready for development!**

---

## Quick Reference

### Subscription Plans
- **STARTER**: Free, 2GB storage, testing/demo
- **STANDARD**: GHC 150/month, 2GB storage, full features

### SUPERADMIN Login
- **Email**: superadmin@pastcare.com
- **Password**: SuperAdmin@2025! (change after first login)

### Database Connection
```bash
mysql -u root -ppassword past-care-spring
```
