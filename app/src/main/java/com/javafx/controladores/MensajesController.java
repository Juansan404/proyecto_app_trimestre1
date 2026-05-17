package com.javafx.controladores;

import com.javafx.model.Mensaje;
import com.javafx.model.SolicitudCita;
import com.javafx.model.Usuario;
import com.javafx.service.MensajeService;
import com.javafx.service.SolicitudService;
import com.javafx.service.UsuarioService;
import com.javafx.utils.AnimationUtils;
import com.javafx.utils.DetalleUtils;
import com.javafx.utils.DialogUtils;
import javafx.event.ActionEvent;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

public class MensajesController {

    @FXML private TableView<Mensaje>              tableView;
    @FXML private TableColumn<Mensaje, Long>      colId;
    @FXML private TableColumn<Mensaje, Long>      colSolicitud;
    @FXML private TableColumn<Mensaje, String>    colRemitente;
    @FXML private TableColumn<Mensaje, String>    colContenido;
    @FXML private TableColumn<Mensaje, String>    colFecha;
    @FXML private TextField txtBuscar;
    @FXML private Label     lblStatus;
    @FXML private ProgressIndicator loadingSpinner;

    private final MensajeService   service          = new MensajeService();
    private final UsuarioService   usuarioService   = new UsuarioService();
    private final SolicitudService solicitudService = new SolicitudService();
    private final ObservableList<Mensaje> todos     = FXCollections.observableArrayList();
    private final ObservableList<Mensaje> mostrados = FXCollections.observableArrayList();
    private List<Usuario>       usuarios    = List.of();
    private List<SolicitudCita> solicitudes = List.of();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdMensaje()));
        colSolicitud.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdSolicitud()));
        colRemitente.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreRemitente()));
        colContenido.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getContenido()));
        colFecha.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCreadoEn() != null && data.getValue().getCreadoEn().length() >= 16
                                ? data.getValue().getCreadoEn().substring(0, 16).replace("T", " ")
                                : ""));

        tableView.setItems(mostrados);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Mensaje sel = tableView.getSelectionModel().getSelectedItem();
                if (sel != null) DetalleUtils.mostrar(sel);
            }
        });
        txtBuscar.textProperty().addListener((o, v, n) -> filtrar());
        cargarAuxiliares();
        cargar();
    }

    private void cargarAuxiliares() {
        Task<List<Usuario>> tu = new Task<>() {
            @Override protected List<Usuario> call() throws Exception { return usuarioService.listarUsuarios(); }
        };
        tu.setOnSucceeded(e -> usuarios = tu.getValue());
        new Thread(tu).start();

        Task<List<SolicitudCita>> ts = new Task<>() {
            @Override protected List<SolicitudCita> call() throws Exception { return solicitudService.listarSolicitudes(); }
        };
        ts.setOnSucceeded(e -> solicitudes = ts.getValue());
        new Thread(ts).start();
    }

    private void filtrar() {
        String q = txtBuscar.getText().toLowerCase();
        mostrados.setAll(todos.stream().filter(m ->
                q.isEmpty()
                || m.getNombreRemitente().toLowerCase().contains(q)
                || (m.getContenido() != null && m.getContenido().toLowerCase().contains(q))
        ).toList());
        lblStatus.setText(mostrados.size() + " mensajes");
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        Task<List<Mensaje>> task = new Task<>() {
            @Override protected List<Mensaje> call() throws Exception {
                return service.listarMensajes();
            }
        };
        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            todos.setAll(task.getValue());
            filtrar();
        });
        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            mostrarError("Error al cargar mensajes", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnCrear() {
        Dialog<Mensaje> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Mensaje");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        ColumnConstraints cc0 = new ColumnConstraints(); cc0.setHgrow(Priority.NEVER);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setHgrow(Priority.NEVER); cc1.setMaxWidth(280);
        grid.getColumnConstraints().addAll(cc0, cc1);

        ComboBox<SolicitudCita> cbSolicitud = new ComboBox<>(FXCollections.observableArrayList(solicitudes));
        ComboBox<Usuario>       cbRemitente = new ComboBox<>(FXCollections.observableArrayList(usuarios));
        cbSolicitud.setPrefWidth(260);
        cbRemitente.setPrefWidth(260);
        TextArea fContenido = new TextArea(); fContenido.setPrefRowCount(3); fContenido.setPrefWidth(260);

        grid.add(new Label("Solicitud *:"),  0, 0); grid.add(cbSolicitud, 1, 0);
        grid.add(new Label("Remitente *:"),  0, 1); grid.add(cbRemitente, 1, 1);
        grid.add(new Label("Contenido *:"),  0, 2); grid.add(fContenido,  1, 2);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vs = new ValidationSupport();
        vs.registerValidator(cbSolicitud, false, (javafx.scene.control.Control c, SolicitudCita val) ->
            val == null ? ValidationResult.fromError(c, "La solicitud es obligatoria") : null);
        vs.registerValidator(cbRemitente, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El remitente es obligatorio") : null);
        vs.registerValidator(fContenido, false, (javafx.scene.control.Control c, String val) ->
            val == null || val.isBlank() ? ValidationResult.fromError(c, "El contenido es obligatorio") : null);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (vs.isInvalid()) {
                vs.getRegisteredControls().forEach(ctrl -> {
                    if (vs.getValidationResult().getErrors().stream().anyMatch(err -> err.getTarget() == ctrl))
                        AnimationUtils.shake(ctrl);
                });
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Mensaje m = new Mensaje();
            m.setSolicitud(cbSolicitud.getValue());
            m.setRemitente(cbRemitente.getValue());
            m.setContenido(fContenido.getText().trim());
            return m;
        });

        dialog.showAndWait().ifPresent(m -> {
            Task<Mensaje> task = new Task<>() {
                @Override protected Mensaje call() throws Exception { return service.crearMensaje(m); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al crear", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEliminar() {
        Mensaje m = tableView.getSelectionModel().getSelectedItem();
        if (m == null) { mostrarAviso("Selecciona un mensaje."); return; }

        if (!DialogUtils.confirmar("Confirmar eliminación", "¿Eliminar el mensaje de " + m.getNombreRemitente() + "?")) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return service.eliminarMensaje(m.getIdMensaje());
            }
        };
        task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo eliminar."); });
        task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private void mostrarError(String titulo, String msg) { DialogUtils.mostrarError(titulo, msg); }
    private void mostrarAviso(String msg)                { DialogUtils.mostrarAviso(msg); }
}
