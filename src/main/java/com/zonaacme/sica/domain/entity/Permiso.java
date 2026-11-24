package com.zonaacme.sica.domain.entity;

import java.util.Objects;

public class Permiso {
    private int id;
    private String nombrePermiso;
    private String descripcion;

    public Permiso() {
    }

    public Permiso(int id, String nombrePermiso, String descripcion) {
        this.id = id;
        this.nombrePermiso = nombrePermiso;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombrePermiso() {
        return nombrePermiso;
    }

    public void setNombrePermiso(String nombrePermiso) {
        this.nombrePermiso = nombrePermiso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permiso permiso)) return false;
        return id == permiso.id && Objects.equals(nombrePermiso, permiso.nombrePermiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombrePermiso);
    }

    @Override
    public String toString() {
        return "Permiso{" +
                "id=" + id +
                ", nombrePermiso='" + nombrePermiso + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
