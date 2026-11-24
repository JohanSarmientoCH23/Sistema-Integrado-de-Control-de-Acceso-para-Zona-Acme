package com.zonaacme.sica.domain.repository;

import com.zonaacme.sica.domain.entity.PerfilCompleto;
import com.zonaacme.sica.domain.entity.PerfilPersona;
import com.zonaacme.sica.domain.entity.Persona;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository {
    Persona guardar(Persona persona, PerfilPersona perfil);
    Optional<Persona> buscarPorDocumento(String documentoIdentidad);
    Optional<Persona> buscarPorId(int id);
    List<PerfilCompleto> listarPerfilesCompletos();
    Optional<PerfilCompleto> buscarPerfilCompletoPorDocumento(String documentoIdentidad);
    List<Persona> listarConBiometria();
    void actualizarEstadoAcceso(int personaId, int nuevoEstadoAccesoId);
}
