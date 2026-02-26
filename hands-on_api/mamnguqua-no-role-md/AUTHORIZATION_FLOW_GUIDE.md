# 🔐 HƯỚNG DẪN PHÂN QUYỀN (AUTHORIZATION) TRONG SPRING SECURITY

## 1️⃣ PHÂN BIỆT: Authentication vs Authorization

| Khái niệm | Ý nghĩa |
|-----------|---------|
| **Authentication** | Xác thực - "Bạn là ai?" (kiểm tra username/password hoặc JWT token) |
| **Authorization** | Phân quyền - "Bạn được làm gì?" (kiểm tra role/permission) |

---

## 2️⃣ KIẾN TRÚC PHÂN QUYỀN TRONG SPRING SECURITY

```
┌────────────────────────────────────────────────────────────────┐
│                         HTTP Request                           │
│              Authorization: Bearer <jwt_token>                 │
└─────────────────────────────┬──────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│            JwtAuthenticationFilter (Xác thực - Authentication) │
│                                                                │
│  1. Extract JWT token từ header                               │
│  2. Validate token                                            │
│  3. Extract username từ token                                 │
│  4. ⭐ QUAN TRỌNG: Extract ROLES/AUTHORITIES từ token        │
│     (ví dụ: ROLE_ADMIN, ROLE_MEMBER, ROLE_STAFF)             │
│  5. Tạo UsernamePasswordAuthenticationToken với:              │
│     - principal: username                                     │
│     - authorities: [ROLE_ADMIN] hoặc [ROLE_MEMBER]            │
│  6. Đặt vào SecurityContext                                   │
└─────────────────────────────┬──────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│       Authorization Filter (Phân quyền - Authorization)       │
│                                                                │
│  1. Kiểm tra @PreAuthorize, @Secured annotations              │
│     (hoặc .authorizeHttpRequests() config trong SecurityConfig)
│  2. Lấy Authentication từ SecurityContext                     │
│  3. Kiểm tra getAuthorities() = [ROLE_ADMIN]?                │
│  4. So sánh với yêu cầu của endpoint:                         │
│     - POST /api/fruits → @PreAuthorize("hasRole('ADMIN')")   │
│     - GET  /api/fruits → @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
│  5. Nếu khớp → ALLOW ✅                                       │
│     Nếu không khớp → DENY 403 Forbidden ❌                    │
└─────────────────────────────┬──────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                    Controller Handler                         │
│              (Xử lý business logic)                           │
└────────────────────────────────────────────────────────────────┘
```

---

## 3️⃣ FLOW CHI TIẾT TRONG ỨNG DỤNG CÓ PHÂN QUYỀN

### 📋 Kịch bản 1: Admin gọi POST /api/fruits (tạo quả)

```
1. Admin login: POST /api/auth/login
   ├─ Request: { username: "admin", password: "123456" }
   ├─ AuthController.login() xử lý
   ├─ Kiểm tra password ✅
   └─ JWT Service tạo token chứa: { sub: "admin", roles: ["ROLE_ADMIN"] }
   
2. Admin gửi request: POST /api/fruits
   ├─ Authorization: Bearer <jwt_token_admin>
   ├─ JwtAuthenticationFilter chạy:
   │  ├─ Extract token từ header
   │  ├─ Validate token ✅
   │  ├─ Extract username: "admin"
   │  ├─ Extract roles từ token: ["ROLE_ADMIN"] ⭐
   │  ├─ Tạo UsernamePasswordAuthenticationToken:
   │  │  - principal: "admin"
   │  │  - authorities: [SimpleGrantedAuthority("ROLE_ADMIN")]
   │  └─ SecurityContext.setAuthentication(token) ✅
   │
   ├─ UsernamePasswordAuthenticationFilter chạy
   │  └─ Thấy đã authenticated → SKIP
   │
   ├─ Authorization Filter chạy:
   │  ├─ Lấy Authentication từ SecurityContext
   │  ├─ Kiểm tra @PreAuthorize("hasRole('ADMIN')") trên POST /api/fruits
   │  ├─ getAuthorities() = [ROLE_ADMIN] ✅ MATCH!
   │  └─ ALLOW ✅
   │
   └─ FruitController.createFruit() xử lý ✅

3. Response: 201 Created
```

