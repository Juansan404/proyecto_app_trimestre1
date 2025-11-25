package com.javafx.controladores;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

import com.javafx.dao.ClienteDAO;
import com.javafx.modelos.Cliente;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ventanaAEClienteController implements Initializable {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelefono;

    @FXML
    private DatePicker dateFechaNac;

    @FXML
    private TextArea txtNotas;

    private ClienteDAO clienteDAO;
    private Cliente clienteAEditar;
    private boolean modoEdicion = false;

    public void setClienteAEditar(Cliente cliente) {
        this.clienteAEditar = cliente;
        this.modoEdicion = true;
        cargarDatosCliente();
    }

    @FXML
    void buttonCancelar(MouseEvent event) {
        cerrarVentana();
    }

    @FXML
    void buttonGuardar(MouseEvent event) {
        try {
            String nombre = txtNombre.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();
            LocalDate fechaLocal = dateFechaNac.getValue();
            String notas = txtNotas.getText().trim();

            if (nombre.isEmpty() || apellidos.isEmpty()) {
                mostrarAlerta("Error", "El nombre y apellidos son obligatorios", Alert.AlertType.ERROR);
                return;
            }

            Date fechaSQL = fechaLocal != null ? Date.valueOf(fechaLocal) : null;

            boolean exito;
            if (modoEdicion) {
                clienteAEditar.setNombre(nombre);
                clienteAEditar.setApellidos(apellidos);
                clienteAEditar.setEmail(email);
                clienteAEditar.setTelefono(telefono);
                clienteAEditar.setFecha_nac(fechaSQL);
                clienteAEditar.setNotas(notas);

                exito = clienteDAO.actualizarCliente(clienteAEditar);
            } else {
                Cliente nuevoCliente = new Cliente(0, nombre, apellidos, telefono, email, fechaSQL, notas);
                exito = clienteDAO.insertarCliente(nuevoCliente);
            }

            if (exito) {
                mostrarAlerta("Éxito", modoEdicion ? "Cliente actualizado correctamente" : "Cliente creado correctamente", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error", "No se pudo " + (modoEdicion ? "actualizar" : "crear") + " el cliente", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clienteDAO = new ClienteDAO();
    }

    private void cargarDatosCliente() {
        if (clienteAEditar != null) {
            txtNombre.setText(clienteAEditar.getNombre());
            txtApellidos.setText(clienteAEditar.getApellidos());
            txtEmail.setText(clienteAEditar.getEmail());
            txtTelefono.setText(clienteAEditar.getTelefono());
            if (clienteAEditar.getFecha_nac() != null) {
                dateFechaNac.setValue(clienteAEditar.getFecha_nac().toLocalDate());
            }
            txtNotas.setText(clienteAEditar.getNotas());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

}
