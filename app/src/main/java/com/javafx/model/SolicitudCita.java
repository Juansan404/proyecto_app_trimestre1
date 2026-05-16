package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolicitudCita {

    @JsonProperty("idSolicitud")
    private Long idSolicitud;
    private Usuario cliente;
    private Usuario artista;
    private String descripcion;
    @JsonProperty("zonaCuerpo")
    private String zonaCuerpo;
    @JsonProperty("tamano")
    private String tamano;
    @JsonProperty("presupuestoAprox")
    private Double presupuestoAprox;
    @JsonProperty("fechaPreferida")
    private String fechaPreferida;
    @JsonProperty("fotoReferencia")
    private String fotoReferencia;
    private String estado;
    @JsonProperty("notasArtista")
    private String notasArtista;
    @JsonProperty("creadoEn")
    private String creadoEn;

    public SolicitudCita() {}

    public Long    getIdSolicitud()             { return idSolicitud; }
    public void    setIdSolicitud(Long v)       { this.idSolicitud = v; }
    public Usuario getCliente()                 { return cliente; }
    public void    setCliente(Usuario v)        { this.cliente = v; }
    public Usuario getArtista()                 { return artista; }
    public void    setArtista(Usuario v)        { this.artista = v; }
    public String  getDescripcion()             { return descripcion; }
    public void    setDescripcion(String v)     { this.descripcion = v; }
    public String  getZonaCuerpo()              { return zonaCuerpo; }
    public void    setZonaCuerpo(String v)      { this.zonaCuerpo = v; }
    public String  getTamano()                  { return tamano; }
    public void    setTamano(String v)          { this.tamano = v; }
    public Double  getPresupuestoAprox()        { return presupuestoAprox; }
    public void    setPresupuestoAprox(Double v){ this.presupuestoAprox = v; }
    public String  getFechaPreferida()          { return fechaPreferida; }
    public void    setFechaPreferida(String v)  { this.fechaPreferida = v; }
    public String  getFotoReferencia()          { return fotoReferencia; }
    public void    setFotoReferencia(String v)  { this.fotoReferencia = v; }
    public String  getEstado()                  { return estado; }
    public void    setEstado(String v)          { this.estado = v; }
    public String  getNotasArtista()            { return notasArtista; }
    public void    setNotasArtista(String v)    { this.notasArtista = v; }
    public String  getCreadoEn()                { return creadoEn; }
    public void    setCreadoEn(String v)        { this.creadoEn = v; }

    public String getNombreCliente() {
        return cliente != null ? cliente.getNombreCompleto() : "";
    }

    public String getNombreArtista() {
        return artista != null ? artista.getNombreCompleto() : "";
    }

    @Override
    public String toString() {
        return "#" + idSolicitud + " — " + getNombreCliente() + " → " + getNombreArtista();
    }
}
