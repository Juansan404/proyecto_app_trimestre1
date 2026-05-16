package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MensajeDirecto {

    @JsonProperty("idMensaje")
    private Long idMensaje;

    @JsonProperty("conversacion")
    private Conversacion conversacion;

    @JsonProperty("remitente")
    private Usuario remitente;

    private String contenido;
    private Boolean leido;

    @JsonProperty("creadoEn")
    private String creadoEn;

    public MensajeDirecto() {}

    public Long         getIdMensaje()              { return idMensaje; }
    public void         setIdMensaje(Long v)        { this.idMensaje = v; }
    public Conversacion getConversacion()            { return conversacion; }
    public void         setConversacion(Conversacion v) { this.conversacion = v; }
    public Usuario      getRemitente()               { return remitente; }
    public void         setRemitente(Usuario v)      { this.remitente = v; }
    public String       getContenido()               { return contenido; }
    public void         setContenido(String v)       { this.contenido = v; }
    public Boolean      getLeido()                   { return leido; }
    public void         setLeido(Boolean v)          { this.leido = v; }
    public String       getCreadoEn()               { return creadoEn; }
    public void         setCreadoEn(String v)        { this.creadoEn = v; }

    public String getNombreRemitente() {
        return remitente != null ? remitente.getNombreCompleto() : "Desconocido";
    }

    @Override
    public String toString() {
        String hora = creadoEn != null && creadoEn.length() >= 16
                ? creadoEn.substring(11, 16) : "";
        return "[" + hora + "] " + getNombreRemitente() + ": " + contenido;
    }
}
