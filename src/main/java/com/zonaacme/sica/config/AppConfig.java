package com.zonaacme.sica.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Configuración centralizada de SICA (Patrón Singleton mediante enum en Java 17).
 * Carga config.properties exclusivamente desde el classpath para soporte "clonar y ejecutar".
 */
public enum AppConfig {
    INSTANCE;

    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties = new Properties();

    AppConfig() {
        cargarDesdeClasspath();
    }

    private void cargarDesdeClasspath() {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("No se encontró el archivo '" + CONFIG_FILE + "' en el classpath.");
            }
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar '" + CONFIG_FILE + "' desde el classpath.", e);
        }
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getDbUrl() {
        return properties.getProperty("db.url", "").trim();
    }

    public String getDbUser() {
        return properties.getProperty("db.user", "").trim();
    }

    public String getDbPassword() {
        return properties.getProperty("db.password", "").trim();
    }

    public String getMailSmtpHost() {
        return properties.getProperty("mail.smtp.host", "smtp.gmail.com").trim();
    }

    public int getMailSmtpPort() {
        String raw = properties.getProperty("mail.smtp.port", "587").trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 587;
        }
    }

    public String getMailUser() {
        return properties.getProperty("mail.user", "").trim();
    }

    public String getMailPassword() {
        return properties.getProperty("mail.password", "").trim();
    }

    public int getPollingIntervalSeconds() {
        String raw = properties.getProperty("polling.interval.seconds", "3").trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 3;
        }
    }
}
