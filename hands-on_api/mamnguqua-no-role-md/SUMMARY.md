# 🎯 TÓM TẮT - PHÂN QUYỀN TRONG SPRING SECURITY

## 📌 CẢN TRẢ LỜI CÂU HỎI CỦA BẠN

### ❓ Câu hỏi
> "Nếu code có thêm phân quyền cho API (role admin thì làm POST, DELETE, role member/staff thì gọi vài API khác), theo mạch logic thì các class khác và filter khác trong Spring Security sẽ vận hành theo flow, kịch bản gì, phần code developer sẽ cần can thiệp thêm gì?"

### ✅ Câu trả lời

---

## 🔄 FLOW QUÁ TRÌNH KHI CÓ PHÂN QUYỀN

```
┌──────────────────────────────┐
│ 1. CLIENT LOGIN              │
│    POST /api/auth/login      │
│    { username, password }    │
└────────────┬─────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 2. AuthController.login()               │
│    - Verify username/password           │
│    ✅ Get user.roles from DB            │
│    ✅ JwtService.generateToken(         │
│         username,                       │
│         roles  ⭐ THÊM ROLES VÀO TOKEN  │
│       )                                 │
└────────────┬────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 3. RESPONSE 200 OK                       │
│    {                                     │
│      "token": "...",                     │
│      "username": "admin",                │
│      "roles": ["ROLE_ADMIN"] ⭐         │
│    }                                     │
└────────────┬────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 4. CLIENT: API REQUEST                   │
│    POST /api/fruits (CREATE)             │
│    Authorization: Bearer <token>         │
│    Body: { name, price }                 │
└────────────┬────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 5. ⭐ JwtAuthenticationFilter            │
│    (Spring Security Filter #1)           │
│                                          │
│    ✅ Extract token từ header            │
│    ✅ Validate token                     │
│    ✅ Extract username: "admin"          │
│    ✅ Extract roles: ["ROLE_ADMIN"]      │
│    ✅ Create UsernamePasswordAuth...     │
│       Token(                             │
│         principal: "admin",              │
│         authorities: [ROLE_ADMIN] ⭐     │
│       )                                  │
│    ✅ SecurityContext.set(token)         │
│                                          │
│    ⭐ DEVELOPER CỐ GẮN:                 │
│    - Sửa doFilterInternal()             │
│    - Extract authorities từ token       │
│    - Set vào Authentication object      │
└────────────┬────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 6. UsernamePasswordAuthenticationFilter  │
│    (Spring Security Filter #2)           │
│                                          │
│    ✅ Check: Auth exists?                │
│    ✅ YES → SKIP (user đã auth)          │
│                                          │
│    (Không cần developer can thiệp)       │
└────────────┬────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 7. ⭐ Authorization Filter               │
│    (Spring Security Filter #3)           │
│                                          │
│    ✅ Check endpoint: POST /api/fruits   │
│    ✅ Check requirement: .hasRole()      │
│    ✅ Get user's roles from Context      │
│    ✅ Compare: ROLE_ADMIN == ROLE_ADMIN? │
│    ✅ YES → ALLOW                        │
│    ❌ NO → DENY 403 Forbidden            │
│                                          │
│    ⭐ DEVELOPER CỐ GẮN:                 │
│    - Config .hasRole() trong            │
│      SecurityConfig.securityFilterChain │
│    - Quy định quyền cho từng endpoint   │
└────────────┬────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 8. (Nếu ALLOW) FruitController.create() │
│                                          │
│    ✅ Business logic                     │
│    ✅ Validate, save to DB               │
│    ✅ Return response                    │
│                                          │
│    ⭐ DEVELOPER CÓ THỂ THÊM:            │
│    - @PreAuthorize() kiểm tra thêm      │
│    - Advanced authorization checks      │
└────────────┬────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 9. RESPONSE                              │
│    ✅ 201 Created (nếu ALLOW)           │
│    ❌ 403 Forbidden (nếu DENY)          │
└──────────────────────────────────────────┘
```

---

## 🛠️ PHẦN CODE DEVELOPER CẦN CAN THIỆP

### 1️⃣ JwtService.java - Thêm 2 methods

```java
// Extract roles từ token
public List<String> extractRoles(String token) {
    return extractAllClaims(token).get("roles", List.class);
}

// Convert roles → GrantedAuthority (dùng cho Spring Security)
public List<GrantedAuthority> extractAuthorities(String token) {
    return extractRoles(token).stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
}

// Sửa generateToken để thêm roles
public String generateToken(String username, List<String> roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);  // ⭐ THÊM ROLES VÀO TOKEN
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        // ... rest of code
}
```

