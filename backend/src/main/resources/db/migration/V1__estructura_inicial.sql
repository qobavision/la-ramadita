/* ============================================================
   SISTEMA INTEGRAL DE GESTIÓN - LA RAMADITA
   Base de Datos V1
   Motor: Microsoft SQL Server / Azure SQL Database
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* ========================================================
       1. ROLES
       ======================================================== */
    CREATE TABLE dbo.roles (
        rol_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(50) NOT NULL,
        descripcion NVARCHAR(200) NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_roles_activo DEFAULT (1),

        CONSTRAINT UQ_roles_nombre UNIQUE (nombre)
    );


    /* ========================================================
       2. USUARIOS
       ======================================================== */
    CREATE TABLE dbo.usuarios (
        usuario_id INT IDENTITY(1,1) PRIMARY KEY,
        rol_id INT NOT NULL,
        nombre NVARCHAR(80) NOT NULL,
        apellido NVARCHAR(80) NULL,
        username NVARCHAR(60) NOT NULL,
        password_hash NVARCHAR(255) NOT NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_usuarios_activo DEFAULT (1),
        fecha_creacion DATETIME2(0) NOT NULL
            CONSTRAINT DF_usuarios_fecha DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT UQ_usuarios_username UNIQUE (username),

        CONSTRAINT FK_usuarios_roles
            FOREIGN KEY (rol_id)
            REFERENCES dbo.roles(rol_id)
    );


    /* ========================================================
       3. UNIDADES DE NEGOCIO
       Restaurante / Pollería
       ======================================================== */
    CREATE TABLE dbo.unidades_negocio (
        unidad_negocio_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(50) NOT NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_unidades_activo DEFAULT (1),

        CONSTRAINT UQ_unidades_nombre UNIQUE (nombre)
    );


    /* ========================================================
       4. TURNOS
       ======================================================== */
    CREATE TABLE dbo.turnos (
        turno_id INT IDENTITY(1,1) PRIMARY KEY,
        unidad_negocio_id INT NOT NULL,
        usuario_apertura_id INT NOT NULL,
        usuario_cierre_id INT NULL,

        fecha_apertura DATETIME2(0) NOT NULL
            CONSTRAINT DF_turnos_apertura DEFAULT (SYSUTCDATETIME()),

        fecha_cierre DATETIME2(0) NULL,

        estado NVARCHAR(20) NOT NULL
            CONSTRAINT DF_turnos_estado DEFAULT (N'ABIERTO'),

        CONSTRAINT CK_turnos_estado
            CHECK (estado IN (N'ABIERTO', N'CERRADO')),

        CONSTRAINT CK_turnos_fechas
            CHECK (
                fecha_cierre IS NULL
                OR fecha_cierre >= fecha_apertura
            ),

        CONSTRAINT FK_turnos_unidad
            FOREIGN KEY (unidad_negocio_id)
            REFERENCES dbo.unidades_negocio(unidad_negocio_id),

        CONSTRAINT FK_turnos_usuario_apertura
            FOREIGN KEY (usuario_apertura_id)
            REFERENCES dbo.usuarios(usuario_id),

        CONSTRAINT FK_turnos_usuario_cierre
            FOREIGN KEY (usuario_cierre_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       5. PROVEEDORES
       ======================================================== */
    CREATE TABLE dbo.proveedores (
        proveedor_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre_razon_social NVARCHAR(150) NOT NULL,
        documento NVARCHAR(20) NULL,
        telefono NVARCHAR(20) NULL,
        direccion NVARCHAR(250) NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_proveedores_activo DEFAULT (1),

        fecha_creacion DATETIME2(0) NOT NULL
            CONSTRAINT DF_proveedores_fecha DEFAULT (SYSUTCDATETIME())
    );


    /* ========================================================
       6. INSUMOS
       ======================================================== */
    CREATE TABLE dbo.insumos (
        insumo_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(100) NOT NULL,
        unidad_medida NVARCHAR(30) NOT NULL,

        stock_actual DECIMAL(18,3) NOT NULL
            CONSTRAINT DF_insumos_stock DEFAULT (0),

        stock_minimo DECIMAL(18,3) NOT NULL
            CONSTRAINT DF_insumos_minimo DEFAULT (0),

        costo_referencial DECIMAL(18,4) NULL,

        activo BIT NOT NULL
            CONSTRAINT DF_insumos_activo DEFAULT (1),

        CONSTRAINT UQ_insumos_nombre UNIQUE (nombre),

        CONSTRAINT CK_insumos_stock
            CHECK (stock_actual >= 0),

        CONSTRAINT CK_insumos_stock_minimo
            CHECK (stock_minimo >= 0),

        CONSTRAINT CK_insumos_costo
            CHECK (
                costo_referencial IS NULL
                OR costo_referencial >= 0
            )
    );


    /* ========================================================
       7. COMPRAS
       ======================================================== */
    CREATE TABLE dbo.compras (
        compra_id INT IDENTITY(1,1) PRIMARY KEY,
        proveedor_id INT NOT NULL,
        usuario_id INT NOT NULL,

        fecha_compra DATETIME2(0) NOT NULL
            CONSTRAINT DF_compras_fecha DEFAULT (SYSUTCDATETIME()),

        fecha_recepcion DATETIME2(0) NULL,

        estado NVARCHAR(30) NOT NULL
            CONSTRAINT DF_compras_estado
            DEFAULT (N'PENDIENTE_RECEPCION'),

        observacion NVARCHAR(500) NULL,

        CONSTRAINT CK_compras_estado
            CHECK (
                estado IN (
                    N'PENDIENTE_RECEPCION',
                    N'RECIBIDA'
                )
            ),

        CONSTRAINT FK_compras_proveedor
            FOREIGN KEY (proveedor_id)
            REFERENCES dbo.proveedores(proveedor_id),

        CONSTRAINT FK_compras_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       8. DETALLE DE COMPRA
       ======================================================== */
    CREATE TABLE dbo.compra_detalle (
        compra_detalle_id INT IDENTITY(1,1) PRIMARY KEY,
        compra_id INT NOT NULL,
        insumo_id INT NOT NULL,

        cantidad_comprada DECIMAL(18,3) NOT NULL,
        cantidad_recibida DECIMAL(18,3) NOT NULL
            CONSTRAINT DF_compra_detalle_recibida DEFAULT (0),

        costo_unitario DECIMAL(18,4) NOT NULL,

        subtotal AS (
            cantidad_comprada * costo_unitario
        ) PERSISTED,

        CONSTRAINT CK_compra_detalle_cantidad
            CHECK (cantidad_comprada > 0),

        CONSTRAINT CK_compra_detalle_recibida
            CHECK (cantidad_recibida >= 0),

        CONSTRAINT CK_compra_detalle_costo
            CHECK (costo_unitario >= 0),

        CONSTRAINT UQ_compra_detalle
            UNIQUE (compra_id, insumo_id),

        CONSTRAINT FK_compra_detalle_compra
            FOREIGN KEY (compra_id)
            REFERENCES dbo.compras(compra_id),

        CONSTRAINT FK_compra_detalle_insumo
            FOREIGN KEY (insumo_id)
            REFERENCES dbo.insumos(insumo_id)
    );


    /* ========================================================
       9. PRODUCTOS
       Platos, bebidas, etc.
       ======================================================== */
    CREATE TABLE dbo.productos (
        producto_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(120) NOT NULL,
        categoria NVARCHAR(60) NOT NULL,

        precio DECIMAL(12,2) NOT NULL,

        ambito NVARCHAR(20) NOT NULL,

        activo BIT NOT NULL
            CONSTRAINT DF_productos_activo DEFAULT (1),

        CONSTRAINT CK_productos_precio
            CHECK (precio >= 0),

        CONSTRAINT CK_productos_ambito
            CHECK (
                ambito IN (
                    N'RESTAURANTE',
                    N'POLLERIA',
                    N'AMBOS'
                )
            ),

        CONSTRAINT UQ_productos_nombre UNIQUE (nombre)
    );


    /* ========================================================
       10. RECETAS
       Una receta vigente por producto.
       ======================================================== */
    CREATE TABLE dbo.receta_detalle (
        receta_detalle_id INT IDENTITY(1,1) PRIMARY KEY,
        producto_id INT NOT NULL,
        insumo_id INT NOT NULL,

        cantidad_requerida DECIMAL(18,3) NOT NULL,

        CONSTRAINT CK_receta_cantidad
            CHECK (cantidad_requerida > 0),

        CONSTRAINT UQ_receta_producto_insumo
            UNIQUE (producto_id, insumo_id),

        CONSTRAINT FK_receta_producto
            FOREIGN KEY (producto_id)
            REFERENCES dbo.productos(producto_id),

        CONSTRAINT FK_receta_insumo
            FOREIGN KEY (insumo_id)
            REFERENCES dbo.insumos(insumo_id)
    );


    /* ========================================================
       11. CARTAS
       ======================================================== */
    CREATE TABLE dbo.cartas (
        carta_id INT IDENTITY(1,1) PRIMARY KEY,
        turno_id INT NOT NULL,

        fecha DATE NOT NULL
            CONSTRAINT DF_cartas_fecha
            DEFAULT (CAST(SYSUTCDATETIME() AS DATE)),

        estado NVARCHAR(20) NOT NULL
            CONSTRAINT DF_cartas_estado DEFAULT (N'ACTIVA'),

        CONSTRAINT CK_cartas_estado
            CHECK (
                estado IN (N'ACTIVA', N'CERRADA')
            ),

        CONSTRAINT UQ_cartas_turno UNIQUE (turno_id),

        CONSTRAINT FK_cartas_turno
            FOREIGN KEY (turno_id)
            REFERENCES dbo.turnos(turno_id)
    );


    /* ========================================================
       12. DETALLE DE CARTA
       ======================================================== */
    CREATE TABLE dbo.carta_detalle (
        carta_detalle_id INT IDENTITY(1,1) PRIMARY KEY,
        carta_id INT NOT NULL,
        producto_id INT NOT NULL,

        estado_producto NVARCHAR(20) NOT NULL
            CONSTRAINT DF_carta_detalle_estado
            DEFAULT (N'DISPONIBLE'),

        CONSTRAINT CK_carta_detalle_estado
            CHECK (
                estado_producto IN (
                    N'DISPONIBLE',
                    N'AGOTADO'
                )
            ),

        CONSTRAINT UQ_carta_producto
            UNIQUE (carta_id, producto_id),

        CONSTRAINT FK_carta_detalle_carta
            FOREIGN KEY (carta_id)
            REFERENCES dbo.cartas(carta_id),

        CONSTRAINT FK_carta_detalle_producto
            FOREIGN KEY (producto_id)
            REFERENCES dbo.productos(producto_id)
    );


    /* ========================================================
       13. ZONAS / PISOS
       ======================================================== */
    CREATE TABLE dbo.zonas (
        zona_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(60) NOT NULL,
        descripcion NVARCHAR(200) NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_zonas_activo DEFAULT (1),

        CONSTRAINT UQ_zonas_nombre UNIQUE (nombre)
    );


    /* ========================================================
       14. MESAS
       ======================================================== */
    CREATE TABLE dbo.mesas (
        mesa_id INT IDENTITY(1,1) PRIMARY KEY,
        zona_id INT NOT NULL,
        numero NVARCHAR(20) NOT NULL,

        estado NVARCHAR(30) NOT NULL
            CONSTRAINT DF_mesas_estado
            DEFAULT (N'DISPONIBLE'),

        activo BIT NOT NULL
            CONSTRAINT DF_mesas_activo DEFAULT (1),

        CONSTRAINT CK_mesas_estado
            CHECK (
                estado IN (
                    N'DISPONIBLE',
                    N'OCUPADA',
                    N'PENDIENTE_PAGO'
                )
            ),

        CONSTRAINT UQ_mesa_zona
            UNIQUE (zona_id, numero),

        CONSTRAINT FK_mesas_zona
            FOREIGN KEY (zona_id)
            REFERENCES dbo.zonas(zona_id)
    );


    /* ========================================================
       15. ATENCIONES
       Una atención representa toda la visita de una mesa.
       ======================================================== */
    CREATE TABLE dbo.atenciones (
        atencion_id INT IDENTITY(1,1) PRIMARY KEY,

        mesa_id INT NULL,
        turno_id INT NOT NULL,
        mesero_id INT NOT NULL,

        tipo_atencion NVARCHAR(20) NOT NULL,

        fecha_inicio DATETIME2(0) NOT NULL
            CONSTRAINT DF_atenciones_inicio
            DEFAULT (SYSUTCDATETIME()),

        fecha_fin DATETIME2(0) NULL,

        estado NVARCHAR(30) NOT NULL
            CONSTRAINT DF_atenciones_estado DEFAULT (N'ABIERTA'),

        CONSTRAINT CK_atenciones_tipo
            CHECK (
                tipo_atencion IN (
                    N'MESA',
                    N'PARA_LLEVAR'
                )
            ),

        CONSTRAINT CK_atenciones_mesa
            CHECK (
                (tipo_atencion = N'MESA' AND mesa_id IS NOT NULL)
                OR
                (tipo_atencion = N'PARA_LLEVAR' AND mesa_id IS NULL)
            ),

        CONSTRAINT CK_atenciones_estado
            CHECK (
                estado IN (
                    N'ABIERTA',
                    N'PENDIENTE_PAGO',
                    N'CERRADA'
                )
            ),

        CONSTRAINT CK_atenciones_fechas
            CHECK (
                fecha_fin IS NULL
                OR fecha_fin >= fecha_inicio
            ),

        CONSTRAINT FK_atenciones_mesa
            FOREIGN KEY (mesa_id)
            REFERENCES dbo.mesas(mesa_id),

        CONSTRAINT FK_atenciones_turno
            FOREIGN KEY (turno_id)
            REFERENCES dbo.turnos(turno_id),

        CONSTRAINT FK_atenciones_mesero
            FOREIGN KEY (mesero_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       16. PEDIDOS
       Una atención puede tener múltiples pedidos/envíos.
       ======================================================== */
    CREATE TABLE dbo.pedidos (
        pedido_id INT IDENTITY(1,1) PRIMARY KEY,

        atencion_id INT NOT NULL,
        usuario_id INT NOT NULL,

        fecha_creacion DATETIME2(0) NOT NULL
            CONSTRAINT DF_pedidos_creacion
            DEFAULT (SYSUTCDATETIME()),

        fecha_envio DATETIME2(0) NULL,

        estado NVARCHAR(30) NOT NULL
            CONSTRAINT DF_pedidos_estado DEFAULT (N'BORRADOR'),

        CONSTRAINT CK_pedidos_estado
            CHECK (
                estado IN (
                    N'BORRADOR',
                    N'ENVIADO_COCINA',
                    N'CERRADO',
                    N'CANCELADO'
                )
            ),

        CONSTRAINT FK_pedidos_atencion
            FOREIGN KEY (atencion_id)
            REFERENCES dbo.atenciones(atencion_id),

        CONSTRAINT FK_pedidos_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       17. DETALLE DE PEDIDO
       Aquí se guarda el precio histórico.
       La observación va como una columna.
       ======================================================== */
    CREATE TABLE dbo.pedido_detalle (
        pedido_detalle_id INT IDENTITY(1,1) PRIMARY KEY,

        pedido_id INT NOT NULL,
        producto_id INT NOT NULL,

        cantidad INT NOT NULL,
        precio_unitario DECIMAL(12,2) NOT NULL,

        subtotal AS (
            cantidad * precio_unitario
        ) PERSISTED,

        observacion NVARCHAR(500) NULL,

        estado_cocina NVARCHAR(20) NOT NULL
            CONSTRAINT DF_pedido_detalle_cocina
            DEFAULT (N'PENDIENTE'),

        estado_item NVARCHAR(20) NOT NULL
            CONSTRAINT DF_pedido_detalle_item
            DEFAULT (N'ACTIVO'),

        preparado_por_usuario_id INT NULL,
        fecha_preparado DATETIME2(0) NULL,

        CONSTRAINT CK_pedido_detalle_cantidad
            CHECK (cantidad > 0),

        CONSTRAINT CK_pedido_detalle_precio
            CHECK (precio_unitario >= 0),

        CONSTRAINT CK_pedido_detalle_cocina
            CHECK (
                estado_cocina IN (
                    N'PENDIENTE',
                    N'PREPARADO'
                )
            ),

        CONSTRAINT CK_pedido_detalle_item
            CHECK (
                estado_item IN (
                    N'ACTIVO',
                    N'CANCELADO'
                )
            ),

        CONSTRAINT FK_pedido_detalle_pedido
            FOREIGN KEY (pedido_id)
            REFERENCES dbo.pedidos(pedido_id),

        CONSTRAINT FK_pedido_detalle_producto
            FOREIGN KEY (producto_id)
            REFERENCES dbo.productos(producto_id),

        CONSTRAINT FK_pedido_detalle_preparado_usuario
            FOREIGN KEY (preparado_por_usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       18. HISTORIAL DE CAMBIOS DE PEDIDOS
       ======================================================== */
    CREATE TABLE dbo.pedido_historial (
        historial_id INT IDENTITY(1,1) PRIMARY KEY,

        pedido_detalle_id INT NOT NULL,
        usuario_id INT NOT NULL,

        tipo_evento NVARCHAR(30) NOT NULL,

        cantidad_anterior INT NULL,
        cantidad_nueva INT NULL,

        estado_anterior NVARCHAR(30) NULL,
        estado_nuevo NVARCHAR(30) NULL,

        detalle_cambio NVARCHAR(500) NULL,
        motivo NVARCHAR(500) NULL,

        fecha_evento DATETIME2(0) NOT NULL
            CONSTRAINT DF_historial_fecha
            DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT CK_historial_tipo
            CHECK (
                tipo_evento IN (
                    N'MODIFICACION',
                    N'CANCELACION'
                )
            ),

        CONSTRAINT FK_historial_detalle
            FOREIGN KEY (pedido_detalle_id)
            REFERENCES dbo.pedido_detalle(pedido_detalle_id),

        CONSTRAINT FK_historial_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       19. MÉTODOS DE PAGO
       ======================================================== */
    CREATE TABLE dbo.metodos_pago (
        metodo_pago_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(50) NOT NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_metodos_activo DEFAULT (1),

        CONSTRAINT UQ_metodos_nombre UNIQUE (nombre)
    );


    /* ========================================================
       20. SESIONES DE CAJA
       ======================================================== */
    CREATE TABLE dbo.caja_sesiones (
        caja_sesion_id INT IDENTITY(1,1) PRIMARY KEY,

        turno_id INT NOT NULL,

        usuario_apertura_id INT NOT NULL,
        usuario_cierre_id INT NULL,

        fecha_apertura DATETIME2(0) NOT NULL
            CONSTRAINT DF_caja_apertura
            DEFAULT (SYSUTCDATETIME()),

        fecha_cierre DATETIME2(0) NULL,

        monto_inicial DECIMAL(12,2) NOT NULL
            CONSTRAINT DF_caja_inicial DEFAULT (0),

        efectivo_esperado DECIMAL(12,2) NULL,
        efectivo_real DECIMAL(12,2) NULL,

        diferencia AS (
            CASE
                WHEN efectivo_real IS NULL
                  OR efectivo_esperado IS NULL
                THEN NULL
                ELSE efectivo_real - efectivo_esperado
            END
        ),

        observacion_cierre NVARCHAR(500) NULL,

        estado NVARCHAR(20) NOT NULL
            CONSTRAINT DF_caja_estado DEFAULT (N'ABIERTA'),

        CONSTRAINT CK_caja_estado
            CHECK (
                estado IN (
                    N'ABIERTA',
                    N'CERRADA'
                )
            ),

        CONSTRAINT CK_caja_monto
            CHECK (monto_inicial >= 0),

        CONSTRAINT CK_caja_fechas
            CHECK (
                fecha_cierre IS NULL
                OR fecha_cierre >= fecha_apertura
            ),

        CONSTRAINT FK_caja_turno
            FOREIGN KEY (turno_id)
            REFERENCES dbo.turnos(turno_id),

        CONSTRAINT FK_caja_usuario_apertura
            FOREIGN KEY (usuario_apertura_id)
            REFERENCES dbo.usuarios(usuario_id),

        CONSTRAINT FK_caja_usuario_cierre
            FOREIGN KEY (usuario_cierre_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       21. MOVIMIENTOS DE CAJA
       Ingresos / egresos que no son pagos de ventas.
       ======================================================== */
    CREATE TABLE dbo.caja_movimientos (
        movimiento_caja_id INT IDENTITY(1,1) PRIMARY KEY,

        caja_sesion_id INT NOT NULL,
        usuario_id INT NOT NULL,

        tipo NVARCHAR(20) NOT NULL,
        monto DECIMAL(12,2) NOT NULL,
        motivo NVARCHAR(300) NOT NULL,

        fecha_movimiento DATETIME2(0) NOT NULL
            CONSTRAINT DF_movimiento_caja_fecha
            DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT CK_movimiento_caja_tipo
            CHECK (
                tipo IN (
                    N'INGRESO',
                    N'EGRESO'
                )
            ),

        CONSTRAINT CK_movimiento_caja_monto
            CHECK (monto > 0),

        CONSTRAINT FK_movimiento_caja_sesion
            FOREIGN KEY (caja_sesion_id)
            REFERENCES dbo.caja_sesiones(caja_sesion_id),

        CONSTRAINT FK_movimiento_caja_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       22. PAGOS
       Una atención puede tener múltiples pagos.
       ======================================================== */
    CREATE TABLE dbo.pagos (
        pago_id INT IDENTITY(1,1) PRIMARY KEY,

        atencion_id INT NOT NULL,
        metodo_pago_id INT NOT NULL,
        caja_sesion_id INT NOT NULL,
        usuario_id INT NOT NULL,

        monto DECIMAL(12,2) NOT NULL,

        fecha_pago DATETIME2(0) NOT NULL
            CONSTRAINT DF_pagos_fecha
            DEFAULT (SYSUTCDATETIME()),

        estado NVARCHAR(20) NOT NULL
            CONSTRAINT DF_pagos_estado DEFAULT (N'CONFIRMADO'),

        observacion NVARCHAR(300) NULL,

        CONSTRAINT CK_pagos_monto
            CHECK (monto > 0),

        CONSTRAINT CK_pagos_estado
            CHECK (
                estado IN (
                    N'CONFIRMADO',
                    N'ANULADO'
                )
            ),

        CONSTRAINT FK_pagos_atencion
            FOREIGN KEY (atencion_id)
            REFERENCES dbo.atenciones(atencion_id),

        CONSTRAINT FK_pagos_metodo
            FOREIGN KEY (metodo_pago_id)
            REFERENCES dbo.metodos_pago(metodo_pago_id),

        CONSTRAINT FK_pagos_caja
            FOREIGN KEY (caja_sesion_id)
            REFERENCES dbo.caja_sesiones(caja_sesion_id),

        CONSTRAINT FK_pagos_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       23. DETALLE DE PAGO
       Se usa cuando el cliente paga productos específicos.
       Si paga solo "S/ 30", no es obligatorio registrar detalle.
       ======================================================== */
    CREATE TABLE dbo.pago_detalle (
        pago_detalle_id INT IDENTITY(1,1) PRIMARY KEY,

        pago_id INT NOT NULL,
        pedido_detalle_id INT NOT NULL,

        cantidad_pagada INT NULL,
        monto_aplicado DECIMAL(12,2) NOT NULL,

        CONSTRAINT CK_pago_detalle_cantidad
            CHECK (
                cantidad_pagada IS NULL
                OR cantidad_pagada > 0
            ),

        CONSTRAINT CK_pago_detalle_monto
            CHECK (monto_aplicado > 0),

        CONSTRAINT UQ_pago_producto
            UNIQUE (
                pago_id,
                pedido_detalle_id
            ),

        CONSTRAINT FK_pago_detalle_pago
            FOREIGN KEY (pago_id)
            REFERENCES dbo.pagos(pago_id),

        CONSTRAINT FK_pago_detalle_pedido
            FOREIGN KEY (pedido_detalle_id)
            REFERENCES dbo.pedido_detalle(pedido_detalle_id)
    );


    /* ========================================================
       24. COMPROBANTES SIMULADOS
       No SUNAT.
       ======================================================== */
    CREATE TABLE dbo.comprobantes (
        comprobante_id INT IDENTITY(1,1) PRIMARY KEY,

        atencion_id INT NOT NULL,
        pago_id INT NULL,
        emitido_por_usuario_id INT NOT NULL,

        tipo NVARCHAR(20) NOT NULL,
        serie NVARCHAR(10) NOT NULL,
        numero INT NOT NULL,

        fecha_emision DATETIME2(0) NOT NULL
            CONSTRAINT DF_comprobante_fecha
            DEFAULT (SYSUTCDATETIME()),

        total DECIMAL(12,2) NOT NULL,

        tipo_documento_cliente NVARCHAR(20) NULL,
        numero_documento_cliente NVARCHAR(20) NULL,
        nombre_cliente NVARCHAR(150) NULL,
        direccion_cliente NVARCHAR(250) NULL,

        CONSTRAINT CK_comprobante_tipo
            CHECK (
                tipo IN (
                    N'TICKET',
                    N'BOLETA',
                    N'FACTURA'
                )
            ),

        CONSTRAINT CK_comprobante_numero
            CHECK (numero > 0),

        CONSTRAINT CK_comprobante_total
            CHECK (total >= 0),

        CONSTRAINT UQ_comprobante
            UNIQUE (
                tipo,
                serie,
                numero
            ),

        CONSTRAINT FK_comprobante_atencion
            FOREIGN KEY (atencion_id)
            REFERENCES dbo.atenciones(atencion_id),

        CONSTRAINT FK_comprobante_pago
            FOREIGN KEY (pago_id)
            REFERENCES dbo.pagos(pago_id),

        CONSTRAINT FK_comprobante_usuario
            FOREIGN KEY (emitido_por_usuario_id)
            REFERENCES dbo.usuarios(usuario_id)
    );


    /* ========================================================
       25. MOVIMIENTOS DE INVENTARIO
       ======================================================== */
    CREATE TABLE dbo.inventario_movimientos (
        movimiento_id INT IDENTITY(1,1) PRIMARY KEY,

        insumo_id INT NOT NULL,

        unidad_negocio_id INT NULL,
        turno_id INT NULL,
        usuario_id INT NOT NULL,

        compra_detalle_id INT NULL,
        pedido_detalle_id INT NULL,

        tipo_movimiento NVARCHAR(30) NOT NULL,

        cantidad DECIMAL(18,3) NOT NULL,

        stock_anterior DECIMAL(18,3) NOT NULL,
        stock_nuevo DECIMAL(18,3) NOT NULL,

        motivo NVARCHAR(300) NULL,

        fecha_movimiento DATETIME2(0) NOT NULL
            CONSTRAINT DF_inventario_movimiento_fecha
            DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT CK_inventario_tipo
            CHECK (
                tipo_movimiento IN (
                    N'ENTRADA_COMPRA',
                    N'SALIDA_RECETA',
                    N'AJUSTE_ENTRADA',
                    N'AJUSTE_SALIDA'
                )
            ),

        CONSTRAINT CK_inventario_cantidad
            CHECK (cantidad > 0),

        CONSTRAINT CK_inventario_stock_anterior
            CHECK (stock_anterior >= 0),

        CONSTRAINT CK_inventario_stock_nuevo
            CHECK (stock_nuevo >= 0),

        CONSTRAINT CK_inventario_referencia
            CHECK (
                (
                    tipo_movimiento = N'ENTRADA_COMPRA'
                    AND compra_detalle_id IS NOT NULL
                    AND pedido_detalle_id IS NULL
                )
                OR
                (
                    tipo_movimiento = N'SALIDA_RECETA'
                    AND pedido_detalle_id IS NOT NULL
                    AND compra_detalle_id IS NULL
                )
                OR
                tipo_movimiento IN (
                    N'AJUSTE_ENTRADA',
                    N'AJUSTE_SALIDA'
                )
            ),

        CONSTRAINT FK_inventario_insumo
            FOREIGN KEY (insumo_id)
            REFERENCES dbo.insumos(insumo_id),

        CONSTRAINT FK_inventario_unidad
            FOREIGN KEY (unidad_negocio_id)
            REFERENCES dbo.unidades_negocio(unidad_negocio_id),

        CONSTRAINT FK_inventario_turno
            FOREIGN KEY (turno_id)
            REFERENCES dbo.turnos(turno_id),

        CONSTRAINT FK_inventario_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios(usuario_id),

        CONSTRAINT FK_inventario_compra_detalle
            FOREIGN KEY (compra_detalle_id)
            REFERENCES dbo.compra_detalle(compra_detalle_id),

        CONSTRAINT FK_inventario_pedido_detalle
            FOREIGN KEY (pedido_detalle_id)
            REFERENCES dbo.pedido_detalle(pedido_detalle_id)
    );


    /* ========================================================
       ÍNDICES IMPORTANTES
       ======================================================== */

    CREATE UNIQUE INDEX UX_turno_abierto_unidad
        ON dbo.turnos(unidad_negocio_id)
        WHERE fecha_cierre IS NULL;

    CREATE UNIQUE INDEX UX_atencion_abierta_mesa
        ON dbo.atenciones(mesa_id)
        WHERE mesa_id IS NOT NULL
          AND fecha_fin IS NULL;

    CREATE UNIQUE INDEX UX_caja_abierta_turno
        ON dbo.caja_sesiones(turno_id)
        WHERE fecha_cierre IS NULL;

    CREATE INDEX IX_atenciones_turno
        ON dbo.atenciones(turno_id);

    CREATE INDEX IX_pedidos_atencion
        ON dbo.pedidos(atencion_id);

    CREATE INDEX IX_pedido_detalle_pedido
        ON dbo.pedido_detalle(pedido_id);

    CREATE INDEX IX_pagos_atencion
        ON dbo.pagos(atencion_id);

    CREATE INDEX IX_pagos_caja
        ON dbo.pagos(caja_sesion_id);

    CREATE INDEX IX_compras_estado
        ON dbo.compras(estado, fecha_compra);

    CREATE INDEX IX_inventario_insumo_fecha
        ON dbo.inventario_movimientos(
            insumo_id,
            fecha_movimiento
        );


    /* ========================================================
       DATOS INICIALES
       ======================================================== */

    INSERT INTO dbo.roles (nombre, descripcion)
    VALUES
        (N'DUEÑO', N'Acceso general y administrativo'),
        (N'SUPERVISOR', N'Control operativo, compras e inventario'),
        (N'MESERO', N'Atención de mesas y pedidos'),
        (N'COCINA', N'Gestión de pedidos enviados a cocina'),
        (N'CAJA', N'Cobros, pagos y cierre de caja');


    INSERT INTO dbo.unidades_negocio (nombre)
    VALUES
        (N'RESTAURANTE'),
        (N'POLLERIA');


    INSERT INTO dbo.metodos_pago (nombre)
    VALUES
        (N'EFECTIVO'),
        (N'YAPE'),
        (N'PLIN'),
        (N'TARJETA'),
        (N'TRANSFERENCIA');


    COMMIT TRANSACTION;

    PRINT N'Base de datos La Ramadita V1 creada correctamente.';

END TRY
BEGIN CATCH

    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;

END CATCH;
SELECT COUNT(*) AS TotalTablas
FROM sys.tables;

SELECT name
FROM sys.tables
ORDER BY name;

SELECT * FROM dbo.roles;

SELECT * FROM dbo.unidades_negocio;

SELECT * FROM dbo.metodos_pago;