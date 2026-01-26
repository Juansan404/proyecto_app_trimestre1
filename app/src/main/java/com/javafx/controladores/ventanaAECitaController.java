package com.javafx.controladores;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.javafx.dao.CitaDAO;
import com.javafx.dao.ClienteDAO;
import com.javafx.dao.TatuadorDAO;
import com.javafx.modelos.Cita;
import com.javafx.modelos.Cliente;
import com.javafx.modelos.Tatuador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import com.javafx.utils.ImageUtils;
import com.javafx.utils.StageUtils;
import com.javafx.utils.CSSUtils;
import com.javafx.utils.AnimationUtils;
import com.javafx.utils.ErrorUtils;
import com.javafx.exceptions.DatabaseConnectionException;

public class ventanaAECitaController implements Initializable {

    @FXML
    private TextField txtCliente;

    @FXML
    private Button btnBuscarCliente;

    @FXML
    private ChoiceBox<Tatuador> choiceTatuador;

    @FXML
    private DatePicker dateFecha;

    @FXML
    private ChoiceBox<String> choiceSala;

    @FXML
    private TextField txtDuracion;

    @FXML
    private TextField txtPrecio;

    @FXML
    private ChoiceBox<String> choiceEstado;

    @FXML
    private TextArea txtNotas;

    @FXML
    private Button btnSeleccionarImagen;

    @FXML
    private Button btnEliminarImagen;

    @FXML
    private ImageView imageViewPreview;

    private CitaDAO citaDAO;
    private ClienteDAO clienteDAO;
    private TatuadorDAO tatuadorDAO;
    private Cita citaAEditar;
    private boolean modoEdicion = false;
    private Cliente clienteSeleccionado;
    private ValidationSupport validationSupport;
    private byte[] imagenSeleccionada;

    public void setCitaAEditar(Cita cita) {
        this.citaAEditar = cita;
        this.modoEdicion = true;
        cargarDatosCita();
    }

