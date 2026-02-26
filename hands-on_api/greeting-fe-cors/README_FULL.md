# 🚀 Spring Boot + Static HTML Demo (CORS Focus)

## 📌 Mục tiêu

Demo hiện tượng **CORS** khi gọi API từ một static HTML chạy ở port
khác.

------------------------------------------------------------------------

# 🧠 1️⃣ Sơ đồ minh họa CORS

    Browser (http://localhost:3979)
            |
            |  GET /api/greeting/fu
            v
    Spring Boot (http://localhost:6969)

➡ Vì **Origin khác port (3979 ≠ 6969)** nên Browser sẽ kiểm tra CORS.

Nếu server không cho phép origin → ❌ Bị chặn.

------------------------------------------------------------------------

# ⚡ 2️⃣ Chạy Static HTML bằng npx serve

## Bước 1: Cài Node.js

Kiểm tra:

``` bash
node -v
npm -v
```

## Bước 2: Chạy server

``` bash
npx serve -l 3979
```

Giải thích:

-   `npx` → chạy package không cần cài global
-   `serve` → web static server
-   `-l 3979` → chạy ở port 3979

## Bước 3: Mở trình duyệt

    http://localhost:3979

------------------------------------------------------------------------

# 🧪 3️⃣ Demo CORS

HTML sẽ gọi:

    http://localhost:6969/api/greeting/fu

Nếu chưa cấu hình CORS:

-   ❌ TypeError: Failed to fetch
-   Hoặc lỗi CORS trong Console

👉 Mở **F12 → Network** để quan sát request.

------------------------------------------------------------------------

# 🔧 4️⃣ Cách FIX CORS trong Spring Boot

## ✔ Cách 1: Dùng @CrossOrigin

``` java
@CrossOrigin(origins = "http://localhost:3979")
@RestController
@RequestMapping("/api/greeting")
public class GreetingController {
}
```

------------------------------------------------------------------------

## ✔ Cách 2: Cấu hình Global CORS (Professional)

``` java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3979")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}
```

👉 Cách này dùng cho môi trường thực tế, tránh lặp annotation.

------------------------------------------------------------------------

# 🎓 5️⃣ README phiên bản siêu ngắn (cho sinh viên)

## Chạy demo:

``` bash
npx serve -l 3979
```

Mở:

    http://localhost:3979

Nếu bị lỗi CORS → cấu hình @CrossOrigin trong Spring Boot.

------------------------------------------------------------------------

# 🧩 Kiến thức rút ra

-   fetch() gửi HTTP request từ browser
-   Origin = scheme + host + port
-   CORS là cơ chế bảo vệ của browser
-   Server phải cho phép origin hợp lệ

------------------------------------------------------------------------

© 2026 giáo.làng with AI support
