-- Parche: agrega columna facturas_generadas a reportes_consolidados
-- Ejecutar una sola vez en PostgreSQL (pgAdmin o psql)

ALTER TABLE reportes_consolidados
    ADD COLUMN IF NOT EXISTS facturas_generadas BOOLEAN NOT NULL DEFAULT FALSE;