### 📋 Kịch bản 2: Member gọi POST /api/fruits (tạo quả)

```
1. Member gửi request: POST /api/fruits
   ├─ Authorization: Bearer <jwt_token_member>
   ├─ JwtAuthenticationFilter chạy:
   │  ├─ Extract token từ header
   │  ├─ Validate token ✅
   │  ├─ Extract username: "member1"
   │  ├─ Extract roles từ token: ["ROLE_MEMBER"] ⭐
   │  ├─ Tạo UsernamePasswordAuthenticationToken:
   │  │  - principal: "member1"
   │  │  - authorities: [SimpleGrantedAuthority("ROLE_MEMBER")]
   │  └─ SecurityContext.setAuthentication(token) ✅
   │
   ├─ Authorization Filter chạy:
   │  ├─ Lấy Authentication từ SecurityContext
   │  ├─ Kiểm tra @PreAuthorize("hasRole('ADMIN')") trên POST /api/fruits
   │  ├─ getAuthorities() = [ROLE_MEMBER] ❌ KHÔNG MATCH!
   │  └─ DENY 403 Forbidden ❌
   │
   └─ Request bị chặn, không đến controller

2. Response: 403 Forbidden
   {
     "timestamp": "2026-02-21T10:30:00",
     "status": 403,
     "error": "Forbidden",
     "message": "Access Denied: User does not have required role"
   }
```

### 📋 Kịch bản 3: Member gọi GET /api/fruits (xem danh sách)

```
1. Member gửi request: GET /api/fruits
   ├─ Authorization: Bearer <jwt_token_member>
   ├─ JwtAuthenticationFilter chạy:
   │  ├─ Extract token, validate ✅
   │  ├─ Extract username: "member1"
   │  ├─ Extract roles: ["ROLE_MEMBER"] ⭐
   │  └─ SecurityContext.setAuthentication(token) ✅
   │
   ├─ Authorization Filter chạy:
   │  ├─ Kiểm tra @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')") trên GET /api/fruits
   │  ├─ getAuthorities() = [ROLE_MEMBER] ✅ MATCH!
   │  └─ ALLOW ✅
   │
   └─ FruitController.getAllFruits() xử lý ✅

2. Response: 200 OK
   [
     { "id": 1, "name": "Táo", "price": 10000 },
     { "id": 2, "name": "Cam", "price": 15000 }
   ]
```

---

## 4️⃣ CODE CẦN THÊM/THAY ĐỔI

### 📝 A. JWT Service - Extract Roles từ Token

```java
// Trong JwtService.java - Thêm method mới

public List<String> extractRoles(String token) {
    return extractAllClaims(token)
            .get("roles", List.class);  // Lấy "roles" claim từ token
}

// Hoặc nếu dùng Spring Security GrantedAuthority:
public List<GrantedAuthority> extractAuthorities(String token) {
    List<String> roles = extractRoles(token);
    return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role))
            .collect(Collectors.toList());
}

// Khi tạo token (trong login):
public String generateToken(String username, List<String> roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);  // ⭐ Thêm roles vào token
    
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))  // 24 hours
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}
```

### 📝 B. JwtAuthenticationFilter - Tạo Authentication với Roles

```java
// Trong JwtAuthenticationFilter.java - Sửa doFilterInternal()

@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
    try {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtService.isValidToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String username = jwtService.extractUsername(token);
        if (username == null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // ⭐ QUAN TRỌNG: Extract roles/authorities từ token
        List<GrantedAuthority> authorities = jwtService.extractAuthorities(token);
        
        // Tạo Authentication với authorities (roles)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,           // principal (ai)
                        null,               // credentials
                        authorities         // ⭐ ROLES/AUTHORITIES (quyền gì)
                );
        
        // Đặt vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authentication set for user: {} with roles: {}", username, authorities);
        
        filterChain.doFilter(request, response);
        
    } catch (Exception e) {
        log.error("Error in JWT authentication filter: {}", e.getMessage());
        filterChain.doFilter(request, response);
    }
}
```

