package com.zonaacme.sica.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rol {
    private int id;
    private String nombreRol;
    private List<Permiso> permisos;

    public Rol() {
        this.permisos = new ArrayList<>();
    }

    public Rol(int id, String nombreRol, List<Permiso> permisos) {
        this.id = id;
        this.nombreRol = nombreRol;
        this.permisos = permisos != null ? new ArrayList<>(permisos) : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public List<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos != null ? new ArrayList<>(permisos) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rol rol)) return false;
        return id == rol.id && Objects.equals(nombreRol, rol.nombreRol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombreRol);
    }

    @Override
    public String toString() {
        return "Rol{" +
                "id=" + id +
                ", nombreRol='" + nombreRol + '\'' +
                ", permisos=" + permisos +
                '}';
    }
}
