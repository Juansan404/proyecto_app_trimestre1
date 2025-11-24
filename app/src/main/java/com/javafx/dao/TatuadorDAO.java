package com.javafx.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.javafx.database.DatabaseConnection;
import com.javafx.modelos.Tatuador;

public class TatuadorDAO {

    public List<Tatuador> cargarTatuadores() {
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
        } catch (SQLException e) {
            System.out.println("Error al cargar tatuadores: " + e.getMessage());
            e.printStackTrace();
        }
        return listaTatuadores;
    }
}
