package com.zonaacme.sica.application.usecase.visita.strategy;

import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.Persona;
import com.zonaacme.sica.domain.entity.TokenAcceso;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.repository.PersonaRepository;
import com.zonaacme.sica.domain.repository.TokenAccesoRepository;
import com.zonaacme.sica.domain.repository.VisitaRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Estrategia de validación de acceso por token (PIN_Digitado, Escaneo_QR, Tarjeta_RFID).
 * Invoca TokenAccesoRepository.validarEstadoToken(codigo) (función SQL fn_validar_estado_token)
 * y comprueba el estado de acceso del perfil asociado.
 */
public class ValidacionPorTokenStrategy implements EstrategiaValidacionAcceso {

    private final TokenAccesoRepository tokenAccesoRepository;
    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;

    public ValidacionPorTokenStrategy(TokenAccesoRepository tokenAccesoRepository,
                                      VisitaRepository visitaRepository,
                                      PersonaRepository personaRepository) {
        this.tokenAccesoRepository = Objects.requireNonNull(tokenAccesoRepository, "tokenAccesoRepository no puede ser nulo");
        this.visitaRepository = Objects.requireNonNull(visitaRepository, "visitaRepository no puede ser nulo");
        this.personaRepository = Objects.requireNonNull(personaRepository, "personaRepository no puede ser nulo");
    }

    @Override
    public ResultadoValidacionAcceso validar(SolicitudAcceso solicitud) {
        if (solicitud == null || solicitud.codigoToken() == null || solicitud.codigoToken().isBlank()) {
            return new ResultadoValidacionAcceso(false, "El código de token es obligatorio.", null, null, null);
        }

        String codigo = solicitud.codigoToken().trim();
        String estadoToken = tokenAccesoRepository.validarEstadoToken(codigo);

        if (!"VALIDO".equalsIgnoreCase(estadoToken)) {
            return new ResultadoValidacionAcceso(
                    false,
                    "Token inválido: " + (estadoToken != null ? estadoToken : "TOKEN_NO_EXISTE"),
                    null,
                    null,
                    null
            );
        }

        PerfilCompleto perfil = resolverPerfilAsociado(solicitud, codigo);
        Integer visitaId = tokenAccesoRepository.buscarPorCodigo(codigo)
                .map(TokenAcceso::getVisitaId)
                .orElse(null);

        if (perfil == null) {
            return new ResultadoValidacionAcceso(
                    false,
                    "No se encontró un perfil de persona asociado al token: " + codigo,
                    null,
                    null,
                    visitaId
            );
        }

        if (!ValidacionPorDocumentoStrategy.esEstadoAccesoPermitido(perfil.estadoAcceso())) {
            return new ResultadoValidacionAcceso(
                    false,
                    "Acceso denegado. Estado de acceso de la persona: " + perfil.estadoAcceso(),
                    perfil.personaId(),
                    perfil,
                    visitaId
            );
        }

        return new ResultadoValidacionAcceso(
                true,
                "Acceso autorizado por token para " + perfil.nombrePersona(),
                perfil.personaId(),
                perfil,
                visitaId
        );
    }

    private PerfilCompleto resolverPerfilAsociado(SolicitudAcceso solicitud, String codigoToken) {
        if (solicitud.documentoIdentidad() != null && !solicitud.documentoIdentidad().isBlank()) {
            Optional<PerfilCompleto> porDoc = personaRepository.buscarPerfilCompletoPorDocumento(solicitud.documentoIdentidad().trim());
            if (porDoc.isPresent()) {
                return porDoc.get();
            }
        }

        Optional<TokenAcceso> optToken = tokenAccesoRepository.buscarPorCodigo(codigoToken);
        if (optToken.isEmpty()) {
            return null;
        }

        int visitaId = optToken.get().getVisitaId();
        Optional<Visita> optVisita = visitaRepository.buscarPorId(visitaId);
        if (optVisita.isEmpty()) {
            return null;
        }

        int personaId = optVisita.get().getPersonaId();
        Optional<Persona> optPersona = personaRepository.buscarPorId(personaId);
        if (optPersona.isPresent() && optPersona.get().getDocumentoIdentidad() != null) {
            Optional<PerfilCompleto> porDoc = personaRepository.buscarPerfilCompletoPorDocumento(optPersona.get().getDocumentoIdentidad());
            if (porDoc.isPresent()) {
                return porDoc.get();
            }
        }

        return personaRepository.listarPerfilesCompletos().stream()
                .filter(p -> p.personaId() == personaId)
                .findFirst()
                .orElse(null);
    }
}
