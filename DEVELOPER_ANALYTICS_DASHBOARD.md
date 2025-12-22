# PastCare - Developer Analytics Dashboard

**Purpose**: Backend monitoring system for tracking user sign-ons, behavior analytics, system health, and usage patterns to understand how churches use PastCare and optimize the platform.

**Last Updated**: 2025-12-20

---

## Overview

The Developer Analytics Dashboard is a separate admin interface for platform developers/administrators (not church admins) to monitor:
- Multi-tenant usage across all churches
- User authentication patterns and security events
- Feature adoption and usage metrics
- System performance and health
- Error tracking and debugging
- Business intelligence for product decisions

---

## Module Architecture

### 1. Authentication & Access Control

#### Admin User Types
- **PLATFORM_ADMIN** - Full analytics access
- **PLATFORM_DEVELOPER** - Read-only analytics, error logs
- **PLATFORM_SUPPORT** - User support data, no system metrics

#### Separate Authentication System
- [ ] Separate admin portal (subdomain: `analytics.pastcare.app`)
- [ ] OAuth 2.0 integration (Google Workspace for team)
- [ ] IP whitelist for added security
- [ ] MFA required for all platform admin accounts
- [ ] Session timeout: 30 minutes of inactivity
- [ ] Audit log of all admin access

---

## 2. User Sign-On Analytics

### 2.1 Authentication Events Tracking

#### Event Types to Track
```java
@Entity
@Table(name = "auth_events")
public class AuthEvent extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private AuthEventType eventType;
    // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_REFRESH,
    // PASSWORD_RESET, PASSWORD_CHANGE, ACCOUNT_LOCKED,
    // TWO_FACTOR_ENABLED, TWO_FACTOR_DISABLED

    @ManyToOne
    private User user; // Can be null for failed attempts

    private String email; // Email attempted (for failed logins)

    @ManyToOne
    private Church church;

    private String ipAddress;
    private String userAgent;
    private String device; // Parsed from user agent
    private String browser; // Parsed from user agent
    private String os; // Parsed from user agent
    private String country; // GeoIP lookup
    private String city; // GeoIP lookup

    private LocalDateTime timestamp;
    private Boolean successful;
    private String failureReason; // Invalid credentials, account locked, etc.

    @Column(columnDefinition = "JSON")
    private String metadata; // Additional context as JSON
}
```

#### Implementation Tasks

**Backend** ⏳
- [ ] Create `AuthEvent` entity and repository
- [ ] Create `AuthEventService` to log all auth events
- [ ] Integrate with existing `AuthService`:
  - [ ] Log event on every login attempt (success/failure)
  - [ ] Log event on logout
  - [ ] Log event on token refresh
  - [ ] Log event on password reset/change
  - [ ] Log event on 2FA enable/disable
  - [ ] Log event on account lock/unlock
- [ ] Add GeoIP lookup library (MaxMind GeoLite2)
  ```xml
  <dependency>
    <groupId>com.maxmind.geoip2</groupId>
    <artifactId>geoip2</artifactId>
    <version>4.1.0</version>
  </dependency>
  ```
- [ ] Create IP geolocation service
- [ ] Create user agent parser service (device, browser, OS)
- [ ] Add database indexes for performance:
  - [ ] Index on timestamp (for time-based queries)
  - [ ] Index on user_id (for user-specific queries)
  - [ ] Index on church_id (for church-specific queries)
  - [ ] Index on eventType (for event filtering)
  - [ ] Composite index on (timestamp, eventType)
- [ ] Create data retention policy (purge events older than 2 years)
- [ ] Add privacy compliance (anonymize IP after 90 days)

**API Endpoints** ⏳
- [ ] `GET /api/analytics/auth-events` - List auth events (paginated, filtered)
  - Query params: startDate, endDate, eventType, churchId, userId, successful
- [ ] `GET /api/analytics/auth-events/stats` - Authentication statistics
  - Total logins, success rate, failure rate
  - Logins by day/week/month
  - Peak login times
  - Unique users logged in
- [ ] `GET /api/analytics/auth-events/geo` - Geographic distribution
  - Logins by country
  - Logins by city
  - Map visualization data
