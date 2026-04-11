package com.unisof.insumos.controller;

import com.unisof.insumos.dto.CheckoutRequest;
import com.unisof.insumos.service.AuditoriaService;
import com.unisof.insumos.service.MercadoPagoService;
import com.unisof.insumos.service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para CheckoutController (pagos).
 */
@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private MercadoPagoService mercadoPagoService;

    @Mock
    private StripeService stripeService;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private CheckoutController controller;

    private CheckoutRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "paymentProvider", "mercadopago");

        CheckoutRequest.ClienteData cliente = new CheckoutRequest.ClienteData(
                "Juan", "juan@test.com", "300", "Calle 1", "CC", "123");
        CheckoutRequest.CheckoutItem item = new CheckoutRequest.CheckoutItem(
                "p1", "Producto", 1, 100000.0, "COP");
        request = new CheckoutRequest(List.of(item), cliente);
    }

    @Test
    @DisplayName("Crear preferencia con MercadoPago retorna URL de checkout (init_point)")
    void createPreference_mercadopago_retornaUrl() {
        when(mercadoPagoService.createPreference(any(CheckoutRequest.class)))
                .thenReturn("https://www.mercadopago.com/checkout/abc123");

        ResponseEntity<?> response = controller.createPreference(request, mockRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("init_point"))
                .isEqualTo("https://www.mercadopago.com/checkout/abc123");
    }

    @Test
    @DisplayName("Crear preferencia con Stripe retorna URL de sesión de checkout")
    void createPreference_stripe_retornaUrl() {
        ReflectionTestUtils.setField(controller, "paymentProvider", "stripe");
        when(stripeService.createCheckoutSession(any(CheckoutRequest.class)))
                .thenReturn("https://checkout.stripe.com/session/xyz");

        ResponseEntity<?> response = controller.createPreference(request, mockRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(((Map<?, ?>) response.getBody()).get("init_point"))
                .isEqualTo("https://checkout.stripe.com/session/xyz");
    }

    @Test
    @DisplayName("Crear preferencia con error de configuración retorna Bad Request")
    void createPreference_error_retornaBadRequest() {
        when(mercadoPagoService.createPreference(any(CheckoutRequest.class)))
                .thenThrow(new IllegalStateException("Error de configuración"));

        ResponseEntity<?> response = controller.createPreference(request, mockRequest);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("mensaje")).toString()
                .contains("Error de configuración");
    }
}
