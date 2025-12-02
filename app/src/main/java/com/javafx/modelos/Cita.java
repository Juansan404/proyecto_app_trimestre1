package com.javafx.modelos;

import java.sql.Date;

public class Cita {

    public enum EstadoCita {
        PENDIENTE("Pendiente"),
        CONFIRMADA("Confirmada"),
        CANCELADA("Cancelada"),
        COMPLETADA("Completada");

        private final String valor;

        EstadoCita(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }

        public static EstadoCita fromString(String texto) {
            for (EstadoCita estado : EstadoCita.values()) {
                if (estado.valor.equalsIgnoreCase(texto)) {
                    return estado;
                }
            }
            throw new IllegalArgumentException("Estado no válido: " + texto);
        }
    }

    private int id_cita;
    private int id_cliente;
    private int id_artista;
    private Date fecha_cita;
    private int duracion_aproximada;
    private double precio;
    private EstadoCita estado;
    private String sala;
    private byte[] foto_diseno;
    private String notas;

    // Campos adicionales para mostrar en la tabla
    private String nombreCliente;
    private String nombreArtista;

    public Cita(int id_cita, int id_cliente, int id_artista, Date fecha_cita, int duracion_aproximada,
                double precio, EstadoCita estado, String sala, byte[] foto_diseno, String notas) {
        this.id_cita = id_cita;
        this.id_cliente = id_cliente;
        this.id_artista = id_artista;
        this.fecha_cita = fecha_cita;
        this.duracion_aproximada = duracion_aproximada;
        this.precio = precio;
        this.estado = estado;
        this.sala = sala;
        this.foto_diseno = foto_diseno;
        this.notas = notas;
    }

    // Getters y Setters
    public int getId_cita() {
        return id_cita;
    }

    public void setId_cita(int id_cita) {
        this.id_cita = id_cita;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public int getId_artista() {
        return id_artista;
    }

    public void setId_artista(int id_artista) {
        this.id_artista = id_artista;
    }

    public Date getFecha_cita() {
        return fecha_cita;
    }

    public void setFecha_cita(Date fecha_cita) {
        this.fecha_cita = fecha_cita;
    }

    public int getDuracion_aproximada() {
        return duracion_aproximada;
    }

    public void setDuracion_aproximada(int duracion_aproximada) {
        this.duracion_aproximada = duracion_aproximada;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public byte[] getFoto_diseno() {
        return foto_diseno;
    }

    public void setFoto_diseno(byte[] foto_diseno) {
        this.foto_diseno = foto_diseno;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreArtista() {
        return nombreArtista;
    }

    public void setNombreArtista(String nombreArtista) {
        this.nombreArtista = nombreArtista;
    }

    // Método auxiliar para construir el nombre completo del cliente
    public void setNombreCompletoCliente(String nombre, String apellidos) {
        this.nombreCliente = (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "");
        this.nombreCliente = this.nombreCliente.trim();
    }

    // Método auxiliar para construir el nombre completo del artista
    public void setNombreCompletoArtista(String nombre, String apellidos) {
        this.nombreArtista = (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "");
        this.nombreArtista = this.nombreArtista.trim();
    }
}
