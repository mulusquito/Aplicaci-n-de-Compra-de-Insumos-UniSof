-- Agregar columna estado a la tabla recibos (para recibos existentes)
ALTER TABLE recibos ADD COLUMN IF NOT EXISTS estado VARCHAR(20) DEFAULT 'PENDIENTE';
UPDATE recibos SET estado = 'PENDIENTE' WHERE estado IS NULL;
ALTER TABLE recibos ALTER COLUMN estado SET NOT NULL;
