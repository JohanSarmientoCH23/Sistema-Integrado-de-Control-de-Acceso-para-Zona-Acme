package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.Incidente;
import com.zonaacme.sica.domain.repository.IncidenteRepository;
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

public class JdbcIncidenteRepository implements IncidenteRepository {

    private final DatabaseConnectionPool connectionPool;

    public JdbcIncidenteRepository() {
        this(DatabaseConnectionPool.getInstance());
    }

    public JdbcIncidenteRepository(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Incidente guardar(Incidente incidente) {
        String sql = "INSERT INTO incidentes (visita_id, reportado_por_id, fecha, descripcion) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime fecha = incidente.getFecha() != null ? incidente.getFecha() : LocalDateTime.now();
            if (incidente.getVisitaId() != null && incidente.getVisitaId() > 0) {
                stmt.setInt(1, incidente.getVisitaId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, incidente.getReportadoPorId());
            stmt.setTimestamp(3, Timestamp.valueOf(fecha));
            stmt.setString(4, incidente.getDescripcion());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    incidente.setId(keys.getInt(1));
                }
            }
            incidente.setFecha(fecha);
            return incidente;
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar incidente de seguridad.", e);
        }
    }

    @Override
    public List<Incidente> listarPorVisita(int visitaId) {
        String sql = "SELECT id, visita_id, reportado_por_id, fecha, descripcion FROM incidentes WHERE visita_id = ? ORDER BY fecha DESC";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    incidentes.add(mapearIncidente(rs));
                }
            }
            return incidentes;
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar incidentes para la visita id: " + visitaId, e);
        }
    }

    @Override
    public List<Incidente> listarTodos() {
        String sql = "SELECT id, visita_id, reportado_por_id, fecha, descripcion FROM incidentes ORDER BY fecha DESC";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                incidentes.add(mapearIncidente(rs));
            }
            return incidentes;
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar todos los incidentes.", e);
        }
    }

    private Incidente mapearIncidente(ResultSet rs) throws SQLException {
        int rawVisitaId = rs.getInt("visita_id");
        Integer visitaId = rs.wasNull() ? null : rawVisitaId;
        Timestamp tsFecha = rs.getTimestamp("fecha");
        return new Incidente(
                rs.getInt("id"),
                visitaId,
                rs.getInt("reportado_por_id"),
                tsFecha != null ? tsFecha.toLocalDateTime() : null,
                rs.getString("descripcion")
        );
    }
}
