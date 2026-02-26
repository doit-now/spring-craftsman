package com.giaolang.mamnguqua.exception;

/**
 * Custom Exception - Resource không tìm thấy
 *
 * Được throw khi:
 * - Lấy chi tiết fruit với ID không tồn tại
 * - Cập nhật/Xóa fruit với ID không tồn tại
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor với message
     *
     * @param message Mô tả lỗi
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     *
     * @param message Mô tả lỗi
     * @param cause   Nguyên nhân gốc
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
