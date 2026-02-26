# 📊 VISUAL DIAGRAMS - PHÂN QUYỀN TRONG SPRING SECURITY

## 1️⃣ AUTHENTICATION vs AUTHORIZATION

```
┌─────────────────────────────────────────────────────────────────┐
│                     HTTP REQUEST                                │
│              POST /api/fruits (Create)                         │
│              Authorization: Bearer <token>                      │
│              Body: { "name": "Xoài" }                           │
└────────────────────────┬────────────────────────────────────────┘
                         ↓
        ┌────────────────────────────────────┐
        │  GIAI ĐOẠN 1: AUTHENTICATION      │
        │  (Xác thực - "Bạn là ai?")        │
        │                                    │
        │  JwtAuthenticationFilter:          │
        │  ✅ Validate token                │
        │  ✅ Extract username: "admin"     │
        │  ✅ Extract roles: ["ROLE_ADMIN"] │
        │  ✅ Create Authentication object  │
        │  ✅ SecurityContext.set()         │
        │                                    │
        │  KẾT QUẢ: User đã known           │
        │           (authenticated)         │
        └────────────────────────────────────┘
                         ↓
        ┌────────────────────────────────────┐
        │  GIAI ĐOẠN 2: AUTHORIZATION       │
        │  (Phân quyền - "Bạn được làm gì?")│
        │                                    │
        │  Authorization Filter:            │
        │  ✅ Get user's roles              │
        │  ✅ Get endpoint's requirement    │
        │  ✅ hasRole("ADMIN")?             │
        │  ✅ ROLE_ADMIN == ROLE_ADMIN?     │
        │  ✅ YES → ALLOW                   │
        │                                    │
        │  KẾT QUẢ: User có quyền           │
        │           (authorized)            │
        └────────────────────────────────────┘
                         ↓
        ┌────────────────────────────────────┐
        │      FruitController.create()      │
        │                                    │
        │  ✅ Xử lý business logic          │
        │  ✅ Lưu fruit vào DB              │
        │  ✅ Return 201 Created            │
        └────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│                    HTTP RESPONSE                                │
│                   201 Created ✅                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2️⃣ PHÂN QUYỀN THEO ENDPOINT

```
                    ┌─── Admin ───┐
                    │ ROLE_ADMIN  │
                    └─────────────┘
                          │
            ┌─────────────┼─────────────┐
            │             │             │
    ┌───────▼───────┐    │    ┌────────▼────────┐
    │ POST /fruits  │    │    │ DELETE /fruits  │
    │ hasRole ADMIN │    │    │ hasRole ADMIN   │
    │      ✅       │    │    │       ✅        │
    └───────────────┘    │    └─────────────────┘
                         │
              ┌──────────┼──────────┐
              │          │          │
         ┌────▼──────┐  │  ┌───────▼───────┐
         │ PUT /fruits   │   │ GET /fruits   │
         │ hasAnyRole    │   │ hasAnyRole    │
         │ ADMIN, STAFF  │   │ ADMIN, STAFF, │
         │      ✅       │   │ MEMBER   ✅   │
         └──────────────┘   └───────────────┘
         
         
                    ┌──── Staff ─────┐
                    │ ROLE_STAFF     │
                    └────────────────┘
                         │
            ┌────────────┼────────────┐
            │            │            │
    ┌───────▼────────┐   │   ┌────────▼────────┐
    │ POST /fruits   │   │   │ DELETE /fruits  │
    │ hasRole ADMIN  │   │   │ hasRole ADMIN   │
    │       ❌       │   │   │       ❌        │
    └────────────────┘   │   └─────────────────┘
                         │
              ┌──────────┼──────────┐
              │          │          │
         ┌────▼──────────┐ ┌───────▼───────┐
         │ PUT /fruits   │ │ GET /fruits   │
         │ hasAnyRole    │ │ hasAnyRole    │
         │ ADMIN, STAFF  │ │ ADMIN, STAFF, │
         │      ✅       │ │ MEMBER   ✅   │
         └───────────────┘ └───────────────┘


                  ┌──── Member ────┐
                  │ ROLE_MEMBER    │
                  └────────────────┘
                       │
          ┌────────────┼────────────┐
          │            │            │
  ┌───────▼────────┐   │   ┌────────▼────────┐
  │ POST /fruits   │   │   │ DELETE /fruits  │
  │ hasRole ADMIN  │   │   │ hasRole ADMIN   │
  │       ❌       │   │   │       ❌        │
  └────────────────┘   │   └─────────────────┘
                       │
            ┌──────────┼──────────┐
            │          │          │
       ┌────▼────────┐ ┌─────────▼──────────┐
       │ PUT /fruits │ │ GET /fruits        │
       │ hasAnyRole  │ │ hasAnyRole         │
       │ ADMIN, STAFF│ │ ADMIN, STAFF,      │
       │      ❌     │ │ MEMBER        ✅   │
       └─────────────┘ └────────────────────┘

