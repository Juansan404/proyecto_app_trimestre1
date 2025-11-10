package com.javafx.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/tattooage";
    private static final String USER = "admin"; // Usuario de la base de datos
    private static final String PASSWORD = "2dhXx23nk4rN"; // Contraseña
    private static Connection conexion = null; // Para realizar la conexión

    void main() {
        DriverManager.drivers().forEach(driver -> System.out.println(driver.toString()));
        realizarConexion();
        cerrarConexion();
    }

    public static Connection getConnection() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexion realizada");
            }
        } catch (SQLException e) {
            System.out.println("No se conectó con la base de datos, error: " + e.getMessage());
        }
        return conexion;
    }

    public void realizarConexion() {
        try {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexion realizada");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void seleccionarDatosClientes() {
        try {
            Statement st = conexion.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM CLIENTES");

            while (resultado.next()) {
                String id_cliente = resultado.getString("id_cliente");
                String nombre = resultado.getString("nombre");
                String apellidos = resultado.getString("apellidos");
                String telefono = resultado.getString("email");
                String email = resultado.getString("email");
                String fecha_nacimiento = resultado.getString("fecha_nacimiento");
                String notas = resultado.getString("notas");
                System.out.println("CLIENTE " + nombre + " " + apellidos + " - " + email);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            cerrarConexion();
        }
    }

    public void seleccionarDatosTatuadores() {
        try {
            Statement st = conexion.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM TATUADORES");

            while (resultado.next()) {
                String id_artista = resultado.getString("id_artista");
                String nombre = resultado.getString("nombre");
                String apellidos = resultado.getString("apellidos");
                String telefono = resultado.getString("telefono");
                String email = resultado.getString("email");
                String activo = resultado.getString("email");
                System.out.println("CLIENTE " + nombre + " " + apellidos + " - " + email);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            cerrarConexion();
        }
    }

    public void seleccionarDatos() {
        try {
            Statement st = conexion.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM CITAS");

            while (resultado.next()) {
                String id_cita = resultado.getString("id_cita");
                String id_cliente = resultado.getString("id_cliente");
                String id_artista = resultado.getString("id_artista");
                String fecha_cita = resultado.getString("fecha_cita"); 
                String duracion_aproximada = resultado.getString("duracion_aproximada");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            cerrarConexion();
        }
    }

    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                System.out.println("Conexion cerrada");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

} 
