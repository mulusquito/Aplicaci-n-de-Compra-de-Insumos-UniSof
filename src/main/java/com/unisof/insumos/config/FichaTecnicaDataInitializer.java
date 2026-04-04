package com.unisof.insumos.config;

import com.unisof.insumos.model.FichaTecnica;
import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.repository.FichaTecnicaRepository;
import com.unisof.insumos.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Puebla fichas_tecnicas con materiales requeridos por prenda, género y talla.
 * Ejecuta después del seed de insumos (@Order 200 → este usa 300).
 *
 * · Géneros : Caballero / Dama
 * · Tallas  : XS S M L XL 2XL 3XL  (uniformes para todas las prendas)
 *
 * Mapeo prenda → insumos:
 *  Blaizeres  → tela + forro viscosa + entretela blaizer + botón metal + hilo negro + etiqueta + bolsa
 *  Camisas    → tela + entretela camisa + botón (nácar/plástico) + hilo + etiqueta + bolsa
 *  Cazadoras  → tela + rib (puños/cuello) + cremallera + hilo negro + etiqueta + bolsa
 *  Pantalones → tela [+ forro traje] + cremallera invisible + botón plástico + hilo negro + etiqueta + bolsa
 */
@Component
@Order(300)
@RequiredArgsConstructor
@Slf4j
public class FichaTecnicaDataInitializer implements CommandLineRunner {

    private final FichaTecnicaRepository fichaRepo;
    private final InsumoRepository insumoRepo;

    private Map<String, Insumo> insumoMap;
    private List<FichaTecnica> fichas;

    private static final String CABALLERO = "Caballero";
    private static final String DAMA      = "Dama";
    private static final String[] TALLAS  = {"XS", "S", "M", "L", "XL", "2XL", "3XL"};

