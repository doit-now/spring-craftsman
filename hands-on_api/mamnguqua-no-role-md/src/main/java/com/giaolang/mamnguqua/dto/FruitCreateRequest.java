package com.giaolang.mamnguqua.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO - Request để tạo hoặc cập nhật trái cây (Create/Update)
 *
 * Được sử dụng trong:
 * - POST /api/fruits (tạo mới)
 * - PUT /api/fruits/{id} (cập nhật)
 *
 * Validation:
 * - name: Bắt buộc, 3-100 ký tự
 * - desc: Bắt buộc, 10-500 ký tự
 * - price: Dương, 2 chữ số thập phân
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FruitCreateRequest {

    /**
     * Tên trái cây
     */
    @NotBlank(message = "Tên trái cây không được để trống")
    @Size(min = 2, max = 100, message = "Tên trái cây phải từ 2 đến 100 ký tự")
    private String name;

    /**
     * Mô tả chi tiết trái cây
     */
    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 10, max = 500, message = "Mô tả phải từ 10 đến 500 ký tự")
    private String desc;

    /**
     * Giá bán trái cây (VNĐ)
     */
    @Positive(message = "Giá phải lớn hơn 0")
    @DecimalMin(value = "0.01", message = "Giá trái cây phải ít nhất 0.01")
    @Digits(integer = 10, fraction = 2, message = "Giá phải có tối đa 2 chữ số thập phân")
    private BigDecimal price;
}
