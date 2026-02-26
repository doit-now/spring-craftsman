package com.giaolang.mamnguqua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Entry Point - Fruit Management System
 *
 * REST API để quản lý trái cây với các tính năng:
 * - JWT Authentication (đăng nhập, lấy token)
 * - CRUD operations cho Fruit
 * - Pagination, sorting, searching
 * - Swagger/OpenAPI documentation
 * - Comprehensive error handling
 * - 30 sample fruits pre-loaded
 *
 * Server: http://localhost:6969
 * Swagger: http://localhost:6969/swagger-ui.html
 *
 * Default credentials:
 * - Username: admin
 * - Password: 123456
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@SpringBootApplication
public class MamNguQuaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MamNguQuaApplication.class, args);

        // Print startup info
        printStartupInfo();
    }

    /**
     * Print startup information to console
     */
    private static void printStartupInfo() {
        System.out.println("""
                
                ╔═══════════════════════════════════════════════════════════╗
                ║                                                           ║
                ║   🍎                 MÂM NGŨ QUẢ                    🍎   ║
                ║   🍎 FRUIT MANAGEMENT SYSTEM - STARTED SUCCESSFULLY 🍎   ║
                ║                                                           ║
                ╠═══════════════════════════════════════════════════════════╣
                ║                                                           ║
                ║  📌 Server:    http://localhost:6969                      ║
                ║  📚 Swagger:   http://localhost:6969/swagger-ui.html      ║
                ║                                                           ║
                ║  🔑 Login Credentials:                                    ║
                ║     • Username: admin                                     ║
                ║     • Password: 123456                                    ║
                ║                                                           ║
                ║  ✅ 30 Sample Fruits Pre-loaded in Database              ║
                ║                                                           ║
                ║  🚀 API Endpoints:                                        ║
                ║     • POST   /api/auth/login                              ║
                ║     • GET    /api/fruits                                  ║
                ║     • GET    /api/fruits/{id}                             ║
                ║     • GET    /api/fruits/search                           ║
                ║     • POST   /api/fruits                                  ║
                ║     • PUT    /api/fruits/{id}                             ║
                ║     • DELETE /api/fruits/{id}                             ║
                ║                                                           ║
                ╚═══════════════════════════════════════════════════════════╝
                """);
    }
}
