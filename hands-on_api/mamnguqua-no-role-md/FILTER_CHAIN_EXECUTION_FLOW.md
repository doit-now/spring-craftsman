# 🔄 FILTER CHAIN EXECUTION FLOW CHI TIẾT

## 📊 SƠ ĐỒ TỔNG QUÁT

```
┌──────────────────────────────────────────────────────────────────────┐
│                      HTTP REQUEST từ Client                          │
│                                                                       │
│  POST /api/fruits                                                   │
│  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...      │
│  Content-Type: application/json                                     │
│  Body: { "name": "Xoài", "price": 20000 }                           │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain (theo thứ tự)              │
│                                                                       │
│  🔹 CorsFilter (đã bật với .cors(Customizer.withDefaults()))        │
│                                                                       │
│  🔹 CsrfFilter (đã tắt với .csrf(csrf -> csrf.disable()))           │
│                                                                       │
│  ⭐ JwtAuthenticationFilter (chạy ĐẦU TIÊN)                          │
│     - Extract token từ Authorization header                         │
│     - Validate token                                                │
│     - Extract username: "admin"                                     │
│     - Extract roles: ["ROLE_ADMIN"]                                 │
│     - Tạo UsernamePasswordAuthenticationToken(                      │
│         principal="admin",                                          │
│         authorities=[SimpleGrantedAuthority("ROLE_ADMIN")]          │
│       )                                                             │
│     - SecurityContextHolder.getContext().setAuthentication(token)   │
│     - ✅ User đã authenticated với role ADMIN                       │
│                                                                       │
│  🔹 UsernamePasswordAuthenticationFilter (chạy THỨ HAI)              │
│     - Kiểm tra SecurityContext.getAuthentication()                 │
│     - Thấy != null → User đã authenticated                          │
│     - SKIP (bỏ qua) → Không cần xử lý username/password             │
│                                                                       │
│  ⭐ Authorization Filter (GIAI ĐOẠN PHÂN QUYỀN)                       │
│     - Lấy endpoint: POST /api/fruits                                │
│     - Lấy yêu cầu quyền: .hasRole("ADMIN")                          │
│     - Lấy Authentication từ SecurityContext                         │
│     - Lấy authorities: [SimpleGrantedAuthority("ROLE_ADMIN")]       │
│     - So sánh: ROLE_ADMIN == ROLE_ADMIN? ✅ YES!                    │
│     - ALLOW → Request tiếp tục đi qua                               │
│                                                                       │
│  🔹 [Các filter khác...]                                            │
│                                                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│             Dispatcher Servlet → Controller Mapping                  │
│                                                                       │
│  Route: POST /api/fruits                                            │
│  Match: FruitController.createFruit(@RequestBody FruitCreateRequest)│
│                                                                       │
│  ⭐ @PreAuthorize("hasRole('ADMIN')")                               │
│     - Kiểm tra thêm 1 lần nữa trước khi vào method                 │
│     - SecurityContext vẫn có [ROLE_ADMIN]                           │
│     - PASS ✅ → Vào method                                          │
│                                                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│                    FruitController.createFruit()                    │
│                                                                       │
│  ✅ Xử lý business logic:                                            │
│     - Validate request body                                         │
│     - Tạo Fruit entity mới                                          │
│     - Save vào database                                             │
│     - Return FruitResponse                                          │
│                                                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│                        HTTP RESPONSE                                 │
│                                                                       │
│  Status: 201 Created                                                │
│  Headers: Content-Type: application/json                           │
│  Body: {                                                            │
│    "id": 5,                                                         │
│    "name": "Xoài",                                                  │
│    "price": 20000,                                                  │
│    "createdAt": "2026-02-21T10:30:00"                               │
│  }                                                                  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 🔴 KỊCH BẢN 2: MEMBER GỌI POST /api/fruits (UNAUTHORIZED)

```
┌──────────────────────────────────────────────────────────────────────┐
│                      HTTP REQUEST từ Client                          │
│                                                                       │
│  POST /api/fruits                                                   │
│  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...      │
│  (token của member1, roles: ["ROLE_MEMBER"])                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain (theo thứ tự)              │
│                                                                       │
│  ⭐ JwtAuthenticationFilter                                          │
│     - Extract token từ Authorization header                         │
│     - Validate token ✅                                             │
│     - Extract username: "member1"                                   │
│     - Extract roles: ["ROLE_MEMBER"]                                │
│     - Tạo UsernamePasswordAuthenticationToken(                      │
│         principal="member1",                                        │
│         authorities=[SimpleGrantedAuthority("ROLE_MEMBER")]         │
│       )                                                             │
│     - SecurityContextHolder.getContext().setAuthentication(token)   │
│     - ✅ User đã authenticated với role MEMBER                      │
│                                                                       │
│  🔹 UsernamePasswordAuthenticationFilter (SKIP)                      │
│                                                                       │
│  ⭐ Authorization Filter (GIAI ĐOẠN PHÂN QUYỀN) ⚠️ FAIL HERE        │
│     - Lấy endpoint: POST /api/fruits                                │
│     - Lấy yêu cầu quyền: .hasRole("ADMIN")                          │
│     - Lấy authorities từ SecurityContext: [ROLE_MEMBER]             │
│     - So sánh: ROLE_MEMBER == ROLE_ADMIN? ❌ NO!                    │
│     - DENY → Throw AccessDeniedException                            │
│                                                                       │
│  🔹 Exception Handler (CatchException)                              │
│     - Bắt AccessDeniedException                                    │
│     - Chuyển đổi thành response                                    │
│                                                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│                        HTTP RESPONSE                                 │
│                                                                       │
│  Status: 403 Forbidden                                              │
│  Headers: Content-Type: application/json                           │
│  Body: {                                                            │
│    "timestamp": "2026-02-21T10:30:00",                              │
│    "status": 403,                                                   │
│    "error": "Forbidden",                                            │
│    "message": "Access is denied"                                   │
│  }                                                                  │
│                                                                       │
│  ❌ Request bị từ chối - Member không có quyền POST                │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 🟢 KỊCH BẢN 3: MEMBER GỌI GET /api/fruits (OK)

