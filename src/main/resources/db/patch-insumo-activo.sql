-- Parche: soft-delete en tabla insumos
-- Ejecutar una sola vez en PostgreSQL (pgAdmin o psql) antes de reiniciar el servidor

ALTER TABLE insumos
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;
