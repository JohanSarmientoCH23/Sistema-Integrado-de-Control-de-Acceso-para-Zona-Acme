package com.zonaacme.sica.application.usecase.token;

import com.zonaacme.sica.domain.entity.TokenAcceso;
import com.zonaacme.sica.domain.repository.TokenAccesoRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso para validar y consultar el estado de un token de acceso.
 */
public class ValidarTokenUseCase {

    private final TokenAccesoRepository tokenAccesoRepository;

    public ValidarTokenUseCase(TokenAccesoRepository tokenAccesoRepository) {
        this.tokenAccesoRepository = Objects.requireNonNull(tokenAccesoRepository, "tokenAccesoRepository no puede ser nulo");
    }

    public String validarEstado(String codigoToken) {
        if (codigoToken == null || codigoToken.isBlank()) {
            return "TOKEN_NO_EXISTE";
        }
        return tokenAccesoRepository.validarEstadoToken(codigoToken.trim());
    }

    public boolean esValido(String codigoToken) {
        return "VALIDO".equalsIgnoreCase(validarEstado(codigoToken));
    }

    public Optional<TokenAcceso> buscarPorCodigo(String codigoToken) {
        if (codigoToken == null || codigoToken.isBlank()) {
            return Optional.empty();
        }
        return tokenAccesoRepository.buscarPorCodigo(codigoToken.trim());
    }
}
