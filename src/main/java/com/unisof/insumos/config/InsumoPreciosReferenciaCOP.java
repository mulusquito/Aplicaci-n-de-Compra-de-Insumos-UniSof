package com.unisof.insumos.config;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Precios de referencia en COP por <strong>código de insumo</strong>, alineados al catálogo UNISOF del seed.
 * <p>
 * El importe es siempre <strong>por una unidad de la medida del insumo</strong> ({@code unidadMedida}): por metro si es {@code m},
 * por unidad si es {@code ud}, por cono, por docena, etc. Así, al emitir órdenes de compra a proveedores se multiplica
 * la cantidad pedida (en esa misma unidad) × este precio para estimar el subtotal de la línea.
 * </p>
 */
public final class InsumoPreciosReferenciaCOP {

    private InsumoPreciosReferenciaCOP() {
    }

    private static final Map<String, BigDecimal> MAP;

    static {
        Map<String, String> raw = new LinkedHashMap<>();
        // Telas (COP / metro)
        raw.put("REF-TEL-OXF-NAV", "26500");
        raw.put("REF-TEL-OXF-BLC", "26500");
        raw.put("REF-TEL-EASY-CRM", "24200");
        raw.put("REF-TEL-LINO-ALG", "31000");
        raw.put("REF-TEL-TEXT-GRS", "27800");
        raw.put("REF-TEL-ESP-NGR", "29500");
        raw.put("REF-TEL-PIEL-CFE", "35200");
        raw.put("REF-TEL-TEC-GRF", "32800");
        raw.put("REF-TEL-ACOL-NEG", "19800");
        raw.put("REF-TEL-CHINO-KAK", "25500");
        raw.put("REF-TEL-PANA-TIE", "28900");
        raw.put("REF-TEL-TRJ-RAY", "42000");
        raw.put("REF-TEL-VIS-BAS", "8900");
        // Cremalleras (COP / ud)
        raw.put("CREM-18-NGR", "3200");
        raw.put("CREM-20-NAV", "2800");
        raw.put("CREM-22-INV", "2400");
        // Rib (COP / metro)
        raw.put("RIB-1X1-NGR", "11800");
        raw.put("RIB-1X1-BLC", "11800");
        raw.put("RIB-2X2-GRM", "12500");
        // Hilos (COP / cono)
        raw.put("HILO-POL-NEG-120", "8200");
        raw.put("HILO-POL-BLC-120", "8200");
        raw.put("HILO-LIN-NAT-80", "11200");
        // Botones (COP / docena)
        raw.put("BTN-NAC-18L", "9600");
        raw.put("BTN-PLA-15N", "4200");
        raw.put("BTN-MET-20P", "10800");
        // Entretelas (COP / metro)
        raw.put("ENT-CAM-FUS", "7400");
        raw.put("ENT-BLZ-MED", "8100");
        // Otros (COP / ud)
        raw.put("ETQ-TEJ-UNI", "180");
        raw.put("BOL-KRAFT-M", "450");

        Map<String, BigDecimal> built = new LinkedHashMap<>();
        raw.forEach((k, v) -> built.put(k.toUpperCase(Locale.ROOT), new BigDecimal(v)));
        MAP = Collections.unmodifiableMap(built);
    }

    /** Mapa inmutable: código de insumo (mayúsculas) → precio COP por unidad de medida. */
    public static Map<String, BigDecimal> porCodigo() {
        return MAP;
    }

    /**
     * Precio de referencia para el código, si existe en el catálogo (clave en mayúsculas).
     */
    public static Optional<BigDecimal> precioParaCodigo(String codigoInsumo) {
        if (codigoInsumo == null || codigoInsumo.isBlank()) {
            return Optional.empty();
        }
        String key = codigoInsumo.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(MAP.get(key));
    }
}
