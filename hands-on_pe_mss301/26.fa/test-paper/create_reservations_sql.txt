USE [MSS301_2026_PE]
GO

/****** Object:  Table [dbo].[reservations]    Script Date: 6/29/2026 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[reservations](
    [reservation_id]   [int]           IDENTITY(1,1) NOT NULL,
    [guest_name]       [nvarchar](100) NOT NULL,
    [guest_email]      [nvarchar](100) NOT NULL,
    [guest_phone]      [nvarchar](20)  NOT NULL,
    [check_in_date]    [date]          NOT NULL,
    [check_out_date]   [date]          NOT NULL,
    [number_of_guests] [int]           NOT NULL,
    [total_amount]     [decimal](18,2) NOT NULL,
    [status]           [nvarchar](15)  NOT NULL,
    [room_id]          [int]           NOT NULL,

 CONSTRAINT [PK_Reservations] PRIMARY KEY CLUSTERED
(
    [reservation_id] ASC
) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
        ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON,
        OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

-- check_out_date must be after check_in_date
ALTER TABLE [dbo].[reservations]
    WITH CHECK ADD CONSTRAINT [CK_Reservations_Dates]
    CHECK ([check_out_date] > [check_in_date]);
GO

ALTER TABLE [dbo].[reservations] CHECK CONSTRAINT [CK_Reservations_Dates];
GO

-- number_of_guests must be >= 1
ALTER TABLE [dbo].[reservations]
    WITH CHECK ADD CONSTRAINT [CK_Reservations_Guests]
    CHECK ([number_of_guests] >= 1);
GO

ALTER TABLE [dbo].[reservations] CHECK CONSTRAINT [CK_Reservations_Guests];
GO

-- total_amount must be >= 0
ALTER TABLE [dbo].[reservations]
    WITH CHECK ADD CONSTRAINT [CK_Reservations_Amount]
    CHECK ([total_amount] >= 0);
GO

ALTER TABLE [dbo].[reservations] CHECK CONSTRAINT [CK_Reservations_Amount];
GO

-- Status allowed values
ALTER TABLE [dbo].[reservations]
    WITH CHECK ADD CONSTRAINT [CK_Reservations_Status]
    CHECK ([status] IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'));
GO

ALTER TABLE [dbo].[reservations] CHECK CONSTRAINT [CK_Reservations_Status];
GO

-- Foreign key to rooms
ALTER TABLE [dbo].[reservations]
    WITH CHECK ADD CONSTRAINT [FK_Reservations_Room]
    FOREIGN KEY ([room_id]) REFERENCES [dbo].[rooms]([room_id]);
GO

ALTER TABLE [dbo].[reservations] CHECK CONSTRAINT [FK_Reservations_Room];
GO
