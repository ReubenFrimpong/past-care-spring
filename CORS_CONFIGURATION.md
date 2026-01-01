# CORS Configuration - Production Fix

**Date**: January 1, 2026
**Issue**: Hardcoded CORS origins in `CorsConfig.java` only allowed `localhost`, blocking production requests
**Status**: ✅ Fixed

---

## Problem

The original CORS configuration in `CorsConfig.java` had hardcoded origins:

```java
.allowedOriginPatterns(
    "http://localhost:*",
    "https://localhost:*"
)
```

This configuration would **block all requests from your production domain**, causing CORS errors when the frontend tries to access the backend API in production.

---

## Solution

### 1. Extracted CORS Origins to Environment Variable

**File**: `src/main/java/com/reuben/pastcare_spring/security/CorsConfig.java`

**Changes**:
```java
@Value("${cors.allowed-origins:http://localhost:*,https://localhost:*}")
private String allowedOrigins;

@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            // Split the comma-separated allowed origins from environment variable
            String[] origins = allowedOrigins.split(",");

            registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
        }
    };
}
```

### 2. Added CORS Configuration to Properties Files

**Development** (`application.properties`):
```properties
# CORS Configuration (Development)
cors.allowed-origins=http://localhost:*,https://localhost:*
```

**Production** (`application-prod.properties`):
```properties
# CORS Configuration (Production)
# Allow requests from your production domain
# Format: comma-separated list of allowed origins
# Example: https://yourdomain.com,https://www.yourdomain.com,https://app.yourdomain.com
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com,https://www.yourdomain.com}
```

### 3. Updated Environment Template

**File**: `setup-production-hooks.sh`

Added to `.env.template`:
```bash
# CORS Configuration (Production)
# CRITICAL: Add all domains that should be allowed to access your API
# Format: comma-separated list (no spaces)
# Example: https://pastcare.app,https://www.pastcare.app,https://app.pastcare.app
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

---

## Production Deployment Checklist

### Before Deployment

1. **Update `.env` file on production server** with your actual domains:
   ```bash
   # On production server: /var/www/pastcare.app/backend/.env (or /opt/pastcare/backend/.env)
   CORS_ALLOWED_ORIGINS=https://pastcare.app,https://www.pastcare.app
   ```

2. **Include all subdomains** that will access the API:
   - Main domain: `https://pastcare.app`
   - WWW subdomain: `https://www.pastcare.app`
   - API subdomain (if separate): `https://api.pastcare.app`
   - Admin subdomain: `https://admin.pastcare.app`

3. **No spaces** in the comma-separated list:
   ```bash
   # ✅ CORRECT
   CORS_ALLOWED_ORIGINS=https://example.com,https://www.example.com

   # ❌ WRONG (spaces will break it)
   CORS_ALLOWED_ORIGINS=https://example.com, https://www.example.com
   ```

### Testing CORS in Production

1. **Deploy backend** with the new CORS configuration
2. **Test from browser console** on your production frontend:
   ```javascript
   fetch('https://your-backend-domain.com/api/health')
     .then(response => response.json())
     .then(data => console.log('CORS working:', data))
     .catch(error => console.error('CORS error:', error));
   ```

3. **Check browser Network tab** for CORS errors:
   - Look for `Access-Control-Allow-Origin` header in response
   - Should match your frontend domain

4. **Expected CORS headers** in response:
   ```
   Access-Control-Allow-Origin: https://pastcare.app
   Access-Control-Allow-Credentials: true
   Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
   Access-Control-Allow-Headers: *
   Access-Control-Expose-Headers: Authorization
   ```

---

## Troubleshooting

### Issue: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Cause**: Domain not in `CORS_ALLOWED_ORIGINS`

**Fix**:
```bash
# Add your domain to .env
CORS_ALLOWED_ORIGINS=https://your-actual-domain.com,https://www.your-actual-domain.com
```

### Issue: "CORS policy: The value of the 'Access-Control-Allow-Origin' header must not be the wildcard '*'"

**Cause**: Using `allowCredentials(true)` requires specific origins, not wildcards

**Fix**: Already handled - we use specific origin patterns, not wildcards

### Issue: OPTIONS preflight request fails

