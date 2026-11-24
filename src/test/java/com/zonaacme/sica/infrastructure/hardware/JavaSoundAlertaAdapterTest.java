package com.zonaacme.sica.infrastructure.hardware;

import com.zonaacme.sica.application.port.out.AlertaSonoraPort.TipoAlerta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JavaSoundAlertaAdapterTest {

    @Test
    void debeReproducirLos4TiposDeAlertaSinLanzarExcepcionesInclusoSinDispositivoDeAudio() {
        JavaSoundAlertaAdapter adapter = new JavaSoundAlertaAdapter();

        for (TipoAlerta tipo : TipoAlerta.values()) {
            assertDoesNotThrow(() -> adapter.reproducir(tipo),
                    "Invocar reproducir(" + tipo + ") no debe lanzar excepciones");
        }
    }
}