### 📝 C. SecurityConfig - Cấu hình phân quyền theo endpoint

```java
// Trong SecurityConfig.java - securityFilterChain()

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    
                    // ⭐ PHÂN QUYỀN THEO ENDPOINT:
                    
                    // POST /api/fruits - Chỉ ADMIN được tạo
                    .requestMatchers(HttpMethod.POST, "/api/fruits/**")
                            .hasRole("ADMIN")
                    
                    // DELETE /api/fruits - Chỉ ADMIN được xóa
                    .requestMatchers(HttpMethod.DELETE, "/api/fruits/**")
                            .hasRole("ADMIN")
                    
                    // PUT /api/fruits - ADMIN và STAFF được sửa
                    .requestMatchers(HttpMethod.PUT, "/api/fruits/**")
                            .hasAnyRole("ADMIN", "STAFF")
                    
                    // GET /api/fruits - Tất cả role được xem
                    .requestMatchers(HttpMethod.GET, "/api/fruits/**")
                            .hasAnyRole("ADMIN", "MEMBER", "STAFF")
                    
                    // Tất cả endpoint khác phải authenticated
                    .anyRequest().authenticated())
            
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

### 📝 D. Controller - Dùng @PreAuthorize (Method-level)

```java
// Trong FruitController.java - Sử dụng @PreAuthorize annotation

@RestController
@RequestMapping("/api/fruits")
@RequiredArgsConstructor
public class FruitController {
    
    // GET - Tất cả role
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'STAFF')")
    public ResponseEntity<List<FruitResponse>> getAllFruits() {
        // ...
    }
    
    // POST - Chỉ ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FruitResponse> createFruit(@RequestBody FruitCreateRequest request) {
        // ...
    }
    
    // PUT - ADMIN và STAFF
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<FruitResponse> updateFruit(
            @PathVariable Long id,
            @RequestBody FruitCreateRequest request) {
        // ...
    }
    
    // DELETE - Chỉ ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFruit(@PathVariable Long id) {
        // ...
    }
    
    // Advanced: Kiểm tra quyền trên object cụ thể
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @fruitService.isOwner(#id, authentication.name)")
    public ResponseEntity<FruitResponse> updateOwnFruit(
            @PathVariable Long id,
            @RequestBody FruitCreateRequest request) {
        // ...
    }
}
```

### 📝 E. AuthController - Thêm roles khi login

```java
// Trong AuthController.java

@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    // Kiểm tra username/password
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    if (!user.getPassword().equals(request.getPassword())) {
        throw new RuntimeException("Invalid password");
    }
    
    // ⭐ Lấy roles của user
    List<String> roles = user.getRoles();  // ["ROLE_ADMIN"]
    
    // Tạo JWT token chứa roles
    String token = jwtService.generateToken(user.getUsername(), roles);
    
    return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), roles));
}
```

### 📝 F. Entity - Thêm roles cho User

```java
// Trong User.java hoặc UserEntity.java

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
    
    // ⭐ Thêm roles
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = Arrays.asList("ROLE_MEMBER");  // Default role
    
    private String email;
    private String fullName;
    // ... các field khác
}
```

### 📝 G. LoginResponse - Thêm roles

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private List<String> roles;  // ⭐ Thêm field này
}
```

---

## 5️⃣ FLOW TỔNG QUÁT CÓ PHÂN QUYỀN

