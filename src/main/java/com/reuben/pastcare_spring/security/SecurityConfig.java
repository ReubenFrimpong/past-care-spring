package com.reuben.pastcare_spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private SubscriptionFilter subscriptionFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // Enable CORS with default settings (uses CorsConfig)
            .authorizeHttpRequests(auth -> auth
                // Public API endpoints
                .requestMatchers("/api/auth/**", "/api/location/**", "/api/billing/plans").permitAll()
                // Public payment verification (no auth required for returning from Paystack)
                .requestMatchers("/api/billing/public/**").permitAll()
                // Paystack webhooks (no auth required - Paystack servers call this)
                .requestMatchers("/api/webhooks/paystack/**").permitAll()
                // Portal endpoints - public for member self-registration and profile management
                .requestMatchers("/api/portal/register", "/api/portal/register-with-photo", "/api/portal/login", "/api/portal/verify", "/api/portal/resend-verification", "/api/portal/forgot-password", "/api/portal/reset-password", "/api/portal/profile/picture", "/api/portal/profile").permitAll()
                // Invitation code validation - public for registration
                .requestMatchers("/api/invitation-codes/validate/**").permitAll()
                // Church logo - public for landing page and favicon
                .requestMatchers("/api/churches/public/logo").permitAll()
                // Billing endpoints - accessible to authenticated users even without active subscription
                .requestMatchers("/api/billing/**", "/api/churches/*/subscription").authenticated()
                // Storage add-ons - accessible to authenticated users (for billing page)
                .requestMatchers("/api/storage-addons").authenticated()
                // All API requests require authentication
                .requestMatchers("/api/**").authenticated()
                // Allow all other requests (Angular routes, static resources)
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(subscriptionFilter, JwtAuthenticationFilter.class); // Check subscription after authentication

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}