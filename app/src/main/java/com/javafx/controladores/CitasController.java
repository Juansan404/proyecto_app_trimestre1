package com.javafx.controladores;

import com.javafx.model.Cita;
import com.javafx.model.Usuario;
import com.javafx.service.CitaService;
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

import java.util.*;
import java.util.stream.Collectors;

public class CitasController {

    // Panel izquierdo: relaciones únicas
    @FXML private TableView<ParticipantePar>           tableRelaciones;
    @FXML private TableColumn<ParticipantePar, String> colCliente;
    @FXML private TableColumn<ParticipantePar, String> colArtista;
    @FXML private TableColumn<ParticipantePar, Number> colNumCitas;

    // Panel derecho: citas del par seleccionado
    @FXML private TableView<Cita>            tableCitas;
    @FXML private TableColumn<Cita, Long>    colId;
    @FXML private TableColumn<Cita, String>  colEstado;
    @FXML private TableColumn<Cita, String>  colFecha;
    @FXML private TableColumn<Cita, String>  colHora;
    @FXML private TableColumn<Cita, Double>  colPrecio;
    @FXML private TableColumn<Cita, String>  colSolicitud;

    @FXML private Label             lblParTitulo;
    @FXML private TextField         txtBuscar;
    @FXML private Label             lblStatus;
    @FXML private ProgressIndicator loadingSpinner;

    private final CitaService    citaService    = new CitaService();
    private final UsuarioService usuarioService = new UsuarioService();

    private final ObservableList<ParticipantePar> todasRelaciones = FXCollections.observableArrayList();
    private final ObservableList<ParticipantePar> mostradas       = FXCollections.observableArrayList();
    private final ObservableList<Cita>            citasPar        = FXCollections.observableArrayList();
    private List<Cita>    todasCitas = List.of();
    private List<Usuario> usuarios   = List.of();

    private static class ParticipantePar {
        final String key;
        final String nombreCliente;
        final String nombreArtista;
        final List<Cita> citas;

        ParticipantePar(String key, String nombreCliente, String nombreArtista, List<Cita> citas) {
            this.key           = key;
            this.nombreCliente = nombreCliente;
            this.nombreArtista = nombreArtista;
            this.citas         = new ArrayList<>(citas);
        }
    }

    @FXML
    private void initialize() {
        configurarColumnas();
        configurarFiltro();
        cargarUsuarios();
        cargar();
    }

