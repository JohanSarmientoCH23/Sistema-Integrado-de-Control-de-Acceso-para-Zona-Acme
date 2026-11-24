package com.zonaacme.sica.infrastructure.database;

/**
 * Excepción runtime de infraestructura para encapsular errores de acceso a datos (SQLException)
 * manteniendo los puertos del dominio limpios de dependencias de JDBC.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
