package com.unisof.insumos.config;

import com.unisof.insumos.model.CategoriaInsumo;
import com.unisof.insumos.model.Proveedor;
import com.unisof.insumos.repository.CategoriaInsumoRepository;
import com.unisof.insumos.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Un proveedor de demostración por cada categoría de insumo (misma taxonomía que {@link InsumoDataInitializer}).
 * Solo corre si no hay proveedores; las categorías deben existir (orden 200 vs 250).
 */
@Component
@Order(250)
@RequiredArgsConstructor
public class ProveedorDataInitializer implements ApplicationRunner {

    private static final String[][] DEMOS = {
            {"TELAS", "Proveedor demo — Telas principales", "901-PROV-TEL-01"},
            {"CIERRES_CREMALLERAS", "Proveedor demo — Cierres y cremalleras", "901-PROV-CREM-02"},
            {"TEJIDO_PUNTO", "Proveedor demo — Tejido de punto (rib)", "901-PROV-RIB-03"},
            {"HILOS", "Proveedor demo — Hilos", "901-PROV-HIL-04"},
            {"BOTONES", "Proveedor demo — Botones", "901-PROV-BTN-05"},
            {"ENTRETELAS", "Proveedor demo — Entretelas", "901-PROV-ENT-06"},
            {"OTROS", "Proveedor demo — Otros insumos", "901-PROV-OTR-07"},
    };

    private final ProveedorRepository proveedorRepository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (proveedorRepository.count() > 0) {
            return;
        }
        if (categoriaInsumoRepository.count() == 0) {
            return;
        }
        for (String[] row : DEMOS) {
            String codigoCategoria = row[0];
            categoriaInsumoRepository.findByCodigo(codigoCategoria).ifPresent(cat -> crearProveedor(cat, row[1], row[2]));
        }
    }

    private void crearProveedor(CategoriaInsumo categoria, String nombre, String nit) {
        Proveedor p = new Proveedor();
        p.setNombre(nombre);
        p.setNit(nit);
        p.setContactoNombre("Contacto comercial");
        p.setTelefono("6015550000");
        p.setCorreo("demo.proveedor." + categoria.getCodigo().toLowerCase() + "@unisof.local");
        p.setDireccion("Bogotá — datos de prueba");
        p.setObservaciones("Proveedor sembrado automáticamente para la categoría " + categoria.getNombre() + ".");
        p.getCategorias().add(categoria);
        proveedorRepository.save(p);
    }
}
