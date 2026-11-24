package com.zonaacme.sica.infrastructure.hardware;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ZxingQrServiceTest {

    private final ZxingQrService qrService = new ZxingQrService();

    @Test
    void debeGenerarQrConToken159263YDecodificarloExactamente() {
        String tokenEsperado = "159263";

        BufferedImage qrImage = qrService.generarCodigoQr(tokenEsperado, 250, 250);
        assertNotNull(qrImage);
        assertEquals(250, qrImage.getWidth());
        assertEquals(250, qrImage.getHeight());

        Optional<String> decodificado = qrService.decodificarQr(qrImage);
        assertTrue(decodificado.isPresent(), "El código QR generado debe poder decodificarse");
        assertEquals(tokenEsperado, decodificado.get());
    }

    @Test
    void debeGenerarBytesPngValidosConCabeceraPng() {
        byte[] pngBytes = qrService.generarCodigoQrPng("QR-A1B2C3D4E5", 200, 200);
        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 100, "El archivo PNG en memoria debe tener tamaño válido");

        // Firma mágica PNG: 0x89 'P' 'N' 'G'
        assertEquals((byte) 0x89, pngBytes[0]);
        assertEquals((byte) 0x50, pngBytes[1]);
        assertEquals((byte) 0x4E, pngBytes[2]);
        assertEquals((byte) 0x47, pngBytes[3]);
    }
}
