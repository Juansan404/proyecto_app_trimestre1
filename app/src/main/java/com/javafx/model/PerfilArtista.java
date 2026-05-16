package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PerfilArtista {

    @JsonProperty("idPerfil")
    private Long idPerfil;
    private Usuario usuario;
    private Estudio estudio;
    private String especialidades;
    @JsonProperty("anosExperiencia")
    private Integer anosExperiencia;
    private String instagram;
    @JsonProperty("precioHora")
    private Double precioHora;
    private Boolean activo;
    private Boolean disponible;
    @JsonProperty("portfolioUrl")
    private String portfolioUrl;

    private transient long   seguidores       = 0;
    private transient double mediaValoracion  = 0.0;
    private transient long   totalValoraciones = 0;
    private List<Estilo> estilos;

    public PerfilArtista() {}

    public Long    getIdPerfil()               { return idPerfil; }
    public void    setIdPerfil(Long v)         { this.idPerfil = v; }
    public Usuario getUsuario()                { return usuario; }
    public void    setUsuario(Usuario v)       { this.usuario = v; }
    public Estudio getEstudio()                { return estudio; }
    public void    setEstudio(Estudio v)       { this.estudio = v; }
    public String  getEspecialidades()         { return especialidades; }
    public void    setEspecialidades(String v) { this.especialidades = v; }
    public Integer getAnosExperiencia()        { return anosExperiencia; }
    public void    setAnosExperiencia(Integer v){ this.anosExperiencia = v; }
    public String  getInstagram()              { return instagram; }
    public void    setInstagram(String v)      { this.instagram = v; }
    public Double  getPrecioHora()             { return precioHora; }
    public void    setPrecioHora(Double v)     { this.precioHora = v; }
    public Boolean getActivo()                 { return activo; }
    public void    setActivo(Boolean v)        { this.activo = v; }
    public Boolean getDisponible()             { return disponible; }
    public void    setDisponible(Boolean v)    { this.disponible = v; }
    public String  getPortfolioUrl()           { return portfolioUrl; }
    public void    setPortfolioUrl(String v)   { this.portfolioUrl = v; }

    public long         getSeguidores()                   { return seguidores; }
    public void         setSeguidores(long v)             { this.seguidores = v; }
    public double       getMediaValoracion()              { return mediaValoracion; }
    public void         setMediaValoracion(double v)      { this.mediaValoracion = v; }
    public long         getTotalValoraciones()            { return totalValoraciones; }
    public void         setTotalValoraciones(long v)      { this.totalValoraciones = v; }
    public List<Estilo> getEstilos()                      { return estilos; }
    public void         setEstilos(List<Estilo> v)        { this.estilos = v; }

    public String getEstilosStr() {
        if (estilos != null && !estilos.isEmpty())
            return estilos.stream().map(Estilo::getNombre).collect(Collectors.joining(", "));
        return especialidades != null ? especialidades : "";
    }

    public String getNombreArtista() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }

    public String getNombreEstudio() {
        return estudio != null ? estudio.getNombre() : "Sin estudio";
    }
}
