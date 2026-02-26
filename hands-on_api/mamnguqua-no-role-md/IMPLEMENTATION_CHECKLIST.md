# ✅ IMPLEMENTATION CHECKLIST - PHÂN QUYỀN TRONG SPRING SECURITY

## 📋 DANH SÁCH CÔNG VIỆC

### Phase 1: Chuẩn bị Database
- [ ] **Thêm field roles vào User Entity**
  - [ ] Tạo Entity class `User` nếu chưa có
  - [ ] Thêm field: `@ElementCollection List<String> roles`
  - [ ] Default role: `"ROLE_MEMBER"`
  - [ ] Áp dụng migration database

- [ ] **Tạo User data trong database**
  - [ ] INSERT admin user với roles: ["ROLE_ADMIN"]
  - [ ] INSERT staff user với roles: ["ROLE_STAFF"]
  - [ ] INSERT member user với roles: ["ROLE_MEMBER"]

---

### Phase 2: Update JwtService
- [ ] **Thêm method extractRoles()**
  - [ ] Lấy "roles" claim từ JWT token
  - [ ] Return `List<String>`
  - [ ] Handle trường hợp roles không tồn tại

- [ ] **Thêm method extractAuthorities()**
  - [ ] Call `extractRoles(token)`
  - [ ] Convert từng role string → `SimpleGrantedAuthority`
  - [ ] Return `List<GrantedAuthority>`

- [ ] **Sửa method generateToken()**
  - [ ] Thêm tham số `List<String> roles`
  - [ ] Add roles vào claims: `claims.put("roles", roles)`
  - [ ] Build token với roles claim

---

### Phase 3: Update JwtAuthenticationFilter
- [ ] **Sửa doFilterInternal() method**
  - [ ] Sau `extractUsername()`, thêm `extractAuthorities()`
  - [ ] Lấy authorities từ token:
    ```java
    List<GrantedAuthority> authorities = jwtService.extractAuthorities(token);
    ```
  - [ ] Tạo UsernamePasswordAuthenticationToken với authorities:
    ```java
    new UsernamePasswordAuthenticationToken(
        username,
        null,
        authorities  // ⭐ IMPORTANT
    )
    ```
  - [ ] Set vào SecurityContext

- [ ] **Thêm logging**
  - [ ] Log extracted authorities
  - [ ] Log authentication setup

---

### Phase 4: Update SecurityConfig
- [ ] **Sửa securityFilterChain() method**
  - [ ] Thêm phân quyền cho POST endpoint:
    ```java
    .requestMatchers(HttpMethod.POST, "/api/fruits/**").hasRole("ADMIN")
    ```
  - [ ] Thêm phân quyền cho DELETE endpoint:
    ```java
    .requestMatchers(HttpMethod.DELETE, "/api/fruits/**").hasRole("ADMIN")
    ```
  - [ ] Thêm phân quyền cho PUT endpoint:
    ```java
    .requestMatchers(HttpMethod.PUT, "/api/fruits/**").hasAnyRole("ADMIN", "STAFF")
    ```
  - [ ] Thêm phân quyền cho GET endpoint:
    ```java
    .requestMatchers(HttpMethod.GET, "/api/fruits/**").hasAnyRole("ADMIN", "STAFF", "MEMBER")
    ```

---

### Phase 5: Update AuthController
- [ ] **Sửa login() method**
  - [ ] Lấy user từ database: `userRepository.findByUsername()`
  - [ ] Verify password
  - [ ] Get user roles: `user.getRoles()`
  - [ ] Call JwtService.generateToken() với roles:
    ```java
    String token = jwtService.generateToken(username, roles);
    ```
  - [ ] Return LoginResponse với roles field:
    ```java
    return LoginResponse.builder()
        .token(token)
        .username(username)
        .roles(roles)  // ⭐ IMPORTANT
        .build();
    ```

---

