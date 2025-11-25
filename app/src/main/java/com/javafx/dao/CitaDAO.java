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
import com.javafx.modelos.Cita;

public class CitaDAO {

    public List<Cita> cargarCitas() {
        List<Cita> listaCitas = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM CITAS");

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
                listaCitas.add(cita);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar citas: " + e.getMessage());
            e.printStackTrace();
        }
        return listaCitas;
    }

    public boolean insertarCita(Cita cita) {
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
        } catch (SQLException e) {
            System.out.println("Error al insertar cita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarCita(Cita cita) {
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
        } catch (SQLException e) {
            System.out.println("Error al actualizar cita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarCita(int idCita) {
        String sql = "DELETE FROM CITAS WHERE id_cita = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, idCita);

            int filasAfectadas = pst.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar cita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Cita> buscarCitas(String criterio, String valor, Date fechaInicio, Date fechaFin) {
        List<Cita> listaCitas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM CITAS WHERE 1=1");

        if (criterio != null && valor != null && !valor.trim().isEmpty()) {
            switch (criterio.toLowerCase()) {
                case "cliente":
                    sql.append(" AND id_cliente = ?");
                    break;
                case "artista":
                    sql.append(" AND id_artista = ?");
                    break;
                case "estado":
                    sql.append(" AND estado = ?");
                    break;
                case "sala":
                    sql.append(" AND sala = ?");
                    break;
            }
        }

        if (fechaInicio != null && fechaFin != null) {
            sql.append(" AND fecha_cita BETWEEN ? AND ?");
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            if (criterio != null && valor != null && !valor.trim().isEmpty()) {
                if (criterio.equalsIgnoreCase("cliente") || criterio.equalsIgnoreCase("artista")) {
                    pst.setInt(paramIndex++, Integer.parseInt(valor));
                } else {
                    pst.setString(paramIndex++, valor);
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
                listaCitas.add(cita);
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error al buscar citas: " + e.getMessage());
            e.printStackTrace();
        }
        return listaCitas;
    }
}
