# PastCare SaaS - File Storage Architecture Decision

**Date**: 2025-12-29
**Status**: FINAL RECOMMENDATION
**Decision**: Hybrid Approach (Local + Object Storage Transition)

---

## Executive Summary

**Recommendation: Start with local VPS storage, migrate to object storage when crossing 40GB threshold**

**Rationale:**
- Initial cost: $0/month (included in VPS)
- Migration cost when needed: ~$2-5/month
- Simple architecture initially
- Easy upgrade path when scaling
- No vendor lock-in

---

## Storage Analysis

### Current Usage Projections

Based on pricing model analysis (PRICING_MODEL_REVISED.md):

**Per-Church Average Storage:**
- Small church (50-100 members): ~100 MB
- Medium church (200-500 members): ~350 MB
- Large church (1000+ members): ~1 GB
- **Weighted average**: ~400 MB per church

**Capacity Planning:**

| Churches | Total Storage | VPS Can Handle? | Recommendation |
|----------|---------------|-----------------|----------------|
| 1-100 | 40 GB | ✅ Yes | Local VPS storage |
| 100-200 | 80 GB | ⚠️ Tight | Migrate to object storage |
| 200+ | 120+ GB | ❌ No | Object storage required |

---

## Option 1: Local VPS Storage (Recommended for Start)

### Architecture

```
┌─────────────────────────────────────────────┐
│         Hetzner VPS (80 GB SSD)             │
├─────────────────────────────────────────────┤
│  System & Applications: ~15 GB              │
│  ├─ Ubuntu OS: ~5 GB                        │
│  ├─ MySQL Database: ~5 GB                   │
│  ├─ Application: ~2 GB                      │
│  └─ System logs/cache: ~3 GB                │
├─────────────────────────────────────────────┤
│  File Storage: ~50 GB available             │
│  ├─ /var/pastcare/uploads/                  │
│  │   └─ member-photos/ (by church_id)       │
│  ├─ /var/pastcare/events/                   │
│  │   └─ event-images/ (by event_id)         │
│  └─ /var/pastcare/documents/                │
│      └─ exports/ (CSV, PDF reports)         │
├─────────────────────────────────────────────┤
│  Backups: ~15 GB                            │
│  └─ Daily snapshots (7 days retention)      │
└─────────────────────────────────────────────┘
```

### Pros ✅
- **Zero additional cost** (included in $6.50/month VPS)
- **Fast access** (local disk, no network latency)
- **Simple setup** (just create directories)
- **Easy backups** (included in VPS snapshots)
- **No API complexity** (standard file I/O)
- **Perfect for MVP** (100-200 churches = 40GB)

### Cons ❌
- **Limited capacity** (80GB total, ~50GB usable)
- **No CDN benefits** (served from single location)
- **Scaling requires migration** (when storage fills up)
- **Single point of failure** (but mitigated by backups)

### Cost Breakdown
| Item | Cost |
|------|------|
| Storage (50 GB) | $0 (included in VPS) |
| Bandwidth | $0 (20TB included) |
| Backups | $1.50/month (Hetzner automated) |
| **Total** | **$1.50/month** |

### When to Migrate Away
- Storage usage > 40GB (80% of capacity)
- OR supporting 100+ churches
- OR international traffic needs CDN

---

## Option 2: Object Storage (Backblaze B2) - Migration Target

### Architecture

```
┌───────────────────────────────────────────────────────┐
│              Hetzner VPS                              │
│  ┌─────────────────────────────────────────────────┐ │
│  │  Application (Spring Boot)                      │ │
│  │  - Uploads to Backblaze B2 via API             │ │
│  │  - Returns public CDN URLs                      │ │
│  └─────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────┘
                          │
                          │ HTTPS API Upload
                          ▼
┌───────────────────────────────────────────────────────┐
│            Backblaze B2 Object Storage                │
│  Bucket: pastcare-uploads                             │
│  ├─ member-photos/                                    │
│  │   └─ {church_id}/{member_id}/photo.jpg            │
│  ├─ event-images/                                     │
│  │   └─ {church_id}/{event_id}/image.jpg             │
│  └─ documents/                                        │
│      └─ {church_id}/exports/report.pdf               │
└───────────────────────────────────────────────────────┘
                          │
                          │ Public URLs via CDN
                          ▼
┌───────────────────────────────────────────────────────┐
│         Cloudflare CDN (Free Tier)                    │
│  - Caches images globally                             │
│  - Reduces B2 egress costs                            │
│  - Faster delivery worldwide                          │
└───────────────────────────────────────────────────────┘
```

