/* ============================================================
   V4 - Archivado lógico y último acceso
   ------------------------------------------------------------
   Distingue Inactivo (temporal, sigue en el listado operativo)
   de Archivado (baja lógica de "Eliminar": no aparece en Todos
   ni Activos; se consulta al final de Inactivos).

   No hay DELETE físico. V1, V2 y V3 no se modifican.

   fecha_ultimo_acceso: último inicio de sesión exitoso.
   NULL si la cuenta nunca autenticó después de esta migración.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    ALTER TABLE dbo.empleados
        ADD archivado BIT NOT NULL
            CONSTRAINT DF_empleados_archivado DEFAULT (0);

    ALTER TABLE dbo.usuarios
        ADD archivado BIT NOT NULL
            CONSTRAINT DF_usuarios_archivado DEFAULT (0);

    ALTER TABLE dbo.usuarios
        ADD fecha_ultimo_acceso DATETIME2(0) NULL;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
