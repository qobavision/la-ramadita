/* ============================================================
   V7 - Compras, recepción parcial y trazabilidad
   ------------------------------------------------------------
   Reutiliza proveedores, insumos, compras, compra_detalle e
   inventario_movimientos de V1. Agrega únicamente lo que V1
   no cubre: código COMP-xxx, estados PARCIAL/CANCELADA,
   documento/contacto de proveedor, historial de recepciones
   e índice único para el futuro consumo idempotente por receta.

   V1 a V6 no se modifican.
   Los GO separan lotes: SQL Server no puede referenciar en el
   mismo lote una columna que acaba de agregarse.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF COL_LENGTH('dbo.proveedores', 'tipo_documento') IS NULL
BEGIN
    ALTER TABLE dbo.proveedores
        ADD tipo_documento NVARCHAR(3) NULL;
END;
GO

IF COL_LENGTH('dbo.proveedores', 'persona_contacto') IS NULL
BEGIN
    ALTER TABLE dbo.proveedores
        ADD persona_contacto NVARCHAR(120) NULL;
END;
GO

UPDATE dbo.proveedores
SET tipo_documento = CASE
        WHEN LEN(LTRIM(RTRIM(documento))) = 11 THEN N'RUC'
        ELSE N'DNI'
    END
WHERE tipo_documento IS NULL
  AND documento IS NOT NULL
  AND LTRIM(RTRIM(documento)) <> N'';
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_proveedores_tipo_documento'
      AND parent_object_id = OBJECT_ID(N'dbo.proveedores')
)
BEGIN
    ALTER TABLE dbo.proveedores
        ADD CONSTRAINT CK_proveedores_tipo_documento
        CHECK (
            tipo_documento IS NULL
            OR tipo_documento IN (N'RUC', N'DNI')
        );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_proveedores_documento'
      AND object_id = OBJECT_ID(N'dbo.proveedores')
)
BEGIN
    CREATE UNIQUE INDEX UX_proveedores_documento
        ON dbo.proveedores(documento)
        WHERE documento IS NOT NULL;
END;
GO

IF COL_LENGTH('dbo.compras', 'codigo') IS NULL
BEGIN
    ALTER TABLE dbo.compras
        ADD codigo NVARCHAR(20) NULL;
END;
GO

UPDATE dbo.compras
SET codigo = CONCAT(N'COMP-', FORMAT(compra_id, '000'))
WHERE codigo IS NULL;
GO

ALTER TABLE dbo.compras
    ALTER COLUMN codigo NVARCHAR(20) NOT NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_compras_codigo'
      AND object_id = OBJECT_ID(N'dbo.compras')
)
BEGIN
    CREATE UNIQUE INDEX UX_compras_codigo
        ON dbo.compras(codigo);
END;
GO

IF OBJECT_ID(N'dbo.seq_compra_codigo', N'SO') IS NULL
BEGIN
    DECLARE @siguiente INT = ISNULL((SELECT MAX(compra_id) FROM dbo.compras), 0) + 1;
    DECLARE @sql NVARCHAR(300) = N'CREATE SEQUENCE dbo.seq_compra_codigo AS INT START WITH '
        + CAST(@siguiente AS NVARCHAR(20))
        + N' INCREMENT BY 1;';
    EXEC sys.sp_executesql @sql;
END;
GO

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_compras_estado'
      AND parent_object_id = OBJECT_ID(N'dbo.compras')
)
BEGIN
    ALTER TABLE dbo.compras DROP CONSTRAINT CK_compras_estado;
END;
GO

IF EXISTS (
    SELECT 1
    FROM sys.default_constraints
    WHERE name = N'DF_compras_estado'
      AND parent_object_id = OBJECT_ID(N'dbo.compras')
)
BEGIN
    ALTER TABLE dbo.compras DROP CONSTRAINT DF_compras_estado;
END;
GO

UPDATE dbo.compras
SET estado = N'PENDIENTE'
WHERE estado = N'PENDIENTE_RECEPCION';
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.default_constraints
    WHERE name = N'DF_compras_estado'
      AND parent_object_id = OBJECT_ID(N'dbo.compras')
)
BEGIN
    ALTER TABLE dbo.compras
        ADD CONSTRAINT DF_compras_estado
        DEFAULT (N'PENDIENTE') FOR estado;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_compras_estado'
      AND parent_object_id = OBJECT_ID(N'dbo.compras')
)
BEGIN
    ALTER TABLE dbo.compras
        ADD CONSTRAINT CK_compras_estado
        CHECK (
            estado IN (
                N'PENDIENTE',
                N'PARCIAL',
                N'RECIBIDA',
                N'CANCELADA'
            )
        );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_compra_detalle_recibida_max'
      AND parent_object_id = OBJECT_ID(N'dbo.compra_detalle')
)
BEGIN
    ALTER TABLE dbo.compra_detalle
        ADD CONSTRAINT CK_compra_detalle_recibida_max
        CHECK (cantidad_recibida <= cantidad_comprada);
END;
GO

IF OBJECT_ID(N'dbo.recepciones', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.recepciones (
        recepcion_id INT IDENTITY(1,1) PRIMARY KEY,
        compra_id INT NOT NULL,
        usuario_id INT NOT NULL,
        fecha_recepcion DATETIME2(0) NOT NULL
            CONSTRAINT DF_recepciones_fecha
            DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT FK_recepciones_compra
            FOREIGN KEY (compra_id)
            REFERENCES dbo.compras(compra_id),

        CONSTRAINT FK_recepciones_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );

    CREATE INDEX IX_recepciones_compra
        ON dbo.recepciones(compra_id, fecha_recepcion);
END;
GO

IF OBJECT_ID(N'dbo.recepcion_detalle', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.recepcion_detalle (
        recepcion_detalle_id INT IDENTITY(1,1) PRIMARY KEY,
        recepcion_id INT NOT NULL,
        compra_detalle_id INT NOT NULL,
        cantidad DECIMAL(18,3) NOT NULL,

        CONSTRAINT CK_recepcion_detalle_cantidad
            CHECK (cantidad > 0),

        CONSTRAINT UQ_recepcion_detalle
            UNIQUE (recepcion_id, compra_detalle_id),

        CONSTRAINT FK_recepcion_detalle_recepcion
            FOREIGN KEY (recepcion_id)
            REFERENCES dbo.recepciones(recepcion_id),

        CONSTRAINT FK_recepcion_detalle_compra_detalle
            FOREIGN KEY (compra_detalle_id)
            REFERENCES dbo.compra_detalle(compra_detalle_id)
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_inventario_salida_pedido'
      AND object_id = OBJECT_ID(N'dbo.inventario_movimientos')
)
BEGIN
    CREATE UNIQUE INDEX UX_inventario_salida_pedido
        ON dbo.inventario_movimientos(pedido_detalle_id)
        WHERE tipo_movimiento = N'SALIDA_RECETA'
          AND pedido_detalle_id IS NOT NULL;
END;
GO
