# 📚 README - PHÂN QUYỀN SPRING SECURITY - HƯỚNG DẪN HOÀN CHỈNH

## 📖 Giới thiệu

Repo này chứa **hướng dẫn chi tiết** về cách implement **phân quyền (Authorization) trong Spring Security** với **JWT token-based authentication**.

---

## 📂 Cấu trúc tài liệu

### 1. **SUMMARY.md** ⭐ START HERE
   - Tóm tắt ngắn gọn câu trả lời cho câu hỏi của bạn
   - Flow quy trình khi có phân quyền
   - Phần code developer cần can thiệp
   - **👉 Đọc file này trước!**

### 2. **AUTHORIZATION_FLOW_GUIDE.md**
   - Giải thích chi tiết về Authentication vs Authorization
   - Kiến trúc phân quyền trong Spring Security
   - Các kịch bản (scenario) khác nhau
   - Code example hoàn chỉnh cho tất cả các class

### 3. **FILTER_CHAIN_EXECUTION_FLOW.md**
   - Trình tự chạy của các filter
   - Flow chi tiết trong ứng dụng có phân quyền
   - 4 kịch bản khác nhau (Admin POST, Member POST, Member GET, No token)
   - Bảng so sánh các trường hợp
   - Decision tree ALLOW/DENY

### 4. **CODE_EXAMPLES_AUTHORIZATION.md**
   - Code mẫu hoàn chỉnh cho 7 files
   - JwtService.java, JwtAuthenticationFilter.java, SecurityConfig.java
   - AuthController.java, User.java, LoginResponse.java, FruitController.java
   - Có thể copy-paste trực tiếp

### 5. **VISUAL_DIAGRAMS.md**
   - Sơ đồ minh họa các khái niệm
   - Kiến trúc filter chain
   - Bảng phân quyền theo role
   - Database schema
   - JWT token structure
   - Flow tổng hợp

### 6. **IMPLEMENTATION_CHECKLIST.md**
   - Danh sách công việc từng bước
   - Phân chia theo Phase (1-9)
   - Dependencies cần thiết
   - Quick test commands
   - Common issues & solutions

### 7. **SecurityConfig.java** (đã cập nhật)
   - Ghi chú tiếng Việt chi tiết
   - Giải thích từng dòng code
   - Ví dụ cấu hình phân quyền

---

## 🎯 QUICK START (5 phút)

### Bước 1: Đọc SUMMARY.md
```
→ Hiểu flow tổng quát
→ Biết developer phải can thiệp ở đâu
```

### Bước 2: Xem VISUAL_DIAGRAMS.md
```
→ Hình dung kiến trúc
→ Dễ hình dung filter chain
```

### Bước 3: Copy code từ CODE_EXAMPLES_AUTHORIZATION.md
```
→ Dùng code mẫu
→ Thay đổi theo project của bạn
```

### Bước 4: Follow IMPLEMENTATION_CHECKLIST.md
```
→ Làm theo từng bước
→ Tích vào ☑️ khi xong
```

### Bước 5: Test với Quick Test Commands
```
→ Chạy các test case
→ Verify phân quyền hoạt động
```

---

## 🔑 KEY CONCEPTS

### Authentication (Xác thực)
```
"Bạn là ai?"

JwtAuthenticationFilter:
1. Extract JWT token từ Authorization header
2. Validate token
3. Extract username & roles từ token
4. Tạo UsernamePasswordAuthenticationToken với:
   - principal: username
   - authorities: [ROLE_ADMIN, ROLE_MEMBER, ...]
5. SecurityContext.setAuthentication(token)

→ Kết quả: SecurityContext có Authentication object
```

### Authorization (Phân quyền)
```
"Bạn được làm gì?"

Authorization Filter:
1. Kiểm tra endpoint yêu cầu gì
   (ví dụ: .hasRole("ADMIN"))
2. Kiểm tra user có role đó không
   (từ SecurityContext.getAuthentication().getAuthorities())
3. So sánh:
   - MATCH → ALLOW ✅
   - NO MATCH → DENY 403 ❌
```

