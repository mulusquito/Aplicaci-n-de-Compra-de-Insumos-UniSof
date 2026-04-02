-- Parche manual (PostgreSQL / DBeaver): rellena precio_unitario solo si está NULL
-- y el código coincide con el catálogo UNISOF. Ejecutar una vez si reiniciar la app no aplicó el parche Java.
-- Revisar en consola: filas actualizadas.

UPDATE insumos AS i
SET precio_unitario = m.precio
FROM (
    VALUES
        ('REF-TEL-OXF-NAV', 26500::numeric),
        ('REF-TEL-OXF-BLC', 26500),
        ('REF-TEL-EASY-CRM', 24200),
        ('REF-TEL-LINO-ALG', 31000),
        ('REF-TEL-TEXT-GRS', 27800),
        ('REF-TEL-ESP-NGR', 29500),
        ('REF-TEL-PIEL-CFE', 35200),
        ('REF-TEL-TEC-GRF', 32800),
        ('REF-TEL-ACOL-NEG', 19800),
        ('REF-TEL-CHINO-KAK', 25500),
        ('REF-TEL-PANA-TIE', 28900),
        ('REF-TEL-TRJ-RAY', 42000),
        ('REF-TEL-VIS-BAS', 8900),
        ('CREM-18-NGR', 3200),
        ('CREM-20-NAV', 2800),
        ('CREM-22-INV', 2400),
        ('RIB-1X1-NGR', 11800),
        ('RIB-1X1-BLC', 11800),
        ('RIB-2X2-GRM', 12500),
        ('HILO-POL-NEG-120', 8200),
        ('HILO-POL-BLC-120', 8200),
        ('HILO-LIN-NAT-80', 11200),
        ('BTN-NAC-18L', 9600),
        ('BTN-PLA-15N', 4200),
        ('BTN-MET-20P', 10800),
        ('ENT-CAM-FUS', 7400),
        ('ENT-BLZ-MED', 8100),
        ('ETQ-TEJ-UNI', 180),
        ('BOL-KRAFT-M', 450)
) AS m(codigo, precio)
WHERE i.precio_unitario IS NULL
  AND UPPER(TRIM(i.codigo)) = UPPER(TRIM(m.codigo));
