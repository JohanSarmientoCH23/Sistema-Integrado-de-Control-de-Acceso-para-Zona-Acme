package com.zonaacme.sica.application.usecase.visita;

import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para consultar las visitas pendientes de aprobación (vw_visitas_pendientes).
 */
public class ConsultarVisitasPendientesUseCase {

    private final VisitaRepository visitaRepository;

    public ConsultarVisitasPendientesUseCase(VisitaRepository visitaRepository) {
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
    }

    public List<VisitaPendiente> ejecutar() {
        return visitaRepository.listarVisitasPendientes();
    }
}
