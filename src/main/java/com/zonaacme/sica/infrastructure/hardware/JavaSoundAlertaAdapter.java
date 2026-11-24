package com.zonaacme.sica.infrastructure.hardware;

import com.zonaacme.sica.application.port.out.AlertaSonoraPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de alertas sonoras que sintetiza ondas acústicas en memoria usando javax.sound.sampled
 * sin requerir archivos .wav externos. Se ejecuta en un hilo asíncrono para no bloquear la UI.
 */
public class JavaSoundAlertaAdapter implements AlertaSonoraPort {

    private static final Logger LOG = LoggerFactory.getLogger(JavaSoundAlertaAdapter.class);
    private static final float SAMPLE_RATE = 44100f;

    @Override
    public void reproducir(TipoAlerta tipo) {
        if (tipo == null) {
            return;
        }
        CompletableFuture.runAsync(() -> sintetizarSecuenciaSegura(tipo));
    }

    public void sintetizarSecuenciaSegura(TipoAlerta tipo) {
        try {
            switch (tipo) {
                case ACCESO_CONCEDIDO -> {
                    emitirTono(880, 110, 0.55);
                    emitirTono(1175, 140, 0.55);
                }
                case ACCESO_DENEGADO -> {
                    emitirTono(280, 140, 0.65);
                    Thread.sleep(50);
                    emitirTono(220, 180, 0.65);
                }
                case PERSONA_PROHIBIDA -> {
                    for (int i = 0; i < 3; i++) {
                        emitirTono(1450, 120, 0.8);
                        Thread.sleep(45);
                    }
                }
                case EMERGENCIA_EVACUACION -> {
                    for (int i = 0; i < 2; i++) {
                        emitirTono(650, 150, 0.8);
                        emitirTono(980, 150, 0.8);
                    }
                }
            }
        } catch (Throwable t) {
            LOG.warn("Dispositivo de audio no disponible o interrumpido al reproducir alerta {}: {}", tipo, t.getMessage());
        }
    }

    private void emitirTono(int hz, int duracionMs, double volumen) throws Exception {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();

            int totalSamples = (int) ((duracionMs / 1000.0) * SAMPLE_RATE);
            byte[] buffer = new byte[totalSamples];
            for (int i = 0; i < totalSamples; i++) {
                double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
                buffer[i] = (byte) (Math.sin(angle) * 127.0 * volumen);
            }

            line.write(buffer, 0, buffer.length);
            line.drain();
            line.stop();
        }
    }
}
