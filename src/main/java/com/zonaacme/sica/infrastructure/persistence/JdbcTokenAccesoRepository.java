package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.TokenAcceso;
import com.zonaacme.sica.domain.repository.TokenAccesoRepository;
import com.zonaacme.sica.infrastructure.database.DataAccessException;
import com.zonaacme.sica.infrastructure.database.DatabaseConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcTokenAccesoRepository implements TokenAccesoRepository {

    private final DatabaseConnectionPool connectionPool;

    public JdbcTokenAccesoRepository() {
        this(DatabaseConnectionPool.getInstance());
    }

    public JdbcTokenAccesoRepository(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public TokenAcceso guardar(TokenAcceso tokenAcceso) {
        String sql = """
                INSERT INTO tokens_acceso (visita_id, codigo_token, tipo_token_id, estado_token_id, fecha_emision, fecha_expiracion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime emision = tokenAcceso.getFechaEmision() != null ? tokenAcceso.getFechaEmision() : LocalDateTime.now();
            stmt.setInt(1, tokenAcceso.getVisitaId());
            stmt.setString(2, tokenAcceso.getCodigoToken());
            stmt.setInt(3, tokenAcceso.getTipoTokenId());
            stmt.setInt(4, tokenAcceso.getEstadoTokenId());
            stmt.setTimestamp(5, Timestamp.valueOf(emision));
            stmt.setTimestamp(6, Timestamp.valueOf(tokenAcceso.getFechaExpiracion()));
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tokenAcceso.setId(keys.getLong(1));
                }
            }
            tokenAcceso.setFechaEmision(emision);
            return tokenAcceso;
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar token de acceso: " + tokenAcceso.getCodigoToken(), e);
        }
    }

    @Override
    public Optional<TokenAcceso> buscarPorCodigo(String codigoToken) {
        String sql = """
                SELECT id, visita_id, codigo_token, tipo_token_id, estado_token_id, fecha_emision, fecha_expiracion
                FROM tokens_acceso
                WHERE codigo_token = ?
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigoToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp tsEmision = rs.getTimestamp("fecha_emision");
                    Timestamp tsExpiracion = rs.getTimestamp("fecha_expiracion");
                    TokenAcceso token = new TokenAcceso(
                            rs.getLong("id"),
                            rs.getInt("visita_id"),
                            rs.getString("codigo_token"),
                            rs.getInt("tipo_token_id"),
                            rs.getInt("estado_token_id"),
                            tsEmision != null ? tsEmision.toLocalDateTime() : null,
                            tsExpiracion != null ? tsExpiracion.toLocalDateTime() : null
                    );
                    return Optional.of(token);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar token de acceso por código: " + codigoToken, e);
        }
    }

    @Override
    public String validarEstadoToken(String codigoToken) {
        String sql = "SELECT fn_validar_estado_token(?)";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigoToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return "TOKEN_NO_EXISTE";
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al validar estado del token: " + codigoToken, e);
        }
    }

    @Override
    public void actualizarEstado(String codigoToken, int nuevoEstadoTokenId) {
        String sql = "UPDATE tokens_acceso SET estado_token_id = ? WHERE codigo_token = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevoEstadoTokenId);
            stmt.setString(2, codigoToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar estado del token: " + codigoToken, e);
        }
    }
}
