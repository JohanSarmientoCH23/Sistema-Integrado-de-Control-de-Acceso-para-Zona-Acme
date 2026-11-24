package com.zonaacme.sica.application.usecase.visita;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.application.usecase.visita.strategy.ValidadorAccesoContext;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Caso de uso para registrar la salida (normal o forzada por sistema) de una visita activa.
 */
public class RegistrarSalidaUseCase {

    public static final String PERMISO_REGISTRAR_VISITA_MANUAL = "registrar_visita_manual";
    public static final String PERMISO_CERRAR_VISITA_FORZADA = "cerrar_visita_forzada";
    public static final int ESTADO_VISITA_DENTRO_ID = 4;
    public static final int ESTADO_VISITA_FUERA_ID = 5;

    private final AutorizacionService autorizacionService;
    private final VisitaRepository visitaRepository;
    private final BitacoraRepository bitacoraRepository;

    public RegistrarSalidaUseCase(AutorizacionService autorizacionService,
                                  VisitaRepository visitaRepository,
                                  BitacoraRepository bitacoraRepository) {
        this.autorizacionService = Objects.requireNonNull(autorizacionService, "autorizacionService no puede ser nulo");
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public Visita registrarSalidaNormal(Usuario operador, int visitaId, String tipoMetodoSalida) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")
                && !autorizacionService.tienePermiso(operador, PERMISO_CERRAR_VISITA_FORZADA)) {
            autorizacionService.validarPermiso(operador, PERMISO_REGISTRAR_VISITA_MANUAL);
        }

        Visita visita = visitaRepository.buscarPorId(visitaId)
                .orElseThrow(() -> new BusinessRuleException("No existe la visita con id: " + visitaId));

        if (visita.getEstadoVisitaId() != ESTADO_VISITA_DENTRO_ID) {
            throw new BusinessRuleException("La visita con id " + visitaId + " no se encuentra activa ('Dentro').");
        }

        int metodoSalidaId = ValidadorAccesoContext.resolverMetodoAccesoId(tipoMetodoSalida);
        visita.setFechaSalida(LocalDateTime.now());
        visita.setEstadoVisitaId(ESTADO_VISITA_FUERA_ID);
        visita.setEstadoVisitaNombre("Fuera");
        visita.setMetodoSalidaId(metodoSalidaId);

        visitaRepository.actualizar(visita);

        String accion = metodoSalidaId == ValidadorAccesoContext.METODO_BIOMETRICO_CAMARA_ID
                ? "CHECK_OUT_BIOMETRICO"
                : "CHECK_OUT_MANUAL";

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada(accion)
                .tablaAfectada("visitas")
                .registroIdAfectado(visitaId)
                .detallesJson("{\"visitaId\": " + visitaId + ", \"estado\": \"Fuera\"}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return visita;
    }

    public void cerrarVisitaForzadaPorSistema(Usuario operador, int visitaId) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")) {
            autorizacionService.validarPermiso(operador, PERMISO_CERRAR_VISITA_FORZADA);
        }

        visitaRepository.buscarPorId(visitaId)
                .orElseThrow(() -> new BusinessRuleException("No existe la visita con id: " + visitaId));

        visitaRepository.cerrarVisitaPorSistema(visitaId);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada("CIERRE_FORZADO_VISITA")
                .tablaAfectada("visitas")
                .registroIdAfectado(visitaId)
                .detallesJson("{\"visitaId\": " + visitaId + ", \"estado\": \"Cerrada por Sistema\"}")
                .build();
        bitacoraRepository.registrar(bitacora);
    }
}
