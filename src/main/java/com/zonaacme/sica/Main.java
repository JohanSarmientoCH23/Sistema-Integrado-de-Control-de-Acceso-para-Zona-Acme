package com.zonaacme.sica;

import com.zonaacme.sica.config.AppConfig;

public class Main {
    public static void main(String[] args) {
        AppConfig config = AppConfig.getInstance();
        System.out.println("SICA - Sistema Integrado de Control de Acceso para Zona Acme");
        System.out.println("Intervalo de polling configurado: " + config.getPollingIntervalSeconds() + "s");
    }
}
