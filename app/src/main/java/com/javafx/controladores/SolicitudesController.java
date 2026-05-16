package com.javafx.controladores;

import com.javafx.model.Mensaje;
import com.javafx.model.SolicitudCita;
import com.javafx.model.Usuario;
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
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;

public class SolicitudesController {

    @FXML private TableView<SolicitudCita>               tableView;
    @FXML private TableColumn<SolicitudCita, Long>       colId;
    @FXML private TableColumn<SolicitudCita, String>     colCliente;
    @FXML private TableColumn<SolicitudCita, String>     colArtista;
    @FXML private TableColumn<SolicitudCita, String>     colZona;
    @FXML private TableColumn<SolicitudCita, String>     colTamano;
    @FXML private TableColumn<SolicitudCita, String>     colEstado;
    @FXML private TableColumn<SolicitudCita, String>     colFecha;
    @FXML private ListView<Mensaje>  listViewMensajes;
    @FXML private Label              lblMensajesHeader;
    @FXML private Label              lblStatus;
    @FXML private ProgressIndicator  loadingSpinner;
    @FXML private ComboBox<String>   comboFiltroEstado;

    private final SolicitudService service        = new SolicitudService();
    private final UsuarioService   usuarioService = new UsuarioService();
    private final ObservableList<SolicitudCita> todos     = FXCollections.observableArrayList();
    private final ObservableList<SolicitudCita> mostrados = FXCollections.observableArrayList();
    private final ObservableList<Mensaje>       mensajes  = FXCollections.observableArrayList();
    private List<Usuario> usuarios = List.of();