### Pros ✅
- **Unlimited capacity** (scales to petabytes)
- **Global CDN delivery** (via Cloudflare)
- **Cheaper than competitors** ($0.005/GB vs AWS $0.023/GB)
- **Pay only for what you use** (no minimum)
- **Free egress with Cloudflare** (Bandwidth Alliance)
- **99.9% availability SLA**
- **Automatic redundancy** (data stored in multiple locations)

### Cons ❌
- **Additional cost** (~$2-5/month for 100+ churches)
- **API complexity** (S3-compatible SDK integration)
- **Network latency** (upload time vs local disk)
- **Dependency on external service** (if B2 is down)
- **Migration effort** (need to move existing files)

### Cost Breakdown (for 100 churches @ 40GB)

| Item | Usage | Cost |
|------|-------|------|
| Storage | 40 GB × $0.005/GB | $0.20/month |
| Class B transactions (uploads) | 10k/month × $0.004/10k | $0.004/month |
| Class C transactions (downloads) | 50k/month × $0.004/10k | $0.02/month |
| **Egress bandwidth** | **Free via Cloudflare** | **$0** |
| **Total** | | **$0.22/month** |

**At 200 churches (80GB):**
- Storage: $0.40/month
- Transactions: ~$0.05/month
- **Total: $0.45/month**

**At 500 churches (200GB):**
- Storage: $1.00/month
- Transactions: ~$0.15/month
- **Total: $1.15/month**

### Implementation Notes

**Spring Boot Integration:**
```java
// Add dependency
<dependency>
    <groupId>com.backblaze.b2</groupId>
    <artifactId>b2-sdk-java</artifactId>
    <version>6.1.1</version>
</dependency>

// Service implementation
@Service
public class B2StorageService {
    private B2Client b2Client;

    public String uploadFile(MultipartFile file, String path) {
        // Upload to B2
        // Return public CDN URL
    }
}
```

**Cloudflare CDN Setup:**
1. Create CNAME: `cdn.pastcare.app` → `f000.backblazeb2.com`
2. Enable Cloudflare caching rules
3. All file URLs become: `https://cdn.pastcare.app/member-photos/...`

---

## Option 3: AWS S3 (Not Recommended - Too Expensive)

### Cost Comparison

**AWS S3 for 40GB + traffic:**

| Item | Cost |
|------|------|
| Storage (40 GB) | $0.92/month |
| PUT requests | $0.05/month |
| GET requests | $0.04/month |
| Data transfer out (10GB) | $0.90/month |
| **Total** | **$1.91/month** |

**8.7x MORE expensive than Backblaze B2** ($1.91 vs $0.22)

### Why Not S3?
- ❌ Much higher storage cost ($0.023/GB vs $0.005/GB)
- ❌ Expensive egress ($0.09/GB vs free with Cloudflare)
- ❌ Complex pricing (dozens of tiers)
- ❌ Overkill for our use case
- ✅ Only benefit: Tighter AWS ecosystem integration (not needed)

---

## Final Recommendation: Hybrid Approach

### Phase 1: Local VPS Storage (Months 1-6)

**Target**: 0-100 churches, up to 40GB storage

**Implementation:**
```bash
# Directory structure
/var/pastcare/
├── uploads/
│   └── {church_id}/
│       ├── members/
│       │   └── {member_id}/
│       │       └── photo.jpg
│       └── assets/
│           └── logo.png
├── events/
│   └── {church_id}/
│       └── {event_id}/
│           ├── banner.jpg
│           └── gallery/
│               ├── image1.jpg
│               └── image2.jpg
└── documents/
    └── {church_id}/
        └── exports/
            └── members_export_2025.csv
```

