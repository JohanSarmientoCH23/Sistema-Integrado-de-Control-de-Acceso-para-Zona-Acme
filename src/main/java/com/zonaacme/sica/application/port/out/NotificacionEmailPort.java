package com.zonaacme.sica.application.port.out;

/**
 * Puerto de salida para envío de notificaciones por correo electrónico (tokens QR de acceso y alertas).
 */
public interface NotificacionEmailPort {
    boolean enviarTokenAcceso(String destinatario, String nombreVisitante, String codigoToken, String fechaExpiracion, byte[] qrPngAdjunto);
    boolean enviarAlertaIncidente(String destinatario, String asunto, String detalle);
    boolean estaConfigurado();
}
