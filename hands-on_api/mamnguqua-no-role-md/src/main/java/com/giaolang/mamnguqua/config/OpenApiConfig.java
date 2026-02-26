package com.giaolang.mamnguqua.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration - OpenAPI 3.0 (Swagger) Documentation
 *
 * Tạo API documentation tự động từ code annotations
 * Truy cập tại: http://localhost:6969/swagger-ui.html
 *
 * Cung cấp:
 * - Interactive API documentation
 * - Try-it-out feature để test endpoints
 * - Detailed request/response schemas
 * - Security scheme definition (Bearer JWT)
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Configuration
public class OpenApiConfig {

    /**
     * Define OpenAPI specification
     *
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI fruitManagementOpenAPI() {
        return new OpenAPI()
                // API Info
                .info(new Info()
                        .title("🍎 Mâm ngũ quả API")
                        .description("REST API để quản lý tiệm trái cây với JWT Authentication\n\n" +
                                "Default credentials: admin/123456\n\n" +
                                "30 fruits pre-loaded in database")
                        .version("2026.0217")
                        .contact(new Contact()
                                .name("giáo.làng - AI mãi đỉnh")
                                .email("hoangnt20@fe.edu.vn")
                                .url("http://localhost:6969"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // Security scheme (Bearer JWT)
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Lấy JWT token từ /api/auth/login endpoint")))

                // Apply security globally
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"));
    }
}
