package com.giaolang.mamnguqua.repository;

import com.giaolang.mamnguqua.entity.Fruit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository - Data Access Object cho Fruit entity
 *
 * Extends JpaRepository để cung cấp:
 * - findAll(), findById(), save(), delete() (mặc định)
 * - Custom query methods (định nghĩa bên dưới)
 *
 * Spring Data JPA tự động implement interface này
 * và tạo SQL queries dựa trên method names
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Repository
public interface FruitRepository extends JpaRepository<Fruit, Long> {

    /**
     * Tìm kiếm trái cây theo tên (case-insensitive)
     * <p>
     * Spring Data sẽ generate SQL query:
     * SELECT * FROM fruits WHERE UPPER(name) LIKE UPPER('%name%')
     *
     * @param name     Tên hoặc phần của tên trái cây
     * @param pageable Pagination info
     * @return Page chứa danh sách Fruit phù hợp
     */
    Page<Fruit> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
