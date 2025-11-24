package com.javafx.dao;

import java.sql.Connection;
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
}
