package com.zonaacme.sica.domain.entity;

import java.time.LocalDateTime;

/**
 * Record de Java 17 que mapea la vista SQL vw_visitas_pendientes.
 */
public record VisitaPendiente(
        int visitaId,
        String visitante,
        String documentoIdentidad,
        String empresaDestino,
        LocalDateTime horaSolicitud
) {
}
