# Platform Admin Dashboard - Phases 3-4 Implementation Plan

**Date**: 2025-12-30
**Status**: 🚧 IN PROGRESS
**Priority**: 🔴 HIGHEST

---

## 📋 Overview

Complete Phases 3-4 of the Platform Admin Dashboard to provide comprehensive storage management, billing oversight, system logging, and performance monitoring capabilities for SUPERADMIN users.

---

## 🎯 Phase 3: Storage & Billing Management

### 3.1 Storage Management Dashboard

**Backend Requirements** (Already Complete ✅):
- ✅ `GET /api/storage/current/{churchId}` - Current storage usage
- ✅ `GET /api/storage/history/{churchId}` - 90-day history
- ✅ `POST /api/storage/calculate/{churchId}` - Manual recalculation
- ✅ StorageUsage entity with breakdown by type

**Frontend Implementation**:
1. **Storage Overview Tab**
   - Platform-wide storage statistics card
   - Total storage used across all churches
   - Average storage per church
   - Storage growth trend (last 30 days)

2. **Top Consumers Widget**
   - Table of top 10 churches by storage usage
   - Sort by: Total usage, % of limit, growth rate
   - Visual progress bars
   - Quick actions: View details, Calculate now

3. **Storage Trends Chart**
   - Line chart showing platform storage growth
   - Aggregated across all churches
   - Date range selector (7d, 30d, 90d)
   - Breakdown by category (files vs database)

4. **Storage Breakdown**
   - Pie chart: Files vs Database
   - Bar chart: By category (photos, events, documents, etc.)
   - List view with details

**Interfaces Needed**:
```typescript
interface PlatformStorageStats {
  totalStorageUsedMB: number;
  totalStorageLimitMB: number;
  averageStoragePerChurch: number;
  topConsumers: StorageConsumer[];
  growthLastMonth: number;
  breakdown: StorageBreakdown;
}

interface StorageConsumer {
  churchId: number;
  churchName: string;
  storageUsedMB: number;
  storageLimitMB: number;
  percentUsed: number;
  lastCalculated: string;
}
```

---

### 3.2 Billing Overview Dashboard

**Backend Requirements**:
- ✅ `GET /api/billing/stats` - Platform-wide billing stats (SUPERADMIN)
- ✅ ChurchSubscription entity with status tracking
- ✅ Payment entity with revenue data

**Frontend Implementation**:
1. **Revenue Metrics Card**
   - Total monthly recurring revenue (MRR)
   - Total annual recurring revenue (ARR)
   - Month-over-month growth
   - Year-over-year growth

2. **Subscription Distribution**
   - Pie chart: Plans distribution (STANDARD, ENTERPRISE, etc.)
   - Count per plan
   - Percentage of total

3. **Payment Status Overview**
   - Active subscriptions count
   - Past due subscriptions count (warning badge)
   - Canceled subscriptions count
   - Suspended subscriptions count

4. **Recent Payments Table**
   - Last 20 payments across all churches
   - Columns: Date, Church, Amount, Plan, Status, Payment Method
   - Filter by status (successful, failed, pending)
   - Export to CSV

5. **Overdue Alerts**
   - List of churches with past_due status
   - Days overdue indicator
   - Quick action: View billing details
   - Warning level (yellow: 1-3 days, red: >3 days)

**Backend Endpoint Needed** (New):
```java
GET /api/billing/platform/stats
GET /api/billing/platform/revenue-trends?period=30d
GET /api/billing/platform/recent-payments?limit=20
GET /api/billing/platform/overdue-subscriptions
```

---

## 🎯 Phase 4: Troubleshooting Tools

### 4.1 System Logs Viewer

**Backend Requirements** (New):
```java
GET /api/platform/logs/stream?level=INFO&limit=1000&churchId={id}&search={term}
GET /api/platform/logs/download?startDate={date}&endDate={date}
```

**Frontend Implementation**:
1. **Log Viewer Component**
   - Real-time log streaming (WebSocket or polling)
   - Auto-scroll toggle
   - Pause/Resume streaming
   - Clear logs button

2. **Log Filtering**
   - Level filter dropdown (ALL, ERROR, WARN, INFO, DEBUG, TRACE)
   - Church filter (All churches or specific church)
   - Search by keyword
   - Date range picker

3. **Log Entry Display**
   - Color-coded by level (red: ERROR, yellow: WARN, etc.)
   - Timestamp with milliseconds
   - Logger name
   - Message with syntax highlighting
   - Stack trace expansion (for errors)

4. **Export Functionality**
   - Download current logs as .log file
   - Download filtered logs
   - Date range export

**Implementation Details**:
- Use SSE (Server-Sent Events) or Long Polling for real-time updates
- Virtual scrolling for performance (large log volumes)
- Limit to last 1000 lines in UI (server can store more)

---

### 4.2 Performance Metrics Dashboard

**Backend Requirements** (New):
```java
GET /api/platform/metrics/api-performance
GET /api/platform/metrics/database-stats
GET /api/platform/metrics/system-health
```

**Frontend Implementation**:
1. **API Performance Widget**
   - Average response time (last hour)
   - P50, P95, P99 percentiles
   - Slowest endpoints (top 10)
   - Request rate (requests/minute)
   - Error rate

2. **Database Performance**
   - Connection pool status (active, idle, max)
   - Slow query count (queries >1s)
   - Top slow queries with execution time
   - Database size growth