```
┌──────────────────────────────────────────────────────────────────────┐
│                      HTTP REQUEST từ Client                          │
│                                                                       │
│  GET /api/fruits                                                    │
│  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...      │
│  (token của member1, roles: ["ROLE_MEMBER"])                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain (theo thứ tự)              │
│                                                                       │
│  ⭐ JwtAuthenticationFilter                                          │
│     - Extract & validate token ✅                                   │
│     - Extract username: "member1"                                   │
│     - Extract roles: ["ROLE_MEMBER"]                                │
│     - SecurityContext.setAuthentication(token)                      │
│                                                                       │
│  🔹 UsernamePasswordAuthenticationFilter (SKIP)                      │
│                                                                       │
│  ⭐ Authorization Filter (GIAI ĐOẠN PHÂN QUYỀN) ✅ PASS HERE        │
│     - Lấy endpoint: GET /api/fruits                                 │
│     - Lấy yêu cầu quyền: .hasAnyRole("ADMIN", "STAFF", "MEMBER")  │
│     - Lấy authorities từ SecurityContext: [ROLE_MEMBER]             │
│     - So sánh: ROLE_MEMBER có trong ADMIN/STAFF/MEMBER? ✅ YES!     │
│     - ALLOW → Request tiếp tục                                      │
│                                                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│                 FruitController.getAllFruits()                       │
│                                                                       │
│  ✅ Xử lý business logic:                                            │
│     - Query tất cả fruits từ database                               │
│     - Map sang FruitResponse list                                   │
│     - Return response                                               │
│                                                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│                        HTTP RESPONSE                                 │
│                                                                       │
│  Status: 200 OK                                                     │
│  Headers: Content-Type: application/json                           │
│  Body: [                                                            │
│    { "id": 1, "name": "Táo", "price": 10000 },                      │
│    { "id": 2, "name": "Cam", "price": 15000 },                      │
│    { "id": 5, "name": "Xoài", "price": 20000 }                      │
│  ]                                                                  │
│                                                                       │
│  ✅ Member có quyền xem danh sách fruits                            │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 🟡 KỊCH BẢN 4: CLIENT KHÔNG GỬI TOKEN (UNAUTHENTICATED)

```
┌──────────────────────────────────────────────────────────────────────┐
│                      HTTP REQUEST từ Client                          │
│                                                                       │
│  GET /api/fruits                                                    │
│  (Không có Authorization header)                                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain (theo thứ tự)              │
│                                                                       │
│  ⭐ JwtAuthenticationFilter                                          │
│     - request.getHeader("Authorization") == null                    │
│     - Log: "No JWT token found in Authorization header"             │
│     - filterChain.doFilter(request, response) → SKIP                │
│     - ❌ User không được authenticated                              │
│                                                                       │
│  🔹 UsernamePasswordAuthenticationFilter (SKIP vì không cần)         │
│                                                                       │
│  ⭐ Authorization Filter (GIAI ĐOẠN PHÂN QUYỀN)                       │
│     - Lấy endpoint: GET /api/fruits                                 │
│     - Yêu cầu: .authenticated()                                     │
│     - Lấy Authentication từ SecurityContext: null ❌                 │
│     - Check: authenticated() → SecurityContext.getAuthentication() == null? ❌
│     - DENY → Throw AuthenticationException                          │
│                                                                       │
│  🔹 Exception Handler (CatchException)                              │
│     - Bắt AuthenticationException                                  │
│     - Chuyển đổi thành 401 Unauthorized response                   │
│                                                                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               ↓
┌──────────────────────────────────────────────────────────────────────┐
│                        HTTP RESPONSE                                 │
│                                                                       │
│  Status: 401 Unauthorized                                           │
│  Headers: Content-Type: application/json                           │
│  Body: {                                                            │
│    "timestamp": "2026-02-21T10:30:00",                              │
│    "status": 401,                                                   │
│    "error": "Unauthorized",                                         │
│    "message": "Full authentication is required to access this resource"
│  }                                                                  │
│                                                                       │
│  ❌ Client phải gửi JWT token                                       │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 📋 BẢNG SO SÁNH: CÁC TRƯỜNG HỢP KHÁC NHAU

