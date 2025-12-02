package com.javafx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.javafx.database.DatabaseConnection;
import com.javafx.modelos.Cliente;

public class ClienteDAO {

    public List<Cliente> cargarClientes() {
        List<Cliente> listaClientes = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM CLIENTES");

            while (resultado.next()) {
                Cliente cliente = new Cliente(
                    resultado.getInt("id_cliente"),
                    resultado.getString("nombre"),
                    resultado.getString("apellidos"),
                    resultado.getString("telefono"),
                    resultado.getString("email"),
                    resultado.getDate("fecha_nacimiento"),
                    resultado.getString("notas")
                );
                listaClientes.add(cliente);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar clientes: " + e.getMessage());
            e.printStackTrace();
        }
        return listaClientes;
    }

    public boolean insertarCliente(Cliente cliente) {
        String sql = "INSERT INTO CLIENTES (nombre, apellidos, telefono, email, fecha_nacimiento, notas) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellidos());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setString(4, cliente.getEmail());
            pstmt.setDate(5, cliente.getFecha_nac());
            pstmt.setString(6, cliente.getNotas());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.out.println("Error al insertar cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE CLIENTES SET nombre = ?, apellidos = ?, telefono = ?, email = ?, fecha_nacimiento = ?, notas = ? WHERE id_cliente = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellidos());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setString(4, cliente.getEmail());
            pstmt.setDate(5, cliente.getFecha_nac());
            pstmt.setString(6, cliente.getNotas());
            pstmt.setInt(7, cliente.getId_cliente());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarCliente(int idCliente) {
        String sql = "DELETE FROM CLIENTES WHERE id_cliente = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Cliente obtenerClientePorId(int idCliente) {
        String sql = "SELECT * FROM CLIENTES WHERE id_cliente = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);

            ResultSet resultado = pstmt.executeQuery();

            if (resultado.next()) {
                return new Cliente(
                    resultado.getInt("id_cliente"),
                    resultado.getString("nombre"),
                    resultado.getString("apellidos"),
                    resultado.getString("telefono"),
                    resultado.getString("email"),
                    resultado.getDate("fecha_nacimiento"),
                    resultado.getString("notas")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener cliente por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Cliente> buscarClientes(String criterio, String valor) {
        List<Cliente> resultados = new ArrayList<>();
        String sql = "SELECT * FROM CLIENTES WHERE ";

        switch (criterio) {
            case "Nombre":
                sql += "nombre LIKE ?";
                break;
            case "Apellidos":
                sql += "apellidos LIKE ?";
                break;
            case "Email":
                sql += "email LIKE ?";
                break;
            case "Telefono":
                sql += "telefono LIKE ?";
                break;
            default:
                return resultados;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + valor + "%");

            ResultSet resultado = pstmt.executeQuery();

            while (resultado.next()) {
                Cliente cliente = new Cliente(
                    resultado.getInt("id_cliente"),
                    resultado.getString("nombre"),
                    resultado.getString("apellidos"),
                    resultado.getString("telefono"),
                    resultado.getString("email"),
                    resultado.getDate("fecha_nacimiento"),
                    resultado.getString("notas")
                );
                resultados.add(cliente);
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar clientes: " + e.getMessage());
            e.printStackTrace();
        }
        return resultados;
    }
}
