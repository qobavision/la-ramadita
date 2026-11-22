/* ============================================================
   V15 - Boleta simple, anulación de pagos, nombre histórico,
         consumo por receta y cierre diferido del turno
   ------------------------------------------------------------
   V1 a V14 no se modifican. Solo se agregan columnas, reglas e
   índices sobre tablas existentes.

   1. pedido_detalle.nombre_producto: nombre del producto al
      momento de pedirlo (las ventas pasadas no cambian si el
      producto se renombra).
   2. pagos: evidencia de la anulación (quién, cuándo y por qué).
      Solo el ADMINISTRADOR anula.
   3. comprobantes: estado EMITIDO/ANULADO. Una sola boleta
      emitida por atención.
   4. inventario_movimientos: una salida por receta por insumo de
      cada línea de pedido (una receta tiene varios insumos).
   5. turnos: finalizar con mesas por cobrar o caja abierta deja
      el turno en cierre; se cierra solo al terminar lo pendiente.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

/* 1. Nombre histórico del producto */
IF COL_LENGTH('dbo.pedido_detalle', 'nombre_producto') IS NULL
BEGIN
    ALTER TABLE dbo.pedido_detalle
        ADD nombre_producto NVARCHAR(150) NULL;
END;
GO

UPDATE pd
SET pd.nombre_producto = p.nombre
FROM dbo.pedido_detalle pd
JOIN dbo.productos p ON p.producto_id = pd.producto_id
WHERE pd.nombre_producto IS NULL;
GO

/* 2. Anulación de pagos con evidencia */
IF COL_LENGTH('dbo.pagos', 'anulado_por_usuario_id') IS NULL
BEGIN
    ALTER TABLE dbo.pagos
        ADD anulado_por_usuario_id INT NULL,
            fecha_anulacion DATETIME2(0) NULL,
            motivo_anulacion NVARCHAR(300) NULL;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_pagos_anulado_por')
BEGIN
    ALTER TABLE dbo.pagos
        ADD CONSTRAINT FK_pagos_anulado_por
            FOREIGN KEY (anulado_por_usuario_id)
            REFERENCES dbo.usuarios(usuario_id);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_pagos_anulacion')
BEGIN
    ALTER TABLE dbo.pagos
        ADD CONSTRAINT CK_pagos_anulacion
            CHECK (
                (
                    estado = N'CONFIRMADO'
                    AND anulado_por_usuario_id IS NULL
                    AND fecha_anulacion IS NULL
                )
                OR
                (
                    estado = N'ANULADO'
                    AND anulado_por_usuario_id IS NOT NULL
                    AND fecha_anulacion IS NOT NULL
                    AND motivo_anulacion IS NOT NULL
                )
            );
END;
GO

/* 3. Estado del comprobante */
IF COL_LENGTH('dbo.comprobantes', 'estado') IS NULL
BEGIN
    ALTER TABLE dbo.comprobantes
        ADD estado NVARCHAR(20) NOT NULL
                CONSTRAINT DF_comprobante_estado DEFAULT (N'EMITIDO'),
            fecha_anulacion DATETIME2(0) NULL;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_comprobante_estado')
BEGIN
    ALTER TABLE dbo.comprobantes
        ADD CONSTRAINT CK_comprobante_estado
            CHECK (estado IN (N'EMITIDO', N'ANULADO'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_comprobante_emitido_atencion'
      AND object_id = OBJECT_ID(N'dbo.comprobantes')
)
BEGIN
    CREATE UNIQUE INDEX UX_comprobante_emitido_atencion
        ON dbo.comprobantes(atencion_id)
        WHERE estado = N'EMITIDO';
END;
GO

/* 4. Consumo por receta: una salida por insumo de cada línea */
IF EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_inventario_salida_pedido'
      AND object_id = OBJECT_ID(N'dbo.inventario_movimientos')
)
BEGIN
    DROP INDEX UX_inventario_salida_pedido ON dbo.inventario_movimientos;
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'UX_inventario_salida_pedido_insumo'
      AND object_id = OBJECT_ID(N'dbo.inventario_movimientos')
)
BEGIN
    CREATE UNIQUE INDEX UX_inventario_salida_pedido_insumo
        ON dbo.inventario_movimientos(pedido_detalle_id, insumo_id)
        WHERE tipo_movimiento = N'SALIDA_RECETA'
          AND pedido_detalle_id IS NOT NULL;
END;
GO

/* 5. Cierre diferido del turno */
IF COL_LENGTH('dbo.turnos', 'cierre_solicitado_por_usuario_id') IS NULL
BEGIN
    ALTER TABLE dbo.turnos
        ADD cierre_solicitado_por_usuario_id INT NULL,
            fecha_cierre_solicitado DATETIME2(0) NULL;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_turnos_cierre_solicitado_por')
BEGIN
    ALTER TABLE dbo.turnos
        ADD CONSTRAINT FK_turnos_cierre_solicitado_por
            FOREIGN KEY (cierre_solicitado_por_usuario_id)
            REFERENCES dbo.usuarios(usuario_id);
END;
GO
