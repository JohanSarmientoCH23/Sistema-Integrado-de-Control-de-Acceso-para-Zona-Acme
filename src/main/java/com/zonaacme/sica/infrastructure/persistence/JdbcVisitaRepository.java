package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.AforoActual;
import com.zonaacme.sica.domain.entity.Visita;
import com.zonaacme.sica.domain.entity.VisitaPendiente;
import com.zonaacme.sica.domain.repository.VisitaRepository;
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
import java.util.Optional;

public class JdbcVisitaRepository implements VisitaRepository {

    private static final int ESTADO_CERRADA_POR_SISTEMA_ID = 6;

    private final DatabaseConnectionPool connectionPool;

    public JdbcVisitaRepository() {
        this(DatabaseConnectionPool.getInstance());
    }

    public JdbcVisitaRepository(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Visita guardar(Visita visita) {
        String sqlVisita = """
                INSERT INTO visitas (persona_id, fecha_entrada, fecha_salida, estado_visita_id,
                                     visita_aprobada_por, metodo_ingreso_id, metodo_salida_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String sqlEnsureVehiculo = "INSERT IGNORE INTO vehiculos (placa, descripcion) VALUES (?, 'Vehiculo registrado en visita')";
        String sqlVisitaVehiculo = "INSERT INTO visita_vehiculo (visita_id, vehiculo_placa) VALUES (?, ?)";

        try (Connection conn = connectionPool.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlVisita, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, visita.getPersonaId());
                    stmt.setTimestamp(2, visita.getFechaEntrada() != null ? Timestamp.valueOf(visita.getFechaEntrada()) : Timestamp.valueOf(LocalDateTime.now()));
                    if (visita.getFechaSalida() != null) {
                        stmt.setTimestamp(3, Timestamp.valueOf(visita.getFechaSalida()));
                    } else {
                        stmt.setNull(3, Types.TIMESTAMP);
                    }
                    stmt.setInt(4, visita.getEstadoVisitaId());
                    if (visita.getVisitaAprobadaPor() != null) {
                        stmt.setInt(5, visita.getVisitaAprobadaPor());
                    } else {
                        stmt.setNull(5, Types.INTEGER);
                    }
                    stmt.setInt(6, visita.getMetodoIngresoId());
                    if (visita.getMetodoSalidaId() != null) {
                        stmt.setInt(7, visita.getMetodoSalidaId());
                    } else {
                        stmt.setNull(7, Types.INTEGER);
                    }
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            visita.setId(keys.getInt(1));
                        }
                    }
                }

                if (visita.getVehiculosPlacas() != null && !visita.getVehiculosPlacas().isEmpty()) {
                    try (PreparedStatement stmtVeh = conn.prepareStatement(sqlEnsureVehiculo);
                         PreparedStatement stmtRel = conn.prepareStatement(sqlVisitaVehiculo)) {
                        for (String placa : visita.getVehiculosPlacas()) {
                            if (placa != null && !placa.isBlank()) {
                                String placaLimpia = placa.trim();
                                stmtVeh.setString(1, placaLimpia);
                                stmtVeh.addBatch();

                                stmtRel.setInt(1, visita.getId());
                                stmtRel.setString(2, placaLimpia);
                                stmtRel.addBatch();
                            }
                        }
                        stmtVeh.executeBatch();
                        stmtRel.executeBatch();
                    }
                }

                conn.commit();
                return visita;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar visita para persona id: " + visita.getPersonaId(), e);
        }
    }

    @Override
    public void actualizar(Visita visita) {
        String sql = """
                UPDATE visitas
                SET persona_id = ?, fecha_entrada = ?, fecha_salida = ?, estado_visita_id = ?,
                    visita_aprobada_por = ?, metodo_ingreso_id = ?, metodo_salida_id = ?
                WHERE id = ?
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visita.getPersonaId());
            stmt.setTimestamp(2, visita.getFechaEntrada() != null ? Timestamp.valueOf(visita.getFechaEntrada()) : null);
            if (visita.getFechaSalida() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(visita.getFechaSalida()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            stmt.setInt(4, visita.getEstadoVisitaId());
            if (visita.getVisitaAprobadaPor() != null) {
                stmt.setInt(5, visita.getVisitaAprobadaPor());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, visita.getMetodoIngresoId());
            if (visita.getMetodoSalidaId() != null) {
                stmt.setInt(7, visita.getMetodoSalidaId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setInt(8, visita.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar visita con id: " + visita.getId(), e);
        }
    }

    @Override
    public Optional<Visita> buscarPorId(int id) {
        String sql = """
                SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida,
                       v.estado_visita_id, ve.nombre_estado AS estado_visita_nombre,
                       v.visita_aprobada_por, v.metodo_ingreso_id, v.metodo_salida_id
                FROM visitas v
                JOIN visita_estados ve ON v.estado_visita_id = ve.id
                WHERE v.id = ?
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp tsEntrada = rs.getTimestamp("fecha_entrada");
                    Timestamp tsSalida = rs.getTimestamp("fecha_salida");
                    int aprobadaPor = rs.getInt("visita_aprobada_por");
                    Integer visitaAprobadaPor = rs.wasNull() ? null : aprobadaPor;
                    int salidaId = rs.getInt("metodo_salida_id");
                    Integer metodoSalidaId = rs.wasNull() ? null : salidaId;

                    List<String> placas = cargarPlacasPorVisita(conn, id);

                    Visita visita = new Visita(
                            rs.getInt("id"),
                            rs.getInt("persona_id"),
                            tsEntrada != null ? tsEntrada.toLocalDateTime() : null,
                            tsSalida != null ? tsSalida.toLocalDateTime() : null,
                            rs.getInt("estado_visita_id"),
                            rs.getString("estado_visita_nombre"),
                            visitaAprobadaPor,
                            rs.getInt("metodo_ingreso_id"),
                            metodoSalidaId,
                            placas
                    );
                    return Optional.of(visita);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar visita por id: " + id, e);
        }
    }

    @Override
    public boolean tieneVisitaAbierta(int personaId) {
        String sql = "SELECT fn_tiene_visita_abierta(?)";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, personaId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al verificar si tiene visita abierta la persona id: " + personaId, e);
        }
    }

    @Override
    public void cerrarVisitaPorSistema(int visitaId) {
        String sql = "UPDATE visitas SET fecha_salida = ?, estado_visita_id = ? WHERE id = ?";
        try (Connection conn = connectionPool.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(2, ESTADO_CERRADA_POR_SISTEMA_ID);
                stmt.setInt(3, visitaId);
                stmt.executeUpdate();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al cerrar visita por sistema con id: " + visitaId, e);
        }
    }

    @Override
    public List<AforoActual> listarAforoActual() {
        String sql = "SELECT * FROM vw_aforo_actual";
        List<AforoActual> aforo = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp tsEntrada = rs.getTimestamp("fecha_entrada");
                aforo.add(new AforoActual(
                        rs.getInt("visita_id"),
                        rs.getString("nombre_persona"),
                        rs.getString("documento_identidad"),
                        rs.getString("empresa"),
                        rs.getString("tipo_persona"),
                        tsEntrada != null ? tsEntrada.toLocalDateTime() : null,
                        rs.getString("metodo_ingreso")
                ));
            }
            return aforo;
        } catch (SQLException e) {
            throw new DataAccessException("Error al consultar vw_aforo_actual.", e);
        }
    }

    @Override
    public List<VisitaPendiente> listarVisitasPendientes() {
        String sql = "SELECT * FROM vw_visitas_pendientes";
        List<VisitaPendiente> pendientes = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp tsSolicitud = rs.getTimestamp("hora_solicitud");
                pendientes.add(new VisitaPendiente(
                        rs.getInt("visita_id"),
                        rs.getString("visitante"),
                        rs.getString("documento_identidad"),
                        rs.getString("empresa_destino"),
                        tsSolicitud != null ? tsSolicitud.toLocalDateTime() : null
                ));
            }
            return pendientes;
        } catch (SQLException e) {
            throw new DataAccessException("Error al consultar vw_visitas_pendientes.", e);
        }
    }

    private List<String> cargarPlacasPorVisita(Connection conn, int visitaId) throws SQLException {
        String sql = "SELECT vehiculo_placa FROM visita_vehiculo WHERE visita_id = ?";
        List<String> placas = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    placas.add(rs.getString("vehiculo_placa"));
                }
            }
        }
        return placas;
    }
}
