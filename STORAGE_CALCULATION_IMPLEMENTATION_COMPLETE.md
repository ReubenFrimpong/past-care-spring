# Storage Calculation Implementation Complete

## Summary

Successfully implemented a comprehensive storage tracking system that calculates both **file storage** and **database storage** for each church (tenant). This enables accurate billing based on the $9.99/month pricing model with 2 GB base storage.

---

## What Was Implemented

### 1. StorageUsage Entity

**File**: `src/main/java/com/reuben/pastcare_spring/models/StorageUsage.java`

Tracks storage consumption for each church with:
- **File Storage (MB)**: Profile photos, event images, documents, attachments
- **Database Storage (MB)**: Estimated from row counts × average row sizes
- **Total Storage (MB)**: File + Database
- **JSON Breakdowns**: Detailed breakdown by category and entity type
- **Calculated At**: Timestamp of when calculation was performed

**Key Fields**:
```java
private Double fileStorageMb;           // File storage in MB
private Double databaseStorageMb;       // DB storage in MB
private Double totalStorageMb;          // Total = file + db
private String fileStorageBreakdown;    // JSON: {profilePhotos: 50.5, eventImages: 120.3, ...}
private String databaseStorageBreakdown; // JSON: {members: 5.2, donations: 8.5, ...}
private LocalDateTime calculatedAt;     // When calculated
```

### 2. StorageUsageRepository

**File**: `src/main/java/com/reuben/pastcare_spring/repositories/StorageUsageRepository.java`

Repository methods:
- `findFirstByChurchIdOrderByCalculatedAtDesc()` - Get latest usage
- `findByChurchIdAndCalculatedAtBetweenOrderByCalculatedAtDesc()` - Get history
- `findLatestForAllChurches()` - Get latest for all churches (admin)
- `deleteByCalculatedAtBefore()` - Cleanup old records (keep 90 days)

### 3. StorageCalculationService

**File**: `src/main/java/com/reuben/pastcare_spring/services/StorageCalculationService.java`

Core service that calculates storage usage.

#### Scheduled Job
```java
@Scheduled(cron = "0 0 2 * * *")  // Daily at 2 AM
public void calculateStorageForAllChurches()
```

Automatically calculates storage for all churches daily at 2 AM.

#### File Storage Calculation

Scans actual files in the uploads directory:
```
/home/user/pastcare-uploads/churches/{churchId}/
  ├── members/photos/      → profilePhotos
  ├── events/images/       → eventImages
  ├── documents/           → documents
  └── attachments/         → attachments
```

**Method**: `calculateFileStorage(Long churchId)`
- Walks directory tree recursively
- Sums file sizes in bytes
- Converts to megabytes
- Returns breakdown by category

#### Database Storage Estimation

**Method**: `calculateDatabaseStorage(Long churchId)`

Estimates database storage based on:
1. **Row Counts**: Count rows for each entity type per church
2. **Average Row Sizes**: Pre-defined estimates based on typical field sizes

**Entity Size Estimates** (in bytes):
| Entity Type | Avg Size | Rationale |
|-------------|----------|-----------|
| members | 1024 (1 KB) | Name, email, phone, address, dates |
| donations | 512 | Amount, type, date, campaign |
| events | 2048 (2 KB) | Title, description, location, dates |
| visits | 1024 | Member, notes, date, type |
| households | 768 | Name, location, contacts |
| attendance_sessions | 512 | Event, date, stats |
| attendance_records | 128 | Member, session, status |
| campaigns | 1024 | Name, goal, dates, description |
| pledges | 256 | Amount, member, campaign |
| fellowships | 512 | Name, leader, description |
| care_needs | 768 | Member, type, notes, status |
| prayer_requests | 512 | Content, member, date |
| visitors | 512 | Name, contact, visit date |
| users | 384 | Email, password hash, role |
| sms_messages | 256 | Phone, message, status |
| event_images | 128 | Metadata only (path stored) |

**Calculation Formula**:
```
Storage (MB) = (Row Count × Average Size in Bytes) / 1024 / 1024
```

**Example**:
- Church has 500 members
- Storage = (500 × 1024) / 1024 / 1024 = 0.49 MB

#### Public Methods

