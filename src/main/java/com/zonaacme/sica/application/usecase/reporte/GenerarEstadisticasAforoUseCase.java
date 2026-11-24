package com.zonaacme.sica.application.usecase.reporte;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.PersonaRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Caso de uso para generar estadísticas de aforo en tiempo real y distribución de perfiles
 * utilizando Java Stream API (Collectors.groupingBy, Collectors.counting).
 */
public class GenerarEstadisticasAforoUseCase {

    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;

    public GenerarEstadisticasAforoUseCase(VisitaRepository visitaRepository, PersonaRepository personaRepository) {
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
        this.personaRepository = Objects.requireNonNull(personaRepository, "personaRepository no puede ser nulo");
    }

    public ResumenAforo generarResumen() {
        List<AforoActual> aforoActual = visitaRepository.listarAforoActual();
        List<VisitaPendiente> pendientes = visitaRepository.listarVisitasPendientes();

        List<AforoActual> listaSegura = aforoActual != null ? aforoActual : Collections.emptyList();
        int totalPendientes = pendientes != null ? pendientes.size() : 0;

        Map<String, Long> conteoPorEmpresa = listaSegura.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        a -> a.empresa() != null ? a.empresa() : "Sin Empresa",
                        Collectors.counting()
                ));

        Map<String, Long> conteoPorTipoPersona = listaSegura.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        a -> a.tipoPersona() != null ? a.tipoPersona() : "Sin Tipo",
                        Collectors.counting()
                ));

        Map<String, Long> conteoPorMetodoIngreso = listaSegura.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        a -> a.metodoIngreso() != null ? a.metodoIngreso() : "Desconocido",
                        Collectors.counting()
                ));

        return new ResumenAforo(
                listaSegura.size(),
                conteoPorEmpresa,
                conteoPorTipoPersona,
                conteoPorMetodoIngreso,
                totalPendientes
        );
    }

    public Map<String, Long> contarPerfilesPorEstadoAcceso() {
        List<PerfilCompleto> perfiles = personaRepository.listarPerfilesCompletos();
        if (perfiles == null || perfiles.isEmpty()) {
            return Collections.emptyMap();
        }
        return perfiles.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        p -> p.estadoAcceso() != null ? p.estadoAcceso() : "Desconocido",
                        Collectors.counting()
                ));
    }
}
