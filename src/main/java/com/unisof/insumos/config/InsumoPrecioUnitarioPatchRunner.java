package com.unisof.insumos.config;

import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Completa {@code precio_unitario} en insumos existentes cuando está en NULL, usando
 * {@link InsumoPreciosReferenciaCOP} según el <strong>código</strong> del insumo.
 * No sobrescribe precios ya cargados.
 * <p>
 * Recorre todos los registros (no solo claves del catálogo) para que coincida aunque
 * el código tenga distinto uso de mayúsculas en BD.
 * </p>
 */
@Slf4j
@Component
@Order(201)
@RequiredArgsConstructor
public class InsumoPrecioUnitarioPatchRunner implements ApplicationRunner {

    private final InsumoRepository insumoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int actualizados = 0;
        int sinCatalogo = 0;
        List<Insumo> pendientes = new ArrayList<>();
        for (Insumo ins : insumoRepository.findAll()) {
            if (ins.getPrecioUnitario() != null) {
                continue;
            }
            var opt = InsumoPreciosReferenciaCOP.precioParaCodigo(ins.getCodigo());
            if (opt.isEmpty()) {
                sinCatalogo++;
                continue;
            }
            ins.setPrecioUnitario(opt.get());
            pendientes.add(ins);
            actualizados++;
        }
        if (!pendientes.isEmpty()) {
            insumoRepository.saveAll(pendientes);
        }
        if (actualizados > 0) {
            log.info("UNISOF: precio unitario de referencia aplicado a {} insumo(s).", actualizados);
        }
        if (sinCatalogo > 0) {
            log.warn("UNISOF: {} insumo(s) sin precio y sin entrada en el catálogo de referencia (revise el código o cargue precio en inventario).",
                    sinCatalogo);
        }
        if (actualizados == 0 && sinCatalogo == 0) {
            log.debug("UNISOF: parche precios — nada que actualizar (todos con precio o tabla vacía).");
        }
    }
}
