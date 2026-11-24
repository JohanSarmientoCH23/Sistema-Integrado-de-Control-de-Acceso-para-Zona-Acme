package com.zonaacme.sica.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class TokenAcceso {
    private long id;
    private int visitaId;
    private String codigoToken;
    private int tipoTokenId;
    private int estadoTokenId;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaExpiracion;

    public TokenAcceso() {
    }

    public TokenAcceso(long id, int visitaId, String codigoToken, int tipoTokenId, int estadoTokenId,
                       LocalDateTime fechaEmision, LocalDateTime fechaExpiracion) {
        this.id = id;
        this.visitaId = visitaId;
        this.codigoToken = codigoToken;
        this.tipoTokenId = tipoTokenId;
        this.estadoTokenId = estadoTokenId;
        this.fechaEmision = fechaEmision;
        this.fechaExpiracion = fechaExpiracion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVisitaId() {
        return visitaId;
    }

    public void setVisitaId(int visitaId) {
        this.visitaId = visitaId;
    }

    public String getCodigoToken() {
        return codigoToken;
    }

    public void setCodigoToken(String codigoToken) {
        this.codigoToken = codigoToken;
    }

    public int getTipoTokenId() {
        return tipoTokenId;
    }

    public void setTipoTokenId(int tipoTokenId) {
        this.tipoTokenId = tipoTokenId;
    }

    public int getEstadoTokenId() {
        return estadoTokenId;
    }

    public void setEstadoTokenId(int estadoTokenId) {
        this.estadoTokenId = estadoTokenId;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenAcceso that)) return false;
        return id == that.id && Objects.equals(codigoToken, that.codigoToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigoToken);
    }

    @Override
    public String toString() {
        return "TokenAcceso{" +
                "id=" + id +
                ", visitaId=" + visitaId +
                ", codigoToken='" + codigoToken + '\'' +
                ", tipoTokenId=" + tipoTokenId +
                ", estadoTokenId=" + estadoTokenId +
                ", fechaEmision=" + fechaEmision +
                ", fechaExpiracion=" + fechaExpiracion +
                '}';
    }
}
