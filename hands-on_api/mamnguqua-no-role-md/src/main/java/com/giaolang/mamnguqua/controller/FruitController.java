package com.giaolang.mamnguqua.controller;

import com.giaolang.mamnguqua.dto.FruitCreateRequest;
import com.giaolang.mamnguqua.dto.FruitResponse;
import com.giaolang.mamnguqua.service.FruitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller - Fruit Management API Endpoints
 *
 * REST API để quản lý dữ liệu trái cây.
 * Tất cả endpoints được protect bằng JWT token (require Authorization header)
 *
 * Endpoints:
 * - GET /api/fruits - Danh sách trái cây (phân trang, sắp xếp)
 * - GET /api/fruits/{id} - Chi tiết trái cây
 * - GET /api/fruits/search - Tìm kiếm trái cây theo tên
 * - POST /api/fruits - Tạo trái cây mới
 * - PUT /api/fruits/{id} - Cập nhật trái cây
 * - DELETE /api/fruits/{id} - Xóa trái cây
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@RestController
@RequestMapping("/api/fruits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fruit Management", description = "API để quản lý trái cây")
@SecurityRequirement(name = "Bearer Authentication")
public class FruitController {

    /**
     * Service layer dependency (auto-injected) by Lombok's @RequiredArgsConstructor
     */
    private final FruitService fruitService;

    /**
     * GET /api/fruits - Lấy danh sách trái cây
     * <p>
     * Lấy tất cả trái cây với phân trang, sắp xếp, và lọc.
     * Mặc định: 10 trái cây per trang, sắp xếp theo id (ASC)
     * <p>
     * Query Parameters:
     * - page: Số trang (zero-based, mặc định 0)
     * - size: Số lượng items per trang (mặc định 10)
     * - sortBy: Trường sắp xếp - id, name, price, createdAt (mặc định: id)
     * - direction: Hướng sắp xếp - ASC hoặc DESC (mặc định: ASC)
     *
     * @param page      Số trang
     * @param size      Số lượng per trang
     * @param sortBy    Trường sắp xếp
     * @param direction Hướng sắp xếp
     * @return ResponseEntity<Page < FruitResponse>>
     */
    @GetMapping
    @Operation(
            summary = "Lấy danh sách trái cây",
            description = "Lấy danh sách tất cả trái cây với phân trang, sắp xếp (tối đa 30 items/page)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Danh sách trái cây lấy thành công"),
            @ApiResponse(responseCode = "400", description = "Request parameters không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - cần JWT token")
    })
    public ResponseEntity<Page<FruitResponse>> getAllFruits(
            @Parameter(description = "Số trang (zero-based, mặc định 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng items per trang (max 30)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Trường sắp xếp: id, name, price, createdAt")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Hướng sắp xếp: ASC hoặc DESC")
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/fruits - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        Page<FruitResponse> fruits = fruitService.getAllFruits(page, size, sortBy, direction);

        return ResponseEntity.ok(fruits);
    }

    /**
     * GET /api/fruits/search - Tìm kiếm trái cây theo tên
     * <p>
     * Tìm kiếm trái cây bằng tên (không phân biệt hoa/thường, tìm kiếm từng phần)
     *
     * @param name Tên hoặc phần tên trái cây (required)
     * @param page Số trang (mặc định 0)
     * @param size Số lượng per trang (mặc định 10)
     * @return ResponseEntity<Page < FruitResponse>>
     */
    @GetMapping("/search")
    @Operation(
            summary = "Tìm kiếm trái cây theo tên",
            description = "Tìm kiếm trái cây theo tên (không phân biệt hoa/thường, partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<FruitResponse>> searchFruits(
            @Parameter(description = "Tên hoặc phần tên trái cây", required = true)
            @RequestParam String name,

            @Parameter(description = "Số trang (mặc định 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng per trang (mặc định 10)")
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/fruits/search - name: {}", name);

        Page<FruitResponse> results = fruitService.searchFruitsByName(name, page, size);

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/fruits/{id} - Lấy chi tiết trái cây
     *
     * @param id ID của trái cây (required)
     * @return ResponseEntity<FruitResponse>
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết trái cây",
            description = "Lấy thông tin chi tiết của một trái cây theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FruitResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Trái cây không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<FruitResponse> getFruitById(
            @Parameter(description = "ID của trái cây", required = true)
            @PathVariable Long id) {

        log.info("GET /api/fruits/{}", id);

        FruitResponse fruit = fruitService.getFruitById(id);

        return ResponseEntity.ok(fruit);
    }

    /**
     * POST /api/fruits - Tạo trái cây mới
     *
     * @param request FruitCreateRequest chứa name, desc, price
     * @return ResponseEntity<FruitResponse> (HTTP 201 Created)
     */
    @PostMapping
    @Operation(
            summary = "Tạo trái cây mới",
            description = "Tạo một trái cây mới trong hệ thống"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tạo thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FruitResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<FruitResponse> createFruit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin trái cây mới",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FruitCreateRequest.class)
                    )
            )
            @Valid @RequestBody FruitCreateRequest request) {

        log.info("POST /api/fruits - Create new fruit: {}", request.getName());

        FruitResponse createdFruit = fruitService.createFruit(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdFruit);
    }

    /**
     * PUT /api/fruits/{id} - Cập nhật trái cây
     *
     * @param id      ID của trái cây cần cập nhật
     * @param request FruitCreateRequest chứa name, desc, price
     * @return ResponseEntity<FruitResponse>
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Cập nhật trái cây",
            description = "Cập nhật thông tin của một trái cây"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FruitResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Trái cây không tồn tại"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<FruitResponse> updateFruit(
            @Parameter(description = "ID của trái cây", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin cập nhật",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FruitCreateRequest.class)
                    )
            )
            @Valid @RequestBody FruitCreateRequest request) {

        log.info("PUT /api/fruits/{} - Update fruit", id);

        FruitResponse updatedFruit = fruitService.updateFruit(id, request);

        return ResponseEntity.ok(updatedFruit);
    }

    /**
     * DELETE /api/fruits/{id} - Xóa trái cây
     *
     * @param id ID của trái cây cần xóa
     * @return ResponseEntity<Void> (HTTP 204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xóa trái cây",
            description = "Xóa một trái cây khỏi hệ thống"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Trái cây không tồn tại"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteFruit(
            @Parameter(description = "ID của trái cây", required = true)
            @PathVariable Long id) {

        log.info("DELETE /api/fruits/{} - Delete fruit", id);

        fruitService.deleteFruitById(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/fruits/count/total - Lấy tổng số trái cây
     *
     * @return ResponseEntity<Long>
     */
    @GetMapping("/count/total")
    @Operation(
            summary = "Lấy tổng số trái cây",
            description = "Lấy tổng số lượng trái cây trong database"
    )
    @ApiResponse(responseCode = "200", description = "Lấy thành công")
    public ResponseEntity<Long> getTotalFruitCount() {
        log.info("GET /api/fruits/count/total");

        long count = fruitService.getTotalFruitCount();

        return ResponseEntity.ok(count);
    }
}