### Phase 6: Update DTOs
- [ ] **Sửa LoginResponse class**
  - [ ] Thêm field: `private List<String> roles;`
  - [ ] Add getter/setter (hoặc dùng @Data)
  - [ ] Update constructor/builder

---

### Phase 7: Update Controller Methods
- [ ] **Add @PreAuthorize annotations (optional)**
  - [ ] GET endpoints: `@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'MEMBER')")`
  - [ ] POST endpoints: `@PreAuthorize("hasRole('ADMIN')")`
  - [ ] PUT endpoints: `@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")`
  - [ ] DELETE endpoints: `@PreAuthorize("hasRole('ADMIN')")`

---

### Phase 8: Enable @PreAuthorize (Optional)
- [ ] **Add @EnableGlobalMethodSecurity annotation**
  - [ ] File: `SecurityConfig.java` hoặc riêng config file
  - [ ] Code:
    ```java
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class MethodSecurityConfig {
    }
    ```

---

### Phase 9: Testing
- [ ] **Test case: Admin login**
  - [ ] Call POST /api/auth/login với admin credentials
  - [ ] Verify response có token + roles: ["ROLE_ADMIN"]
  - [ ] Try POST /api/fruits → ✅ Should return 201 Created

- [ ] **Test case: Admin DELETE**
  - [ ] Use admin token
  - [ ] Call DELETE /api/fruits/{id}
  - [ ] ✅ Should return 204 No Content

- [ ] **Test case: Member login**
  - [ ] Call POST /api/auth/login với member credentials
  - [ ] Verify response có roles: ["ROLE_MEMBER"]
  - [ ] Try POST /api/fruits → ❌ Should return 403 Forbidden

- [ ] **Test case: Member GET**
  - [ ] Use member token
  - [ ] Call GET /api/fruits
  - [ ] ✅ Should return 200 OK with fruits list

- [ ] **Test case: Staff PUT**
  - [ ] Use staff token
  - [ ] Call PUT /api/fruits/{id}
  - [ ] ✅ Should return 200 OK

- [ ] **Test case: Staff POST**
  - [ ] Use staff token
  - [ ] Call POST /api/fruits
  - [ ] ❌ Should return 403 Forbidden

- [ ] **Test case: No token**
  - [ ] Call GET /api/fruits WITHOUT Authorization header
  - [ ] ❌ Should return 401 Unauthorized

- [ ] **Test case: Invalid token**
  - [ ] Call GET /api/fruits with invalid token
  - [ ] ❌ Should return 401 Unauthorized

---

## 📊 DEPENDENCIES

Đảm bảo project có các dependencies:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 🔧 CONFIGURATION

Thêm vào `application.properties`:

```properties
# JWT Configuration
jwt.secret=YourLongSecretKeyHere1234567890123456789
jwt.expiration=86400000

# Logging
logging.level.com.giaolang.mamnguqua.security=DEBUG
```

---

## 🚀 QUICK START - TÓMLẠI CÔNG VIỆC

| Bước | File | Thay đổi | ✅/❌ |
|------|------|---------|--------|
| 1 | User Entity | Thêm `List<String> roles` field | [ ] |
| 2 | Database | INSERT user data with roles | [ ] |
| 3 | JwtService | Thêm `extractRoles()`, `extractAuthorities()` | [ ] |
| 4 | JwtService | Sửa `generateToken()` để thêm roles | [ ] |
| 5 | JwtAuthenticationFilter | Extract & set authorities | [ ] |
| 6 | SecurityConfig | Cấu hình `.hasRole()`, `.hasAnyRole()` | [ ] |
| 7 | AuthController | Get roles, pass to generateToken() | [ ] |
| 8 | LoginResponse | Thêm field `roles` | [ ] |
| 9 | FruitController | Add @PreAuthorize annotations | [ ] |
| 10 | Test | Chạy test cases, verify phân quyền | [ ] |

---

## 🧪 QUICK TEST COMMANDS

