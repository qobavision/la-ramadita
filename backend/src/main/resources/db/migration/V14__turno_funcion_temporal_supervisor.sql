/* ============================================================
   V14 - Supervisor como funcion temporal del turno
   ------------------------------------------------------------
   El administrador puede asignar SUPERVISOR solo para ese
   turno. El supervisor no otorga esa funcion.

   V1 a V13 no se modifican.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = N'CK_turno_asistencia_funcion'
          AND parent_object_id = OBJECT_ID(N'dbo.turno_asistencia')
    )
    BEGIN
        ALTER TABLE dbo.turno_asistencia DROP CONSTRAINT CK_turno_asistencia_funcion;
    END;

    ALTER TABLE dbo.turno_asistencia
        ADD CONSTRAINT CK_turno_asistencia_funcion
            CHECK (funcion_temporal IS NULL
                OR funcion_temporal IN (N'MESERO', N'COCINA', N'CAJA', N'SUPERVISOR'));

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