```java
// Calculate and save storage usage for a church
StorageUsage calculateAndSaveStorageUsage(Long churchId)

// Get latest storage usage (calculates if not exists)
StorageUsage getLatestStorageUsage(Long churchId)

// Get storage history
List<StorageUsage> getStorageUsageHistory(Long churchId, LocalDateTime start, LocalDateTime end)

// Check if over limit
boolean isOverStorageLimit(Long churchId, double limitMb)

// Get usage percentage
double getStorageUsagePercentage(Long churchId, double limitMb)

// Cleanup old records
void cleanupOldStorageRecords()  // Keeps last 90 days
```

### 4. Storage Usage Controller

**File**: `src/main/java/com/reuben/pastcare_spring/controllers/StorageUsageController.java`

REST API endpoints for storage usage.

#### Endpoints

**GET /api/storage-usage/current**
- Permission: `SUBSCRIPTION_VIEW` or `CHURCH_SETTINGS_VIEW`
- Returns: Latest storage usage for authenticated church
- Response: `StorageUsageResponse` with breakdown and percentages

**GET /api/storage-usage/history**
- Permission: `SUBSCRIPTION_VIEW` or `CHURCH_SETTINGS_VIEW`
- Parameters: `startDate`, `endDate` (ISO DateTime)
- Returns: List of storage usage records within date range
- Use case: Historical analysis, trend charts

**POST /api/storage-usage/calculate**
- Permission: `SUBSCRIPTION_MANAGE`
- Returns: Immediately calculates and returns current storage
- Use case: After bulk import/delete operations

### 5. Storage Usage Response DTO

**File**: `src/main/java/com/reuben/pastcare_spring/dtos/StorageUsageResponse.java`

Response structure:
```java
{
  "id": 123,
  "churchId": 5,
  "churchName": "First Baptist Church",
  "fileStorageMb": 150.5,
  "databaseStorageMb": 25.3,
  "totalStorageMb": 175.8,
  "storageLimitMb": 2048.0,
  "usagePercentage": 8.59,
  "isOverLimit": false,
  "fileStorageBreakdown": {
    "profilePhotos": 50.5,
    "eventImages": 95.2,
    "documents": 4.8,
    "attachments": 0.0
  },
  "databaseStorageBreakdown": {
    "members": 5.2,
    "donations": 8.5,
    "events": 6.1,
    "visits": 2.8,
    "households": 1.5,
    "attendance_sessions": 0.9,
    "campaigns": 0.3
  },
  "calculatedAt": "2025-12-29T02:00:00",
  "fileStorageDisplay": "150.50 MB",
  "databaseStorageDisplay": "25.30 MB",
  "totalStorageDisplay": "175.80 MB",
  "storageLimitDisplay": "2.00 GB"
}
```

### 6. Database Migration

**File**: `src/main/resources/db/migration/V55__create_storage_usage_table.sql`

Creates `storage_usage` table with:
- Auto-increment ID
- Foreign key to `church` table (CASCADE delete)
- Storage metrics in MB
- JSON breakdown fields
- Timestamps
- Index on `(church_id, calculated_at DESC)` for fast lookup

### 7. Repository Count Methods Added

Added `countByChurch()` or `countByChurchId()` methods to:
- ✅ `VisitRepository`
- ✅ `AttendanceSessionRepository`
- ✅ `CampaignRepository`
- ✅ `FellowshipRepository`
- ✅ `PrayerRequestRepository`
- ✅ `VisitorRepository`

*(Other repositories already had these methods)*

---

## How It Works

### Daily Automated Calculation

**Schedule**: Every day at 2:00 AM

**Process**:
1. Scheduled job `calculateStorageForAllChurches()` runs
2. For each church:
   - Calculate file storage by scanning upload directories
   - Calculate database storage by counting rows and estimating sizes
   - Save `StorageUsage` record with breakdown JSON
3. Log results (success/failure counts)
4. Clean up old storage records (keep last 90 days)

**Logging Output**:
```
INFO: Starting scheduled storage calculation for all churches
DEBUG: Storage calculated for church First Baptist Church (5)
INFO: Storage usage for church 5: Files=150.50 MB, DB=25.30 MB, Total=175.80 MB
INFO: Completed storage calculation: 25 succeeded, 0 failed
INFO: Cleaned up storage records older than 2025-09-30
```

