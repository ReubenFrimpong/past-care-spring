package com.reuben.pastcare_spring.security;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionFilter.
 * Tests subscription-based access control for protected endpoints.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionFilterTest {

    @Mock
    private ChurchSubscriptionRepository subscriptionRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SubscriptionFilter subscriptionFilter;

    private UserPrincipal userPrincipal;
    private ChurchSubscription activeSubscription;
    private ChurchSubscription suspendedSubscription;
    private ChurchSubscription canceledSubscription;
    private ChurchSubscription gracePeriodSubscription;
    private ChurchSubscription promotionalCreditsSubscription;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        // Setup church
        Church church = new Church();
        church.setId(1L);
        church.setName("Test Church");

        // Setup user with church
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setRole(Role.ADMIN);
        user.setChurch(church);

        // Setup user principal
        userPrincipal = new UserPrincipal(user);

        // Setup active subscription
        activeSubscription = new ChurchSubscription();
        activeSubscription.setId(1L);
        activeSubscription.setChurchId(1L);
        activeSubscription.setStatus("ACTIVE");

        // Setup suspended subscription
        suspendedSubscription = new ChurchSubscription();
        suspendedSubscription.setId(2L);
        suspendedSubscription.setChurchId(1L);
        suspendedSubscription.setStatus("SUSPENDED");

        // Setup canceled subscription
        canceledSubscription = new ChurchSubscription();
        canceledSubscription.setId(3L);
        canceledSubscription.setChurchId(1L);
        canceledSubscription.setStatus("CANCELED");

        // Setup grace period subscription (past due but within grace period)
        gracePeriodSubscription = new ChurchSubscription();
        gracePeriodSubscription.setId(4L);
        gracePeriodSubscription.setChurchId(1L);
        gracePeriodSubscription.setStatus("PAST_DUE");
        gracePeriodSubscription.setNextBillingDate(LocalDate.now().minusDays(2)); // 2 days overdue
        gracePeriodSubscription.setGracePeriodDays(7); // 7 day grace period, so still within grace

        // Setup subscription with promotional credits (free months)
        promotionalCreditsSubscription = new ChurchSubscription();
        promotionalCreditsSubscription.setId(5L);
        promotionalCreditsSubscription.setChurchId(1L);
        promotionalCreditsSubscription.setStatus("SUSPENDED"); // Status is suspended but has free months
        promotionalCreditsSubscription.setFreeMonthsRemaining(3); // 3 free months remaining
    }

    @Test
    void shouldAllowAccessToExemptedEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(subscriptionRepository, never()).findByChurchId(anyLong());
    }

    @Test
    void shouldAllowAccessToBillingEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/billing/subscription");

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(subscriptionRepository, never()).findByChurchId(anyLong());
    }

    @Test
    void shouldAllowAccessToNonProtectedEndpoints() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/public/status");

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(subscriptionRepository, never()).findByChurchId(anyLong());
    }

    @Test
    void shouldAllowAccessWithActiveSubscription() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/dashboard/stats");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(subscriptionRepository.findByChurchId(1L)).thenReturn(Optional.of(activeSubscription));

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
    }

    @Test
    void shouldAllowAccessDuringGracePeriod() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/members/list");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(subscriptionRepository.findByChurchId(1L)).thenReturn(Optional.of(gracePeriodSubscription));

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
    }

    @Test
    void shouldAllowAccessWithPromotionalCredits() throws Exception {
        // Given - Church has promotional credits (free months) despite SUSPENDED status
        when(request.getRequestURI()).thenReturn("/api/dashboard/stats");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(subscriptionRepository.findByChurchId(1L)).thenReturn(Optional.of(promotionalCreditsSubscription));

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then - Access should be allowed due to promotional credits
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
    }

    @Test
    void shouldBlockAccessWithSuspendedSubscription() throws Exception {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/api/dashboard/stats");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(subscriptionRepository.findByChurchId(1L)).thenReturn(Optional.of(suspendedSubscription));
        when(response.getWriter()).thenReturn(writer);

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
        verify(response).setContentType("application/json");

        String jsonResponse = stringWriter.toString();
        assert(jsonResponse.contains("SUBSCRIPTION_REQUIRED"));
        assert(jsonResponse.contains("SUSPENDED"));
    }

    @Test
    void shouldBlockAccessWithCanceledSubscription() throws Exception {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/api/members/list");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(subscriptionRepository.findByChurchId(1L)).thenReturn(Optional.of(canceledSubscription));
        when(response.getWriter()).thenReturn(writer);

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);

        String jsonResponse = stringWriter.toString();
        assert(jsonResponse.contains("CANCELED"));
    }

    @Test
    void shouldBlockAccessWithNoSubscription() throws Exception {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/api/events/list");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(subscriptionRepository.findByChurchId(1L)).thenReturn(Optional.empty());
        when(response.getWriter()).thenReturn(writer);

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);

        String jsonResponse = stringWriter.toString();
        assert(jsonResponse.contains("NO_SUBSCRIPTION"));
    }

    @Test
    void shouldSkipFilterForUnauthenticatedRequests() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/dashboard/stats");
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(subscriptionRepository, never()).findByChurchId(anyLong());
    }

    @Test
    void shouldSkipFilterWhenChurchIdIsNull() throws Exception {
        // Given - SUPERADMIN with null church_id
        User superAdminUser = new User();
        superAdminUser.setId(1L);
        superAdminUser.setEmail("admin@example.com");
        superAdminUser.setPassword("password");
        superAdminUser.setName("Super Admin");
        superAdminUser.setRole(Role.SUPERADMIN);
        superAdminUser.setChurch(null); // SUPERADMIN has no church

        UserPrincipal superAdminPrincipal = new UserPrincipal(superAdminUser);

        when(request.getRequestURI()).thenReturn("/api/dashboard/stats");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(superAdminPrincipal);

        // When
        subscriptionFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(subscriptionRepository, never()).findByChurchId(any());
    }

    @Test
    void shouldBlockAllProtectedEndpointsWithInactiveSubscription() throws Exception {
        // Given
        String[] protectedEndpoints = {
            "/api/dashboard/stats",
            "/api/members/list",
            "/api/attendance/sessions",
            "/api/events/upcoming",
            "/api/donations/list",
            "/api/fellowships/all",
            "/api/users/list",
            "/api/reports/summary"
        };

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(subscriptionRepository.findByChurchId(1L)).thenReturn(Optional.of(suspendedSubscription));
        when(response.getWriter()).thenReturn(writer);

        // When & Then
        for (String endpoint : protectedEndpoints) {
            reset(filterChain, response);
            when(response.getWriter()).thenReturn(writer);
            when(request.getRequestURI()).thenReturn(endpoint);

            subscriptionFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
        }
    }
}
