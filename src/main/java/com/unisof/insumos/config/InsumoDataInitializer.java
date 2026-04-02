package com.unisof.insumos.config;

import com.unisof.insumos.model.CategoriaInsumo;
import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.repository.CategoriaInsumoRepository;
import com.unisof.insumos.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Categorías e insumos de ejemplo alineados al catálogo de ventas UNISOF (index/ventas: mujer y hombre).
 * Solo ejecuta si la tabla de insumos está vacía. Si ya hay datos de una corrida anterior,
 * use el CRUD de inventario para unificar el texto «Prendas catálogo» o vacíe la tabla en desarrollo.
 * <p>
 * Los precios unitarios provienen de {@link InsumoPreciosReferenciaCOP} (COP por unidad de medida del insumo).
 * </p>
 */
@Component
@Order(200)
@RequiredArgsConstructor
public class InsumoDataInitializer implements ApplicationRunner {

    /**
     * Texto unificado para el campo productos_catalogo: todo insumo/tela del seed queda alineado
     * a las mismas familias que muestran index.html y ventas.html (mujer/hombre: blaizer, camisas, chaquetas, pantalones).
     */
    private static final String CATALOGO_UNISOF_TODAS_PRENDAS =
            "Catálogo UNISOF completo — mujer y hombre: blaizer, camisas, chaquetas/cazadoras y pantalones "
                    + "(todas las variantes publicadas en inicio y módulo ventas).";

    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final InsumoRepository insumoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (insumoRepository.count() > 0) {
            return;
        }
        ensureCategoria("TELAS", "Telas principales", "Telas planas (metros) para blaizer, camisas, chaquetas y pantalones del catálogo mujer/hombre.");
        ensureCategoria("CIERRES_CREMALLERAS", "Cierres y cremalleras", "Cremalleras metálicas y de nailon por medida.");
        ensureCategoria("TEJIDO_PUNTO", "Tejido de punto (rib)", "Rib y punto para cuellos, puños y acabados.");
        ensureCategoria("HILOS", "Hilos", "Conos de hilo para costura industrial.");
        ensureCategoria("BOTONES", "Botones", "Botones de nácar, plástico y metal.");
        ensureCategoria("ENTRETELAS", "Entretelas", "Entretelas termoadhesivas y de coser.");
        ensureCategoria("OTROS", "Otros insumos", "Etiquetas, empaques y consumibles.");

        CategoriaInsumo tel = cat("TELAS");
        CategoriaInsumo crem = cat("CIERRES_CREMALLERAS");
        CategoriaInsumo rib = cat("TEJIDO_PUNTO");
        CategoriaInsumo hil = cat("HILOS");
        CategoriaInsumo bot = cat("BOTONES");
        CategoriaInsumo ent = cat("ENTRETELAS");
        CategoriaInsumo otr = cat("OTROS");

        List<Insumo> batch = new ArrayList<>();
        batch.add(ins(tel, "REF-TEL-OXF-NAV", "Tela Oxford azul marino", "m", "120", "80", "REF-OXF-240", "Azul marino",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-OXF-BLC", "Tela Oxford blanco", "m", "95", "70", "REF-OXF-001", "Blanco",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-EASY-CRM", "Tela Easy Care crema", "m", "55", "60", "REF-EC-112", "Crema",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-LINO-ALG", "Mezcla lino-algodón natural", "m", "42", "50", "REF-LA-305", "Natural",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-TEXT-GRS", "Tela textura gris medio", "m", "88", "55", "REF-TX-88", "Gris",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-ESP-NGR", "Sarga estructura espiga negro", "m", "210", "120", "REF-ESP-901", "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-PIEL-CFE", "Sintético efecto piel café", "m", "38", "45", "REF-PL-210", "Café",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-TEC-GRF", "Tejido técnico gris grafito", "m", "150", "100", "REF-TC-440", "Grafito",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-ACOL-NEG", "Forro y carcasa acolchada negro", "m", "72", "90", "REF-AC-701", "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-CHINO-KAK", "Sarga chino kaki", "m", "180", "100", "REF-CH-18", "Kaki",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-PANA-TIE", "Pana fina tierra", "m", "64", "75", "REF-PN-332", "Tierra",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-TRJ-RAY", "Tela traje raya diplomática", "m", "52", "60", "REF-TRJ-55", "Azul raya",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(tel, "REF-TEL-VIS-BAS", "Forro viscosa básico", "m", "400", "200", "REF-VS-10", "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));

