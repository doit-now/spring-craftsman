-- =============================================
-- schema.sql
-- Create tables Category and Fruit
-- SQL Server
-- =============================================

DROP TABLE IF EXISTS Fruit;
DROP TABLE IF EXISTS Category;

-- =========================
-- Category table
-- =========================
CREATE TABLE Category (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL
);

-- =========================
-- Fruit table
-- =========================
CREATE TABLE Fruit (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255),
    price DECIMAL(10,2),
    expired_date DATE,
    category_id INT,

    CONSTRAINT FK_Fruit_Category
        FOREIGN KEY (category_id)
        REFERENCES Category(id)
);