- [ ] `GET /api/analytics/auth-events/devices` - Device analytics
  - Logins by device type (mobile, desktop, tablet)
  - Logins by browser
  - Logins by OS
- [ ] `GET /api/analytics/auth-events/security` - Security events
  - Failed login attempts
  - Account lockouts
  - Brute force attacks detected
  - Suspicious IPs

**Frontend Dashboard** ⏳
- [ ] Authentication overview page
  - [ ] Total logins today/week/month (cards)
  - [ ] Success rate percentage
  - [ ] Failed login attempts (red alert card)
  - [ ] Active sessions (current logged-in users)
- [ ] Login trends chart
  - [ ] Line chart: logins over time (hourly/daily/weekly)
  - [ ] Compare to previous period
  - [ ] Peak login times heatmap
- [ ] Geographic distribution
  - [ ] World map with login counts by country
  - [ ] Top 10 countries table
  - [ ] Top 10 cities table
- [ ] Device analytics
  - [ ] Pie chart: mobile vs desktop vs tablet
  - [ ] Bar chart: browsers (Chrome, Firefox, Safari, etc.)
  - [ ] Bar chart: operating systems
- [ ] Security monitoring
  - [ ] Table: recent failed logins
  - [ ] Table: locked accounts
  - [ ] Alert: suspicious IP addresses
  - [ ] Alert: brute force attacks detected
- [ ] User session list
  - [ ] Table: currently active sessions
  - [ ] Columns: user, church, IP, device, login time
  - [ ] Action: force logout (admin only)

**Edge Cases & Considerations**:
- VPN users (IP location inaccurate)
- Shared IP addresses (multiple users from same office)
- Bot/crawler traffic (filter out)
- Session hijacking detection
- Cookie theft detection
- Privacy regulations (GDPR, CCPA)
- Data retention and purging
- High-volume logging (millions of events)
- Real-time vs batch processing
- Database performance with large event tables

---

### 2.2 User Behavior Analytics

#### User Activity Tracking

```java
@Entity
@Table(name = "user_activities")
public class UserActivity extends BaseEntity {
    @ManyToOne
    private User user;

    @ManyToOne
    private Church church;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    // MEMBER_CREATED, MEMBER_UPDATED, MEMBER_DELETED,
    // ATTENDANCE_MARKED, DONATION_RECORDED, EVENT_CREATED,
    // REPORT_GENERATED, COMMUNICATION_SENT, etc.

    private String entityType; // Member, Attendance, Donation, etc.
    private Long entityId; // ID of the entity acted upon

    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;

    @Column(columnDefinition = "JSON")
    private String metadata; // Action-specific data
}
```

#### Implementation Tasks

**Backend** ⏳
- [ ] Create `UserActivity` entity and repository
- [ ] Create `ActivityTrackingService`
- [ ] Add activity logging to all services:
  - [ ] MemberService: log create, update, delete, bulk operations
  - [ ] AttendanceService: log session creation, attendance marking
  - [ ] GivingService: log donation recording, receipts generated
  - [ ] EventService: log event creation, registrations
  - [ ] CommunicationService: log messages sent
  - [ ] ReportService: log reports generated
- [ ] Use AspectJ for automatic activity logging (AOP)
  ```java
  @Aspect
  @Component
  public class ActivityLoggingAspect {
      @AfterReturning("@annotation(TrackActivity)")
      public void logActivity(JoinPoint joinPoint) {
          // Extract method info and log activity
      }
  }
  ```
- [ ] Add `@TrackActivity` annotation to service methods
- [ ] Implement async activity logging (queue-based to avoid performance impact)
- [ ] Add database indexes
- [ ] Data retention policy (purge after 1 year, aggregate for historical analysis)

**API Endpoints** ⏳
- [ ] `GET /api/analytics/user-activities` - List activities (paginated, filtered)
- [ ] `GET /api/analytics/user-activities/stats` - Activity statistics
  - Total activities by type
  - Activities per user
  - Activities per church
  - Peak activity times
- [ ] `GET /api/analytics/user-activities/timeline/{userId}` - User activity timeline
- [ ] `GET /api/analytics/user-activities/heatmap` - Activity heatmap
  - Day of week vs hour of day
  - Identify peak usage times

