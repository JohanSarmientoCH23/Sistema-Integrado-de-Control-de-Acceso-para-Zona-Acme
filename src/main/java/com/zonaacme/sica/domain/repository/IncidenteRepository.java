package com.zonaacme.sica.domain.repository;

import com.zonaacme.sica.domain.entity.Incidente;

import java.util.List;

public interface IncidenteRepository {
    Incidente guardar(Incidente incidente);
    List<Incidente> listarPorVisita(int visitaId);
    List<Incidente> listarTodos();
}
