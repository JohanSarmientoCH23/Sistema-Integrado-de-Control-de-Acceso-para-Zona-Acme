package com.zonaacme.sica.application.usecase.visita;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para consultar el aforo actual en tiempo real (vw_aforo_actual).
 */
public class ConsultarAforoActualUseCase {

    private final VisitaRepository visitaRepository;

    public ConsultarAforoActualUseCase(VisitaRepository visitaRepository) {
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
    }

    public List<AforoActual> ejecutar() {
        return visitaRepository.listarAforoActual();
    }
}
