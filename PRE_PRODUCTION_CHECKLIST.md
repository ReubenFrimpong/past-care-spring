# Pre-Production Checklist & Answers

**Date**: December 31, 2025
**Status**: READY FOR REVIEW

---

## Your Questions

### 1. Database Migration Consolidation
### 2. Promotional Access Code UI Location
### 3. Subscription Plans Auto-Insert
### 4. Safe SUPERADMIN Account Creation
### 5. Church Registration Form Button Fixes

---

## 1. ✅ Database Migration Consolidation

### Current State
You have **77 migration files** (V1 to V80), which is fine for development but should be consolidated before production.

### Recommended Consolidation Strategy

#### Option A: Keep All Migrations (Recommended for Now)
**Pros**:
- No risk of breaking existing development databases
- Full audit trail of all changes
- Easy to track when features were added

**Cons**:
- Many migration files to manage
- Some duplicate/superseded migrations exist

**When to use**: If you have existing development/staging databases with data you need to preserve.

#### Option B: Consolidate to Single Initial Migration
**Pros**:
- Clean slate for production
- Single source of truth for initial schema
- Easier to understand database structure

**Cons**:
- Requires dropping all existing databases
- Loses migration history

**When to use**: If starting fresh for production with no existing data to preserve.

### My Recommendation: **Hybrid Approach**

Create a consolidated initial migration **while keeping the old ones**:

1. **Export current schema** as one migration file
2. **Mark old migrations as "legacy"** (move to `legacy/` folder)
3. **Start production with fresh V1_initial_schema.sql**
4. **Keep migration history** for reference

### Issues Found in Current Migrations

#### ❌ **CRITICAL: Missing STARTER Plan**
**Files**: V58, V65, V70
- V58 creates STARTER plan in table creation
- V65 only inserts STANDARD plan
- V70 adds BASIC, PRO, ENTERPRISE
- **Missing**: STARTER plan re-insertion if V58 is skipped

**Fix Required**: Consolidate subscription plan insertions into one migration.

#### ⚠️ **Deleted Migrations**
Your git status shows deleted migrations:
```
D src/main/resources/db/migration/V10__add_email_to_member.sql
D src/main/resources/db/migration/V19__create_recurring_donations_table.sql
D src/main/resources/db/migration/V20__create_payment_transactions_table.sql
D src/main/resources/db/migration/V29__Create_Event_Images_Table.sql
```

**These were replaced by**:
- V75__add_email_to_member.sql
- V76__create_recurring_donations_table.sql
- V77__create_payment_transactions_table.sql
- V78__Create_Event_Images_Table.sql

**Recommendation**: Clean up git by committing these deletions.

#### ⚠️ **Duplicate Content**
- V65 and V70 both insert subscription plans
- Should be consolidated into one comprehensive migration

### Action Plan for Fresh Production Database

I'll create a new consolidated migration file that can be used to bootstrap production:

**File**: `V1__initial_production_schema.sql`
- Contains ALL tables
- Contains ALL subscription plans (STARTER, BASIC, STANDARD, PRO, ENTERPRISE)
- Contains ALL default data
- Contains ALL constraints and indexes

**Then**: Move V1-V80 to `src/main/resources/db/migration/legacy/`

**When deploying to production**:
1. Use the consolidated V1 for initial setup
2. Future changes use V2, V3, etc.
3. Keep legacy migrations for reference

---

## 2. 🔍 Promotional Access Code UI Location

### Where to Create Partnership/Promotional Codes

Partnership codes are managed through **SUPERADMIN-only API endpoints**. There's currently **NO UI** for this.

### Backend Endpoints

#### Create Partnership Code (SUPERADMIN only)
```
POST /api/admin/partnership-codes
```

**API Location**: [PartnershipCodeController.java](src/main/java/com/reuben/pastcare_spring/controllers/PartnershipCodeController.java)

**Current Issue**: Controller only has `apply`, `validate`, and `grace-period/status` endpoints.
**Missing**: CREATE, LIST, DELETE endpoints for SUPERADMIN

### Where the UI Should Be

**Platform Admin Dashboard** (SUPERADMIN portal):
1. **Navigation**: Settings → Partnership Codes
2. **Features Needed**:
   - Create new partnership code
   - Set grace period days (e.g., 30, 60, 90 days)
   - Set description
   - Set expiration date
   - Set max uses
   - View active codes
   - Deactivate codes

**Church Registration Flow**:
- Input field for partnership code during church signup
- Validates code before completing registration
- Automatically applies grace period

### Required Implementation

#### Backend (Missing):
```java
// In PartnershipCodeController
@PostMapping // Create partnership code (SUPERADMIN only)
@GetMapping  // List all partnership codes (SUPERADMIN only)
@DeleteMapping("/{id}") // Deactivate code (SUPERADMIN only)
```

#### Frontend (Missing):
```
past-care-spring-frontend/src/app/
├── admin-dashboard/           # SUPERADMIN portal (DOESN'T EXIST YET)
│   ├── partnership-codes/
│   │   ├── partnership-codes.component.ts
│   │   ├── partnership-codes.component.html
│   │   └── create-code-dialog.component.ts
```