3. **System Health Indicators**
   - JVM memory usage (heap, non-heap)
   - GC pause time
   - Thread count (active, peak)
   - CPU usage
   - Disk I/O

4. **Uptime & Availability**
   - Application uptime
   - Last restart timestamp
   - Crash count (last 24h)
   - Health check status

**Charts**:
- Line chart: API response times (last 24h)
- Bar chart: Top slow endpoints
- Gauge: Memory usage
- Gauge: CPU usage

---

## 🏗️ Implementation Steps

### Week 1: Phase 3 - Storage & Billing

**Day 1-2: Storage Management**
- [ ] Create platform-storage-page component
- [ ] Add storage tab to platform admin
- [ ] Implement storage stats service method
- [ ] Create storage trends chart (Chart.js/ApexCharts)
- [ ] Create top consumers table
- [ ] Add storage breakdown visualization

**Day 3-4: Billing Overview**
- [ ] Create platform-billing-page component
- [ ] Add billing tab to platform admin
- [ ] Backend: Create BillingStatsController endpoints
- [ ] Frontend: Revenue metrics cards
- [ ] Frontend: Subscription distribution pie chart
- [ ] Frontend: Payment status overview
- [ ] Frontend: Recent payments table
- [ ] Frontend: Overdue alerts widget

**Day 5: Testing & Polish**
- [ ] E2E test storage dashboard
- [ ] E2E test billing dashboard
- [ ] Fix bugs and UI polish
- [ ] Add loading states and error handling

---

### Week 2: Phase 4 - Troubleshooting Tools

**Day 1-2: System Logs Viewer**
- [ ] Backend: Create LogStreamingController
- [ ] Backend: Implement log file reading service
- [ ] Backend: Add log filtering logic
- [ ] Frontend: Create system-logs-page component
- [ ] Frontend: Implement SSE connection for real-time logs
- [ ] Frontend: Add log filtering UI
- [ ] Frontend: Add export functionality

**Day 3-4: Performance Metrics**
- [ ] Backend: Create MetricsController using Micrometer/Actuator
- [ ] Backend: Expose API performance metrics
- [ ] Backend: Expose database metrics
- [ ] Backend: Expose JVM/system metrics
- [ ] Frontend: Create performance-metrics-page component
- [ ] Frontend: API performance chart
- [ ] Frontend: Database stats display
- [ ] Frontend: System health gauges

**Day 5: Integration & Testing**
- [ ] Integrate all tabs into platform admin dashboard
- [ ] Add navigation between tabs
- [ ] E2E testing of all features
- [ ] Load testing with large datasets
- [ ] Performance optimization
- [ ] Documentation update

---

## 🧪 Testing Strategy

### E2E Test Scenarios

**Storage Dashboard**:
1. Load storage overview → Verify stats displayed
2. View top consumers → Verify sorted correctly
3. Click "Calculate Now" for church → Verify recalculation
4. View storage trends chart → Verify data accuracy
5. Filter by date range → Verify filtered data

**Billing Dashboard**:
1. Load billing overview → Verify revenue metrics
2. View subscription distribution → Verify chart data
3. Filter payments by status → Verify filtered results
4. View overdue subscriptions → Verify alerts displayed
5. Export payments to CSV → Verify file download

**System Logs**:
1. Load logs viewer → Verify initial logs displayed
2. Filter by ERROR level → Verify only errors shown
3. Search for keyword → Verify search results
4. Pause/Resume streaming → Verify stream control
5. Export logs → Verify file download

**Performance Metrics**:
1. Load performance dashboard → Verify all metrics displayed
2. View API response times chart → Verify trend line
3. View slow queries → Verify queries listed
4. Check system health → Verify gauges accurate
5. Refresh metrics → Verify live updates

---

## 📊 Success Metrics

**Completion Criteria**:
- ✅ All 4 new tabs functional (Storage, Billing, Logs, Performance)
- ✅ Real-time data updates working
- ✅ All charts rendering correctly
- ✅ Export functionality working
- ✅ E2E tests passing (100% coverage of features)
- ✅ No TypeScript compilation errors
- ✅ Responsive design (mobile, tablet, desktop)
- ✅ Documentation updated

**Performance Targets**:
- Page load time < 2 seconds
- Log streaming < 100ms latency
- Chart rendering < 500ms
- Export file generation < 3 seconds

---

## 🚀 Deployment Checklist

- [ ] Backend endpoints tested with Postman
- [ ] Frontend components tested in dev environment
- [ ] E2E tests passing
- [ ] Code reviewed
- [ ] Documentation updated (CONSOLIDATED_PENDING_TASKS.md)
- [ ] Release notes created
- [ ] Backend build successful
- [ ] Frontend build successful
- [ ] Manual testing completed

---

## 📚 Related Files

**Backend**:
- `StorageUsageController.java` (existing)
- `BillingController.java` (existing)
- `BillingStatsController.java` (new)
- `LogStreamingController.java` (new)
- `MetricsController.java` (new)
- `PlatformStatsController.java` (existing)

**Frontend**:
- `platform-admin-page.ts/html` (modify - add tabs)
- `platform-storage-page.ts/html` (new)
- `platform-billing-page.ts/html` (new)
- `system-logs-page.ts/html` (new)
- `performance-metrics-page.ts/html` (new)
- `platform.service.ts` (extend)
- `platform.model.ts` (extend)

---

**Plan Status**: ✅ Complete and Ready for Implementation
**Next Action**: Begin Day 1-2 implementation (Storage Management)
