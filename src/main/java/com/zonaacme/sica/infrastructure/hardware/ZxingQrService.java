package com.zonaacme.sica.infrastructure.hardware;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.zonaacme.sica.application.port.out.QrServicePort;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador de códigos QR basado en Google ZXing.
 */
public class ZxingQrService implements QrServicePort {

    @Override
    public BufferedImage generarCodigoQr(String contenido, int ancho, int alto) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El contenido del código QR no puede estar vacío.");
        }
        int width = Math.max(64, ancho);
        int height = Math.max(64, alto);

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(contenido.trim(), BarcodeFormat.QR_CODE, width, height, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new IllegalStateException("Error al generar el código QR para el contenido proporcionado.", e);
        }
    }

    @Override
    public byte[] generarCodigoQrPng(String contenido, int ancho, int alto) {
        BufferedImage imagen = generarCodigoQr(contenido, ancho, alto);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error al serializar el código QR a formato PNG.", e);
        }
    }

    @Override
    public Optional<String> decodificarQr(BufferedImage imagen) {
        if (imagen == null) {
            return Optional.empty();
        }
        try {
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(imagen);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128));

            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap, hints);
            if (result != null && result.getText() != null && !result.getText().isBlank()) {
                return Optional.of(result.getText().trim());
            }
            return Optional.empty();
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