Legend:
✅ = ALLOWED
❌ = DENIED (403 Forbidden)
```

---

## 3️⃣ FILTER CHAIN EXECUTION ORDER

```
┌─────────────────────────────────────────────────────────────┐
│           REQUEST từ Client đến Server                      │
│  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...      │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  1️⃣  CorsFilter                                             │
│      - Xử lý CORS preflight requests (OPTIONS)              │
│      - Kiểm tra allowed origins                             │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  2️⃣  CsrfFilter                                             │
│      - Bảo vệ CSRF attacks                                  │
│      - DISABLED trong config (vì dùng JWT)                  │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  3️⃣  ⭐ JwtAuthenticationFilter (CUSTOM - chạy trước)       │
│                                                              │
│      - Extract token từ Authorization header                │
│      - Validate token                                       │
│      - Extract username & roles từ token                    │
│      - Create UsernamePasswordAuthenticationToken:          │
│        - principal: "admin"                                 │
│        - authorities: [ROLE_ADMIN, ROLE_MEMBER]             │
│      - SecurityContextHolder.getContext()                   │
│        .setAuthentication(token)                            │
│                                                              │
│      🔑 KEY POINT: Authentication object được set           │
│                    với authorities (roles) đầy đủ           │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  4️⃣  UsernamePasswordAuthenticationFilter (DEFAULT)         │
│                                                              │
│      - Check: SecurityContext.getAuthentication() != null   │
│      - YES → User already authenticated, SKIP filter        │
│      - NO → Process username/password auth                  │
│                                                              │
│      🔑 KEY POINT: Vì JWT filter đã set auth, filter này   │
│                    sẽ bỏ qua (skip)                         │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  5️⃣  ⭐ Authorization Filter (BUILT-IN - kiểm tra quyền)     │
│                                                              │
│      - Get endpoint: POST /api/fruits                        │
│      - Get requirement: .hasRole("ADMIN")                   │
│      - Get Authentication từ SecurityContext:               │
│        → authorities = [ROLE_ADMIN, ROLE_MEMBER]            │
│                                                              │
│      - Check: ROLE_ADMIN in [ROLE_ADMIN]?                  │
│      - YES → ALLOW, continue filter chain                   │
│      - NO → Throw AccessDeniedException (403)               │
│                                                              │
│      🔑 KEY POINT: Đây là nơi PHÂN QUYỀN thực sự xảy ra    │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  6️⃣  [Các filter khác...]                                   │
│      - SecurityContextPersistenceFilter                     │
│      - ExceptionTranslationFilter                           │
│      - etc.                                                 │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  7️⃣  DispatcherServlet                                      │
│      - Route request đến controller                         │
│      - FruitController.createFruit()                        │
│      - @PreAuthorize("hasRole('ADMIN')") [Optional check]   │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  Handler (Business Logic)                                   │
│  - Validate input                                           │
│  - Save to database                                         │
│  - Return response                                          │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│           RESPONSE về Client                                │
│           201 Created                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 4️⃣ DECISION TREE - ALLOW/DENY

