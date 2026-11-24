package com.zonaacme.sica.application.usecase.visita.strategy;

/**
 * Datos de entrada para evaluar una solicitud de acceso al complejo Zona Acme.
 *
 * @param documentoIdentidad  Documento de identidad para validación manual o referencia
 * @param codigoToken         Código de token (PIN_Numerico, Codigo_QR, Codigo_Barras)
 * @param vectorBiometricoJson Vector facial en formato JSON (p. ej. "[-0.1, 0.5, 0.9, 0.2]")
 * @param tipoMetodo          Nombre o identificador del método de acceso (Manual_Documento, Biometrico_Camara, Escaneo_QR, PIN_Digitado, Tarjeta_RFID)
 */
public record SolicitudAcceso(
        String documentoIdentidad,
        String codigoToken,
        String vectorBiometricoJson,
        String tipoMetodo
) {
}
