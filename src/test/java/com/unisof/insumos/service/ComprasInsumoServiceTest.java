package com.unisof.insumos.service;

import com.unisof.insumos.dto.CategoriaInsumoResponse;
import com.unisof.insumos.dto.InsumoRequest;
import com.unisof.insumos.dto.InsumoResponse;
import com.unisof.insumos.model.CategoriaInsumo;
import com.unisof.insumos.model.Insumo;
import com.unisof.insumos.repository.CategoriaInsumoRepository;
import com.unisof.insumos.repository.InsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link ComprasInsumoService} — Proceso 3.
 * Cubre CRUD de insumos: listar, obtener, crear, actualizar y soft-delete.
 */
@ExtendWith(MockitoExtension.class)
class ComprasInsumoServiceTest {

    @Mock
    private InsumoRepository insumoRepository;

    @Mock
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @InjectMocks
    private ComprasInsumoService service;

    private CategoriaInsumo categoriaTela;
    private Insumo insumoBase;
    private InsumoRequest requestBase;

    @BeforeEach
    void setUp() {
        categoriaTela = new CategoriaInsumo();
        categoriaTela.setId(1L);
        categoriaTela.setCodigo("TEL");
        categoriaTela.setNombre("Telas");

        insumoBase = new Insumo();
        insumoBase.setId(10L);
        insumoBase.setCodigo("TEL-001");
        insumoBase.setNombre("Tela algodón");
        insumoBase.setCategoria(categoriaTela);
        insumoBase.setUnidadMedida("m");
        insumoBase.setStockDisponible(new BigDecimal("100.00"));
        insumoBase.setStockMinimo(new BigDecimal("20.00"));
        insumoBase.setActivo(true);

        requestBase = new InsumoRequest();
        requestBase.setCodigo("TEL-001");
        requestBase.setNombre("Tela algodón");
        requestBase.setCategoriaCodigo("TEL");
        requestBase.setUnidadMedida("m");
        requestBase.setStockDisponible(new BigDecimal("100.00"));
        requestBase.setStockMinimo(new BigDecimal("20.00"));
    }

    // ─── listarCategorias ────────────────────────────────────────────────────