**Frontend Dashboard** ⏳
- [ ] User activity overview
  - [ ] Total activities today/week/month
  - [ ] Most active users leaderboard
  - [ ] Most active churches leaderboard
  - [ ] Activity by type (pie chart)
- [ ] Activity timeline
  - [ ] Real-time activity feed (WebSocket)
  - [ ] Filter by user, church, activity type
  - [ ] Search activities
- [ ] Activity heatmap
  - [ ] 7x24 grid (day of week x hour)
  - [ ] Color intensity = activity volume
  - [ ] Identify optimal support hours
- [ ] User engagement metrics
  - [ ] Daily Active Users (DAU)
  - [ ] Weekly Active Users (WAU)
  - [ ] Monthly Active Users (MAU)
  - [ ] DAU/MAU ratio (engagement health)
- [ ] Feature usage analytics
  - [ ] Members module: X uses
  - [ ] Attendance module: Y uses
  - [ ] Giving module: Z uses
  - [ ] Identify underutilized features

---

## 3. System Health & Performance Monitoring

### 3.1 Application Performance Metrics

#### Metrics to Track

```java
@Entity
@Table(name = "performance_metrics")
public class PerformanceMetric extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private MetricType metricType;
    // API_RESPONSE_TIME, DATABASE_QUERY_TIME, MEMORY_USAGE,
    // CPU_USAGE, REQUEST_COUNT, ERROR_COUNT, etc.

    private String endpoint; // API endpoint (for API metrics)
    private Double value; // Metric value
    private String unit; // ms, MB, %, count, etc.

    private LocalDateTime timestamp;

    @Column(columnDefinition = "JSON")
    private String metadata; // Additional context
}
```

#### Implementation Tasks

**Backend** ⏳
- [ ] Integrate Spring Boot Actuator
  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  ```
- [ ] Integrate Micrometer for metrics collection
  ```xml
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
  ```
- [ ] Configure metrics endpoints
  - [ ] `/actuator/health` - Health check
  - [ ] `/actuator/metrics` - Application metrics
  - [ ] `/actuator/prometheus` - Prometheus metrics
- [ ] Add custom metrics:
  - [ ] API endpoint response times (all endpoints)
  - [ ] Database query execution times
  - [ ] Cache hit/miss rates
  - [ ] Active database connections
  - [ ] Queue depths (if using message queues)
  - [ ] File storage usage
  - [ ] Email/SMS delivery rates
- [ ] Create performance monitoring service
  - [ ] Collect metrics every minute
  - [ ] Store aggregated metrics (1-hour, 1-day averages)
  - [ ] Alert on thresholds (response time > 2s, error rate > 5%)
- [ ] Add request interceptor for automatic timing
  ```java
  @Component
  public class PerformanceInterceptor implements HandlerInterceptor {
      @Override
      public boolean preHandle(HttpServletRequest request, ...) {
          request.setAttribute("startTime", System.currentTimeMillis());
          return true;
      }

      @Override
      public void afterCompletion(HttpServletRequest request, ...) {
          long duration = System.currentTimeMillis() -
                         (Long) request.getAttribute("startTime");
          // Log performance metric
      }
  }
  ```

**API Endpoints** ⏳
- [ ] `GET /api/analytics/performance/overview` - Performance overview
  - Average response time (all endpoints)
  - P50, P95, P99 response times
  - Error rate
  - Request rate
- [ ] `GET /api/analytics/performance/endpoints` - Per-endpoint metrics
  - Sorted by slowest average response time
  - Error rate per endpoint
  - Request count per endpoint
- [ ] `GET /api/analytics/performance/database` - Database performance
  - Query execution times
  - Slow queries (>1s)
  - Connection pool usage
  - Deadlocks detected
- [ ] `GET /api/analytics/performance/system` - System resources
  - CPU usage (%)
  - Memory usage (MB, %)
  - Disk usage (GB, %)
  - Network I/O (MB/s)

**Frontend Dashboard** ⏳
- [ ] Performance overview
  - [ ] Average response time (gauge)
  - [ ] Error rate (gauge with color coding)
  - [ ] Requests per minute (gauge)
  - [ ] System uptime (percentage)
- [ ] Response time chart
  - [ ] Line chart: average response time over time
  - [ ] P95 and P99 lines
  - [ ] Highlight spikes
- [ ] Slowest endpoints table
  - [ ] Endpoint path
  - [ ] Average response time
  - [ ] P95 response time
  - [ ] Request count
  - [ ] Error rate
  - [ ] Action: optimize (links to code/query)
- [ ] System resources dashboard
  - [ ] CPU usage gauge
  - [ ] Memory usage gauge
  - [ ] Disk usage gauge
  - [ ] Network I/O chart
- [ ] Database performance
  - [ ] Slow queries table (queries >1s)
  - [ ] Connection pool chart
  - [ ] Query distribution (read vs write)
- [ ] Alerts configuration
  - [ ] Set threshold: response time > X ms
  - [ ] Set threshold: error rate > Y %
  - [ ] Set threshold: CPU usage > Z %
  - [ ] Notification channels (email, Slack, SMS)

---

### 3.2 Error Tracking & Debugging

#### Error Logging

```java
@Entity
@Table(name = "error_logs")
public class ErrorLog extends BaseEntity {
    private String errorType; // Exception class name
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    private String endpoint; // API endpoint where error occurred
    private String httpMethod; // GET, POST, etc.
    private Integer httpStatus; // 400, 500, etc.

