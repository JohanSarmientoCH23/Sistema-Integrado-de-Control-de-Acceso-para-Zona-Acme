package com.zonaacme.sica.application.usecase.autorizacion;

import com.zonaacme.sica.application.exception.AccessDeniedException;
import com.zonaacme.sica.domain.entity.Permiso;
import com.zonaacme.sica.domain.entity.Rol;
import com.zonaacme.sica.domain.entity.Usuario;

import java.util.Collection;
import java.util.Objects;

/**
 * Servicio de autorización RBAC basado en Java Stream API (stream, flatMap, anyMatch).
 */
public class AutorizacionService {

    public boolean tieneRol(Usuario usuario, String nombreRol) {
        if (usuario == null || !usuario.isEstaActivo() || nombreRol == null || nombreRol.isBlank()) {
            return false;
        }
        return usuario.getRoles().stream()
                .filter(Objects::nonNull)
                .map(Rol::getNombreRol)
                .filter(Objects::nonNull)
                .anyMatch(rol -> rol.equalsIgnoreCase(nombreRol.trim()));
    }

    public boolean tienePermiso(Usuario usuario, String nombrePermiso) {
        if (usuario == null || !usuario.isEstaActivo() || nombrePermiso == null || nombrePermiso.isBlank()) {
            return false;
        }
        return usuario.getRoles().stream()
                .filter(Objects::nonNull)
                .map(Rol::getPermisos)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(Permiso::getNombrePermiso)
                .filter(Objects::nonNull)
                .anyMatch(permiso -> permiso.equalsIgnoreCase(nombrePermiso.trim()));
    }

    public void validarPermiso(Usuario usuario, String nombrePermiso) {
        if (usuario == null) {
            throw new AccessDeniedException("No hay un usuario autenticado para realizar la acción: " + nombrePermiso);
        }
        if (!usuario.isEstaActivo()) {
            throw new AccessDeniedException("El usuario '" + usuario.getEmail() + "' está inactivo.");
        }
        if (!tienePermiso(usuario, nombrePermiso)) {
            throw new AccessDeniedException("Acceso denegado: el usuario '" + usuario.getEmail()
                    + "' no cuenta con el permiso requerido '" + nombrePermiso + "'.");
        }
    }
}
