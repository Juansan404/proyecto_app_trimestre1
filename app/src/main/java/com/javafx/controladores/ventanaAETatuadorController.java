package com.javafx.controladores;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

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
    private ValidationSupport validationSupport;

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
        // Verificar validaciones
        if (validationSupport.isInvalid()) {
            mostrarAlerta("Error de Validación",
                          "Por favor, corrija los errores en el formulario antes de guardar",
                          Alert.AlertType.ERROR);
            return;
        }

        try {
            String nombre = txtNombre.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();
            boolean activo = checkActivo.isSelected();

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
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        validationSupport = new ValidationSupport();


        // Validador para nombre
        Validator<String> nombreValidator = (control, value) -> {
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.fromError(control, "El nombre es obligatorio");
            }
            return null;
        };

        // Validador para apellidos
        Validator<String> apellidosValidator = (control, value) -> {
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.fromError(control, "Los apellidos son obligatorios");
            }
            return null;
        };

        // Validador para email
        Validator<String> emailValidator = (control, value) -> {
            if (value != null && !value.trim().isEmpty()) {
                String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
                if (!value.matches(emailRegex)) {
                    return ValidationResult.fromError(control, "Email no válido");
                }
            }
            return null;
        };

        // Validador para teléfono (si tiene valor debe tener 9 dígitos)
        Validator<String> telefonoValidator = (control, value) -> {
            if (value != null && !value.trim().isEmpty()) {
                if (!value.matches("\\d{9}")) {
                    return ValidationResult.fromError(control, "El teléfono debe tener 9 dígitos");
                }
            }
            return null;
        };

        validationSupport.registerValidator(txtNombre, nombreValidator);
        validationSupport.registerValidator(txtApellidos, apellidosValidator);
        validationSupport.registerValidator(txtEmail, emailValidator);
        validationSupport.registerValidator(txtTelefono, telefonoValidator);
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