    @FXML
    private void initialize() {
        configurarColumnas();
        configurarFiltro();
        configurarListaMensajes();
        cargarUsuarios();
        cargar();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, nueva) -> {
            if (nueva != null) cargarMensajes(nueva);
        });
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                SolicitudCita sel = tableView.getSelectionModel().getSelectedItem();
                if (sel != null) DetalleUtils.mostrar(sel);
            }
        });
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdSolicitud()));
        colCliente.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreCliente()));
        colArtista.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreArtista()));
        colZona.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getZonaCuerpo()));
        colTamano.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTamano()));
        colEstado.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado()));
        colFecha.setCellValueFactory(data -> {
            String f = data.getValue().getFechaPreferida();
            return new javafx.beans.property.SimpleStringProperty(f != null && !f.isBlank() ? f : "—");
        });

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(switch (item) {
                    case "Aceptada"   -> "-fx-text-fill: #2ecc71; -fx-font-weight: bold;";
                    case "Rechazada"  -> "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
                    case "Completada" -> "-fx-text-fill: #3498db; -fx-font-weight: bold;";
                    default           -> "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
                });
            }
        });

        tableView.setItems(mostrados);
    }

    private void configurarFiltro() {
        comboFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Pendiente", "Aceptada", "Rechazada", "Completada"));
        comboFiltroEstado.setValue("Todos");
        comboFiltroEstado.valueProperty().addListener((o, v, n) -> filtrar());
    }

    private void filtrar() {
        String estado = comboFiltroEstado.getValue();
        mostrados.setAll(todos.stream().filter(s ->
                "Todos".equals(estado) || estado.equals(s.getEstado())
        ).toList());
        lblStatus.setText(mostrados.size() + " solicitudes");
    }

    private void configurarListaMensajes() {
        listViewMensajes.setItems(mensajes);
        listViewMensajes.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Mensaje m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) { setText(null); setStyle(""); return; }
                setText(m.toString());
                setWrapText(true);
                setStyle("");
            }
        });
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        Task<List<SolicitudCita>> task = new Task<>() {
            @Override protected List<SolicitudCita> call() throws Exception { return service.listarSolicitudes(); }
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
            mostrarError("Error", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    private void cargarUsuarios() {
        Task<List<Usuario>> t = new Task<>() {
            @Override protected List<Usuario> call() throws Exception { return usuarioService.listarUsuarios(); }
        };
        t.setOnSucceeded(e -> usuarios = t.getValue());
        new Thread(t).start();
    }

    private void cargarMensajes(SolicitudCita sol) {
        lblMensajesHeader.setText("Mensajes — Solicitud #" + sol.getIdSolicitud());
        mensajes.clear();
        Task<List<Mensaje>> task = new Task<>() {
            @Override protected List<Mensaje> call() throws Exception { return service.obtenerMensajes(sol.getIdSolicitud()); }
        };
        task.setOnSucceeded(e -> mensajes.setAll(task.getValue()));
        task.setOnFailed(e    -> mensajes.setAll());
        new Thread(task).start();
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnCrear() {
        mostrarFormulario(null).ifPresent(s -> {
            Task<SolicitudCita> task = new Task<>() {
                @Override protected SolicitudCita call() throws Exception { return service.crearSolicitud(s); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al crear", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEditar() {
        SolicitudCita sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una solicitud."); return; }
        mostrarFormulario(sel).ifPresent(editada -> {
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception { return service.actualizarSolicitud(editada.getIdSolicitud(), editada); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al editar", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEliminar() {
        SolicitudCita sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una solicitud."); return; }

        if (!DialogUtils.confirmar("Confirmar eliminación", "¿Eliminar la solicitud #" + sel.getIdSolicitud() + "?")) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception { return service.eliminarSolicitud(sel.getIdSolicitud()); }
        };
        task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo eliminar."); });
        task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML private void btnAceptar()  { cambiarEstado("Aceptada");  }
    @FXML private void btnRechazar() { cambiarEstado("Rechazada"); }

    @FXML
    private void btnCambiarEstado() {
        SolicitudCita sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una solicitud."); return; }

        ChoiceDialog<String> dlg = new ChoiceDialog<>(sel.getEstado(),
                "Pendiente", "Aceptada", "Rechazada", "Completada");
        dlg.setTitle("Cambiar Estado");
        dlg.setHeaderText("Nuevo estado para la solicitud #" + sel.getIdSolicitud());
        DialogUtils.estilizar(dlg);
        dlg.showAndWait().ifPresent(nuevoEstado -> cambiarEstado(nuevoEstado));
    }

    private void cambiarEstado(String nuevoEstado) {
        SolicitudCita sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una solicitud."); return; }
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception { return service.cambiarEstado(sel.getIdSolicitud(), nuevoEstado); }
        };
        task.setOnSucceeded(e -> cargar());
        task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private Optional<SolicitudCita> mostrarFormulario(SolicitudCita original) {
        Dialog<SolicitudCita> dialog = new Dialog<>();
        dialog.setTitle(original == null ? "Nueva Solicitud" : "Editar Solicitud #" + original.getIdSolicitud());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<Usuario> cbCliente = new ComboBox<>(FXCollections.observableArrayList(usuarios));
        cbCliente.setPrefWidth(250);
        ComboBox<Usuario> cbArtista = new ComboBox<>(FXCollections.observableArrayList(
                usuarios.stream().filter(u -> "ARTISTA".equals(u.getRol()) || "ADMIN".equals(u.getRol())).toList()));
        cbArtista.setPrefWidth(250);

        if (original != null) {
            cbCliente.setValue(original.getCliente());
            cbArtista.setValue(original.getArtista());
        }

        TextArea  fDesc        = new TextArea(s(original != null ? original.getDescripcion() : ""));
        fDesc.setPrefRowCount(2); fDesc.setPrefWidth(250);
        TextField fZona        = new TextField(s(original != null ? original.getZonaCuerpo() : ""));
        TextField fTamano      = new TextField(s(original != null ? original.getTamano() : ""));
        TextField fPresupuesto = new TextField(original != null && original.getPresupuestoAprox() != null
                ? original.getPresupuestoAprox().toString() : "");
        DatePicker dpFecha = new DatePicker();
        if (original != null && original.getFechaPreferida() != null) {
            try { dpFecha.setValue(java.time.LocalDate.parse(original.getFechaPreferida())); } catch (Exception ignored) {}
        }
        TextField fFotoRef = new TextField(s(original != null ? original.getFotoReferencia() : ""));
        TextField fNotas   = new TextField(s(original != null ? original.getNotasArtista() : ""));
        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList(
                "Pendiente", "Aceptada", "Rechazada", "Completada"));
        cbEstado.setValue(original != null && original.getEstado() != null ? original.getEstado() : "Pendiente");

        int row = 0;
        grid.add(new Label("Cliente *:"),       0, row); grid.add(cbCliente,    1, row++);
        grid.add(new Label("Artista *:"),        0, row); grid.add(cbArtista,    1, row++);
        grid.add(new Label("Descripción *:"),    0, row); grid.add(fDesc,        1, row++);
        grid.add(new Label("Zona cuerpo:"),      0, row); grid.add(fZona,        1, row++);
        grid.add(new Label("Tamaño:"),           0, row); grid.add(fTamano,      1, row++);
        grid.add(new Label("Presupuesto (€):"),  0, row); grid.add(fPresupuesto, 1, row++);
        grid.add(new Label("Fecha preferida:"),  0, row); grid.add(dpFecha,      1, row++);
        grid.add(new Label("Foto referencia:"),  0, row); grid.add(fFotoRef,     1, row++);
        grid.add(new Label("Notas artista:"),    0, row); grid.add(fNotas,       1, row++);
        grid.add(new Label("Estado:"),           0, row); grid.add(cbEstado,     1, row);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vs = new ValidationSupport();
        vs.registerValidator(cbCliente, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El cliente es obligatorio") : null);
        vs.registerValidator(cbArtista, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El artista es obligatorio") : null);
        vs.registerValidator(fDesc, false, (javafx.scene.control.Control c, String val) ->
            val == null || val.isBlank() ? ValidationResult.fromError(c, "La descripción es obligatoria") : null);
        vs.registerValidator(fZona, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 50 ? ValidationResult.fromError(c, "Máximo 50 caracteres") : null);
        vs.registerValidator(fTamano, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 20 ? ValidationResult.fromError(c, "Máximo 20 caracteres") : null);
        vs.registerValidator(fPresupuesto, false, (javafx.scene.control.Control c, String val) -> {
            if (val != null && !val.isBlank()) try {
                if (Double.parseDouble(val.trim()) < 0) return ValidationResult.fromError(c, "Debe ser positivo");
            } catch (NumberFormatException e) { return ValidationResult.fromError(c, "Número decimal requerido (ej. 200.00)"); }
            return null;
        });

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
            SolicitudCita s = original != null ? original : new SolicitudCita();
            s.setCliente(cbCliente.getValue());
            s.setArtista(cbArtista.getValue());
            s.setDescripcion(fDesc.getText().trim());
            s.setZonaCuerpo(fZona.getText().trim());
            s.setTamano(fTamano.getText().trim());
            try { s.setPresupuestoAprox(Double.parseDouble(fPresupuesto.getText().trim())); } catch (Exception ignored) {}
            if (dpFecha.getValue() != null) s.setFechaPreferida(dpFecha.getValue().toString());
            s.setFotoReferencia(fFotoRef.getText().trim());
            s.setNotasArtista(fNotas.getText().trim());
            s.setEstado(cbEstado.getValue());
            return s;
        });
        return dialog.showAndWait();
    }

    private void mostrarError(String t, String m) { DialogUtils.mostrarError(t, m); }
    private void mostrarAviso(String m)            { DialogUtils.mostrarAviso(m); }
    private String s(String v) { return v != null ? v : ""; }
}
