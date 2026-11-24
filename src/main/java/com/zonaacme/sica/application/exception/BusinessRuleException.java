package com.zonaacme.sica.application.exception;

/**
 * Excepción de aplicación cuando se infringe una regla de negocio de SICA
 * (por ejemplo: acceso prohibido, anti-passback por visita ya abierta, token inválido o expirado).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
