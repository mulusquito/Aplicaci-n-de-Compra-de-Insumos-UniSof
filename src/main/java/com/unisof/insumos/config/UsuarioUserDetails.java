package com.unisof.insumos.config;

import com.unisof.insumos.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Adaptador de {@link Usuario} a {@link UserDetails} para Spring Security.
 * SCRUM-36: Usado al establecer la sesion tras verificar token 2FA.
 */
@RequiredArgsConstructor
public class UsuarioUserDetails implements UserDetails {

    private final Usuario usuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rol = usuario.getRol() != null ? usuario.getRol() : "WORKER";
        return Stream.of(new SimpleGrantedAuthority("ROLE_" + rol)).toList();
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena();
    }

    @Override
    public String getUsername() {
        return usuario.getUsuario();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
