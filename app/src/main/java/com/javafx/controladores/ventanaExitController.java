package com.javafx.controladores;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ventanaExitController implements Initializable{

    @FXML
    void botonNo(MouseEvent event) {
        // Cerrar solo la ventana de confirmación
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void botonSi(MouseEvent event) {
        // Cerrar toda la aplicación
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización de la ventana
    }

}
