package com.unisof.insumos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private boolean valido;
    private String mensaje;
    private UsuarioResponse usuario;
    /** SCRUM-35: true cuando se requiere verificacion en dos pasos */
    private boolean requiereVerificacionDosPasos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioResponse {
        private Long id;
        private String usuario;
        private String nombre;
        private String correo;
        private String rol;
    }
}
