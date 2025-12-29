package com.reuben.pastcare_spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String username = null;
        String jwt = null;

        // Get JWT from HttpOnly cookie
        jwt = cookieUtil.getAccessToken(request).orElse(null);
        if (jwt != null) {
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Set tenant context from JWT
                Long churchId = jwtUtil.extractChurchId(jwt);
                Long userId = jwtUtil.extractUserId(jwt);
                String role = jwtUtil.extractRole(jwt);

                if (churchId != null) {
                    TenantContext.setCurrentChurchId(churchId);
                }
                if (userId != null) {
                    TenantContext.setCurrentUserId(userId);
                }
                if (role != null) {
                    TenantContext.setCurrentUserRole(role);
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Clear tenant context after request
            TenantContext.clear();
        }
    }
}