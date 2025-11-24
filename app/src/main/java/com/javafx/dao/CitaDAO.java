package com.javafx.dao;

import java.sql.Connection;
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
}
