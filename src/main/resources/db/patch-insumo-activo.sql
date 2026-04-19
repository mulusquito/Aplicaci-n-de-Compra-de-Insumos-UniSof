-- Parche: soft-delete en tabla insumos
-- Ejecutar una sola vez en PostgreSQL (pgAdmin o psql) antes de reiniciar el servidor.
--
-- Nota: Hibernate ddl-auto=update puede emitir "ADD activo BOOLEAN NOT NULL" sin DEFAULT;
-- en tablas con filas eso falla en PostgreSQL. Este script usa DEFAULT TRUE explícito.
-- Si la columna ya existe pero admite NULL, ejecutar antes:
--   UPDATE insumos SET activo = TRUE WHERE activo IS NULL;
--   ALTER TABLE insumos ALTER COLUMN activo SET NOT NULL;
--   ALTER TABLE insumos ALTER COLUMN activo SET DEFAULT TRUE;

ALTER TABLE insumos
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;
