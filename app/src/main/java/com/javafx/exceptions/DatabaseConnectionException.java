package com.javafx.exceptions;

public class DatabaseConnectionException extends RuntimeException {

    public DatabaseConnectionException(String mensaje) {
        super(mensaje);
    }

    public DatabaseConnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
