# 💻 CODE EXAMPLES - PHÂN QUYỀN THỰC HÀNH

## 📝 A. JwtService.java - Thêm methods để extract roles

```java
package com.giaolang.mamnguqua.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Service - Quản lý JWT token
 */
@Service
public class JwtService {

    @Value("${jwt.secret:YourSecretKeyHere1234567890123456}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")  // 24 hours
    private long expirationTime;

    // ============ GENERATE TOKEN ============

    /**
     * Tạo JWT token với username và roles
     *
     * @param username Tên người dùng
     * @param roles    Danh sách roles của user (ví dụ: ["ROLE_ADMIN", "ROLE_MEMBER"])
     * @return JWT token
     */
    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        
        // ⭐ Thêm roles vào JWT token
        claims.put("roles", roles);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)  // Username
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ============ VALIDATE TOKEN ============

    /**
     * Kiểm tra JWT token có hợp lệ không
     *
     * @param token JWT token
     * @return true nếu hợp lệ, false nếu không
     */
    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ============ EXTRACT USERNAME ============

    /**
     * Lấy username từ JWT token
     *
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ============ ⭐ EXTRACT ROLES ============

    /**
     * Lấy danh sách roles từ JWT token
     *
     * ⭐ METHOD MỚI - DÙNG CHO PHÂN QUYỀN
     *
     * @param token JWT token
     * @return List of role strings (ví dụ: ["ROLE_ADMIN", "ROLE_MEMBER"])
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        
        return Collections.emptyList();  // Return empty list nếu không có roles
    }

    // ============ ⭐ EXTRACT AUTHORITIES ============

    /**
     * Lấy danh sách GrantedAuthority từ JWT token
     *
     * ⭐ METHOD MỚI - DÙNG CHO SPRING SECURITY AUTHORIZATION
     *
     * @param token JWT token
     * @return List of GrantedAuthority
     *         (ví dụ: [SimpleGrantedAuthority("ROLE_ADMIN"), SimpleGrantedAuthority("ROLE_MEMBER")])
     */
    public List<GrantedAuthority> extractAuthorities(String token) {
        List<String> roles = extractRoles(token);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)  // Chuyển "ROLE_ADMIN" → SimpleGrantedAuthority("ROLE_ADMIN")
                .collect(Collectors.toList());
    }

    // ============ HELPER METHODS ============

    /**
     * Lấy tất cả claims từ JWT token
     *
     * @param token JWT token
     * @return Claims object
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Lấy signing key từ secret key
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

---

## 📝 B. JwtAuthenticationFilter.java - Sửa để set authorities

```java
package com.giaolang.mamnguqua.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter - Xác thực JWT token
 *
 * ⭐ QUAN TRỌNG: Phần này EXTRACT ROLES từ token và SET vào Authentication object
 * để Authentication Filter sau đó có thể kiểm tra quyền
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

            // 1️⃣ Lấy Authorization header
            String authHeader = request.getHeader("Authorization");

            // 2️⃣ Kiểm tra header có tồn tại và bắt đầu bằng "Bearer " không
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No JWT token found in Authorization header");
                filterChain.doFilter(request, response);
                return;
            }

            // 3️⃣ Trích xuất token (bỏ "Bearer " prefix)
            String token = authHeader.substring(7);
            log.debug("JWT token extracted from header");

            // 4️⃣ Validate token
            if (!jwtService.isValidToken(token)) {
                log.warn("JWT token validation failed");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("JWT token is valid");

            // 5️⃣ Lấy username từ token
            String username = jwtService.extractUsername(token);
            if (username == null) {
                log.warn("Failed to extract username from token");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("Username extracted: {}", username);

            // ⭐ 6️⃣ LẤY AUTHORITIES/ROLES TỪ TOKEN (QUAN TRỌNG!)
            List<GrantedAuthority> authorities = jwtService.extractAuthorities(token);
            log.debug("Authorities extracted: {}", authorities);

            // ⭐ 7️⃣ TẠO AUTHENTICATION OBJECT VỚI AUTHORITIES
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,              // principal (ai)
                            null,                  // credentials (không cần)
                            authorities            // ⭐ AUTHORITIES/ROLES (quyền gì)
                    );

            // ⭐ 8️⃣ ĐẶT AUTHENTICATION VÀO SECURITYCONTEXT
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication set for user: {} with roles: {}", username, authorities);

            // 9️⃣ TIẾP TỤC FILTER CHAIN
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in JWT authentication filter: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}
```

---

## 📝 C. SecurityConfig.java - Cấu hình phân quyền

```java
package com.giaolang.mamnguqua.config;

