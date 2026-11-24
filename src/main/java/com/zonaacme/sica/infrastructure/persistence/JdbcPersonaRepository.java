package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.PerfilPersona;
import com.zonaacme.sica.domain.entity.Persona;
import com.zonaacme.sica.domain.repository.PersonaRepository;
import com.zonaacme.sica.infrastructure.database.DataAccessException;
import com.zonaacme.sica.infrastructure.database.DatabaseConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPersonaRepository implements PersonaRepository {

    private final DatabaseConnectionPool connectionPool;

    public JdbcPersonaRepository() {
        this(DatabaseConnectionPool.getInstance());
    }

    public JdbcPersonaRepository(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Persona guardar(Persona persona, PerfilPersona perfil) {
        String sqlPersona = "INSERT INTO personas (nombre, documento_identidad, url_foto, vector_biometrico) VALUES (?, ?, ?, ?)";
        String sqlPerfil = "INSERT INTO perfiles_persona (persona_id, empresa_id, tipo_persona_id, estado_acceso_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionPool.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmtPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                    stmtPersona.setString(1, persona.getNombre());
                    stmtPersona.setString(2, persona.getDocumentoIdentidad());
                    stmtPersona.setString(3, persona.getUrlFoto());
                    if (persona.getVectorBiometrico() != null && !persona.getVectorBiometrico().isBlank()) {
                        stmtPersona.setString(4, persona.getVectorBiometrico().trim());
                    } else {
                        stmtPersona.setNull(4, Types.VARCHAR);
                    }
                    stmtPersona.executeUpdate();
                    try (ResultSet keys = stmtPersona.getGeneratedKeys()) {
                        if (keys.next()) {
                            persona.setId(keys.getInt(1));
                        }
                    }
                }

                if (perfil != null) {
                    perfil.setPersonaId(persona.getId());
                    try (PreparedStatement stmtPerfil = conn.prepareStatement(sqlPerfil, Statement.RETURN_GENERATED_KEYS)) {
                        stmtPerfil.setInt(1, persona.getId());
                        stmtPerfil.setInt(2, perfil.getEmpresaId());
                        stmtPerfil.setInt(3, perfil.getTipoPersonaId());
                        stmtPerfil.setInt(4, perfil.getEstadoAccesoId());
                        stmtPerfil.executeUpdate();
                        try (ResultSet keys = stmtPerfil.getGeneratedKeys()) {
                            if (keys.next()) {
                                perfil.setId(keys.getInt(1));
                            }
                        }
                    }
                }

                conn.commit();
                return persona;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar persona con documento: " + persona.getDocumentoIdentidad(), e);
        }
    }

    @Override
    public Optional<Persona> buscarPorDocumento(String documentoIdentidad) {
        String sql = "SELECT id, nombre, documento_identidad, url_foto, vector_biometrico FROM personas WHERE documento_identidad = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documentoIdentidad);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPersona(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar persona por documento: " + documentoIdentidad, e);
        }
    }

    @Override
    public Optional<Persona> buscarPorId(int id) {
        String sql = "SELECT id, nombre, documento_identidad, url_foto, vector_biometrico FROM personas WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPersona(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar persona por id: " + id, e);
        }
    }

    @Override
    public List<PerfilCompleto> listarPerfilesCompletos() {
        String sql = """
                SELECT persona_id, nombre_persona, documento_identidad, url_foto,
                       vector_biometrico, empresa_id, nombre_empresa, tipo_persona, estado_acceso
                FROM vw_perfiles_completos
                ORDER BY persona_id
                """;
        List<PerfilCompleto> perfiles = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                perfiles.add(mapearPerfilCompleto(rs));
            }
            return perfiles;
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar perfiles completos desde vw_perfiles_completos.", e);
        }
    }

    @Override
    public Optional<PerfilCompleto> buscarPerfilCompletoPorDocumento(String documentoIdentidad) {
        String sql = """
                SELECT persona_id, nombre_persona, documento_identidad, url_foto,
                       vector_biometrico, empresa_id, nombre_empresa, tipo_persona, estado_acceso
                FROM vw_perfiles_completos
                WHERE documento_identidad = ?
                """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documentoIdentidad);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPerfilCompleto(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar perfil completo por documento: " + documentoIdentidad, e);
        }
    }

    @Override
    public List<Persona> listarConBiometria() {
        String sql = """
                SELECT id, nombre, documento_identidad, url_foto, vector_biometrico
                FROM personas
                WHERE vector_biometrico IS NOT NULL
                ORDER BY id
                """;
        List<Persona> personas = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                personas.add(mapearPersona(rs));
            }
            return personas;
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar personas con biometría.", e);
        }
    }

    @Override
    public void actualizarEstadoAcceso(int personaId, int nuevoEstadoAccesoId) {
        String sql = "UPDATE perfiles_persona SET estado_acceso_id = ? WHERE persona_id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevoEstadoAccesoId);
            stmt.setInt(2, personaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar estado de acceso para persona id: " + personaId, e);
        }
    }

    private Persona mapearPersona(ResultSet rs) throws SQLException {
        return new Persona(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("documento_identidad"),
                rs.getString("url_foto"),
                rs.getString("vector_biometrico")
        );
    }

    private PerfilCompleto mapearPerfilCompleto(ResultSet rs) throws SQLException {
        return new PerfilCompleto(
                rs.getInt("persona_id"),
                rs.getString("nombre_persona"),
                rs.getString("documento_identidad"),
                rs.getString("url_foto"),
                rs.getString("vector_biometrico"),
                rs.getInt("empresa_id"),
                rs.getString("nombre_empresa"),
                rs.getString("tipo_persona"),
                rs.getString("estado_acceso")
        );
    }
}
