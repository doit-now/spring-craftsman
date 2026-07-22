USE [HSF302_2026_PE]
GO

/****** Object:  Table [dbo].[tours]    Script Date: 3/15/2026 9:13:54 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[tours](
	[tour_id] [int] IDENTITY(1,1) NOT NULL,
	[tour_name] [nvarchar](200) NOT NULL,
	[destination] [nvarchar](200) NOT NULL,
	[capacity] [int] NOT NULL,
	[duration] [int] NOT NULL,
	[start_date] [date] NOT NULL,
	[price] [float] NOT NULL,
	[status] [nvarchar](10) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[tour_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[tour_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

INSERT INTO [dbo].[tours] 
(tour_name, destination, capacity, duration, start_date, price, status)
VALUES
(N'HaLongBay3D2N', N'Ha Long', 30, 3, '2026-06-10', 2500, N'AC'),
(N'SapaAdventure5D4N', N'Sapa', 20, 5, '2026-07-15', 3200, N'AC'),
(N'DanangRelax4D3N', N'Da Nang', 25, 4, '2026-08-05', 2800, N'IN'),
(N'PhuQuocLuxury6D5N', N'Phu Quoc', 15, 6, '2026-09-01', 5500, N'AC'),
(N'NhaTrangFun3D2N', N'Nha Trang', 40, 3, '2026-06-20', 2100, N'DR'),
(N'DalatChill2D1N', N'Da Lat', 35, 2, '2026-05-25', 1500, N'AC');