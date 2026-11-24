package com.zonaacme.sica.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Empresa {
    private int id;
    private String nombre;
    private List<ContactoEmpresa> contactos;

    public Empresa() {
        this.contactos = new ArrayList<>();
    }

    public Empresa(int id, String nombre, List<ContactoEmpresa> contactos) {
        this.id = id;
        this.nombre = nombre;
        this.contactos = contactos != null ? new ArrayList<>(contactos) : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<ContactoEmpresa> getContactos() {
        return contactos;
    }

    public void setContactos(List<ContactoEmpresa> contactos) {
        this.contactos = contactos != null ? new ArrayList<>(contactos) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Empresa empresa)) return false;
        return id == empresa.id && Objects.equals(nombre, empresa.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", contactos=" + contactos +
                '}';
    }
}
