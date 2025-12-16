package com.reuben.pastcare_spring.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.expiration.remember-me}")
    private Long rememberMeExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, false);
    }

    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        long tokenExpiration = rememberMe ? rememberMeExpiration : expiration;
        return createToken(claims, userDetails.getUsername(), tokenExpiration);
    }

    /**
     * Generate JWT with tenant (church) information.
     * This is the primary method for multi-tenant JWT generation.
     */
    public String generateToken(UserDetails userDetails, Long userId, Long churchId, String role, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("churchId", churchId);
        claims.put("role", role);
        claims.put("tokenType", "access");

        long tokenExpiration = rememberMe ? rememberMeExpiration : expiration;
        return createToken(claims, userDetails.getUsername(), tokenExpiration);
    }

    /**
     * Extract church ID (tenant) from JWT token.
     */
    public Long extractChurchId(String token) {
        Claims claims = extractAllClaims(token);
        Object churchId = claims.get("churchId");
        if (churchId instanceof Integer) {
            return ((Integer) churchId).longValue();
        }
        return (Long) churchId;
    }

    /**
     * Extract user ID from JWT token.
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * Extract role from JWT token.
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    /**
     * Extract token type from JWT token.
     */
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("tokenType");
    }

    private String createToken(Map<String, Object> claims, String subject, long tokenExpiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
