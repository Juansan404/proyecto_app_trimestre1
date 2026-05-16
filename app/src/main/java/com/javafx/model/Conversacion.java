package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversacion {

    @JsonProperty("idConversacion")
    private Long idConversacion;

    @JsonProperty("usuario1")
    private Usuario usuario1;

    @JsonProperty("usuario2")
    private Usuario usuario2;

    @JsonProperty("creadoEn")
    private String creadoEn;

    public Conversacion() {}

    public Long     getIdConversacion()          { return idConversacion; }
    public void     setIdConversacion(Long v)    { this.idConversacion = v; }
    public Usuario  getUsuario1()                { return usuario1; }
    public void     setUsuario1(Usuario v)       { this.usuario1 = v; }
    public Usuario  getUsuario2()                { return usuario2; }
    public void     setUsuario2(Usuario v)       { this.usuario2 = v; }
    public String   getCreadoEn()               { return creadoEn; }
    public void     setCreadoEn(String v)        { this.creadoEn = v; }

    public String getNombresParticipantes() {
        String n1 = usuario1 != null ? usuario1.getNombreCompleto() : "?";
        String n2 = usuario2 != null ? usuario2.getNombreCompleto() : "?";
        return n1 + " ↔ " + n2;
    }

    @Override
    public String toString() { return getNombresParticipantes(); }
}
