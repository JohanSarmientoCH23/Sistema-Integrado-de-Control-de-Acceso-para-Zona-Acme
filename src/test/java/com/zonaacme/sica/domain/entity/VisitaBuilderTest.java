package com.zonaacme.sica.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitaBuilderTest {

    @Test
    void debeConstruirVisitaValidaConBuilder() {
        LocalDateTime entrada = LocalDateTime.of(2026, 11, 19, 8, 0);
        LocalDateTime salida = LocalDateTime.of(2026, 11, 19, 12, 0);

        Visita visita = Visita.builder()
                .id(1)
                .personaId(10)
                .fechaEntrada(entrada)
                .fechaSalida(salida)
                .estadoVisitaId(4)
                .estadoVisitaNombre("Dentro")
                .visitaAprobadaPor(3)
                .metodoIngresoId(2)
                .metodoSalidaId(2)
                .agregarVehiculoPlaca("AAA-111")
                .build();

        assertEquals(1, visita.getId());
        assertEquals(10, visita.getPersonaId());
        assertEquals(entrada, visita.getFechaEntrada());
        assertEquals(salida, visita.getFechaSalida());
        assertEquals(4, visita.getEstadoVisitaId());
        assertEquals("Dentro", visita.getEstadoVisitaNombre());
        assertEquals(3, visita.getVisitaAprobadaPor());
        assertEquals(2, visita.getMetodoIngresoId());
        assertEquals(2, visita.getMetodoSalidaId());
        assertEquals(List.of("AAA-111"), visita.getVehiculosPlacas());
    }

    @Test
    void debeAsignarFechaEntradaPorDefectoSiEsNull() {
        Visita visita = new Visita.Builder()
                .personaId(1)
                .estadoVisitaId(4)
                .metodoIngresoId(1)
                .build();

        assertNotNull(visita.getFechaEntrada());
    }

    @Test
    void debeLanzarExcepcionSiPersonaIdEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Visita.builder()
                .personaId(0)
                .estadoVisitaId(4)
                .metodoIngresoId(1)
                .build());
    }

    @Test
    void debeLanzarExcepcionSiEstadoVisitaIdEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Visita.builder()
                .personaId(1)
                .estadoVisitaId(0)
                .metodoIngresoId(1)
                .build());
    }

    @Test
    void debeLanzarExcepcionSiMetodoIngresoIdEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Visita.builder()
                .personaId(1)
                .estadoVisitaId(4)
                .metodoIngresoId(0)
                .build());
    }

    @Test
    void debeLanzarExcepcionSiFechaSalidaEsAnteriorAFechaEntrada() {
        LocalDateTime entrada = LocalDateTime.of(2026, 11, 19, 10, 0);
        LocalDateTime salidaInvalida = LocalDateTime.of(2026, 11, 19, 9, 0);

        assertThrows(IllegalArgumentException.class, () -> Visita.builder()
                .personaId(1)
                .estadoVisitaId(5)
                .metodoIngresoId(1)
                .fechaEntrada(entrada)
                .fechaSalida(salidaInvalida)
                .build());
    }
}
