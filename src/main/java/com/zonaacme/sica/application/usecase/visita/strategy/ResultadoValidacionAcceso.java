package com.zonaacme.sica.application.usecase.visita.strategy;

import com.zonaacme.sica.domain.entity.PerfilCompleto;

/**
 * Resultado inmutable devuelto por una estrategia de validación de acceso.
 */
public record ResultadoValidacionAcceso(
        boolean autorizado,
        String mensaje,
        Integer personaId,
        PerfilCompleto perfilCompleto,
        Integer visitaAsociadaId
) {
}
