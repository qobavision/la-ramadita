/* ============================================================
   V5 - Fecha de archivado (baja lógica)
   ------------------------------------------------------------
   Conserva cuándo se pulsó Eliminar. NULL si el registro nunca
   se archivó, o si ya estaba archivado antes de esta migración.

   V1 a V4 no se modifican. No hay DELETE físico.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    ALTER TABLE dbo.empleados
        ADD fecha_archivado DATETIME2(0) NULL;

    ALTER TABLE dbo.usuarios
        ADD fecha_archivado DATETIME2(0) NULL;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
