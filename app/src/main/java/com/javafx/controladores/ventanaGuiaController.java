package com.javafx.controladores;

import com.dlsc.pdfviewfx.PDFView;
import com.javafx.exceptions.DatabaseConnectionException;
import com.javafx.utils.ErrorUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;

public class ventanaGuiaController {

    @FXML
    private StackPane pdfContainer;

    @FXML
    public void initialize() {
        try {
            // Verificar que el recurso existe
            InputStream pdfStream = getClass().getResourceAsStream("/guia.pdf");

            if (pdfStream == null) {
                mostrarError("No se encontró el archivo guia.pdf en los recursos de la aplicación.");
                return;
            }

            // Crear y configurar el visor PDF
            PDFView pdfView = new PDFView();
            pdfView.load(pdfStream);

            // Añadir al contenedor
            pdfContainer.getChildren().add(pdfView);

        } catch (DatabaseConnectionException e) {
            ErrorUtils.mostrarErrorConectividad(e);
        } catch (Exception e) {
            System.err.println("Error al cargar el PDF: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar el archivo PDF: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Label errorLabel = new Label(mensaje);
        errorLabel.setTextAlignment(TextAlignment.CENTER);
        errorLabel.setMaxWidth(400);
        pdfContainer.getChildren().add(errorLabel);
    }
}
