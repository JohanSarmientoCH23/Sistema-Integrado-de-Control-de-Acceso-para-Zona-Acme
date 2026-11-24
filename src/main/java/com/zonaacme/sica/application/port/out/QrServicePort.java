package com.zonaacme.sica.application.port.out;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Puerto de salida para generación y lectura de códigos QR.
 */
public interface QrServicePort {
    BufferedImage generarCodigoQr(String contenido, int ancho, int alto);
    byte[] generarCodigoQrPng(String contenido, int ancho, int alto);
    Optional<String> decodificarQr(BufferedImage imagen);
}
