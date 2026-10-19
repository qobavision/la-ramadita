/* ============================================================
   V6 - Cargos Ayudante de cajero y Motorizado
   ------------------------------------------------------------
   Amplía el catálogo laboral. No agrega Área ni cargo
   Administrador (Administrador es un rol, no un puesto).

   V1 a V5 no se modifican.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    INSERT INTO dbo.cargos (nombre)
    SELECT v.nombre
    FROM (VALUES
            (N'Ayudante de cajero'),
            (N'Motorizado')
        ) AS v (nombre)
    WHERE NOT EXISTS (
        SELECT 1
        FROM dbo.cargos c
        WHERE c.nombre = v.nombre
    );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
