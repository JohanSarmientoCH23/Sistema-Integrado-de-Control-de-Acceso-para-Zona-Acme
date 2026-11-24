package com.zonaacme.sica.application.usecase.visita;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Caso de uso para aprobar o rechazar solicitudes de visitas pendientes (vw_visitas_pendientes).
 */
public class GestionarAprobacionVisitaUseCase {

    public static final String PERMISO_APROBAR_VISITA = "aprobar_visita";
    public static final String PERMISO_RECHAZAR_VISITA = "rechazar_visita";

    public static final int ESTADO_PENDIENTE_APROBACION_ID = 2;
    public static final int ESTADO_RECHAZADO_ID = 3;
    public static final int ESTADO_DENTRO_ID = 4;

    private final AutorizacionService autorizacionService;
    private final VisitaRepository visitaRepository;
    private final BitacoraRepository bitacoraRepository;

    public GestionarAprobacionVisitaUseCase(AutorizacionService autorizacionService,
                                            VisitaRepository visitaRepository,
                                            BitacoraRepository bitacoraRepository) {
        this.autorizacionService = Objects.requireNonNull(autorizacionService, "autorizacionService no puede ser nulo");
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public Visita aprobarVisita(Usuario funcionario, int visitaId) {
        if (!autorizacionService.tieneRol(funcionario, "Superusuario")) {
            autorizacionService.validarPermiso(funcionario, PERMISO_APROBAR_VISITA);
        }

        Visita visita = obtenerVisitaPendiente(visitaId);
        visita.setEstadoVisitaId(ESTADO_DENTRO_ID);
        visita.setEstadoVisitaNombre("Dentro");
        visita.setVisitaAprobadaPor(funcionario.getId());
        if (visita.getFechaEntrada() == null) {
            visita.setFechaEntrada(LocalDateTime.now());
        }

        visitaRepository.actualizar(visita);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(funcionario.getId())
                .accionRealizada("APROBACION_VISITA")
                .tablaAfectada("visitas")
                .registroIdAfectado(visitaId)
                .detallesJson("{\"funcionario\": \"" + funcionario.getNombre() + "\"}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return visita;
    }

    public Visita rechazarVisita(Usuario funcionario, int visitaId, String motivo) {
        if (!autorizacionService.tieneRol(funcionario, "Superusuario")
                && !autorizacionService.tienePermiso(funcionario, PERMISO_APROBAR_VISITA)) {
            autorizacionService.validarPermiso(funcionario, PERMISO_RECHAZAR_VISITA);
        }

        Visita visita = obtenerVisitaPendiente(visitaId);
        visita.setEstadoVisitaId(ESTADO_RECHAZADO_ID);
        visita.setEstadoVisitaNombre("Rechazado");
        visita.setVisitaAprobadaPor(funcionario.getId());
        visita.setFechaSalida(LocalDateTime.now());

        visitaRepository.actualizar(visita);

        String motivoLimpio = (motivo != null && !motivo.isBlank()) ? motivo.trim() : "Rechazado por funcionario";
        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(funcionario.getId())
                .accionRealizada("RECHAZO_VISITA")
                .tablaAfectada("visitas")
                .registroIdAfectado(visitaId)
                .detallesJson("{\"funcionario\": \"" + funcionario.getNombre() + "\", \"motivo\": \"" + motivoLimpio + "\"}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return visita;
    }

    private Visita obtenerVisitaPendiente(int visitaId) {
        Visita visita = visitaRepository.buscarPorId(visitaId)
                .orElseThrow(() -> new BusinessRuleException("No se encontró la visita con id: " + visitaId));
        if (visita.getEstadoVisitaId() != ESTADO_PENDIENTE_APROBACION_ID) {
            throw new BusinessRuleException("La visita con id " + visitaId + " no se encuentra en estado 'Pendiente Aprobacion'.");
        }
        return visita;
    }
}
