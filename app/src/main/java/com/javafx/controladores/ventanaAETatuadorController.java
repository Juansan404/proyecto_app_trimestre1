package com.javafx.controladores;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.javafx.dao.TatuadorDAO;
import com.javafx.modelos.Tatuador;
import com.javafx.utils.AnimationUtils;
import com.javafx.utils.StageUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
            // Aplicar animación shake a los campos con errores
            validationSupport.getRegisteredControls().forEach(control -> {
                if (validationSupport.getValidationResult().getErrors().stream()
                        .anyMatch(error -> error.getTarget() == control)) {
                    AnimationUtils.shake(control);
                }
            });
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

        // Detectar Enter en los campos de texto para guardar
        txtNombre.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonGuardar(null);
            }
        });
        txtApellidos.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonGuardar(null);
            }
        });
        txtEmail.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonGuardar(null);
            }
        });
        txtTelefono.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonGuardar(null);
            }
        });
    }

    private void configurarValidaciones() {
        validationSupport = new ValidationSupport();


        // Configurar tooltips en los controles para mostrar ayuda al pasar el ratón
        txtNombre.setTooltip(new Tooltip("El nombre es obligatorio"));
        txtApellidos.setTooltip(new Tooltip("Los apellidos son obligatorios"));
        txtEmail.setTooltip(new Tooltip("Formato: <texto>@<texto>.<texto>"));
        txtTelefono.setTooltip(new Tooltip("Debe contener exactamente 9 dígitos"));

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

        validationSupport.registerValidator(txtNombre, false, nombreValidator);
        validationSupport.registerValidator(txtApellidos, false, apellidosValidator);
        validationSupport.registerValidator(txtEmail, false, emailValidator);
        validationSupport.registerValidator(txtTelefono, false, telefonoValidator);
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
        Stage alertStage = (Stage) alerta.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);
        alerta.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

}