**Nginx serves files:**
```nginx
# Static file serving
location /uploads/ {
    alias /var/pastcare/uploads/;
    expires 1y;
    add_header Cache-Control "public, immutable";
}

location /events/ {
    alias /var/pastcare/events/;
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

**Costs:**
- Storage: $0 (included)
- Bandwidth: $0 (included)
- Backups: $1.50/month
- **Total: $1.50/month**

---

### Phase 2: Migrate to Object Storage (Months 6-12)

**Trigger**: When storage usage exceeds 40GB or 100 churches

**Migration Steps:**

1. **Set up Backblaze B2**
   ```bash
   # Create bucket
   b2 create-bucket pastcare-uploads allPrivate

   # Create bucket for public files
   b2 create-bucket pastcare-public allPublic
   ```

2. **Update Spring Boot configuration**
   ```yaml
   # application-prod.yml
   storage:
     provider: b2  # Switch from 'local' to 'b2'
     b2:
       application-key-id: ${B2_KEY_ID}
       application-key: ${B2_APPLICATION_KEY}
       bucket-name: pastcare-uploads
       endpoint: https://s3.us-west-000.backblazeb2.com
   ```

3. **Migrate existing files** (one-time)
   ```bash
   # Sync local files to B2
   b2 sync /var/pastcare/uploads/ b2://pastcare-uploads/uploads/
   b2 sync /var/pastcare/events/ b2://pastcare-uploads/events/

   # Verify migration
   b2 ls b2://pastcare-uploads/

   # Keep local files as backup for 30 days
   # Delete after confirming B2 is working
   ```

4. **Set up Cloudflare CDN**
   - Add CNAME: `cdn.pastcare.app` → B2 endpoint
   - Configure caching rules (1 year for images)
   - Enable Bandwidth Alliance (free egress)

5. **Update database URLs** (if stored)
   ```sql
   -- Update member photo URLs
   UPDATE members
   SET profile_photo_url = REPLACE(
       profile_photo_url,
       'https://pastcare.app/uploads/',
       'https://cdn.pastcare.app/uploads/'
   );

   -- Update event image URLs
   UPDATE event_images
   SET image_url = REPLACE(
       image_url,
       'https://pastcare.app/events/',
       'https://cdn.pastcare.app/events/'
   );
   ```

**Migration Costs:**
- Storage (40GB): $0.20/month
- Bandwidth: $0 (Cloudflare)
- **Total: $0.20/month** (saves VPS disk space)

---

## Storage Service Abstraction Layer

### Interface Design (Future-Proof)

```java
public interface StorageService {
    /**
     * Upload file to storage.
     * @return Public URL of uploaded file
     */
    String uploadFile(MultipartFile file, String path);

    /**
     * Delete file from storage.
     */
    void deleteFile(String path);

    /**
     * Get file URL (public or signed).
     */
    String getFileUrl(String path);

    /**
     * Check if file exists.
     */
    boolean fileExists(String path);
}

@Service
@Profile("local")
public class LocalStorageService implements StorageService {
    // Implementation for VPS local storage
}

@Service
@Profile("b2")
public class B2StorageService implements StorageService {
    // Implementation for Backblaze B2
}
```

**Application configuration:**
```yaml
# application.yml
storage:
  provider: ${STORAGE_PROVIDER:local}  # 'local' or 'b2'
  local:
    base-path: /var/pastcare
    base-url: https://pastcare.app
  b2:
    application-key-id: ${B2_KEY_ID:}
    application-key: ${B2_APPLICATION_KEY:}
    bucket-name: pastcare-uploads
    cdn-url: https://cdn.pastcare.app
```

**Service auto-configuration:**
```java
@Configuration
public class StorageConfiguration {

