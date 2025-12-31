# Paystack Configuration Guide

## 1. What Is Being Sent to Paystack

### Currency Being Sent: **GHS (Ghana Cedis)**

Looking at the code in [BillingService.java](src/main/java/com/reuben/pastcare_spring/services/BillingService.java):

**Line 177** (Payment Record):
```java
.currency("GHS")  // Ghana Cedis
```

**Line 191** (API Request to Paystack):
```java
request.setCurrency("GHS");  // Ghana Cedis
```

### Complete Payment Request to Paystack

When a subscription payment is initialized, here's what gets sent to Paystack:

**Endpoint**: `POST https://api.paystack.co/transaction/initialize`

**Request Body**:
```json
{
  "email": "user@example.com",
  "amount": 15000,  // GHS 150.00 in pesewas (smallest unit)
  "currency": "GHS",
  "reference": "PCS-<uuid>",
  "callback_url": "http://localhost:4200/payment/verify",
  "channels": ["card", "mobile_money"],
  "metadata": {
    "memberId": null,
    "setupRecurring": true
  }
}
```

**Important Notes**:
1. **Amount is in pesewas** (1 GHS = 100 pesewas)
   - GHS 150 = 15,000 pesewas
   - GHS 450 = 45,000 pesewas
   - GHS 900 = 90,000 pesewas
   - GHS 1,800 = 180,000 pesewas

2. **Channels**: Both `card` and `mobile_money` are enabled
   - Allows payment via debit/credit cards
   - Allows payment via MTN, Vodafone, AirtelTigo mobile money

3. **Currency**: Hardcoded as **"GHS"**

---

## 2. Why Currency Error Might Still Occur

### Possible Reasons:

#### A. Test vs Live Mode Currency Configuration
Your Paystack account might have **different currencies enabled for test mode vs live mode**.

**Current Configuration** ([application.properties](src/main/resources/application.properties)):
```properties
paystack.test-mode=true
paystack.secret-key=sk_test_...  # TEST secret key
paystack.public-key=pk_test_...  # TEST public key
```

**Solution**: Log into Paystack Dashboard → Settings → **Test Mode Settings** → Enable GHS for test transactions

#### B. Account Country Mismatch
If your Paystack account was created in **Nigeria**, it defaults to NGN and may require business verification to enable GHS.

**To Check**:
1. Log in to https://dashboard.paystack.com
2. Go to **Settings** → **Business Profile**
3. Check "Country" - if it's Nigeria, you'll need to:
   - Submit business documentation
   - Request GHS currency activation
   - Wait for approval (usually 1-3 business days)

#### C. API Key Permissions
The test API keys might not have permission to use GHS.

**To Verify**:
1. Dashboard → **Settings** → **API Keys & Webhooks**
2. Check if test keys have **all permissions enabled**
3. Regenerate keys if needed

---

## 3. Callback URL vs Webhook URL

These are **two different things** with different purposes:

### Callback URL (Frontend Redirect)

**Purpose**: Where to **redirect the user** after they complete payment on Paystack's payment page.

**Current Configuration**:
```properties
paystack.callback-url=http://localhost:4200/portal/giving/verify
```

**How It Works**:
1. User clicks "Pay" button
2. Redirected to Paystack payment page
3. User completes payment
4. Paystack **redirects browser** to callback URL with `?reference=PCS-xxx&trxref=xxx` parameters
5. Frontend calls backend `/api/billing/verify/{reference}` to confirm payment

**For Subscription Payments**, the callback URL is:
```
http://localhost:4200/payment/verify
```

This is passed dynamically in the payment initialization request:
```java
request.setCallbackUrl(callbackUrl);  // From frontend
```

**What to Set in Paystack Dashboard**:
- **Test Mode**: `http://localhost:4200/payment/verify` (for local development)
- **Live Mode**: `https://yourdomain.com/payment/verify` (for production)

---

### Webhook URL (Server-to-Server Notification)

**Purpose**: Paystack sends a **server-to-server POST request** to notify your backend when payment events occur.

**Webhook Endpoint**: `/api/webhooks/paystack/events`

**Full URL**:
- **Local Development**: `https://your-ngrok-url.ngrok.io/api/webhooks/paystack/events`
- **Production**: `https://yourdomain.com/api/webhooks/paystack/events`

**How It Works**:
1. User completes payment
2. Paystack sends POST request to your webhook URL with event data
3. Your backend verifies the signature
4. Backend processes the event (updates subscription status, adds credits, etc.)

