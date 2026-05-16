package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Estudio {

    @JsonProperty("idEstudio")
    private Long idEstudio;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private String email;
    private String descripcion;
    private String web;
    private String instagram;
    private String localizacion;
    @JsonProperty("fotoPortada")
    private String fotoPortada;
    @JsonProperty("creadoEn")
    private String creadoEn;

    public Estudio() {}

    public Long   getIdEstudio()              { return idEstudio; }
    public void   setIdEstudio(Long v)        { this.idEstudio = v; }
    public String getNombre()                 { return nombre; }
    public void   setNombre(String v)         { this.nombre = v; }
    public String getDireccion()              { return direccion; }
    public void   setDireccion(String v)      { this.direccion = v; }
    public String getCiudad()                 { return ciudad; }
    public void   setCiudad(String v)         { this.ciudad = v; }
    public String getTelefono()               { return telefono; }
    public void   setTelefono(String v)       { this.telefono = v; }
    public String getEmail()                  { return email; }
    public void   setEmail(String v)          { this.email = v; }
    public String getDescripcion()            { return descripcion; }
    public void   setDescripcion(String v)    { this.descripcion = v; }
    public String getWeb()                    { return web; }
    public void   setWeb(String v)            { this.web = v; }
    public String getInstagram()              { return instagram; }
    public void   setInstagram(String v)      { this.instagram = v; }
    public String getLocalizacion()           { return localizacion; }
    public void   setLocalizacion(String v)   { this.localizacion = v; }
    public String getFotoPortada()            { return fotoPortada; }
    public void   setFotoPortada(String v)    { this.fotoPortada = v; }
    public String getCreadoEn()               { return creadoEn; }
    public void   setCreadoEn(String v)       { this.creadoEn = v; }

    @Override
    public String toString() { return nombre != null ? nombre : ""; }
}