    @FXML
    void buttonBuscarCliente(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanaBCliente.fxml"));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
            Parent root = loader.load();

            ventanaBClienteController controller = loader.getController();
            controller.setControladorCita(this);

            Stage stage = new Stage();
            stage.setTitle("Buscar Cliente");
            Scene scene = new Scene(root);
            CSSUtils.aplicarEstilos(scene);
            stage.setScene(scene);
            StageUtils.setAppIcon(stage);
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al abrir ventana de búsqueda: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setClienteSeleccionado(Cliente cliente) {
        this.clienteSeleccionado = cliente;
        if (cliente != null) {
            txtCliente.setText(cliente.getNombre() + " " + cliente.getApellidos());
        }
    }

    @FXML
    void buttonCancelar(MouseEvent event) {
        cerrarVentana();
    }

    @FXML
    void buttonSeleccionarImagen(MouseEvent event) {
        Stage stage = (Stage) txtCliente.getScene().getWindow();
        byte[] imagen = ImageUtils.seleccionarYConvertirImagen(stage);

        if (imagen != null) {
            imagenSeleccionada = imagen;
            // Mostrar preview
            ImageUtils.mostrarImagenEnImageView(imageViewPreview, imagen, 96, 96);
            mostrarAlerta("Éxito", "Imagen cargada correctamente", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    void buttonEliminarImagen(MouseEvent event) {
        imagenSeleccionada = null;
        imageViewPreview.setImage(null);
        mostrarAlerta("Éxito", "Imagen eliminada", Alert.AlertType.INFORMATION);
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
            Tatuador tatuadorSeleccionado = choiceTatuador.getValue();
            LocalDate fechaLocal = dateFecha.getValue();
            int duracion = Integer.parseInt(txtDuracion.getText().trim());
            String precioTexto = txtPrecio.getText().trim().replace(',', '.');
            double precio = Double.parseDouble(precioTexto);
            String estadoStr = choiceEstado.getValue();
            String sala = choiceSala.getValue();
            String notas = txtNotas.getText().trim();

            Date fechaSQL = Date.valueOf(fechaLocal);
            Cita.EstadoCita estado = Cita.EstadoCita.fromString(estadoStr);

            boolean exito;
            if (modoEdicion) {
                citaAEditar.setId_cliente(clienteSeleccionado.getId_cliente());
                citaAEditar.setId_artista(tatuadorSeleccionado.getId_artista());
                citaAEditar.setFecha_cita(fechaSQL);
                citaAEditar.setDuracion_aproximada(duracion);
                citaAEditar.setPrecio(precio);
                citaAEditar.setEstado(estado);
                citaAEditar.setSala(sala);
                citaAEditar.setNotas(notas);
                citaAEditar.setFoto_diseno(imagenSeleccionada);

                exito = citaDAO.actualizarCita(citaAEditar);
            } else {
                Cita nuevaCita = new Cita(0, clienteSeleccionado.getId_cliente(), tatuadorSeleccionado.getId_artista(),
                    fechaSQL, duracion, precio, estado, sala, imagenSeleccionada, notas);
                exito = citaDAO.insertarCita(nuevaCita);
            }

            if (exito) {
                mostrarAlerta("Éxito", modoEdicion ? "Cita actualizada correctamente" : "Cita creada correctamente", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error", "No se pudo " + (modoEdicion ? "actualizar" : "crear") + " la cita", Alert.AlertType.ERROR);
            }
        } catch (DatabaseConnectionException e) {
            ErrorUtils.mostrarErrorConectividad(e);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        citaDAO = new CitaDAO();
        clienteDAO = new ClienteDAO();
        tatuadorDAO = new TatuadorDAO();

        cargarTatuadores();
        configurarChoiceSala();
        configurarChoiceEstado();
        configurarValidaciones();

        // Detectar Enter en los campos de texto para guardar
        txtDuracion.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonGuardar(null);
            }
        });
        txtPrecio.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonGuardar(null);
            }
        });
    }

    private void configurarValidaciones() {
        validationSupport = new ValidationSupport();

        // Configurar tooltips en los controles para mostrar ayuda al pasar el ratón
        txtCliente.setTooltip(new Tooltip("Debe buscar y seleccionar un cliente"));
        choiceTatuador.setTooltip(new Tooltip("Seleccione un tatuador"));
        dateFecha.setTooltip(new Tooltip("Seleccione la fecha de la cita"));
        txtDuracion.setTooltip(new Tooltip("Duración aproximada en minutos (número entero positivo)"));
        txtPrecio.setTooltip(new Tooltip("Precio de la cita (número decimal positivo)"));
        choiceSala.setTooltip(new Tooltip("Seleccione la sala (A, B, C o D)"));
        choiceEstado.setTooltip(new Tooltip("Seleccione el estado de la cita"));

        // Validador para cliente (obligatorio - debe haber cliente seleccionado)
        Validator<String> clienteValidator = (control, value) -> {
            if (clienteSeleccionado == null) {
                return ValidationResult.fromError(control, "Debe buscar y seleccionar un cliente");
            }
            return null;
        };

        // Validador para tatuador (obligatorio)
        Validator<Object> tatuadorValidator = (control, value) -> {
            if (value == null) {
                return ValidationResult.fromError(control, "Debe seleccionar un tatuador");
            }
            return null;
        };

        // Validador para fecha (obligatorio)
        Validator<LocalDate> fechaValidator = (control, value) -> {
            if (value == null) {
                return ValidationResult.fromError(control, "Debe seleccionar una fecha");
            }
            return null;
        };

        // Validador para duración (obligatorio, número entero positivo)
        Validator<String> duracionValidator = (control, value) -> {
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.fromError(control, "La duración es obligatoria");
            }
            try {
                int num = Integer.parseInt(value.trim());
                if (num <= 0) {
                    return ValidationResult.fromError(control, "Debe ser un número positivo");
                }
            } catch (NumberFormatException e) {
                return ValidationResult.fromError(control, "Debe ser un número entero");
            }
            return null;
        };

        // Validador para precio (obligatorio, número decimal positivo)
        Validator<String> precioValidator = (control, value) -> {
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.fromError(control, "El precio es obligatorio");
            }
            try {
                double num = Double.parseDouble(value.trim().replace(',', '.'));
                if (num <= 0) {
                    return ValidationResult.fromError(control, "Debe ser un número positivo");
                }
            } catch (NumberFormatException e) {
                return ValidationResult.fromError(control, "Debe ser un número decimal válido");
            }
            return null;
        };

        // Validador para sala (obligatorio)
        Validator<Object> salaValidator = (control, value) -> {
            if (value == null) {
                return ValidationResult.fromError(control, "Debe seleccionar una sala");
            }
            return null;
        };

        // Validador para estado (obligatorio)
        Validator<Object> estadoValidator = (control, value) -> {
            if (value == null) {
                return ValidationResult.fromError(control, "Debe seleccionar un estado");
            }
            return null;
        };

        // Registrar validadores
        validationSupport.registerValidator(txtCliente, false, clienteValidator);
        validationSupport.registerValidator(choiceTatuador, false, tatuadorValidator);
        validationSupport.registerValidator(dateFecha, false, fechaValidator);
        validationSupport.registerValidator(txtDuracion, false, duracionValidator);
        validationSupport.registerValidator(txtPrecio, false, precioValidator);
        validationSupport.registerValidator(choiceSala, false, salaValidator);
        validationSupport.registerValidator(choiceEstado, false, estadoValidator);
    }

    private void cargarTatuadores() {
        try {
            List<Tatuador> tatuadores = tatuadorDAO.cargarTatuadores();
            choiceTatuador.setItems(FXCollections.observableArrayList(tatuadores));
            choiceTatuador.setConverter(new javafx.util.StringConverter<Tatuador>() {
                @Override
                public String toString(Tatuador tatuador) {
                    return tatuador != null ? tatuador.getNombre() + " " + tatuador.getApellidos() : "";
                }

                @Override
                public Tatuador fromString(String string) {
                    return null;
                }
            });
        } catch (DatabaseConnectionException e) {
            ErrorUtils.mostrarErrorConectividad(e);
        }
    }

    private void configurarChoiceSala() {
        choiceSala.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
    }

    private void configurarChoiceEstado() {
        choiceEstado.setItems(FXCollections.observableArrayList(
            "Pendiente", "Confirmada", "Cancelada", "Completada"
        ));
    }

    private void cargarDatosCita() {
        if (citaAEditar != null) {
            try {
                // Cargar cliente
                Cliente cliente = clienteDAO.obtenerClientePorId(citaAEditar.getId_cliente());
                if (cliente != null) {
                    clienteSeleccionado = cliente;
                    txtCliente.setText(cliente.getNombre() + " " + cliente.getApellidos());
                }

                // Cargar tatuador
                for (Tatuador t : choiceTatuador.getItems()) {
                    if (t.getId_artista() == citaAEditar.getId_artista()) {
                        choiceTatuador.setValue(t);
                        break;
                    }
                }

                dateFecha.setValue(citaAEditar.getFecha_cita().toLocalDate());
                txtDuracion.setText(String.valueOf(citaAEditar.getDuracion_aproximada()));
                txtPrecio.setText(String.valueOf(citaAEditar.getPrecio()));
                choiceEstado.setValue(citaAEditar.getEstado().getValor());
                choiceSala.setValue(citaAEditar.getSala());
                txtNotas.setText(citaAEditar.getNotas());

                // Cargar imagen si existe
                if (citaAEditar.getFoto_diseno() != null && citaAEditar.getFoto_diseno().length > 0) {
                    imagenSeleccionada = citaAEditar.getFoto_diseno();
                    ImageUtils.mostrarImagenEnImageView(imageViewPreview, imagenSeleccionada, 96, 96);
                }
            } catch (DatabaseConnectionException e) {
                ErrorUtils.mostrarErrorConectividad(e);
            }
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
        Stage stage = (Stage) txtCliente.getScene().getWindow();
        stage.close();
    }

}
