package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comentario {

    @JsonProperty("idComentario")
    private Long idComentario;
    private Publicacion publicacion;
    private Usuario usuario;
    private String contenido;
    @JsonProperty("creadoEn")
    private String creadoEn;

    public Comentario() {}

    public Long        getIdComentario()           { return idComentario; }
    public void        setIdComentario(Long v)     { this.idComentario = v; }
    public Publicacion getPublicacion()            { return publicacion; }
    public void        setPublicacion(Publicacion v){ this.publicacion = v; }
    public Usuario     getUsuario()                { return usuario; }
    public void        setUsuario(Usuario v)       { this.usuario = v; }
    public String      getContenido()              { return contenido; }
    public void        setContenido(String v)      { this.contenido = v; }
    public String      getCreadoEn()               { return creadoEn; }
    public void        setCreadoEn(String v)       { this.creadoEn = v; }

    public String getNombreUsuario() {
        return usuario != null ? usuario.getNombreCompleto() : "Desconocido";
    }

    public Long getIdPublicacion() {
        return publicacion != null ? publicacion.getIdPublicacion() : null;
    }
}
