package com.javafx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Estilo {

    @JsonProperty("idEstilo")
    private Long idEstilo;
    private String nombre;

    public Estilo() {}

    public Long   getIdEstilo()        { return idEstilo; }
    public void   setIdEstilo(Long v)  { this.idEstilo = v; }
    public String getNombre()          { return nombre; }
    public void   setNombre(String v)  { this.nombre = v; }

    @Override
    public String toString() { return nombre != null ? nombre : ""; }
}
