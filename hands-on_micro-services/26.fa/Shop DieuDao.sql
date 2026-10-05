USE master;
GO

IF DB_ID('MSS_SHOP_DIEUDAO') IS NULL
BEGIN
    CREATE DATABASE MSS_SHOP_DIEUDAO;
END
GO

USE MSS_SHOP_DIEUDAO;
GO

IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID('dbo.accounts', 'U') IS NOT NULL DROP TABLE dbo.accounts;
GO

CREATE TABLE accounts (
    account_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    phone NVARCHAR(15) NULL,
    date_of_birth DATE NOT NULL,
    membership_points INT NOT NULL CONSTRAINT DF_accounts_points DEFAULT 100,
    status NVARCHAR(10) NOT NULL,
    registered_date DATE NOT NULL,
    CONSTRAINT CK_accounts_points CHECK (membership_points >= 0),
    CONSTRAINT CK_accounts_status CHECK (status IN ('ACTIVE', 'LOCKED'))
);
GO

CREATE TABLE orders (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_date DATE NOT NULL,
    expected_delivery_date DATE NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status NVARCHAR(15) NOT NULL,
    payment_method NVARCHAR(10) NULL,
    account_id BIGINT NOT NULL,
    CONSTRAINT CK_orders_amount CHECK (total_amount >= 0),
    CONSTRAINT CK_orders_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_orders_payment CHECK (
        payment_method IS NULL OR payment_method IN ('CASH', 'CARD', 'TRANSFER')
    ),
    CONSTRAINT CK_orders_delivery CHECK (expected_delivery_date > order_date),
    --CONSTRAINT FK_orders_accounts FOREIGN KEY (account_id)
    --    REFERENCES accounts(account_id)
);
GO

INSERT INTO accounts(full_name, email, phone, date_of_birth, membership_points, status, registered_date)
VALUES
(N'Nguyễn Văn An',     'an.nguyen@example.com',   '0901000001', '2000-05-10', 120, 'ACTIVE', '2026-08-01'),
(N'Trần Thị Bình',     'binh.tran@example.com',   '0901000002', '2001-03-15', 350, 'ACTIVE', '2026-08-02'),
(N'Lê Minh Cường',     'cuong.le@example.com',    '0901000003', '1999-11-20', 100, 'ACTIVE', '2026-08-03'),
(N'Phạm Thu Dung',     'dung.pham@example.com',   NULL,         '2002-07-01', 180, 'LOCKED', '2026-08-04'),
(N'Hoàng Gia Huy',     'huy.hoang@example.com',   '0901000005', '2000-12-12', 500, 'ACTIVE', '2026-08-05'),
(N'Võ Khánh Linh',     'linh.vo@example.com',     '0901000006', '2003-01-21', 260, 'ACTIVE', '2026-08-06'),
(N'Đặng Quốc Nam',     'nam.dang@example.com',    '0901000007', '1998-09-09', 100, 'LOCKED', '2026-08-07'),
(N'Bùi Ngọc Oanh',     'oanh.bui@example.com',    '0901000008', '2001-04-25', 420, 'ACTIVE', '2026-08-08'),
(N'Đỗ Minh Phúc',      'phuc.do@example.com',     '0901000009', '2002-02-18', 150, 'ACTIVE', '2026-08-09'),
(N'Ngô Thảo Quyên',    'quyen.ngo@example.com',   '0901000010', '2000-06-30', 230, 'ACTIVE', '2026-08-10'),
(N'Phan Đức Sơn',      'son.phan@example.com',    NULL,         '1999-10-10', 100, 'ACTIVE', '2026-08-11'),
(N'Vũ Thanh Trang',    'trang.vu@example.com',    '0901000012', '2001-08-14', 310, 'ACTIVE', '2026-08-12');
GO

INSERT INTO orders(order_date, expected_delivery_date, total_amount, status, payment_method, account_id)
VALUES
('2026-09-01','2026-09-04',  250000.00,'PENDING',   'CASH',     1),
('2026-09-02','2026-09-06', 1250000.00,'COMPLETED', 'CARD',     2),
('2026-09-03','2026-09-07',  890000.00,'PENDING',   'TRANSFER', 3),
('2026-09-04','2026-09-08',  430000.00,'CANCELLED', NULL,       5),
('2026-09-05','2026-09-10',  670000.00,'COMPLETED', 'CASH',     6),
('2026-09-06','2026-09-09',  150000.00,'PENDING',   'CARD',     8),
('2026-09-07','2026-09-12', 2150000.00,'PENDING',   'TRANSFER', 9),
('2026-09-08','2026-09-11',  990000.00,'COMPLETED', 'CARD',     10),
('2026-09-09','2026-09-15',  320000.00,'PENDING',   NULL,       11),
('2026-09-10','2026-09-14',  760000.00,'COMPLETED', 'CASH',     12),
('2026-09-11','2026-09-16', 1400000.00,'PENDING',   'TRANSFER', 1),
('2026-09-12','2026-09-17',  510000.00,'CANCELLED', 'CARD',     2),
('2026-09-13','2026-09-18',  280000.00,'PENDING',   'CASH',     3),
('2026-09-14','2026-09-19',  610000.00,'COMPLETED', 'TRANSFER', 5),
('2026-09-15','2026-09-20',  845000.00,'PENDING',   'CARD',     6),
('2026-09-16','2026-09-21', 1100000.00,'COMPLETED', 'CASH',     8),
('2026-09-17','2026-09-22',  475000.00,'PENDING',   NULL,       9),
('2026-09-18','2026-09-23',  930000.00,'CANCELLED', 'CARD',     10),
('2026-09-19','2026-09-24',  365000.00,'PENDING',   'TRANSFER', 11),
('2026-09-20','2026-09-25', 1780000.00,'COMPLETED', 'CARD',     12);
GO

SELECT * FROM accounts ORDER BY account_id;
SELECT * FROM orders ORDER BY order_id;
GO
