package com.zonaacme.sica.infrastructure.service;

import com.zonaacme.sica.application.port.out.NotificacionEmailPort;
import com.zonaacme.sica.config.AppConfig;
import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Adaptador de correo electrónico vía SMTP STARTTLS (Gmail puerto 587).
 * Si mail.user o mail.password están vacíos en config.properties, informa limpiamente y retorna false sin fallar.
 */
public class GmailNotificacionAdapter implements NotificacionEmailPort {

    private static final Logger LOG = LoggerFactory.getLogger(GmailNotificacionAdapter.class);

    private final String smtpHost;
    private final int smtpPort;
    private final String mailUser;
    private final String mailPassword;

    public GmailNotificacionAdapter() {
        this(
                AppConfig.getInstance().getMailSmtpHost(),
                AppConfig.getInstance().getMailSmtpPort(),
                AppConfig.getInstance().getMailUser(),
                AppConfig.getInstance().getMailPassword()
        );
    }

    public GmailNotificacionAdapter(String smtpHost, int smtpPort, String mailUser, String mailPassword) {
        this.smtpHost = smtpHost != null ? smtpHost.trim() : "smtp.gmail.com";
        this.smtpPort = smtpPort > 0 ? smtpPort : 587;
        this.mailUser = mailUser != null ? mailUser.trim() : "";
        this.mailPassword = mailPassword != null ? mailPassword.trim() : "";
    }

    @Override
    public boolean estaConfigurado() {
        return !mailUser.isBlank() && !mailPassword.isBlank();
    }

    @Override
    public boolean enviarTokenAcceso(String destinatario,
                                     String nombreVisitante,
                                     String codigoToken,
                                     String fechaExpiracion,
                                     byte[] qrPngAdjunto) {
        if (!estaConfigurado()) {
            LOG.warn("Servicio SMTP no configurado (mail.user o mail.password vacíos). Se omite envío de token a: {}", destinatario);
            return false;
        }
        if (destinatario == null || destinatario.isBlank()) {
            LOG.warn("El destinatario del correo está vacío; no se puede enviar el token de acceso.");
            return false;
        }

        String asunto = "SICA Zona Acme - Credencial de Acceso Temporal";
        String htmlCuerpo = construirPlantillaHtmlToken(nombreVisitante, codigoToken, fechaExpiracion);

        return enviarCorreoInterno(destinatario.trim(), asunto, htmlCuerpo, qrPngAdjunto);
    }

    @Override
    public boolean enviarAlertaIncidente(String destinatario, String asunto, String detalle) {
        if (!estaConfigurado()) {
            LOG.warn("Servicio SMTP no configurado (mail.user o mail.password vacíos). Se omite envío de alerta a: {}", destinatario);
            return false;
        }
        if (destinatario == null || destinatario.isBlank()) {
            LOG.warn("El destinatario de la alerta está vacío.");
            return false;
        }

        String asuntoFinal = (asunto != null && !asunto.isBlank())
                ? "SICA Alerta de Seguridad: " + asunto.trim()
                : "SICA Alerta de Seguridad - Zona Acme";
        String htmlCuerpo = construirPlantillaHtmlAlerta(asuntoFinal, detalle);

        return enviarCorreoInterno(destinatario.trim(), asuntoFinal, htmlCuerpo, null);
    }

    private boolean enviarCorreoInterno(String destinatario, String asunto, String htmlCuerpo, byte[] qrPngAdjunto) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.connectiontimeout", "8000");
            props.put("mail.smtp.timeout", "8000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUser, mailPassword);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUser));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto, StandardCharsets.UTF_8.name());

            Multipart multipart = new MimeMultipart();

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlCuerpo, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            if (qrPngAdjunto != null && qrPngAdjunto.length > 0) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                ByteArrayDataSource dataSource = new ByteArrayDataSource(qrPngAdjunto, "image/png");
                attachmentPart.setDataHandler(new DataHandler(dataSource));
                attachmentPart.setFileName("token-acceso-qr.png");
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            LOG.info("Correo enviado exitosamente a: {}", destinatario);
            return true;
        } catch (Exception e) {
            LOG.warn("No fue posible enviar el correo a {}: {}", destinatario, e.getMessage());
            return false;
        }
    }

    private String construirPlantillaHtmlToken(String nombreVisitante, String codigoToken, String fechaExpiracion) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; border: 1px solid #d4ded8; border-radius: 6px; overflow: hidden;">
                    <div style="background-color: #143d2b; color: #ffffff; padding: 18px 24px;">
                        <h2 style="margin: 0; font-size: 18px;">SICA &mdash; Complejo Empresarial Zona Acme</h2>
                    </div>
                    <div style="padding: 24px; background-color: #ffffff; color: #1f2924;">
                        <p>Estimado(a) <strong>%s</strong>,</p>
                        <p>Se ha generado su credencial temporal de ingreso al complejo Zona Acme:</p>
                        <div style="background-color: #f2f7f4; border-left: 4px solid #1b5e40; padding: 14px 18px; margin: 18px 0;">
                            <p style="margin: 4px 0; font-size: 16px;">C&oacute;digo de Acceso: <strong>%s</strong></p>
                            <p style="margin: 4px 0; font-size: 13px; color: #4b5e54;">V&aacute;lido hasta: %s</p>
                        </div>
                        <p style="font-size: 13px; color: #4b5e54;">Presente el c&oacute;digo QR adjunto o digite el c&oacute;digo en la porter&iacute;a principal.</p>
                    </div>
                </div>
                """.formatted(
                nombreVisitante != null ? nombreVisitante : "Visitante",
                codigoToken != null ? codigoToken : "",
                fechaExpiracion != null ? fechaExpiracion : ""
        );
    }

    private String construirPlantillaHtmlAlerta(String titulo, String detalle) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; border: 1px solid #d4ded8; border-radius: 6px; overflow: hidden;">
                    <div style="background-color: #143d2b; color: #ffffff; padding: 18px 24px;">
                        <h2 style="margin: 0; font-size: 18px;">%s</h2>
                    </div>
                    <div style="padding: 24px; background-color: #ffffff; color: #1f2924;">
                        <p>%s</p>
                    </div>
                </div>
                """.formatted(titulo, detalle != null ? detalle : "");
    }
}
