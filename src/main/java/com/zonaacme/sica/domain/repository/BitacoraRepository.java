package com.zonaacme.sica.domain.repository;

import com.zonaacme.sica.domain.entity.BitacoraAuditoria;

import java.util.List;

public interface BitacoraRepository {
    BitacoraAuditoria registrar(BitacoraAuditoria bitacora);
    List<BitacoraAuditoria> listarUltimos(int limite);
}
