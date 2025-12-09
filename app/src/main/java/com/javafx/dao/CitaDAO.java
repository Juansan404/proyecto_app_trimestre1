package com.javafx.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.javafx.database.DatabaseConnection;
import com.javafx.exceptions.DatabaseConnectionException;
import com.javafx.modelos.Cita;

public class CitaDAO {

    public List<Cita> cargarCitas() throws DatabaseConnectionException {
        List<Cita> listaCitas = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "cl.nombre AS nombre_cliente, cl.apellidos AS apellidos_cliente, " +
                     "t.nombre AS nombre_artista, t.apellidos AS apellidos_artista " +
                     "FROM CITAS c " +
                     "LEFT JOIN CLIENTES cl ON c.id_cliente = cl.id_cliente " +
                     "LEFT JOIN TATUADORES t ON c.id_artista = t.id_artista";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet resultado = st.executeQuery(sql);

            while (resultado.next()) {
                Cita cita = new Cita(
                    resultado.getInt("id_cita"),
                    resultado.getInt("id_cliente"),
                    resultado.getInt("id_artista"),
                    resultado.getDate("fecha_cita"),
                    resultado.getInt("duracion_aproximada"),
                    resultado.getDouble("precio"),
                    Cita.EstadoCita.fromString(resultado.getString("estado")),
                    resultado.getString("sala"),
                    resultado.getBytes("foto_diseno"),
                    resultado.getString("notas")
                );
                cita.setNombreCompletoCliente(resultado.getString("nombre_cliente"), resultado.getString("apellidos_cliente"));
                cita.setNombreCompletoArtista(resultado.getString("nombre_artista"), resultado.getString("apellidos_artista"));
                listaCitas.add(cita);
            }
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al cargar citas: " + e.getMessage());
            e.printStackTrace();
        }
        return listaCitas;
    }

    public boolean insertarCita(Cita cita) throws DatabaseConnectionException {
        String sql = "INSERT INTO CITAS (id_cliente, id_artista, fecha_cita, duracion_aproximada, precio, estado, sala, foto_diseno, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, cita.getId_cliente());
            pst.setInt(2, cita.getId_artista());
            pst.setDate(3, cita.getFecha_cita());
            pst.setInt(4, cita.getDuracion_aproximada());
            pst.setDouble(5, cita.getPrecio());
            pst.setString(6, cita.getEstado().getValor());
            pst.setString(7, cita.getSala());
            pst.setBytes(8, cita.getFoto_diseno());
            pst.setString(9, cita.getNotas());

            int filasAfectadas = pst.executeUpdate();
            return filasAfectadas > 0;
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al insertar cita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarCita(Cita cita) throws DatabaseConnectionException {
        String sql = "UPDATE CITAS SET id_cliente = ?, id_artista = ?, fecha_cita = ?, duracion_aproximada = ?, precio = ?, estado = ?, sala = ?, foto_diseno = ?, notas = ? WHERE id_cita = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, cita.getId_cliente());
            pst.setInt(2, cita.getId_artista());
            pst.setDate(3, cita.getFecha_cita());
            pst.setInt(4, cita.getDuracion_aproximada());
            pst.setDouble(5, cita.getPrecio());
            pst.setString(6, cita.getEstado().getValor());
            pst.setString(7, cita.getSala());
            pst.setBytes(8, cita.getFoto_diseno());
            pst.setString(9, cita.getNotas());
            pst.setInt(10, cita.getId_cita());

            int filasAfectadas = pst.executeUpdate();
            return filasAfectadas > 0;
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al actualizar cita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarCita(int idCita) throws DatabaseConnectionException {
        String sql = "DELETE FROM CITAS WHERE id_cita = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, idCita);

            int filasAfectadas = pst.executeUpdate();
            return filasAfectadas > 0;
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException e) {
            System.out.println("Error al eliminar cita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Cita> buscarCitas(String criterio, String valor, Date fechaInicio, Date fechaFin) throws DatabaseConnectionException {
        List<Cita> listaCitas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT c.*, " +
                                              "cl.nombre AS nombre_cliente, cl.apellidos AS apellidos_cliente, " +
                                              "t.nombre AS nombre_artista, t.apellidos AS apellidos_artista " +
                                              "FROM CITAS c " +
                                              "LEFT JOIN CLIENTES cl ON c.id_cliente = cl.id_cliente " +
                                              "LEFT JOIN TATUADORES t ON c.id_artista = t.id_artista " +
                                              "WHERE 1=1");

        if (criterio != null && valor != null && !valor.trim().isEmpty()) {
            switch (criterio.toLowerCase()) {
                case "cliente":
                    sql.append(" AND (LOWER(cl.nombre) LIKE ? OR LOWER(cl.apellidos) LIKE ? OR LOWER(CONCAT(cl.nombre, ' ', cl.apellidos)) LIKE ?)");
                    break;
                case "artista":
                    sql.append(" AND (LOWER(t.nombre) LIKE ? OR LOWER(t.apellidos) LIKE ? OR LOWER(CONCAT(t.nombre, ' ', t.apellidos)) LIKE ?)");
                    break;
                case "estado":
                    sql.append(" AND LOWER(c.estado) LIKE ?");
                    break;
                case "sala":
                    sql.append(" AND LOWER(c.sala) LIKE ?");
                    break;
            }
        }

        if (fechaInicio != null && fechaFin != null) {
            sql.append(" AND c.fecha_cita BETWEEN ? AND ?");
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            if (criterio != null && valor != null && !valor.trim().isEmpty()) {
                String valorBusqueda = "%" + valor.toLowerCase() + "%";
                if (criterio.equalsIgnoreCase("cliente") || criterio.equalsIgnoreCase("artista")) {
                    // Para cliente y artista, buscamos en nombre, apellidos y nombre completo
                    pst.setString(paramIndex++, valorBusqueda);
                    pst.setString(paramIndex++, valorBusqueda);
                    pst.setString(paramIndex++, valorBusqueda);
                } else {
                    pst.setString(paramIndex++, valorBusqueda);
                }
            }

            if (fechaInicio != null && fechaFin != null) {
                pst.setDate(paramIndex++, fechaInicio);
                pst.setDate(paramIndex++, fechaFin);
            }

            ResultSet resultado = pst.executeQuery();

            while (resultado.next()) {
                Cita cita = new Cita(
                    resultado.getInt("id_cita"),
                    resultado.getInt("id_cliente"),
                    resultado.getInt("id_artista"),
                    resultado.getDate("fecha_cita"),
                    resultado.getInt("duracion_aproximada"),
                    resultado.getDouble("precio"),
                    Cita.EstadoCita.fromString(resultado.getString("estado")),
                    resultado.getString("sala"),
                    resultado.getBytes("foto_diseno"),
                    resultado.getString("notas")
                );
                cita.setNombreCompletoCliente(resultado.getString("nombre_cliente"), resultado.getString("apellidos_cliente"));
                cita.setNombreCompletoArtista(resultado.getString("nombre_artista"), resultado.getString("apellidos_artista"));
                listaCitas.add(cita);
            }
        } catch (DatabaseConnectionException e) {
            throw e;
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error al buscar citas: " + e.getMessage());
            e.printStackTrace();
        }
        return listaCitas;
    }
}
