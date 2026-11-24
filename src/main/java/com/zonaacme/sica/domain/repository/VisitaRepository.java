package com.zonaacme.sica.domain.repository;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.entity.VisitaPendiente;

import java.util.List;
import java.util.Optional;

public interface VisitaRepository {
    Visita guardar(Visita visita);
    void actualizar(Visita visita);
    Optional<Visita> buscarPorId(int id);
    boolean tieneVisitaAbierta(int personaId);
    void cerrarVisitaPorSistema(int visitaId);
    List<AforoActual> listarAforoActual();
    List<VisitaPendiente> listarVisitasPendientes();
}
