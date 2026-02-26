package com.giaolang.mamnguqua.controller;

import com.giaolang.mamnguqua.dto.LoginRequest;
import com.giaolang.mamnguqua.dto.LoginResponse;
import com.giaolang.mamnguqua.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller - Authentication API Endpoints
 *
 * Cung cấp endpoint:
 * - POST /api/auth/login - Đăng nhập và lấy JWT token
 *
 * Hardcoded credentials (chỉ dùng để học):
 * - username: admin
 * - password: 123456
 * - role: ROLE_ADMIN
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication", description = "API để xác thực người dùng")
public class AuthController {

    /**
     * JWT Service để generate tokens
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Hardcoded admin credentials (chỉ dùng để học)
     * ⚠️ CẢNH BÁO: Không bao giờ dùng hardcoded credentials trong production!
     */
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "123456";

    /**
     * POST /api/auth/login - Đăng nhập
     * <p>
     * Xác thực người dùng và return JWT token.
     * Token được dùng trong Authorization header cho các request tiếp theo.
     * <p>
     * Flow:
     * 1. Client gửi username và password
     * 2. Server kiểm tra credentials (hardcoded)
     * 3. Nếu đúng, generate JWT token với expiration sau x giờ khai báo trong application.properties
     * 4. Return token cho client
     * 5. Client sử dụng token: Authorization: Bearer <token>
     *
     * @param request LoginRequest chứa username và password
     * @return ResponseEntity chứa LoginResponse với JWT token
     */
    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập",
            description = "Xác thực người dùng và nhận JWT token (24 giờ). Dùng credentials: admin/123456"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Đăng nhập thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Username hoặc password sai"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu request không hợp lệ")
    })
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin đăng nhập (admin/123456)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
            @Valid @RequestBody LoginRequest request) {

        log.info("Login attempt with username: {}", request.getUsername());

        // Validate credentials
        if (!ADMIN_USERNAME.equals(request.getUsername()) ||
                !ADMIN_PASSWORD.equals(request.getPassword())) {
            log.warn("Login failed - invalid credentials for username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .token("")
                            .message("Username hoặc password không chính xác")
                            .build());
        }

        log.info("Login successful for user: {}", request.getUsername());

        // Generate JWT token
        String token = jwtService.generateToken(request.getUsername());
        log.debug("JWT token generated for user: {}", request.getUsername());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .message("Đăng nhập thành công")
                .build();

        return ResponseEntity.ok(response);
    }
}
