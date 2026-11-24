package com.zonaacme.sica.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactoEmpresa {
    private int id;
    private int empresaId;
    private String nombreContacto;
    private String cargo;
    private List<String> telefonos;
    private List<String> correos;

    public ContactoEmpresa() {
        this.telefonos = new ArrayList<>();
        this.correos = new ArrayList<>();
    }

    public ContactoEmpresa(int id, int empresaId, String nombreContacto, String cargo, List<String> telefonos, List<String> correos) {
        this.id = id;
        this.empresaId = empresaId;
        this.nombreContacto = nombreContacto;
        this.cargo = cargo;
        this.telefonos = telefonos != null ? new ArrayList<>(telefonos) : new ArrayList<>();
        this.correos = correos != null ? new ArrayList<>(correos) : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(int empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public List<String> getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(List<String> telefonos) {
        this.telefonos = telefonos != null ? new ArrayList<>(telefonos) : new ArrayList<>();
    }

    public List<String> getCorreos() {
        return correos;
    }

    public void setCorreos(List<String> correos) {
        this.correos = correos != null ? new ArrayList<>(correos) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactoEmpresa that)) return false;
        return id == that.id && empresaId == that.empresaId && Objects.equals(nombreContacto, that.nombreContacto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, empresaId, nombreContacto);
    }

    @Override
    public String toString() {
        return "ContactoEmpresa{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", nombreContacto='" + nombreContacto + '\'' +
                ", cargo='" + cargo + '\'' +
                ", telefonos=" + telefonos +
                ", correos=" + correos +
                '}';
    }
}
