package com.zonaacme.sica.infrastructure.hardware;

import com.zonaacme.sica.application.port.out.CamaraPort;
import com.zonaacme.sica.application.port.out.QrServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Servicio que coordina CamaraPort y QrServicePort en un hilo en segundo plano
 * con callbacks para el frame de video y el código QR detectado, aplicando un cooldown
 * de 3 segundos para evitar lecturas repetidas del mismo código QR.
 */
public class EscaneoQrContinuoService {

    private static final Logger LOG = LoggerFactory.getLogger(EscaneoQrContinuoService.class);
    public static final long DEFAULT_COOLDOWN_MS = 3000L;
    private static final long FRAME_INTERVAL_MS = 100L;

    private final CamaraPort camaraPort;
    private final QrServicePort qrServicePort;
    private final long cooldownMs;
    private final AtomicBoolean ejecutando = new AtomicBoolean(false);

    private ScheduledExecutorService executor;
    private volatile String ultimoQrLeido = null;
    private volatile long timestampUltimaLectura = 0L;

    public EscaneoQrContinuoService(CamaraPort camaraPort, QrServicePort qrServicePort) {
        this(camaraPort, qrServicePort, DEFAULT_COOLDOWN_MS);
    }

    public EscaneoQrContinuoService(CamaraPort camaraPort, QrServicePort qrServicePort, long cooldownMs) {
        this.camaraPort = Objects.requireNonNull(camaraPort, "camaraPort no puede ser nulo");
        this.qrServicePort = Objects.requireNonNull(qrServicePort, "qrServicePort no puede ser nulo");
        this.cooldownMs = Math.max(500L, cooldownMs);
    }

    public synchronized boolean iniciar(int indiceCamara,
                                        Consumer<BufferedImage> onFrameCapturado,
                                        Consumer<String> onQrDetectado) {
        if (ejecutando.get()) {
            return true;
        }
        if (!camaraPort.iniciar(indiceCamara)) {
            LOG.warn("No se pudo iniciar el escaneo continuo de QR: cámara {} no disponible.", indiceCamara);
            return false;
        }

        ejecutando.set(true);
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SICA-QrScanner-Daemon");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleWithFixedDelay(
                () -> procesarCicloCaptura(onFrameCapturado, onQrDetectado),
                0,
                FRAME_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
        return true;
    }

    public void procesarCicloCaptura(Consumer<BufferedImage> onFrameCapturado, Consumer<String> onQrDetectado) {
        try {
            Optional<BufferedImage> optFrame = camaraPort.capturarFrame();
            if (optFrame.isEmpty()) {
                return;
            }
            BufferedImage frame = optFrame.get();
            if (onFrameCapturado != null) {
                onFrameCapturado.accept(frame);
            }

            Optional<String> optQr = qrServicePort.decodificarQr(frame);
            if (optQr.isPresent() && onQrDetectado != null) {
                String codigo = optQr.get();
                long ahora = System.currentTimeMillis();
                if (!codigo.equals(ultimoQrLeido) || (ahora - timestampUltimaLectura) >= cooldownMs) {
                    ultimoQrLeido = codigo;
                    timestampUltimaLectura = ahora;
                    onQrDetectado.accept(codigo);
                }
            }
        } catch (Exception e) {
            LOG.warn("Error controlado durante ciclo de escaneo QR: {}", e.getMessage());
        }
    }

    public boolean estaEjecutando() {
        return ejecutando.get();
    }

    public synchronized void detener() {
        ejecutando.set(false);
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        camaraPort.detener();
    }
}
