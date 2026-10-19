/* ============================================================
   V8 - Productos, cartas nombradas, secciones y combos
   ------------------------------------------------------------
   Reutiliza dbo.productos, dbo.receta_detalle, dbo.cartas,
   dbo.carta_detalle y dbo.pedido_detalle de V1.

   Agrega lo que V1 no cubre:
   - cartas con nombre, ámbito y vigencia por unidad
   - turno_id opcional (la carta ya no depende de un turno)
   - carta_detalle.seccion y precio_dia
   - unicidad (carta, producto, seccion)
   - producto_componente para combos
   - pedido_detalle.padre_id (preparación futura, sin flujo)

   V1 a V7 no se modifican. Sin datos semilla.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

/* ---------- cartas: nombre, ambito, vigente ---------- */

IF COL_LENGTH('dbo.cartas', 'nombre') IS NULL
BEGIN
    ALTER TABLE dbo.cartas
        ADD nombre NVARCHAR(120) NULL;
END;
GO

IF COL_LENGTH('dbo.cartas', 'ambito') IS NULL
BEGIN
    ALTER TABLE dbo.cartas
        ADD ambito NVARCHAR(20) NULL;
END;
GO

IF COL_LENGTH('dbo.cartas', 'vigente') IS NULL
BEGIN
    ALTER TABLE dbo.cartas
        ADD vigente BIT NOT NULL
            CONSTRAINT DF_cartas_vigente DEFAULT (0);
END;
GO

UPDATE c
SET
    c.nombre = COALESCE(c.nombre, N'Carta ' + CONVERT(NVARCHAR(20), c.carta_id)),
    c.ambito = COALESCE(c.ambito, u.nombre)
FROM dbo.cartas c
INNER JOIN dbo.turnos t ON t.turno_id = c.turno_id
INNER JOIN dbo.unidades_negocio u ON u.unidad_negocio_id = t.unidad_negocio_id
WHERE c.nombre IS NULL OR c.ambito IS NULL;
GO

UPDATE dbo.cartas
SET
    nombre = COALESCE(nombre, N'Carta ' + CONVERT(NVARCHAR(20), carta_id)),
    ambito = COALESCE(ambito, N'RESTAURANTE')
WHERE nombre IS NULL OR ambito IS NULL;
GO

ALTER TABLE dbo.cartas
    ALTER COLUMN nombre NVARCHAR(120) NOT NULL;
GO

ALTER TABLE dbo.cartas
    ALTER COLUMN ambito NVARCHAR(20) NOT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_cartas_ambito' AND parent_object_id = OBJECT_ID(N'dbo.cartas')
)
BEGIN
    ALTER TABLE dbo.cartas
        ADD CONSTRAINT CK_cartas_ambito
            CHECK (ambito IN (N'RESTAURANTE', N'POLLERIA'));
END;
GO

IF EXISTS (
    SELECT 1 FROM sys.key_constraints
    WHERE name = N'UQ_cartas_turno' AND parent_object_id = OBJECT_ID(N'dbo.cartas')
)
BEGIN
    ALTER TABLE dbo.cartas DROP CONSTRAINT UQ_cartas_turno;
END;
GO

ALTER TABLE dbo.cartas
    ALTER COLUMN turno_id INT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_cartas_turno' AND object_id = OBJECT_ID(N'dbo.cartas')
)
BEGIN
    CREATE UNIQUE INDEX UX_cartas_turno
        ON dbo.cartas (turno_id)
        WHERE turno_id IS NOT NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_cartas_ambito_nombre' AND object_id = OBJECT_ID(N'dbo.cartas')
)
BEGIN
    CREATE UNIQUE INDEX UX_cartas_ambito_nombre
        ON dbo.cartas (ambito, nombre);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = N'UX_cartas_ambito_vigente' AND object_id = OBJECT_ID(N'dbo.cartas')
)
BEGIN
    CREATE UNIQUE INDEX UX_cartas_ambito_vigente
        ON dbo.cartas (ambito)
        WHERE vigente = 1;
END;
GO

/* ---------- carta_detalle: seccion y precio del día ---------- */

