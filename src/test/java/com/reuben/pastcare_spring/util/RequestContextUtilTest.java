package com.reuben.pastcare_spring.util;

import com.reuben.pastcare_spring.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestContextUtil.
 * Tests extraction of church ID and user ID from JWT tokens in cookies and headers.
 */
@ExtendWith(MockitoExtension.class)
class RequestContextUtilTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    private RequestContextUtil requestContextUtil;

    @BeforeEach
    void setUp() {
        requestContextUtil = new RequestContextUtil(jwtUtil);
    }

    @Test
    void testExtractChurchIdFromCookie() {
        // Given
        String token = "valid.jwt.token";
        Long expectedChurchId = 123L;
        Cookie[] cookies = {new Cookie("access_token", token)};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.extractChurchId(token)).thenReturn(expectedChurchId);

        // When
        Long actualChurchId = requestContextUtil.extractChurchId(request);

        // Then
        assertEquals(expectedChurchId, actualChurchId);
        verify(jwtUtil).extractChurchId(token);
    }

    @Test
    void testExtractChurchIdFromAuthorizationHeader() {
        // Given
        String token = "valid.jwt.token";
        Long expectedChurchId = 456L;

        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractChurchId(token)).thenReturn(expectedChurchId);

        // When
        Long actualChurchId = requestContextUtil.extractChurchId(request);

        // Then
        assertEquals(expectedChurchId, actualChurchId);
        verify(jwtUtil).extractChurchId(token);
    }

    @Test
    void testExtractChurchIdNoToken() {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> requestContextUtil.extractChurchId(request)
        );

        assertEquals("No valid JWT token found in request", exception.getMessage());
        verify(jwtUtil, never()).extractChurchId(anyString());
    }

    @Test
    void testExtractChurchIdEmptyCookies() {
        // Given
        Cookie[] cookies = {};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getHeader("Authorization")).thenReturn(null);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> requestContextUtil.extractChurchId(request)
        );

        assertEquals("No valid JWT token found in request", exception.getMessage());
    }

    @Test
    void testExtractChurchIdWrongCookieName() {
        // Given
        String token = "valid.jwt.token";
        Long expectedChurchId = 789L;
        Cookie[] cookies = {
            new Cookie("other_cookie", "value"),
            new Cookie("session", "session_value")
        };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractChurchId(token)).thenReturn(expectedChurchId);

        // When
        Long actualChurchId = requestContextUtil.extractChurchId(request);

        // Then - Should fall back to Authorization header
        assertEquals(expectedChurchId, actualChurchId);
        verify(jwtUtil).extractChurchId(token);
    }

    @Test
    void testExtractChurchIdInvalidAuthorizationHeaderFormat() {
        // Given
        Cookie[] cookies = {};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // When & Then - Should fail because header doesn't start with "Bearer "
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> requestContextUtil.extractChurchId(request)
        );

        assertEquals("No valid JWT token found in request", exception.getMessage());
    }

    @Test
    void testExtractUserIdFromCookie() {
        // Given
        String token = "valid.jwt.token";
        Long expectedUserId = 999L;
        Cookie[] cookies = {new Cookie("access_token", token)};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.extractUserId(token)).thenReturn(expectedUserId);

        // When
        Long actualUserId = requestContextUtil.extractUserId(request);

        // Then
        assertEquals(expectedUserId, actualUserId);
        verify(jwtUtil).extractUserId(token);
    }

    @Test
    void testExtractUserIdFromAuthorizationHeader() {
        // Given
        String token = "valid.jwt.token";
        Long expectedUserId = 888L;

        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUserId(token)).thenReturn(expectedUserId);

        // When
        Long actualUserId = requestContextUtil.extractUserId(request);

        // Then
        assertEquals(expectedUserId, actualUserId);
        verify(jwtUtil).extractUserId(token);
    }

    @Test
    void testExtractUserIdNoToken() {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> requestContextUtil.extractUserId(request)
        );

        assertEquals("No valid JWT token found in request", exception.getMessage());
        verify(jwtUtil, never()).extractUserId(anyString());
    }

    @Test
    void testCookieTakesPrecedenceOverHeader() {
        // Given
        String cookieToken = "cookie.jwt.token";
        Long expectedChurchId = 111L;

        Cookie[] cookies = {new Cookie("access_token", cookieToken)};
        when(request.getCookies()).thenReturn(cookies);
        // Note: We don't stub getHeader because it should never be called when cookie exists
        when(jwtUtil.extractChurchId(cookieToken)).thenReturn(expectedChurchId);

        // When
        Long actualChurchId = requestContextUtil.extractChurchId(request);

        // Then - Should use cookie token without checking header
        assertEquals(expectedChurchId, actualChurchId);
        verify(jwtUtil).extractChurchId(cookieToken);
        verify(request, never()).getHeader("Authorization");
    }
}
