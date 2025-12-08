package com.javafx.controladores;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.javafx.dao.TatuadorDAO;
import com.javafx.modelos.Tatuador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ventanaBTatuadorController implements Initializable {

    @FXML
    private ComboBox<String> comboCriterio;

    @FXML
    private TextField txtValor;

    private TatuadorDAO tatuadorDAO;
    private miControlador controladorPrincipal;

    public void setControladorPrincipal(miControlador controlador) {
        this.controladorPrincipal = controlador;
    }

    @FXML
    void buttonAceptar(MouseEvent event) {
        String criterio = comboCriterio.getValue();
        String valor = txtValor.getText().trim();

        if (criterio == null || valor.isEmpty()) {
            mostrarAlerta("Advertencia", "Debe seleccionar un criterio e ingresar un valor de búsqueda", Alert.AlertType.WARNING);
            return;
        }

        List<Tatuador> resultados = tatuadorDAO.buscarTatuadores(criterio, valor);

        if (resultados.isEmpty()) {
            mostrarAlerta("Sin resultados", "No se encontraron tatuadores con los criterios especificados", Alert.AlertType.INFORMATION);
        } else {
            if (controladorPrincipal != null) {
                controladorPrincipal.actualizarTablaTatuadoresConResultados(resultados);
            }
            cerrarVentana();
        }
    }

    @FXML
    void buttonCancelar(MouseEvent event) {
        cerrarVentana();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tatuadorDAO = new TatuadorDAO();

        comboCriterio.setItems(FXCollections.observableArrayList(
            "Nombre", "Apellidos", "Email", "Activo"
        ));

        // Detectar Enter en el campo de texto
        txtValor.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonAceptar(null);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) comboCriterio.getScene().getWindow();
        stage.close();
    }

}
