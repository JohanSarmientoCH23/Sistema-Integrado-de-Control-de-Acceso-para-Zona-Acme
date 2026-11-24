package com.zonaacme.sica.infrastructure.persistence;

import com.zonaacme.sica.domain.entity.Permiso;
import com.zonaacme.sica.domain.entity.Rol;
import com.zonaacme.sica.domain.entity.Usuario;
import com.zonaacme.sica.domain.repository.UsuarioRepository;
import com.zonaacme.sica.infrastructure.database.DataAccessException;
import com.zonaacme.sica.infrastructure.database.DatabaseConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcUsuarioRepository implements UsuarioRepository {

    private final DatabaseConnectionPool connectionPool;

    public JdbcUsuarioRepository() {
        this(DatabaseConnectionPool.getInstance());
    }

    public JdbcUsuarioRepository(DatabaseConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT id, nombre, email, password, esta_activo FROM usuarios WHERE email = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuarioBase(rs);
                    usuario.setRoles(obtenerRolesConPermisosInterno(conn, usuario.getId()));
                    return Optional.of(usuario);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar usuario por email: " + email, e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        String sql = "SELECT id, nombre, email, password, esta_activo FROM usuarios WHERE id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuarioBase(rs);
                    usuario.setRoles(obtenerRolesConPermisosInterno(conn, usuario.getId()));
                    return Optional.of(usuario);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar usuario por id: " + id, e);
        }
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        String sqlUsuario = "INSERT INTO usuarios (nombre, email, password, esta_activo) VALUES (?, ?, ?, ?)";
        String sqlRol = "INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (?, ?)";
        try (Connection conn = connectionPool.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, usuario.getNombre());
                    stmt.setString(2, usuario.getEmail());
                    stmt.setString(3, usuario.getPassword());
                    stmt.setBoolean(4, usuario.isEstaActivo());
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            usuario.setId(keys.getInt(1));
                        }
                    }
                }

                if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                    try (PreparedStatement stmtRol = conn.prepareStatement(sqlRol)) {
                        for (Rol rol : usuario.getRoles()) {
                            stmtRol.setInt(1, usuario.getId());
                            stmtRol.setInt(2, rol.getId());
                            stmtRol.addBatch();
                        }
                        stmtRol.executeBatch();
                    }
                }

                conn.commit();
                return usuario;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar usuario: " + usuario.getEmail(), e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sqlUpdate = "UPDATE usuarios SET nombre = ?, email = ?, password = ?, esta_activo = ? WHERE id = ?";
        String sqlDeleteRoles = "DELETE FROM usuario_roles WHERE usuario_id = ?";
        String sqlInsertRol = "INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (?, ?)";

        try (Connection conn = connectionPool.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setString(1, usuario.getNombre());
                    stmt.setString(2, usuario.getEmail());
                    stmt.setString(3, usuario.getPassword());
                    stmt.setBoolean(4, usuario.isEstaActivo());
                    stmt.setInt(5, usuario.getId());
                    stmt.executeUpdate();
                }

                if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                    try (PreparedStatement stmtDel = conn.prepareStatement(sqlDeleteRoles)) {
                        stmtDel.setInt(1, usuario.getId());
                        stmtDel.executeUpdate();
                    }
                    try (PreparedStatement stmtIns = conn.prepareStatement(sqlInsertRol)) {
                        for (Rol rol : usuario.getRoles()) {
                            stmtIns.setInt(1, usuario.getId());
                            stmtIns.setInt(2, rol.getId());
                            stmtIns.addBatch();
                        }
                        stmtIns.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar usuario con id: " + usuario.getId(), e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT id, nombre, email, password, esta_activo FROM usuarios ORDER BY id";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = mapearUsuarioBase(rs);
                usuario.setRoles(obtenerRolesConPermisosInterno(conn, usuario.getId()));
                usuarios.add(usuario);
            }
            return usuarios;
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar todos los usuarios.", e);
        }
    }

    @Override
    public List<Rol> obtenerRolesConPermisos(int usuarioId) {
        try (Connection conn = connectionPool.getConnection()) {
            return obtenerRolesConPermisosInterno(conn, usuarioId);
        } catch (SQLException e) {
            throw new DataAccessException("Error al obtener roles y permisos para el usuario id: " + usuarioId, e);
        }
    }

    private List<Rol> obtenerRolesConPermisosInterno(Connection conn, int usuarioId) throws SQLException {
        String sql = """
                SELECT r.id AS rol_id, r.nombre_rol,
                       p.id AS permiso_id, p.nombre_permiso, p.descripcion
                FROM usuario_roles ur
                JOIN roles r ON ur.rol_id = r.id
                LEFT JOIN rol_permisos rp ON r.id = rp.rol_id
                LEFT JOIN permisos p ON rp.permiso_id = p.id
                WHERE ur.usuario_id = ?
                ORDER BY r.id, p.id
                """;
        Map<Integer, Rol> rolesMap = new LinkedHashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int rolId = rs.getInt("rol_id");
                    Rol rol = rolesMap.computeIfAbsent(rolId, id -> {
                        try {
                            return new Rol(id, rs.getString("nombre_rol"), new ArrayList<>());
                        } catch (SQLException e) {
                            throw new DataAccessException("Error mapeando rol.", e);
                        }
                    });
                    int permisoId = rs.getInt("permiso_id");
                    if (!rs.wasNull() && permisoId > 0) {
                        Permiso permiso = new Permiso(
                                permisoId,
                                rs.getString("nombre_permiso"),
                                rs.getString("descripcion")
                        );
                        rol.getPermisos().add(permiso);
                    }
                }
            }
        }
        return new ArrayList<>(rolesMap.values());
    }

    private Usuario mapearUsuarioBase(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getBoolean("esta_activo"),
                new ArrayList<>()
        );
    }
}