| Kịch bản | Token | Roles | Endpoint | Yêu cầu | Authentication Filter | Authorization Filter | Kết quả |
|---------|-------|-------|----------|---------|----------------------|----------------------|---------|
| **1️⃣** | ✅ Admin | [ROLE_ADMIN] | POST /api/fruits | .hasRole("ADMIN") | ✅ Set | ✅ ROLE_ADMIN == ROLE_ADMIN | **201 Created** ✅ |
| **2️⃣** | ✅ Member | [ROLE_MEMBER] | POST /api/fruits | .hasRole("ADMIN") | ✅ Set | ❌ ROLE_MEMBER ≠ ROLE_ADMIN | **403 Forbidden** ❌ |
| **3️⃣** | ✅ Member | [ROLE_MEMBER] | GET /api/fruits | .hasAnyRole(...) | ✅ Set | ✅ ROLE_MEMBER ∈ [ADMIN, STAFF, MEMBER] | **200 OK** ✅ |
| **4️⃣** | ❌ Null | null | GET /api/fruits | .authenticated() | ❌ Null | ❌ Null ≠ authenticated | **401 Unauthorized** ❌ |

---

## 🎯 KEY POINTS QUAN TRỌNG

### ✨ Authentication vs Authorization

```
┌─────────────────────────────────────────┐
│  GIAI ĐOẠN 1: AUTHENTICATION            │
│  (Xác thực - "Bạn là ai?")              │
│                                         │
│  JwtAuthenticationFilter:                │
│  - Extract JWT token                    │
│  - Validate token                       │
│  - Extract username & roles              │
│  - Create & set Authentication object    │
│  → SecurityContext.setAuthentication()   │
│                                         │
│  ✅ Kết quả: SecurityContext có         │
│     Authentication object               │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│  GIAI ĐOẠN 2: AUTHORIZATION             │
│  (Phân quyền - "Bạn được làm gì?")     │
│                                         │
│  Authorization Filter:                  │
│  - Kiểm tra roles của user              │
│  - So sánh với yêu cầu endpoint         │
│  - hasRole(), hasAnyRole(), authenticated()
│  - ALLOW hoặc DENY                      │
│                                         │
│  ✅ Kết quả: User được truy cập hay      │
│     bị chặn 403 Forbidden               │
└─────────────────────────────────────────┘
```

