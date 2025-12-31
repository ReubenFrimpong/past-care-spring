# SPA Routing Development Mode Fix

**Date**: 2025-12-30
**Issue**: Platform admin routes returning "No static resource" errors in development mode

---

## Problem

When accessing platform admin routes like `/platform/stats` or `/platform/churches/all`, the backend was throwing errors:

```
No static resource platform/stats.
No static resource platform/churches/all.
```

**Root Cause**: The SpaRoutingConfig was configured to return early in development mode (when `index.html` doesn't exist), but Spring's default resource handler was still trying to resolve these Angular routes as static files, causing the errors.

---

## Error Details

```
org.springframework.web.servlet.resource.NoResourceFoundException: No static resource platform/churches/all.
    at org.springframework.web.servlet.resource.ResourceHttpRequestHandler.handleRequest(ResourceHttpRequestHandler.java:585)
```

**What was happening**:
1. Frontend Angular app runs on `http://localhost:4200` in development
2. Backend runs on `http://localhost:8080`
3. Browser navigates to `/platform/stats` on the backend (wrong port)
4. Backend's SpaRoutingConfig detects no `index.html` and returns early
5. Spring's default handler tries to find a static file at `platform/stats`
6. No file exists → "No static resource" error

---

## Solution

Updated [SpaRoutingConfig.java](src/main/java/com/reuben/pastcare_spring/config/SpaRoutingConfig.java) to explicitly configure limited resource handling in development mode:

### Before (Problematic)
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if (!INDEX_HTML_EXISTS) {
        // Return early - but Spring still tries to resolve routes!
        return;
    }

    // Production config...
}
```

**Problem**: Returning early left Spring's default behavior active, which tried to resolve all routes as static files.

### After (Fixed)
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Development mode: Only serve actual static files
    if (!INDEX_HTML_EXISTS) {
        registry
            .addResourceHandler("/assets/**", "/favicon.ico", "/*.js", "/*.css", "/*.map")
            .addResourceLocations("classpath:/static/", "classpath:/public/");
        return;
    }

    // Production mode: Serve everything and forward to index.html
    registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/static/", "classpath:/public/")
        .resourceChain(true)
        .addResolver(new PathResourceResolver() {
            // Forward Angular routes to index.html
        });
}
```

**Fix**: Explicitly register a limited resource handler that only serves actual static files (`/assets/**`, `.js`, `.css`, etc.), preventing Spring from trying to resolve Angular routes.

---

## Behavior

### Development Mode (Frontend Separate)

**Static Files** (`/assets/logo.png`, `/styles.css`):
- ✅ Served if they exist in `classpath:/static/`
- ❌ 404 if they don't exist (expected)

**Angular Routes** (`/dashboard`, `/platform/stats`, `/members`):
- ❌ 404 from backend (expected - frontend handles these on port 4200)
- ✅ No more "No static resource" errors

**API Routes** (`/api/auth/login`, `/api/dashboard/stats`):
- ✅ Handled by Spring controllers as normal

### Production Mode (Frontend Bundled)

**Static Files**:
- ✅ Served from `classpath:/static/`

**Angular Routes**:
- ✅ Forwarded to `index.html`
- ✅ Angular router handles client-side routing

**API Routes**:
- ✅ Handled by Spring controllers

---

## Development Workflow

In development, you should:

1. **Run Backend** on port 8080:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Run Frontend** on port 4200:
   ```bash
   cd past-care-spring-frontend
   ng serve
   ```

3. **Access Application**:
   - Frontend: `http://localhost:4200` ← Use this for UI
   - Backend APIs: `http://localhost:8080/api/*` ← Only for API calls
   - ❌ Don't access `http://localhost:8080/platform/*` directly

4. **Frontend Makes API Calls**:
   - Angular app at `:4200` makes HTTP requests to `:8080/api/*`
   - CORS is configured to allow this

---

## Why This Matters

### Before Fix
- Logs filled with "No static resource" errors
- Confusing error messages when accidentally accessing Angular routes on backend
- Made it harder to spot real errors

### After Fix
- Clean logs in development
- Angular routes return 404 (expected behavior)
- Only actual resource issues show errors
- Clear separation: frontend serves UI, backend serves API

---

## Production Deployment

In production, the Angular frontend is built and copied to `src/main/resources/static/`:

```bash
# Build frontend
cd past-care-spring-frontend
ng build --configuration=production

# Copy to backend
cp -r dist/past-care-spring-frontend/* ../src/main/resources/static/

# Build backend with bundled frontend
cd ..
./mvnw clean package
```

Then:
- `INDEX_HTML_EXISTS = true` (production mode)
- Backend serves both UI and API from single port
- All Angular routes forwarded to `index.html`
- Angular handles client-side routing

---

## Testing

### Verify Development Mode Works

1. Start backend only (frontend NOT running):
   ```bash
   ./mvnw spring-boot:run
   ```

2. Test static file (should 404, no error log):
   ```bash
   curl http://localhost:8080/assets/nonexistent.png
   # Returns 404, no "No static resource" error in logs
   ```

3. Test Angular route (should 404, no error log):
   ```bash
   curl http://localhost:8080/platform/stats
   # Returns 404, no "No static resource" error in logs
   ```

4. Test API route (should work):
   ```bash
   curl http://localhost:8080/api/health
   # Returns API response
   ```

### Verify Production Mode Works

1. Build and bundle frontend:
   ```bash
   cd past-care-spring-frontend && ng build --prod
   cp -r dist/* ../src/main/resources/static/
   cd .. && ./mvnw clean package
   ```

2. Run packaged application:
   ```bash
   java -jar target/pastcare-spring-0.0.1-SNAPSHOT.jar
   ```

3. Test Angular route (should serve index.html):
   ```bash
   curl http://localhost:8080/platform/stats
   # Returns index.html content
   ```

4. Test API route (should work):
   ```bash
   curl http://localhost:8080/api/health
   # Returns API response
   ```

---

## Related Files

- ✅ [SpaRoutingConfig.java](src/main/java/com/reuben/pastcare_spring/config/SpaRoutingConfig.java) - Fixed
- ℹ️ [CorsConfig.java](src/main/java/com/reuben/pastcare_spring/security/CorsConfig.java) - Allows frontend on :4200 to call backend on :8080
- ℹ️ [SecurityConfig.java](src/main/java/com/reuben/pastcare_spring/security/SecurityConfig.java) - Security configuration

---

## Key Takeaways

1. **Empty return != No configuration**: Returning early from `addResourceHandlers()` doesn't prevent Spring's default behavior - you must explicitly configure what to serve.

2. **Development vs Production**: Different modes need different configurations:
   - Dev: Limited static files, no Angular route handling
   - Prod: Full SPA support with client-side routing

3. **Clean Logs**: Proper configuration eliminates noise from logs, making real issues easier to spot.

4. **Port Separation**: In development, UI (4200) and API (8080) run separately - this is expected and correct.

---

**Status**: ✅ **FIXED**
**Date**: 2025-12-30
**Impact**: No more "No static resource" errors in development mode logs
