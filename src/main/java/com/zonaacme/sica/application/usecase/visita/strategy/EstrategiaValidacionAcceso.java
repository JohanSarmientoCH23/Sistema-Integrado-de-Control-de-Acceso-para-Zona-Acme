package com.zonaacme.sica.application.usecase.visita.strategy;

/**
 * Patrón de Diseño #2 (Strategy): Contrato para las estrategias de validación de acceso a Zona Acme.
 */
public interface EstrategiaValidacionAcceso {
    ResultadoValidacionAcceso validar(SolicitudAcceso solicitud);
}
