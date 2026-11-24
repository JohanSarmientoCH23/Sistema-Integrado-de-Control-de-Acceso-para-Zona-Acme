package com.zonaacme.sica.application.sync;

import com.zonaacme.sica.config.AppConfig;
import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Patrón de Diseño #3 (Observer + Polling Multi-PC): Sujeto observable que consulta
 * periódicamente VisitaRepository en un hilo daemon usando el intervalo configurado en AppConfig,
 * compara contra el último snapshot y notifica a los AforoObserver solo cuando hay cambios
 * (o explícitamente mediante forzarActualizacion()).
 */
public class SincronizadorEstadoService {

    private final VisitaRepository visitaRepository;
    private final int intervaloSegundos;
    private final List<AforoObserver> observadores = new CopyOnWriteArrayList<>();
    private final AtomicBoolean ejecutando = new AtomicBoolean(false);

    private ScheduledExecutorService scheduler;
    private volatile List<AforoActual> ultimoAforo = null;
    private volatile List<VisitaPendiente> ultimasPendientes = null;

    public SincronizadorEstadoService(VisitaRepository visitaRepository) {
        this(visitaRepository, AppConfig.getInstance().getPollingIntervalSeconds());
    }

    public SincronizadorEstadoService(VisitaRepository visitaRepository, int intervaloSegundos) {
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
        this.intervaloSegundos = Math.max(1, intervaloSegundos);
    }

    public void suscribir(AforoObserver observer) {
        if (observer != null && !observadores.contains(observer)) {
            observadores.add(observer);
        }
    }

    public void desuscribir(AforoObserver observer) {
        if (observer != null) {
            observadores.remove(observer);
        }
    }

    public synchronized void iniciar() {
        if (ejecutando.get()) {
            return;
        }
        ejecutando.set(true);
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread hilo = new Thread(runnable, "SICA-AforoPolling-Daemon");
            hilo.setDaemon(true);
            return hilo;
        });
        scheduler.scheduleAtFixedRate(this::verificarCambiosYNotificar, 0, intervaloSegundos, TimeUnit.SECONDS);
    }

    public synchronized void detener() {
        ejecutando.set(false);
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public boolean estaEjecutando() {
        return ejecutando.get();
    }

    /**
     * Consulta el estado actual y notifica a los observadores únicamente si hubo cambios
     * respecto al snapshot anterior.
     */
    public synchronized boolean verificarCambiosYNotificar() {
        try {
            List<AforoActual> nuevoAforo = obtenerListaInmutable(visitaRepository.listarAforoActual());
            List<VisitaPendiente> nuevasPendientes = obtenerListaInmutable(visitaRepository.listarVisitasPendientes());

            boolean huboCambio = !Objects.equals(ultimoAforo, nuevoAforo)
                    || !Objects.equals(ultimasPendientes, nuevasPendientes);

            if (huboCambio) {
                ultimoAforo = nuevoAforo;
                ultimasPendientes = nuevasPendientes;
                notificarObservadores(nuevoAforo, nuevasPendientes);
                return true;
            }
            return false;
        } catch (Exception ignored) {
            // Evita que un fallo temporal de red detenga el hilo daemon de polling
            return false;
        }
    }

    /**
     * Fuerza una consulta inmediata al repositorio, actualiza el snapshot y notifica a todos los observadores.
     */
    public synchronized void forzarActualizacion() {
        List<AforoActual> nuevoAforo = obtenerListaInmutable(visitaRepository.listarAforoActual());
        List<VisitaPendiente> nuevasPendientes = obtenerListaInmutable(visitaRepository.listarVisitasPendientes());
        ultimoAforo = nuevoAforo;
        ultimasPendientes = nuevasPendientes;
        notificarObservadores(nuevoAforo, nuevasPendientes);
    }

    private void notificarObservadores(List<AforoActual> aforo, List<VisitaPendiente> pendientes) {
        for (AforoObserver observer : observadores) {
            observer.onEstadoActualizado(aforo, pendientes);
        }
    }

    private static <T> List<T> obtenerListaInmutable(List<T> origen) {
        return origen != null ? List.copyOf(origen) : Collections.emptyList();
    }
}
