package com.zonaacme.sica.application.exception;

/**
 * Excepción de aplicación cuando un usuario intenta ejecutar una acción sin contar con el permiso RBAC requerido.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