### Test Login
```bash
# Admin login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Response:
{
  "token": "eyJhbGciOi...",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### Test POST (Create) - Admin
```bash
# Admin create fruit
TOKEN="<paste_token_here>"
curl -X POST http://localhost:8080/api/fruits \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Xoài","price":20000}'

# Response: 201 Created ✅
```

### Test POST (Create) - Member
```bash
# Member login first
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"member1","password":"member123"}'

# Get token, then try POST
TOKEN="<token_from_member>"
curl -X POST http://localhost:8080/api/fruits \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Xoài","price":20000}'

# Response: 403 Forbidden ❌
```

### Test GET - Member
```bash
# Member get fruits
TOKEN="<member_token>"
curl -X GET http://localhost:8080/api/fruits \
  -H "Authorization: Bearer $TOKEN"

# Response: 200 OK ✅
```

---

## 📚 REFERENCE DOCUMENTS

- ✅ `AUTHORIZATION_FLOW_GUIDE.md` - Chi tiết về flow phân quyền
- ✅ `FILTER_CHAIN_EXECUTION_FLOW.md` - Trình tự chạy filter
- ✅ `CODE_EXAMPLES_AUTHORIZATION.md` - Code mẫu hoàn chỉnh
- ✅ `VISUAL_DIAGRAMS.md` - Các sơ đồ minh họa

---

## 🎯 NGUYÊN TẮC QUAN TRỌNG

1. **Authentication (xác thực)** → JwtAuthenticationFilter
   - Extract username & roles từ token
   - Set Authentication object vào SecurityContext

2. **Authorization (phân quyền)** → Authorization Filter
   - Kiểm tra user's roles có khớp endpoint requirement không
   - ALLOW hoặc DENY (403)

3. **SecurityContext** - Nơi lưu trữ toàn bộ thông tin user
   - Mọi filter đều truy cập SecurityContext
   - `SecurityContextHolder.getContext().getAuthentication()`

4. **Authorities** - Là representation của roles trong Spring Security
   - String "ROLE_ADMIN" → `SimpleGrantedAuthority("ROLE_ADMIN")`
   - Phải bắt đầu với "ROLE_" prefix

---

## 🔐 SECURITY BEST PRACTICES

1. ✅ **Hash password** - Không lưu plain text password
   ```java
   // Use PasswordEncoder
   String encodedPassword = passwordEncoder.encode(rawPassword);
   ```

2. ✅ **HTTPS** - Luôn dùng HTTPS trong production

3. ✅ **JWT expiration** - Set thời gian hết hạn token
   ```properties
   jwt.expiration=3600000  # 1 hour
   ```

4. ✅ **Refresh token** - Implement refresh token mechanism (nâng cao)

5. ✅ **CORS** - Cấu hình CORS đúng cách để tránh XSS/CSRF

6. ✅ **Logging** - Log toàn bộ access & authorization events

---

## 📞 COMMON ISSUES & SOLUTIONS

### Issue 1: 401 Unauthorized dù có valid token
**Nguyên nhân:** JwtService method không được inject đúng
**Giải pháp:** Kiểm tra @Autowired, @RequiredArgsConstructor

### Issue 2: 403 Forbidden dù user có role
**Nguyên nhân:** Role string không khớp (VD: "ADMIN" vs "ROLE_ADMIN")
**Giải pháp:** Đảm bảo role bắt đầu bằng "ROLE_"

### Issue 3: @PreAuthorize không hoạt động
**Nguyên nhân:** Chưa enable global method security
**Giải pháp:** Thêm `@EnableGlobalMethodSecurity(prePostEnabled = true)`

### Issue 4: Token không chứa roles
**Nguyên nhân:** generateToken() không thêm roles vào claims
**Giải pháp:** Kiểm tra `claims.put("roles", roles)`

### Issue 5: NullPointerException lấy roles từ token
**Nguyên nhân:** Token của user cũ không có roles claim
**Giải pháp:** User phải login lại để lấy token mới

---

Chúc bạn implement thành công! 🚀✨

