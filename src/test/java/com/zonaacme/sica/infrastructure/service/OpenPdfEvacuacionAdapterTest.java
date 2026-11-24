package com.zonaacme.sica.infrastructure.service;

import com.zonaacme.sica.application.usecase.reporte.ResumenAforo;
import com.zonaacme.sica.domain.entity.AforoActual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenPdfEvacuacionAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void debeGenerarPdfDeEvacuacionValidoConPesoMayorA1KbYCabeceraPdf() throws IOException {
        OpenPdfEvacuacionAdapter adapter = new OpenPdfEvacuacionAdapter();

        LocalDateTime entrada = LocalDateTime.of(2026, 11, 24, 8, 30);
        List<AforoActual> personasEnSitio = List.of(
                new AforoActual(2, "Alberto Ruiz", "1098000222", "Avicola ERP Solutions", "Trabajador", entrada, "Biometrico_Camara"),
                new AforoActual(4, "Daniela Vargas", "1098000444", "Logistica Acme", "Invitado", entrada.plusMinutes(15), "PIN_Digitado")
        );

        ResumenAforo resumen = new ResumenAforo(
                2,
                Map.of("Avicola ERP Solutions", 1L, "Logistica Acme", 1L),
                Map.of("Trabajador", 1L, "Invitado", 1L),
                Map.of("Biometrico_Camara", 1L, "PIN_Digitado", 1L),
                1
        );

        Path destino = tempDir.resolve("reporte-evacuacion-sica.pdf");
        Path generado = adapter.generarReporteEvacuacion(personasEnSitio, resumen, destino);

        assertNotNull(generado);
        assertTrue(Files.exists(generado), "El archivo PDF debe existir en el directorio temporal");

        byte[] contenido = Files.readAllBytes(generado);
        assertTrue(contenido.length > 1024, "El PDF generado debe pesar más de 1 KB");

        String cabecera = new String(contenido, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", cabecera, "El archivo generado debe comenzar con la firma %PDF");
    }
}
