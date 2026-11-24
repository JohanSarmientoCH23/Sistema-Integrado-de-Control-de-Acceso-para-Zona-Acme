package com.zonaacme.sica.application.port.out;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Puerto de salida para captura de video desde cámara física o integrada.
 */
public interface CamaraPort {
    boolean iniciar(int indiceCamara);
    Optional<BufferedImage> capturarFrame();
    boolean estaActiva();
    void detener();
}
