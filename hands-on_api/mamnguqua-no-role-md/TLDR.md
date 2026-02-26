# ⚡ TLDR (Too Long; Didn't Read) - 2 PHÚT SUMMARY

## ❓ Câu hỏi của bạn:
> Nếu code có phân quyền (admin POST/DELETE, member xem), flow nào chạy, cần can thiệp code ở đâu?

## ✅ Câu trả lời ngắn gọn:

### 🔄 Flow:
```
1. Client login → nhận JWT token (chứa roles)
2. Client gửi API request + token
3. JwtAuthenticationFilter:
   - Extract roles từ token
   - Set vào SecurityContext
4. Authorization Filter (tự động):
   - Check: user's roles vs endpoint requirement
   - ALLOW (200) hoặc DENY (403)
5. Controller xử lý (nếu ALLOW)
```

### 💻 Code cần thêm:

#### 1. JwtService - Thêm 2 methods
```java
public List<GrantedAuthority> extractAuthorities(String token) {
    return jwtService.extractRoles(token)
        .stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
}
```

#### 2. JwtAuthenticationFilter - Sửa doFilterInternal()
```java
List<GrantedAuthority> authorities = jwtService.extractAuthorities(token);

UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(
        username,
        null,
        authorities  // ⭐ THÊM AUTHORITIES
    );
```

#### 3. SecurityConfig - Cấu hình phân quyền
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/api/fruits").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET, "/api/fruits").hasAnyRole("ADMIN", "MEMBER")
    .anyRequest().authenticated())
```

#### 4. AuthController - Login
```java
List<String> roles = user.getRoles();  // Get từ DB
String token = jwtService.generateToken(username, roles);  // Thêm roles
```

#### 5. Entities
- User: Thêm `List<String> roles`
- LoginResponse: Thêm `List<String> roles`

### 📊 Kịch bản:

```
Admin request POST /api/fruits
├─ JwtAuthenticationFilter: roles = [ROLE_ADMIN]
├─ Authorization Filter: POST yêu cầu hasRole("ADMIN")
└─ ✅ MATCH → 201 Created

Member request POST /api/fruits  
├─ JwtAuthenticationFilter: roles = [ROLE_MEMBER]
├─ Authorization Filter: POST yêu cầu hasRole("ADMIN")
└─ ❌ NO MATCH → 403 Forbidden

Member request GET /api/fruits
├─ JwtAuthenticationFilter: roles = [ROLE_MEMBER]
├─ Authorization Filter: GET yêu cầu hasAnyRole("ADMIN", "MEMBER")
└─ ✅ MATCH → 200 OK
```

---

## 🎯 TÓM TẮT:

| Phần | Công việc |
|------|----------|
| **Authentication** | JwtAuthenticationFilter: Extract roles & set vào SecurityContext |
| **Authorization** | Authorization Filter (tự động): Check roles vs endpoint requirement |
| **Code phải thêm** | JwtService (extract), JwtAuthenticationFilter (set), SecurityConfig (config), AuthController (login), Entities (roles field) |
| **Spring Security tự xử lý** | Authorization Filter, Exception Handler, 401/403 responses |

---

## 📚 Đọc chi tiết:

- **Nhanh (10 min):** README_AUTHORIZATION.md + SUMMARY.md
- **Sâu (30 min):** AUTHORIZATION_FLOW_GUIDE.md + FILTER_CHAIN_EXECUTION_FLOW.md
- **Code (15 min):** CODE_EXAMPLES_AUTHORIZATION.md

---

**Done! 🚀**

