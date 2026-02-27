package com.unisof.insumos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Clase principal de la aplicacion de Compra de Insumos UniSof.
 * <p>
 * Spring Boot 3.x con autenticacion (SCRUM-7) y verificacion 2FA por correo (SCRUM-35).
 * </p>
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
public class InsumosApplication {

    /**
     * Punto de entrada de la aplicacion.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(InsumosApplication.class, args);
    }

}
