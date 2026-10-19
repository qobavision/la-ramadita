/* ============================================================
   V11 - Archivado de proveedores
   ------------------------------------------------------------
   Eliminar un proveedor es baja lógica: sale del listado
   operativo y conserva compras, recepciones y el documento.
   Activo/inactivo sigue siendo el estado temporal, desde Editar.

   V1 a V10 no se modifican. No hay DELETE físico.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.proveedores', 'archivado') IS NULL
    BEGIN
        ALTER TABLE dbo.proveedores
            ADD archivado BIT NOT NULL
                CONSTRAINT DF_proveedores_archivado DEFAULT (0);
    END;

    IF COL_LENGTH('dbo.proveedores', 'fecha_archivado') IS NULL
    BEGIN
        ALTER TABLE dbo.proveedores
            ADD fecha_archivado DATETIME2(0) NULL;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