---

## 📊 ARCHITECTURE

```
┌──────────────────────────────────────────────────┐
│                HTTP REQUEST                      │
│        Authorization: Bearer <jwt_token>         │
└─────────────────┬──────────────────────────────┘
                  ↓
┌──────────────────────────────────────────────────┐
│ JwtAuthenticationFilter (CUSTOM - Extract roles) │
│ - Validate token                                 │
│ - Extract username & roles                      │
│ - Set authorities vào SecurityContext            │
└─────────────────┬──────────────────────────────┘
                  ↓
┌──────────────────────────────────────────────────┐
│ Authorization Filter (BUILT-IN - Check roles)    │
│ - Get user's authorities from SecurityContext    │
│ - Compare with endpoint requirement              │
│ - ALLOW or DENY (403)                           │
└─────────────────┬──────────────────────────────┘
                  ↓
┌──────────────────────────────────────────────────┐
│              Controller Handler                  │
│         (Business Logic)                        │
└──────────────────────────────────────────────────┘
```

---

## 🛠️ DEVELOPER WORK

### Files cần sửa:
1. ✅ **JwtService.java**
   - Thêm `extractRoles(token)`
   - Thêm `extractAuthorities(token)`
   - Sửa `generateToken(username, roles)`

2. ✅ **JwtAuthenticationFilter.java**
   - Sửa `doFilterInternal()` để extract & set authorities

3. ✅ **SecurityConfig.java**
   - Cấu hình `.hasRole()`, `.hasAnyRole()` cho từng endpoint

4. ✅ **AuthController.java**
   - Get roles khi login
   - Pass vào `generateToken()`

5. ✅ **User Entity**
   - Thêm field `List<String> roles`

6. ✅ **LoginResponse.java**
   - Thêm field `roles`

7. ⚪ **FruitController.java** (Optional)
   - Thêm `@PreAuthorize` annotations

---

## 📋 AUTHORIZATION RULES EXAMPLE

```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers("/api/auth/login").permitAll()
    
    // Phân quyền chi tiết
    .requestMatchers(HttpMethod.POST, "/api/fruits")
        .hasRole("ADMIN")  // ✅ Admin POST
    
    .requestMatchers(HttpMethod.DELETE, "/api/fruits/**")
        .hasRole("ADMIN")  // ✅ Admin DELETE
    
    .requestMatchers(HttpMethod.PUT, "/api/fruits/**")
        .hasAnyRole("ADMIN", "STAFF")  // ✅ Admin & Staff PUT
    
    .requestMatchers(HttpMethod.GET, "/api/fruits/**")
        .hasAnyRole("ADMIN", "STAFF", "MEMBER")  // ✅ All GET
    
    .anyRequest().authenticated())
```

---

## 🧪 TEST EXAMPLE

### Login:
```bash
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGc...",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### Create Fruit (Admin):
```bash
POST /api/fruits
Authorization: Bearer eyJhbGc...
{
  "name": "Xoài",
  "price": 20000
}

Response: 201 Created ✅
```

### Create Fruit (Member):
```bash
POST /api/fruits
Authorization: Bearer <member_token>
{
  "name": "Xoài",
  "price": 20000
}

Response: 403 Forbidden ❌
```

### Get Fruits (Member):
```bash
GET /api/fruits
Authorization: Bearer <member_token>