### 2️⃣ JwtAuthenticationFilter.java - Sửa doFilterInternal()

```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    // ... existing code ...
    
    String username = jwtService.extractUsername(token);
    
    // ⭐ THÊM PHẦN NÀY:
    List<GrantedAuthority> authorities = jwtService.extractAuthorities(token);
    
    // ⭐ SỬA TẠO TOKEN:
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            username,
            null,
            authorities  // ⭐ THÊM AUTHORITIES
        );
    
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

### 3️⃣ SecurityConfig.java - Cấu hình phân quyền

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // ⭐ THÊM PHẦN NÀY:
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/auth/login").permitAll()
            
            // Phân quyền theo role
            .requestMatchers(HttpMethod.POST, "/api/fruits/**")
                .hasRole("ADMIN")  // ⭐ Chỉ ADMIN
            
            .requestMatchers(HttpMethod.DELETE, "/api/fruits/**")
                .hasRole("ADMIN")  // ⭐ Chỉ ADMIN
            
            .requestMatchers(HttpMethod.PUT, "/api/fruits/**")
                .hasAnyRole("ADMIN", "STAFF")  // ⭐ ADMIN hoặc STAFF
            
            .requestMatchers(HttpMethod.GET, "/api/fruits/**")
                .hasAnyRole("ADMIN", "STAFF", "MEMBER")  // ⭐ Tất cả
            
            .anyRequest().authenticated())
        
        .addFilterBefore(jwtAuthenticationFilter, 
            UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

### 4️⃣ AuthController.java - Lấy roles khi login

```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    if (!user.getPassword().equals(request.getPassword())) {
        throw new RuntimeException("Invalid password");
    }
    
    // ⭐ THÊM PHẦN NÀY:
    List<String> roles = user.getRoles();  // Get roles từ DB
    String token = jwtService.generateToken(user.getUsername(), roles);  // Thêm roles vào token
    
    return ResponseEntity.ok(LoginResponse.builder()
        .token(token)
        .username(user.getUsername())
        .roles(roles)  // ⭐ Gửi roles về client
        .build());
}
```

### 5️⃣ User Entity.java - Thêm roles field

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    // ⭐ THÊM FIELD NÀY:
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    private List<String> roles = Arrays.asList("ROLE_MEMBER");  // Default
}
```

### 6️⃣ LoginResponse.java - Thêm roles field

```java
@Data
public class LoginResponse {
    private String token;
    private String username;
    private List<String> roles;  // ⭐ THÊM FIELD NÀY
}
```

### 7️⃣ FruitController.java (Optional) - Thêm @PreAuthorize

```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'MEMBER')")
public ResponseEntity<List<FruitResponse>> getAllFruits() { ... }

@PostMapping
@PreAuthorize("hasRole('ADMIN')")  // ⭐ THÊM ANNOTATION NÀY
public ResponseEntity<FruitResponse> createFruit(...) { ... }

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")  // ⭐ THÊM ANNOTATION NÀY
public ResponseEntity<Void> deleteFruit(@PathVariable Long id) { ... }
```

---

## 🎬 KỊCH BẢN VẬN HÀNH

### ✅ Kịch bản 1: Admin POST Fruit
```
1. Admin login → Get token với roles: ["ROLE_ADMIN"]
2. Admin gửi POST /api/fruits với token
3. JwtAuthenticationFilter:
   - Extract authorities: [ROLE_ADMIN]
   - Set vào SecurityContext
4. Authorization Filter:
   - Check: POST /api/fruits yêu cầu .hasRole("ADMIN")
   - User có [ROLE_ADMIN]
   - ✅ MATCH → ALLOW
5. FruitController.create() xử lý
6. Response: 201 Created ✅
```

### ❌ Kịch bản 2: Member POST Fruit
```
1. Member login → Get token với roles: ["ROLE_MEMBER"]
2. Member gửi POST /api/fruits với token
3. JwtAuthenticationFilter:
   - Extract authorities: [ROLE_MEMBER]
   - Set vào SecurityContext
4. Authorization Filter:
   - Check: POST /api/fruits yêu cầu .hasRole("ADMIN")
   - User có [ROLE_MEMBER]
   - ❌ NO MATCH → DENY
5. Exception Handler bắt AccessDeniedException
6. Response: 403 Forbidden ❌
```

### ✅ Kịch bản 3: Member GET Fruits
```
1. Member login → Get token với roles: ["ROLE_MEMBER"]
2. Member gửi GET /api/fruits với token
3. JwtAuthenticationFilter:
   - Extract authorities: [ROLE_MEMBER]
   - Set vào SecurityContext
4. Authorization Filter:
   - Check: GET /api/fruits yêu cầu .hasAnyRole("ADMIN", "STAFF", "MEMBER")
   - User có [ROLE_MEMBER]
   - ✅ MATCH → ALLOW
5. FruitController.getAll() xử lý
6. Response: 200 OK với danh sách fruits ✅
```

