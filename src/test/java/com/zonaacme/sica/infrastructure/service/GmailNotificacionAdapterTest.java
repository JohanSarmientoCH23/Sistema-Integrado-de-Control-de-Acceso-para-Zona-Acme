package com.zonaacme.sica.infrastructure.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GmailNotificacionAdapterTest {

    @Test
    void debeManejarDeFormaSeguraSinExcepcionesCuandoCredencialesSmtpEstanVacias() {
        GmailNotificacionAdapter adapterSinConfigurar = new GmailNotificacionAdapter("smtp.gmail.com", 587, "", "");

        assertFalse(adapterSinConfigurar.estaConfigurado(), "estaConfigurado() debe retornar false cuando user/password están vacíos");

        boolean resultadoToken = assertDoesNotThrow(() ->
                adapterSinConfigurar.enviarTokenAcceso(
                        "visitante@empresa.com",
                        "Daniela Vargas",
                        "159263",
                        "2026-12-31 23:59:59",
                        new byte[]{1, 2, 3}
                )
        );
        assertFalse(resultadoToken, "enviarTokenAcceso debe retornar false limpiamente cuando no hay credenciales");

        boolean resultadoAlerta = assertDoesNotThrow(() ->
                adapterSinConfigurar.enviarAlertaIncidente(
                        "seguridad@zonaacme.com",
                        "Intento de acceso prohibido",
                        "Se detectó intento de ingreso en portería principal."
                )
        );
        assertFalse(resultadoAlerta, "enviarAlertaIncidente debe retornar false limpiamente cuando no hay credenciales");
    }
}
