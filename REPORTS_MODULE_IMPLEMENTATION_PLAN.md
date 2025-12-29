# Reports Module - Complete Implementation Plan

**Module**: Reports Module (Module 9)
**Status**: Not Started (0%)
**Timeline**: 4-5 weeks (3 phases)
**Priority**: ⭐⭐
**Date Created**: 2025-12-28

---

## Overview

The Reports Module will provide comprehensive reporting capabilities across all church data, including pre-built reports, custom report builder, and advanced export/visualization features.

### Key Features
- 10+ pre-built reports covering all modules
- Custom report builder with drag-and-drop interface
- Export to PDF, Excel, CSV
- Scheduled report generation
- Charts and visualizations
- Email distribution
- Report templates and versioning

### Dependencies
- ✅ Members Module (100% complete)
- ✅ Attendance Module (100% complete)
- ✅ Fellowship Module (100% complete)
- ✅ Dashboard Module (100% complete)
- ✅ Pastoral Care Module (100% complete)
- ⚠️ Giving Module (75% complete - sufficient for reporting)
- ✅ Events Module (85% complete - sufficient for reporting)
- ✅ Communications Module (100% complete)

---

## Phase 1: Pre-built Reports (2 weeks) ⭐⭐⭐

**Objective**: Implement 10 essential pre-built reports with filtering, sorting, and basic export.

### 1.1 Backend - Report Infrastructure (Days 1-2)

#### Step 1: Create Report Enums
**File**: `src/main/java/com/reuben/pastcare_spring/enums/ReportType.java`

```java
public enum ReportType {
    // Member Reports
    MEMBER_DIRECTORY("Member Directory", "Complete member listing with contact info", "MEMBERS"),
    BIRTHDAY_ANNIVERSARY_LIST("Birthday & Anniversary List", "Upcoming birthdays and anniversaries", "MEMBERS"),
    INACTIVE_MEMBERS("Inactive Members Report", "Members who haven't attended recently", "MEMBERS"),
    HOUSEHOLD_ROSTER("Household Roster", "Family groupings and household information", "MEMBERS"),

    // Attendance Reports
    ATTENDANCE_SUMMARY("Attendance Summary", "Attendance statistics by date range", "ATTENDANCE"),
    FIRST_TIME_VISITORS("First-Time Visitors", "New visitor tracking and follow-up", "ATTENDANCE"),

    // Giving Reports
    GIVING_SUMMARY("Giving Summary", "Donation statistics by date range", "GIVING"),
    TOP_DONORS("Top Donors Report", "Highest contributing members", "GIVING"),
    CAMPAIGN_PROGRESS("Campaign Progress Report", "Fundraising campaign statistics", "GIVING"),

    // Fellowship Reports
    FELLOWSHIP_ROSTER("Fellowship Roster", "Fellowship membership and leaders", "FELLOWSHIPS"),

    // Pastoral Care Reports
    PASTORAL_CARE_SUMMARY("Pastoral Care Summary", "Care needs, visits, and prayer requests", "PASTORAL_CARE"),

    // Events Reports
    EVENT_ATTENDANCE("Event Attendance Report", "Event participation statistics", "EVENTS"),

    // Growth Reports
    GROWTH_TREND("Growth Trend Report", "Membership and attendance trends", "ANALYTICS");

    private final String displayName;
    private final String description;
    private final String category;

    ReportType(String displayName, String description, String category) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    // Getters
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/enums/ReportFormat.java`

```java
public enum ReportFormat {
    PDF("application/pdf", ".pdf"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    CSV("text/csv", ".csv");

    private final String mimeType;
    private final String extension;

    ReportFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    // Getters
}
```

#### Step 2: Create Report Entities
**File**: `src/main/java/com/reuben/pastcare_spring/models/Report.java`

```java
@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Column(nullable = false)
    private Boolean isCustom = false;

    @Column(columnDefinition = "TEXT")
    private String filters; // JSON string of filter configuration

    @Column(columnDefinition = "TEXT")
    private String fields; // JSON string of selected fields

    @Column(columnDefinition = "TEXT")
    private String sorting; // JSON string of sort configuration

    @Column(columnDefinition = "TEXT")
    private String grouping; // JSON string of grouping configuration

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    private Boolean isTemplate = false;

    private Boolean isShared = false;

    @ElementCollection
    @CollectionTable(name = "report_shared_users", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "user_id")
    private List<Long> sharedWithUserIds = new ArrayList<>();
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/models/ReportSchedule.java`

```java
@Entity
@Table(name = "report_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleFrequency frequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY

    private Integer dayOfWeek; // 1-7 for weekly

    private Integer dayOfMonth; // 1-31 for monthly

    @Column(nullable = false)
    private LocalTime executionTime;

    @ElementCollection
    @CollectionTable(name = "report_schedule_recipients", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "email")
    private List<String> recipientEmails = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportFormat format;

    private LocalDateTime nextExecutionDate;

    private LocalDateTime lastExecutionDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/models/ReportExecution.java`

