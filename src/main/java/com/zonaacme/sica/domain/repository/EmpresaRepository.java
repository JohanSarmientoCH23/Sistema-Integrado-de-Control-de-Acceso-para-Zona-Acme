package com.zonaacme.sica.domain.repository;

import com.zonaacme.sica.domain.entity.ContactoEmpresa;
import com.zonaacme.sica.domain.entity.Empresa;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {
    Empresa guardar(Empresa empresa);
    void actualizar(Empresa empresa);
    Optional<Empresa> buscarPorId(int id);
    List<Empresa> listarTodas();
    ContactoEmpresa guardarContacto(ContactoEmpresa contacto);
}