### Current Workaround

**Create codes directly in database**:
```sql
INSERT INTO partnership_codes (
    code, description, grace_period_days,
    max_uses, is_active, created_at, expires_at
) VALUES (
    'PARTNER2025Q1',
    'Q1 2025 Partnership Program',
    90,  -- 90 days grace period
    100, -- Max 100 uses
    1,   -- Active
    NOW(),
    '2025-03-31'
);
```

**Then churches can use** via API:
```bash
POST /api/partnership-codes/apply
Body: { "code": "PARTNER2025Q1" }
```

### Recommendation

**For Pre-Production**:
1. Create partnership codes via SQL
2. Document the codes for your sales/support team
3. Churches apply codes during registration

**For Post-Launch**:
1. Build SUPERADMIN dashboard UI
2. Add partnership code management screen
3. Self-service code creation for admins

---

## 3. ✅ Subscription Plans Auto-Insert

### Current Status

**Good news**: Subscription plans ARE already auto-inserted via migrations!

**Files**:
- [V65__insert_default_subscription_plans.sql](src/main/resources/db/migration/V65__insert_default_subscription_plans.sql) - Inserts STANDARD plan
- [V70__add_additional_subscription_plans.sql](src/main/resources/db/migration/V70__add_additional_subscription_plans.sql) - Inserts BASIC, PRO, ENTERPRISE

### Problem: STARTER Plan Missing

**Issue**: The free STARTER plan is created in V58 table creation but not re-inserted in later migrations.

**Risk**: If migrations are run from scratch, STARTER plan might be missing.

### Fix Required

Create a comprehensive subscription plans migration that includes ALL plans aligned with your pricing model.

### Aligned with Pricing Model

Based on [PRICING_MODEL_REVISED.md](PRICING_MODEL_REVISED.md):

**Plans Needed**:
1. **STARTER** (Free, 2GB, limited features) - For testing/demo
2. **STANDARD** ($9.99/month, 2GB, all features) - Main plan

**Plans to Remove** (not in pricing model):
- BASIC
- PRO
- ENTERPRISE

**Storage Add-ons** (separate table):
- +3GB: $1.50/month
- +8GB: $3.00/month
- +18GB: $6.00/month
- +48GB: $12.00/month

### I'll Create: V81__consolidate_subscription_plans.sql

This will:
1. Remove outdated plans (BASIC, PRO, ENTERPRISE)
2. Ensure STARTER plan exists (free, 2GB)
3. Ensure STANDARD plan exists ($9.99, 2GB)
4. Match exactly with your pricing model
5. Be idempotent (can run multiple times safely)

---

## 4. 🔐 Safe SUPERADMIN Account Creation

### Current Situation

**Problem**: Database starts with NO users, making login impossible.

**Previous Fix**: I manually created SUPERADMIN in database (not ideal for production).

### Recommended Solutions (Pick One)

### Option A: Database Migration (Recommended for Production)

**Pros**:
- Automatic on first deployment
- Documented in migration history
- Can be version controlled

**Cons**:
- Password visible in migration file (can be changed after login)
- Everyone with repo access sees initial password

**Implementation**: Create V82__create_initial_superadmin.sql

```sql
-- Insert initial SUPERADMIN user
-- IMPORTANT: Change password immediately after first login
INSERT INTO users (
    created_at, updated_at, account_locked, email,
    failed_login_attempts, is_active, must_change_password,
    name, password, role, church_id, title
)
SELECT
    NOW(), NOW(), 0, 'admin@pastcare.com',
    0, 1, 1,  -- must_change_password = TRUE
    'Platform Administrator',
    '$2b$12$QV/DZPulRFmk2SsribdLru9EV2Bo6ERZyw5A9MFxdyq1JOObMpJIe', -- Password@123
    'SUPERADMIN', NULL, 'System Administrator'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE role = 'SUPERADMIN' LIMIT 1
);
```

**Security Features**:
- `must_change_password = TRUE` forces password change on first login
- Only creates if NO SUPERADMIN exists
- Uses temporary password that must be changed

### Option B: Application Startup Hook (Most Secure)

**Pros**:
- Password generated on first run
- Not stored in version control
- Can use environment variables

**Cons**:
- More complex implementation
- Password must be communicated securely to first admin

**Implementation**: Create ApplicationInitializer