    @ManyToOne
    private User user; // User who triggered error (if authenticated)

    @ManyToOne
    private Church church;

    private String ipAddress;
    private String userAgent;

    private LocalDateTime timestamp;

    @Column(columnDefinition = "JSON")
    private String requestBody; // For debugging (sanitize sensitive data)

    @Column(columnDefinition = "JSON")
    private String requestParams;

    private Boolean resolved; // Has issue been fixed?
    private String resolution; // Fix description
    private LocalDateTime resolvedAt;

    private Integer occurrenceCount; // Deduplicate similar errors
}
```

#### Implementation Tasks

**Backend** ⏳
- [ ] Integrate Sentry for error tracking
  ```xml
  <dependency>
    <groupId>io.sentry</groupId>
    <artifactId>sentry-spring-boot-starter</artifactId>
    <version>7.0.0</version>
  </dependency>
  ```
- [ ] Configure Sentry DSN in application.properties
- [ ] Create `@ControllerAdvice` for global exception handling
  ```java
  @ControllerAdvice
  public class GlobalExceptionHandler {
      @ExceptionHandler(Exception.class)
      public ResponseEntity<ErrorResponse> handleException(Exception ex) {
          // Log to ErrorLog entity
          // Send to Sentry
          // Return user-friendly error response
      }
  }
  ```
- [ ] Create `ErrorLoggingService`
  - [ ] Log all exceptions to database
  - [ ] Deduplicate similar errors (group by error type + message)
  - [ ] Sanitize sensitive data (passwords, tokens)
  - [ ] Capture request context
- [ ] Add error severity levels
  - [ ] CRITICAL: data loss, security breach
  - [ ] HIGH: feature completely broken
  - [ ] MEDIUM: feature partially broken
  - [ ] LOW: minor UI glitch
- [ ] Add error grouping/fingerprinting
  - [ ] Group similar errors together
  - [ ] Show occurrence count
  - [ ] First seen / Last seen timestamps

**API Endpoints** ⏳
- [ ] `GET /api/analytics/errors` - List errors (paginated, filtered)
  - Filter by severity, date range, endpoint, resolved status
- [ ] `GET /api/analytics/errors/stats` - Error statistics
  - Total errors today/week/month
  - Error rate (errors per request)
  - Errors by type
  - Errors by endpoint
  - Errors by church (identify problematic tenants)
- [ ] `GET /api/analytics/errors/{id}` - Error details
  - Full stack trace
  - Request context
  - Occurrence timeline
  - Similar errors
- [ ] `PATCH /api/analytics/errors/{id}/resolve` - Mark error as resolved
  - Add resolution description
- [ ] `POST /api/analytics/errors/{id}/reproduce` - Attempt to reproduce error
  - Re-execute request with same parameters

**Frontend Dashboard** ⏳
- [ ] Error overview
  - [ ] Total errors today/week/month (cards)
  - [ ] Error rate percentage (gauge)
  - [ ] Unresolved errors count (red alert)
  - [ ] Critical errors count (red alert)
- [ ] Error list
  - [ ] Table with columns:
    - [ ] Error type
    - [ ] Message (truncated)
    - [ ] Endpoint
    - [ ] Occurrences
    - [ ] First seen
    - [ ] Last seen
    - [ ] Severity
    - [ ] Resolved status
  - [ ] Sort by occurrence count (most frequent first)
  - [ ] Filter by severity, endpoint, date range
  - [ ] Click to view details
- [ ] Error details page
  - [ ] Error type and message
  - [ ] Full stack trace (formatted, syntax highlighted)
  - [ ] Request details (method, endpoint, params, body)
  - [ ] User context (user, church, IP, device)
  - [ ] Occurrence timeline (chart)
  - [ ] Similar errors (links)
  - [ ] Actions:
    - [ ] Mark as resolved (with description)
    - [ ] Assign to developer
    - [ ] Create Jira/GitHub issue
    - [ ] Ignore (for known non-critical errors)
- [ ] Error trends chart
  - [ ] Line chart: errors over time
  - [ ] Grouped by severity
  - [ ] Identify error spikes
- [ ] Error distribution
  - [ ] Pie chart: errors by endpoint
  - [ ] Pie chart: errors by type
  - [ ] Bar chart: errors by church (identify problematic tenants)

---

## 4. Feature Adoption & Usage Analytics

### 4.1 Module Usage Tracking

#### Metrics to Track
- Feature usage by module (Members, Attendance, Giving, etc.)
- Feature usage over time (trends)
- Feature abandonment (started but not completed)
- Feature discoverability (how users find features)
- User journey mapping (feature to feature navigation)

#### Implementation

**Backend** ⏳
- [ ] Create `ModuleUsage` entity
  ```java
  @Entity
  public class ModuleUsage extends BaseEntity {
      @ManyToOne
      private Church church;

      @Enumerated(EnumType.STRING)
      private Module module; // MEMBERS, ATTENDANCE, GIVING, etc.

      private LocalDate usageDate;
      private Integer actionCount; // Number of actions in this module on this date
      private Integer uniqueUsers; // Number of unique users who used this module
  }
  ```
- [ ] Create aggregation service to count daily module usage
- [ ] Track specific features within modules:
  - Members: quick add, bulk import, advanced search, etc.
  - Attendance: QR check-in, manual mark, bulk mark, etc.
  - Giving: manual entry, online donation, pledge creation, etc.
- [ ] Calculate adoption rates:
  - Churches using feature / Total churches
  - Users using feature / Total users in church

**API Endpoints** ⏳
- [ ] `GET /api/analytics/feature-adoption` - Feature adoption overview
  - Adoption rate per module
  - Adoption rate per feature
  - Growth trends
- [ ] `GET /api/analytics/feature-adoption/{module}` - Module-specific adoption
  - Churches using module
  - Feature usage within module
  - User engagement metrics
- [ ] `GET /api/analytics/feature-adoption/comparison` - Compare modules
  - Most used modules
  - Least used modules
  - Identify modules needing improvement

**Frontend Dashboard** ⏳
- [ ] Feature adoption overview
  - [ ] Module usage chart (bar chart)
  - [ ] Adoption rate by module (%)
  - [ ] Growth trends (line chart)
- [ ] Module deep dive
  - [ ] Select module dropdown
  - [ ] Churches using module (count, %)
  - [ ] Features within module (usage count)
  - [ ] User engagement (active users)
  - [ ] Recommendations for improvement
- [ ] Feature comparison
  - [ ] Side-by-side comparison table
  - [ ] Identify underutilized features
  - [ ] Prioritize feature development based on demand

---

### 4.2 Church Segmentation & Insights

#### Church Metrics

```java
@Entity
@Table(name = "church_metrics")
public class ChurchMetric extends BaseEntity {
    @ManyToOne
    private Church church;

