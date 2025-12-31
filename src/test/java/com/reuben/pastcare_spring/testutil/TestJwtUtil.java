package com.reuben.pastcare_spring.testutil;

import com.reuben.pastcare_spring.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating JWT tokens in tests.
 *
 * This class provides methods to create valid JWT tokens for different roles
 * and test scenarios, including expired tokens for security testing.
 */
public class TestJwtUtil {

    // Must match the secret in application-test.properties (base64 encoded)
    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkluVGVzdEVudmlyb25tZW50VGhhdElzTG9uZ0Vub3VnaA==";
    private static final long DEFAULT_EXPIRATION = 3600000; // 1 hour
    private static final long REFRESH_EXPIRATION = 86400000; // 24 hours

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
    }

    /**
     * Generate a standard access token for a user with specified role.
     *
     * @param userId The user's ID
     * @param email The user's email
     * @param churchId The church ID (can be null for SUPERADMIN)
     * @param role The user's role
     * @return JWT token string
     */
    public static String generateToken(Long userId, String email, Long churchId, Role role) {
        return generateToken(userId, email, churchId, role, false);
    }

    /**
     * Generate a token with remember-me flag.
     *
     * @param userId The user's ID
     * @param email The user's email
     * @param churchId The church ID (can be null for SUPERADMIN)
     * @param role The user's role
     * @param rememberMe Whether to use extended expiration
     * @return JWT token string
     */
    public static String generateToken(Long userId, String email, Long churchId, Role role, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("churchId", churchId);
        claims.put("role", role.name());
        claims.put("tokenType", "access");

        long expiration = rememberMe ? REFRESH_EXPIRATION : DEFAULT_EXPIRATION;

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate token for SUPERADMIN (no church association).
     *
     * @param userId The superadmin user's ID
     * @param email The superadmin's email
     * @return JWT token string
     */
    public static String generateSuperadminToken(Long userId, String email) {
        return generateToken(userId, email, null, Role.SUPERADMIN);
    }

    /**
     * Generate token for ADMIN role.
     *
     * @param userId The admin user's ID
     * @param email The admin's email
     * @param churchId The church ID
     * @return JWT token string
     */
    public static String generateAdminToken(Long userId, String email, Long churchId) {
        return generateToken(userId, email, churchId, Role.ADMIN);
    }

    /**
     * Generate token for PASTOR role.
     *
     * @param userId The pastor user's ID
     * @param email The pastor's email
     * @param churchId The church ID
     * @return JWT token string
     */
    public static String generatePastorToken(Long userId, String email, Long churchId) {
        return generateToken(userId, email, churchId, Role.PASTOR);
    }

    /**
     * Generate token for TREASURER role.
     *
     * @param userId The treasurer user's ID
     * @param email The treasurer's email
     * @param churchId The church ID
     * @return JWT token string
     */
    public static String generateTreasurerToken(Long userId, String email, Long churchId) {
        return generateToken(userId, email, churchId, Role.TREASURER);
    }

    /**
     * Generate token for MEMBER_MANAGER role.
     *
     * @param userId The member manager user's ID
     * @param email The member manager's email
     * @param churchId The church ID
     * @return JWT token string
     */
    public static String generateMemberManagerToken(Long userId, String email, Long churchId) {
        return generateToken(userId, email, churchId, Role.MEMBER_MANAGER);
    }

    /**
     * Generate token for FELLOWSHIP_LEADER role.
     *
     * @param userId The fellowship leader user's ID
     * @param email The fellowship leader's email
     * @param churchId The church ID
     * @return JWT token string
     */
    public static String generateFellowshipLeaderToken(Long userId, String email, Long churchId) {
        return generateToken(userId, email, churchId, Role.FELLOWSHIP_LEADER);
    }

    /**
     * Generate token for MEMBER role.
     *
     * @param userId The member user's ID
     * @param email The member's email
     * @param churchId The church ID
     * @return JWT token string
     */
    public static String generateMemberToken(Long userId, String email, Long churchId) {
        return generateToken(userId, email, churchId, Role.MEMBER);
    }

    /**
     * Generate an expired token for testing token expiry scenarios.
     *
     * @param userId The user's ID
     * @param email The user's email
     * @param churchId The church ID
     * @param role The user's role
     * @return Expired JWT token string
     */
    public static String generateExpiredToken(Long userId, String email, Long churchId, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("churchId", churchId);
        claims.put("role", role.name());
        claims.put("tokenType", "access");

        // Token expired 1 hour ago
        long pastTime = System.currentTimeMillis() - 3600000;

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(pastTime - 1000))
                .expiration(new Date(pastTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a refresh token.
     *
     * @param userId The user's ID
     * @param email The user's email
     * @param churchId The church ID
     * @param role The user's role
     * @return Refresh JWT token string
     */
    public static String generateRefreshToken(Long userId, String email, Long churchId, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("churchId", churchId);
        claims.put("role", role.name());
        claims.put("tokenType", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a token with custom expiration time.
     *
     * @param userId The user's ID
     * @param email The user's email
     * @param churchId The church ID
     * @param role The user's role
     * @param expirationMs Expiration time in milliseconds from now
     * @return JWT token string
     */
    public static String generateTokenWithCustomExpiration(Long userId, String email, Long churchId,
                                                           Role role, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("churchId", churchId);
        claims.put("role", role.name());
        claims.put("tokenType", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }
}
