package com.zonaacme.sica.domain.entity;

import java.util.Objects;

public class Vehiculo {
    private String placa;
    private String descripcion;

    public Vehiculo() {
    }

    public Vehiculo(String placa, String descripcion) {
        this.placa = placa;
        this.descripcion = descripcion;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
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
        if (!(o instanceof Vehiculo vehiculo)) return false;
        return Objects.equals(placa, vehiculo.placa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placa);
    }

    @Override
    public String toString() {
        return "Vehiculo{" +
                "placa='" + placa + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
