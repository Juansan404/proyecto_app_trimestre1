package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cita {

    @JsonProperty("idCita")
    private Long idCita;
    private Usuario cliente;
    private Usuario artista;
    @JsonProperty("idSolicitud")
    private Long idSolicitud;
    @JsonProperty("fechaCita")
    private String fechaCita;
    @JsonProperty("horaInicio")
    private String horaInicio;
    @JsonProperty("duracionAproximada")
    private Integer duracionAproximada;
    private Double precio;
    private String estado;
    private String sala;
    @JsonProperty("fotoDiseno")
    private String fotoDiseno;
    private String notas;
    @JsonProperty("creadoEn")
    private String creadoEn;

    public Cita() {}

    public Long    getIdCita()                    { return idCita; }
    public void    setIdCita(Long v)              { this.idCita = v; }
    public Usuario getCliente()                   { return cliente; }
    public void    setCliente(Usuario v)          { this.cliente = v; }
    public Usuario getArtista()                   { return artista; }
    public void    setArtista(Usuario v)          { this.artista = v; }
    public Long    getIdSolicitud()               { return idSolicitud; }
    public void    setIdSolicitud(Long v)         { this.idSolicitud = v; }
    public String  getFechaCita()                 { return fechaCita; }
    public void    setFechaCita(String v)         { this.fechaCita = v; }
    public String  getHoraInicio()                { return horaInicio; }
    public void    setHoraInicio(String v)        { this.horaInicio = v; }
    public Integer getDuracionAproximada()        { return duracionAproximada; }
    public void    setDuracionAproximada(Integer v){ this.duracionAproximada = v; }
    public Double  getPrecio()                    { return precio; }
    public void    setPrecio(Double v)            { this.precio = v; }
    public String  getEstado()                    { return estado; }
    public void    setEstado(String v)            { this.estado = v; }
    public String  getSala()                      { return sala; }
    public void    setSala(String v)              { this.sala = v; }
    public String  getFotoDiseno()                { return fotoDiseno; }
    public void    setFotoDiseno(String v)        { this.fotoDiseno = v; }
    public String  getNotas()                     { return notas; }
    public void    setNotas(String v)             { this.notas = v; }
    public String  getCreadoEn()                  { return creadoEn; }
    public void    setCreadoEn(String v)          { this.creadoEn = v; }

    public String getNombreCliente() {
        return cliente != null ? cliente.getNombreCompleto() : "";
    }

    public String getNombreArtista() {
        return artista != null ? artista.getNombreCompleto() : "";
    }
}
