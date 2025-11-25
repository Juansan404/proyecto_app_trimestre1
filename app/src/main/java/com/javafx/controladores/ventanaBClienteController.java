package com.javafx.controladores;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.javafx.dao.ClienteDAO;
import com.javafx.modelos.Cliente;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ventanaBClienteController implements Initializable {

    @FXML
    private ComboBox<String> comboCriterio;

    @FXML
    private TextField txtValor;

    private ClienteDAO clienteDAO;
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

        List<Cliente> resultados = clienteDAO.buscarClientes(criterio, valor);

        if (resultados.isEmpty()) {
            mostrarAlerta("Sin resultados", "No se encontraron clientes con los criterios especificados", Alert.AlertType.INFORMATION);
        } else {
            if (controladorPrincipal != null) {
                controladorPrincipal.actualizarTablaClientesConResultados(resultados);
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
        clienteDAO = new ClienteDAO();

        comboCriterio.setItems(FXCollections.observableArrayList(
            "Nombre", "Apellidos", "Email", "Telefono"
        ));
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