### Manual Calculation

API endpoint allows manual trigger:
```bash
POST /api/storage-usage/calculate
Authorization: Bearer {jwt-token}
```

Use cases:
- After bulk member import
- After deleting many files
- Testing/verification

### Viewing Storage Usage

**Frontend Integration**:
```typescript
// Get current usage
GET /api/storage-usage/current
→ Shows current consumption, percentage, breakdown

// Get historical trend
GET /api/storage-usage/history?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
→ Shows usage over time for charts
```

**Dashboard Widget Example**:
```
Storage Usage
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
175.8 MB / 2.0 GB (8.59%)
[████░░░░░░░░░░░░░░░░░░░░] 8.59%

Breakdown:
  Files:    150.5 MB (85.6%)
    - Profile Photos: 50.5 MB
    - Event Images:   95.2 MB
    - Documents:       4.8 MB
  Database:  25.3 MB (14.4%)
    - Members:         5.2 MB
    - Donations:       8.5 MB
    - Events:          6.1 MB
    - Other:           5.5 MB
```

---

## Storage Pricing Model

### Base Plan: $9.99/month

**Includes**:
- 2 GB (2048 MB) storage
- File storage: ~1.5 GB
- Database storage: ~500 MB

**Typical Church (500 members)**:
- Files: ~200 MB (profile photos, event images)
- Database: ~50 MB (all data)
- **Total: ~250 MB (12% of limit)**

### Storage Add-Ons (Future)

| Tier | Storage | Monthly Cost | Use Case |
|------|---------|--------------|----------|
| Base | 2 GB | $9.99 (included) | Small church (< 500 members) |
| Tier 1 | +5 GB | +$4.99 | Medium church (500-2000 members) |
| Tier 2 | +10 GB | +$8.99 | Large church (2000-5000 members) |
| Tier 3 | +25 GB | +$19.99 | Mega church (5000+ members) |

### Cost Breakdown

**Infrastructure Cost**:
- VPS storage: $0.10/GB/month
- 2 GB base: $0.20/month
- Backup storage: $0.30/month
- **Total cost: $0.50/month**

**Revenue**:
- Subscription: $9.99/month
- **Profit: $9.49/month (95% margin)**

---

## API Examples

### Get Current Storage Usage

**Request**:
```http
GET /api/storage-usage/current
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response** (200 OK):
```json
{
  "id": 123,
  "churchId": 5,
  "churchName": "First Baptist Church",
  "fileStorageMb": 150.5,
  "databaseStorageMb": 25.3,
  "totalStorageMb": 175.8,
  "storageLimitMb": 2048.0,
  "usagePercentage": 8.59,
  "isOverLimit": false,
  "fileStorageBreakdown": {
    "profilePhotos": 50.5,
    "eventImages": 95.2,
    "documents": 4.8,
    "attachments": 0.0
  },
  "databaseStorageBreakdown": {
    "members": 5.2,
    "donations": 8.5,
    "events": 6.1,
    "visits": 2.8,
    "households": 1.5,
    "attendance_sessions": 0.9,
    "campaigns": 0.3
  },
  "calculatedAt": "2025-12-29T02:00:00",
  "fileStorageDisplay": "150.50 MB",
  "databaseStorageDisplay": "25.30 MB",
  "totalStorageDisplay": "175.80 MB",
  "storageLimitDisplay": "2.00 GB"
}
```

### Get Storage History

**Request**:
```http
GET /api/storage-usage/history?startDate=2025-12-01T00:00:00&endDate=2025-12-31T23:59:59
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response** (200 OK):
```json
[
  {
    "calculatedAt": "2025-12-29T02:00:00",
    "totalStorageMb": 175.8,
    ...
  },
  {
    "calculatedAt": "2025-12-28T02:00:00",
    "totalStorageMb": 172.3,
    ...
  },
  ...
]
```

Use this data to render a line chart showing storage growth over time.

### Manually Calculate Storage

