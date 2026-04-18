package com.unisof.insumos.service;

import com.unisof.insumos.dto.CategoriaInsumoResponse;
import com.unisof.insumos.dto.ProveedorRequest;
import com.unisof.insumos.dto.ProveedorResponse;
import com.unisof.insumos.model.CategoriaInsumo;
import com.unisof.insumos.model.Proveedor;
import com.unisof.insumos.repository.CategoriaInsumoRepository;
import com.unisof.insumos.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de proveedores de insumos — Proceso 3.
 *
 * <p>Gestiona el ciclo de vida completo de un {@link com.unisof.insumos.model.Proveedor}:
 * registro, consulta, actualización y eliminación. Valida unicidad de NIT y correo
 * electrónico antes de persistir, y envía notificación por correo al proveedor
 * al momento del registro.</p>
 *
 * <p>Un proveedor debe tener al menos una {@link com.unisof.insumos.model.CategoriaInsumo}
 * asignada, que indica qué tipo de insumos suministra.</p>
 *
 * @see com.unisof.insumos.controller.ProveedorController
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProveedorService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private final ProveedorRepository proveedorRepository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<ProveedorResponse> listar(String criterio, String categoriaCodigo, String nit) {
        String crit = criterio != null ? criterio.trim() : "";
        String cat = categoriaCodigo != null ? categoriaCodigo.trim().toUpperCase(Locale.ROOT) : "";
        String nitF = nit != null ? nit.trim() : "";

        List<Proveedor> lista;
        if (!cat.isEmpty()) {
            lista = proveedorRepository.findByCategoriaCodigoWithCategorias(cat);
        } else if (!nitF.isEmpty()) {
            lista = proveedorRepository.findByNitContainingIgnoreCaseWithCategorias(nitF);
        } else if (!crit.isEmpty()) {
            lista = proveedorRepository.findByNombreContainingIgnoreCaseWithCategorias(crit);
        } else {
            lista = proveedorRepository.findAllWithCategoriasOrderByNombreAsc();
        }

        return lista.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtener(Long id) {
        return proveedorRepository.findByIdWithCategorias(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado."));
    }

    @Transactional
    public ProveedorResponse crear(ProveedorRequest req) {
        validarNitUnico(req.getNit(), null);
        validarCorreoUnico(req.getCorreo(), null);
        Proveedor p = new Proveedor();
        aplicar(p, req);
        if (p.getFechaRegistro() == null) {
            p.setFechaRegistro(Instant.now());
        }
        Proveedor guardado = proveedorRepository.save(p);
        notificarRegistroProveedorPorCorreo(guardado);
        return toResponse(guardado);
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest req) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado."));
        validarNitUnico(req.getNit(), id);
        validarCorreoUnico(req.getCorreo(), id);
        aplicar(p, req);
        return toResponse(proveedorRepository.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new IllegalArgumentException("Proveedor no encontrado.");
        }
        proveedorRepository.deleteById(id);
    }

    private void validarNitUnico(String nit, Long excludeId) {
        if (nit == null || nit.isBlank()) return;
        String n = nit.trim();
        proveedorRepository.findByNit(n).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new IllegalArgumentException("Ya existe un proveedor con ese NIT o documento.");
            }
        });
    }

    private void validarCorreoUnico(String correo, Long excludeId) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        String c = correo.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(c).matches()) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        proveedorRepository.findByCorreoIgnoreCase(c).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new IllegalArgumentException("Ya existe un proveedor registrado con ese correo electrónico.");
            }
        });
    }

    private void notificarRegistroProveedorPorCorreo(Proveedor p) {
        if (p.getCorreo() == null || p.getCorreo().isBlank()) {
            return;
        }
        try {
            List<String> nombres = p.getCategorias().stream()
                    .map(CategoriaInsumo::getNombre)
                    .filter(n -> n != null && !n.isBlank())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
            emailService.enviarNotificacionRegistroProveedor(p.getCorreo().trim(), p.getNombre(), nombres);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de registro al proveedor {}: {}", p.getCorreo(), e.getMessage());
        }
    }

    private void aplicar(Proveedor p, ProveedorRequest req) {
        p.setNombre(req.getNombre().trim());
        p.setNit(req.getNit() != null && !req.getNit().isBlank() ? req.getNit().trim() : null);
        p.setContactoNombre(blankToNull(req.getContactoNombre()));
        p.setTelefono(blankToNull(req.getTelefono()));
        String correoNorm = blankToNull(req.getCorreo());
        p.setCorreo(correoNorm == null ? null : correoNorm.toLowerCase(Locale.ROOT));
        p.setDireccion(blankToNull(req.getDireccion()));
        p.setObservaciones(blankToNull(req.getObservaciones()));
        if (req.getActivo() != null) {
            p.setActivo(req.getActivo());
        }

        p.getCategorias().clear();
        if (req.getCategoriaCodigos() == null || req.getCategoriaCodigos().isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos una categoría de insumo.");
        }
        for (String raw : req.getCategoriaCodigos()) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String codigo = raw.trim().toUpperCase(Locale.ROOT);
            CategoriaInsumo cat = categoriaInsumoRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría de insumo no reconocida: " + raw.trim()));
            p.getCategorias().add(cat);
        }
        if (p.getCategorias().isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos una categoría de insumo.");
        }
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private ProveedorResponse toResponse(Proveedor p) {
        List<CategoriaInsumoResponse> categorias = p.getCategorias() == null ? List.of() : p.getCategorias().stream()
                .sorted(Comparator.comparing(CategoriaInsumo::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(c -> CategoriaInsumoResponse.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .descripcion(c.getDescripcion())
                        .build())
                .collect(Collectors.toList());

        return ProveedorResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .nit(p.getNit())
                .contactoNombre(p.getContactoNombre())
                .telefono(p.getTelefono())
                .correo(p.getCorreo())
                .direccion(p.getDireccion())
                .observaciones(p.getObservaciones())
                .activo(p.isActivo())
                .fechaRegistro(p.getFechaRegistro())
                .categorias(categorias)
                .build();
    }
}