**Events Handled**:
- `charge.success` - Payment succeeded
- `charge.failed` - Payment failed

**What to Set in Paystack Dashboard**:
1. Go to **Settings** → **API Keys & Webhooks**
2. Scroll to **Webhook**
3. Click **Add Webhook URL**
4. Enter: `https://yourdomain.com/api/webhooks/paystack/events`
5. Copy the **Webhook Secret** provided
6. Add to your `application.properties`:
```properties
paystack.webhook-secret=wh_secret_xxxxxxxxxxxxx
```

---

## 4. Step-by-Step Paystack Dashboard Configuration

### Step 1: Enable GHS Currency

1. Log in to https://dashboard.paystack.com
2. Click **Settings** → **Preferences**
3. Find **Currencies** section
4. Enable **GHS (Ghana Cedis)**
5. If prompted, complete business verification:
   - Business registration documents
   - Proof of address
   - Identification documents

### Step 2: Configure Callback URL

1. Go to **Settings** → **API Keys & Webhooks**
2. Under **Live Callback URL**: `https://yourdomain.com/payment/verify`
3. Under **Test Callback URL**: `http://localhost:4200/payment/verify`
4. Click **Save**

### Step 3: Configure Webhook URL

**For Local Development** (requires ngrok or similar):

1. Install ngrok: `npm install -g ngrok` or download from https://ngrok.com
2. Start your Spring Boot app: `./mvnw spring-boot:run`
3. In another terminal: `ngrok http 8080`
4. Copy the HTTPS URL (e.g., `https://abc123.ngrok.io`)
5. In Paystack Dashboard:
   - **Settings** → **API Keys & Webhooks**
   - **Add Webhook URL**: `https://abc123.ngrok.io/api/webhooks/paystack/events`
   - Copy the **Webhook Secret** shown
6. Add to `application.properties`:
```properties
paystack.webhook-secret=wh_secret_xxxxxxxxxxxxx
```

**For Production**:

1. In Paystack Dashboard:
   - **Settings** → **API Keys & Webhooks**
   - **Add Webhook URL**: `https://yourdomain.com/api/webhooks/paystack/events`
   - Copy the **Webhook Secret**
2. Add to production environment variables:
```bash
export PAYSTACK_WEBHOOK_SECRET=wh_secret_xxxxxxxxxxxxx
```

### Step 4: Get Your API Keys

1. Go to **Settings** → **API Keys & Webhooks**
2. Copy your keys:
   - **Test Secret Key**: `sk_test_...`
   - **Test Public Key**: `pk_test_...`
   - **Live Secret Key**: `sk_live_...` (when ready for production)
   - **Live Public Key**: `pk_live_...`

3. Update `application.properties`:
```properties
# For Test Mode (Development)
paystack.secret-key=sk_test_xxxxxxxxxxxxx
paystack.public-key=pk_test_xxxxxxxxxxxxx
paystack.test-mode=true

# For Live Mode (Production)
# paystack.secret-key=sk_live_xxxxxxxxxxxxx
# paystack.public-key=pk_live_xxxxxxxxxxxxx
# paystack.test-mode=false
```

---

## 5. Testing Your Configuration

### Test 1: Verify Currency Support

```bash
curl -X POST https://api.paystack.co/transaction/initialize \
  -H "Authorization: Bearer sk_test_YOUR_SECRET_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "amount": 15000,
    "currency": "GHS"
  }'
```

**Expected Response** (if GHS is enabled):
```json
{
  "status": true,
  "message": "Authorization URL created",
  "data": {
    "authorization_url": "https://checkout.paystack.com/...",
    "access_code": "...",
    "reference": "..."
  }
}
```

**Error Response** (if GHS not enabled):
```json
{
  "status": false,
  "message": "Currency not supported by merchant",
  "type": "validation_error",
  "code": "unsupported_currency"
}
```

### Test 2: Check Webhook Endpoint

```bash
curl http://localhost:8080/api/webhooks/paystack/health
```

**Expected Response**:
```
Paystack Webhook Handler - Status: healthy, Secret Key Configured: true
```

### Test 3: End-to-End Payment Test

1. Start your backend: `./mvnw spring-boot:run`
2. Start your frontend: `cd past-care-spring-frontend && ng serve`
3. Navigate to: `http://localhost:4200/subscription/select`
4. Select a plan and click "Proceed to Payment"
5. Should redirect to Paystack payment page
6. Use Paystack test cards:
   - **Success**: `4084084084084081` (Visa)
   - **Insufficient Funds**: `5060666666666666666` (Verve)
   - **Invalid CVV**: `5060666666666666666` with wrong CVV