**Cause**: OPTIONS method not allowed or CORS headers missing

**Fix**: Already handled - OPTIONS is in `allowedMethods()` and handled by Spring Security

---

## Security Considerations

### 1. Never Use Wildcard in Production

❌ **DO NOT**:
```java
.allowedOrigins("*")  // Allows ANY domain - SECURITY RISK
```

✅ **DO**:
```java
.allowedOriginPatterns(origins)  // Specific domains only
```

### 2. Validate HTTPS in Production

Production domains should **always use HTTPS**:
```bash
# ✅ CORRECT
CORS_ALLOWED_ORIGINS=https://pastcare.app,https://www.pastcare.app

# ❌ WRONG (insecure HTTP)
CORS_ALLOWED_ORIGINS=http://pastcare.app
```

### 3. Minimize Allowed Origins

Only add domains you actually use:
- ✅ Add: `https://pastcare.app` (your actual frontend)
- ❌ Don't add: `https://random-test-domain.com` (unused domain)

---

## How It Works

### 1. Development Environment

When running locally (`mvnw spring-boot:run`), Spring loads `application.properties`:
```properties
cors.allowed-origins=http://localhost:*,https://localhost:*
```

This allows:
- `http://localhost:4200` (Angular dev server)
- `http://localhost:8080` (Backend)
- Any port on localhost

### 2. Production Environment

When running with `-Dspring.profiles.active=prod`:
```properties
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com,https://www.yourdomain.com}
```

This reads from environment variable `CORS_ALLOWED_ORIGINS`, falling back to default if not set.

### 3. Request Flow

1. **Browser makes request** from `https://pastcare.app` to `https://api.pastcare.app/api/members`
2. **Browser sends OPTIONS preflight** (if needed)
3. **Spring Security checks origin** against allowed patterns
4. **CorsConfig adds headers**:
   - `Access-Control-Allow-Origin: https://pastcare.app`
   - `Access-Control-Allow-Credentials: true`
   - `Access-Control-Allow-Methods: GET, POST, ...`
5. **Browser allows request** if origin matches

---

## Files Modified

1. ✅ `src/main/java/com/reuben/pastcare_spring/security/CorsConfig.java`
   - Added `@Value` injection for `cors.allowed-origins`
   - Dynamic origin pattern splitting

2. ✅ `src/main/resources/application.properties`
   - Added `cors.allowed-origins` for development

3. ✅ `src/main/resources/application-prod.properties`
   - Added `cors.allowed-origins` for production with environment variable

4. ✅ `setup-production-hooks.sh`
   - Added `CORS_ALLOWED_ORIGINS` to `.env.template`

---

## Related Configuration

### Spring Security

CORS configuration works alongside Spring Security. The security config already permits CORS preflight requests:

```java
.cors(cors -> cors.configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    // ... handled by CorsConfig
}))
```

### Nginx (if used as reverse proxy)

If Nginx is used as reverse proxy, ensure it **doesn't add its own CORS headers** (conflicts with Spring):

```nginx
# DON'T add these in Nginx if Spring handles CORS:
# add_header 'Access-Control-Allow-Origin' '*';
```

Let Spring handle CORS via `CorsConfig.java`.

---

## Quick Reference

### Add a new domain:

1. SSH to production server
2. Edit `.env`:
   ```bash
   nano /opt/pastcare/backend/.env
   ```
3. Add domain to `CORS_ALLOWED_ORIGINS`:
   ```bash
   CORS_ALLOWED_ORIGINS=https://pastcare.app,https://www.pastcare.app,https://new-domain.com
   ```
4. Restart backend:
   ```bash
   systemctl restart pastcare-backend
   ```

### Test CORS is working:

```bash
# From command line
curl -H "Origin: https://pastcare.app" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     https://your-backend.com/api/health -v

# Look for:
# < Access-Control-Allow-Origin: https://pastcare.app
```

---

## Conclusion

✅ **CORS is now configurable via environment variables**
✅ **No hardcoded domains in source code**
✅ **Development and production use separate configurations**
✅ **Security maintained** (no wildcards, credentials enabled only for specific origins)

**Next Step**: Update production `.env` with actual domain before deploying!