```
                         START
                            ↓
        ┌───────────────────────────────────────┐
        │  Request có Authorization header?     │
        └───────┬───────────────────────────┬───┘
               YES                          NO
                ↓                            ↓
        ┌──────────────────────┐    ┌──────────────────┐
        │  Extract & Validate   │    │  Log: No token   │
        │  JWT Token            │    │  filterChain.do  │
        └───┬──────────────────┘    │  Filter()        │
            ↓                        │  ↓               │
        ┌──────────────────────┐    │  Authorization   │
        │  Valid Token?        │    │  Filter runs     │
        └───┬──────────────┬──┘    │  ↓               │
           YES             NO       │  Endpoint needs  │
            ↓              ↓        │  .authenticated()
        ┌─────────┐    ┌──────────┐│  ↓               │
        │Extract  │    │Skip, log │  security...     │
        │username │    │warning   │  Context ==      │
        │& roles  │    │          │  null?           │
        └───┬─────┘    └─────┬────┘│  ↓               │
            ↓                ↓     │  ❌ 401 Unauth   │
        ┌─────────────────────────┐└──────────────────┘
        │  SecurityContext        │
        │  .setAuthentication(    │
        │    principal: user,     │
        │    authorities: [roles] │
        │  )                      │
        └────────┬────────────────┘
                 ↓
        ┌──────────────────────┐
        │ Authorization Filter │
        │ runs                 │
        └───┬──────────────┬──┘
            ↓              ↓
        ┌───────────┐  ┌──────────────┐
        │ User's    │  │ Endpoint's   │
        │ roles:    │  │ requirement: │
        │ [ROLE_    │  │ .hasRole()   │
        │  ADMIN]   │  │ or           │
        │           │  │ .hasAnyRole()│
        └───┬───────┘  └──────┬───────┘
            │                 │
            └────────┬────────┘
                     ↓
        ┌───────────────────────────┐
        │  Match?                   │
        └────┬─────────────────┬────┘
            YES               NO
             ↓                ↓
        ┌────────────┐   ┌──────────────────┐
        │  ALLOW     │   │  DENY            │
        │  Continue  │   │  Throw Access    │
        │  filter    │   │  Denied          │
        │  chain     │   │  Exception       │
        └────┬───────┘   └──────┬───────────┘
             ↓                   ↓
        ┌────────────────┐   ┌──────────────────┐
        │ Controller     │   │ Exception        │
        │ Handler        │   │ Translator       │
        │ ↓              │   │ ↓                │
        │ Business       │   │ 403 Forbidden    │
        │ Logic          │   │ Response         │
        │ ↓              │   └──────────────────┘
        │ 200/201/etc    │
        └────────────────┘
```

---

## 5️⃣ DATABASE SCHEMA

```
┌────────────────────────────────────┐
│          users TABLE               │
├────────────────────────────────────┤
│ id          | INT (PK)             │
│ username    | VARCHAR (UNIQUE)     │
│ password    | VARCHAR              │
│ email       | VARCHAR (UNIQUE)     │
│ full_name   | VARCHAR              │
│ created_at  | TIMESTAMP            │
│ updated_at  | TIMESTAMP            │
└────────────────────────────────────┘
            ▲
            │ 1:N
            │ (One User has Many Roles)
            │
┌────────────────────────────────────┐
│       user_roles TABLE             │
├────────────────────────────────────┤
│ user_id | INT (FK → users.id)      │
│ role    | VARCHAR                  │
│         | e.g., ROLE_ADMIN         │
│         |       ROLE_STAFF         │
│         |       ROLE_MEMBER        │
└────────────────────────────────────┘

EXAMPLE DATA:
├─ User: id=1, username="admin", password="..."
│  └─ Roles: ["ROLE_ADMIN"]
│
├─ User: id=2, username="staff1", password="..."
│  └─ Roles: ["ROLE_STAFF"]
│
└─ User: id=3, username="member1", password="..."
   └─ Roles: ["ROLE_MEMBER"]
```

---

## 6️⃣ JWT TOKEN STRUCTURE

```
JWT Token Format:
┌──────────────────────────────────────────────────────────┐
│ Header.Payload.Signature                                 │
│                                                          │
│ eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.               │
│ eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXX0. │
│ SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c          │
└──────────────────────────────────────────────────────────┘

1️⃣  Header (Base64):
    {
      "alg": "HS256",
      "typ": "JWT"
    }

2️⃣  Payload (Base64) - ⭐ ROLES LƯU Ở ĐÂY:
    {
      "sub": "admin",           ← username
      "roles": [                ← ⭐ ROLES
        "ROLE_ADMIN"            ← Role 1
      ],
      "iat": 1708416600,       ← issued at
      "exp": 1708503000        ← expiration
    }

3️⃣  Signature (HMAC):
    HMACSHA256(
      base64UrlEncode(header) + "." +
      base64UrlEncode(payload),
      secret_key
    )
```

