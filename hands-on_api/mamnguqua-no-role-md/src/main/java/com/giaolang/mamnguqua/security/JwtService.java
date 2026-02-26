package com.giaolang.mamnguqua.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service - JWT Token Management
 *
 * Chức năng:
 * - Generate JWT token từ username
 * - Validate JWT token
 * - Extract username từ token
 *
 * JWT Structure:
 * - Header: {alg: "HS256", typ: "JWT"}
 * - Payload: {sub: "username", iat: timestamp, exp: timestamp}
 * - Signature: HMAC-SHA256(header + payload, secret)
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Secret key để sign JWT tokens
     * Được load từ application.properties (jwt.secret)
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Token expiration time (milliseconds)
     * Được load từ application.properties (jwt.expiration)
     * Mặc định: 86400000 ms = 24 giờ
     */
    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Generate JWT token từ username
     * <p>
     * Flow:
     * 1. Tạo secret key từ jwtSecret string
     * 2. Tính thời gian hết hạn
     * 3. Build JWT token với claims
     * 4. Sign bằng secret key
     * 5. Compact to string và return
     *
     * @param username Username đưa vào token
     * @return JWT token (String)
     */
    public String generateToken(String username) {
        log.debug("Generating JWT token for username: {}", username);

        try {
            String token = Jwts.builder()
                    .subject(username) // Lưu tên người dùng vào Payload
                    .claim("role", "ADMIN") // Thêm thông tin vai trò vào Payload
                    .issuedAt(new Date()) // Ngày tạo
                    .expiration(new Date(System.currentTimeMillis() + expirationMs)) // Ngày hết hạn
                    .signWith(getSigningKey()) // Ký tên bằng chìa khóa bí mật
                    .compact();

            log.info("JWT token generated successfully for user: {}", username);
            return token;

        } catch (Exception e) {
            log.error("Error generating JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate JWT token
     * <p>
     * Kiểm tra:
     * - Token không bị tampered (signature hợp lệ)
     * - Token không hết hạn
     * - Token format đúng
     *
     * @param token JWT token cần validate
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean isValidToken(String token) {
        try {
            getClaimsFromToken(token);
            log.debug("Token validation successful");
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT token is invalid: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username từ JWT token
     *
     * @param token JWT token
     * @return Username được extract, hoặc null nếu không thể extract
     */
    public String extractUsername(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            log.debug("Username extracted from token: {}", username);
            return username;

        } catch (ExpiredJwtException e) {
            log.warn("Cannot extract username - token expired");
            return null;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Cannot extract username - invalid token");
            return null;

        } catch (Exception e) {
            log.error("Error extracting username: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Hàm hỗ trợ: Tạo SecretKey từ secretKey string
     *
     * @return SecretKey được tạo từ secretKey string dùng để sign và verify JWT tokens. SecretKey phải đủ dài (ít nhất 256 bit - 32 kí tự) để đảm bảo an toàn khi sử dụng HMAC-SHA256
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Hàm hỗ trợ: Lấy claims từ token
     *
     * @param token JWT token từ client gửi lên
     * @return Claims chứa thông tin payload của token
     * @throws JwtException nếu token không hợp lệ hoặc đã hết hạn
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
