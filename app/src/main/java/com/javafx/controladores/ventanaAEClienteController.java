package com.javafx.controladores;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.javafx.dao.ClienteDAO;
import com.javafx.modelos.Cliente;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
    private ValidationSupport validationSupport;

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
            LocalDate fechaLocal = dateFechaNac.getValue();
            String notas = txtNotas.getText().trim();

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

        // Validador para teléfono (valor debe tener 9 dígitos)
        Validator<String> telefonoValidator = (control, value) -> {
            if (value != null && !value.trim().isEmpty()) {
                if (!value.matches("\\d{9}")) {
                    return ValidationResult.fromError(control, "El teléfono debe tener 9 dígitos");
                }
            }
            return null;
        };

        // Registrar validadores
        validationSupport.registerValidator(txtNombre, false, nombreValidator);
        validationSupport.registerValidator(txtApellidos, false, apellidosValidator);
        validationSupport.registerValidator(txtEmail, false, emailValidator);
        validationSupport.registerValidator(txtTelefono, false, telefonoValidator);
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