```java
@Entity
@Table(name = "report_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ReportSchedule schedule; // Null if manually executed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    private User executedBy; // Null if scheduled

    @Column(nullable = false)
    private LocalDateTime executionDate;

    @Column(columnDefinition = "TEXT")
    private String parameters; // JSON string of execution parameters

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportFormat format;

    private String outputFileUrl; // S3 URL or file path

    private String outputFileName;

    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status; // PENDING, RUNNING, COMPLETED, FAILED

    @Column(length = 2000)
    private String errorMessage;

    private Integer rowCount;

    private Long executionTimeMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;
}
```

#### Step 3: Create Repositories
**File**: `src/main/java/com/reuben/pastcare_spring/repositories/ReportRepository.java`

```java
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByChurchIdAndIsCustom(Long churchId, Boolean isCustom);
    List<Report> findByChurchIdAndIsTemplate(Long churchId, Boolean isTemplate);
    List<Report> findByChurchIdAndCreatedById(Long churchId, Long userId);
    List<Report> findByChurchIdAndIsSharedTrue(Long churchId);
    Optional<Report> findByIdAndChurchId(Long id, Long churchId);
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/repositories/ReportScheduleRepository.java`

```java
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findByChurchIdAndIsActiveTrue(Long churchId);
    List<ReportSchedule> findByNextExecutionDateBeforeAndIsActiveTrue(LocalDateTime dateTime);
    Optional<ReportSchedule> findByIdAndChurchId(Long id, Long churchId);
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/repositories/ReportExecutionRepository.java`

```java
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {
    List<ReportExecution> findByReportIdOrderByExecutionDateDesc(Long reportId);
    List<ReportExecution> findByChurchIdOrderByExecutionDateDesc(Long churchId, Pageable pageable);
    List<ReportExecution> findByExecutedByIdOrderByExecutionDateDesc(Long userId);
    Optional<ReportExecution> findByIdAndChurchId(Long id, Long churchId);
}
```

#### Step 4: Create DTOs
**File**: `src/main/java/com/reuben/pastcare_spring/dtos/ReportRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private String name;
    private String description;
    private ReportType reportType;
    private String filters; // JSON
    private String fields; // JSON
    private String sorting; // JSON
    private String grouping; // JSON
    private Boolean isTemplate;
    private Boolean isShared;
    private List<Long> sharedWithUserIds;
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/dtos/GenerateReportRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportRequest {
    private Long reportId;
    private ReportType reportType;
    private ReportFormat format;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> filters; // Dynamic filters
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/dtos/ReportResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String name;
    private String description;
    private ReportType reportType;
    private Boolean isCustom;
    private Boolean isTemplate;
    private Boolean isShared;
    private String createdByName;
    private LocalDateTime createdAt;
    private Integer executionCount;
    private LocalDateTime lastExecutedAt;
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/dtos/ReportExecutionResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecutionResponse {
    private Long id;
    private String reportName;
    private LocalDateTime executionDate;
    private String executedByName;
    private ReportFormat format;
    private String outputFileUrl;
    private String outputFileName;
    private Long fileSizeBytes;
    private ExecutionStatus status;
    private String errorMessage;
    private Integer rowCount;
    private Long executionTimeMs;
}
```

#### Step 5: Create Database Migrations
**File**: `src/main/resources/db/migration/V52__create_reports_table.sql`

