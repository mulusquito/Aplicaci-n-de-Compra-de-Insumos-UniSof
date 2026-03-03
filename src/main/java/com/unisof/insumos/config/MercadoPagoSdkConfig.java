package com.unisof.insumos.config;

import com.mercadopago.MercadoPagoConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configura el SDK oficial de Mercado Pago con el access token.
 * El token se lee de mercadopago.access-token o MERCADOPAGO_ACCESS_TOKEN.
 */
@Configuration
@Slf4j
public class MercadoPagoSdkConfig {

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @PostConstruct
    public void init() {
        if (accessToken != null && !accessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(accessToken.trim());
            log.info("Mercado Pago SDK configurado correctamente");
        }
    }
}
