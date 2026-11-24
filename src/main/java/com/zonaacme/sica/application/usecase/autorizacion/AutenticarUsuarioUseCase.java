package com.zonaacme.sica.application.usecase.autorizacion;

import com.zonaacme.sica.application.exception.AuthenticationException;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.Rol;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para autenticar usuarios del sistema, cargar sus roles/permisos
 * y auditar el evento LOGIN_EXITOSO en la bitácora.
 */
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final BitacoraRepository bitacoraRepository;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository, BitacoraRepository bitacoraRepository) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public Usuario autenticar(String email, String passwordInput) {
        if (email == null || email.isBlank() || passwordInput == null || passwordInput.isBlank()) {
            throw new AuthenticationException("Debe proporcionar correo electrónico y contraseña.");
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email.trim())
                .orElseThrow(() -> new AuthenticationException("Credenciales inválidas para el correo: " + email));

        if (!usuario.isEstaActivo()) {
            throw new AuthenticationException("La cuenta del usuario '" + email + "' se encuentra inactiva.");
        }

        if (!verificarPassword(passwordInput, usuario.getPassword())) {
            throw new AuthenticationException("Credenciales inválidas para el correo: " + email);
        }

        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            List<Rol> rolesCargados = usuarioRepository.obtenerRolesConPermisos(usuario.getId());
            usuario.setRoles(rolesCargados);
        }

        BitacoraAuditoria registroAuditoria = BitacoraAuditoria.builder()
                .usuarioId(usuario.getId())
                .accionRealizada("LOGIN_EXITOSO")
                .tablaAfectada("usuarios")
                .registroIdAfectado(usuario.getId())
                .detallesJson("{\"email\": \"" + usuario.getEmail() + "\"}")
                .build();
        bitacoraRepository.registrar(registroAuditoria);

        SesionUsuario.getInstance().iniciarSesion(usuario);
        return usuario;
    }

    private boolean verificarPassword(String passwordInput, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        byte[] inputBytes = passwordInput.getBytes(StandardCharsets.UTF_8);
        byte[] storedBytes = storedPassword.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(inputBytes, storedBytes);
    }
}
