# Communications Module - Next Implementation Steps

**Current Status**: Phase 1 Backend Complete ✅
**Next Priority**: Frontend Implementation + Infrastructure Setup

---

## 🎯 Immediate Next Steps

### 1. Backend Testing & Setup (1-2 hours)

#### A. Run Application & Verify Migrations
```bash
./mvnw spring-boot:run
```

**Verify Database**:
```sql
-- Check that migrations ran successfully
SELECT * FROM flyway_schema_history WHERE version >= 34;

-- Verify tables created
SHOW TABLES LIKE 'sms_%';

-- Check default SMS rates loaded
SELECT * FROM sms_rates;
```

#### B. Configure SMS Gateway Credentials

**Option 1: Africa's Talking Sandbox (For Testing)**
1. Sign up at https://account.africastalking.com
2. Get sandbox credentials
3. Set environment variables:
```bash
export AFRICASTALKING_API_KEY="your_sandbox_api_key"
export AFRICASTALKING_USERNAME="sandbox"
export AFRICASTALKING_SENDER_ID=""  # Optional
```

**Option 2: Production Setup**
```bash
# Africa's Talking (Production)
export AFRICASTALKING_API_KEY="your_production_api_key"
export AFRICASTALKING_USERNAME="your_username"
export AFRICASTALKING_SENDER_ID="YourChurch"

# Twilio (For International SMS)
export TWILIO_ACCOUNT_SID="your_account_sid"
export TWILIO_AUTH_TOKEN="your_auth_token"
export TWILIO_FROM_NUMBER="+1234567890"
```

#### C. Test SMS Endpoints with Postman/Curl

**1. Create User Wallet (Auto-created on first use)**
```bash
GET http://localhost:8080/api/sms/credits/balance
Authorization: Bearer <jwt_token>
```

**2. Calculate SMS Cost**
```bash
POST http://localhost:8080/api/sms/credits/calculate-cost
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "phoneNumber": "+233240000000",
  "message": "Hello from PastCare!"
}
```

**3. Purchase Credits (Manual - requires payment integration)**
```bash
POST http://localhost:8080/api/sms/credits/purchase
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "amount": 10.00,
  "paymentReference": "PAY-TEST-12345"
}
```

**4. Send SMS**
```bash
POST http://localhost:8080/api/sms/send
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "recipientPhone": "+233240000000",
  "recipientName": "John Doe",
  "message": "Test message from PastCare!",
  "memberId": null,
  "scheduledTime": null
}
```

**5. Get SMS History**
```bash
GET http://localhost:8080/api/sms/history?page=0&size=10
Authorization: Bearer <jwt_token>
```

**6. Get SMS Statistics**
```bash
GET http://localhost:8080/api/sms/stats
Authorization: Bearer <jwt_token>
```

---

### 2. Frontend Implementation (3-5 days)

#### A. Create Angular Services