```sql
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    report_type VARCHAR(100) NOT NULL,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE,
    filters TEXT,
    fields TEXT,
    sorting TEXT,
    grouping TEXT,
    created_by BIGINT,
    church_id BIGINT NOT NULL,
    is_template BOOLEAN DEFAULT FALSE,
    is_shared BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    INDEX idx_church_type (church_id, report_type),
    INDEX idx_church_custom (church_id, is_custom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE report_shared_users (
    report_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    INDEX idx_report_user (report_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**File**: `src/main/resources/db/migration/V53__create_report_schedules_table.sql`

```sql
CREATE TABLE report_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    day_of_week INT,
    day_of_month INT,
    execution_time TIME NOT NULL,
    format VARCHAR(50) NOT NULL,
    next_execution_date TIMESTAMP,
    last_execution_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    INDEX idx_church_active (church_id, is_active),
    INDEX idx_next_execution (next_execution_date, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE report_schedule_recipients (
    schedule_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES report_schedules(id) ON DELETE CASCADE,
    INDEX idx_schedule_email (schedule_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**File**: `src/main/resources/db/migration/V54__create_report_executions_table.sql`

```sql
CREATE TABLE report_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    schedule_id BIGINT,
    executed_by BIGINT,
    execution_date TIMESTAMP NOT NULL,
    parameters TEXT,
    format VARCHAR(50) NOT NULL,
    output_file_url VARCHAR(500),
    output_file_name VARCHAR(255),
    file_size_bytes BIGINT,
    status VARCHAR(50) NOT NULL,
    error_message VARCHAR(2000),
    row_count INT,
    execution_time_ms BIGINT,
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES report_schedules(id) ON DELETE SET NULL,
    FOREIGN KEY (executed_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    INDEX idx_church_date (church_id, execution_date),
    INDEX idx_report_date (report_id, execution_date),
    INDEX idx_user_date (executed_by, execution_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 1.2 Backend - Report Generation Services (Days 3-6)

#### Step 6: Create Report Generator Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/ReportGeneratorService.java`

This service will handle the actual report generation logic for each pre-built report type.

**Key Methods**:
```java
- byte[] generateMemberDirectory(GenerateReportRequest request, Long churchId)
- byte[] generateBirthdayAnniversaryList(GenerateReportRequest request, Long churchId)
- byte[] generateInactiveMembersReport(GenerateReportRequest request, Long churchId)
- byte[] generateAttendanceSummary(GenerateReportRequest request, Long churchId)
- byte[] generateGivingSummary(GenerateReportRequest request, Long churchId)
- byte[] generateTopDonors(GenerateReportRequest request, Long churchId)
- byte[] generateFellowshipRoster(GenerateReportRequest request, Long churchId)
- byte[] generatePastoralCareSummary(GenerateReportRequest request, Long churchId)
- byte[] generateEventAttendance(GenerateReportRequest request, Long churchId)
- byte[] generateGrowthTrend(GenerateReportRequest request, Long churchId)
```

**Implementation Pattern** (using existing AttendanceExportService as reference):
1. Query data based on filters
2. Format data into report structure
3. Generate output in requested format (Excel/CSV/PDF)
4. Return byte array

#### Step 7: Create Excel Export Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/ExcelReportService.java`

Reusable service for generating Excel reports using Apache POI.

**Key Methods**:
```java
- byte[] generateExcelReport(List<String> headers, List<List<Object>> rows, String sheetName)
- CellStyle createHeaderStyle(Workbook workbook)
- CellStyle createDateStyle(Workbook workbook)
- CellStyle createCurrencyStyle(Workbook workbook)
- void autoSizeColumns(Sheet sheet, int columnCount)
```

#### Step 8: Create PDF Export Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/PdfReportService.java`

Service for generating PDF reports using iText or similar library.

**Key Methods**:
```java
- byte[] generatePdfReport(String title, List<String> headers, List<List<Object>> rows)
- byte[] generatePdfReportWithCharts(ReportData data)
- void addHeader(Document document, String churchName, String reportTitle)
- void addFooter(Document document, int pageNumber)
```

#### Step 9: Create CSV Export Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/CsvReportService.java`

Simple CSV generation service.

**Key Methods**:
```java
- byte[] generateCsvReport(List<String> headers, List<List<Object>> rows)
```

### 1.3 Backend - Report Management Service (Days 7-8)

#### Step 10: Create Report Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/ReportService.java`

Main service for managing report definitions, executions, and orchestration.

**Key Methods**:
```java
// Report CRUD
- ReportResponse createReport(ReportRequest request, Long userId, Long churchId)
- ReportResponse updateReport(Long id, ReportRequest request, Long churchId)
- void deleteReport(Long id, Long churchId)
- ReportResponse getReportById(Long id, Long churchId)
- List<ReportResponse> getAllReports(Long churchId)
- List<ReportResponse> getPrebuiltReports()
- List<ReportResponse> getCustomReports(Long userId, Long churchId)
- List<ReportResponse> getSharedReports(Long churchId)

// Report Execution
- ReportExecutionResponse generateReport(GenerateReportRequest request, Long userId, Long churchId)
- byte[] downloadReport(Long executionId, Long churchId)
- List<ReportExecutionResponse> getReportHistory(Long reportId, Long churchId)
- List<ReportExecutionResponse> getUserReportHistory(Long userId, Long churchId)

// Report Sharing
- void shareReport(Long reportId, List<Long> userIds, Long churchId)
- void unshareReport(Long reportId, Long churchId)

// Report Templates
- ReportResponse saveAsTemplate(Long reportId, Long churchId)
- List<ReportResponse> getReportTemplates(Long churchId)
```

**Implementation Pattern**:
1. Validate request and permissions
2. Call appropriate ReportGeneratorService method
3. Create ReportExecution record
4. Store output file
5. Return execution response

### 1.4 Backend - Controllers (Day 9)

#### Step 11: Create Report Controller
**File**: `src/main/java/com/reuben/pastcare_spring/controllers/ReportController.java`

REST API endpoints for report management.

**Endpoints**:
```java
// Pre-built Reports
GET    /api/reports/pre-built                  - List all pre-built report types
POST   /api/reports/generate                   - Generate a report
GET    /api/reports/{id}/download              - Download generated report

// Custom Reports
POST   /api/reports                            - Create custom report
GET    /api/reports                            - List all reports
GET    /api/reports/{id}                       - Get report details
PUT    /api/reports/{id}                       - Update report
DELETE /api/reports/{id}                       - Delete report

// Report Execution History
GET    /api/reports/{id}/executions            - Get report execution history
GET    /api/reports/executions/my              - Get user's report history

// Report Templates
POST   /api/reports/{id}/save-template         - Save report as template
GET    /api/reports/templates                  - List report templates

// Report Sharing
POST   /api/reports/{id}/share                 - Share report with users
DELETE /api/reports/{id}/share                 - Unshare report
```

### 1.5 Frontend - Reports Page (Days 10-14)

#### Step 12: Create Reports Service
**File**: `src/app/services/report.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiUrl = `${environment.apiUrl}/reports`;

  getPrebuiltReports(): Observable<ReportType[]>
  getAllReports(): Observable<Report[]>
  getReportById(id: number): Observable<Report>
  createReport(request: ReportRequest): Observable<Report>
  updateReport(id: number, request: ReportRequest): Observable<Report>
  deleteReport(id: number): Observable<void>

  generateReport(request: GenerateReportRequest): Observable<Blob>
  downloadReport(executionId: number): Observable<Blob>
  getReportHistory(reportId: number): Observable<ReportExecution[]>

  getReportTemplates(): Observable<Report[]>
  saveAsTemplate(reportId: number): Observable<Report>

  shareReport(reportId: number, userIds: number[]): Observable<void>
}
```

#### Step 13: Create Reports Page Component
**File**: `src/app/reports-page/reports-page.ts`

**Structure**:
```typescript
export class ReportsPage {
  // Signals
  reportTypes = signal<ReportType[]>([]);
  myReports = signal<Report[]>([]);
  sharedReports = signal<Report[]>([]);
  reportHistory = signal<ReportExecution[]>([]);
  loading = signal(false);

  // Filter signals
  selectedCategory = signal<string>('all');
  searchTerm = signal('');

  // Computed
  filteredReportTypes = computed(() => { /* filter logic */ });

  // Methods
  ngOnInit(): void
  loadReportTypes(): void
  loadMyReports(): void
  loadSharedReports(): void
  loadReportHistory(): void

  generateReport(reportType: ReportType): void
  openGenerateDialog(reportType: ReportType): void
  downloadReport(execution: ReportExecution): void
  deleteReport(report: Report): void
  shareReport(report: Report): void
}
```

#### Step 14: Create Reports Page HTML
**File**: `src/app/reports-page/reports-page.html`

**Sections**:
1. **Header**: Title, stats, search, filters
2. **Pre-built Reports Grid**: Cards for each report type
3. **My Reports Section**: User's custom/saved reports
4. **Shared Reports Section**: Reports shared by others
5. **Recent Executions**: Report generation history

**Key Features**:
- Category filter tabs (Members, Attendance, Giving, etc.)
- Search by report name
- Generate button on each card
- Download links in history
- Delete/Share actions on custom reports

#### Step 15: Create Generate Report Dialog
**File**: `src/app/components/generate-report-dialog/generate-report-dialog.ts`

**Form Fields**:
- Report Type (pre-selected)
- Date Range (start/end dates)
- Format (PDF/Excel/CSV dropdown)
- Dynamic filters based on report type
- Advanced options (grouping, sorting)

**Actions**:
- Generate and Download
- Schedule Report (opens schedule dialog)
- Cancel

#### Step 16: Create Report History Dialog
**File**: `src/app/components/report-history-dialog/report-history-dialog.ts`

Shows execution history for a specific report with:
- Execution date/time
- Generated by user
- Format
- File size
- Status
- Download button
- Delete button

#### Step 17: Style Reports Page
**File**: `src/app/reports-page/reports-page.css`

Modern, card-based design similar to dashboard and other pages:
- Report type cards with icons
- Stats overview
- Responsive grid layout
- Category filter tabs
- Search bar
- Action buttons

### 1.6 Testing (Day 14)

#### Step 18: Backend Unit Tests
**Files**:
- `ReportServiceTest.java` - Test report CRUD and generation
- `ReportGeneratorServiceTest.java` - Test individual report generators
- `ExcelReportServiceTest.java` - Test Excel generation

#### Step 19: E2E Tests
**File**: `e2e/reports.spec.ts`

**Test Scenarios**:
```typescript
test('should display all pre-built report types', async () => {});
test('should generate member directory report', async () => {});
test('should filter reports by category', async () => {});
test('should download generated report', async () => {});
test('should view report execution history', async () => {});
test('should generate report with date filters', async () => {});
test('should export report in different formats', async () => {});
```

---

## Phase 2: Custom Report Builder (2 weeks) ⭐⭐

**Objective**: Enable users to create custom reports with field selection, filters, grouping, and sorting.

### 2.1 Backend - Custom Report Engine (Days 15-17)

#### Step 20: Create Query Builder Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/QueryBuilderService.java`

Dynamic query builder that constructs JPA queries based on user-defined criteria.

**Key Methods**:
```java
- Specification<T> buildSpecification(String entityType, Map<String, Object> filters)
- Sort buildSort(String sortConfig)
- List<Object[]> executeCustomQuery(CustomReportConfig config)
- List<String> getAvailableFields(String entityType)
- List<String> getAvailableFilters(String entityType)
```

**Supported Entities**:
- Member
- Attendance
- Donation
- Fellowship
- Event
- PrayerRequest
- Visit

#### Step 21: Create Field Metadata Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/FieldMetadataService.java`

Provides metadata about available fields for each entity (for UI field selector).

**Returns**:
```json
{
  "Member": [
    { "name": "firstName", "displayName": "First Name", "type": "STRING", "filterable": true, "sortable": true },
    { "name": "lastName", "displayName": "Last Name", "type": "STRING", "filterable": true, "sortable": true },
    { "name": "dateOfBirth", "displayName": "Date of Birth", "type": "DATE", "filterable": true, "sortable": true },
    { "name": "membershipDate", "displayName": "Membership Date", "type": "DATE", "filterable": true, "sortable": true },
    { "name": "memberStatus", "displayName": "Status", "type": "ENUM", "filterable": true, "sortable": true, "options": ["ACTIVE", "INACTIVE", "PENDING"] }
  ]
}
```

#### Step 22: Enhance Report Service for Custom Reports
Add methods to ReportService:
```java
- ReportResponse createCustomReport(CustomReportRequest request, Long userId, Long churchId)
- byte[] generateCustomReport(Long reportId, GenerateReportRequest request, Long churchId)
- List<FieldMetadata> getAvailableFields(String entityType)
- List<FilterOption> getAvailableFilters(String entityType)
```

#### Step 23: Create Custom Report DTOs
**File**: `src/main/java/com/reuben/pastcare_spring/dtos/CustomReportRequest.java`

```java
@Data
public class CustomReportRequest {
    private String name;
    private String description;
    private String entityType; // "Member", "Attendance", etc.
    private List<String> selectedFields;
    private List<FilterCriteria> filters;
    private List<SortCriteria> sorting;
    private List<String> groupByFields;
    private Boolean isTemplate;
    private Boolean isShared;
}
```

### 2.2 Frontend - Custom Report Builder (Days 18-24)

#### Step 24: Create Report Builder Page
**File**: `src/app/report-builder-page/report-builder-page.ts`

**Features**:
- Entity type selector
- Field selector (drag-and-drop or multi-select)
- Filter builder (add/remove filter conditions)
- Sort builder (add/remove sort fields)
- Group by selector
- Preview button
- Save button
- Generate button

**UI Flow**:
1. Select entity type (Member, Attendance, etc.)
2. Add fields to report (multi-select checkboxes)
3. Add filters (field + operator + value)
4. Add sorting (field + direction)
5. Add grouping (optional)
6. Preview data (first 10 rows)
7. Save as custom report
8. Generate full report

#### Step 25: Create Field Selector Component
**File**: `src/app/components/field-selector/field-selector.ts`

Displays available fields with:
- Checkboxes for selection
- Search/filter capability
- Category grouping (Demographics, Contact, Dates, etc.)
- Drag-and-drop reordering

#### Step 26: Create Filter Builder Component
**File**: `src/app/components/filter-builder/filter-builder.ts`

Dynamic filter builder with:
- Add filter button
- Field dropdown
- Operator dropdown (equals, contains, greater than, etc.)
- Value input (text, date picker, dropdown based on field type)
- AND/OR logic
- Remove filter button

**Example Filter**:
```
Member Status [equals] ACTIVE
AND
Membership Date [after] 2024-01-01
OR
Last Attendance Date [within last] 30 days
```

#### Step 27: Create Sort Builder Component
**File**: `src/app/components/sort-builder/sort-builder.ts`

Sort configuration with:
- Add sort field button
- Field dropdown
- Direction (ASC/DESC)
- Order priority (drag to reorder)
- Remove button

#### Step 28: Create Report Preview Component
**File**: `src/app/components/report-preview/report-preview.ts`

Shows first 10 rows of report data in a table:
- Column headers (selected fields)
- Data rows
- Refresh button
- Full column names as tooltips

#### Step 29: Create Report Templates Gallery
**File**: `src/app/components/report-templates-gallery/report-templates-gallery.ts`

Displays saved report templates:
- Template cards with name, description, entity type
- Use Template button (loads template into builder)
- Edit Template button
- Delete Template button
- Share Template button

### 2.3 Backend - Report Scheduling (Days 25-26)

#### Step 30: Create Schedule Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/ReportScheduleService.java`

**Key Methods**:
```java
- ReportSchedule createSchedule(ScheduleReportRequest request, Long churchId)
- ReportSchedule updateSchedule(Long id, ScheduleReportRequest request, Long churchId)
- void deleteSchedule(Long id, Long churchId)
- List<ReportSchedule> getActiveSchedules(Long churchId)
- void executeScheduledReports() // Called by scheduled task
- void calculateNextExecutionDate(ReportSchedule schedule)
```

#### Step 31: Create Scheduled Task
**File**: `src/main/java/com/reuben/pastcare_spring/config/ScheduledTasks.java`

Add method:
```java
@Scheduled(cron = "0 0 * * * *") // Every hour
public void executeScheduledReports() {
    reportScheduleService.executeScheduledReports();
}
```

### 2.4 Frontend - Report Scheduling (Days 27-28)

#### Step 32: Create Schedule Report Dialog
**File**: `src/app/components/schedule-report-dialog/schedule-report-dialog.ts`

**Form Fields**:
- Frequency (Daily, Weekly, Monthly, Quarterly, Yearly)
- Day of week (for weekly)
- Day of month (for monthly)
- Execution time (time picker)
- Format (PDF/Excel/CSV)
- Recipients (email multi-input)
- Active toggle

**Actions**:
- Save Schedule
- Cancel

#### Step 33: Create Scheduled Reports Page
**File**: `src/app/scheduled-reports-page/scheduled-reports-page.ts`

Displays all scheduled reports:
- Report name
- Frequency
- Next execution date
- Last execution date
- Recipients
- Active status toggle
- Edit button
- Delete button

---

## Phase 3: Export & Visualization (1 week) ⭐

**Objective**: Enhance reports with charts, better formatting, email distribution, and archiving.

### 3.1 Backend - Enhanced Export (Days 29-30)

#### Step 34: Add Chart Generation to PDF Service
Enhance PdfReportService to support:
- Bar charts
- Line charts
- Pie charts
- Trend graphs

Use JFreeChart or similar library.

#### Step 35: Add Church Branding Support
**File**: `src/main/java/com/reuben/pastcare_spring/services/ReportBrandingService.java`

**Key Methods**:
```java
- byte[] addChurchLogo(byte[] reportBytes, Long churchId)
- void applyChurchColors(Document document, Long churchId)
- String getChurchHeaderText(Long churchId)
```

**Church Settings** (add to Church entity):
- logoUrl
- primaryColor
- secondaryColor
- reportHeaderText
- reportFooterText

#### Step 36: Create Email Distribution Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/ReportEmailService.java`

**Key Methods**:
```java
- void emailReport(Long executionId, List<String> recipients, Long churchId)
- void emailScheduledReport(ReportExecution execution, ReportSchedule schedule)
```

Uses existing email service/infrastructure.

### 3.2 Frontend - Visualization (Days 31-33)

#### Step 37: Add Chart Support to Reports
**File**: `src/app/components/report-chart-viewer/report-chart-viewer.ts`

Display charts in reports using Chart.js or ngx-charts:
- Chart type selector
- Chart configuration
- Export chart as image
- Include in PDF toggle

#### Step 38: Create Print Preview Dialog
**File**: `src/app/components/report-print-preview/report-print-preview.ts`

Print-optimized view:
- Page breaks
- Headers/footers
- Church logo
- Print button
- Export to PDF button

#### Step 39: Enhance Report Display
Add to existing report views:
- Summary statistics at top
- Charts/graphs (if applicable)
- Grouped sections
- Subtotals/totals
- Conditional formatting (e.g., highlight overdue items)

### 3.3 Backend - Report Archiving (Day 34)

#### Step 40: Create Report Archive Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/ReportArchiveService.java`

**Key Methods**:
```java
- void archiveReport(Long executionId, Long churchId)
- void cleanupOldReports(int daysToKeep)
- List<ReportExecution> getArchivedReports(Long churchId)
- void restoreArchivedReport(Long executionId)
```

**Database Changes**:
Add to ReportExecution:
- `archived` (boolean)
- `archivedAt` (timestamp)
- `archiveUrl` (for long-term storage like S3)

#### Step 41: Create File Storage Service
**File**: `src/main/java/com/reuben/pastcare_spring/services/FileStorageService.java`

Handles file storage (local or S3):
```java
- String storeFile(byte[] data, String fileName, Long churchId)
- byte[] retrieveFile(String fileUrl)
- void deleteFile(String fileUrl)
```

### 3.4 Frontend - Report Management (Day 35)

#### Step 42: Create Report Archive Page
**File**: `src/app/report-archive-page/report-archive-page.ts`

Displays archived reports:
- Archive date
- Report name
- File size
- Download button
- Restore button
- Delete permanently button

---

## Implementation Checklist

### Backend Tasks
- [ ] Phase 1: Pre-built Reports
  - [ ] Create report enums (ReportType, ReportFormat, ScheduleFrequency, ExecutionStatus)
  - [ ] Create entities (Report, ReportSchedule, ReportExecution)
  - [ ] Create repositories (3 repositories)
  - [ ] Create DTOs (6 DTOs)
  - [ ] Create database migrations (V52-V54)
  - [ ] Create ReportGeneratorService (10 report generators)
  - [ ] Create ExcelReportService
  - [ ] Create PdfReportService
  - [ ] Create CsvReportService
  - [ ] Create ReportService (main orchestration)
  - [ ] Create ReportController (13 endpoints)
  - [ ] Write unit tests

- [ ] Phase 2: Custom Report Builder
  - [ ] Create QueryBuilderService
  - [ ] Create FieldMetadataService
  - [ ] Enhance ReportService for custom reports
  - [ ] Create CustomReportRequest DTO
  - [ ] Create ReportScheduleService
  - [ ] Add scheduled task for report execution
  - [ ] Write unit tests

- [ ] Phase 3: Export & Visualization
  - [ ] Enhance PdfReportService with charts
  - [ ] Create ReportBrandingService
  - [ ] Create ReportEmailService
  - [ ] Create ReportArchiveService
  - [ ] Create FileStorageService
  - [ ] Update Church entity for branding
  - [ ] Write unit tests

### Frontend Tasks
- [ ] Phase 1: Pre-built Reports
  - [ ] Create ReportService (TypeScript)
  - [ ] Create ReportsPage component
  - [ ] Create reports-page.html
  - [ ] Create reports-page.css
  - [ ] Create GenerateReportDialog component
  - [ ] Create ReportHistoryDialog component
  - [ ] Add route and navigation link
  - [ ] Write E2E tests

- [ ] Phase 2: Custom Report Builder
  - [ ] Create ReportBuilderPage component
  - [ ] Create FieldSelectorComponent
  - [ ] Create FilterBuilderComponent
  - [ ] Create SortBuilderComponent
  - [ ] Create ReportPreviewComponent
  - [ ] Create ReportTemplatesGallery component
  - [ ] Create ScheduleReportDialog component
  - [ ] Create ScheduledReportsPage component
  - [ ] Write E2E tests

- [ ] Phase 3: Export & Visualization
  - [ ] Create ReportChartViewer component
  - [ ] Create ReportPrintPreview dialog
  - [ ] Enhance report display with charts
  - [ ] Create ReportArchivePage component
  - [ ] Write E2E tests

### Database Tasks
- [ ] Create V52__create_reports_table.sql
- [ ] Create V53__create_report_schedules_table.sql
- [ ] Create V54__create_report_executions_table.sql
- [ ] Add branding fields to churches table
- [ ] Run migrations

### Testing Tasks
- [ ] Unit tests for all services (12 test classes)
- [ ] E2E tests for all user flows (15 scenarios)
- [ ] Test report generation for all types
- [ ] Test custom report builder
- [ ] Test report scheduling
- [ ] Test export formats (PDF, Excel, CSV)

### Dependencies to Add
**Backend** (pom.xml):
```xml
<!-- Apache POI for Excel (already exists) -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>

<!-- iText for PDF generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- JFreeChart for chart generation -->
<dependency>
    <groupId>org.jfree</groupId>
    <artifactId>jfreechart</artifactId>
    <version>1.5.4</version>
</dependency>

<!-- Apache Commons CSV -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version>
</dependency>
```

**Frontend** (package.json):
```json
{
  "dependencies": {
    "chart.js": "^4.4.0",
    "ng2-charts": "^5.0.4",
    "file-saver": "^2.0.5"
  }
}
```

---

## Navigation Integration

### Side Nav
Add to side nav under new "Reports" section:
```html
<!-- Reports Section -->
<div class="nav-section">
  <h3>Reports</h3>
  <a routerLink="/reports" routerLinkActive="active">
    <i class="pi pi-chart-bar"></i>
    <span>All Reports</span>
  </a>
  <a routerLink="/report-builder" routerLinkActive="active">
    <i class="pi pi-sliders-h"></i>
    <span>Custom Builder</span>
  </a>
  <a routerLink="/scheduled-reports" routerLinkActive="active">
    <i class="pi pi-clock"></i>
    <span>Scheduled</span>
  </a>
  <a routerLink="/report-archive" routerLinkActive="active">
    <i class="pi pi-folder-open"></i>
    <span>Archive</span>
  </a>
</div>
```

### Routes
```typescript
{
  path: 'reports',
  component: ReportsPage,
  canActivate: [authGuard]
},
{
  path: 'report-builder',
  component: ReportBuilderPage,
  canActivate: [authGuard]
},
{
  path: 'report-builder/:id',
  component: ReportBuilderPage,
  canActivate: [authGuard]
},
{
  path: 'scheduled-reports',
  component: ScheduledReportsPage,
  canActivate: [authGuard]
},
{
  path: 'report-archive',
  component: ReportArchivePage,
  canActivate: [authGuard]
}
```

---

## Security Considerations

### Role-Based Access
- **ADMIN/PASTOR**: Full access to all reports
- **TREASURER**: Access to giving and financial reports
- **FELLOWSHIP_LEADER**: Access to fellowship and member reports
- **MEMBER**: No access (reports are admin-only)

### Data Privacy
- Sensitive data (phone numbers, addresses) only in authorized reports
- Confidential notes excluded from all reports
- Financial data only accessible to ADMIN and TREASURER roles
- Report sharing requires explicit permission

### File Security
- Generated reports stored with unique filenames (UUIDs)
- Access controlled via church_id filtering
- Automatic cleanup of old reports (configurable retention period)
- Secure file storage (S3 with presigned URLs or encrypted local storage)

---

## Performance Optimization

### Query Optimization
- Use pagination for large datasets
- Indexed columns for common filters (church_id, report_type, execution_date)
- Eager loading for related entities when needed
- Query result caching for frequently run reports

### File Generation
- Asynchronous report generation for large reports (> 10,000 rows)
- Progress tracking for long-running reports
- Limit row count with warnings (e.g., max 100,000 rows)

### Storage Optimization
- Compress large files before storage
- Automatic cleanup of old executions (default: 90 days)
- Archive old reports to cheaper storage (S3 Glacier)

---

## Future Enhancements (Post-MVP)

### Phase 4: Advanced Features
- **Interactive Reports**: Drill-down capabilities, clickable charts
- **Real-time Reports**: Live data updates for dashboards
- **Multi-language Support**: Reports in multiple languages
- **Custom Calculations**: Formula builder for calculated fields
- **Report Annotations**: Add notes and comments to reports
- **Report Comparison**: Compare two time periods side-by-side
- **Export to BI Tools**: Integration with Tableau, Power BI
- **API Access**: Generate reports via REST API for external systems

### Phase 5: AI-Powered Insights
- **Automated Insights**: AI-generated commentary on trends
- **Anomaly Detection**: Highlight unusual patterns
- **Predictive Analytics**: Forecast future trends
- **Natural Language Queries**: "Show me members who joined last month"

---

## Success Metrics

### Phase 1 Success Criteria
- ✅ All 10 pre-built reports working
- ✅ Export to Excel, PDF, CSV functional
- ✅ Report history tracking
- ✅ Download links working
- ✅ Basic filtering (date range)
- ✅ E2E tests passing

### Phase 2 Success Criteria
- ✅ Custom report builder functional
- ✅ Field selection working
- ✅ Filter builder with multiple conditions
- ✅ Sort and group by working
- ✅ Preview before generating
- ✅ Save as template
- ✅ Report scheduling working
- ✅ Scheduled reports execute automatically

### Phase 3 Success Criteria
- ✅ Charts in PDF reports
- ✅ Church branding applied
- ✅ Email distribution working
- ✅ Print-optimized layouts
- ✅ Report archiving functional
- ✅ All E2E tests passing

---

## Timeline Summary

### Week 1 (Days 1-7)
- Backend infrastructure (entities, repos, migrations)
- Report generation services (Excel, PDF, CSV)
- Core report generators (5 of 10)

### Week 2 (Days 8-14)
- Remaining report generators (5 of 10)
- Report management service
- Frontend reports page
- E2E tests for Phase 1

### Week 3 (Days 15-21)
- Custom report engine (query builder, field metadata)
- Report builder frontend
- Field selector, filter builder, sort builder

### Week 4 (Days 22-28)
- Report scheduling (backend + frontend)
- Report templates gallery
- Scheduled reports page
- E2E tests for Phase 2

### Week 5 (Days 29-35)
- Chart generation
- Church branding
- Email distribution
- Report archiving
- Final testing and polish

**Total Duration**: 5 weeks (35 days)

---

## Notes

- Leverage existing AttendanceExportService as reference for Excel generation
- Reuse existing patterns from other modules (signals, computed, PrimeNG components)
- Follow TDD approach - write tests as you implement features
- Use existing multi-tenancy filters (church_id) consistently
- Store report files securely with access control
- Consider performance for large datasets (pagination, async generation)
- Make reports mobile-responsive (card-based layouts)

---

## Next Steps

1. **Get Approval**: Review this plan with stakeholders
2. **Set Up Environment**: Ensure all dependencies are installed
3. **Create Feature Branch**: `git checkout -b feature/reports-module`
4. **Start Phase 1, Step 1**: Create ReportType enum
5. **Follow TDD**: Write test → Implement → Verify → Commit
6. **Track Progress**: Update PLAN.md as phases complete

---

**Ready to begin implementation!** 🚀
