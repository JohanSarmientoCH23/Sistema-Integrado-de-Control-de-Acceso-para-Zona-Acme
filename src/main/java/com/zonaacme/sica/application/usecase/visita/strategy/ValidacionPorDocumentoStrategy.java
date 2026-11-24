package com.zonaacme.sica.application.usecase.visita.strategy;

import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.repository.PersonaRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Estrategia de validación de acceso mediante documento de identidad (Manual_Documento).
 * Verifica la existencia del perfil en vw_perfiles_completos y que su estado_acceso sea 'Activo'.
 */
public class ValidacionPorDocumentoStrategy implements EstrategiaValidacionAcceso {

    private final PersonaRepository personaRepository;

    public ValidacionPorDocumentoStrategy(PersonaRepository personaRepository) {
        this.personaRepository = Objects.requireNonNull(personaRepository, "personaRepository no puede ser nulo");
    }

    @Override
    public ResultadoValidacionAcceso validar(SolicitudAcceso solicitud) {
        if (solicitud == null || solicitud.documentoIdentidad() == null || solicitud.documentoIdentidad().isBlank()) {
            return new ResultadoValidacionAcceso(false, "El documento de identidad es obligatorio.", null, null, null);
        }

        String documento = solicitud.documentoIdentidad().trim();
        Optional<PerfilCompleto> optPerfil = personaRepository.buscarPerfilCompletoPorDocumento(documento);

        if (optPerfil.isEmpty()) {
            return new ResultadoValidacionAcceso(false, "Persona no encontrada con documento: " + documento, null, null, null);
        }

        PerfilCompleto perfil = optPerfil.get();
        if (!esEstadoAccesoPermitido(perfil.estadoAcceso())) {
            return new ResultadoValidacionAcceso(
                    false,
                    "Acceso denegado. Estado de acceso de la persona: " + perfil.estadoAcceso(),
                    perfil.personaId(),
                    perfil,
                    null
            );
        }

        return new ResultadoValidacionAcceso(
                true,
                "Acceso autorizado por documento para " + perfil.nombrePersona(),
                perfil.personaId(),
                perfil,
                null
        );
    }

    static boolean esEstadoAccesoPermitido(String estadoAcceso) {
        if (estadoAcceso == null || estadoAcceso.isBlank()) {
            return false;
        }
        String normalizado = estadoAcceso.trim();
        return normalizado.equalsIgnoreCase("Activo") || normalizado.equalsIgnoreCase("Permitido");
    }
}