**File**: `src/app/services/sms.service.ts`
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SmsMessage {
  id: number;
  senderId: number;
  senderName: string;
  recipientPhone: string;
  recipientName?: string;
  message: string;
  messageCount: number;
  cost: number;
  status: string;
  scheduledTime?: string;
  sentAt?: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class SmsService {
  private apiUrl = '/api/sms';

  constructor(private http: HttpClient) {}

  sendSms(request: any): Observable<SmsMessage> {
    return this.http.post<SmsMessage>(`${this.apiUrl}/send`, request);
  }

  getHistory(page: number, size: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/history?page=${page}&size=${size}`);
  }

  getStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`);
  }

  // Add more methods...
}
```

**File**: `src/app/services/sms-credit.service.ts`
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SmsCreditService {
  private apiUrl = '/api/sms/credits';

  constructor(private http: HttpClient) {}

  getBalance(): Observable<any> {
    return this.http.get(`${this.apiUrl}/balance`);
  }

  calculateCost(request: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/calculate-cost`, request);
  }

  purchaseCredits(request: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/purchase`, request);
  }

  getTransactions(page: number, size: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/transactions?page=${page}&size=${size}`);
  }
}
```

#### B. Create SMS Dashboard Component

**File**: `src/app/sms-page/sms-page.ts` (Standalone Component)

**Features to Implement**:
1. **Stats Cards**: Balance, Total Sent, Delivered, Failed
2. **Send SMS Form**:
   - Recipient selection (manual entry or member dropdown)
   - Message textarea with character counter
   - Cost calculator (updates as you type)
   - Schedule option (date/time picker)
   - Send button (disabled if insufficient credits)
3. **SMS History Table**:
   - DataTable with filtering
   - Status badges (sent, delivered, failed)
   - Cost column
   - Timestamp
   - Actions (cancel if scheduled)
4. **Quick Actions**:
   - "Send to Group" button
   - "Purchase Credits" button
   - "Manage Templates" button

#### C. Create Credit Wallet Component

**File**: `src/app/credit-wallet-page/credit-wallet-page.ts`

**Features**:
1. **Balance Card**: Current balance, total purchased, total used
2. **Purchase Credits Section**:
   - Amount input (with suggestions: 10, 25, 50, 100 GHS)
   - Payment button (integrate with Paystack)
   - Package suggestions (e.g., "50 GHS = ~2,500 local SMS")
3. **Transaction History Table**:
   - Type (purchase, deduction, refund)
   - Amount
   - Balance before/after
   - Description
   - Date
4. **Cost Calculator Widget**:
   - Phone number input
   - Message input
   - Shows: message count, cost, destination country

#### D. Create Template Management Component

**File**: `src/app/sms-templates-page/sms-templates-page.ts`

**Features**:
1. **Templates Grid/List**: Show all templates with categories
2. **Create/Edit Dialog**:
   - Name
   - Description
   - Template content (with variable placeholders)
   - Category dropdown
   - Active toggle
3. **Quick Actions**:
   - Use template (navigate to SMS page with pre-filled message)
   - Duplicate template
   - Delete template

#### E. Update Navigation

**File**: `src/app/app.component.html` (or sidebar component)

Add navigation links:
```html
<a routerLink="/portal/sms" routerLinkActive="active">
  <i class="pi pi-comment"></i>
  <span>SMS</span>
</a>
<a routerLink="/portal/sms/wallet" routerLinkActive="active">
  <i class="pi pi-wallet"></i>
  <span>SMS Credits</span>
</a>
<a routerLink="/portal/sms/templates" routerLinkActive="active">
  <i class="pi pi-file"></i>
  <span>SMS Templates</span>
</a>
```

#### F. Add Routes

**File**: `src/app/app.routes.ts`

```typescript
{
  path: 'portal/sms',
  component: SmsPageComponent,
  canActivate: [authGuard]
},
{
  path: 'portal/sms/wallet',
  component: CreditWalletPageComponent,
  canActivate: [authGuard]
},
{
  path: 'portal/sms/templates',
  component: SmsTemplatesPageComponent,
  canActivate: [authGuard]
}
```

---

### 3. Infrastructure Setup (2-3 hours)

#### A. Scheduled SMS Processor (Cron Job)

**File**: `src/main/java/com/reuben/pastcare_spring/schedulers/SmsScheduler.java`

```java
@Component
public class SmsScheduler {

    private final SmsService smsService;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void processScheduledMessages() {
        smsService.processScheduledMessages();
    }
}
```

**Enable Scheduling in Main Application**:
```java
@SpringBootApplication
@EnableScheduling
public class PastcareSpringApplication {
    // ...
}
```

#### B. Delivery Status Webhook Handler

**File**: `src/main/java/com/reuben/pastcare_spring/controllers/SmsWebhookController.java`

```java
@RestController
@RequestMapping("/api/webhooks/sms")
public class SmsWebhookController {

    @PostMapping("/africas-talking/delivery")
    public ResponseEntity<Void> handleAfricasTalkingDelivery(@RequestBody Map<String, Object> payload) {
        // Update SMS delivery status
        return ResponseEntity.ok().build();
    }

