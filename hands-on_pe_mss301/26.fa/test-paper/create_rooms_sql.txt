
/* Table: rooms
   Target: MS SQL Server 2016
*/
USE [MSS301_2026_PE]
GO

CREATE TABLE [dbo].[rooms] (
    [room_id]         INT              IDENTITY(1,1) NOT NULL, -- Auto increment
    [room_number]     NVARCHAR(20)     NOT NULL,               -- Unique
    [room_type]       NVARCHAR(10)     NOT NULL,
    [price_per_night] DECIMAL(18,2)    NOT NULL,
    [capacity]        INT              NOT NULL,
    [floor]           INT              NOT NULL,
    [status]          NVARCHAR(15)     NOT NULL,

    -- Primary Key
    CONSTRAINT [PK_Rooms] PRIMARY KEY CLUSTERED ([room_id] ASC),

    -- Unique room number
    CONSTRAINT [UQ_Rooms_Number] UNIQUE NONCLUSTERED ([room_number] ASC),

    -- Room type allowed values
    CONSTRAINT [CK_Rooms_Type]
        CHECK ([room_type] IN ('SINGLE', 'DOUBLE', 'SUITE', 'DELUXE')),

    -- Price must be greater than 0
    CONSTRAINT [CK_Rooms_Price]
        CHECK ([price_per_night] > 0),

    -- Capacity between 1 and 10
    CONSTRAINT [CK_Rooms_Capacity]
        CHECK ([capacity] >= 1 AND [capacity] <= 10),

    -- Floor must be >= 1
    CONSTRAINT [CK_Rooms_Floor]
        CHECK ([floor] >= 1),

    -- Status allowed values
    CONSTRAINT [CK_Rooms_Status]
        CHECK ([status] IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE'))
);
GO
