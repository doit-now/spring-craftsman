package com.giaolang.mamnguqua.dto;

import lombok.*;

/**
 * DTO - Response sau khi đăng nhập thành công
 *
 * Chứa JWT token để sử dụng trong các request sau
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * JWT token để sử dụng trong Authorization header
     */
    private String token;

    /**
     * Thông báo từ server
     */
    private String message;
}
