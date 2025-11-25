package com.javafx.controladores;

import java.net.URL;
import java.util.ResourceBundle;

import com.javafx.dao.TatuadorDAO;
import com.javafx.modelos.Tatuador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ventanaAETatuadorController implements Initializable {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelefono;

    @FXML
    private CheckBox checkActivo;

    private TatuadorDAO tatuadorDAO;
    private Tatuador tatuadorAEditar;
    private boolean modoEdicion = false;

    public void setTatuadorAEditar(Tatuador tatuador) {
        this.tatuadorAEditar = tatuador;
        this.modoEdicion = true;
        cargarDatosTatuador();
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
            boolean activo = checkActivo.isSelected();

            if (nombre.isEmpty() || apellidos.isEmpty()) {
                mostrarAlerta("Error", "El nombre y apellidos son obligatorios", Alert.AlertType.ERROR);
                return;
            }

            boolean exito;
            if (modoEdicion) {
                tatuadorAEditar.setNombre(nombre);
                tatuadorAEditar.setApellidos(apellidos);
                tatuadorAEditar.setEmail(email);
                tatuadorAEditar.setTelefono(telefono);
                tatuadorAEditar.setActivo(activo);

                exito = tatuadorDAO.actualizarTatuador(tatuadorAEditar);
            } else {
                Tatuador nuevoTatuador = new Tatuador(0, nombre, apellidos, telefono, email, activo);
                exito = tatuadorDAO.insertarTatuador(nuevoTatuador);
            }

            if (exito) {
                mostrarAlerta("Éxito", modoEdicion ? "Tatuador actualizado correctamente" : "Tatuador creado correctamente", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error", "No se pudo " + (modoEdicion ? "actualizar" : "crear") + " el tatuador", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tatuadorDAO = new TatuadorDAO();
        checkActivo.setSelected(true);
    }

    private void cargarDatosTatuador() {
        if (tatuadorAEditar != null) {
            txtNombre.setText(tatuadorAEditar.getNombre());
            txtApellidos.setText(tatuadorAEditar.getApellidos());
            txtEmail.setText(tatuadorAEditar.getEmail());
            txtTelefono.setText(tatuadorAEditar.getTelefono());
            checkActivo.setSelected(tatuadorAEditar.isActivo());
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
