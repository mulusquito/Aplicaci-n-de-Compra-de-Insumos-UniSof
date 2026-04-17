package com.unisof.insumos.service;

import com.unisof.insumos.dto.CategoriaInsumoResponse;
import com.unisof.insumos.dto.InsumoRequest;
import com.unisof.insumos.dto.InsumoResponse;
import com.unisof.insumos.model.CategoriaInsumo;
import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.repository.CategoriaInsumoRepository;
import com.unisof.insumos.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprasInsumoService {

    private final InsumoRepository insumoRepository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;

    public List<CategoriaInsumoResponse> listarCategorias() {
        return categoriaInsumoRepository.findAll().stream()
                .sorted(Comparator.comparing(CategoriaInsumo::getCodigo))
                .map(this::toCategoriaResponse)
                .toList();
    }

    public List<InsumoResponse> listarInsumos(String categoriaCodigo, String criterio, String tipo) {
        List<Insumo> base;
        boolean filtroCat = categoriaCodigo != null && !categoriaCodigo.isBlank();
        boolean busca = criterio != null && !criterio.isBlank();
        String tipoNorm = tipo != null ? tipo.trim().toLowerCase() : "nombre";

        // Solo insumos activos (soft-delete)
        if (busca) {
            if ("codigo".equals(tipoNorm)) {
                base = insumoRepository.findByActivoTrueAndCodigoContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc(criterio.trim());
            } else {
                base = insumoRepository.findByActivoTrueAndNombreContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc(criterio.trim());
            }
        } else if (filtroCat) {
            base = insumoRepository.findByActivoTrueAndCategoria_CodigoOrderByNombreAsc(categoriaCodigo.trim());
        } else {
            base = insumoRepository.findByActivoTrueOrderByCategoria_CodigoAscNombreAsc();
        }

        if (filtroCat && busca) {
            String cc = categoriaCodigo.trim();
            base = base.stream().filter(i -> i.getCategoria() != null && cc.equalsIgnoreCase(i.getCategoria().getCodigo())).toList();
        }

        return base.stream().map(this::toInsumoResponse).toList();
    }

    public InsumoResponse obtener(Long id) {
        Insumo i = insumoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado."));
        return toInsumoResponse(i);
    }

    @Transactional
    public InsumoResponse crear(InsumoRequest req) {
        String cod = req.getCodigo().trim();
        if (insumoRepository.existsByCodigoIgnoreCase(cod)) {
            throw new IllegalArgumentException("Ya existe un insumo con ese código.");
        }
        CategoriaInsumo cat = categoriaInsumoRepository.findByCodigo(req.getCategoriaCodigo().trim())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no válida."));
        Insumo i = new Insumo();
        i.setCodigo(cod);
        aplicarCampos(i, req, cat);
        return toInsumoResponse(insumoRepository.save(i));
    }

    @Transactional
    public InsumoResponse actualizar(Long id, InsumoRequest req) {
        Insumo i = insumoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado."));
        if (!i.getCodigo().equalsIgnoreCase(req.getCodigo().trim())) {
            throw new IllegalArgumentException("No se puede cambiar el código del insumo.");
        }
        CategoriaInsumo cat = categoriaInsumoRepository.findByCodigo(req.getCategoriaCodigo().trim())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no válida."));
        aplicarCampos(i, req, cat);
        return toInsumoResponse(insumoRepository.save(i));
    }

    /** Soft-delete: marca el insumo como inactivo sin borrar el registro. */
    @Transactional
    public void eliminar(Long id) {
        Insumo i = insumoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado."));
        i.setActivo(false);
        insumoRepository.save(i);
    }

    private void aplicarCampos(Insumo i, InsumoRequest req, CategoriaInsumo cat) {
        i.setNombre(req.getNombre().trim());
        i.setCategoria(cat);
        i.setUnidadMedida(req.getUnidadMedida().trim());
        if (req.getStockDisponible().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El stock disponible no puede ser negativo.");
        }
        i.setStockDisponible(req.getStockDisponible());
        if (req.getStockMinimo() != null) {
            if (req.getStockMinimo().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
            }
            i.setStockMinimo(req.getStockMinimo());
        } else {
            i.setStockMinimo(null);
        }
        if (req.getPrecioUnitario() != null) {
            if (req.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio unitario no puede ser negativo.");
            }
            i.setPrecioUnitario(req.getPrecioUnitario());
        } else {
            i.setPrecioUnitario(null);
        }
        i.setReferenciaTela(blankToNull(req.getReferenciaTela()));
        i.setColor(blankToNull(req.getColor()));
        i.setObservaciones(blankToNull(req.getObservaciones()));
        i.setProductosCatalogo(blankToNull(req.getProductosCatalogo()));
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private CategoriaInsumoResponse toCategoriaResponse(CategoriaInsumo c) {
        return CategoriaInsumoResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .build();
    }

    public InsumoResponse toInsumoResponse(Insumo i) {
        CategoriaInsumo c = i.getCategoria();
        return InsumoResponse.builder()
                .id(i.getId())
                .codigo(i.getCodigo())
                .nombre(i.getNombre())
                .categoriaCodigo(c != null ? c.getCodigo() : null)
                .categoriaNombre(c != null ? c.getNombre() : null)
                .unidadMedida(i.getUnidadMedida())
                .stockDisponible(i.getStockDisponible())
                .stockMinimo(i.getStockMinimo())
                .precioUnitario(i.getPrecioUnitario())
                .referenciaTela(i.getReferenciaTela())
                .color(i.getColor())
                .observaciones(i.getObservaciones())
                .productosCatalogo(i.getProductosCatalogo())
                .build();
    }

    public List<Insumo> todosOrdenados() {
        return insumoRepository.findByActivoTrueOrderByCategoria_CodigoAscNombreAsc();
    }

    /** Suma de stock mínimo donde está definido (proxy de “requerido” hasta exista BOM/pedidos). */
    public BigDecimal sumaStockMinimo() {
        return todosOrdenados().stream()
                .map(Insumo::getStockMinimo)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal sumaStockDisponible() {
        return todosOrdenados().stream()
                .map(Insumo::getStockDisponible)
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long contarBajoMinimo() {
        return todosOrdenados().stream()
                .filter(i -> i.getStockMinimo() != null
                        && i.getStockDisponible() != null
                        && i.getStockDisponible().compareTo(i.getStockMinimo()) <= 0)
                .count();
    }
}