Response: 200 OK ✅
[
  { "id": 1, "name": "Táo", ... },
  { "id": 2, "name": "Cam", ... }
]
```

---

## 📚 READING ORDER

### Nếu bạn muốn **hiểu nhanh** (15 phút):
1. SUMMARY.md
2. VISUAL_DIAGRAMS.md (phần 1-3)
3. IMPLEMENTATION_CHECKLIST.md

### Nếu bạn muốn **hiểu sâu** (30 phút):
1. SUMMARY.md
2. AUTHORIZATION_FLOW_GUIDE.md
3. FILTER_CHAIN_EXECUTION_FLOW.md
4. VISUAL_DIAGRAMS.md
5. CODE_EXAMPLES_AUTHORIZATION.md

### Nếu bạn muốn **implement ngay** (45 phút):
1. IMPLEMENTATION_CHECKLIST.md
2. CODE_EXAMPLES_AUTHORIZATION.md
3. VISUAL_DIAGRAMS.md (reference)
4. Test với Quick Test Commands

---

## 🎓 LEARNING OUTCOMES

Sau khi đọc xong, bạn sẽ hiểu:

✅ **Authentication vs Authorization** - 2 khái niệm khác nhau
✅ **SecurityContext** - Nơi lưu trữ thông tin user
✅ **Filter Chain** - Trình tự chạy các filter
✅ **Authorities/Roles** - Cách Spring Security quản lý quyền
✅ **JWT Token** - Làm thế nào roles được lưu trong token
✅ **@PreAuthorize** - Cách kiểm tra quyền trên method
✅ **.hasRole() vs .hasAnyRole()** - Khác biệt giữa 2 cách
✅ **SecurityFilterChain** - Cách cấu hình phân quyền global
✅ **Exception Handling** - Cách xử lý 401/403 errors
✅ **Best Practices** - Security best practices

---

## 🚀 TIPS

1. **Roles phải bắt đầu với "ROLE_"** - Spring Security convention
2. **Extract roles từ token**, không từ database mỗi request
3. **Set authorities khi tạo Authentication object** - Đây là chìa khóa
4. **SecurityContext là thread-safe** - Spring quản lý ThreadLocal
5. **Test kỹ** - Kiểm tra tất cả role combinations
6. **Logging là bạn** - Log tất cả authorization decisions
7. **HTTPS là must** - Không bao giờ gửi token qua HTTP

---

## ❓ FAQ

**Q: "BEFORE" trong `.addFilterBefore()` có phải override không?**
A: Không! "Before" = "chạy trước", không override. Cả 2 filter đều tồn tại, nhưng JWT filter chạy trước.

**Q: Phải lưu roles vào JWT token không?**
A: Không bắt buộc. Có thể lưu vào Redis hoặc database, nhưng lưu trong token nhanh hơn (stateless).

**Q: UsernamePasswordAuthenticationFilter có bắt buộc không?**
A: Không. Spring Security chỉ cần Authentication object. UsernamePasswordAuthenticationFilter sẽ skip nếu user đã authenticated.

**Q: Làm thế nào để check quyền trên object cụ thể?**
A: Dùng `@PreAuthorize` với SpEL: `@fruitService.isOwner(#id, authentication.name)`

**Q: 401 vs 403 - Khác nhau gì?**
A: 401 = không authenticated (không có token hoặc token invalid)
   403 = authenticated nhưng không có quyền (insufficient permissions)

---

## 📞 SUPPORT

Nếu có câu hỏi:

1. Đọc lại SUMMARY.md
2. Xem FILTER_CHAIN_EXECUTION_FLOW.md - Decision Tree
3. Tìm kịch bản tương tự trong AUTHORIZATION_FLOW_GUIDE.md
4. Kiểm tra code examples trong CODE_EXAMPLES_AUTHORIZATION.md
5. Follow IMPLEMENTATION_CHECKLIST.md từng bước

---

## ✨ CONCLUSION

**Spring Security là framework rất mạnh, nhưng đòi hỏi phải hiểu rõ các concepts.**

Hãy đọc từng tài liệu một, làm theo checklist, test kỹ, và bạn sẽ implement phân quyền thành công! 🎯

---

**Created:** 2026-02-21
**Version:** 1.0
**Author:** GitHub Copilot with Giao.Lang
**Language:** Tiếng Việt