IF COL_LENGTH('dbo.carta_detalle', 'seccion') IS NULL
BEGIN
    ALTER TABLE dbo.carta_detalle
        ADD seccion NVARCHAR(40) NULL;
END;
GO

UPDATE dbo.carta_detalle
SET seccion = N'OTRO'
WHERE seccion IS NULL;
GO

ALTER TABLE dbo.carta_detalle
    ALTER COLUMN seccion NVARCHAR(40) NOT NULL;
GO

IF COL_LENGTH('dbo.carta_detalle', 'precio_dia') IS NULL
BEGIN
    ALTER TABLE dbo.carta_detalle
        ADD precio_dia DECIMAL(12,2) NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_carta_detalle_seccion' AND parent_object_id = OBJECT_ID(N'dbo.carta_detalle')
)
BEGIN
    ALTER TABLE dbo.carta_detalle
        ADD CONSTRAINT CK_carta_detalle_seccion
            CHECK (seccion IN (
                N'MENU_CRIOLLO',
                N'MENU_CRIOLLO_ENTRADA',
                N'MENU_CRIOLLO_SEGUNDO',
                N'MENU_EJECUTIVO',
                N'A_LA_CARTA',
                N'BEBIDA',
                N'POSTRE',
                N'OTRO',
                N'BRASA',
                N'CHIFA',
                N'EXTRA',
                N'COMBO'
            ));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_carta_detalle_precio_dia' AND parent_object_id = OBJECT_ID(N'dbo.carta_detalle')
)
BEGIN
    ALTER TABLE dbo.carta_detalle
        ADD CONSTRAINT CK_carta_detalle_precio_dia
            CHECK (precio_dia IS NULL OR precio_dia >= 0);
END;
GO

IF EXISTS (
    SELECT 1 FROM sys.key_constraints
    WHERE name = N'UQ_carta_producto' AND parent_object_id = OBJECT_ID(N'dbo.carta_detalle')
)
BEGIN
    ALTER TABLE dbo.carta_detalle DROP CONSTRAINT UQ_carta_producto;
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.key_constraints
    WHERE name = N'UQ_carta_producto_seccion' AND parent_object_id = OBJECT_ID(N'dbo.carta_detalle')
)
BEGIN
    ALTER TABLE dbo.carta_detalle
        ADD CONSTRAINT UQ_carta_producto_seccion UNIQUE (carta_id, producto_id, seccion);
END;
GO

/* ---------- combos: un producto compuesto por otros productos ---------- */

IF OBJECT_ID(N'dbo.producto_componente', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.producto_componente (
        producto_componente_id INT IDENTITY(1,1) PRIMARY KEY,
        producto_id INT NOT NULL,
        componente_producto_id INT NOT NULL,
        cantidad DECIMAL(18,3) NOT NULL,

        CONSTRAINT CK_producto_componente_cantidad
            CHECK (cantidad > 0),

        CONSTRAINT CK_producto_componente_distinto
            CHECK (producto_id <> componente_producto_id),

        CONSTRAINT UQ_producto_componente
            UNIQUE (producto_id, componente_producto_id),

        CONSTRAINT FK_producto_componente_padre
            FOREIGN KEY (producto_id)
            REFERENCES dbo.productos (producto_id),

        CONSTRAINT FK_producto_componente_hijo
            FOREIGN KEY (componente_producto_id)
            REFERENCES dbo.productos (producto_id)
    );
END;
GO

/* ---------- pedidos futuros: línea padre (menú criollo) ---------- */

IF COL_LENGTH('dbo.pedido_detalle', 'padre_id') IS NULL
BEGIN
    ALTER TABLE dbo.pedido_detalle
        ADD padre_id INT NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_pedido_detalle_padre' AND parent_object_id = OBJECT_ID(N'dbo.pedido_detalle')
)
BEGIN
    ALTER TABLE dbo.pedido_detalle
        ADD CONSTRAINT FK_pedido_detalle_padre
            FOREIGN KEY (padre_id)
            REFERENCES dbo.pedido_detalle (pedido_detalle_id);
END;
GO
