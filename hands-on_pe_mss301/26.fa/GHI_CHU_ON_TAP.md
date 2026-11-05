# GHI CHÚ TỔNG HỢP ĐỀ THI - SE686868

## 1. Công nghệ
- Spring Boot **4.1.2**, GA mới nhất kiểm tra ngày 04/11/2026; JDK **25**; Spring Cloud **2025.1.4**; Springdoc **3.2.0**.
- Room và Reservation: MVC (blocking) + JPA; Gateway: WebFlux (reactive Netty). KHÔNG gắn JPA trên Gateway.
- 3 project riêng, một parent POM để dễ import/build. Package root: `fu.se686868.room`, `fu.se686868.reservation`, `fu.se686868.gateway`.
- Swagger UI các cổng: Room 8081, Reservation 8082, Gateway 8080. Gateway tập hợp OpenAPI docs thành 3 lựa chọn.

## 2. Nghiệp vụ cần nhớ / xem comment trong code
1. Room number unique, giá > 0, capacity 1..10, floor >=1; loại phòng/status là enum.
2. Room CREATE mặc định AVAILABLE (bỏ status client); Room DELETE đặt MAINTENANCE, không xóa.
3. Reservation CREATE: room phải tồn tại và AVAILABLE; khách 1..capacity; out > in; tổng tiền = pricePerNight * số đêm.
4. Reservation CREATE mặc định CONFIRMED; DELETE đặt CANCELLED, không xóa.
5. Kết quả GET reservation trả ReservationDetailDTO có `room: RoomDTO`; tạo/sửa trả ReservationDTO có roomId.
6. Chặn ngày chồng lấn các reservation CONFIRMED, CHECKED_IN (ngoài đề nhưng cần tránh double booking). Khoảng [checkIn, checkOut) cho phép lượt sau checkIn đúng ngày lượt trước checkOut.
7. `totalAmount` chỉ do server tính, dù request truyền giá khác vẫn bỏ qua.
8. PUT chỉ sửa thuộc tính được truyền; trường bỏ trống (null) giữ nguyên. Update ngày, phòng, khách phải tính lại giá và xác minh room.
9. Không cho sửa các reservation CANCELLED/CHECKED_OUT (business rule đề xuất, không có trong đề gốc).
10. Pagination phải lọc ở DB trước khi page; page 0-based, size mặc định 10, max 100.

## 3. Mâu thuẫn/chỗ thiếu trong đề và cách xử lý
- **Đề yêu cầu JDK 21, Boot >=3.5.11** nhưng bài ôn tập cố ý dùng **JDK 25, Boot 4.1.2** theo yêu cầu mới; khi thi thật tuân thủ giám thị.
- **Đề dùng PUT nhưng nói partial update**: xử lý partial PUT để phù hợp thang chấm, giải thích về lý thuyết đây là PATCH.
- **406 validation**: theo đề, mặc dù REST thường là 400 hoặc 422. **226 duplicate room** theo đề, mặc dù REST đúng hơn là 409 Conflict. 404=4; 400=5; 500=0.
- **Room status chỉ có AVAILABLE/OCCUPIED/MAINTENANCE, không có RESERVED**. Booking KHÔNG đổi status sang OCCUPIED vì người đặt có thể chưa check-in và đề không yêu cầu API nội bộ cập nhật phòng. Thay vào đó kiểm tra lịch đặt chồng lấn trong ReservationService.
- **Tính sẵn phòng**: không chỉ check AVAILABLE; phải tính khoảng ngày. Tuy nhiên check + save vẫn có race condition khi nhiều request đồng thời; production cần DB lock/transaction isolation/constraint phù hợp.
- **Chia sẻ database** (có FK reservations.room_id -> rooms.room_id) đi ngược nguyên tắc database-per-service. Tạm giữ đúng schema đề, không tạo Entity Room trong ReservationService, gọi Feign lấy dữ liệu.
- **Script gốc** cắt dở tại `CHECK CONSTRAINT FK_...` và thứ tự tạo reservations trước rooms làm lỗi FK. File `database/01_schema.sql` đã sửa, tạo rooms trước reservations.
- **Kiểu ID**: DB `INT IDENTITY` nhưng entity/DTO ghi `long`. Hibernate mapping Long -> SQL int có thể chạy cho giá trị trong phạm vi; với hbm2ddl=none cần kiểm tra mapping thực tế. Muốn tuyệt đối thống nhất nên sửa DB lên BIGINT hoặc Java thành Integer; bản ôn tập giữ Long theo DTO đề.
- **Ngày**: trường reservation dùng java.util.Date theo đề, JSON `dd/MM/yyyy`, SQL `date`, không có giờ. `timestamp` của envelope dùng ISO 8601 UTC, không nhầm lẫn hai format này.
- **Security**: đề ghi Spring Security ở Gateway nhưng không nêu authentication. Để sinh viên kiểm thử không cần token, cấu hình permitAll; production không được áp dụng.
- **CORS**: chỉ đặt một CorsWebFilter trên Gateway, origins=* không cho credentials. Khi đưa lên web thật giới hạn origins.
- **Swagger**: Swagger UI Gateway gom docs; việc Try it out có thể cần chọn đúng URL server nếu service docs tự khai báo host nội bộ. Với bài ôn tập ưu tiên test URL 8080.
- **Nếu RoomService tắt**: ReservationService khi tạo/cập nhật/GET chi tiết sẽ thất bại; đây là dependency đồng bộ và cần timeout/circuit breaker ở hệ thống lớn.
- **Đề ghi áp dụng MVC & Layer cho Gateway**: Gateway WebFlux đúng yêu cầu mới không cần Repository/Entity, áp dụng Layer cho hai service xử lý DB.

