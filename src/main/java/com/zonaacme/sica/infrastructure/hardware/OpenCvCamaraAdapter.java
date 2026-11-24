package com.zonaacme.sica.infrastructure.hardware;

import com.zonaacme.sica.application.port.out.CamaraPort;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adaptador de cámara física utilizando OpenCV autocontenido (org.openpnp:opencv).
 * Protege el acceso concurrente y maneja de forma limpia la ausencia de hardware de video.
 */
public class OpenCvCamaraAdapter implements CamaraPort {

    private static final Logger LOG = LoggerFactory.getLogger(OpenCvCamaraAdapter.class);
    private static final AtomicBoolean OPENCV_CARGADO = new AtomicBoolean(false);

    private VideoCapture videoCapture;
    private volatile boolean activa = false;

    public OpenCvCamaraAdapter() {
        cargarLibreriaNativaSegura();
    }

    private static synchronized void cargarLibreriaNativaSegura() {
        if (OPENCV_CARGADO.get()) {
            return;
        }
        try {
            OpenCV.loadLocally();
            OPENCV_CARGADO.set(true);
        } catch (Throwable t) {
            LOG.warn("No se pudo cargar la librería nativa de OpenCV en este entorno: {}", t.getMessage());
        }
    }

    @Override
    public synchronized boolean iniciar(int indiceCamara) {
        detener();
        if (!OPENCV_CARGADO.get()) {
            LOG.warn("OpenCV no está disponible; no es posible iniciar la cámara {}.", indiceCamara);
            return false;
        }
        try {
            VideoCapture capture = new VideoCapture(indiceCamara);
            if (capture.isOpened()) {
                this.videoCapture = capture;
                this.activa = true;
                LOG.info("Cámara física iniciada correctamente en el índice {}.", indiceCamara);
                return true;
            } else {
                capture.release();
                LOG.warn("No se detectó cámara física disponible en el índice {}.", indiceCamara);
                return false;
            }
        } catch (Throwable t) {
            LOG.warn("Fallo controlado al intentar abrir la cámara {}: {}", indiceCamara, t.getMessage());
            this.activa = false;
            return false;
        }
    }

    @Override
    public synchronized Optional<BufferedImage> capturarFrame() {
        if (!activa || videoCapture == null || !videoCapture.isOpened()) {
            return Optional.empty();
        }
        Mat frame = new Mat();
        try {
            if (videoCapture.read(frame) && !frame.empty()) {
                return Optional.ofNullable(convertirMatABufferedImage(frame));
            }
            return Optional.empty();
        } catch (Throwable t) {
            LOG.warn("Error controlado al capturar frame de la cámara: {}", t.getMessage());
            return Optional.empty();
        } finally {
            try {
                frame.release();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public synchronized boolean estaActiva() {
        return activa && videoCapture != null && videoCapture.isOpened();
    }

    @Override
    public synchronized void detener() {
        activa = false;
        if (videoCapture != null) {
            try {
                if (videoCapture.isOpened()) {
                    videoCapture.release();
                }
            } catch (Throwable t) {
                LOG.warn("Aviso al liberar recursos de VideoCapture: {}", t.getMessage());
            } finally {
                videoCapture = null;
            }
        }
    }

    private BufferedImage convertirMatABufferedImage(Mat mat) {
        int channels = mat.channels();
        int type = channels > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        int bufferSize = channels * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}
