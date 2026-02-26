package com.giaolang.mamnguqua.config;

import com.giaolang.mamnguqua.entity.Fruit;
import com.giaolang.mamnguqua.repository.FruitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration - Data Initialization
 *
 * Tự động khởi tạo database với 30 trái cây mẫu khi application start.
 *
 * Flow:
 * 1. Application khởi động
 * 2. Check xem database có dữ liệu không
 * 3. Nếu trống, thêm 30 trái cây
 * 4. Nếu đã có, skip (không duplicate)
 *
 * Chỉ chạy 1 lần duy nhất (chỉ khi database trống)
 *
 * Dữ liệu mẫu bao gồm:
 * - 5 trái cây mâm ngũ quả (Mãng Cầu, Sung, Dừa, Đu đủ, Xoài)
 * - 25 trái cây khác
 * - Giá từ 15,000 đến 70,000 VNĐ
 *
 * @author giáo.làng with AI support
 * @version 2026.0217
 * @since 2026
 */
@Configuration
@Slf4j
public class DataInitializationConfig implements CommandLineRunner {

    @Autowired
    private FruitRepository fruitRepository;

    /**
     * Initialize fruit data on application startup
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("  INITIALIZING FRUIT DATABASE DATA");
        log.info("========================================");

        // Check if database already has data
        long existingCount = fruitRepository.count();
        if (existingCount > 0) {
            log.info("✓ Database already has {} fruits. Skipping initialization.", existingCount);
            return;
        }

        log.info("Database is empty. Adding 30 sample fruits...");

        // Create 30 sample fruits
        List<Fruit> fruits = Arrays.asList(
                // ============ MÂM NGŨ QUẢ (5 trái cây) ============
                Fruit.builder()
                        .name("Mãng Cầu")
                        .desc("Mãng cầu chín mềm, vị ngọt thơm, giàu vitamin C, tốt cho tiêu hóa")
                        .price(new BigDecimal("35000"))
                        .build(),

                Fruit.builder()
                        .name("Sung")
                        .desc("Sung chín đỏ, vị ngọt tự nhiên, chứa enzym tiêu hóa, bổ máu")
                        .price(new BigDecimal("28000"))
                        .build(),

                Fruit.builder()
                        .name("Dừa")
                        .desc("Dừa tươi, nước dừa lạnh giải khát, cung cấp khoáng chất thiết yếu")
                        .price(new BigDecimal("25000"))
                        .build(),

                Fruit.builder()
                        .name("Đu Đủ")
                        .desc("Đu đủ chín vàng, vitamin A cao, làm sáng da, hỗ trợ tiêu hóa")
                        .price(new BigDecimal("22000"))
                        .build(),

                Fruit.builder()
                        .name("Xoài")
                        .desc("Xoài chín vàng, vị ngọt mịn, trái cây vua, giàu vitamin A và C")
                        .price(new BigDecimal("32000"))
                        .build(),

                // ============ TRÁI CÂY KHÁC (25 trái cây) ============
                Fruit.builder()
                        .name("Táo")
                        .desc("Táo đỏ ngon, giàu vitamin C, tốt cho sức khỏe tim mạch")
                        .price(new BigDecimal("25000"))
                        .build(),

                Fruit.builder()
                        .name("Chuối")
                        .desc("Chuối vàng chín, giàu kali, giúp tăng năng lượng và sức bền")
                        .price(new BigDecimal("15000"))
                        .build(),

                Fruit.builder()
                        .name("Cam")
                        .desc("Cam ngọt, giàu vitamin C, tăng cường miễn dịch, phòng chống cúm")
                        .price(new BigDecimal("30000"))
                        .build(),

                Fruit.builder()
                        .name("Dâu Tây")
                        .desc("Dâu tây tươi, chứa chất chống oxy hóa mạnh, giảm viêm")
                        .price(new BigDecimal("45000"))
                        .build(),

                Fruit.builder()
                        .name("Dứa")
                        .desc("Dứa ngon vàng, chứa enzyme bromelain, hỗ trợ tiêu hóa hiệu quả")
                        .price(new BigDecimal("28000"))
                        .build(),

                Fruit.builder()
                        .name("Dưa Hấu")
                        .desc("Dưa hấu lạnh, 90% nước, giải khát vào hè, giàu lycopene")
                        .price(new BigDecimal("20000"))
                        .build(),

                Fruit.builder()
                        .name("Nho Tím")
                        .desc("Nho tím chín, chứa resveratrol tốt cho tim, chống oxy hóa")
                        .price(new BigDecimal("50000"))
                        .build(),

                Fruit.builder()
                        .name("Việt Quất")
                        .desc("Việt quất xanh, chứa anthocyanin, tăng cường trí não sắc bén")
                        .price(new BigDecimal("60000"))
                        .build(),

                Fruit.builder()
                        .name("Mâm Xôi Đỏ")
                        .desc("Mâm xôi đỏ chín, giàu chất xơ, tốt cho tiêu hóa và giảm cân")
                        .price(new BigDecimal("55000"))
                        .build(),

                Fruit.builder()
                        .name("Chanh Tây")
                        .desc("Chanh tây, chua sảng khoái, sử dụng được nhiều cách nấu ăn")
                        .price(new BigDecimal("18000"))
                        .build(),

                Fruit.builder()
                        .name("Chanh Xanh")
                        .desc("Chanh xanh tươi, acid citric cao, khử khuẩn, làm sạch")
                        .price(new BigDecimal("20000"))
                        .build(),

                Fruit.builder()
                        .name("Kiwi Xanh")
                        .desc("Quả kiwi xanh, vitamin E cao, giúp chống lão hóa da")
                        .price(new BigDecimal("40000"))
                        .build(),

                Fruit.builder()
                        .name("Ổi Trắng")
                        .desc("Ổi trắng, giàu vitamin C, hỗ trợ miễn dịch, thanh lọc")
                        .price(new BigDecimal("16000"))
                        .build(),

                Fruit.builder()
                        .name("Chanh Leo")
                        .desc("Chanh leo, vị chua ngọt độc đáo, giàu chất xơ, tốt sức khỏe")
                        .price(new BigDecimal("38000"))
                        .build(),

                Fruit.builder()
                        .name("Thanh Long Tím")
                        .desc("Thanh long tím, ít calo, giàu vitamin, đẹp da tự nhiên")
                        .price(new BigDecimal("42000"))
                        .build(),

                Fruit.builder()
                        .name("Lê Vàng")
                        .desc("Lê vàng ngon, giàu chất xơ, hỗ trợ giảm cân an toàn")
                        .price(new BigDecimal("29000"))
                        .build(),

                Fruit.builder()
                        .name("Đào Tươi")
                        .desc("Đào tươi, vị ngọt mềm, giàu vitamin A và C, nuôi dưỡng da")
                        .price(new BigDecimal("31000"))
                        .build(),

                Fruit.builder()
                        .name("Lựu Đỏ")
                        .desc("Lựu đỏ, giàu chất chống oxy hóa, tốt cho tim mạch")
                        .price(new BigDecimal("65000"))
                        .build(),

                Fruit.builder()
                        .name("Mâm Xôi Đen")
                        .desc("Mâm xôi đen, vitamin K cao, giúp xương khỏe mạnh")
                        .price(new BigDecimal("58000"))
                        .build(),

                Fruit.builder()
                        .name("Dâu Đen")
                        .desc("Dâu đen, giàu sắt, hỗ trợ tạo máu, tăng năng lượng")
                        .price(new BigDecimal("52000"))
                        .build(),

                Fruit.builder()
                        .name("Dưa Lưới Cam")
                        .desc("Dưa lưới cam, vitamin A dồi dào, cắt lát ngon ngọt")
                        .price(new BigDecimal("35000"))
                        .build(),

                Fruit.builder()
                        .name("Dưa Lưới Xanh")
                        .desc("Dưa lưới xanh, ngọt tự nhiên, dễ tiêu hóa, bổ dưỡng")
                        .price(new BigDecimal("33000"))
                        .build(),

                Fruit.builder()
                        .name("Quýt Cam")
                        .desc("Quýt cam, vitamin C nhiều, dễ bóc vỏ, tiện ăn")
                        .price(new BigDecimal("26000"))
                        .build(),

                Fruit.builder()
                        .name("Bưởi Hồng")
                        .desc("Bưởi hồng, chua nhẹ, giúp giảm cân, hạ cholesterol")
                        .price(new BigDecimal("32000"))
                        .build(),

                Fruit.builder()
                        .name("Bơ")
                        .desc("Bơ chín mềm, chứa chất béo lành mạnh, làm trắng da hiệu quả")
                        .price(new BigDecimal("48000"))
                        .build(),

                Fruit.builder()
                        .name("Anh Đào Đỏ")
                        .desc("Anh đào đỏ, sắc đẹp tự nhiên, giàu chất chống viêm")
                        .price(new BigDecimal("70000"))
                        .build(),

                Fruit.builder()
                        .name("Mận Tím")
                        .desc("Mận tím, giàu chất xơ, hỗ trợ tiêu hóa tốt, nhuận tràng")
                        .price(new BigDecimal("27000"))
                        .build()
        );

        // Save all fruits to database
        fruitRepository.saveAll(fruits);

        log.info("✓ Successfully added {} fruits to database", fruits.size());
        log.info("========================================");
        log.info("  DATABASE INITIALIZATION COMPLETE");
        log.info("========================================");
    }
}
