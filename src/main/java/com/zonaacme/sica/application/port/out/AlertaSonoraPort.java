package com.zonaacme.sica.application.port.out;

/**
 * Puerto de salida para reproducción de alertas sonoras operativas en portería.
 */
public interface AlertaSonoraPort {

    enum TipoAlerta {
        ACCESO_CONCEDIDO,
        ACCESO_DENEGADO,
        PERSONA_PROHIBIDA,
        EMERGENCIA_EVACUACION
    }

    void reproducir(TipoAlerta tipo);
}
