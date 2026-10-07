/* ============================================================
   V3 - Empleados, cargos y vínculo con usuarios
   ------------------------------------------------------------
   EMPLEADO y USUARIO son entidades distintas. Un empleado puede
   no tener cuenta; un usuario pertenece a un solo empleado
   (relación 1 → 0..1).

   Los usuarios ya existentes se conservan: se crea un empleado
   por cada uno (cargo Otro, DNI nulo) y se vinculan. No se
   inventan DNI ni se modifican username, password_hash, rol ni
   estado. Las columnas nombre/apellido de usuarios se mantienen
   como copia sincronizada; la fuente funcional es EMPLEADO.

   DNI es NULL en registros heredados. Los empleados nuevos
   creados desde la aplicación deben llevar DNI de 8 dígitos
   (validado en backend). V1 y V2 no se modifican.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* ========================================================
       CARGOS LABORALES (no son roles del sistema)
       ======================================================== */
    CREATE TABLE dbo.cargos (
        cargo_id INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(50) NOT NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_cargos_activo DEFAULT (1),

        CONSTRAINT UQ_cargos_nombre UNIQUE (nombre)
    );

    INSERT INTO dbo.cargos (nombre)
    VALUES
        (N'Cocinero'),
        (N'Ayudante de cocina'),
        (N'Mesero'),
        (N'Cajero'),
        (N'Supervisor'),
        (N'Seguridad'),
        (N'Almacenero'),
        (N'Otro');

    /* ========================================================
       EMPLEADOS
       dni NULL: solo para trabajadores migrados desde usuarios
       ya existentes, que no tenían DNI.
       ======================================================== */
    CREATE TABLE dbo.empleados (
        empleado_id INT IDENTITY(1,1) PRIMARY KEY,
        cargo_id INT NOT NULL,
        nombres NVARCHAR(80) NOT NULL,
        apellidos NVARCHAR(80) NOT NULL
            CONSTRAINT DF_empleados_apellidos DEFAULT (N''),
        dni NVARCHAR(8) NULL,
        telefono NVARCHAR(20) NULL,
        correo_personal NVARCHAR(120) NULL,
        activo BIT NOT NULL
            CONSTRAINT DF_empleados_activo DEFAULT (1),
        fecha_creacion DATETIME2(0) NOT NULL
            CONSTRAINT DF_empleados_fecha DEFAULT (SYSUTCDATETIME()),
        usuario_origen_id INT NULL,

        CONSTRAINT FK_empleados_cargos
            FOREIGN KEY (cargo_id)
            REFERENCES dbo.cargos(cargo_id),

        CONSTRAINT CK_empleados_dni
            CHECK (
                dni IS NULL
                OR (LEN(dni) = 8 AND dni NOT LIKE N'%[^0-9]%')
            )
    );

    CREATE UNIQUE INDEX UQ_empleados_dni
        ON dbo.empleados (dni)
        WHERE dni IS NOT NULL;

    DECLARE @cargo_otro INT;
    SELECT @cargo_otro = cargo_id FROM dbo.cargos WHERE nombre = N'Otro';

    IF @cargo_otro IS NULL
    BEGIN
        THROW 50001, N'V3: no se encontró el cargo Otro.', 1;
    END;

    INSERT INTO dbo.empleados (
        cargo_id,
        nombres,
        apellidos,
        dni,
        telefono,
        correo_personal,
        activo,
        fecha_creacion,
        usuario_origen_id
    )
    SELECT
        @cargo_otro,
        u.nombre,
        ISNULL(u.apellido, N''),
        NULL,
        NULL,
        NULL,
        1,
        u.fecha_creacion,
        u.usuario_id
    FROM dbo.usuarios u;

    /* SQL Server valida el lote completo: no se puede usar una columna
       recién agregada en el mismo batch. EXEC fuerza compilación diferida. */
    ALTER TABLE dbo.usuarios
        ADD empleado_id INT NULL;

    EXEC(N'
        UPDATE u
        SET u.empleado_id = e.empleado_id
        FROM dbo.usuarios u
        INNER JOIN dbo.empleados e ON e.usuario_origen_id = u.usuario_id;
    ');

    EXEC(N'
        IF EXISTS (SELECT 1 FROM dbo.usuarios WHERE empleado_id IS NULL)
        BEGIN
            THROW 50002, N''V3: quedaron usuarios sin empleado asociado.'', 1;
        END;
    ');

    EXEC(N'ALTER TABLE dbo.usuarios ALTER COLUMN empleado_id INT NOT NULL;');

    EXEC(N'
        ALTER TABLE dbo.usuarios
            ADD CONSTRAINT FK_usuarios_empleados
                FOREIGN KEY (empleado_id)
                REFERENCES dbo.empleados(empleado_id);
    ');

    EXEC(N'
        ALTER TABLE dbo.usuarios
            ADD CONSTRAINT UQ_usuarios_empleado UNIQUE (empleado_id);
    ');

    ALTER TABLE dbo.empleados
        DROP COLUMN usuario_origen_id;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
