package com.zonaacme.sica.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Visita {
    private int id;
    private int personaId;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private int estadoVisitaId;
    private String estadoVisitaNombre;
    private Integer visitaAprobadaPor;
    private int metodoIngresoId;
    private Integer metodoSalidaId;
    private List<String> vehiculosPlacas;

    public Visita() {
        this.vehiculosPlacas = new ArrayList<>();
    }

    public Visita(int id, int personaId, LocalDateTime fechaEntrada, LocalDateTime fechaSalida,
                  int estadoVisitaId, String estadoVisitaNombre, Integer visitaAprobadaPor,
                  int metodoIngresoId, Integer metodoSalidaId, List<String> vehiculosPlacas) {
        this.id = id;
        this.personaId = personaId;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.estadoVisitaId = estadoVisitaId;
        this.estadoVisitaNombre = estadoVisitaNombre;
        this.visitaAprobadaPor = visitaAprobadaPor;
        this.metodoIngresoId = metodoIngresoId;
        this.metodoSalidaId = metodoSalidaId;
        this.vehiculosPlacas = vehiculosPlacas != null ? new ArrayList<>(vehiculosPlacas) : new ArrayList<>();
    }

    private Visita(Builder builder) {
        this.id = builder.id;
        this.personaId = builder.personaId;
        this.fechaEntrada = builder.fechaEntrada;
        this.fechaSalida = builder.fechaSalida;
        this.estadoVisitaId = builder.estadoVisitaId;
        this.estadoVisitaNombre = builder.estadoVisitaNombre;
        this.visitaAprobadaPor = builder.visitaAprobadaPor;
        this.metodoIngresoId = builder.metodoIngresoId;
        this.metodoSalidaId = builder.metodoSalidaId;
        this.vehiculosPlacas = new ArrayList<>(builder.vehiculosPlacas);
    }

    public static Builder builder() {
        return new Builder();
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

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public int getEstadoVisitaId() {
        return estadoVisitaId;
    }

    public void setEstadoVisitaId(int estadoVisitaId) {
        this.estadoVisitaId = estadoVisitaId;
    }

    public String getEstadoVisitaNombre() {
        return estadoVisitaNombre;
    }

    public void setEstadoVisitaNombre(String estadoVisitaNombre) {
        this.estadoVisitaNombre = estadoVisitaNombre;
    }

    public Integer getVisitaAprobadaPor() {
        return visitaAprobadaPor;
    }

    public void setVisitaAprobadaPor(Integer visitaAprobadaPor) {
        this.visitaAprobadaPor = visitaAprobadaPor;
    }

    public int getMetodoIngresoId() {
        return metodoIngresoId;
    }

    public void setMetodoIngresoId(int metodoIngresoId) {
        this.metodoIngresoId = metodoIngresoId;
    }

    public Integer getMetodoSalidaId() {
        return metodoSalidaId;
    }

    public void setMetodoSalidaId(Integer metodoSalidaId) {
        this.metodoSalidaId = metodoSalidaId;
    }

    public List<String> getVehiculosPlacas() {
        return vehiculosPlacas;
    }

    public void setVehiculosPlacas(List<String> vehiculosPlacas) {
        this.vehiculosPlacas = vehiculosPlacas != null ? new ArrayList<>(vehiculosPlacas) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Visita visita)) return false;
        return id == visita.id && personaId == visita.personaId && Objects.equals(fechaEntrada, visita.fechaEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personaId, fechaEntrada);
    }

    @Override
    public String toString() {
        return "Visita{" +
                "id=" + id +
                ", personaId=" + personaId +
                ", fechaEntrada=" + fechaEntrada +
                ", fechaSalida=" + fechaSalida +
                ", estadoVisitaId=" + estadoVisitaId +
                ", estadoVisitaNombre='" + estadoVisitaNombre + '\'' +
                ", visitaAprobadaPor=" + visitaAprobadaPor +
                ", metodoIngresoId=" + metodoIngresoId +
                ", metodoSalidaId=" + metodoSalidaId +
                ", vehiculosPlacas=" + vehiculosPlacas +
                '}';
    }

    /**
     * Patrón de Diseño #1: Builder explícito para Visita.
     */
    public static class Builder {
        private int id;
        private int personaId;
        private LocalDateTime fechaEntrada;
        private LocalDateTime fechaSalida;
        private int estadoVisitaId;
        private String estadoVisitaNombre;
        private Integer visitaAprobadaPor;
        private int metodoIngresoId;
        private Integer metodoSalidaId;
        private List<String> vehiculosPlacas = new ArrayList<>();

        public Builder() {
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder personaId(int personaId) {
            this.personaId = personaId;
            return this;
        }

        public Builder fechaEntrada(LocalDateTime fechaEntrada) {
            this.fechaEntrada = fechaEntrada;
            return this;
        }

        public Builder fechaSalida(LocalDateTime fechaSalida) {
            this.fechaSalida = fechaSalida;
            return this;
        }

        public Builder estadoVisitaId(int estadoVisitaId) {
            this.estadoVisitaId = estadoVisitaId;
            return this;
        }

        public Builder estadoVisitaNombre(String estadoVisitaNombre) {
            this.estadoVisitaNombre = estadoVisitaNombre;
            return this;
        }

        public Builder visitaAprobadaPor(Integer visitaAprobadaPor) {
            this.visitaAprobadaPor = visitaAprobadaPor;
            return this;
        }

        public Builder metodoIngresoId(int metodoIngresoId) {
            this.metodoIngresoId = metodoIngresoId;
            return this;
        }

        public Builder metodoSalidaId(Integer metodoSalidaId) {
            this.metodoSalidaId = metodoSalidaId;
            return this;
        }

        public Builder vehiculosPlacas(List<String> vehiculosPlacas) {
            this.vehiculosPlacas = vehiculosPlacas != null ? new ArrayList<>(vehiculosPlacas) : new ArrayList<>();
            return this;
        }

        public Builder agregarVehiculoPlaca(String placa) {
            if (placa != null && !placa.isBlank()) {
                this.vehiculosPlacas.add(placa.trim());
            }
            return this;
        }

        public Visita build() {
            if (this.personaId <= 0) {
                throw new IllegalArgumentException("El personaId debe ser mayor a 0.");
            }
            if (this.estadoVisitaId <= 0) {
                throw new IllegalArgumentException("El estadoVisitaId debe ser mayor a 0.");
            }
            if (this.metodoIngresoId <= 0) {
                throw new IllegalArgumentException("El metodoIngresoId debe ser mayor a 0.");
            }
            if (this.fechaEntrada == null) {
                this.fechaEntrada = LocalDateTime.now();
            }
            if (this.fechaSalida != null && this.fechaSalida.isBefore(this.fechaEntrada)) {
                throw new IllegalArgumentException("La fechaSalida no puede ser anterior a fechaEntrada.");
            }
            return new Visita(this);
        }
    }
}
