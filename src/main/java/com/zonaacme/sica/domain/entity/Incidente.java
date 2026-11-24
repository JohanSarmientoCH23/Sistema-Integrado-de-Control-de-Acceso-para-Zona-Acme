package com.zonaacme.sica.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class Incidente {
    private int id;
    private Integer visitaId;
    private int reportadoPorId;
    private LocalDateTime fecha;
    private String descripcion;

    public Incidente() {
    }

    public Incidente(int id, Integer visitaId, int reportadoPorId, LocalDateTime fecha, String descripcion) {
        this.id = id;
        this.visitaId = visitaId;
        this.reportadoPorId = reportadoPorId;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getVisitaId() {
        return visitaId;
    }

    public void setVisitaId(Integer visitaId) {
        this.visitaId = visitaId;
    }

    public int getReportadoPorId() {
        return reportadoPorId;
    }

    public void setReportadoPorId(int reportadoPorId) {
        this.reportadoPorId = reportadoPorId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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
        if (!(o instanceof Incidente incidente)) return false;
        return id == incidente.id && reportadoPorId == incidente.reportadoPorId && Objects.equals(fecha, incidente.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportadoPorId, fecha);
    }

    @Override
    public String toString() {
        return "Incidente{" +
                "id=" + id +
                ", visitaId=" + visitaId +
                ", reportadoPorId=" + reportadoPorId +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