---

## 7️⃣ FLOW TỔNG HỢP - Từ Login đến Response

```
┌─────────────────────────────────────────────────────────────┐
│ 1️⃣  CLIENT: POST /api/auth/login                          │
│     { username: "admin", password: "admin123" }            │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 2️⃣  SERVER: AuthController.login()                         │
│     - Find user in database                                │
│     - Verify password                                      │
│     - Get user.roles = ["ROLE_ADMIN"] ⭐                   │
│     - JwtService.generateToken(username, roles)            │
│     - Token = { sub: "admin", roles: ["ROLE_ADMIN"] }      │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 3️⃣  RESPONSE: 200 OK                                       │
│     {                                                       │
│       "token": "eyJhbGci...",                               │
│       "username": "admin",                                  │
│       "roles": ["ROLE_ADMIN"]                               │
│     }                                                       │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 4️⃣  CLIENT: POST /api/fruits (Create)                      │
│     Authorization: Bearer eyJhbGci...                       │
│     { name: "Xoài", price: 20000 }                          │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 5️⃣  SERVER: JwtAuthenticationFilter                        │
│     - Extract token                                        │
│     - Validate token ✅                                    │
│     - Extract username: "admin"                            │
│     - Extract roles: ["ROLE_ADMIN"] ⭐                     │
│     - Create authorities: [SimpleGrantedAuthority(...)]    │
│     - SecurityContext.set(username, authorities) ✅        │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 6️⃣  SERVER: Authorization Filter                           │
│     - Endpoint: POST /api/fruits                           │
│     - Requirement: .hasRole("ADMIN")                       │
│     - User's authorities: [ROLE_ADMIN] ⭐                  │
│     - Match? ✅ YES                                        │
│     - ALLOW ✅                                             │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 7️⃣  SERVER: FruitController.createFruit()                  │
│     - Validate request                                     │
│     - Create Fruit entity                                  │
│     - fruitRepository.save(fruit)                          │
│     - Return FruitResponse                                 │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 8️⃣  RESPONSE: 201 Created ✅                               │
│     {                                                       │
│       "id": 5,                                              │
│       "name": "Xoài",                                       │
│       "price": 20000                                        │
│     }                                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 8️⃣ ERROR CASES

```
CASE 1: Invalid Token
┌─────────────┐
│ Request     │
│ + Token     │
│  (invalid)  │
└──────┬──────┘
       ↓
┌──────────────────────┐
│ JwtAuthenticationF   │
│ - Validate token     │
│ - isValidToken()     │
│ - ❌ FALSE           │
│ - Skip filter        │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ Authorization Filter │
│ - Check auth         │
│ - null?              │
│ - ❌ YES             │
│ - 401 Unauthorized   │
└──────────────────────┘

CASE 2: No Token
┌──────────────────┐
│ Request          │
│ + No header      │
└──────┬───────────┘
       ↓
┌──────────────────────┐
│ JwtAuthenticationF   │
│ - authHeader == null │
│ - Skip filter        │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ Authorization Filter │
│ - .authenticated()   │
│ - ❌ FAIL            │
│ - 401 Unauthorized   │
└──────────────────────┘

CASE 3: Valid Token, But Insufficient Role
┌──────────────────────┐
│ Request              │
│ + Token (MEMBER)     │
│ POST /api/fruits     │
└──────┬───────────────┘
       ↓
┌──────────────────────────┐
│ JwtAuthenticationFilter  │
│ - Valid token ✅         │
│ - username: "member1"    │
│ - roles: [ROLE_MEMBER]   │
│ - Set authorities ✅     │
└──────┬───────────────────┘
       ↓
┌──────────────────────────┐
│ Authorization Filter     │
│ - Endpoint: POST         │
│ - Requirement: ADMIN     │
│ - User's roles: MEMBER   │
│ - ❌ NOT MATCH           │
│ - 403 Forbidden          │
└──────────────────────────┘
```

---

Hy vọng các diagram này giúp bạn hiểu rõ hơn! 🎯

