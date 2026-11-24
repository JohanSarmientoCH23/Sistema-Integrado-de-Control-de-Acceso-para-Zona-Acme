package com.zonaacme.sica.domain.entity;

import java.util.Objects;

public class Persona {
    private int id;
    private String nombre;
    private String documentoIdentidad;
    private String urlFoto;
    private String vectorBiometrico;

    public Persona() {
    }

    public Persona(int id, String nombre, String documentoIdentidad, String urlFoto, String vectorBiometrico) {
        this.id = id;
        this.nombre = nombre;
        this.documentoIdentidad = documentoIdentidad;
        this.urlFoto = urlFoto;
        this.vectorBiometrico = vectorBiometrico;
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

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public String getVectorBiometrico() {
        return vectorBiometrico;
    }

    public void setVectorBiometrico(String vectorBiometrico) {
        this.vectorBiometrico = vectorBiometrico;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Persona persona)) return false;
        return id == persona.id && Objects.equals(documentoIdentidad, persona.documentoIdentidad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentoIdentidad);
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", documentoIdentidad='" + documentoIdentidad + '\'' +
                ", urlFoto='" + urlFoto + '\'' +
                ", vectorBiometrico='" + vectorBiometrico + '\'' +
                '}';
    }
}
