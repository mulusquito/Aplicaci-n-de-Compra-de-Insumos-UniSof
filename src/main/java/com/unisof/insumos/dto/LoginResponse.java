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
