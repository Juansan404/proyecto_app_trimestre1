package com.javafx.controladores;

import com.javafx.model.Comentario;
import com.javafx.model.Publicacion;
import com.javafx.model.Usuario;
import com.javafx.service.ComentarioService;
import com.javafx.service.PublicacionService;
import com.javafx.service.UsuarioService;
import com.javafx.util.NavigationContext;
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

public class ComentariosController {

    @FXML private TableView<Comentario>              tableView;
    @FXML private TableColumn<Comentario, Long>      colId;
    @FXML private TableColumn<Comentario, String>    colUsuario;
    @FXML private TableColumn<Comentario, Long>      colPublicacion;
    @FXML private TableColumn<Comentario, String>    colContenido;
    @FXML private TableColumn<Comentario, String>    colFecha;
    @FXML private TextField         txtBuscar;
    @FXML private Label             lblStatus;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private Button            btnLimpiarFiltro;

    private Long filtroPublicacion = null;

    private final ComentarioService  service            = new ComentarioService();
    private final PublicacionService publicacionService = new PublicacionService();
    private final UsuarioService     usuarioService     = new UsuarioService();
    private final ObservableList<Comentario> todos     = FXCollections.observableArrayList();
    private final ObservableList<Comentario> mostrados = FXCollections.observableArrayList();
    private List<Usuario>    usuarios    = List.of();
    private List<Publicacion> publicaciones = List.of();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdComentario()));
        colUsuario.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreUsuario()));
        colPublicacion.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdPublicacion()));
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
                Comentario sel = tableView.getSelectionModel().getSelectedItem();
                if (sel != null) DetalleUtils.mostrar(sel);
            }
        });
        txtBuscar.textProperty().addListener((o, v, n) -> filtrar());
        cargarAuxiliares();

        Long pendiente = NavigationContext.consumeFiltroPublicacion();
        if (pendiente != null) {
            filtroPublicacion = pendiente;
            btnLimpiarFiltro.setVisible(true);
            btnLimpiarFiltro.setManaged(true);
        }
        cargar();
    }

    private void cargarAuxiliares() {
        Task<List<Usuario>> tu = new Task<>() {
            @Override protected List<Usuario> call() throws Exception { return usuarioService.listarUsuarios(); }
        };
        tu.setOnSucceeded(e -> usuarios = tu.getValue());
        new Thread(tu).start();

        Task<List<Publicacion>> tp = new Task<>() {
            @Override protected List<Publicacion> call() throws Exception { return publicacionService.listarPublicaciones(); }
        };
        tp.setOnSucceeded(e -> publicaciones = tp.getValue());
        new Thread(tp).start();
    }

    private void filtrar() {
        String q = txtBuscar.getText().toLowerCase();
        mostrados.setAll(todos.stream().filter(c ->
                q.isEmpty()
                || c.getNombreUsuario().toLowerCase().contains(q)
                || (c.getContenido() != null && c.getContenido().toLowerCase().contains(q))
        ).toList());
        lblStatus.setText(mostrados.size() + " comentarios");
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        final Long filtro = filtroPublicacion;
        Task<List<Comentario>> task = new Task<>() {
            @Override protected List<Comentario> call() throws Exception {
                return filtro != null
                        ? service.listarPorPublicacion(filtro)
                        : service.listarComentarios();
            }
        };
        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            todos.setAll(task.getValue());
            if (filtro != null) {
                lblStatus.setText(todos.size() + " comentario(s) — publicación #" + filtro);
                mostrados.setAll(todos);
            } else {
                filtrar();
            }
        });
        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            mostrarError("Error al cargar comentarios", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnLimpiarFiltro() {
        filtroPublicacion = null;
        btnLimpiarFiltro.setVisible(false);
        btnLimpiarFiltro.setManaged(false);
        txtBuscar.clear();
        cargar();
    }

    @FXML
    private void btnCrear() {
        Dialog<Comentario> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Comentario");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<Usuario>    cbUsuario    = new ComboBox<>(FXCollections.observableArrayList(usuarios));
        ComboBox<Publicacion> cbPublicacion = new ComboBox<>(FXCollections.observableArrayList(publicaciones));
        cbUsuario.setPrefWidth(250);
        cbPublicacion.setPrefWidth(250);
        TextArea fContenido = new TextArea(); fContenido.setPrefRowCount(3); fContenido.setPrefWidth(250);

        grid.add(new Label("Usuario *:"),      0, 0); grid.add(cbUsuario,     1, 0);
        grid.add(new Label("Publicación *:"),  0, 1); grid.add(cbPublicacion, 1, 1);
        grid.add(new Label("Contenido *:"),    0, 2); grid.add(fContenido,    1, 2);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vs = new ValidationSupport();
        vs.registerValidator(cbUsuario, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El usuario es obligatorio") : null);
        vs.registerValidator(cbPublicacion, false, (javafx.scene.control.Control c, Publicacion val) ->
            val == null ? ValidationResult.fromError(c, "La publicación es obligatoria") : null);
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
            Comentario c = new Comentario();
            c.setUsuario(cbUsuario.getValue());
            c.setPublicacion(cbPublicacion.getValue());
            c.setContenido(fContenido.getText().trim());
            return c;
        });

        dialog.showAndWait().ifPresent(c -> {
            Task<Comentario> task = new Task<>() {
                @Override protected Comentario call() throws Exception { return service.crearComentario(c); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al crear", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEliminar() {
        Comentario c = tableView.getSelectionModel().getSelectedItem();
        if (c == null) { mostrarAviso("Selecciona un comentario."); return; }

        if (!DialogUtils.confirmar("Confirmar eliminación", "¿Eliminar este comentario de " + c.getNombreUsuario() + "?")) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return service.eliminarComentario(c.getIdComentario());
            }
        };
        task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo eliminar."); });
        task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private void mostrarError(String titulo, String msg) { DialogUtils.mostrarError(titulo, msg); }
    private void mostrarAviso(String msg)                { DialogUtils.mostrarAviso(msg); }
}
