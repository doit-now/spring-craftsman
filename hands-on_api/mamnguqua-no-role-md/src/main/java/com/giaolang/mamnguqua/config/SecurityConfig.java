package com.giaolang.mamnguqua.config;

import com.giaolang.mamnguqua.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Cấu hình bảo mật ứng dụng - Spring Security Setup
 *
 * Các tính năng cấu hình:
 * - Tắt CSRF: Không cần bảo vệ CSRF vì ứng dụng sử dụng JWT (không có trạng thái)
 * - Chính sách phiên: STATELESS (không lưu trữ phiên trên máy chủ)
 * - Endpoints công khai: /api/auth/login, /swagger-ui.html
 * - Endpoints được bảo vệ: /api/fruits/** (yêu cầu JWT token)
 * - Thêm JWT filter trước UsernamePasswordAuthenticationFilter
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Filter xác thực JWT
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Cấu hình bảo mật HTTP
     *
     * @param http Đối tượng cấu hình bảo mật HTTP
     * @return Chuỗi filter bảo mật
     * @throws Exception Nếu có lỗi cấu hình
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF - không cần vì ứng dụng sử dụng JWT (không có trạng thái)
                .csrf(csrf -> csrf.disable())

                // Bật CORS - sử dụng cấu hình mặc định từ bean corsConfigurationSource
                .cors(Customizer.withDefaults())

                // Chính sách phiên: STATELESS (không tạo phiên lưu trữ trên máy chủ)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình phép truy cập cho các yêu cầu HTTP
                // ⭐ GIAI ĐOẠN AUTHORIZATION (Phân quyền):
                // Sau JwtAuthenticationFilter tạo Authentication object, Authorization Filter sẽ kiểm tra
                // xem user có quyền truy cập endpoint không dựa trên roles
                //
                // 📋 CÁC LOẠI KIỂM TRA QUYỀN:
                // 1. hasRole('ADMIN') → Kiểm tra user có role ADMIN
                // 2. hasAnyRole('ADMIN', 'STAFF') → Kiểm tra user có 1 trong các role
                // 3. authenticated() → Chỉ kiểm tra user đã xác thực (không kiểm tra role)
                .authorizeHttpRequests(auth -> auth
                        // ✅ Cho phép các yêu cầu preflight (OPTIONS) đi qua mà không cần xác thực
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints công khai - không cần JWT
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // ⭐ PHÂN QUYỀN THEO ENDPOINT (nếu thêm role):
                        // Ví dụ:
                        // .requestMatchers(HttpMethod.POST, "/api/fruits").hasRole("ADMIN")
                        //     └─ Chỉ ADMIN được POST
                        // .requestMatchers(HttpMethod.DELETE, "/api/fruits/**").hasRole("ADMIN")
                        //     └─ Chỉ ADMIN được DELETE
                        // .requestMatchers(HttpMethod.PUT, "/api/fruits/**").hasAnyRole("ADMIN", "STAFF")
                        //     └─ ADMIN và STAFF được PUT
                        // .requestMatchers(HttpMethod.GET, "/api/fruits/**").hasAnyRole("ADMIN", "STAFF", "MEMBER")
                        //     └─ Tất cả role được GET

                        // Hiện tại: Tất cả /api/fruits chỉ cần authenticated (chưa phân quyền chi tiết)
                        .requestMatchers("/api/fruits/**").authenticated()

                        // Tất cả các yêu cầu khác đều phải được xác thực
                        .anyRequest().authenticated())

                // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
                //
                // 🎯 CÁC KHÁI NIỆM QUAN TRỌNG VỀ SPRING SECURITY:
                //
                // ┌─────────────────────────────────────────────────────────────┐
                // │ 1. SecurityContext và Authentication Object                 │
                // ├─────────────────────────────────────────────────────────────┤
                // │                                                              │
                // │  SecurityContext                                            │
                // │  └── Authentication (interface - không phải 1 class cụ thể) │
                // │      │                                                      │
                // │      ├── UsernamePasswordAuthenticationToken                │
                // │      │   (dùng cho form login - username + password)        │
                // │      │                                                      │
                // │      ├── JwtAuthenticationToken (hoặc tương tự)             │
                // │      │   (dùng cho JWT token-based authentication)          │
                // │      │                                                      │
                // │      └── [...các loại Authentication khác]                 │
                // │          (OAuth2, SAML, LDAP, v.v.)                        │
                // │                                                              │
                // │ ✅ NGUYÊN TẮC: Miễn chứa Authentication object nào trên,    │
                // │    Spring Security sẽ xem user đã được xác thực (authenticated)
                // │                                                              │
                // └─────────────────────────────────────────────────────────────┘
                //
                // ⚠️  QUAN TRỌNG: "BEFORE" NGHĨA LÀ "CHẠY TRƯỚC", KHÔNG PHẢI OVERRIDE!
                //
                // UsernamePasswordAuthenticationFilter là filter mặc định của Spring Security
                // nó được dùng để xử lý login bằng form (username + password).
                // Vì ứng dụng của ta dùng JWT (token-based authentication), không dùng username/password,
                // nên ta cần thêm JwtAuthenticationFilter TRƯỚC nó để:
                //
                // 📊 THỨ TỰ CHẠY CỦA CÁC FILTER (Filter Chain):
                //
                //    JwtAuthenticationFilter (chạy ĐẦU TIÊN)
                //           ↓
                //    UsernamePasswordAuthenticationFilter (chạy THỨ HAI)
                //           ↓
                //    [Các filter khác...]
                //           ↓
                //    Authorization Filter (kiểm tra quyền truy cập)
                //
                // 🔄 CÁCH HOẠT ĐỘNG CHI TIẾT:
                //
                // 1️⃣  Khi client gửi request với Authorization header chứa JWT token
                //     Authorization: Bearer <jwt_token>
                //
                // 2️⃣  JwtAuthenticationFilter chạy ĐẦU TIÊN và:
                //     - Extract token từ Authorization header
                //     - Validate token
                //     - Extract username từ token
                //     - TẠO VÀ ĐẶT AUTHENTICATION OBJECT vào SecurityContext:
                //       UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                //           username,           // principal (ai)
                //           null,               // credentials (không cần)
                //           null                // authorities (quyền)
                //       );
                //       SecurityContextHolder.getContext().setAuthentication(auth); ✅
                //
                // 3️⃣  UsernamePasswordAuthenticationFilter chạy THỨ HAI:
                //     - Kiểm tra SecurityContext.getAuthentication()
                //     - Thấy đã có Authentication object → User đã authenticated ✅
                //     - SKIP (bỏ qua) filter này, không cần xử lý username/password
                //
                // 4️⃣  Authorization Filter (kiểm tra quyền):
                //     - Lấy Authentication từ SecurityContext
                //     - Kiểm tra xem user có quyền truy cập endpoint này không?
                //     - Nếu @RequestMapping("/api/fruits/**").authenticated() → PASS ✅
                //
                // 5️⃣  Request tiếp tục đi tới controller
                //
                // ✨ KỸ NĂNG: Vì vậy, trong Spring Security:
                //    - Không bắt buộc phải dùng UsernamePasswordAuthenticationFilter
                //    - Bắt buộc phải có Authorization object nào đó trong SecurityContext
                //    - Có thể tạo bất kỳ loại Authentication nào (JWT, OAuth2, SAML, v.v.)
                //    - Miễn SecurityContext có Authentication object → coi như authenticated ✅
                //
                // TÓMLẠI: "before" = "chạy trước", không phải override
                //         - JwtAuthenticationFilter chạy trước để tạo Authentication object
                //         - UsernamePasswordAuthenticationFilter chạy sau nhưng sẽ skip
                //         - Kết quả: ứng dụng dùng JWT thay vì username/password ✨
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cấu hình CORS cho Spring Security
     *
     * Cung cấp bean CorsConfigurationSource để Spring Security có thể áp dụng các quy tắc CORS
     *
     * @return Nguồn cấu hình CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Các origin được phép (các tên miền/cổng có thể truy cập API)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",   // React
                "http://localhost:8080",   // Vue
                "http://localhost:4200"    // Angular
        ));

        // Các phương thức HTTP được phép
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Các header yêu cầu được phép
        configuration.setAllowedHeaders(Arrays.asList("*", "Content-Type", "Authorization"));

        // Cho phép gửi credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Thời gian tối đa để cache kết quả preflight request (1 giờ)
        configuration.setMaxAge(3600L);

        // Các header phản hồi được phép hiển thị cho client
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
