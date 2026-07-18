/* =========================================================
   DỮ LIỆU MẪU: 3 PHÒNG BAN
   Công ty phân phối hoa quả Shop Điều Đào
   ========================================================= */

INSERT INTO departments
(
    name,
    code,
    effective_date,
    status,
    location,
    parent_id
)
VALUES
(
    N'Phòng Công nghệ thông tin',
    'IT01',
    '2022-01-10',
    'ACTIVE',
    N'Văn phòng Quận 1',
    NULL
),
(
    N'Phòng Thu mua',
    'PUR01',
    '2022-03-15',
    'ACTIVE',
    N'Chợ đầu mối Thủ Đức',
    NULL
),
(
    N'Phòng Kho vận',
    'WAR01',
    '2022-06-20',
    'ACTIVE',
    N'Kho hàng Bình Tân',
    NULL
);
GO


/* =========================================================
   DỮ LIỆU MẪU: 9 NHÂN VIÊN
   Mỗi phòng ban có đúng 3 nhân viên
   position chỉ dùng: Manager, Developer, Staff
   ========================================================= */

INSERT INTO employees
(
    full_name,
    email,
    position,
    status,
    start_date,
    end_date,
    department_id
)
VALUES

/* =========================================================
   PHÒNG CÔNG NGHỆ THÔNG TIN
   ========================================================= */

(
    N'Nguyễn Thị Lan',
    'lan.nguyen@shopdieudao.com',
    'Manager',
    'ACTIVE',
    '2022-02-01',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'IT01'
    )
),
(
    N'Trần Thị Hồng',
    'hong.tran@shopdieudao.com',
    'Developer',
    'ACTIVE',
    '2023-04-10',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'IT01'
    )
),
(
    N'Lê Thị Huệ',
    'hue.le@shopdieudao.com',
    'Developer',
    'ACTIVE',
    '2024-01-15',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'IT01'
    )
),

/* =========================================================
   PHÒNG THU MUA
   ========================================================= */

(
    N'Phạm Thị Mơ',
    'mo.pham@shopdieudao.com',
    'Manager',
    'ACTIVE',
    '2022-07-01',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'PUR01'
    )
),
(
    N'Võ Thị Mận',
    'man.vo@shopdieudao.com',
    'Staff',
    'ACTIVE',
    '2023-05-20',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'PUR01'
    )
),
(
    N'Đặng Thị Đào',
    'dao.dang@shopdieudao.com',
    'Staff',
    'ACTIVE',
    '2024-03-12',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'PUR01'
    )
),

/* =========================================================
   PHÒNG KHO VẬN
   ========================================================= */

(
    N'Nguyễn Văn Tèo',
    'teo.nguyen@shopdieudao.com',
    'Manager',
    'ACTIVE',
    '2023-02-01',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'WAR01'
    )
),
(
    N'Trần Văn Tí',
    'ti.tran@shopdieudao.com',
    'Staff',
    'ACTIVE',
    '2024-06-18',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'WAR01'
    )
),
(
    N'Lê Văn Bờm',
    'bom.le@shopdieudao.com',
    'Staff',
    'ACTIVE',
    '2025-01-05',
    NULL,
    (
        SELECT department_id
        FROM departments
        WHERE code = 'WAR01'
    )
);
GO


/* =========================================================
   KIỂM TRA NHÂN VIÊN VÀ PHÒNG BAN
   ========================================================= */

SELECT
    e.employee_id,
    e.full_name,
    e.email,
    e.position,
    e.status,
    e.start_date,
    e.end_date,
    d.code AS department_code,
    d.name AS department_name
FROM employees e
JOIN departments d
    ON e.department_id = d.department_id
ORDER BY
    d.department_id,
    e.employee_id;
GO


/* =========================================================
   KIỂM TRA SỐ NHÂN VIÊN CỦA MỖI PHÒNG BAN
   ========================================================= */

SELECT
    d.department_id,
    d.code,
    d.name,
    COUNT(e.employee_id) AS total_employees
FROM departments d
LEFT JOIN employees e
    ON d.department_id = e.department_id
GROUP BY
    d.department_id,
    d.code,
    d.name
ORDER BY d.department_id;
GO