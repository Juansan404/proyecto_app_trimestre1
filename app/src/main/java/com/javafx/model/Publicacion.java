package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Publicacion {

    @JsonProperty("idPublicacion")
    private Long idPublicacion;
    private Usuario usuario;
    @JsonProperty("fotoUrl")
    private String fotoUrl;
    private String descripcion;
    private String estilo;
    @JsonProperty("zonaCuerpo")
    private String zonaCuerpo;
    @JsonProperty("likesCount")
    private Integer likesCount;
    @JsonProperty("creadoEn")
    private String creadoEn;
    private List<Estilo> estilos;

    public Publicacion() {}

    public Long    getIdPublicacion()          { return idPublicacion; }
    public void    setIdPublicacion(Long v)    { this.idPublicacion = v; }
    public Usuario getUsuario()                { return usuario; }
    public void    setUsuario(Usuario v)       { this.usuario = v; }
    public String  getFotoUrl()                { return fotoUrl; }
    public void    setFotoUrl(String v)        { this.fotoUrl = v; }
    public String  getDescripcion()            { return descripcion; }
    public void    setDescripcion(String v)    { this.descripcion = v; }
    public String  getEstilo()                 { return estilo; }
    public void    setEstilo(String v)         { this.estilo = v; }
    public String  getZonaCuerpo()             { return zonaCuerpo; }
    public void    setZonaCuerpo(String v)     { this.zonaCuerpo = v; }
    public Integer getLikesCount()             { return likesCount; }
    public void    setLikesCount(Integer v)    { this.likesCount = v; }
    public String  getCreadoEn()               { return creadoEn; }
    public void    setCreadoEn(String v)       { this.creadoEn = v; }

    public List<Estilo> getEstilos()               { return estilos; }
    public void         setEstilos(List<Estilo> v) { this.estilos = v; }

    public String getNombreUsuario() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }

    @Override
    public String toString() {
        return "#" + idPublicacion + " — " + getNombreUsuario()
                + (estilo != null ? " (" + estilo + ")" : "");
    }

    /** Devuelve los estilos dinámicos; si no hay, cae al campo legacy estilo. */
    public String getEstilosStr() {
        if (estilos != null && !estilos.isEmpty())
            return estilos.stream().map(Estilo::getNombre).collect(Collectors.joining(", "));
        return estilo != null ? estilo : "";
    }

    public String getDescripcionCorta() {
        if (descripcion == null) return "";
        return descripcion.length() > 60 ? descripcion.substring(0, 60) + "…" : descripcion;
    }
}
