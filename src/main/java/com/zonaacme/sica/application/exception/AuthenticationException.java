package com.zonaacme.sica.application.exception;

/**
 * Excepción de aplicación para credenciales inválidas o intento de ingreso de un usuario inactivo.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
