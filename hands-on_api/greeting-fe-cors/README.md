# 🚀 Hướng dẫn chạy demo HTML bằng `npx serve`

Tài liệu này hướng dẫn bạn cách deploy (chạy) file HTML demo lên **local
server** để test gọi API và quan sát hiện tượng **CORS**.

------------------------------------------------------------------------

## 📦 1️⃣ Yêu cầu trước khi chạy

Bạn cần cài:

-   ✅ Node.js (khuyến nghị \>= 16)
-   ✅ npm (đi kèm Node)

Kiểm tra:

``` bash
node -v
npm -v
```

------------------------------------------------------------------------

## ⚡ 2️⃣ Chạy local server bằng npx

Giả sử bạn đang ở thư mục chứa file:

    index_minimal_cors_fu.html

Chạy lệnh:

``` bash
npx serve -l 3979
```

Giải thích:

-   `npx` → chạy package mà không cần cài global
-   `serve` → web static server
-   `-l 3979` → chạy ở port 3979

------------------------------------------------------------------------

## 🌐 3️⃣ Truy cập trình duyệt

Mở:

    http://localhost:3979

Browser sẽ load file HTML của bạn.

------------------------------------------------------------------------

## 🧪 4️⃣ Demo hiện tượng CORS

File HTML sẽ gọi API:

    http://localhost:6969/api/greeting/fu

Nếu Spring Boot **không cấu hình CORS**, bạn sẽ thấy:

-   ❌ `TypeError: Failed to fetch`
-   Hoặc lỗi CORS trong tab Console

👉 Mở **F12 → Network** để quan sát request.

------------------------------------------------------------------------

## 🔧 5️⃣ Nếu muốn fix CORS trong Spring Boot

Ví dụ nhanh:

``` java
@CrossOrigin(origins = "http://localhost:3979")
@RestController
@RequestMapping("/api/greeting")
public class GreetingController {
}
```

Hoặc cấu hình global CORS.

------------------------------------------------------------------------

## 🎯 Mục tiêu học

-   Hiểu fetch() hoạt động thế nào
-   Phân biệt Origin khác nhau
-   Hiểu vì sao CORS chặn request
-   Quan sát HTTP request trong Network

------------------------------------------------------------------------

© 2026 giáo.làng with AI support
