/* ============================================================
   V13 - Funcion temporal en el personal del turno
   ------------------------------------------------------------
   El rol permanente no cambia. El supervisor puede asignar
   MESERO, COCINA o CAJA solo para ese turno.

   V1 a V12 no se modifican.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.turno_asistencia', 'funcion_temporal') IS NULL
    BEGIN
        ALTER TABLE dbo.turno_asistencia
            ADD funcion_temporal NVARCHAR(20) NULL
                CONSTRAINT CK_turno_asistencia_funcion
                    CHECK (funcion_temporal IS NULL
                        OR funcion_temporal IN (N'MESERO', N'COCINA', N'CAJA'));
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
