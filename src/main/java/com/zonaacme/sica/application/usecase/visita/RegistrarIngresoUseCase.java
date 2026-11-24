package com.zonaacme.sica.application.usecase.visita;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.application.usecase.visita.strategy.ResultadoValidacionAcceso;
import com.zonaacme.sica.application.usecase.visita.strategy.SolicitudAcceso;
import com.zonaacme.sica.application.usecase.visita.strategy.ValidadorAccesoContext;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.TokenAccesoRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso para registrar el ingreso de una persona al complejo Zona Acme.
 * Valida RBAC, ejecuta el patrón Strategy, aplica control Anti-Passback (tieneVisitaAbierta),
 * construye la Visita con Visita.Builder, marca el token como Usado si aplica y audita con BitacoraAuditoria.Builder.
 */
public class RegistrarIngresoUseCase {

    public static final String PERMISO_REGISTRAR_VISITA_MANUAL = "registrar_visita_manual";
    public static final int ESTADO_VISITA_DENTRO_ID = 4;
    public static final int ESTADO_TOKEN_USADO_ID = 2;

    private final AutorizacionService autorizacionService;
    private final ValidadorAccesoContext validadorAccesoContext;
    private final VisitaRepository visitaRepository;
    private final TokenAccesoRepository tokenAccesoRepository;
    private final BitacoraRepository bitacoraRepository;

    public RegistrarIngresoUseCase(AutorizacionService autorizacionService,
                                   ValidadorAccesoContext validadorAccesoContext,
                                   VisitaRepository visitaRepository,
                                   TokenAccesoRepository tokenAccesoRepository,
                                   BitacoraRepository bitacoraRepository) {
        this.autorizacionService = Objects.requireNonNull(autorizacionService, "autorizacionService no puede ser nulo");
        this.validadorAccesoContext = Objects.requireNonNull(validadorAccesoContext, "validadorAccesoContext no puede ser nulo");
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
        this.tokenAccesoRepository = Objects.requireNonNull(tokenAccesoRepository, "tokenAccesoRepository no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public Visita ejecutar(Usuario operador, SolicitudAcceso solicitud, List<String> vehiculosPlacas) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")) {
            autorizacionService.validarPermiso(operador, PERMISO_REGISTRAR_VISITA_MANUAL);
        }

        ResultadoValidacionAcceso resultado = validadorAccesoContext.validar(solicitud);
        if (!resultado.autorizado() || resultado.personaId() == null) {
            registrarAuditoriaDenegacion(operador, resultado);
            throw new BusinessRuleException(resultado.mensaje());
        }

        int personaId = resultado.personaId();
        if (visitaRepository.tieneVisitaAbierta(personaId)) {
            throw new BusinessRuleException("Anti-passback activo: la persona ya tiene una visita abierta ('Dentro') en el complejo.");
        }

        int metodoIngresoId = ValidadorAccesoContext.resolverMetodoAccesoId(solicitud.tipoMetodo());

        Visita nuevaVisita = Visita.builder()
                .personaId(personaId)
                .fechaEntrada(LocalDateTime.now())
                .estadoVisitaId(ESTADO_VISITA_DENTRO_ID)
                .estadoVisitaNombre("Dentro")
                .metodoIngresoId(metodoIngresoId)
                .vehiculosPlacas(vehiculosPlacas)
                .build();

        Visita visitaGuardada = visitaRepository.guardar(nuevaVisita);

        if (solicitud.codigoToken() != null && !solicitud.codigoToken().isBlank()) {
            tokenAccesoRepository.actualizarEstado(solicitud.codigoToken().trim(), ESTADO_TOKEN_USADO_ID);
        }

        String accionBitacora = metodoIngresoId == ValidadorAccesoContext.METODO_BIOMETRICO_CAMARA_ID
                ? "CHECK_IN_BIOMETRICO"
                : "CHECK_IN_MANUAL";

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada(accionBitacora)
                .tablaAfectada("visitas")
                .registroIdAfectado(visitaGuardada.getId())
                .detallesJson("{\"metodo\": \"" + solicitud.tipoMetodo() + "\", \"personaId\": " + personaId + "}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return visitaGuardada;
    }

    private void registrarAuditoriaDenegacion(Usuario operador, ResultadoValidacionAcceso resultado) {
        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada("ACCESO_DENEGADO")
                .tablaAfectada("visitas")
                .registroIdAfectado(resultado != null ? resultado.personaId() : null)
                .detallesJson("{\"motivo\": \"" + (resultado != null ? resultado.mensaje() : "Validacion fallida") + "\"}")
                .build();
        bitacoraRepository.registrar(bitacora);
    }
}
