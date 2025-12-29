package com.reuben.pastcare_spring.config;

import com.reuben.pastcare_spring.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that enables Hibernate filters for tenant isolation.
 *
 * <p>This interceptor automatically enables the "churchFilter" defined in TenantBaseEntity
 * for all incoming requests, ensuring that all database queries are automatically scoped
 * to the current tenant (church).
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>Request comes in and is authenticated via JWT</li>
 *   <li>JwtAuthenticationFilter sets churchId in TenantContext</li>
 *   <li>This interceptor runs BEFORE controller method</li>
 *   <li>Enables Hibernate filter with churchId parameter</li>
 *   <li>All subsequent queries automatically include WHERE church_id = :churchId</li>
 * </ol>
 *
 * <p><b>Security Benefits:</b>
 * - Defense in depth: Even if service layer validation fails, filter prevents cross-tenant access
 * - Automatic: No need to manually filter in every repository method
 * - Transparent: Application code doesn't need to know about filtering
 *
 * <p><b>SUPERADMIN Bypass:</b>
 * When a SUPERADMIN user is authenticated, the filter is NOT enabled, allowing
 * platform administrators to access data across all churches for support and maintenance.
 *
 * @see com.reuben.pastcare_spring.models.TenantBaseEntity - Defines the filter
 * @see com.reuben.pastcare_spring.security.TenantContext - Stores current tenant
 */
@Component
@Slf4j
public class HibernateFilterInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Enables the Hibernate church filter before the request is processed.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param handler The handler (controller method) to be executed
     * @return true to continue with the request processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long churchId = TenantContext.getCurrentChurchId();
        boolean isSuperadmin = TenantContext.isSuperadmin();

        // Only enable filter for non-SUPERADMIN users with a valid church context
        if (churchId != null && !isSuperadmin) {
            Session session = entityManager.unwrap(Session.class);

            // Enable the churchFilter defined in TenantBaseEntity
            org.hibernate.Filter filter = session.enableFilter("churchFilter");
            filter.setParameter("churchId", churchId);

            log.debug("Hibernate filter 'churchFilter' enabled for church ID: {}", churchId);
        } else if (isSuperadmin) {
            log.debug("Hibernate filter NOT enabled - SUPERADMIN user detected");
        } else {
            log.debug("Hibernate filter NOT enabled - No church context found (public endpoint or unauthenticated)");
        }

        return true;
    }

    /**
     * Cleanup after request processing.
     * Hibernate Session is request-scoped, so filter will be automatically cleared.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // Session cleanup is automatic in Spring
        // Filter will be disabled when session is closed
        log.trace("Request completed - Hibernate session will be cleaned up");
    }
}
