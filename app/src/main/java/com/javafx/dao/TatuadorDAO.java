package com.javafx.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.javafx.database.DatabaseConnection;
import com.javafx.exceptions.DatabaseConnectionException;
import com.javafx.modelos.Tatuador;

public class TatuadorDAO {

    public List<Tatuador> cargarTatuadores() throws DatabaseConnectionException {
        List<Tatuador> listaTatuadores = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM TATUADORES");

            while (resultado.next()) {
                Tatuador tatuador = new Tatuador(
                    resultado.getInt("id_artista"),
                    resultado.getString("nombre"),
                    resultado.getString("apellidos"),
                    resultado.getString("telefono"),
                    resultado.getString("email"),
                    resultado.getBoolean("activo")
                );
                listaTatuadores.add(tatuador);
            }
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al cargar tatuadores: " + e.getMessage());
            e.printStackTrace();
        }
        return listaTatuadores;
    }

    public boolean insertarTatuador(Tatuador tatuador) throws DatabaseConnectionException {
        String sql = "INSERT INTO TATUADORES (nombre, apellidos, telefono, email, activo) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tatuador.getNombre());
            pstmt.setString(2, tatuador.getApellidos());
            pstmt.setString(3, tatuador.getTelefono());
            pstmt.setString(4, tatuador.getEmail());
            pstmt.setBoolean(5, tatuador.isActivo());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al insertar tatuador: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarTatuador(Tatuador tatuador) throws DatabaseConnectionException {
        String sql = "UPDATE TATUADORES SET nombre = ?, apellidos = ?, telefono = ?, email = ?, activo = ? WHERE id_artista = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tatuador.getNombre());
            pstmt.setString(2, tatuador.getApellidos());
            pstmt.setString(3, tatuador.getTelefono());
            pstmt.setString(4, tatuador.getEmail());
            pstmt.setBoolean(5, tatuador.isActivo());
            pstmt.setInt(6, tatuador.getId_artista());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al actualizar tatuador: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarTatuador(int idArtista) throws DatabaseConnectionException {
        String sql = "DELETE FROM TATUADORES WHERE id_artista = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idArtista);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al eliminar tatuador: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Tatuador> buscarTatuadores(String criterio, String valor) throws DatabaseConnectionException {
        List<Tatuador> resultados = new ArrayList<>();
        String sql = "SELECT * FROM TATUADORES WHERE ";

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
            case "Activo":
                sql += "activo = ?";
                break;
            default:
                return resultados;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            if (criterio.equals("Activo")) {
                pstmt.setBoolean(1, valor.equalsIgnoreCase("true") || valor.equals("1"));
            } else {
                pstmt.setString(1, "%" + valor + "%");
            }

            ResultSet resultado = pstmt.executeQuery();

            while (resultado.next()) {
                Tatuador tatuador = new Tatuador(
                    resultado.getInt("id_artista"),
                    resultado.getString("nombre"),
                    resultado.getString("apellidos"),
                    resultado.getString("telefono"),
                    resultado.getString("email"),
                    resultado.getBoolean("activo")
                );
                resultados.add(tatuador);
            }
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al buscar tatuadores: " + e.getMessage());
            e.printStackTrace();
        }
        return resultados;
    }
}