        batch.add(ins(crem, "CREM-18-NGR", "Cremallera metálica 18 cm negro", "ud", "2200", "800", null, "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(crem, "CREM-20-NAV", "Cremallera nailon 20 cm azul", "ud", "1800", "900", null, "Azul marino",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(crem, "CREM-22-INV", "Cremallera invisible 22 cm", "ud", "340", "500", null, "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));

        batch.add(ins(rib, "RIB-1X1-NGR", "Rib 1x1 negro puños/cuello", "m", "85", "120", "RIB-1X1-BK", "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(rib, "RIB-1X1-BLC", "Rib 1x1 blanco", "m", "110", "90", "RIB-1X1-WH", "Blanco",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(rib, "RIB-2X2-GRM", "Rib 2x2 gris melange", "m", "28", "40", "RIB-2X2-GM", "Gris melange",
                CATALOGO_UNISOF_TODAS_PRENDAS));

        batch.add(ins(hil, "HILO-POL-NEG-120", "Hilo poliéster 120 cono", "cono", "340", "200", null, "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(hil, "HILO-POL-BLC-120", "Hilo poliéster 120 blanco", "cono", "290", "180", null, "Blanco",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(hil, "HILO-LIN-NAT-80", "Hilo lino/natural 80", "cono", "95", "60", null, "Natural",
                CATALOGO_UNISOF_TODAS_PRENDAS));

        batch.add(ins(bot, "BTN-NAC-18L", "Botón nácar 18 mm", "docena", "140", "80", null, "Nácar",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(bot, "BTN-PLA-15N", "Botón plástico 15 mm negro", "docena", "400", "250", null, "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(bot, "BTN-MET-20P", "Botón metal 20 mm plateado", "docena", "22", "40", null, "Plateado",
                CATALOGO_UNISOF_TODAS_PRENDAS));

        batch.add(ins(ent, "ENT-CAM-FUS", "Entretela camisa fusible ligera", "m", "95", "70", "ENT-CAM-L", "Blanco",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(ent, "ENT-BLZ-MED", "Entretela blaizer mediana", "m", "48", "55", "ENT-BLZ-M", "Negro",
                CATALOGO_UNISOF_TODAS_PRENDAS));

        batch.add(ins(otr, "ETQ-TEJ-UNI", "Etiqueta tejida UNISOF", "ud", "5000", "2000", null, "Multicolor",
                CATALOGO_UNISOF_TODAS_PRENDAS));
        batch.add(ins(otr, "BOL-KRAFT-M", "Bolsa kraft mediana e-commerce", "ud", "3200", "1500", null, "Kraft",
                CATALOGO_UNISOF_TODAS_PRENDAS));

        insumoRepository.saveAll(batch);
    }

    private void ensureCategoria(String codigo, String nombre, String descripcion) {
        if (categoriaInsumoRepository.findByCodigo(codigo).isPresent()) {
            return;
        }
        CategoriaInsumo c = new CategoriaInsumo();
        c.setCodigo(codigo);
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        categoriaInsumoRepository.save(c);
    }

    private CategoriaInsumo cat(String codigo) {
        return categoriaInsumoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalStateException("Categoría no encontrada: " + codigo));
    }

    private static Insumo ins(
            CategoriaInsumo categoria,
            String codigo,
            String nombre,
            String unidad,
            String stock,
            String minimo,
            String refTela,
            String color,
            String catalogo
    ) {
        Insumo i = new Insumo();
        i.setCategoria(categoria);
        i.setCodigo(codigo);
        i.setNombre(nombre);
        i.setUnidadMedida(unidad);
        i.setStockDisponible(new BigDecimal(stock));
        i.setStockMinimo(new BigDecimal(minimo));
        InsumoPreciosReferenciaCOP.precioParaCodigo(codigo).ifPresent(i::setPrecioUnitario);
        i.setReferenciaTela(refTela);
        i.setColor(color);
        i.setProductosCatalogo(catalogo);
        i.setObservaciones(null);
        return i;
    }
}
