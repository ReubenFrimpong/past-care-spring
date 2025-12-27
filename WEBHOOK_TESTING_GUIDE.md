# SMS Module Webhook Testing Guide

This guide helps you test the SMS webhook endpoints locally and in production.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Testing Setup](#local-testing-setup)
3. [Testing Each Webhook](#testing-each-webhook)
4. [Production Setup](#production-setup)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Tools
- **ngrok** (for local webhook testing): https://ngrok.com/download
- **curl** or **Postman** (for manual testing)
- **SMS Gateway Accounts**:
  - Africa's Talking: https://africastalking.com
  - Twilio: https://www.twilio.com
  - Paystack: https://paystack.com

### Environment Variables
Copy `.env.sms.example` to `.env` and fill in your credentials:
```bash
cp .env.sms.example .env
```

---

## Local Testing Setup

### 1. Start Your Spring Boot Application
```bash
./mvnw spring-boot:run
```

### 2. Start ngrok Tunnel
```bash
ngrok http 8080
```

You'll get a public URL like: `https://abc123.ngrok.io`

### 3. Configure Webhook URLs

Use your ngrok URL for all webhook endpoints:

**Africa's Talking:**
- Dashboard: https://account.africastalking.com/apps/sandbox/settings/key
- Delivery Reports URL: `https://abc123.ngrok.io/api/webhooks/sms/africas-talking/delivery`

**Twilio:**
- Console: https://console.twilio.com/
- Status Callback URL: `https://abc123.ngrok.io/api/webhooks/sms/twilio/delivery`

**Paystack:**
- Dashboard: https://dashboard.paystack.com/#/settings/developer
- Webhook URL: `https://abc123.ngrok.io/api/webhooks/paystack/events`

---

## Testing Each Webhook

### 1. Test Health Endpoints

```bash
# SMS Webhooks Health
curl http://localhost:8080/api/webhooks/sms/health

# Paystack Webhook Health
curl http://localhost:8080/api/webhooks/paystack/health
```

Expected responses:
```json
{
  "status": "healthy",
  "service": "SMS Webhook Handler",
  "africasTalkingConfigured": "true",
  "twilioConfigured": "true"
}
```

### 2. Test Africa's Talking Webhook

#### Manual Test (curl)
```bash
curl -X POST http://localhost:8080/api/webhooks/sms/africas-talking/delivery \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ATXid_test123",
    "status": "Success",
    "phoneNumber": "+254712345678",
    "networkCode": "63902",
    "cost": "KES 0.80",
    "retryCount": 0
  }'
```

#### Real Test
1. Send an SMS via your application
2. Note the `gatewayMessageId` in the database
3. Africa's Talking will automatically call your webhook when delivery status changes
4. Check your logs: `grep "Africa's Talking" logs/spring.log`

### 3. Test Twilio Webhook

#### Manual Test (curl)
```bash
curl -X POST http://localhost:8080/api/webhooks/sms/twilio/delivery \
  -d "MessageSid=SM12345test" \
  -d "MessageStatus=delivered" \
  -d "To=+1234567890" \
  -d "From=+19876543210" \
  -d "ErrorCode=" \
  -d "ErrorMessage="
```

#### Real Test
1. Send an SMS via your application using Twilio
2. Note the `gatewayMessageId` in database
3. Twilio will call your webhook on status changes
4. Check logs: `grep "Twilio" logs/spring.log`

### 4. Test Paystack Webhook

#### Manual Test (curl)
```bash
curl -X POST http://localhost:8080/api/webhooks/paystack/events \
  -H "Content-Type: application/json" \
  -H "X-Paystack-Signature: test_signature" \
  -d '{
    "event": "charge.success",
    "data": {
      "reference": "TEST-REF-123",
      "amount": 1000000,
      "currency": "NGN",
      "status": "success",
      "customer": {
        "email": "test@example.com"
      },
      "metadata": {
        "church_id": "1",
        "user_id": "1",
        "credit_amount": "100.00"
      }
    }
  }'
```

#### Real Test
1. Initiate a credit purchase from frontend
2. Complete payment on Paystack
3. Paystack will send webhook to your URL
4. Check if credits were added: `SELECT * FROM church_sms_credits WHERE church_id = 1;`
5. Check transaction: `SELECT * FROM sms_transactions ORDER BY created_at DESC LIMIT 1;`

---

## Testing Scheduled Messages

### 1. Create a Scheduled SMS
```bash
curl -X POST http://localhost:8080/api/sms/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "recipientPhone": "+254712345678",
    "message": "This is a test scheduled message",
    "scheduledTime": "2025-12-27T15:30:00"
  }'
```

### 2. Verify Scheduled Status
```sql
SELECT id, message, scheduled_time, status 
FROM sms_messages 
WHERE status = 'SCHEDULED' 
ORDER BY scheduled_time;
```

### 3. Wait for Scheduled Job
The `SmsScheduledJob` runs every minute. Check logs:
```bash
grep "Checking for scheduled SMS" logs/spring.log
grep "Scheduled SMS processing complete" logs/spring.log
```

### 4. Verify Message Sent
```sql
SELECT id, message, status, sent_at 
FROM sms_messages 
WHERE id = YOUR_MESSAGE_ID;
```

---

## Testing SMS Retry Mechanism

### 1. Simulate Failed SMS
Manually set an SMS to FAILED status:
```sql
UPDATE sms_messages 
SET status = 'FAILED', retry_count = 0, last_retry_at = NULL 
WHERE id = YOUR_MESSAGE_ID;
```

### 2. Wait for Retry Job
The `SmsRetryJob` runs every 15 minutes. Or trigger manually in code.

### 3. Check Retry Progress
```sql
SELECT id, status, retry_count, last_retry_at, gateway_response 
FROM sms_messages 
WHERE id = YOUR_MESSAGE_ID;
```

### 4. Monitor Logs
```bash
grep "Starting SMS retry job" logs/spring.log
grep "SMS retry job complete" logs/spring.log
grep "Retry batch complete" logs/spring.log
```

---

## Production Setup

### 1. Use Production URLs
Replace ngrok URLs with your actual domain:
- Africa's Talking: `https://yourchurch.com/api/webhooks/sms/africas-talking/delivery`
- Twilio: `https://yourchurch.com/api/webhooks/sms/twilio/delivery`
- Paystack: `https://yourchurch.com/api/webhooks/paystack/events`

### 2. Enable SSL/TLS
All webhooks require HTTPS in production. Use:
- Let's Encrypt (free)
- Cloud provider SSL (AWS ACM, Google Cloud SSL, etc.)

### 3. Set Environment Variables
```bash
# Using environment variables (recommended)
export AFRICAS_TALKING_API_KEY=your_actual_key
export TWILIO_AUTH_TOKEN=your_actual_token
export PAYSTACK_SECRET_KEY=your_actual_secret

# Or add to application.properties
africas-talking.api-key=${AFRICAS_TALKING_API_KEY}
twilio.auth-token=${TWILIO_AUTH_TOKEN}
paystack.secret-key=${PAYSTACK_SECRET_KEY}
```

### 4. Enable Signature Verification
Ensure these are set in production:
```properties
webhooks.skip-signature-verification=false
```

### 5. Monitor Webhooks
Set up logging and monitoring:
```bash
# View webhook logs
tail -f logs/spring.log | grep "webhook"

# Count webhook calls
grep "Received.*webhook" logs/spring.log | wc -l

# Check failures
grep "Error processing.*webhook" logs/spring.log
```

---

## Troubleshooting

### Webhook Not Being Called

**Check 1: URL Correctness**
```bash
# Test if endpoint is reachable
curl https://your-domain.com/api/webhooks/sms/health
```

**Check 2: Firewall/Security Groups**
- Ensure port 8080 (or 443 for HTTPS) is open
- Check cloud provider security groups
- Verify firewall rules allow incoming POST requests

**Check 3: Gateway Configuration**
- Double-check webhook URL in gateway dashboard
- Ensure URL has no trailing slashes
- Verify URL protocol (http vs https)

### Signature Verification Failing

**Check 1: Secret Key**
```bash
# Verify secret key is loaded
curl http://localhost:8080/api/webhooks/paystack/health
```

**Check 2: Payload Format**
- Ensure Content-Type is correct
- Verify JSON structure matches expected format

**Check 3: Development Mode**
Temporarily skip verification for testing:
```properties
paystack.secret-key=
```

### Credits Not Being Added

**Check 1: Metadata Present**
```bash
# Check Paystack webhook logs
grep "metadata" logs/spring.log
```

**Check 2: Church ID Valid**
```sql
SELECT * FROM churches WHERE id = YOUR_CHURCH_ID;
```

**Check 3: Transaction Created**
```sql
SELECT * FROM sms_transactions 
WHERE reference_id LIKE 'TEST-REF-%' 
ORDER BY created_at DESC;
```

### Scheduled Messages Not Sending

**Check 1: Job Running**
```bash
grep "Checking for scheduled SMS" logs/spring.log | tail -5
```

**Check 2: Message Status**
```sql
SELECT id, status, scheduled_time, sent_at 
FROM sms_messages 
WHERE status = 'SCHEDULED';
```

**Check 3: Time Zone**
Ensure `scheduled_time` is in correct timezone

### Retry Not Working

**Check 1: Job Configuration**
```properties
sms.retry.enabled=true
sms.retry.max-attempts=3
sms.retry.interval-minutes=15
```

**Check 2: Failed Messages**
```sql
SELECT id, retry_count, last_retry_at, status 
FROM sms_messages 
WHERE status = 'FAILED' AND retry_count < 3;
```

**Check 3: Last Retry Time**
```sql
-- Should only retry after 15 minutes
SELECT id, last_retry_at, 
       TIMESTAMPDIFF(MINUTE, last_retry_at, NOW()) as minutes_since_retry 
FROM sms_messages 
WHERE status = 'FAILED';
```

---

## Webhook Payload Examples

### Africa's Talking Success
```json
{
  "id": "ATXid_abc123def456",
  "status": "Success",
  "phoneNumber": "+254712345678",
  "networkCode": "63902",
  "cost": "KES 0.80",
  "retryCount": 0
}
```

### Africa's Talking Failure
```json
{
  "id": "ATXid_abc123def456",
  "status": "Failed",
  "phoneNumber": "+254712345678",
  "networkCode": "63902",
  "failureReason": "InsufficientBalance",
  "retryCount": 0
}
```

### Twilio Delivered
```
MessageSid=SM1234567890abcdef
MessageStatus=delivered
To=+1234567890
From=+19876543210
SmsSid=SM1234567890abcdef
SmsStatus=delivered
```

### Twilio Failed
```
MessageSid=SM1234567890abcdef
MessageStatus=failed
To=+1234567890
From=+19876543210
ErrorCode=30008
ErrorMessage=Unknown destination handset
```

### Paystack Success
```json
{
  "event": "charge.success",
  "data": {
    "id": 123456789,
    "domain": "live",
    "status": "success",
    "reference": "CREDIT-PURCHASE-1-5-1703678400",
    "amount": 1000000,
    "message": null,
    "gateway_response": "Successful",
    "paid_at": "2025-12-27T14:30:00.000Z",
    "created_at": "2025-12-27T14:28:00.000Z",
    "channel": "card",
    "currency": "NGN",
    "ip_address": "102.89.32.123",
    "metadata": {
      "church_id": "1",
      "user_id": "5",
      "credit_amount": "100.00",
      "custom_fields": []
    },
    "customer": {
      "id": 987654,
      "email": "pastor@church.com"
    }
  }
}
```

---

## Monitoring Commands

### Real-time Log Monitoring
```bash
# All webhook activity
tail -f logs/spring.log | grep -E "webhook|Paystack|Twilio|Africa"

# Only successful webhooks
tail -f logs/spring.log | grep "successfully"

# Only errors
tail -f logs/spring.log | grep -E "ERROR|Error|error"
```

### Database Monitoring
```sql
-- Check recent SMS messages
SELECT id, status, retry_count, sent_at, delivery_time, gateway_response 
FROM sms_messages 
ORDER BY created_at DESC 
LIMIT 20;

-- Check credits added via webhooks today
SELECT * FROM sms_transactions 
WHERE type = 'PURCHASE' 
AND DATE(created_at) = CURDATE() 
ORDER BY created_at DESC;

-- Check failed messages needing retry
SELECT id, recipient_phone, status, retry_count, last_retry_at 
FROM sms_messages 
WHERE status = 'FAILED' AND retry_count < 3;
```

---

**Document Version:** 1.0  
**Last Updated:** December 27, 2025  
**Status:** Complete
