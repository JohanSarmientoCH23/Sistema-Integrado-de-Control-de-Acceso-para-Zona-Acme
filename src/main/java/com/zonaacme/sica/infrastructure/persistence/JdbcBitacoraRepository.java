package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.BitacoraAuditoria;
import com.zonaacme.sica.domain.repository.BitacoraRepository;
import com.zonaacme.sica.infrastructure.database.DataAccessException;
import com.zonaacme.sica.infrastructure.database.DatabaseConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcBitacoraRepository implements BitacoraRepository {

    private final DatabaseConnectionPool connectionPool;

    public JdbcBitacoraRepository() {
        this(DatabaseConnectionPool.getInstance());
    }

    public JdbcBitacoraRepository(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public BitacoraAuditoria registrar(BitacoraAuditoria bitacora) {
        String sql = """
                INSERT INTO bitacora_auditoria (usuario_id, fecha_hora, accion_realizada, tabla_afectada, registro_id_afectado, detalles)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime fechaHora = bitacora.getFechaHora() != null ? bitacora.getFechaHora() : LocalDateTime.now();
            String detallesJson = normalizarJsonValido(bitacora.getDetallesJson());

            if (bitacora.getUsuarioId() != null && bitacora.getUsuarioId() > 0) {
                stmt.setInt(1, bitacora.getUsuarioId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setTimestamp(2, Timestamp.valueOf(fechaHora));
            stmt.setString(3, bitacora.getAccionRealizada());
            stmt.setString(4, bitacora.getTablaAfectada());
            if (bitacora.getRegistroIdAfectado() != null) {
                stmt.setInt(5, bitacora.getRegistroIdAfectado());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, detallesJson);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    bitacora.setId(keys.getLong(1));
                }
            }
            bitacora.setFechaHora(fechaHora);
            bitacora.setDetallesJson(detallesJson);
            return bitacora;
        } catch (SQLException e) {
            throw new DataAccessException("Error al registrar entrada en bitácora de auditoría: " + bitacora.getAccionRealizada(), e);
        }
    }

    @Override
    public List<BitacoraAuditoria> listarUltimos(int limite) {
        String sql = """
                SELECT id, usuario_id, fecha_hora, accion_realizada, tabla_afectada, registro_id_afectado, detalles
                FROM bitacora_auditoria
                ORDER BY fecha_hora DESC, id DESC
                LIMIT ?
                """;
        List<BitacoraAuditoria> registros = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Math.max(1, limite));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int rawUsuarioId = rs.getInt("usuario_id");
                    Integer usuarioId = rs.wasNull() ? null : rawUsuarioId;
                    int rawRegistroId = rs.getInt("registro_id_afectado");
                    Integer registroId = rs.wasNull() ? null : rawRegistroId;
                    Timestamp ts = rs.getTimestamp("fecha_hora");

                    registros.add(new BitacoraAuditoria(
                            rs.getLong("id"),
                            usuarioId,
                            ts != null ? ts.toLocalDateTime() : null,
                            rs.getString("accion_realizada"),
                            rs.getString("tabla_afectada"),
                            registroId,
                            rs.getString("detalles")
                    ));
                }
            }
            return registros;
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar últimos registros de bitácora.", e);
        }
    }

    private String normalizarJsonValido(String raw) {
        if (raw == null || raw.isBlank()) {
            return "{}";
        }
        String trimmed = raw.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return trimmed;
        }
        String escaped = trimmed.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"detalle\": \"" + escaped + "\"}";
    }
}
