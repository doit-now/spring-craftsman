package com.giaolang.mamnguqua.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter - JWT Authentication Filter
 *
 * Chạy trên mỗi HTTP request để:
 * 1. Extract JWT token từ Authorization header
 * 2. Validate token
 * 3. Extract username từ token
 * 4. Set authentication vào SecurityContext
 * 5. Cho phép request tiếp tục xử lý
 *
 * OncePerRequestFilter đảm bảo filter chỉ chạy 1 lần per request
 *
 * Authorization header format: "Bearer <token>"
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT Service để validate và extract username
     */
    private final JwtService jwtService;

    /**
     * Filter logic - Chạy trên mỗi HTTP request
     *
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain Filter chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

            // Trích xuất Authorization header để lấy ra token
            String authHeader = request.getHeader("Authorization");

            // Kiểm tra nếu header không tồn tại hoặc không bắt đầu bằng "Bearer " thì bỏ qua filter này, khả năng là request không cần authentication, ví dụ như public endpoint, hoặc client không gửi token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No JWT token found in Authorization header");
                filterChain.doFilter(request, response);
                return;
            }

            // Lấy ra token bằng cách bỏ "Bearer " prefix - 7 ký tự ở đầu của header
            String token = authHeader.substring(7);
            log.debug("JWT token extracted from header");

            // Kiểm tra tính hợp lệ của token
            if (!jwtService.isValidToken(token)) {
                log.warn("JWT token validation failed");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("JWT token is valid");

            // Lấy ra username từ token. Nếu không thể extract được username (ví dụ token hợp lệ nhưng không có subject), thì cũng bỏ qua filter này hoặc có thể trả về lỗi 401 Unauthorized tùy vào yêu cầu bảo mật của ứng dụng
            String username = jwtService.extractUsername(token);
            if (username == null) {
                log.warn("Failed to extract username from token");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("Username extracted: {}", username);

            // Create Authentication object
//            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            username,
//                            null,
//                            Collections.singletonList(authority)
//                    );

            // Token đã được xác thực và username đã được extract, tạo một Authentication object để đại diện cho user đã authenticated.
            // Ở đây, password có thể để null vì chúng ta không cần sử dụng nó sau khi đã xác thực token.
            // Authorities cũng có thể để null hoặc empty list nếu không cần phân quyền chi tiết.
            // Tuy nhiên, nếu ứng dụng có logic phân quyền dựa trên role, thì nên set authorities tương ứng. Trong trường hợp này, vì chỉ có 1 user admin duy nhất, nên có thể gán role ADMIN cho user này để dễ dàng quản lý phân quyền sau này nếu cần.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            null);

            // Gửi authentication vào SecurityContext để Spring Security biết user đã authenticated
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication set for user: {}", username);

            // Continue filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in JWT authentication filter: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}