    private void configurarColumnas() {
        colCliente.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().nombreCliente));
        colArtista.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().nombreArtista));
        colNumCitas.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().citas.size()));

        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdCita()));
        colEstado.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado()));
        colFecha.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFechaCita()));
        colHora.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getHoraInicio()));
        colPrecio.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrecio()));
        colSolicitud.setCellValueFactory(data -> {
            Long idSol = data.getValue().getIdSolicitud();
            return new javafx.beans.property.SimpleStringProperty(idSol != null ? "#" + idSol : "—");
        });

        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f €", item));
            }
        });

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(switch (item) {
                    case "Confirmada"  -> "-fx-text-fill: #2ecc71; -fx-font-weight: bold;";
                    case "Completada"  -> "-fx-text-fill: #3498db; -fx-font-weight: bold;";
                    case "Cancelada"   -> "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
                    default            -> "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
                });
            }
        });

        tableRelaciones.setItems(mostradas);
        tableCitas.setItems(citasPar);

        tableRelaciones.getSelectionModel().selectedItemProperty().addListener((o, prev, par) -> {
            if (par != null) {
                lblParTitulo.setText(par.nombreCliente + " → " + par.nombreArtista);
                citasPar.setAll(par.citas);
            } else {
                lblParTitulo.setText("Selecciona una relación");
                citasPar.clear();
            }
        });

        tableCitas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Cita sel = tableCitas.getSelectionModel().getSelectedItem();
                if (sel != null) DetalleUtils.mostrar(sel);
            }
        });
    }

    private void configurarFiltro() {
        txtBuscar.textProperty().addListener((o, v, n) -> filtrar());
    }

    private void agruparYMostrar() {
        Map<String, List<Cita>> grupos = todasCitas.stream()
            .filter(c -> c.getCliente() != null && c.getArtista() != null)
            .collect(Collectors.groupingBy(c ->
                c.getCliente().getIdUsuario() + "_" + c.getArtista().getIdUsuario()));

        todasRelaciones.setAll(grupos.entrySet().stream()
            .map(e -> {
                Cita primera = e.getValue().get(0);
                return new ParticipantePar(e.getKey(),
                        primera.getNombreCliente(), primera.getNombreArtista(), e.getValue());
            })
            .sorted(Comparator.comparing(p -> p.nombreCliente))
            .toList());
        filtrar();
    }

    private void filtrar() {
        String q = txtBuscar.getText().trim().toLowerCase();
        mostradas.setAll(todasRelaciones.stream().filter(p ->
            q.isBlank()
                || p.nombreCliente.toLowerCase().contains(q)
                || p.nombreArtista.toLowerCase().contains(q)
        ).toList());
        lblStatus.setText(mostradas.size() + " relaciones · " + todasCitas.size() + " citas");
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        Task<List<Cita>> task = new Task<>() {
            @Override protected List<Cita> call() throws Exception { return citaService.listarCitas(); }
        };
        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            todasCitas = task.getValue();
            agruparYMostrar();
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
        Task<List<Usuario>> task = new Task<>() {
            @Override protected List<Usuario> call() throws Exception { return usuarioService.listarUsuarios(); }
        };
        task.setOnSucceeded(e -> usuarios = task.getValue());
        new Thread(task).start();
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnCrear() {
        mostrarFormulario(null).ifPresent(cita -> {
            Task<Cita> task = new Task<>() {
                @Override protected Cita call() throws Exception { return citaService.crearCita(cita); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al crear cita", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEditar() {
        Cita sel = tableCitas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una cita en el panel derecho."); return; }
        mostrarFormulario(sel).ifPresent(editada -> {
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception { return citaService.actualizarCita(editada.getIdCita(), editada); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al editar", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEliminar() {
        Cita sel = tableCitas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una cita en el panel derecho."); return; }
        if (!DialogUtils.confirmar("Confirmar eliminación", "¿Eliminar la cita #" + sel.getIdCita() + "?")) return;
        Task<Boolean> taskElim = new Task<>() {
            @Override protected Boolean call() throws Exception { return citaService.eliminarCita(sel.getIdCita()); }
        };
        taskElim.setOnSucceeded(e -> { if (taskElim.getValue()) cargar(); else mostrarError("Error", "No se pudo eliminar."); });
        taskElim.setOnFailed(e -> mostrarError("Error", taskElim.getException().getMessage()));
        new Thread(taskElim).start();
    }

    @FXML
    private void btnCambiarEstado() {
        Cita sel = tableCitas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una cita en el panel derecho."); return; }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(sel.getEstado(),
                "Pendiente", "Confirmada", "Cancelada", "Completada");
        dlg.setTitle("Cambiar Estado");
        dlg.setHeaderText("Selecciona el nuevo estado para la cita #" + sel.getIdCita());
        DialogUtils.estilizar(dlg);
        dlg.showAndWait().ifPresent(nuevoEstado -> {
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception { return citaService.cambiarEstado(sel.getIdCita(), nuevoEstado); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    private Optional<Cita> mostrarFormulario(Cita original) {
        Dialog<Cita> dialog = new Dialog<>();
        dialog.setTitle(original == null ? "Nueva Cita" : "Editar Cita");
        dialog.setHeaderText(original == null ? "Crear nueva cita" : "Editar cita #" + original.getIdCita());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<Usuario> cbCliente = new ComboBox<>(FXCollections.observableArrayList(usuarios));
        cbCliente.setPrefWidth(250);
        if (original != null) cbCliente.setValue(original.getCliente());

        ComboBox<Usuario> cbArtista = new ComboBox<>(FXCollections.observableArrayList(
                usuarios.stream().filter(u -> "ARTISTA".equals(u.getRol()) || "ADMIN".equals(u.getRol())).toList()));
        cbArtista.setPrefWidth(250);
        if (original != null) cbArtista.setValue(original.getArtista());

        DatePicker dpFecha = new DatePicker();
        if (original != null && original.getFechaCita() != null) {
            try { dpFecha.setValue(java.time.LocalDate.parse(original.getFechaCita())); } catch (Exception ignored) {}
        }

        TextField fHora      = new TextField(original != null ? s(original.getHoraInicio()) : "10:00");
        TextField fDuracion  = new TextField(original != null && original.getDuracionAproximada() != null
                ? original.getDuracionAproximada().toString() : "60");
        TextField fPrecio    = new TextField(original != null && original.getPrecio() != null
                ? String.valueOf(original.getPrecio()) : "");
        TextField fSala      = new TextField(original != null ? s(original.getSala()) : "");
        TextField fFotoDiseno = new TextField(original != null ? s(original.getFotoDiseno()) : "");
        TextArea  fNotas     = new TextArea(original != null ? s(original.getNotas()) : "");
        fNotas.setPrefRowCount(2);

        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList(
                "Pendiente", "Confirmada", "Cancelada", "Completada"));
        cbEstado.setValue(original != null && original.getEstado() != null ? original.getEstado() : "Pendiente");

        int row = 0;
        grid.add(new Label("Cliente:"),        0, row); grid.add(cbCliente,   1, row++);
        grid.add(new Label("Artista:"),        0, row); grid.add(cbArtista,   1, row++);
        grid.add(new Label("Fecha:"),          0, row); grid.add(dpFecha,     1, row++);
        grid.add(new Label("Hora inicio:"),    0, row); grid.add(fHora,       1, row++);
        grid.add(new Label("Duración (min):"), 0, row); grid.add(fDuracion,   1, row++);
        grid.add(new Label("Precio (€):"),     0, row); grid.add(fPrecio,     1, row++);
        grid.add(new Label("Estado:"),         0, row); grid.add(cbEstado,    1, row++);
        grid.add(new Label("Sala:"),           0, row); grid.add(fSala,       1, row++);
        grid.add(new Label("Foto diseño:"),    0, row); grid.add(fFotoDiseno, 1, row++);
        grid.add(new Label("Notas:"),          0, row); grid.add(fNotas,      1, row);

        dialog.getDialogPane().setContent(grid);

        ValidationSupport vs = new ValidationSupport();
        vs.registerValidator(cbCliente, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El cliente es obligatorio") : null);
        vs.registerValidator(cbArtista, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El artista es obligatorio") : null);
        vs.registerValidator(dpFecha, false, (javafx.scene.control.Control c, java.time.LocalDate val) ->
            val == null ? ValidationResult.fromError(c, "La fecha es obligatoria") : null);
        vs.registerValidator(fHora, false, (javafx.scene.control.Control c, String val) ->
            val != null && !val.isBlank() && !val.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
                ? ValidationResult.fromError(c, "Formato HH:mm (ej. 10:30)") : null);
        vs.registerValidator(fDuracion, false, (javafx.scene.control.Control c, String val) -> {
            if (val != null && !val.isBlank()) try {
                if (Integer.parseInt(val.trim()) < 0) return ValidationResult.fromError(c, "Debe ser positivo");
            } catch (NumberFormatException e) { return ValidationResult.fromError(c, "Número entero requerido"); }
            return null;
        });
        vs.registerValidator(fPrecio, false, (javafx.scene.control.Control c, String val) -> {
            if (val != null && !val.isBlank()) try {
                if (Double.parseDouble(val.trim()) < 0) return ValidationResult.fromError(c, "Debe ser positivo");
            } catch (NumberFormatException e) { return ValidationResult.fromError(c, "Número decimal requerido (ej. 50.00)"); }
            return null;
        });
        vs.registerValidator(fSala, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 50 ? ValidationResult.fromError(c, "Máximo 50 caracteres") : null);

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
            Cita c = original != null ? original : new Cita();
            c.setCliente(cbCliente.getValue());
            c.setArtista(cbArtista.getValue());
            if (dpFecha.getValue() != null) c.setFechaCita(dpFecha.getValue().toString());
            c.setHoraInicio(fHora.getText().trim());
            try { c.setDuracionAproximada(Integer.parseInt(fDuracion.getText().trim())); } catch (Exception ignored) {}
            try { c.setPrecio(Double.parseDouble(fPrecio.getText().trim())); } catch (Exception ignored) {}
            c.setEstado(cbEstado.getValue());
            c.setSala(fSala.getText().trim());
            String fd = fFotoDiseno.getText().trim();
            c.setFotoDiseno(fd.isBlank() ? null : fd);
            c.setNotas(fNotas.getText().trim());
            return c;
        });
        return dialog.showAndWait();
    }

    private void mostrarError(String t, String m) { DialogUtils.mostrarError(t, m); }
    private void mostrarAviso(String m)            { DialogUtils.mostrarAviso(m); }
    private String s(String v) { return v != null ? v : ""; }
}
