package com.zonaacme.sica.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class BitacoraAuditoria {
    private long id;
    private Integer usuarioId;
    private LocalDateTime fechaHora;
    private String accionRealizada;
    private String tablaAfectada;
    private Integer registroIdAfectado;
    private String detallesJson;

    public BitacoraAuditoria() {
    }

    public BitacoraAuditoria(long id, Integer usuarioId, LocalDateTime fechaHora, String accionRealizada,
                             String tablaAfectada, Integer registroIdAfectado, String detallesJson) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fechaHora = fechaHora;
        this.accionRealizada = accionRealizada;
        this.tablaAfectada = tablaAfectada;
        this.registroIdAfectado = registroIdAfectado;
        this.detallesJson = detallesJson;
    }

    private BitacoraAuditoria(Builder builder) {
        this.id = builder.id;
        this.usuarioId = builder.usuarioId;
        this.fechaHora = builder.fechaHora;
        this.accionRealizada = builder.accionRealizada;
        this.tablaAfectada = builder.tablaAfectada;
        this.registroIdAfectado = builder.registroIdAfectado;
        this.detallesJson = builder.detallesJson;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getAccionRealizada() {
        return accionRealizada;
    }

    public void setAccionRealizada(String accionRealizada) {
        this.accionRealizada = accionRealizada;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public Integer getRegistroIdAfectado() {
        return registroIdAfectado;
    }

    public void setRegistroIdAfectado(Integer registroIdAfectado) {
        this.registroIdAfectado = registroIdAfectado;
    }

    public String getDetallesJson() {
        return detallesJson;
    }

    public void setDetallesJson(String detallesJson) {
        this.detallesJson = detallesJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitacoraAuditoria that)) return false;
        return id == that.id && Objects.equals(accionRealizada, that.accionRealizada) && Objects.equals(fechaHora, that.fechaHora);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accionRealizada, fechaHora);
    }

    @Override
    public String toString() {
        return "BitacoraAuditoria{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", fechaHora=" + fechaHora +
                ", accionRealizada='" + accionRealizada + '\'' +
                ", tablaAfectada='" + tablaAfectada + '\'' +
                ", registroIdAfectado=" + registroIdAfectado +
                ", detallesJson='" + detallesJson + '\'' +
                '}';
    }

    /**
     * Patrón de Diseño #1: Builder explícito para BitacoraAuditoria.
     */
    public static class Builder {
        private long id;
        private Integer usuarioId;
        private LocalDateTime fechaHora;
        private String accionRealizada;
        private String tablaAfectada;
        private Integer registroIdAfectado;
        private String detallesJson;

        public Builder() {
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder usuarioId(Integer usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public Builder fechaHora(LocalDateTime fechaHora) {
            this.fechaHora = fechaHora;
            return this;
        }

        public Builder accionRealizada(String accionRealizada) {
            this.accionRealizada = accionRealizada;
            return this;
        }

        public Builder tablaAfectada(String tablaAfectada) {
            this.tablaAfectada = tablaAfectada;
            return this;
        }

        public Builder registroIdAfectado(Integer registroIdAfectado) {
            this.registroIdAfectado = registroIdAfectado;
            return this;
        }

        public Builder detallesJson(String detallesJson) {
            this.detallesJson = detallesJson;
            return this;
        }

        public BitacoraAuditoria build() {
            if (this.accionRealizada == null || this.accionRealizada.isBlank()) {
                throw new IllegalArgumentException("La accionRealizada es obligatoria y no puede estar vacía.");
            }
            if (this.usuarioId != null && this.usuarioId <= 0) {
                throw new IllegalArgumentException("El usuarioId, si se especifica, debe ser mayor a 0.");
            }
            if (this.fechaHora == null) {
                this.fechaHora = LocalDateTime.now();
            }
            if (this.detallesJson == null || this.detallesJson.isBlank()) {
                this.detallesJson = "{}";
            }
            return new BitacoraAuditoria(this);
        }
    }
}