import com.giaolang.mamnguqua.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Config - Cấu hình Spring Security
 *
 * ⭐ Phần này cấu hình AUTHORIZATION - phân quyền dựa trên roles
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ⭐ PHÂN QUYỀN THEO ENDPOINT
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ⭐ PHÂN QUYỀN CHI TIẾT:
                        
                        // POST - Chỉ ADMIN được tạo mới
                        .requestMatchers(HttpMethod.POST, "/api/fruits/**")
                                .hasRole("ADMIN")

                        // DELETE - Chỉ ADMIN được xóa
                        .requestMatchers(HttpMethod.DELETE, "/api/fruits/**")
                                .hasRole("ADMIN")

                        // PUT - ADMIN và STAFF được sửa
                        .requestMatchers(HttpMethod.PUT, "/api/fruits/**")
                                .hasAnyRole("ADMIN", "STAFF")

                        // GET - Tất cả roles được xem
                        .requestMatchers(HttpMethod.GET, "/api/fruits/**")
                                .hasAnyRole("ADMIN", "STAFF", "MEMBER")

                        // Tất cả endpoint khác phải authenticated
                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080",
                "http://localhost:4200"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*", "Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

---

## 📝 D. AuthController.java - Thêm roles khi login

```java
package com.giaolang.mamnguqua.controller;

import com.giaolang.mamnguqua.dto.LoginRequest;
import com.giaolang.mamnguqua.dto.LoginResponse;
import com.giaolang.mamnguqua.entity.User;
import com.giaolang.mamnguqua.exception.ResourceNotFoundException;
import com.giaolang.mamnguqua.repository.UserRepository;
import com.giaolang.mamnguqua.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth Controller - Đăng nhập
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Đăng nhập - Trả về JWT token kèm roles
     *
     * ⭐ QUAN TRỌNG: Khi login, ta lấy roles của user từ database
     * và thêm vào JWT token
     *
     * @param request Chứa username và password
     * @return Token và thông tin user
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1️⃣ Tìm user trong database
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2️⃣ Kiểm tra password (trong thực tế phải dùng password encoder)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // ⭐ 3️⃣ LẤY ROLES CỦA USER
        // Giả sử User entity có field List<String> roles
        // Ví dụ: user.getRoles() = ["ROLE_ADMIN", "ROLE_MEMBER"]
        java.util.List<String> roles = user.getRoles();

        // ⭐ 4️⃣ TẠO JWT TOKEN VỚI ROLES
        // Token sẽ chứa: { sub: "admin", roles: ["ROLE_ADMIN"] }
        String token = jwtService.generateToken(user.getUsername(), roles);

        // 5️⃣ TRẢ VỀ RESPONSE
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roles(roles)  // ⭐ Gửi roles về cho client để client biết user có quyền gì
                .build();

        return ResponseEntity.ok(response);
    }
}
```

---

## 📝 E. FruitController.java - Thêm @PreAuthorize

```java
package com.giaolang.mamnguqua.controller;

import com.giaolang.mamnguqua.dto.FruitCreateRequest;
import com.giaolang.mamnguqua.dto.FruitResponse;
import com.giaolang.mamnguqua.service.FruitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Fruit Controller - Quản lý quả
 *
 * ⭐ @PreAuthorize là kiểm tra bổ sung trước khi vào method
 * (cách 1: cấu hình trong SecurityConfig, cách 2: dùng @PreAuthorize)
 */
@RestController
@RequestMapping("/api/fruits")
@RequiredArgsConstructor
public class FruitController {

    private final FruitService fruitService;

    /**
     * Lấy tất cả quả
     *
     * ✅ Tất cả roles (ADMIN, STAFF, MEMBER) được GET
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'MEMBER')")
    public ResponseEntity<List<FruitResponse>> getAllFruits() {
        List<FruitResponse> fruits = fruitService.getAllFruits();
        return ResponseEntity.ok(fruits);
    }

    /**
     * Lấy quả theo ID
     *
     * ✅ Tất cả roles được GET
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'MEMBER')")
    public ResponseEntity<FruitResponse> getFruitById(@PathVariable Long id) {
        FruitResponse fruit = fruitService.getFruitById(id);
        return ResponseEntity.ok(fruit);
    }

    /**
     * Tạo quả mới
     *
     * ⭐ Chỉ ADMIN được POST
     * ❌ STAFF, MEMBER không được
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FruitResponse> createFruit(@RequestBody FruitCreateRequest request) {
        FruitResponse newFruit = fruitService.createFruit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newFruit);
    }

    /**
     * Sửa quả
     *
     * ⭐ ADMIN và STAFF được PUT
     * ❌ MEMBER không được
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<FruitResponse> updateFruit(
            @PathVariable Long id,
            @RequestBody FruitCreateRequest request) {
        FruitResponse updatedFruit = fruitService.updateFruit(id, request);
        return ResponseEntity.ok(updatedFruit);
    }

    /**
     * Xóa quả
     *
     * ⭐ Chỉ ADMIN được DELETE
     * ❌ STAFF, MEMBER không được
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFruit(@PathVariable Long id) {
        fruitService.deleteFruit(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ví dụ nâng cao: Kiểm tra thêm điều kiện
     *
     * ⭐ ADMIN luôn được, hoặc user là người sáng tạo fruit
     * (Giả sử FruitService có method isCreatedBy)
     */
    @PutMapping("/{id}/special")
    @PreAuthorize("hasRole('ADMIN') or @fruitService.isCreatedBy(#id, authentication.name)")
    public ResponseEntity<FruitResponse> updateOwnFruit(
            @PathVariable Long id,
            @RequestBody FruitCreateRequest request) {
        FruitResponse updatedFruit = fruitService.updateFruit(id, request);
        return ResponseEntity.ok(updatedFruit);
    }
}
```

