package com.zonaacme.sica.application.usecase.token;

import com.zonaacme.sica.application.exception.BusinessRuleException;
import com.zonaacme.sica.domain.entity.TokenAcceso;
import com.zonaacme.sica.domain.repository.TokenAccesoRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso para generar códigos de acceso temporales (PIN_Numerico, Codigo_QR, Codigo_Barras)
 * asociados a una visita con vigencia en minutos.
 */
public class GenerarTokenAccesoUseCase {

    public static final int TIPO_TOKEN_PIN_NUMERICO_ID = 1;
    public static final int TIPO_TOKEN_CODIGO_QR_ID = 2;
    public static final int TIPO_TOKEN_CODIGO_BARRAS_ID = 3;
    public static final int ESTADO_TOKEN_EMITIDO_ID = 1;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TokenAccesoRepository tokenAccesoRepository;

    public GenerarTokenAccesoUseCase(TokenAccesoRepository tokenAccesoRepository) {
        this.tokenAccesoRepository = Objects.requireNonNull(tokenAccesoRepository, "tokenAccesoRepository no puede ser nulo");
    }

    public TokenAcceso generar(int visitaId, int tipoTokenId, int vigenciaMinutos) {
        if (visitaId <= 0) {
            throw new BusinessRuleException("El visitaId debe ser mayor a 0 para emitir un token.");
        }
        if (vigenciaMinutos <= 0) {
            throw new BusinessRuleException("La vigencia del token en minutos debe ser mayor a 0.");
        }

        String codigoUnico = generarCodigoUnico(tipoTokenId);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusMinutes(vigenciaMinutos);

        TokenAcceso token = new TokenAcceso(
                0L,
                visitaId,
                codigoUnico,
                tipoTokenId,
                ESTADO_TOKEN_EMITIDO_ID,
                ahora,
                expiracion
        );

        return tokenAccesoRepository.guardar(token);
    }

    private String generarCodigoUnico(int tipoTokenId) {
        for (int intento = 0; intento < 10; intento++) {
            String candidato = switch (tipoTokenId) {
                case TIPO_TOKEN_PIN_NUMERICO_ID -> String.format("%06d", 100000 + SECURE_RANDOM.nextInt(900000));
                case TIPO_TOKEN_CODIGO_QR_ID -> "QR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                case TIPO_TOKEN_CODIGO_BARRAS_ID -> "CB-" + (1000000000L + (Math.abs(SECURE_RANDOM.nextLong()) % 9000000000L));
                default -> "TK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            };
            if (tokenAccesoRepository.buscarPorCodigo(candidato).isEmpty()) {
                return candidato;
            }
        }
        return "QR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
