package com.javafx.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static Connection conexion = null;

    static {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new IOException("No se pudo encontrar el archivo database.properties");
            }
            props.load(input);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la configuración de la base de datos: " + e.getMessage(), e);
        }
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
} 
