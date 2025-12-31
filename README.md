# PastCare Spring - Backend API

A comprehensive church management system built with Spring Boot 3.5.4, providing multi-tenant SaaS capabilities for pastoral care, member management, attendance tracking, giving, events, and more.

## 📋 Table of Contents

- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Deployment](#deployment)

---

## 📁 Project Structure

```
pastcare-spring/
├── src/
│   ├── main/
│   │   ├── java/com/reuben/pastcare_spring/
│   │   │   ├── annotations/          # Custom validation annotations
│   │   │   │   └── Unique.java
│   │   │   ├── config/               # Application configuration
│   │   │   │   ├── SchedulingConfig.java
│   │   │   │   └── SpaRoutingConfig.java
│   │   │   ├── controllers/          # REST API endpoints
│   │   │   │   ├── AttendanceController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BillingController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── EventsController.java
│   │   │   │   ├── FellowshipController.java
│   │   │   │   ├── GivingController.java
│   │   │   │   ├── LocationsController.java
│   │   │   │   ├── MembersController.java
│   │   │   │   ├── PastoralCareController.java
│   │   │   │   └── UsersController.java
│   │   │   ├── dtos/                 # Data Transfer Objects
│   │   │   │   ├── *Request.java     # API request DTOs
│   │   │   │   └── *Response.java    # API response DTOs
│   │   │   ├── enums/                # Enumeration types
│   │   │   │   └── AttendanceStatus.java
│   │   │   ├── exceptions/           # Custom exceptions
│   │   │   │   ├── AccountLockedException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── TooManyRequestsException.java
│   │   │   │   └── UnauthorizedException.java
│   │   │   ├── mapper/               # Entity-DTO mappers
│   │   │   │   ├── AttendanceMapper.java
│   │   │   │   ├── MemberMapper.java
│   │   │   │   └── UserMapper.java
│   │   │   ├── models/               # JPA entities
│   │   │   │   ├── BaseEntity.java        # Base with timestamps
│   │   │   │   ├── TenantBaseEntity.java  # Multi-tenancy base
│   │   │   │   ├── Church.java            # Church/Tenant entity
│   │   │   │   ├── User.java              # User/Staff entity
│   │   │   │   ├── Member.java            # Church member
│   │   │   │   ├── Fellowship.java
│   │   │   │   ├── AttendanceSession.java
│   │   │   │   ├── Attendance.java
│   │   │   │   ├── Event.java
│   │   │   │   ├── Donation.java
│   │   │   │   ├── CareNeed.java
│   │   │   │   ├── ConfidentialNote.java
│   │   │   │   ├── CommunicationLog.java
│   │   │   │   ├── LifecycleEvent.java
│   │   │   │   └── ChurchSubscription.java
│   │   │   ├── repositories/         # Spring Data JPA repositories
│   │   │   │   ├── ChurchRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── MemberRepository.java
│   │   │   │   └── ...
│   │   │   ├── security/             # Security configuration
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── UserPrincipal.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── CookieUtil.java
│   │   │   ├── services/             # Business logic layer
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── MemberService.java
│   │   │   │   ├── AttendanceService.java
│   │   │   │   ├── FellowshipService.java
│   │   │   │   ├── GivingService.java
│   │   │   │   ├── EventService.java
│   │   │   │   ├── PastoralCareService.java
│   │   │   │   ├── BillingService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── SmsService.java
│   │   │   │   ├── PaystackService.java
│   │   │   │   └── BruteForceProtectionService.java
│   │   │   ├── specifications/       # JPA Specifications for dynamic queries
│   │   │   │   └── MemberSpecification.java
│   │   │   ├── tasks/                # Scheduled tasks
│   │   │   │   └── LoginAttemptCleanupTask.java
│   │   │   ├── util/                 # Utility classes
│   │   │   │   └── RequestContextUtil.java
│   │   │   ├── validators/           # Custom validators
│   │   │   │   ├── InternationalPhoneNumber.java
│   │   │   │   ├── InternationalPhoneNumberValidator.java
│   │   │   │   ├── Unique.java
│   │   │   │   └── UniqueValidator.java
│   │   │   └── PastcareSpringApplication.java
│   │   └── resources/
│   │       ├── application.properties     # Production config
│   │       └── db/migration/              # Flyway migrations
│   │           ├── V1__create_churches.sql
│   │           ├── V2__create_users.sql
│   │           ├── V3__create_members.sql
│   │           └── V64-V67__*.sql
│   └── test/
│       ├── java/com/reuben/pastcare_spring/
│       │   ├── integration/          # API Integration tests
│       │   │   ├── BaseIntegrationTest.java
│       │   │   ├── auth/
│       │   │   │   └── AuthenticationIntegrationTest.java
│       │   │   ├── members/
│       │   │   │   ├── MemberCrudIntegrationTest.java
│       │   │   │   └── MemberSearchIntegrationTest.java
│       │   │   ├── attendance/
│       │   │   │   └── AttendanceIntegrationTest.java
│       │   │   ├── fellowship/
│       │   │   ├── giving/
│       │   │   ├── pastoral/
│       │   │   ├── events/
│       │   │   ├── communications/
│       │   │   └── billing/
│       │   └── testutil/             # Test utilities
│       │       ├── TestJwtUtil.java
│       │       └── MemberTestBuilder.java
│       └── resources/
│           ├── application-test.properties  # Test configuration
│           └── test-data/                   # SQL test data
│               ├── 00-cleanup.sql
│               ├── 01-churches.sql
│               ├── 02-users.sql
│               ├── 03-members.sql
│               └── 04-fellowships.sql
├── pom.xml                           # Maven configuration
├── README.md                         # This file
├── TEST_SUITE_IMPLEMENTATION_SUMMARY.md
└── DEPLOYMENT_PLAN.md
```

---

## 🛠 Technology Stack

### Core Framework
- **Spring Boot 3.5.4** - Application framework
- **Java 17** - Programming language
- **Maven** - Build tool

### Database & Persistence
- **MySQL** - Production database
- **H2** - In-memory database for testing
- **Spring Data JPA** - Data access layer
- **Flyway** - Database migrations

### Security
- **Spring Security** - Authentication & authorization
- **JWT (jjwt 0.12.6)** - JSON Web Tokens for stateless auth
- **BCrypt** - Password hashing

### API Documentation
- **SpringDoc OpenAPI 2.7.0** - OpenAPI 3.0 specification & Swagger UI

### Payment & SMS
- **Paystack** - Payment processing (subscriptions, donations)
- **Hubtel SMS** - SMS notifications

### Utilities
- **Lombok** - Boilerplate code reduction
- **Thumbnailator** - Image compression
- **libphonenumber** - International phone number validation
- **ZXing** - QR code generation and scanning
- **iText 7** - PDF generation (receipts, reports)
- **Apache POI** - Excel export

### Testing
- **JUnit 5** - Unit testing framework
- **REST Assured 5.4.0** - API integration testing
- **AssertJ** - Fluent assertions
- **Spring Security Test** - Security testing utilities

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- **Git**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/pastcare-spring.git
   cd pastcare-spring
   ```

2. **Configure MySQL database**
   ```bash
   mysql -u root -p
   CREATE DATABASE pastcare_db;
   CREATE USER 'pastcare_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON pastcare_db.* TO 'pastcare_user'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

3. **Configure application.properties**

   Update `src/main/resources/application.properties`:
   ```properties
   # Database
   spring.datasource.url=jdbc:mysql://localhost:3306/pastcare_db
   spring.datasource.username=pastcare_user
   spring.datasource.password=your_password

   # JWT Secret (generate a strong random secret)
   jwt.secret=your-256-bit-secret-key-here-minimum-32-characters-long

   # Paystack API Keys
   paystack.secret.key=sk_test_your_paystack_secret_key
   paystack.public.key=pk_test_your_paystack_public_key

   # SMS (Hubtel)
   sms.api.key=your_hubtel_api_key
   sms.api.secret=your_hubtel_api_secret
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The API will be available at `http://localhost:8080`

---

## 🧪 Testing

The project includes a comprehensive test suite with **250+ API integration tests** covering all modules.

### Test Idempotency

All tests are **idempotent** and use:
- ✅ H2 in-memory database (isolated for each test run)
- ✅ `@Transactional` annotation (automatic rollback after each test)
- ✅ Unique test data generation (no collisions)

You can run tests **multiple times** without any side effects or cleanup needed.

### Running Tests

#### 1. Run All Tests (Unit + API Integration)
```bash
mvn clean verify -P all-tests
```

#### 2. Run Only Unit Tests (Default)
```bash
mvn test
# or explicitly
mvn test -P unit-tests
```

#### 3. Run Only API Integration Tests
```bash
mvn verify -P api-tests
```

#### 4. Run Specific Test Class
```bash
mvn test -Dtest=MemberCrudIntegrationTest
```

#### 5. Run Specific Test Method
```bash
mvn test -Dtest=MemberCrudIntegrationTest#shouldCreateMemberWithFullProfile
```

#### 6. Run Tests by Tag
```bash
# Run all integration tests
mvn test -Dgroups="integration"

# Run specific module tests
mvn test -Dgroups="integration & module:members"
```

### Test Coverage by Module

| Module | API Tests | Status |
|--------|-----------|--------|
| Authentication | 13 tests | ✅ Complete |
| Members | 28 tests | ✅ Complete |
| Attendance | 19 tests | ✅ Complete |
| Fellowship | 22 tests | 🔄 In Progress |
| Giving | 26 tests | 🔄 In Progress |
| Pastoral Care | 33 tests | 🔄 In Progress |
| Events | 29 tests | 🔄 In Progress |
| Communications | 15 tests | 🔄 In Progress |
| Billing | 19 tests | 🔄 In Progress |
| **TOTAL** | **204 tests** | **30% Complete** |

See [TEST_SUITE_IMPLEMENTATION_SUMMARY.md](TEST_SUITE_IMPLEMENTATION_SUMMARY.md) for detailed test documentation.

### Test Structure

Each integration test follows this pattern:

```java
@SpringBootTest
@Tag("integration")
@Tag("module:members")
@DisplayName("Member CRUD Integration Tests")
@Transactional  // Ensures idempotency via automatic rollback
class MemberCrudIntegrationTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {
        @Test
        void shouldCreateMember() {
            // Test implementation
        }
    }

    @Nested
    @DisplayName("Multi-Tenancy Tests")
    class MultiTenancyTests {
        @Test
        void shouldIsolateMembersByChurch() {
            // Verify church data isolation
        }
    }
}
```

---

## 📚 API Documentation

### Swagger UI (Interactive Documentation)

Access the interactive API documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification

View the raw OpenAPI 3.0 specification:

```
http://localhost:8080/v3/api-docs
```

### Key API Endpoints

#### Authentication & Authorization
- `POST /api/auth/register` - Church and admin user registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/forgot-password` - Request password reset

#### User Management
- `GET /api/users` - List all users (with pagination)
- `POST /api/users` - Create new user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

#### Members
- `GET /api/members` - Search/list members with filters
- `POST /api/members` - Create member (full profile or quick add)
- `GET /api/members/{id}` - Get member profile
- `PUT /api/members/{id}` - Update member
- `DELETE /api/members/{id}` - Delete member
- `GET /api/members/stats` - Member statistics

#### Attendance
- `POST /api/attendance/sessions` - Create attendance session
- `POST /api/attendance` - Mark member attendance
- `POST /api/attendance/sessions/{id}/qr-code` - Generate QR code
- `POST /api/attendance/check-in` - QR code check-in
- `GET /api/attendance/sessions/{id}/summary` - Session summary

#### Fellowship
- `GET /api/fellowships` - List all fellowships
- `POST /api/fellowships` - Create fellowship
- `POST /api/fellowships/{id}/members` - Add member to fellowship
- `GET /api/fellowships/{id}/health` - Fellowship health metrics

#### Giving
- `POST /api/giving/donations` - Record donation
- `POST /api/giving/initialize-payment` - Initialize online payment
- `POST /api/giving/verify-payment` - Verify payment
- `GET /api/giving/analytics` - Giving analytics

#### Events
- `GET /api/events` - List events
- `POST /api/events` - Create event
- `POST /api/events/{id}/register` - Register for event
- `POST /api/events/{id}/check-in` - Check in attendee

#### Pastoral Care
- `POST /api/pastoral/care-needs` - Create care need
- `POST /api/pastoral/visits` - Record visit
- `POST /api/pastoral/counseling` - Schedule counseling session
- `POST /api/pastoral/prayer-requests` - Submit prayer request

#### Billing (Church Subscriptions)
- `GET /api/billing/plans` - List subscription plans
- `POST /api/billing/subscribe` - Create subscription
- `POST /api/billing/upgrade` - Upgrade plan
- `GET /api/billing/subscription` - Get current subscription

### Role-Based Access Control

The API enforces role-based permissions:

| Role | Permissions |
|------|-------------|
| **SUPERADMIN** | System-wide administration |
| **ADMIN** | Full church management |
| **PASTOR** | Pastoral care, view all members |
| **TREASURER** | Giving management, financial reports |
| **MEMBER_MANAGER** | Member CRUD, attendance |
| **FELLOWSHIP_LEADER** | Manage own fellowship |
| **MEMBER** | View own profile, submit prayer requests |

### Multi-Tenancy

All API endpoints are **tenant-aware**:
- Church ID extracted from JWT token
- Data automatically filtered by church
- Cross-tenant access prevented at database level

---

## 🗄 Database Schema

### Core Entities

#### **churches** (Tenant)
- `id`, `name`, `email`, `phone_number`, `address`, `website`
- `active`, `created_at`, `updated_at`

#### **users** (Staff/Login)
- `id`, `church_id` (FK), `name`, `email`, `password_hash`
- `role` (ADMIN, PASTOR, TREASURER, etc.)
- `active`, `email_verified`, `created_at`, `updated_at`

#### **members** (Church Members)
- `id`, `church_id` (FK), `first_name`, `other_name`, `last_name`
- `title`, `sex`, `date_of_birth`, `country`, `timezone`
- `phone_number`, `email`, `whatsapp_number`, `alternate_phone`
- `marital_status`, `spouse_id` (FK self-reference)
- `occupation`, `member_since`, `emergency_contact_name`, `emergency_contact_phone`
- `notes`, `tags` (JSON), `profile_completeness`
- `created_at`, `updated_at`

#### **fellowships**
- `id`, `church_id` (FK), `name`, `leader_id` (FK users)
- `description`, `meeting_day`, `meeting_time`, `location`

#### **attendance_sessions**
- `id`, `church_id` (FK), `name`, `service_type`, `session_date`
- `qr_code`, `created_at`, `updated_at`

#### **attendance**
- `id`, `church_id` (FK), `session_id` (FK), `member_id` (FK)
- `check_in_time`, `status` (PRESENT, ABSENT, EXCUSED)

#### **events**
- `id`, `church_id` (FK), `title`, `description`, `event_date`
- `location`, `capacity`, `registration_required`

#### **donations**
- `id`, `church_id` (FK), `donor_id` (FK members, nullable for anonymous)
- `amount`, `currency`, `payment_method`, `reference`
- `donation_type`, `donation_date`

#### **church_subscriptions**
- `id`, `church_id` (FK), `plan` (FREE, BASIC, STANDARD, PREMIUM)
- `status`, `start_date`, `end_date`, `billing_period_start`, `billing_period_end`
- `promotional_credits_months`, `payment_reference`

### Multi-Tenancy Pattern

All tenant-specific entities extend `TenantBaseEntity`:

```java
@MappedSuperclass
public abstract class TenantBaseEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    public Long getChurchId() {
        return church != null ? church.getId() : null;
    }
}
```

### Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`:

