package com.unisof.insumos.service;

import com.unisof.insumos.dto.ProveedorRequest;
import com.unisof.insumos.dto.ProveedorResponse;
import com.unisof.insumos.model.CategoriaInsumo;
import com.unisof.insumos.model.Proveedor;
import com.unisof.insumos.repository.CategoriaInsumoRepository;
import com.unisof.insumos.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link ProveedorService} — Proceso 3.
 * Cubre CRUD de proveedores: validación de NIT/correo únicos,
 * asignación de categorías y eliminación.
 */
@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ProveedorService service;

    private CategoriaInsumo categoriaTelas;
    private Proveedor proveedorBase;
    private ProveedorRequest requestBase;

    @BeforeEach
    void setUp() {
        categoriaTelas = new CategoriaInsumo();
        categoriaTelas.setId(1L);
        categoriaTelas.setCodigo("TEL");
        categoriaTelas.setNombre("Telas");

        proveedorBase = new Proveedor();
        proveedorBase.setId(5L);
        proveedorBase.setNombre("Textiles S.A.");
        proveedorBase.setNit("900123456-7");
        proveedorBase.setCorreo("contacto@textiles.com");
        proveedorBase.setActivo(true);
        proveedorBase.setCategorias(new HashSet<>(Set.of(categoriaTelas)));

        requestBase = new ProveedorRequest();
        requestBase.setNombre("Textiles S.A.");
        requestBase.setNit("900123456-7");
        requestBase.setCorreo("contacto@textiles.com");
        requestBase.setCategoriaCodigos(List.of("TEL"));
        requestBase.setActivo(true);
    }

    // ─── listar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listar sin filtros retorna todos los proveedores")
    void listar_sinFiltros_retornaTodos() {
        when(proveedorRepository.findAllWithCategoriasOrderByNombreAsc())
                .thenReturn(List.of(proveedorBase));

        List<ProveedorResponse> result = service.listar(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Textiles S.A.");
    }

    @Test
    @DisplayName("listar por nombre aplica búsqueda por criterio")
    void listar_conCriterio_buscaPorNombre() {
        when(proveedorRepository.findByNombreContainingIgnoreCaseWithCategorias("textiles"))
                .thenReturn(List.of(proveedorBase));

        List<ProveedorResponse> result = service.listar("textiles", null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listar por NIT aplica búsqueda por NIT")
    void listar_conNit_buscaPorNit() {
        when(proveedorRepository.findByNitContainingIgnoreCaseWithCategorias("900123456"))
                .thenReturn(List.of(proveedorBase));

        List<ProveedorResponse> result = service.listar(null, null, "900123456");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNit()).isEqualTo("900123456-7");
    }

    // ─── obtener ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtener retorna proveedor cuando existe")
    void obtener_proveedorExiste_retornaResponse() {
        when(proveedorRepository.findByIdWithCategorias(5L)).thenReturn(Optional.of(proveedorBase));

        ProveedorResponse result = service.obtener(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getNombre()).isEqualTo("Textiles S.A.");
    }

    @Test
    @DisplayName("obtener lanza excepción cuando proveedor no existe")
    void obtener_noExiste_lanzaExcepcion() {
        when(proveedorRepository.findByIdWithCategorias(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    // ─── crear ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear guarda proveedor cuando NIT y correo son únicos")
    void crear_datosUnicos_guardaYRetorna() {
        when(proveedorRepository.findByNit("900123456-7")).thenReturn(Optional.empty());
        when(proveedorRepository.findByCorreoIgnoreCase("contacto@textiles.com")).thenReturn(Optional.empty());
        when(categoriaInsumoRepository.findByCodigo("TEL")).thenReturn(Optional.of(categoriaTelas));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorBase);
        when(emailService.enviarNotificacionRegistroProveedor(any(), any(), any())).thenReturn(true);

        ProveedorResponse result = service.crear(requestBase);

        assertThat(result.getNombre()).isEqualTo("Textiles S.A.");
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("crear lanza excepción cuando NIT ya existe")
    void crear_nitDuplicado_lanzaExcepcion() {
        when(proveedorRepository.findByNit("900123456-7")).thenReturn(Optional.of(proveedorBase));

        assertThatThrownBy(() -> service.crear(requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NIT");
    }

    @Test
    @DisplayName("crear lanza excepción cuando correo tiene formato inválido")
    void crear_correoInvalido_lanzaExcepcion() {
        requestBase.setNit(null); // sin NIT para pasar esa validación
        requestBase.setCorreo("correo-invalido");

        assertThatThrownBy(() -> service.crear(requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("crear lanza excepción cuando correo ya está registrado")
    void crear_correoDuplicado_lanzaExcepcion() {
        when(proveedorRepository.findByNit(any())).thenReturn(Optional.empty());
        when(proveedorRepository.findByCorreoIgnoreCase("contacto@textiles.com"))
                .thenReturn(Optional.of(proveedorBase));

        assertThatThrownBy(() -> service.crear(requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("crear lanza excepción cuando no hay categorías asignadas")
    void crear_sinCategorias_lanzaExcepcion() {
        requestBase.setCategoriaCodigos(List.of());
        when(proveedorRepository.findByNit(any())).thenReturn(Optional.empty());
        when(proveedorRepository.findByCorreoIgnoreCase(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoría");
    }

    // ─── actualizar ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar modifica datos del proveedor existente")
    void actualizar_proveedorExiste_actualizaDatos() {
        requestBase.setNombre("Textiles Premium S.A.");
        Proveedor actualizado = new Proveedor();
        actualizado.setId(5L);
        actualizado.setNombre("Textiles Premium S.A.");
        actualizado.setNit("900123456-7");
        actualizado.setCorreo("contacto@textiles.com");
        actualizado.setCategorias(new HashSet<>(Set.of(categoriaTelas)));

        when(proveedorRepository.findById(5L)).thenReturn(Optional.of(proveedorBase));
        when(proveedorRepository.findByNit("900123456-7")).thenReturn(Optional.of(proveedorBase));
        when(proveedorRepository.findByCorreoIgnoreCase("contacto@textiles.com")).thenReturn(Optional.of(proveedorBase));
        when(categoriaInsumoRepository.findByCodigo("TEL")).thenReturn(Optional.of(categoriaTelas));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(actualizado);

        ProveedorResponse result = service.actualizar(5L, requestBase);

        assertThat(result.getNombre()).isEqualTo("Textiles Premium S.A.");
    }

    @Test
    @DisplayName("actualizar lanza excepción cuando proveedor no existe")
    void actualizar_noExiste_lanzaExcepcion() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, requestBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    // ─── eliminar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar borra proveedor cuando existe")
    void eliminar_proveedorExiste_elimina() {
        when(proveedorRepository.existsById(5L)).thenReturn(true);

        service.eliminar(5L);

        verify(proveedorRepository).deleteById(5L);
    }

    @Test
    @DisplayName("eliminar lanza excepción cuando proveedor no existe")
    void eliminar_noExiste_lanzaExcepcion() {
        when(proveedorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }
}
