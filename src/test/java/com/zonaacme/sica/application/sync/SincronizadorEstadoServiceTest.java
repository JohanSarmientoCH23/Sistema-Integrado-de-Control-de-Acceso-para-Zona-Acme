package com.zonaacme.sica.application.sync;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.VisitaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SincronizadorEstadoServiceTest {

    @Test
    void debeNotificarAlObserverSoloCuandoHayaCambiosEnAforoOPendientes() {
        List<AforoActual> aforoMutable = new ArrayList<>();
        List<VisitaPendiente> pendientesMutable = new ArrayList<>();

        LocalDateTime entrada = LocalDateTime.of(2026, 11, 24, 8, 0);
        aforoMutable.add(new AforoActual(2, "Alberto Ruiz", "1098000222", "Avicola ERP Solutions", "Trabajador", entrada, "Biometrico_Camara"));

        VisitaRepository fakeVisitaRepo = new VisitaRepository() {
            @Override public Visita guardar(Visita v) { return v; }
            @Override public void actualizar(Visita v) {}
            @Override public Optional<Visita> buscarPorId(int id) { return Optional.empty(); }
            @Override public boolean tieneVisitaAbierta(int personaId) { return false; }
            @Override public void cerrarVisitaPorSistema(int visitaId) {}
            @Override public List<AforoActual> listarAforoActual() { return new ArrayList<>(aforoMutable); }
            @Override public List<VisitaPendiente> listarVisitasPendientes() { return new ArrayList<>(pendientesMutable); }
        };

        SincronizadorEstadoService sincronizador = new SincronizadorEstadoService(fakeVisitaRepo, 3);
        AtomicInteger notificaciones = new AtomicInteger(0);
        List<AforoActual> ultimoAforoRecibido = new ArrayList<>();

        AforoObserver observer = (aforo, pendientes) -> {
            notificaciones.incrementAndGet();
            ultimoAforoRecibido.clear();
            ultimoAforoRecibido.addAll(aforo);
        };

        sincronizador.suscribir(observer);

        // 1. Primera verificación: detecta estado inicial y notifica
        assertTrue(sincronizador.verificarCambiosYNotificar());
        assertEquals(1, notificaciones.get());
        assertEquals(1, ultimoAforoRecibido.size());

        // 2. Segunda verificación sin cambios: NO debe notificar
        assertFalse(sincronizador.verificarCambiosYNotificar());
        assertEquals(1, notificaciones.get());

        // 3. Nuevo ingreso desde otro PC: detecta cambio y notifica
        aforoMutable.add(new AforoActual(4, "Daniela Vargas", "1098000444", "Logistica Acme", "Invitado", entrada.plusMinutes(15), "PIN_Digitado"));
        assertTrue(sincronizador.verificarCambiosYNotificar());
        assertEquals(2, notificaciones.get());
        assertEquals(2, ultimoAforoRecibido.size());

        // 4. Forzar actualización notifica siempre
        sincronizador.forzarActualizacion();
        assertEquals(3, notificaciones.get());

        // 5. Desuscribir observer
        sincronizador.desuscribir(observer);
        sincronizador.forzarActualizacion();
        assertEquals(3, notificaciones.get());
    }
}
