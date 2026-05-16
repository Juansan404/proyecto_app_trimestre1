package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario {

    @JsonProperty("idUsuario")
    private Long idUsuario;
    private String nombre;
    private String apellidos;
    private String email;
    private String rol;
    private String avatar;
    private String bio;
    private Boolean activo;
    @JsonProperty("estadoRegistro")
    private String estadoRegistro;
    @JsonProperty("creadoEn")
    private String creadoEn;
    private Long numeroPublicaciones;
    private Long seguidores;
    private Long seguidos;

    public Usuario() {}

    public Long    getIdUsuario()              { return idUsuario; }
    public void    setIdUsuario(Long v)        { this.idUsuario = v; }
    public String  getNombre()                 { return nombre; }
    public void    setNombre(String v)         { this.nombre = v; }
    public String  getApellidos()              { return apellidos; }
    public void    setApellidos(String v)      { this.apellidos = v; }
    public String  getEmail()                  { return email; }
    public void    setEmail(String v)          { this.email = v; }
    public String  getRol()                    { return rol; }
    public void    setRol(String v)            { this.rol = v; }
    public String  getAvatar()                 { return avatar; }
    public void    setAvatar(String v)         { this.avatar = v; }
    public String  getBio()                    { return bio; }
    public void    setBio(String v)            { this.bio = v; }
    public Boolean getActivo()                 { return activo; }
    public void    setActivo(Boolean v)        { this.activo = v; }
    public String  getEstadoRegistro()         { return estadoRegistro; }
    public void    setEstadoRegistro(String v) { this.estadoRegistro = v; }
    public String  getCreadoEn()               { return creadoEn; }
    public void    setCreadoEn(String v)       { this.creadoEn = v; }
    public Long    getNumeroPublicaciones()    { return numeroPublicaciones; }
    public void    setNumeroPublicaciones(Long v) { this.numeroPublicaciones = v; }
    public Long    getSeguidores()             { return seguidores; }
    public void    setSeguidores(Long v)       { this.seguidores = v; }
    public Long    getSeguidos()               { return seguidos; }
    public void    setSeguidos(Long v)         { this.seguidos = v; }

    public String getNombreCompleto() {
        String n = nombre   != null ? nombre   : "";
        String a = apellidos != null ? apellidos : "";
        return (n + " " + a).trim();
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + email + ")";
    }
}
