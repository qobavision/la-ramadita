/* ============================================================
   V12 - Apertura automatica de turno y asistencia
   ------------------------------------------------------------
   El horario base puede abrir el turno sin una persona
   (usuario_apertura_id queda NULL: "Apertura automatica").
   El personal del turno se registra al iniciar sesion con el
   turno ya abierto. Reingresar no duplica la fila.

   V1 a V11 no se modifican.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;

IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_turnos_usuario_apertura'
      AND parent_object_id = OBJECT_ID(N'dbo.turnos')
)
BEGIN
    ALTER TABLE dbo.turnos DROP CONSTRAINT FK_turnos_usuario_apertura;
END;
GO

ALTER TABLE dbo.turnos
    ALTER COLUMN usuario_apertura_id INT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_turnos_usuario_apertura'
      AND parent_object_id = OBJECT_ID(N'dbo.turnos')
)
BEGIN
    ALTER TABLE dbo.turnos
        ADD CONSTRAINT FK_turnos_usuario_apertura
            FOREIGN KEY (usuario_apertura_id)
            REFERENCES dbo.usuarios (usuario_id);
END;
GO

IF OBJECT_ID(N'dbo.turno_asistencia', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.turno_asistencia (
        turno_asistencia_id INT IDENTITY(1,1) PRIMARY KEY,
        turno_id INT NOT NULL,
        usuario_id INT NOT NULL,
        hora_ingreso DATETIME2(0) NOT NULL
            CONSTRAINT DF_turno_asistencia_ingreso DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT UQ_turno_asistencia_persona UNIQUE (turno_id, usuario_id),

        CONSTRAINT FK_turno_asistencia_turno
            FOREIGN KEY (turno_id)
            REFERENCES dbo.turnos (turno_id),

        CONSTRAINT FK_turno_asistencia_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES dbo.usuarios (usuario_id)
    );
END;
GO