    @Test
    @DisplayName("listarCategorias retorna lista ordenada por código")
    void listarCategorias_retornaListaOrdenada() {
        CategoriaInsumo c2 = new CategoriaInsumo();
        c2.setId(2L); c2.setCodigo("ACC"); c2.setNombre("Accesorios");

        when(categoriaInsumoRepository.findAll()).thenReturn(List.of(categoriaTela, c2));

        List<CategoriaInsumoResponse> result = service.listarCategorias();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCodigo()).isEqualTo("ACC"); // ACC < TEL
        assertThat(result.get(1).getCodigo()).isEqualTo("TEL");
    }

    // ─── listarInsumos ───────────────────────────────────────────────────────

    @Test
    @DisplayName("listarInsumos sin filtros retorna todos los activos")
    void listarInsumos_sinFiltros_retornaActivos() {
        when(insumoRepository.findByActivoTrueOrderByCategoria_CodigoAscNombreAsc())
                .thenReturn(List.of(insumoBase));

        List<InsumoResponse> result = service.listarInsumos(null, null, "nombre");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigo()).isEqualTo("TEL-001");
    }

    @Test
    @DisplayName("listarInsumos con categoría filtra por categoría")
    void listarInsumos_conCategoria_filtraPorCategoria() {
        when(insumoRepository.findByActivoTrueAndCategoria_CodigoOrderByNombreAsc("TEL"))
                .thenReturn(List.of(insumoBase));

        List<InsumoResponse> result = service.listarInsumos("TEL", null, "nombre");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoriaCodigo()).isEqualTo("TEL");
    }

    @Test
    @DisplayName("listarInsumos con criterio nombre aplica búsqueda por nombre")
    void listarInsumos_conCriterioNombre_buscaPorNombre() {
        when(insumoRepository.findByActivoTrueAndNombreContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc("algodón"))
                .thenReturn(List.of(insumoBase));

        List<InsumoResponse> result = service.listarInsumos(null, "algodón", "nombre");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).contains("algodón");
    }

    @Test
    @DisplayName("listarInsumos con criterio código aplica búsqueda por código")
    void listarInsumos_conCriterioCodigo_buscaPorCodigo() {
        when(insumoRepository.findByActivoTrueAndCodigoContainingIgnoreCaseOrderByCategoria_CodigoAscNombreAsc("TEL"))
                .thenReturn(List.of(insumoBase));

        List<InsumoResponse> result = service.listarInsumos(null, "TEL", "codigo");

        assertThat(result).hasSize(1);
    }

    // ─── obtener ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtener retorna respuesta cuando insumo existe")
    void obtener_insumoExiste_retornaResponse() {
        when(insumoRepository.findById(10L)).thenReturn(Optional.of(insumoBase));

        InsumoResponse result = service.obtener(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getNombre()).isEqualTo("Tela algodón");
    }

    @Test
    @DisplayName("obtener lanza excepción cuando insumo no existe")
    void obtener_insumoNoExiste_lanzaExcepcion() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    // ─── crear ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear guarda insumo y retorna response cuando código no existe")
    void crear_codigoNuevo_guardaYRetorna() {
        when(insumoRepository.existsByCodigoIgnoreCase("TEL-001")).thenReturn(false);
        when(categoriaInsumoRepository.findByCodigo("TEL")).thenReturn(Optional.of(categoriaTela));
        when(insumoRepository.save(any(Insumo.class))).thenReturn(insumoBase);

        InsumoResponse result = service.crear(requestBase);

        assertThat(result.getCodigo()).isEqualTo("TEL-001");
        verify(insumoRepository).save(any(Insumo.class));
    }

    @Test
    @DisplayName("crear lanza excepción cuando código ya existe")
    void crear_codigoDuplicado_lanzaExcepcion() {
        when(insumoRepository.existsByCodigoIgnoreCase("TEL-001")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código");
    }

    @Test
    @DisplayName("crear lanza excepción cuando categoría no existe")
    void crear_categoriaNvalida_lanzaExcepcion() {
        when(insumoRepository.existsByCodigoIgnoreCase("TEL-001")).thenReturn(false);
        when(categoriaInsumoRepository.findByCodigo("TEL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Categoría");
    }

    @Test
    @DisplayName("crear lanza excepción cuando stock disponible es negativo")
    void crear_stockNegativo_lanzaExcepcion() {
        requestBase.setStockDisponible(new BigDecimal("-5.00"));
        when(insumoRepository.existsByCodigoIgnoreCase("TEL-001")).thenReturn(false);
        when(categoriaInsumoRepository.findByCodigo("TEL")).thenReturn(Optional.of(categoriaTela));

        assertThatThrownBy(() -> service.crear(requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
    }

    // ─── actualizar ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar modifica y retorna insumo existente")
    void actualizar_insumoExiste_modificaYRetorna() {
        requestBase.setStockDisponible(new BigDecimal("150.00"));
        Insumo actualizado = new Insumo();
        actualizado.setId(10L);
        actualizado.setCodigo("TEL-001");
        actualizado.setNombre("Tela algodón");
        actualizado.setCategoria(categoriaTela);
        actualizado.setUnidadMedida("m");
        actualizado.setStockDisponible(new BigDecimal("150.00"));

        when(insumoRepository.findById(10L)).thenReturn(Optional.of(insumoBase));
        when(categoriaInsumoRepository.findByCodigo("TEL")).thenReturn(Optional.of(categoriaTela));
        when(insumoRepository.save(any(Insumo.class))).thenReturn(actualizado);

        InsumoResponse result = service.actualizar(10L, requestBase);

        assertThat(result.getStockDisponible()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("actualizar lanza excepción si se intenta cambiar el código")
    void actualizar_cambioCodigo_lanzaExcepcion() {
        requestBase.setCodigo("TEL-999");
        when(insumoRepository.findById(10L)).thenReturn(Optional.of(insumoBase));

        assertThatThrownBy(() -> service.actualizar(10L, requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código");
    }

    // ─── eliminar (soft-delete) ──────────────────────────────────────────────

    @Test
    @DisplayName("eliminar aplica soft-delete marcando activo=false")
    void eliminar_insumoExiste_desactiva() {
        when(insumoRepository.findById(10L)).thenReturn(Optional.of(insumoBase));

        service.eliminar(10L);

        assertThat(insumoBase.isActivo()).isFalse();
        verify(insumoRepository).save(insumoBase);
    }

    @Test
    @DisplayName("eliminar lanza excepción cuando insumo no existe")
    void eliminar_insumoNoExiste_lanzaExcepcion() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    // ─── contarBajoMinimo ────────────────────────────────────────────────────

    @Test
    @DisplayName("contarBajoMinimo retorna insumos cuyo stock está en o bajo el mínimo")
    void contarBajoMinimo_retornaCuentaCorrecta() {
        Insumo bajoPorMinimo = new Insumo();
        bajoPorMinimo.setId(2L);
        bajoPorMinimo.setCodigo("TEL-002");
        bajoPorMinimo.setNombre("Entretela");
        bajoPorMinimo.setCategoria(categoriaTela);
        bajoPorMinimo.setUnidadMedida("m");
        bajoPorMinimo.setStockDisponible(new BigDecimal("5.00"));
        bajoPorMinimo.setStockMinimo(new BigDecimal("20.00")); // stock < mínimo
        bajoPorMinimo.setActivo(true);

        when(insumoRepository.findByActivoTrueOrderByCategoria_CodigoAscNombreAsc())
                .thenReturn(List.of(insumoBase, bajoPorMinimo));

        long resultado = service.contarBajoMinimo();

        assertThat(resultado).isEqualTo(1L);
    }
}
