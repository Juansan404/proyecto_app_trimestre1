package com.javafx.utils;

import com.javafx.exceptions.DatabaseConnectionException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorUtils {

    /**
     * Muestra una alerta de error de conectividad con la base de datos
     * @param e La excepción de conexión a la base de datos
     */
    public static void mostrarErrorConectividad(DatabaseConnectionException e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error de Conectividad");
        alert.setHeaderText("No se pudo conectar con la base de datos");
        alert.setContentText(
            "No se ha podido establecer conexión con la base de datos.\n\n" +
            "Posibles causas:\n" +
            "• El servidor de base de datos no está activo\n" +
            "• La configuración de conexión es incorrecta\n" +
            "• Problemas de red o firewall\n\n" +
            "Por favor, verifique la configuración y el estado del servidor."
        );

        // Crear área expandible con los detalles del error
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de error genérica
     * @param titulo El título de la alerta
     * @param mensaje El mensaje de la alerta
     */
    public static void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de error con detalles de la excepción
     * @param titulo El título de la alerta
     * @param mensaje El mensaje de la alerta
     * @param e La excepción que causó el error
     */
    public static void mostrarErrorConDetalles(String titulo, String mensaje, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(mensaje);
        alert.setContentText("Error: " + e.getMessage());

        // Crear área expandible con los detalles del error
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);
        alert.showAndWait();
    }
}
