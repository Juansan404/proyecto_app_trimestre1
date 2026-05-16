package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notificacion {

    @JsonProperty("idNotificacion")
    private Long idNotificacion;
    private String tipo;
    private Usuario emisor;
    private Boolean leido;
    @JsonProperty("idReferencia")
    private Integer idReferencia;
    @JsonProperty("creadoEn")
    private String creadoEn;

    public Notificacion() {}

    public Long    getIdNotificacion()           { return idNotificacion; }
    public void    setIdNotificacion(Long v)     { this.idNotificacion = v; }
    public String  getTipo()                     { return tipo; }
    public void    setTipo(String v)             { this.tipo = v; }
    public Usuario getEmisor()                   { return emisor; }
    public void    setEmisor(Usuario v)          { this.emisor = v; }
    public Boolean getLeido()                    { return leido; }
    public void    setLeido(Boolean v)           { this.leido = v; }
    public Integer getIdReferencia()             { return idReferencia; }
    public void    setIdReferencia(Integer v)    { this.idReferencia = v; }
    public String  getCreadoEn()                 { return creadoEn; }
    public void    setCreadoEn(String v)         { this.creadoEn = v; }

    public String getDescripcion() {
        String nombre = emisor != null ? emisor.getNombreCompleto() : "Alguien";
        if (tipo == null) return nombre + " realizó una acción";
        return switch (tipo) {
            case "LIKE"       -> nombre + " dio like a tu publicación";
            case "COMENTARIO" -> nombre + " comentó en tu publicación";
            case "SEGUIDOR"   -> nombre + " empezó a seguirte";
            case "SOLICITUD"  -> nombre + " envió una solicitud de cita";
            default           -> nombre + " realizó una acción (" + tipo + ")";
        };
    }
}