    private LocalDate metricDate;

    // Member metrics
    private Integer totalMembers;
    private Integer newMembersThisMonth;
    private Integer activeMembersPercent;

    // Attendance metrics
    private Integer averageAttendance;
    private Integer attendanceRate;

    // Giving metrics
    private Double totalGiving;
    private Integer donorCount;
    private Double averageDonation;

    // Engagement metrics
    private Integer activeUsers; // Church staff
    private Integer modulesUsed;
    private Integer activitiesPerUser;

    // Feature usage
    private Boolean usingMemberPortal;
    private Boolean usingOnlineGiving;
    private Boolean usingQRCheckIn;
    private Boolean usingBulkOperations;
}
```

#### Implementation

**Backend** ⏳
- [ ] Create daily aggregation job to calculate church metrics
- [ ] Run every night at 2 AM (cron job)
- [ ] Calculate all metrics per church
- [ ] Store in church_metrics table
- [ ] Create church segmentation algorithm:
  - Small church: <100 members
  - Medium church: 100-500 members
  - Large church: 500-2000 members
  - Mega church: 2000+ members
- [ ] Calculate church health score (0-100):
  - Factors: engagement, feature adoption, growth, attendance rate
- [ ] Identify at-risk churches:
  - Low engagement (no logins in 30 days)
  - Declining members
  - Low feature adoption

**API Endpoints** ⏳
- [ ] `GET /api/analytics/churches` - List all churches with metrics
- [ ] `GET /api/analytics/churches/{id}/metrics` - Church-specific metrics
- [ ] `GET /api/analytics/churches/segmentation` - Church segmentation
  - Group by size, location, health score
- [ ] `GET /api/analytics/churches/at-risk` - At-risk churches
  - Low engagement, declining, need support

**Frontend Dashboard** ⏳
- [ ] Church overview
  - [ ] Total churches
  - [ ] Active churches (used in last 30 days)
  - [ ] New churches this month
  - [ ] Church health distribution (chart)
- [ ] Church segmentation
  - [ ] Pie chart: churches by size
  - [ ] Map: churches by location
  - [ ] Table: top performing churches
  - [ ] Table: at-risk churches (needing intervention)
- [ ] Church details page
  - [ ] Church profile
  - [ ] Metrics timeline (charts)
  - [ ] Feature adoption
  - [ ] User activity
  - [ ] Support history
  - [ ] Actions: contact, offer training, send resources

---

## 5. Business Intelligence & Reporting

### 5.1 Executive Dashboard

**Key Metrics** ⏳
- [ ] Total churches onboarded
- [ ] Monthly Recurring Revenue (MRR) - if subscription model
- [ ] Churn rate (churches leaving)
- [ ] Net Promoter Score (NPS)
- [ ] Total members across all churches
- [ ] Total transactions (attendance marks, donations, etc.)
- [ ] Platform uptime %
- [ ] Customer support tickets (resolved, pending)

**Frontend** ⏳
- [ ] Executive overview page
  - [ ] Key metrics cards (large numbers)
  - [ ] Growth trends (charts)
  - [ ] Comparisons to previous periods
  - [ ] Goal progress (e.g., reach 1000 churches)

---

### 5.2 Automated Reports

**Report Types** ⏳
- [ ] Weekly platform health report (email to team)
- [ ] Monthly growth report (new churches, MRR, churn)
- [ ] Quarterly business review (comprehensive)
- [ ] Daily error summary (critical errors only)
- [ ] Weekly feature adoption report

**Implementation** ⏳
- [ ] Create scheduled jobs (Spring @Scheduled)
- [ ] Generate reports (PDF or HTML)
- [ ] Email to stakeholders
- [ ] Store reports for historical access

---

## 6. Alerts & Notifications

### 6.1 Real-Time Alerts

**Alert Types** ⏳
- [ ] Critical error spike (>10 errors in 5 minutes)
- [ ] System down (health check failed)
- [ ] Performance degradation (response time >5s)
- [ ] Security threat (brute force attack detected)
- [ ] High traffic (requests >1000/min)
- [ ] Church churn (church deleted account)
- [ ] Payment failure (if subscription model)

**Notification Channels** ⏳
- [ ] Email (for non-urgent alerts)
- [ ] Slack (for urgent alerts)
- [ ] SMS (for critical alerts)
- [ ] Dashboard notifications (in-app)

**Implementation** ⏳
- [ ] Create alert rules engine
- [ ] Monitor metrics in real-time
- [ ] Trigger alerts based on thresholds
- [ ] Send to appropriate channels
- [ ] Escalation (if not acknowledged in 30 min)

---

## 7. Data Privacy & Compliance

### 7.1 Privacy Considerations

**Data Anonymization** ⏳
- [ ] Anonymize IP addresses after 90 days
- [ ] Don't log sensitive data (passwords, credit cards)
- [ ] Sanitize error logs (remove PII)
- [ ] Aggregate metrics (no individual user tracking without consent)

**Access Controls** ⏳
- [ ] Role-based access to analytics
- [ ] Audit log of all analytics dashboard access
- [ ] Time-limited access (sessions expire)
- [ ] IP whitelist for added security

**Compliance** ⏳
- [ ] GDPR compliance (EU churches)
  - [ ] Data deletion on request
  - [ ] Data export on request
  - [ ] Consent for analytics tracking
- [ ] CCPA compliance (California churches)
- [ ] SOC 2 Type II (for enterprise customers)

---

## 8. Technology Stack

**Backend**:
- Spring Boot Actuator (health checks, metrics)
- Micrometer + Prometheus (metrics collection)
- Sentry (error tracking)
- Scheduled jobs (metric aggregation)
- GeoIP2 (location lookup)

**Frontend**:
- Separate Angular app (analytics.pastcare.app)
- Chart.js or D3.js (data visualization)
- Real-time updates (WebSocket or Server-Sent Events)
- Responsive dashboard (desktop-focused)

**Database**:
- Separate analytics database (read replicas for performance)
- TimescaleDB extension for time-series data (metrics over time)
- Partitioning for large tables (auth_events, user_activities)

**Infrastructure**:
- Hosted separately from main app (security isolation)
- CDN for dashboard assets
- Caching layer (Redis) for frequently accessed metrics

---

## 9. Implementation Roadmap

### Phase 1: Foundation (2 weeks) ⏳
- [ ] Set up separate analytics database
- [ ] Create base entities (AuthEvent, UserActivity, ErrorLog)
- [ ] Implement basic logging in existing services
- [ ] Set up Sentry integration
- [ ] Create analytics API endpoints structure

### Phase 2: Authentication Analytics (1 week) ⏳
- [ ] Complete auth event tracking
- [ ] GeoIP integration
- [ ] User agent parsing
- [ ] Authentication dashboard frontend
- [ ] Security monitoring alerts

### Phase 3: Performance Monitoring (1 week) ⏳
- [ ] Spring Boot Actuator setup
- [ ] Prometheus metrics collection
- [ ] Performance dashboard frontend
- [ ] Alert system for performance issues

### Phase 4: User Behavior Analytics (2 weeks) ⏳
- [ ] User activity tracking (AOP)
- [ ] Feature usage tracking
- [ ] Activity timeline dashboard
- [ ] Heatmap visualization

### Phase 5: Business Intelligence (2 weeks) ⏳
- [ ] Church metrics aggregation
- [ ] Church segmentation
- [ ] Executive dashboard
- [ ] Automated reports

### Phase 6: Alerts & Optimization (1 week) ⏳
- [ ] Real-time alerts
- [ ] Notification channels (Slack, email, SMS)
- [ ] Performance optimization
- [ ] Data retention policies

**Total Estimate**: 9 weeks

---

## 10. Success Metrics for Analytics Dashboard

- [ ] 100% of auth events captured
- [ ] <100ms overhead for activity logging
- [ ] Real-time dashboard (updates every 10 seconds)
- [ ] 99% uptime for analytics platform
- [ ] Actionable insights generated weekly
- [ ] 50% reduction in time to identify issues
- [ ] Product decisions informed by data (not guesses)

---

## Security & Access

**Authentication**:
- OAuth 2.0 with Google Workspace
- MFA required
- IP whitelist

**Authorization**:
- PLATFORM_ADMIN: Full access
- PLATFORM_DEVELOPER: Read-only
- PLATFORM_SUPPORT: Limited access

**Audit Trail**:
- Log all analytics dashboard access
- Log all data exports
- Alerts on suspicious access patterns

---

This developer analytics dashboard will provide deep insights into how PastCare is being used, help identify issues proactively, and inform product development decisions based on real user behavior data.
