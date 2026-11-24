package com.zonaacme.sica.domain.entity;

/**
 * Record de Java 17 que mapea la vista SQL vw_perfiles_completos.
 */
public record PerfilCompleto(
        int personaId,
        String nombrePersona,
        String documentoIdentidad,
        String urlFoto,
        String vectorBiometrico,
        int empresaId,
        String nombreEmpresa,
        String tipoPersona,
        String estadoAcceso
) {
}