```java
@Component
public class ApplicationInitializer implements ApplicationRunner {

    @Value("${app.superadmin.email:admin@pastcare.com}")
    private String superadminEmail;

    @Value("${app.superadmin.password:#{null}}")
    private String superadminPassword;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Only create if no SUPERADMIN exists
        if (!userRepository.existsByRole(Role.SUPERADMIN)) {
            String password = superadminPassword != null
                ? superadminPassword
                : generateSecurePassword();

            User superadmin = new User();
            superadmin.setEmail(superadminEmail);
            superadmin.setName("Platform Administrator");
            superadmin.setPassword(passwordEncoder.encode(password));
            superadmin.setRole(Role.SUPERADMIN);
            superadmin.setActive(true);
            superadmin.setMustChangePassword(true);

            userRepository.save(superadmin);

            log.warn("=".repeat(60));
            log.warn("SUPERADMIN ACCOUNT CREATED");
            log.warn("Email: {}", superadminEmail);
            log.warn("Password: {}", password);
            log.warn("CHANGE THIS PASSWORD IMMEDIATELY!");
            log.warn("=".repeat(60));
        }
    }

    private String generateSecurePassword() {
        // Generate random 16-character password
        return RandomStringUtils.randomAlphanumeric(16);
    }
}
```

**Configuration** (application.properties):
```properties
# SUPERADMIN initialization
app.superadmin.email=admin@pastcare.com
app.superadmin.password=${SUPERADMIN_PASSWORD:}
```

**First Deployment**:
1. Start application without SUPERADMIN_PASSWORD env var
2. Application generates random password
3. Password printed in logs (copy it immediately)
4. Login and change password
5. Random password is discarded

### Option C: Manual Setup Script (Simplest)

**Pros**:
- Simple and transparent
- No code changes
- Password not in version control

**Cons**:
- Manual step in deployment
- Can be forgotten

**Implementation**: Create setup-superadmin.sh

```bash
#!/bin/bash
# setup-superadmin.sh - Create initial SUPERADMIN user

read -p "Enter SUPERADMIN email [admin@pastcare.com]: " EMAIL
EMAIL=${EMAIL:-admin@pastcare.com}

read -sp "Enter SUPERADMIN password: " PASSWORD
echo

# Generate BCrypt hash using Python
HASHED=$(python3 -c "
import bcrypt
password = '$PASSWORD'.encode('utf-8')
salt = bcrypt.gensalt()
hashed = bcrypt.hashpw(password, salt)
print(hashed.decode('utf-8'))
")

# Insert into database
mysql -u root -p past-care-spring << EOF
INSERT INTO users (
    created_at, updated_at, account_locked, email,
    failed_login_attempts, is_active, must_change_password,
    name, password, role, church_id, title
) VALUES (
    NOW(), NOW(), 0, '$EMAIL',
    0, 1, 1,
    'Platform Administrator',
    '$HASHED',
    'SUPERADMIN', NULL, 'System Administrator'
);
EOF

echo "SUPERADMIN user created successfully!"
echo "Email: $EMAIL"
echo "Please login and change your password immediately."
```

### My Recommendation: **Option A + Option B**

1. **Use Migration (Option A)** for development/testing
2. **Use Startup Hook (Option B)** for production with environment variable password
3. **Force password change** on first login
4. **Disable migration-created user** in production

---

## 5. 🎨 Church Registration Form Button Fixes

### Problem Description

**Issue 1**: "Add New Church" button when joining existing church
**Issue 2**: "Validate" button stretching unusually

### Let me find the registration form first

I need to locate the church registration/signup form component.

**Status**: PENDING - Need to find the registration form file

**Likely locations**:
- Landing page component
- Auth/registration component
- Signup/onboarding component

### What needs to be fixed:

#### Issue 1: Button Text
```html
<!-- WRONG: When joining existing church -->
<button>Add New Church</button>

<!-- CORRECT: Should be -->
<button>Join Church</button>
```

#### Issue 2: Button Stretching
Likely CSS issue - button width not constrained:

```css
/* WRONG */
.validate-button {
    width: 100%; /* Takes full width */
}

/* CORRECT */
.validate-button {
    width: auto;      /* Or specific width like 150px */
    padding: 0.75rem 2rem;
}
```

### I'll search for this form now...

---

## Summary of Actions Needed

### Immediate (Before Production)

1. ✅ **Create V81__consolidate_subscription_plans.sql**
   - Align with pricing model (STARTER free, STANDARD $9.99)
   - Remove unused plans (BASIC, PRO, ENTERPRISE)

2. ✅ **Create V82__create_initial_superadmin.sql**
   - Auto-create SUPERADMIN on first deployment
   - Force password change on first login

3. ✅ **Create consolidated initial schema**
   - V1__initial_production_schema.sql
   - Move legacy migrations to separate folder

4. 🔄 **Fix church registration form**
   - Find the form component
   - Fix "Add New Church" → "Join Church" when joining existing
   - Fix validate button width

5. 📋 **Document partnership code creation**
   - SQL script for creating codes
   - Instructions for support team

### Post-Launch (Nice to Have)

1. Build SUPERADMIN dashboard UI
2. Add partnership code management screen
3. Create church onboarding wizard
4. Add promotional code field to registration

---

## Next Steps

**Let me now**:
1. ✅ Create the consolidated subscription plans migration
2. ✅ Create the SUPERADMIN initialization migration
3. ✅ Find and fix the church registration form
4. ✅ Create the consolidated initial schema for production
5. ✅ Update todo list

**Proceed?** I'll start creating these files now.