---

## 6. Common Issues and Solutions

### Issue 1: "Currency not supported by merchant"

**Cause**: GHS not enabled on your Paystack account

**Solution**:
1. Log into Paystack Dashboard
2. Go to Settings → Preferences → Currencies
3. Enable GHS
4. If prompted, complete business verification
5. Wait for approval (usually instant for test mode, 1-3 days for live mode)

### Issue 2: Webhook Not Receiving Events

**Cause**: Webhook URL not accessible or incorrect

**Solution**:
1. For local development, use ngrok: `ngrok http 8080`
2. Update webhook URL in Paystack to ngrok URL
3. Check webhook endpoint is accessible: `curl https://your-ngrok-url.ngrok.io/api/webhooks/paystack/events`
4. Check webhook secret is configured in `application.properties`

### Issue 3: Callback Redirect Fails

**Cause**: Frontend not running or wrong callback URL

**Solution**:
1. Ensure frontend is running on `http://localhost:4200`
2. Check callback URL in payment initialization matches frontend URL
3. Verify frontend has route `/payment/verify` configured

### Issue 4: Test Cards Not Working

**Cause**: Wrong test mode or wrong cards

**Solution**:
1. Ensure using `sk_test_...` secret key
2. Use only Paystack test cards:
   - **Success**: `4084084084084081`
   - **Expiry**: Any future date
   - **CVV**: `408`
   - **PIN**: `1234` (if prompted)
3. For mobile money testing in test mode:
   - Use any phone number
   - OTP will be shown on screen (no real SMS sent)

---

## 7. Production Deployment Checklist

Before going live:

- [ ] Business verification completed with Paystack
- [ ] GHS enabled for **Live Mode** (not just test mode)
- [ ] Live API keys generated and secured
- [ ] Webhook URL updated to production domain (HTTPS required)
- [ ] Webhook secret configured in production environment
- [ ] Callback URL updated to production domain
- [ ] Test transactions completed successfully in test mode
- [ ] SSL certificate installed on production domain
- [ ] Environment variables secured (not in code)
- [ ] Webhook endpoint accessible from internet (not blocked by firewall)

---

## 8. Environment Variables for Production

For security, use environment variables instead of hardcoding:

```bash
# Production Environment Variables
export PAYSTACK_SECRET_KEY=sk_live_xxxxxxxxxxxxx
export PAYSTACK_PUBLIC_KEY=pk_live_xxxxxxxxxxxxx
export PAYSTACK_WEBHOOK_SECRET=wh_secret_xxxxxxxxxxxxx
export PAYSTACK_CALLBACK_URL=https://yourdomain.com/payment/verify
export PAYSTACK_TEST_MODE=false
```

Update `application.properties`:
```properties
paystack.secret-key=${PAYSTACK_SECRET_KEY}
paystack.public-key=${PAYSTACK_PUBLIC_KEY}
paystack.webhook-secret=${PAYSTACK_WEBHOOK_SECRET}
paystack.callback-url=${PAYSTACK_CALLBACK_URL}
paystack.test-mode=${PAYSTACK_TEST_MODE:true}
```

---

## Summary

### What's Being Sent to Paystack:
- **Currency**: GHS (Ghana Cedis)
- **Amount**: In pesewas (GHS 150 = 15,000 pesewas)
- **Channels**: Card and Mobile Money
- **Reference**: Unique UUID-based reference

### Callback URL (User Redirect):
- **Purpose**: Redirect user after payment
- **URL**: `http://localhost:4200/payment/verify` (dev) or `https://yourdomain.com/payment/verify` (prod)
- **Set In**: Frontend payment initialization request
- **Also Configure In**: Paystack Dashboard → Settings → API Keys & Webhooks

### Webhook URL (Server Notification):
- **Purpose**: Notify backend of payment events
- **URL**: `https://yourdomain.com/api/webhooks/paystack/events`
- **Set In**: Paystack Dashboard → Settings → API Keys & Webhooks → Add Webhook URL
- **Secret**: Copy from dashboard and add to `application.properties`

### Next Steps:
1. Verify GHS is enabled in your Paystack **test mode** settings
2. If error persists, try using Paystack test card directly to isolate the issue
3. Check Paystack Dashboard → Developers → Logs to see the exact API error
