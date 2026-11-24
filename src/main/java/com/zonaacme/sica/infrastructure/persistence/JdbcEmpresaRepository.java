package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.ContactoEmpresa;
import com.zonaacme.sica.domain.entity.Empresa;
import com.zonaacme.sica.domain.repository.EmpresaRepository;
import com.zonaacme.sica.infrastructure.database.DataAccessException;
import com.zonaacme.sica.infrastructure.database.DatabaseConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmpresaRepository implements EmpresaRepository {

    private static final int DEFAULT_TIPO_TELEFONO_ID = 1;
    private static final int DEFAULT_TIPO_CORREO_ID = 1;

    private final DatabaseConnectionPool connectionPool;

    public JdbcEmpresaRepository() {
        this(DatabaseConnectionPool.getInstance());
    }

    public JdbcEmpresaRepository(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Empresa guardar(Empresa empresa) {
        String sql = "INSERT INTO empresas (nombre) VALUES (?)";
        try (Connection conn = connectionPool.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, empresa.getNombre());
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            empresa.setId(keys.getInt(1));
                        }
                    }
                }

                if (empresa.getContactos() != null && !empresa.getContactos().isEmpty()) {
                    for (ContactoEmpresa contacto : empresa.getContactos()) {
                        contacto.setEmpresaId(empresa.getId());
                        guardarContactoEnTransaccion(conn, contacto);
                    }
                }

                conn.commit();
                return empresa;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar empresa: " + empresa.getNombre(), e);
        }
    }

    @Override
    public void actualizar(Empresa empresa) {
        String sql = "UPDATE empresas SET nombre = ? WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empresa.getNombre());
            stmt.setInt(2, empresa.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar empresa con id: " + empresa.getId(), e);
        }
    }

    @Override
    public Optional<Empresa> buscarPorId(int id) {
        String sql = "SELECT id, nombre FROM empresas WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Empresa empresa = new Empresa(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            cargarContactosPorEmpresa(conn, id)
                    );
                    return Optional.of(empresa);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar empresa por id: " + id, e);
        }
    }

    @Override
    public List<Empresa> listarTodas() {
        String sql = "SELECT id, nombre FROM empresas ORDER BY id";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int empresaId = rs.getInt("id");
                Empresa empresa = new Empresa(
                        empresaId,
                        rs.getString("nombre"),
                        cargarContactosPorEmpresa(conn, empresaId)
                );
                empresas.add(empresa);
            }
            return empresas;
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar todas las empresas.", e);
        }
    }

    @Override
    public ContactoEmpresa guardarContacto(ContactoEmpresa contacto) {
        try (Connection conn = connectionPool.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                guardarContactoEnTransaccion(conn, contacto);
                conn.commit();
                return contacto;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar contacto de empresa: " + contacto.getNombreContacto(), e);
        }
    }

    private void guardarContactoEnTransaccion(Connection conn, ContactoEmpresa contacto) throws SQLException {
        String sqlContacto = "INSERT INTO contactos_empresa (empresa_id, nombre_contacto, cargo) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlContacto, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, contacto.getEmpresaId());
            stmt.setString(2, contacto.getNombreContacto());
            stmt.setString(3, contacto.getCargo());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    contacto.setId(keys.getInt(1));
                }
            }
        }

        if (contacto.getTelefonos() != null && !contacto.getTelefonos().isEmpty()) {
            String sqlTel = "INSERT INTO telefonos_contacto (contacto_id, numero_telefono, tipo_telefono_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmtTel = conn.prepareStatement(sqlTel)) {
                for (String tel : contacto.getTelefonos()) {
                    if (tel != null && !tel.isBlank()) {
                        stmtTel.setInt(1, contacto.getId());
                        stmtTel.setString(2, tel.trim());
                        stmtTel.setInt(3, DEFAULT_TIPO_TELEFONO_ID);
                        stmtTel.addBatch();
                    }
                }
                stmtTel.executeBatch();
            }
        }

        if (contacto.getCorreos() != null && !contacto.getCorreos().isEmpty()) {
            String sqlCorreo = "INSERT INTO correos_contacto (contacto_id, email, tipo_correo_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmtCorreo = conn.prepareStatement(sqlCorreo)) {
                for (String correo : contacto.getCorreos()) {
                    if (correo != null && !correo.isBlank()) {
                        stmtCorreo.setInt(1, contacto.getId());
                        stmtCorreo.setString(2, correo.trim());
                        stmtCorreo.setInt(3, DEFAULT_TIPO_CORREO_ID);
                        stmtCorreo.addBatch();
                    }
                }
                stmtCorreo.executeBatch();
            }
        }
    }

    private List<ContactoEmpresa> cargarContactosPorEmpresa(Connection conn, int empresaId) throws SQLException {
        String sql = "SELECT id, empresa_id, nombre_contacto, cargo FROM contactos_empresa WHERE empresa_id = ? ORDER BY id";
        List<ContactoEmpresa> contactos = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empresaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int contactoId = rs.getInt("id");
                    List<String> telefonos = cargarTelefonosPorContacto(conn, contactoId);
                    List<String> correos = cargarCorreosPorContacto(conn, contactoId);
                    contactos.add(new ContactoEmpresa(
                            contactoId,
                            rs.getInt("empresa_id"),
                            rs.getString("nombre_contacto"),
                            rs.getString("cargo"),
                            telefonos,
                            correos
                    ));
                }
            }
        }
        return contactos;
    }

    private List<String> cargarTelefonosPorContacto(Connection conn, int contactoId) throws SQLException {
        String sql = "SELECT numero_telefono FROM telefonos_contacto WHERE contacto_id = ? ORDER BY id";
        List<String> telefonos = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contactoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    telefonos.add(rs.getString("numero_telefono"));
                }
            }
        }
        return telefonos;
    }

    private List<String> cargarCorreosPorContacto(Connection conn, int contactoId) throws SQLException {
        String sql = "SELECT email FROM correos_contacto WHERE contacto_id = ? ORDER BY id";
        List<String> correos = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contactoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    correos.add(rs.getString("email"));
                }
            }
        }
        return correos;
    }
}
