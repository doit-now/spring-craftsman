package com.giaolang.mamnguqua.service;

import com.giaolang.mamnguqua.dto.FruitCreateRequest;
import com.giaolang.mamnguqua.dto.FruitResponse;
import com.giaolang.mamnguqua.entity.Fruit;
import com.giaolang.mamnguqua.exception.ResourceNotFoundException;
import com.giaolang.mamnguqua.repository.FruitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer - Business logic cho Fruit
 *
 * Chức năng:
 * - Xử lý CRUD operations (Create, Read, Update, Delete)
 * - Validate dữ liệu và business rules
 * - Manage database transactions
 * - Logging cho audit trail
 * - Convert entities <-> DTOs
 *
 * Tất cả methods được @Transactional để đảm bảo data consistency
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FruitService {

    /**
     * Repository dependency (auto-injected bởi Spring)
     */
    private final FruitRepository fruitRepository;

    /**
     * Lấy danh sách tất cả trái cây với phân trang và sắp xếp
     *
     * @param page      Số trang (zero-based, mặc định 0)
     * @param size      Số lượng trái cây mỗi trang (mặc định 10)
     * @param sortBy    Trường để sắp xếp: id, name, price, createdAt (mặc định: id)
     * @param direction Hướng sắp xếp: ASC hoặc DESC (mặc định: ASC)
     * @return Page<FruitResponse> chứa danh sách trái cây
     * @throws IllegalArgumentException nếu sortBy không hợp lệ
     */
    @Transactional(readOnly = true)
    public Page<FruitResponse> getAllFruits(int page, int size, String sortBy, String direction) {
        log.debug("Fetching fruits list - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        // Validate sortBy parameter
        String[] validSortFields = {"id", "name", "price", "createdAt"};
        boolean isValidField = java.util.Arrays.asList(validSortFields).contains(sortBy);
        if (!isValidField) {
            log.warn("Invalid sortBy field: {}", sortBy);
            throw new IllegalArgumentException(
                    String.format("Invalid sortBy field: %s. Valid fields: %s",
                            sortBy, String.join(", ", validSortFields)));
        }

        // Create Sort object
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);

        // Create Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch data
        Page<Fruit> fruitsPage = fruitRepository.findAll(pageable);
        log.info("Found {} fruits (total: {})", fruitsPage.getNumberOfElements(), fruitsPage.getTotalElements());

        return fruitsPage.map(this::convertToResponse);
    }

    /**
     * Tìm kiếm trái cây theo tên (case-insensitive, partial match)
     *
     * @param name Tên hoặc phần của tên trái cây để tìm kiếm
     * @param page Số trang (mặc định 0)
     * @param size Số lượng mỗi trang (mặc định 10)
     * @return Page<FruitResponse> chứa kết quả tìm kiếm
     */
    @Transactional(readOnly = true)
    public Page<FruitResponse> searchFruitsByName(String name, int page, int size) {
        log.debug("Searching fruits by name: {}", name);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<Fruit> results = fruitRepository.findByNameContainingIgnoreCase(name, pageable);

        log.info("Search found {} fruits matching '{}' (total: {})",
                results.getNumberOfElements(), name, results.getTotalElements());

        return results.map(this::convertToResponse);
    }

    /**
     * Lấy chi tiết một trái cây theo ID
     *
     * @param id ID của trái cây cần lấy
     * @return FruitResponse chứa thông tin chi tiết
     * @throws ResourceNotFoundException nếu trái cây không tồn tại
     */
    @Transactional(readOnly = true)
    public FruitResponse getFruitById(Long id) {
        log.debug("Fetching fruit detail with id: {}", id);

        Fruit fruit = fruitRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Fruit not found with id: {}", id);
                    return new ResourceNotFoundException("Trái cây với ID " + id + " không tồn tại");
                });

        log.info("Fruit found: {}", fruit.getName());
        return convertToResponse(fruit);
    }

    /**
     * Tạo trái cây mới
     *
     * @param request FruitCreateRequest chứa thông tin trái cây mới
     * @return FruitResponse của trái cây vừa tạo
     */
    @Transactional
    public FruitResponse createFruit(FruitCreateRequest request) {
        log.debug("Creating new fruit: {}", request.getName());

        Fruit fruit = Fruit.builder()
                .name(request.getName())
                .desc(request.getDesc())
                .price(request.getPrice())
                .build();

        Fruit savedFruit = fruitRepository.save(fruit);
        log.info("Fruit created successfully with id: {} - name: {}", savedFruit.getId(), savedFruit.getName());

        return convertToResponse(savedFruit);
    }

    /**
     * Cập nhật thông tin trái cây
     *
     * @param id      ID của trái cây cần cập nhật
     * @param request FruitCreateRequest chứa thông tin cập nhật
     * @return FruitResponse của trái cây sau cập nhật
     * @throws ResourceNotFoundException nếu trái cây không tồn tại
     */
    @Transactional
    public FruitResponse updateFruit(Long id, FruitCreateRequest request) {
        log.debug("Updating fruit with id: {}", id);

        Fruit fruit = fruitRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Fruit not found with id: {} for update", id);
                    return new ResourceNotFoundException("Trái cây với ID " + id + " không tồn tại");
                });

        fruit.setName(request.getName());
        fruit.setDesc(request.getDesc());
        fruit.setPrice(request.getPrice());

        Fruit updatedFruit = fruitRepository.save(fruit);
        log.info("Fruit updated successfully with id: {}", updatedFruit.getId());

        return convertToResponse(updatedFruit);
    }

    /**
     * Xóa trái cây theo ID
     *
     * @param id ID của trái cây cần xóa
     * @throws ResourceNotFoundException nếu trái cây không tồn tại
     */
    @Transactional
    public void deleteFruitById(Long id) {
        log.debug("Deleting fruit with id: {}", id);

        if (!fruitRepository.existsById(id)) {
            log.warn("Fruit not found with id: {} for deletion", id);
            throw new ResourceNotFoundException("Trái cây với ID " + id + " không tồn tại");
        }

        fruitRepository.deleteById(id);
        log.info("Fruit deleted successfully with id: {}", id);
    }

    /**
     * Lấy tổng số lượng trái cây trong database
     *
     * @return Tổng số trái cây
     */
    @Transactional(readOnly = true)
    public long getTotalFruitCount() {
        long count = fruitRepository.count();
        log.debug("Total fruits in database: {}", count);
        return count;
    }

    /**
     * Helper method - Convert Fruit entity sang FruitResponse DTO
     *
     * @param fruit Fruit entity
     * @return FruitResponse DTO
     */
    private FruitResponse convertToResponse(Fruit fruit) {
        return FruitResponse.builder()
                .id(fruit.getId())
                .name(fruit.getName())
                .desc(fruit.getDesc())
                .price(fruit.getPrice())
                .createdAt(fruit.getCreatedAt())
                .updatedAt(fruit.getUpdatedAt())
                .build();
    }
}
