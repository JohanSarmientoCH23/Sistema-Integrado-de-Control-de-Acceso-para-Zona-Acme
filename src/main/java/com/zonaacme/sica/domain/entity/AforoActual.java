package com.zonaacme.sica.domain.entity;

import java.time.LocalDateTime;

/**
 * Record de Java 17 que mapea la vista SQL vw_aforo_actual.
 */
public record AforoActual(
        int visitaId,
        String nombrePersona,
        String documentoIdentidad,
        String empresa,
        String tipoPersona,
        LocalDateTime fechaEntrada,
        String metodoIngreso
) {
}
