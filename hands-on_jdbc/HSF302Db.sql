CREATE DATABASE HSF302
GO

USE HSF302
GO

CREATE TABLE Subject (
    Code        CHAR(10)	PRIMARY KEY,    -- e.g. CSI104
    Name        NVARCHAR(50)	NOT NULL,       -- e.g. Introduction to Programming
    Description NVARCHAR(255)   NULL,           -- brief course description
    Credits     INT             NOT NULL,       -- credit units
    StudyHours  INT             NOT NULL        -- total time allocation
);
GO

-- Insert sample data for 5 IT subjects
INSERT INTO dbo.Subject (Code, Name, Description, Credits, StudyHours) VALUES
    ('PRO192', 'Object-Oriented Programming', 'This course includes aspects of OOP, learns to build reusable objects...', 3, 45),
    ('DBI202', 'Database Systems', 'This course includes aspects of database management basic concepts...', 3, 45),
    ('HSF302', 'Working with Spring Framework', 'Spring Framework, Spring Boot, which provides a comprehensive ecosystem...', 3, 45),
    ('SWT301', 'Software Testing', 'General principles of software testing, including its purposes and objectives...', 3, 45),
    ('SWR302', 'Software Requirements',  'A model-based introduction to RE, providing the conceptual background...', 3, 45);
GO