    @Override
    public void run(String... args) {
        if (fichaRepo.count() > 0) {
            // Si ya hay fichas pero ninguna tiene género, es seed antiguo → re-seeder
            if (!fichaRepo.existsByGeneroIsNotNull()) {
                log.info("Fichas técnicas sin género detectadas. Re-inicializando con Caballero/Dama…");
                fichaRepo.deleteAll();
            } else {
                log.info("Fichas técnicas ya inicializadas ({} registros). Omitiendo seed.", fichaRepo.count());
                return;
            }
        }

        List<Insumo> insumos = insumoRepo.findAll();
        if (insumos.isEmpty()) {
            log.warn("Sin insumos en BD — fichas técnicas no inicializadas.");
            return;
        }

        insumoMap = insumos.stream().collect(Collectors.toMap(Insumo::getCodigo, Function.identity()));
        fichas = new ArrayList<>();

        poblarFichas();

        fichaRepo.saveAll(fichas);
        log.info("Fichas técnicas inicializadas: {} registros (13 prendas × 2 géneros × 7 tallas).", fichas.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void poblarFichas() {

        // Cantidades de tela (metros) por índice de talla XS→3XL
        double[] telaBlz  = {1.60, 1.70, 1.80, 1.90, 2.00, 2.20, 2.40};
        double[] telaCam  = {1.20, 1.30, 1.40, 1.50, 1.60, 1.80, 2.00};
        double[] telaCaz  = {1.60, 1.70, 1.80, 1.90, 2.00, 2.20, 2.40};
        double[] forroBlz = {1.30, 1.40, 1.50, 1.60, 1.70, 1.90, 2.10};
        double[] entreBlz = {0.70, 0.75, 0.80, 0.85, 0.90, 1.00, 1.10};
        double[] entreCam = {0.25, 0.28, 0.30, 0.32, 0.35, 0.38, 0.40};
        double[] ribCaz   = {0.25, 0.28, 0.30, 0.32, 0.35, 0.38, 0.40};
        double[] telaPant = {1.20, 1.30, 1.40, 1.50, 1.60, 1.80, 2.00};
        double[] forroPantTrj = {0.80, 0.85, 0.90, 0.95, 1.00, 1.10, 1.20};

        for (String g : new String[]{CABALLERO, DAMA}) {

            // ── BLAIZERES ────────────────────────────────────────────────
            for (int i = 0; i < TALLAS.length; i++) {
                blazer("Blaizer clásico",  g, TALLAS[i], "REF-TEL-ESP-NGR", telaBlz[i], forroBlz[i], entreBlz[i]);
                blazer("Blaizer slim fit", g, TALLAS[i], "REF-TEL-OXF-NAV", telaBlz[i], forroBlz[i], entreBlz[i]);
            }

            // ── CAMISAS ──────────────────────────────────────────────────
            for (int i = 0; i < TALLAS.length; i++) {
                camisa("Camisa Estructura Easy Care",       g, TALLAS[i], "REF-TEL-EASY-CRM", telaCam[i], entreCam[i], "BTN-NAC-18L",  "HILO-POL-BLC-120");
                camisa("Camisa Estructura Oxford",          g, TALLAS[i], "REF-TEL-OXF-BLC",  telaCam[i], entreCam[i], "BTN-NAC-18L",  "HILO-POL-BLC-120");
                camisa("Camisa Estructura Textura",         g, TALLAS[i], "REF-TEL-TEXT-GRS", telaCam[i], entreCam[i], "BTN-PLA-15N",  "HILO-POL-NEG-120");
                // Lino-Algodón solo en catálogo Caballero
                if (CABALLERO.equals(g)) {
                    camisa("Camisa Regular Fit Lino - Algodón", g, TALLAS[i], "REF-TEL-LINO-ALG", telaCam[i], entreCam[i], "BTN-NAC-18L", "HILO-LIN-NAT-80");
                }
            }

            // ── CAZADORAS ────────────────────────────────────────────────
            for (int i = 0; i < TALLAS.length; i++) {
                cazadora("Cazadora estructura espiga",  g, TALLAS[i], "REF-TEL-ESP-NGR",  telaCaz[i], ribCaz[i], "RIB-1X1-NGR", "CREM-18-NGR");
                cazadora("Cazadora efecto piel",        g, TALLAS[i], "REF-TEL-PIEL-CFE", telaCaz[i], ribCaz[i], "RIB-1X1-NGR", "CREM-18-NGR");
                cazadora("Cazadora técnica capucha",    g, TALLAS[i], "REF-TEL-TEC-GRF",  telaCaz[i], ribCaz[i], "RIB-2X2-GRM", "CREM-20-NAV");
                // Acolchada solo en catálogo Caballero
                if (CABALLERO.equals(g)) {
                    cazadora("Cazadora acolchada capucha", g, TALLAS[i], "REF-TEL-ACOL-NEG", telaCaz[i], ribCaz[i], "RIB-1X1-NGR", "CREM-18-NGR");
                }
            }

            // ── PANTALONES ───────────────────────────────────────────────
            for (int i = 0; i < TALLAS.length; i++) {
                pantalon("Pantalón Chino Estructura Confort",        g, TALLAS[i], "REF-TEL-CHINO-KAK", telaPant[i], null);
                pantalon("Pantalón Pana Baggy Fit Costuras Giradas", g, TALLAS[i], "REF-TEL-PANA-TIE",  telaPant[i], null);
                pantalon("Pantalón Traje Raya Diplomática",          g, TALLAS[i], "REF-TEL-TRJ-RAY",   telaPant[i], forroPantTrj[i]);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void blazer(String prenda, String genero, String talla, String codigoTela,
                        double metrosTela, double metrosForro, double metrosEntretela) {
        add(prenda, genero, talla, codigoTela,          metrosTela);
        add(prenda, genero, talla, "REF-TEL-VIS-BAS",   metrosForro);
        add(prenda, genero, talla, "ENT-BLZ-MED",        metrosEntretela);
        add(prenda, genero, talla, "BTN-MET-20P",         0.50);   // ~6 botones ÷ 12 = 0.50 docenas
        add(prenda, genero, talla, "HILO-POL-NEG-120",    1.0);
        add(prenda, genero, talla, "ETQ-TEJ-UNI",         1.0);
        add(prenda, genero, talla, "BOL-KRAFT-M",         1.0);
    }

    private void camisa(String prenda, String genero, String talla, String codigoTela,
                        double metrosTela, double metrosEntretela,
                        String codigoBoton, String codigoHilo) {
        add(prenda, genero, talla, codigoTela,          metrosTela);
        add(prenda, genero, talla, "ENT-CAM-FUS",        metrosEntretela);
        add(prenda, genero, talla, codigoBoton,           0.70);   // ~8 botones ÷ 12 ≈ 0.70 docenas
        add(prenda, genero, talla, codigoHilo,            1.0);
        add(prenda, genero, talla, "ETQ-TEJ-UNI",         1.0);
        add(prenda, genero, talla, "BOL-KRAFT-M",         1.0);
    }

    private void cazadora(String prenda, String genero, String talla, String codigoTela,
                          double metrosTela, double metrosRib,
                          String codigoRib, String codigoCremallera) {
        add(prenda, genero, talla, codigoTela,          metrosTela);
        add(prenda, genero, talla, codigoRib,            metrosRib);
        add(prenda, genero, talla, codigoCremallera,      1.0);
        add(prenda, genero, talla, "HILO-POL-NEG-120",    1.0);
        add(prenda, genero, talla, "ETQ-TEJ-UNI",         1.0);
        add(prenda, genero, talla, "BOL-KRAFT-M",         1.0);
    }

    private void pantalon(String prenda, String genero, String talla, String codigoTela,
                          double metrosTela, Double metrosForro) {
        add(prenda, genero, talla, codigoTela,          metrosTela);
        if (metrosForro != null) {
            add(prenda, genero, talla, "REF-TEL-VIS-BAS", metrosForro);
        }
        add(prenda, genero, talla, "CREM-22-INV",         1.0);
        add(prenda, genero, talla, "BTN-PLA-15N",          0.10);  // 1 botón ÷ 12 ≈ 0.10 docenas
        add(prenda, genero, talla, "HILO-POL-NEG-120",     1.0);
        add(prenda, genero, talla, "ETQ-TEJ-UNI",          1.0);
        add(prenda, genero, talla, "BOL-KRAFT-M",          1.0);
    }

    private void add(String prenda, String genero, String talla, String codigoInsumo, double cantidad) {
        Insumo insumo = insumoMap.get(codigoInsumo);
        if (insumo == null) {
            log.warn("Insumo '{}' no encontrado (prenda={}, genero={}, talla={}). Línea omitida.",
                    codigoInsumo, prenda, genero, talla);
            return;
        }
        fichas.add(new FichaTecnica(prenda, genero, talla, insumo, BigDecimal.valueOf(cantidad)));
    }
}
