package com.zonaacme.sica.application.usecase.persona;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.application.usecase.autorizacion.AutorizacionService;
import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.PerfilPersona;
import com.zonaacme.sica.domain.entity.Persona;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.domain.repository.PersonaRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso para gestionar personas (trabajadores, invitados, contratistas, proveedores, auditores)
 * y su estado de acceso (Activo, Inactivo, Suspendido Temporalmente, Prohibicion Definitiva).
 */
public class GestionarPersonaUseCase {

    public static final String PERMISO_CREAR_PERSONA = "crear_persona";
    public static final String PERMISO_EDITAR_PERSONA = "editar_persona";
    public static final String PERMISO_BLOQUEAR_PERSONA = "bloquear_persona";

    public static final int ESTADO_ACCESO_ACTIVO_ID = 1;
    public static final int ESTADO_ACCESO_INACTIVO_ID = 2;
    public static final int ESTADO_ACCESO_SUSPENDIDO_ID = 3;
    public static final int ESTADO_ACCESO_PROHIBICION_DEFINITIVA_ID = 4;

    private final AutorizacionService autorizacionService;
    private final PersonaRepository personaRepository;
    private final BitacoraRepository bitacoraRepository;

    public GestionarPersonaUseCase(AutorizacionService autorizacionService,
                                   PersonaRepository personaRepository,
                                   BitacoraRepository bitacoraRepository) {
        this.autorizacionService = Objects.requireNonNull(autorizacionService, "autorizacionService no puede ser nulo");
        this.personaRepository = Objects.requireNonNull(personaRepository, "personaRepository no puede ser nulo");
        this.bitacoraRepository = Objects.requireNonNull(bitacoraRepository, "bitacoraRepository no puede ser nulo");
    }

    public Persona registrarPersona(Usuario operador, Persona persona, PerfilPersona perfil) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")) {
            autorizacionService.validarPermiso(operador, PERMISO_CREAR_PERSONA);
        }
        if (persona == null || persona.getNombre() == null || persona.getNombre().isBlank()
                || persona.getDocumentoIdentidad() == null || persona.getDocumentoIdentidad().isBlank()) {
            throw new BusinessRuleException("El nombre y el documento de identidad son obligatorios.");
        }
        if (personaRepository.buscarPorDocumento(persona.getDocumentoIdentidad().trim()).isPresent()) {
            throw new BusinessRuleException("Ya existe una persona registrada con el documento: " + persona.getDocumentoIdentidad());
        }

        Persona guardada = personaRepository.guardar(persona, perfil);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada("REGISTRO_PERSONA")
                .tablaAfectada("personas")
                .registroIdAfectado(guardada.getId())
                .detallesJson("{\"documento\": \"" + guardada.getDocumentoIdentidad() + "\"}")
                .build();
        bitacoraRepository.registrar(bitacora);

        return guardada;
    }

    public Optional<PerfilCompleto> buscarPorDocumento(String documentoIdentidad) {
        if (documentoIdentidad == null || documentoIdentidad.isBlank()) {
            return Optional.empty();
        }
        return personaRepository.buscarPerfilCompletoPorDocumento(documentoIdentidad.trim());
    }

    public List<PerfilCompleto> listarPerfilesCompletos() {
        return personaRepository.listarPerfilesCompletos();
    }

    public void cambiarEstadoAcceso(Usuario operador, int personaId, int nuevoEstadoAccesoId) {
        if (!autorizacionService.tieneRol(operador, "Superusuario")) {
            if (nuevoEstadoAccesoId == ESTADO_ACCESO_PROHIBICION_DEFINITIVA_ID || nuevoEstadoAccesoId == ESTADO_ACCESO_SUSPENDIDO_ID) {
                autorizacionService.validarPermiso(operador, PERMISO_BLOQUEAR_PERSONA);
            } else {
                autorizacionService.validarPermiso(operador, PERMISO_EDITAR_PERSONA);
            }
        }

        personaRepository.buscarPorId(personaId)
                .orElseThrow(() -> new BusinessRuleException("No existe la persona con id: " + personaId));

        personaRepository.actualizarEstadoAcceso(personaId, nuevoEstadoAccesoId);

        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .usuarioId(operador != null ? operador.getId() : null)
                .accionRealizada("CAMBIO_ESTADO_ACCESO_PERSONA")
                .tablaAfectada("perfiles_persona")
                .registroIdAfectado(personaId)
                .detallesJson("{\"personaId\": " + personaId + ", \"nuevoEstadoAccesoId\": " + nuevoEstadoAccesoId + "}")
                .build();
        bitacoraRepository.registrar(bitacora);
    }
}
