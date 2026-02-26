package com.giaolang.mamnguqua.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO - Response để trả về thông tin trái cây
 *
 * Được sử dụng để trả về dữ liệu trong:
 * - GET /api/fruits (danh sách)
 * - GET /api/fruits/{id} (chi tiết)
 * - POST /api/fruits (tạo mới)
 * - PUT /api/fruits/{id} (cập nhật)
 *
 * Lợi ích:
 * - Tránh lộ thông tin nội bộ của entity
 * - Kiểm soát cấu trúc JSON response
 * - Tách biệt logic API từ entity
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FruitResponse {

    /**
     * ID của trái cây
     */
    private Long id;

    /**
     * Tên trái cây
     */
    private String name;

    /**
     * Mô tả chi tiết
     */
    private String desc;

    /**
     * Giá bán (VNĐ)
     */
    private BigDecimal price;

    /**
     * Thời gian tạo record
     */
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật cuối cùng
     */
    private LocalDateTime updatedAt;
}