```
┌─────────────────────────────────────────────────────────────────────┐
│  CLIENT REQUEST                                                     │
│  POST /api/fruits                                                   │
│  Authorization: Bearer eyJhbGciOi...                               │
└────────────────────────────┬────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  1️⃣  JwtAuthenticationFilter                                         │
│                                                                      │
│  ✅ Extract & validate token                                       │
│  ✅ Extract username: "admin"                                      │
│  ✅ Extract roles: ["ROLE_ADMIN"] ⭐                               │
│  ✅ Create UsernamePasswordAuthenticationToken(                    │
│       principal="admin",                                           │
│       authorities=[ROLE_ADMIN]                                     │
│     )                                                              │
│  ✅ SecurityContext.setAuthentication(token)                       │
└────────────────────────────┬────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  2️⃣  UsernamePasswordAuthenticationFilter                            │
│                                                                      │
│  ⏭️  SKIP (user đã authenticated)                                  │
└────────────────────────────┬────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  3️⃣  Authorization Filter (⭐ Phân quyền)                            │
│                                                                      │
│  📋 Kiểm tra endpoint: POST /api/fruits                            │
│  📋 Yêu cầu: @PreAuthorize("hasRole('ADMIN')")                     │
│  📋 Lấy authorities: [ROLE_ADMIN]                                   │
│  📋 So sánh: ROLE_ADMIN == ROLE_ADMIN? ✅ YES!                     │
│  ✅ ALLOW - Request đi tiếp                                       │
└────────────────────────────┬────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  4️⃣  FruitController.createFruit()                                  │
│                                                                      │
│  ✅ Xử lý business logic                                           │
│  ✅ Tạo quả mới                                                    │
└────────────────────────────┬────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  RESPONSE: 201 Created                                              │
│  {                                                                  │
│    "id": 5,                                                         │
│    "name": "Xoài",                                                  │
│    "price": 20000                                                   │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6️⃣ BẢNG TÓMLẠI: CODE CẦN CAN THIỆP

| Phần | File | Thay đổi |
|------|------|----------|
| **JWT Service** | `JwtService.java` | Thêm method `extractRoles()`, `extractAuthorities()` |
| **JWT Filter** | `JwtAuthenticationFilter.java` | Sửa `doFilterInternal()` để extract & set authorities |
| **Security Config** | `SecurityConfig.java` | Cấu hình `.hasRole()` và `.hasAnyRole()` cho từng endpoint |
| **Controller** | `FruitController.java` | Thêm `@PreAuthorize` trên từng method |
| **Auth Controller** | `AuthController.java` | Lấy roles khi login, gửi JWT token với roles |
| **LoginResponse** | `LoginResponse.java` | Thêm field `roles` để client biết roles của user |
| **User Entity** | `User.java` hoặc `UserEntity.java` | Thêm field `@ElementCollection List<String> roles` |

---

## 7️⃣ ÁP DỤNG VÀO ỨNG DỤNG CỦA BẠN

### 🎯 Ví dụ hoàn chỉnh cho Fruit API:

```java
// Ứng dụng hiện tại có 3 loại user:
// 1. ROLE_ADMIN  - POST, PUT, DELETE (CRUD đầy đủ)
// 2. ROLE_STAFF  - PUT (chỉnh sửa)
// 3. ROLE_MEMBER - GET (chỉ xem)

// Cấu hình:
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            // ... CORS, CSRF, Session ...
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/fruits/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/fruits/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/fruits/**").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers(HttpMethod.GET, "/api/fruits/**").hasAnyRole("ADMIN", "STAFF", "MEMBER")
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

---

## 📚 TÓMLẠI:

1. **Authentication** (JwtAuthenticationFilter) → Extract roles từ token, set vào SecurityContext
2. **Authorization** (Authorization Filter) → Kiểm tra roles của user có khớp với endpoint không
3. **Developer cần thêm:**
   - Extract roles từ JWT token
   - Cấu hình role-based access control trong SecurityConfig
   - Thêm @PreAuthorize trên controller methods
   - Lưu roles trong User entity
   - Thêm roles vào JWT token khi login

✨ **Kết quả:** Ứng dụng có thể kiểm soát quyền truy cập chi tiết dựa trên role của user!