    @PostMapping("/twilio/delivery")
    public ResponseEntity<Void> handleTwilioDelivery(@RequestBody Map<String, Object> payload) {
        // Update SMS delivery status
        return ResponseEntity.ok().build();
    }
}
```

#### C. Payment Webhook Integration

**Connect to existing Paystack webhook** to handle SMS credit purchases:

**File**: Update `PaystackWebhookController.java`

```java
// In existing webhook handler
if ("charge.success".equals(event)) {
    String reference = (String) data.get("reference");

    // Check if this is an SMS credit purchase
    if (reference.startsWith("SMS-CREDIT-")) {
        // Extract user and amount
        // Call SmsCreditService.purchaseCredits()
    }
}
```

---

## 📊 Progress Tracking

### Backend (Complete) ✅
- [x] Entities (9 files)
- [x] Migrations (5 files)
- [x] Repositories (5 files)
- [x] Services (7 files)
- [x] DTOs (11 files)
- [x] Controllers (3 files)
- [x] Configuration (application.properties)
- [x] Compilation successful

### Frontend (Pending) ⏳
- [ ] SmsService
- [ ] SmsCreditService
- [ ] SmsTemplateService
- [ ] SmsPageComponent
- [ ] CreditWalletPageComponent
- [ ] SmsTemplatesPageComponent
- [ ] Navigation links
- [ ] Routes configuration
- [ ] TypeScript interfaces
- [ ] E2E tests

### Infrastructure (Pending) ⏳
- [ ] Scheduled SMS processor
- [ ] Delivery status webhook
- [ ] Payment webhook integration
- [ ] Rate limiting
- [ ] Webhook signature verification

### Testing (Pending) ⏳
- [ ] Backend API tests (Postman/Curl)
- [ ] Frontend component tests
- [ ] E2E tests (Playwright)
- [ ] Integration tests (payment + SMS)
- [ ] Load testing (bulk SMS)

---

## 🚀 Recommended Implementation Order

1. **Day 1**: Backend testing + SMS gateway setup + API testing
2. **Day 2**: Create Angular services + interfaces + basic SMS page
3. **Day 3**: Implement send SMS form + history table + stats
4. **Day 4**: Credit wallet page + transaction history
5. **Day 5**: Template management + scheduled SMS processor
6. **Day 6**: Member integration (send SMS from member profile)
7. **Day 7**: E2E tests + payment webhook integration

---

## 💡 Tips for Frontend Implementation

1. **Reuse Existing Patterns**: Look at MembersPageComponent for reference (signals, reactive forms, PrimeNG components)

2. **Cost Calculator**: Use reactive forms with `valueChanges` to update cost in real-time:
```typescript
messageControl.valueChanges.subscribe(message => {
  this.calculateCost(this.phoneControl.value, message);
});
```

3. **Balance Warning**: Show warning banner when balance is low:
```typescript
computed(() => {
  const balance = this.balance();
  return balance < 5 ? 'warning' : 'normal';
})
```

4. **Character Counter**: Show character count and message parts:
```typescript
const maxLength = hasUnicode ? 70 : 160;
const messageCount = Math.ceil(message.length / maxLength);
```

5. **Member Selector**: Reuse existing member dropdown component with search

6. **Paystack Integration**: Reuse existing Paystack service from Giving module

---

## 📝 Configuration Checklist

Before going to production:

- [ ] Update SMS gateway credentials (production keys)
- [ ] Configure sender ID (church name)
- [ ] Set up payment webhook URL (for credit purchases)
- [ ] Set up delivery webhook URLs (SMS status updates)
- [ ] Update SMS rates based on actual gateway pricing
- [ ] Configure rate limiting (max SMS per user per hour)
- [ ] Set up monitoring/alerts for failed SMS
- [ ] Test sandbox mode thoroughly before production
- [ ] Add opt-out handling for members
- [ ] Configure backup gateway (if primary fails)

---

## 🎓 Learning Resources

**Africa's Talking**:
- API Docs: https://developers.africastalking.com/docs/sms/overview
- Sandbox: https://account.africastalking.com

**Twilio**:
- API Docs: https://www.twilio.com/docs/sms
- Trial Account: https://www.twilio.com/try-twilio

**Paystack**:
- Webhook Docs: https://paystack.com/docs/payments/webhooks

---

**Ready to continue with frontend implementation!** 🚀