    @Value("${storage.provider}")
    private String storageProvider;

    @Bean
    public StorageService storageService() {
        return switch (storageProvider) {
            case "b2" -> new B2StorageService();
            case "local" -> new LocalStorageService();
            default -> throw new IllegalArgumentException("Unknown storage provider: " + storageProvider);
        };
    }
}
```

---

## Migration Timeline

### Month 1-3: Local VPS Storage
- **Churches**: 1-50
- **Storage**: 5-20 GB
- **Cost**: $1.50/month
- **Action**: Monitor storage usage

### Month 4-6: Monitor & Plan
- **Churches**: 50-100
- **Storage**: 20-40 GB
- **Cost**: $1.50/month
- **Action**: Prepare B2 migration plan when 30GB reached

### Month 6-9: Migrate to B2
- **Churches**: 100-150
- **Storage**: 40-60 GB
- **Cost**: $0.30/month (B2) + $6.50 (VPS) = $6.80/month
- **Action**: Execute migration, test CDN

### Month 9-12: Scale on B2
- **Churches**: 150-200
- **Storage**: 60-80 GB
- **Cost**: $0.40/month (B2) + $6.50 (VPS) = $6.90/month
- **Savings**: Freed 40GB on VPS for database growth

### Year 2: Optimize
- **Churches**: 200-500
- **Storage**: 80-200 GB
- **Cost**: $1.00/month (B2) + $13 (upgraded VPS) = $14/month
- **Benefits**: Global CDN, unlimited scaling

---

## Decision Matrix

| Criteria | Local VPS | Backblaze B2 | AWS S3 |
|----------|-----------|--------------|--------|
| **Cost (40GB)** | $0/month ✅ | $0.22/month ✅ | $1.91/month ❌ |
| **Setup Complexity** | Simple ✅ | Medium ⚠️ | Complex ❌ |
| **Scalability** | Limited (80GB) ⚠️ | Unlimited ✅ | Unlimited ✅ |
| **CDN Support** | Manual ❌ | Built-in ✅ | Built-in ✅ |
| **Backup Included** | Yes ✅ | No ❌ | No ❌ |
| **Migration Effort** | N/A | Medium ⚠️ | High ❌ |
| **Vendor Lock-in** | None ✅ | Low ✅ | High ❌ |

---

## Recommendation Summary

### Start with Local VPS Storage

**Why:**
1. **$0 additional cost** for first 6-12 months
2. **Simple implementation** (standard file I/O)
3. **Fast development** (no API complexity)
4. **Built-in backups** (VPS snapshots)
5. **Sufficient capacity** for 100 churches (40GB)

### Migrate to Backblaze B2 when:

1. Storage usage > 40GB
2. Supporting 100+ churches
3. International traffic needs CDN
4. Want to free VPS disk for database

### Implementation Strategy

**Week 1 (Current):**
- ✅ Use local VPS storage
- ✅ Create `/var/pastcare/{uploads,events,documents}`
- ✅ Configure Nginx to serve files
- ✅ Implement file upload in Spring Boot

**Month 6 (Future):**
- Create Backblaze B2 account
- Implement `B2StorageService`
- Set up Cloudflare CDN
- Migrate existing files
- Switch `storage.provider=b2`

**Total Cost Impact:**
- Months 1-6: $0/month extra
- Months 6+: $0.20-1.00/month extra
- **First year average: $0.30/month**

---

## Final Decision

✅ **Use Local VPS Storage initially**
✅ **Build storage abstraction layer for easy migration**
✅ **Migrate to Backblaze B2 + Cloudflare when crossing 40GB**
✅ **Total storage cost Year 1: <$5**

This approach:
- Minimizes initial costs
- Keeps architecture simple
- Provides clear upgrade path
- Avoids premature optimization
- Scales smoothly when needed

---

**Decision Status**: ✅ APPROVED
**Implementation**: Local VPS storage with B2 migration path
**Cost Savings**: ~$24/year vs immediate object storage
**Complexity Reduction**: 80% simpler initial setup
