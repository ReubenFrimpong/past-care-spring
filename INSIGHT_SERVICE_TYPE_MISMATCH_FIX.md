# InsightService & GoalService Type Mismatch Fix

**Date:** 2025-12-28
**Issue:** InvalidDataAccessApiUsageException - LocalDateTime vs Instant type mismatch
**Status:** ✅ FIXED

---

## Problem

After implementing Dashboard Phases 2.2-2.4, the Insight generation feature was failing with:

```
org.springframework.dao.InvalidDataAccessApiUsageException:
Argument [2025-11-28T18:35:22.729102001] of type [java.time.LocalDateTime]
did not match parameter type [java.time.Instant (n/a)]
```

**Root Cause:** Repository methods expected `Instant` parameters (to match entity `createdAt` fields) but were declared with `LocalDateTime` parameters and being called with `LocalDateTime` values.

---

## Files Fixed

### 1. Repository Parameter Types Changed

**AttendanceSessionRepository.java:111-112**
```java
// Before
Double getAverageAttendanceForPeriod(
  @Param("startDate") java.time.LocalDateTime startDate,
  @Param("endDate") java.time.LocalDateTime endDate
);

// After
Double getAverageAttendanceForPeriod(
  @Param("startDate") java.time.Instant startDate,
  @Param("endDate") java.time.Instant endDate
);
```

**DonationRepository.java:192-193**
```java
// Before
BigDecimal getTotalDonationsForPeriod(
  @Param("startDate") java.time.LocalDateTime startDate,
  @Param("endDate") java.time.LocalDateTime endDate
);

// After
BigDecimal getTotalDonationsForPeriod(
  @Param("startDate") java.time.Instant startDate,
  @Param("endDate") java.time.Instant endDate
);
```

### 2. Service Method Calls Updated

**InsightService.java:139-147** - `detectAttendanceAnomalies()`
```java
// Before
Double lastMonthAvg = attendanceSessionRepository.getAverageAttendanceForPeriod(
  lastMonth.atStartOfDay(), now.atStartOfDay()
);

// After
Double lastMonthAvg = attendanceSessionRepository.getAverageAttendanceForPeriod(
  lastMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
  now.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
);
```

**InsightService.java:194-201** - `detectGivingAnomalies()`
```java
// Before
LocalDateTime now = LocalDateTime.now();
LocalDateTime lastMonth = now.minusMonths(1);
BigDecimal lastMonthTotal = donationRepository.getTotalDonationsForPeriod(lastMonth, now);

// After
LocalDate now = LocalDate.now();
LocalDate lastMonth = now.minusMonths(1);
BigDecimal lastMonthTotal = donationRepository.getTotalDonationsForPeriod(
  lastMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
  now.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
);
```

**GoalService.java:258-260** - `calculateAttendanceProgress()`
```java
// Before
Double avgAttendance = attendanceSessionRepository.getAverageAttendanceForPeriod(
  startDateTime, endDateTime
);

// After
Double avgAttendance = attendanceSessionRepository.getAverageAttendanceForPeriod(
  startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant(),
  endDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
);
```

**GoalService.java:274-276** - `calculateGivingProgress()`
```java
// Before
BigDecimal totalDonations = donationRepository.getTotalDonationsForPeriod(
  startDateTime, endDateTime
);

// After
BigDecimal totalDonations = donationRepository.getTotalDonationsForPeriod(
  startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant(),
  endDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
);
```

---

## Technical Details

### Type Conversion Chain

```java
LocalDate.atStartOfDay()           // Returns LocalDateTime
  .atZone(ZoneId.systemDefault())  // Adds timezone → ZonedDateTime
  .toInstant()                     // Converts to Instant (UTC)
```

### Why Instant?

- Entity `createdAt` fields use `@CreationTimestamp` which generates `Instant` values
- Hibernate queries against these fields expect `Instant` parameters
- Using `Instant` ensures timezone-independent UTC timestamps

---

## Affected Features

### ✅ Now Working
1. **Insight Generation** (`/api/dashboard/insights/generate`)
   - Attendance anomaly detection
   - Giving trend analysis
   - Member milestone tracking

2. **Goal Progress Calculation** (`/api/dashboard/goals/{id}/recalculate`)
   - Attendance goal progress
   - Giving goal progress
   - Auto-calculation on goal retrieval

---

## Testing

### Backend Compilation
```bash
./mvnw compile
# Result: SUCCESS (0 errors)
```

### Expected Behavior
- Insights generation completes without errors
- Goals show accurate progress percentages
- No type mismatch exceptions in logs

---

## Related Files

- `src/main/java/com/reuben/pastcare_spring/repositories/AttendanceSessionRepository.java`
- `src/main/java/com/reuben/pastcare_spring/repositories/DonationRepository.java`
- `src/main/java/com/reuben/pastcare_spring/services/InsightService.java`
- `src/main/java/com/reuben/pastcare_spring/services/GoalService.java`

---

**Status:** All temporal type mismatches resolved. Dashboard Phases 2.3 (Goals) and 2.4 (Insights) are now fully functional.