---

## 📝 F. User.java - Entity với roles

```java
package com.giaolang.mamnguqua.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * User Entity - Lưu trữ thông tin user
 *
 * ⭐ Thêm field roles để lưu các role của user
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    private String fullName;

    // ⭐ QUAN TRỌNG: Thêm field roles
    // @ElementCollection lưu collection trong table riêng
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",  // Tên table lưu roles
            joinColumns = @JoinColumn(name = "user_id")  // FK trỏ về user
    )
    @Column(name = "role")  // Tên column lưu role
    private List<String> roles = Arrays.asList("ROLE_MEMBER");  // Default role

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    // ... getter, setter, constructor, etc.
}
```

---

## 📝 G. LoginResponse.java - DTO

```java
package com.giaolang.mamnguqua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LoginResponse - Trả về khi login thành công
 *
 * ⭐ Thêm field roles để client biết user có quyền gì
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT token - gửi kèm mỗi request trong Authorization header
     */
    private String token;

    /**
     * Username của user
     */
    private String username;

    /**
     * ⭐ Danh sách roles của user
     * Ví dụ: ["ROLE_ADMIN", "ROLE_MEMBER"]
     * 
     * Frontend dùng để biết user có quyền gì
     */
    private List<String> roles;
}
```

---

## 🧪 TEST CASES

### Test 1: Admin login và tạo fruit

```bash
# 1️⃣ Login với admin
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}

# 2️⃣ Dùng token để POST fruit
POST http://localhost:8080/api/fruits
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "Xoài",
  "price": 20000
}

# Response: 201 Created ✅
{
  "id": 5,
  "name": "Xoài",
  "price": 20000
}
```

### Test 2: Member login và cố gắng tạo fruit

```bash
# 1️⃣ Login với member
POST http://localhost:8080/api/auth/login

{
  "username": "member1",
  "password": "member123"
}

# Response:
{
  "token": "...",
  "username": "member1",
  "roles": ["ROLE_MEMBER"]
}

# 2️⃣ Cố gắng POST fruit
POST http://localhost:8080/api/fruits
Authorization: Bearer ...

{
  "name": "Xoài",
  "price": 20000
}

# Response: 403 Forbidden ❌
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access is denied"
}
```

### Test 3: Member login và GET fruit

```bash
# 1️⃣ Login với member
# (như trên)

# 2️⃣ GET fruit
GET http://localhost:8080/api/fruits
Authorization: Bearer ...

# Response: 200 OK ✅
[
  { "id": 1, "name": "Táo", "price": 10000 },
  { "id": 2, "name": "Cam", "price": 15000 },
  { "id": 5, "name": "Xoài", "price": 20000 }
]
```

---

## 🎯 SUMMARY

```
┌──────────────────────────────────────────┐
│ JAM FLOW VỚI PHÂN QUYỀN:                 │
│                                          │
│ 1. Login                                 │
│    → JwtService.generateToken(           │
│         username,                        │
│         user.getRoles()  ⭐              │
│       )                                  │
│    → Token chứa: roles claim             │
│                                          │
│ 2. Request API                           │
│    → Authorization: Bearer <token>       │
│                                          │
│ 3. JwtAuthenticationFilter               │
│    → Extract roles từ token              │
│    → JwtService.extractAuthorities()     │
│    → SecurityContext.setAuthentication(  │
│         username,                        │
│         authorities ⭐                   │
│       )                                  │
│                                          │
│ 4. Authorization Filter                  │
│    → Kiểm tra roles vs endpoint          │
│    → .hasRole("ADMIN") ?                 │
│    → ALLOW hoặc DENY                     │
│                                          │
│ 5. Controller                            │
│    → Xử lý business logic                │
│                                          │
└──────────────────────────────────────────┘
```

✨ **Done! Bạn đã có đủ code để implement phân quyền!**

