package com.zonaacme.sica.domain.repository;

import com.zonaacme.sica.domain.entity.Rol;
import com.zonaacme.sica.domain.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorId(int id);
    Usuario guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    List<Usuario> listarTodos();
    List<Rol> obtenerRolesConPermisos(int usuarioId);
}
