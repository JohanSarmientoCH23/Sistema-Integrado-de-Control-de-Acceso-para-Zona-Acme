package com.zonaacme.sica.application.usecase.autorizacion;

import com.zonaacme.sica.domain.entity.Usuario;

import java.util.Optional;

/**
 * Gestor de sesión de usuario en memoria para la aplicación de escritorio.
 */
public final class SesionUsuario {

    private static final SesionUsuario INSTANCE = new SesionUsuario();
    private volatile Usuario usuarioActual;

    private SesionUsuario() {
    }

    public static SesionUsuario getInstance() {
        return INSTANCE;
    }

    public void iniciarSesion(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario para iniciar sesión no puede ser nulo.");
        }
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public Optional<Usuario> obtenerUsuarioActual() {
        return Optional.ofNullable(usuarioActual);
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null && usuarioActual.isEstaActivo();
    }
}
