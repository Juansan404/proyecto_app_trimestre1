package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Valoracion {

    private Integer idValoracion;
    private Integer puntuacion;
    private String  comentario;
    private String  creadoEn;
    private ClienteInfo cliente;

    public Valoracion() {}

    public Integer     getIdValoracion()             { return idValoracion; }
    public void        setIdValoracion(Integer v)    { this.idValoracion = v; }
    public Integer     getPuntuacion()               { return puntuacion; }
    public void        setPuntuacion(Integer v)      { this.puntuacion = v; }
    public String      getComentario()               { return comentario; }
    public void        setComentario(String v)       { this.comentario = v; }
    public String      getCreadoEn()                 { return creadoEn; }
    public void        setCreadoEn(String v)         { this.creadoEn = v; }
    public ClienteInfo getCliente()                  { return cliente; }
    public void        setCliente(ClienteInfo v)     { this.cliente = v; }

    public String getNombreCliente() {
        if (cliente == null) return "—";
        return cliente.getNombre() + (cliente.getApellidos() != null ? " " + cliente.getApellidos() : "");
    }

    public String getEstrellas() {
        if (puntuacion == null) return "";
        return "★".repeat(puntuacion) + "☆".repeat(5 - puntuacion);
    }

    public String getFechaCorta() {
        return creadoEn != null && creadoEn.length() >= 10 ? creadoEn.substring(0, 10) : "";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClienteInfo {
        private Integer idUsuario;
        private String  nombre;
        private String  apellidos;

        public Integer getIdUsuario()            { return idUsuario; }
        public void    setIdUsuario(Integer v)   { this.idUsuario = v; }
        public String  getNombre()               { return nombre; }
        public void    setNombre(String v)       { this.nombre = v; }
        public String  getApellidos()            { return apellidos; }
        public void    setApellidos(String v)    { this.apellidos = v; }
    }
}