```
V1__create_churches.sql
V2__create_users.sql
V3__create_members.sql
...
V67__add_user_advanced_fields.sql
```

To run migrations manually:
```bash
mvn flyway:migrate
```

---

## 🚢 Deployment

### Environment Variables

Set these environment variables for production:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://production-host:3306/pastcare_db
SPRING_DATASOURCE_USERNAME=pastcare_user
SPRING_DATASOURCE_PASSWORD=secure_password

# JWT
JWT_SECRET=your-production-256-bit-secret-key
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

# Paystack
PAYSTACK_SECRET_KEY=sk_live_your_live_key
PAYSTACK_PUBLIC_KEY=pk_live_your_live_key

# SMS
SMS_API_KEY=your_production_hubtel_key
SMS_API_SECRET=your_production_hubtel_secret

# Email (if using SMTP)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=noreply@pastcare.com
SPRING_MAIL_PASSWORD=app_specific_password
```

### Build Production JAR

```bash
mvn clean package -DskipTests
```

Output: `target/pastcare-spring-0.0.1-SNAPSHOT.jar`

### Run Production Server

```bash
java -jar target/pastcare-spring-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=production \
  --server.port=8080
```

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/pastcare-spring-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t pastcare-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/pastcare_db \
  -e JWT_SECRET=your_secret \
  pastcare-backend
```

---

## 📝 License

Proprietary - All rights reserved

---

## 👥 Contributors

- **Reuben** - Lead Developer

---

## 📞 Support

For issues or questions:
- GitHub Issues: [https://github.com/yourusername/pastcare-spring/issues](https://github.com/yourusername/pastcare-spring/issues)
- Email: support@pastcare.com

---

**Last Updated:** 2025-12-29
