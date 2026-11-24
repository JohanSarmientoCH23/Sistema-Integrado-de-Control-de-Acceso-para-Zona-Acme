package com.zonaacme.sica.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BitacoraAuditoriaBuilderTest {

    @Test
    void debeConstruirBitacoraAuditoriaValidaConBuilder() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 19, 11, 0);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .id(100L)
                .usuarioId(1)
                .fechaHora(ahora)
                .accionRealizada("CHECK_IN_BIOMETRICO")
                .tablaAfectada("visitas")
                .registroIdAfectado(15)
                .detallesJson("{\"coincidencia\": \"99.1%\"}")
                .build();

        assertEquals(100L, bitacora.getId());
        assertEquals(1, bitacora.getUsuarioId());
        assertEquals(ahora, bitacora.getFechaHora());
        assertEquals("CHECK_IN_BIOMETRICO", bitacora.getAccionRealizada());
        assertEquals("visitas", bitacora.getTablaAfectada());
        assertEquals(15, bitacora.getRegistroIdAfectado());
        assertEquals("{\"coincidencia\": \"99.1%\"}", bitacora.getDetallesJson());
    }

    @Test
    void debeAsignarValoresPorDefectoParaFechaHoraYDetallesJson() {
        BitacoraAuditoria bitacora = new BitacoraAuditoria.Builder()
                .accionRealizada("LOGIN_EXITOSO")
                .build();

        assertNotNull(bitacora.getFechaHora());
        assertEquals("{}", bitacora.getDetallesJson());
    }

    @Test
    void debeLanzarExcepcionSiAccionRealizadaEsNulaOVacia() {
        assertThrows(IllegalArgumentException.class, () -> BitacoraAuditoria.builder()
                .accionRealizada("   ")
                .build());

        assertThrows(IllegalArgumentException.class, () -> BitacoraAuditoria.builder()
                .build());
    }

    @Test
    void debeLanzarExcepcionSiUsuarioIdEsMenorOIgualACero() {
        assertThrows(IllegalArgumentException.class, () -> BitacoraAuditoria.builder()
                .usuarioId(0)
                .accionRealizada("LOGIN_FALLIDO")
                .build());
    }
}
