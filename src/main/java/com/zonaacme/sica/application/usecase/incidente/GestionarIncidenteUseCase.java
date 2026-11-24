package com.zonaacme.sica.application.usecase.incidente;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.Incidente;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.IncidenteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para registrar y consultar incidentes de seguridad en Zona Acme.
 */
public class GestionarIncidenteUseCase {

    public static final String PERMISO_REPORTAR_INCIDENTE = "reportar_incidente";
    public static final String PERMISO_VER_INCIDENTES = "ver_incidentes";

    private final AutorizacionService autorizacionService;
    private final IncidenteRepository incidenteRepository;
    private final BitacoraRepository bitacoraRepository;

    public GestionarIncidenteUseCase(AutorizacionService autorizacionService,
                                     IncidenteRepository incidenteRepository,
                                     BitacoraRepository bitacoraRepository) {
        this.autorizacionService = Objects.requireNonNull(autorizacionService, "autorizacionService no puede ser nulo");
        this.incidenteRepository = Objects.requireNonNull(incidenteRepository, "incidenteRepository no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public Incidente registrarIncidente(Usuario reportadoPor, Integer visitaId, String descripcion) {
        autorizacionService.validarPermiso(reportadoPor, PERMISO_REPORTAR_INCIDENTE);

        if (descripcion == null || descripcion.isBlank()) {
            throw new BusinessRuleException("La descripción del incidente es obligatoria.");
        }

        Incidente incidente = new Incidente(
                0,
                visitaId,
                reportadoPor.getId(),
                LocalDateTime.now(),
                descripcion.trim()
        );

        Incidente guardado = incidenteRepository.guardar(incidente);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(reportadoPor.getId())
                .accionRealizada("CREACION_INCIDENTE")
                .tablaAfectada("incidentes")
                .registroIdAfectado(guardado.getId())
                .detallesJson("{\"visitaId\": " + (visitaId != null ? visitaId : "null") + "}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return guardado;
    }

    public List<Incidente> listarPorVisita(int visitaId) {
        return incidenteRepository.listarPorVisita(visitaId);
    }

    public List<Incidente> listarTodos(Usuario solicitante) {
        if (!autorizacionService.tieneRol(solicitante, "Superusuario")
                && !autorizacionService.tienePermiso(solicitante, PERMISO_REPORTAR_INCIDENTE)) {
            autorizacionService.validarPermiso(solicitante, PERMISO_VER_INCIDENTES);
        }
        return incidenteRepository.listarTodos();
    }
}
