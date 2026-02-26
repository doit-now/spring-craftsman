package com.giaolang.mamnguqua.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO - Request để đăng nhập
 *
 * Sử dụng trong:
 * - POST /api/auth/login
 *
 * Hardcoded credentials (chỉ dùng để học):
 * - username: admin
 * - password: 123456
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * Tên đăng nhập
     */
    @NotBlank(message = "Username không được để trống")
    private String username;

    /**
     * Mật khẩu
     */
    @NotBlank(message = "Password không được để trống")
    private String password;
}