### 🔐 SecurityContext - Tâm điểm của Spring Security

```java
// Tất cả các filter truy cập SecurityContext để:
SecurityContext context = SecurityContextHolder.getContext();

// 1. SET authentication (trong JwtAuthenticationFilter)
context.setAuthentication(authToken);  // Lưu user info & roles

// 2. GET authentication (trong Authorization Filter)
Authentication auth = context.getAuthentication();
if (auth != null) {
    List<GrantedAuthority> authorities = auth.getAuthorities();
    // ["ROLE_ADMIN", "ROLE_USER"]
}

// 3. CLEAR authentication (khi logout)
context.clearAuthentication();  // Xóa user info
```

### 📌 Quy trình ra quyết định ALLOW/DENY

```
┌─────────────────────────────┐
│ Authorization Filter chạy   │
│                             │
│ Lấy yêu cầu của endpoint:   │
│ .hasRole("ADMIN")           │
│                             │
│ Lấy authorities của user:   │
│ [ROLE_MEMBER]               │
│                             │
│ So sánh:                    │
│ ROLE_MEMBER == ROLE_ADMIN?  │
│           ❌ NO             │
│                             │
│ → THROW AccessDeniedException
│ → HTTP 403 Forbidden        │
└─────────────────────────────┘
```

---

## 🚀 ĐỂ CÓ CÓ PHÂN QUYỀN, DEVELOPER CẦN THÊM:

| Thứ tự | Thành phần | Mục đích |
|--------|-----------|---------|
| **1** | **JwtService** | Extract roles từ JWT token |
| **2** | **JwtAuthenticationFilter** | Set authorities (roles) vào Authentication object |
| **3** | **SecurityConfig** | Cấu hình `.hasRole()` `.hasAnyRole()` cho từng endpoint |
| **4** | **Controller** | Thêm `@PreAuthorize` trên methods (optional, kiểm tra thêm) |
| **5** | **AuthController** | Extract roles khi login, thêm roles vào JWT token |
| **6** | **User Entity** | Thêm field `List<String> roles` để lưu roles |
| **7** | **LoginResponse** | Thêm field `roles` để gửi về client |

---

## 💡 LOGIC QUYẾT ĐỊNH CUỐI CÙNG:

```
if (JWT token hợp lệ?) {
    ✅ → JwtAuthenticationFilter tạo Authentication object
    
    if (SecurityContext có Authentication object?) {
        ✅ → User đã AUTHENTICATED
        
        if (User's roles match endpoint requirement?) {
            ✅ → ALLOW → Đi tới Controller
        } else {
            ❌ → DENY → 403 Forbidden
        }
    } else {
        ❌ → User không AUTHENTICATED → 401 Unauthorized
    }
} else {
    ❌ → Invalid token → 401 Unauthorized
}
```

Đó là toàn bộ flow chi tiết! 🎯

