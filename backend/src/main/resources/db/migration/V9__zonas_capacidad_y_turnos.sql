/* ============================================================
   V9 - Zonas (cajas/cocinas) y operación de turnos
   ------------------------------------------------------------
   Reutiliza dbo.zonas, dbo.mesas, dbo.turnos y dbo.unidades_negocio.

   Agrega:
   - capacidad de cajas y cocinas en la zona
   - horario base por unidad
   - estado EN_CIERRE en turnos
   - un solo turno abierto por unidad

   V1 a V8 no se modifican. Sin datos de carta ni inventario.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF COL_LENGTH('dbo.zonas', 'cajas') IS NULL
BEGIN
    ALTER TABLE dbo.zonas
        ADD cajas INT NOT NULL
            CONSTRAINT DF_zonas_cajas DEFAULT (0);
END;
GO

IF COL_LENGTH('dbo.zonas', 'cocinas') IS NULL
BEGIN
    ALTER TABLE dbo.zonas
        ADD cocinas INT NOT NULL
            CONSTRAINT DF_zonas_cocinas DEFAULT (0);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_zonas_cajas' AND parent_object_id = OBJECT_ID(N'dbo.zonas')
)
BEGIN
    ALTER TABLE dbo.zonas
        ADD CONSTRAINT CK_zonas_cajas CHECK (cajas >= 0);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_zonas_cocinas' AND parent_object_id = OBJECT_ID(N'dbo.zonas')
)
BEGIN
    ALTER TABLE dbo.zonas
        ADD CONSTRAINT CK_zonas_cocinas CHECK (cocinas >= 0);
END;
GO

IF OBJECT_ID(N'dbo.horario_operacion', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.horario_operacion (
        horario_operacion_id INT IDENTITY(1,1) PRIMARY KEY,
        unidad_negocio_id INT NOT NULL,
        hora_inicio TIME(0) NOT NULL,
        hora_fin TIME(0) NOT NULL,

        CONSTRAINT UQ_horario_unidad UNIQUE (unidad_negocio_id),

        CONSTRAINT FK_horario_unidad
            FOREIGN KEY (unidad_negocio_id)
            REFERENCES dbo.unidades_negocio (unidad_negocio_id)
    );
END;
GO

INSERT INTO dbo.horario_operacion (unidad_negocio_id, hora_inicio, hora_fin)
SELECT
    u.unidad_negocio_id,
    CASE u.nombre WHEN N'RESTAURANTE' THEN CAST(N'07:00' AS TIME(0)) ELSE CAST(N'16:00' AS TIME(0)) END,
    CASE u.nombre WHEN N'RESTAURANTE' THEN CAST(N'16:00' AS TIME(0)) ELSE CAST(N'01:00' AS TIME(0)) END
FROM dbo.unidades_negocio u
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.horario_operacion h
    WHERE h.unidad_negocio_id = u.unidad_negocio_id
);
GO

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_turnos_estado' AND parent_object_id = OBJECT_ID(N'dbo.turnos')
)
BEGIN
    ALTER TABLE dbo.turnos DROP CONSTRAINT CK_turnos_estado;
END;
GO

ALTER TABLE dbo.turnos
    ADD CONSTRAINT CK_turnos_estado
        CHECK (estado IN (N'ABIERTO', N'EN_CIERRE', N'CERRADO'));
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_turnos_unidad_abierto' AND object_id = OBJECT_ID(N'dbo.turnos')
)
BEGIN
    CREATE UNIQUE INDEX UX_turnos_unidad_abierto
        ON dbo.turnos (unidad_negocio_id)
        WHERE estado IN (N'ABIERTO', N'EN_CIERRE');
END;
GO
