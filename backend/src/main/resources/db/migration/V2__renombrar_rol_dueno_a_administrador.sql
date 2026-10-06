/* ============================================================
   V2 - Renombrar el rol DUEÑO a ADMINISTRADOR
   ------------------------------------------------------------
   Decisión del equipo (AGENTS.md, sección 7): el rol con control
   total del sistema se denomina ADMINISTRADOR. Se conserva el
   mismo rol_id, por lo que los usuarios existentes mantienen su
   asignación. La migración V1 no se modifica.
   ============================================================ */

UPDATE dbo.roles
SET nombre = N'ADMINISTRADOR'
WHERE nombre = N'DUEÑO';