---

## 📊 BẢNG TÓMLẠI: DEVELOPER PHẢI CAN THIỆP Ở ĐÂU

| File | Method/Class | Công việc | Khó độ |
|------|--------------|----------|--------|
| JwtService.java | extractRoles() | Lấy roles từ token | ⭐ Dễ |
| JwtService.java | extractAuthorities() | Convert roles → GrantedAuthority | ⭐ Dễ |
| JwtService.java | generateToken() | Thêm roles vào token | ⭐ Dễ |
| JwtAuthenticationFilter.java | doFilterInternal() | Set authorities vào Authentication | ⭐⭐ Trung bình |
| SecurityConfig.java | securityFilterChain() | Cấu hình .hasRole(), .hasAnyRole() | ⭐⭐ Trung bình |
| AuthController.java | login() | Get roles, pass vào generateToken() | ⭐ Dễ |
| User.java | Entity | Thêm field List<String> roles | ⭐ Dễ |
| LoginResponse.java | DTO | Thêm field roles | ⭐ Dễ |
| FruitController.java | Methods | Thêm @PreAuthorize (optional) | ⭐ Dễ |

---

## 🔑 KEY POINTS

### 🎯 Bản chất của phân quyền:

1. **Authentication (JWT Filter)** → Tạo Authentication object với authorities/roles
2. **Authorization (Built-in Filter)** → Kiểm tra roles của user có khớp endpoint requirement không
3. **SecurityContext** → Nơi lưu trữ toàn bộ user info & roles

### ⚡ Flow hoạt động:

```
JWT Token (chứa roles)
    ↓
JwtAuthenticationFilter (Extract & set authorities)
    ↓
SecurityContext (lưu user info + roles)
    ↓
Authorization Filter (Check roles vs requirement)
    ↓
ALLOW (✅ 200/201) hoặc DENY (❌ 403)
```

### 💡 Roles format:

- Must start with "ROLE_" prefix (Spring Security convention)
- Examples: "ROLE_ADMIN", "ROLE_STAFF", "ROLE_MEMBER"
- Stored in database as List<String>
- Added to JWT token as "roles" claim

---

## 🚀 BƯỚC TIẾP THEO

1. ✅ **Đã sửa SecurityConfig.java** - Ghi chú đã cập nhật
2. 📝 **Tạo 4 documents hướng dẫn:**
   - `AUTHORIZATION_FLOW_GUIDE.md` - Chi tiết flow
   - `FILTER_CHAIN_EXECUTION_FLOW.md` - Trình tự filter
   - `CODE_EXAMPLES_AUTHORIZATION.md` - Code mẫu
   - `VISUAL_DIAGRAMS.md` - Sơ đồ minh họa
   - `IMPLEMENTATION_CHECKLIST.md` - Checklist làm việc

3. 🛠️ **Developer cần làm:**
   - Thêm 2 methods vào JwtService
   - Sửa JwtAuthenticationFilter
   - Sửa SecurityConfig (phân quyền)
   - Sửa AuthController (lấy roles)
   - Thêm roles field vào User entity
   - Thêm roles field vào LoginResponse
   - (Optional) Thêm @PreAuthorize vào controller

4. 🧪 **Test:**
   - Login với các role khác nhau
   - Test POST, PUT, DELETE, GET
   - Verify 403 Forbidden khi không đủ quyền
   - Verify 401 Unauthorized khi không có token

---

## 📚 TÀI LIỆU THAM KHẢO

Tất cả files hướng dẫn đã được tạo trong project:
- 📄 AUTHORIZATION_FLOW_GUIDE.md
- 📄 FILTER_CHAIN_EXECUTION_FLOW.md
- 📄 CODE_EXAMPLES_AUTHORIZATION.md
- 📄 VISUAL_DIAGRAMS.md
- 📄 IMPLEMENTATION_CHECKLIST.md

Mở chúng để xem chi tiết từng phần! 🎯

---

## ✨ KẾT LUẬN

**Spring Security tự động xử lý hầu hết công việc**, developer chỉ cần:
1. **Set authorities vào Authentication object** (JwtAuthenticationFilter)
2. **Cấu hình rule phân quyền** (SecurityConfig)

Phần còn lại (Authorization Filter, Exception Handler, etc.) **Spring Security tự động chạy**, không cần can thiệp! 🚀