**Request**:
```http
POST /api/storage-usage/calculate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response** (200 OK):
```json
{
  "id": 124,
  "totalStorageMb": 176.2,
  "calculatedAt": "2025-12-29T10:30:00",
  ...
}
```

---

## Permission Requirements

| Endpoint | Required Permission |
|----------|---------------------|
| GET /current | `SUBSCRIPTION_VIEW` OR `CHURCH_SETTINGS_VIEW` |
| GET /history | `SUBSCRIPTION_VIEW` OR `CHURCH_SETTINGS_VIEW` |
| POST /calculate | `SUBSCRIPTION_MANAGE` |

**Roles with Access**:
- **ADMIN**: All endpoints (has all permissions)
- **SUPERADMIN**: All endpoints (bypasses checks)
- **PASTOR, TREASURER, MEMBER_MANAGER**: View only (no manual calculate)
- **MEMBER, FELLOWSHIP_LEADER**: No access

---

## Testing

### Manual Test

1. **Run migration**:
   ```bash
   ./mvnw flyway:migrate
   ```

2. **Start application**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Calculate storage for a church**:
   ```bash
   curl -X POST http://localhost:8080/api/storage-usage/calculate \
     -H "Authorization: Bearer {admin-jwt-token}"
   ```

4. **View current usage**:
   ```bash
   curl -X GET http://localhost:8080/api/storage-usage/current \
     -H "Authorization: Bearer {admin-jwt-token}"
   ```

### Verify Scheduled Job

Wait until 2:00 AM or temporarily change cron to:
```java
@Scheduled(cron = "0 * * * * *")  // Every minute (for testing)
```

Check logs for:
```
INFO: Starting scheduled storage calculation for all churches
INFO: Completed storage calculation: X succeeded, 0 failed
```

---

## Files Created/Modified

### New Files

1. **`models/StorageUsage.java`** - Entity for storage tracking
2. **`repositories/StorageUsageRepository.java`** - JPA repository
3. **`services/StorageCalculationService.java`** - Core calculation logic (307 lines)
4. **`controllers/StorageUsageController.java`** - REST API endpoints
5. **`dtos/StorageUsageResponse.java`** - Response DTO
6. **`db/migration/V55__create_storage_usage_table.sql`** - Database schema

### Modified Files

7. **`repositories/VisitRepository.java`** - Added `countByChurch()`
8. **`repositories/AttendanceSessionRepository.java`** - Added `countByChurchId()`
9. **`repositories/CampaignRepository.java`** - Added `countByChurchId()`
10. **`repositories/FellowshipRepository.java`** - Added `countByChurch()`
11. **`repositories/PrayerRequestRepository.java`** - Added `countByChurchId()`
12. **`repositories/VisitorRepository.java`** - Added `countByChurchId()`

---

## Compilation Status

```bash
./mvnw compile -Dmaven.test.skip=true
```

**Result**: ✅ BUILD SUCCESS

All code compiles successfully with no errors.

---

## Next Steps

### Short-Term

1. **Run Flyway Migration**:
   ```bash
   ./mvnw flyway:migrate
   ```

2. **Test API Endpoints**:
   - Create some test data (members, donations, events)
   - Upload some files
   - Call `/api/storage-usage/calculate`
   - Verify breakdown is accurate

3. **Frontend Integration**:
   - Create storage usage widget for dashboard
   - Show usage percentage and breakdown
   - Add alert when approaching 80% limit

### Medium-Term

4. **Billing Integration**:
   - Check storage on payment processing
   - Block uploads if over limit
   - Send notification emails at 80%, 90%, 100%

5. **Storage Add-Ons**:
   - Create `SubscriptionPlan` entity with storage tiers
   - Allow upgrading to higher storage tiers
   - Adjust `storageLimitMb` based on plan

### Long-Term

6. **Storage Optimization**:
   - Image compression on upload
   - Auto-delete old files (e.g., > 2 years)
   - Archive old database records

7. **Advanced Analytics**:
   - Storage growth prediction
   - Recommend plan upgrades
   - Cost optimization suggestions

---

## Summary

✅ **Complete Storage Tracking System Implemented**

- File storage calculation (actual file sizes)
- Database storage estimation (row counts × avg sizes)
- Automated daily calculation at 2 AM
- REST API for viewing usage
- Storage breakdown by category
- Ready for billing integration

**Storage Calculation**:
- Fair estimation based on actual data
- Includes both files and database
- Accurate enough for billing purposes
- Transparent breakdown for users

**Next**: Run migration, test endpoints, integrate with frontend dashboard.
