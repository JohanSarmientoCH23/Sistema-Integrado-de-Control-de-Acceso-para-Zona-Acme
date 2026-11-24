package com.zonaacme.sica.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void debeCargarPropiedadesDesdeClasspathCorrectamente() {
        AppConfig config = AppConfig.getInstance();
        assertNotNull(config);
        assertEquals(AppConfig.INSTANCE, config);

        assertTrue(config.getDbUrl().startsWith("jdbc:mysql://mysql-2bfd872c-sica-proyecto.g.aivencloud.com:13481/defaultdb"));
        assertEquals("avnadmin", config.getDbUser());
        assertFalse(config.getDbPassword().isBlank());
        assertEquals("smtp.gmail.com", config.getMailSmtpHost());
        assertEquals(587, config.getMailSmtpPort());
        assertEquals(3, config.getPollingIntervalSeconds());
    }
}
