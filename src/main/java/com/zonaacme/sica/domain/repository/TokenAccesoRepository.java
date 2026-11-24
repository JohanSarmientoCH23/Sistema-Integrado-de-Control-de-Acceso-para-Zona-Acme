package com.zonaacme.sica.domain.repository;

import com.zonaacme.sica.domain.entity.TokenAcceso;

import java.util.Optional;

public interface TokenAccesoRepository {
    TokenAcceso guardar(TokenAcceso tokenAcceso);
    Optional<TokenAcceso> buscarPorCodigo(String codigoToken);
    String validarEstadoToken(String codigoToken);
    void actualizarEstado(String codigoToken, int nuevoEstadoTokenId);
}
