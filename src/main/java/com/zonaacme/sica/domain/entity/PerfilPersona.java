package com.zonaacme.sica.domain.entity;

import java.util.Objects;

public class PerfilPersona {
    private int id;
    private int personaId;
    private int empresaId;
    private int tipoPersonaId;
    private String tipoPersonaNombre;
    private int estadoAccesoId;
    private String estadoAccesoNombre;

    public PerfilPersona() {
    }

    public PerfilPersona(int id, int personaId, int empresaId, int tipoPersonaId, String tipoPersonaNombre, int estadoAccesoId, String estadoAccesoNombre) {
        this.id = id;
        this.personaId = personaId;
        this.empresaId = empresaId;
        this.tipoPersonaId = tipoPersonaId;
        this.tipoPersonaNombre = tipoPersonaNombre;
        this.estadoAccesoId = estadoAccesoId;
        this.estadoAccesoNombre = estadoAccesoNombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPersonaId() {
        return personaId;
    }

    public void setPersonaId(int personaId) {
        this.personaId = personaId;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(int empresaId) {
        this.empresaId = empresaId;
    }

    public int getTipoPersonaId() {
        return tipoPersonaId;
    }

    public void setTipoPersonaId(int tipoPersonaId) {
        this.tipoPersonaId = tipoPersonaId;
    }

    public String getTipoPersonaNombre() {
        return tipoPersonaNombre;
    }

    public void setTipoPersonaNombre(String tipoPersonaNombre) {
        this.tipoPersonaNombre = tipoPersonaNombre;
    }

    public int getEstadoAccesoId() {
        return estadoAccesoId;
    }

    public void setEstadoAccesoId(int estadoAccesoId) {
        this.estadoAccesoId = estadoAccesoId;
    }

    public String getEstadoAccesoNombre() {
        return estadoAccesoNombre;
    }

    public void setEstadoAccesoNombre(String estadoAccesoNombre) {
        this.estadoAccesoNombre = estadoAccesoNombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerfilPersona that)) return false;
        return id == that.id && personaId == that.personaId && empresaId == that.empresaId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personaId, empresaId);
    }

    @Override
    public String toString() {
        return "PerfilPersona{" +
                "id=" + id +
                ", personaId=" + personaId +
                ", empresaId=" + empresaId +
                ", tipoPersonaId=" + tipoPersonaId +
                ", tipoPersonaNombre='" + tipoPersonaNombre + '\'' +
                ", estadoAccesoId=" + estadoAccesoId +
                ", estadoAccesoNombre='" + estadoAccesoNombre + '\'' +
                '}';
    }
}