## 4. Những điểm giảng viên nên hỏi sinh viên
- Vì sao Controller không chứa SQL? Vì sao dùng Service Interface và implementation?
- @Entity, @Id, @GeneratedValue(IDENTITY), @Column, @Enumerated(STRING) khác nhau thế nào?
- Vì sao `pricePerNight`/`totalAmount` dùng BigDecimal, không dùng double?
- HTTP status khác trường `status` của ApiResponseDTO thế nào?
- `@FeignClient` là client HTTP đồng bộ; nếu lỗi 404 room, mapping về error nào?
- 2 khoảng ngày chồng lấn kiểm tra bằng điều kiện nào?
- `@Transactional` bảo đảm gì, **không** bảo đảm điều gì khi service khác và nhiều request cùng lúc?
- Gateway WebFlux có phải là WebFlux Controller cho 2 service không? KHÔNG.
- Vì sao nên tránh `ddl-auto=update` khi đã có script DB chuẩn?
- Vì sao Server tính totalAmount và bỏ qua giá do client gửi?

## 5. Checklist thử API
- Tạo phòng đúng -> HTTP 201/status 1; trùng roomNumber -> 226/status 3.
- Thiếu roomNumber, giá âm, capacity=11 -> 406/status 2.
- RoomId không có -> 404/status 4; DELETE phòng -> MAINTENANCE.
- Tạo booking với roomId không có -> 404/status 4; room MAINTENANCE -> 400/status 5.
- Số khách vượt capacity, out <= in -> 406/status 2.
- 2 booking cùng phòng trùng ngày -> 400/status 5; không trùng thì được.
- Tính tiền bằng pricePerNight * nights (vd 850000*3 = 2550000.00).
- GET detail có room object; GET list có page/size/totalPages/totalElements/first/last/content.
- PUT một field giữ nguyên field khác; DELETE reservation -> CANCELLED.
- Swagger ở cả 3 port và gateway route `/api/rooms`, `/api/reservations`.

## 6. Giới hạn bản mã nguồn
Có 2 unit tests mẫu (room default status và reservation tính tiền). Bộ mã nguồn được dựng để học và đọc source; môi trường tạo tệp không có sẵn JDK 25 / Maven / SQL Server để chạy end-to-end. Cần kiểm tra `mvn clean package`, kết nối SQL Server và smoke test Swagger trên máy có đủ công cụ. Các lựa chọn mở rộng để production: atomic booking, bảo mật thực, logging/monitoring, exception contract chuẩn, contract tests, tránh shared DB.
