package com.javafx.controladores;

import com.javafx.model.Publicacion;
import com.javafx.util.NavigationContext;
import com.javafx.model.Usuario;
import com.javafx.service.PublicacionService;
import com.javafx.service.UsuarioService;
import com.javafx.utils.AnimationUtils;
import com.javafx.utils.DetalleUtils;
import com.javafx.utils.DialogUtils;
import com.javafx.utils.ImageUtils;
import javafx.event.ActionEvent;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PublicacionesController {

    @FXML private TableView<Publicacion>            tableView;
    @FXML private TableColumn<Publicacion, Long>    colId;
    @FXML private TableColumn<Publicacion, String>  colUsuario;
    @FXML private TableColumn<Publicacion, String>  colEstilo;
    @FXML private TableColumn<Publicacion, String>  colDescripcion;
    @FXML private TableColumn<Publicacion, Integer> colLikes;
    @FXML private TableColumn<Publicacion, String>  colFecha;
    @FXML private TextField   txtBuscar;
    @FXML private Label       lblStatus;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private Button      btnPrev;
    @FXML private Button      btnNext;
    @FXML private Label       lblPagina;

    private static final int PAGE_SIZE = 20;
    private int currentPage  = 0;
    private int totalPages   = 1;

    private final PublicacionService service        = new PublicacionService();
    private final UsuarioService     usuarioService = new UsuarioService();
    private final ObservableList<Publicacion> mostrados = FXCollections.observableArrayList();
    private List<Publicacion> todasEnPagina = List.of();
    private List<Usuario> usuarios = List.of();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdPublicacion()));
        colUsuario.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreUsuario()));
        colEstilo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstilosStr()));
        colDescripcion.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDescripcionCorta()));
        colLikes.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getLikesCount()));
        colFecha.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCreadoEn() != null && data.getValue().getCreadoEn().length() >= 10
                        ? data.getValue().getCreadoEn().substring(0, 10) : ""));

        tableView.setItems(mostrados);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Publicacion p = tableView.getSelectionModel().getSelectedItem();
                if (p != null) DetalleUtils.mostrar(p);
            }
        });

        txtBuscar.textProperty().addListener((o, v, n) -> {
            String q = n.trim().toLowerCase();
            if (q.isBlank()) {
                mostrados.setAll(todasEnPagina);
                lblStatus.setText(todasEnPagina.size() + " publicaciones  (pág. " + (currentPage + 1) + "/" + totalPages + ")");
            } else {
                List<Publicacion> filtradas = todasEnPagina.stream()
                    .filter(p -> safe(p.getNombreUsuario()).contains(q)
                              || safe(p.getEstilosStr()).contains(q)
                              || safe(p.getDescripcion()).contains(q)
                              || safe(p.getZonaCuerpo()).contains(q))
                    .toList();
                mostrados.setAll(filtradas);
                lblStatus.setText(filtradas.size() + " resultado(s) para \"" + n.trim() + "\"");
            }
        });

        cargarUsuarios();
        cargar();
    }

    private void cargarUsuarios() {
        Task<List<Usuario>> t = new Task<>() {
            @Override protected List<Usuario> call() throws Exception { return usuarioService.listarUsuarios(); }
        };
        t.setOnSucceeded(e -> usuarios = t.getValue());
        new Thread(t).start();
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        actualizarBotonesPaginacion();

        final int page = currentPage;
        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() throws Exception {
                return service.listarPaginado(page, PAGE_SIZE);
            }
        };
        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            Object[] resultado = task.getValue();
            @SuppressWarnings("unchecked")
            List<Publicacion> lista = (List<Publicacion>) resultado[0];
            totalPages = (int) resultado[1];
            todasEnPagina = lista;
            String q = txtBuscar.getText().trim().toLowerCase();
            if (q.isBlank()) {
                mostrados.setAll(lista);
                lblStatus.setText(lista.size() + " publicaciones  (pág. " + (currentPage + 1) + "/" + totalPages + ")");
            } else {
                List<Publicacion> filtradas = lista.stream()
                    .filter(p -> safe(p.getNombreUsuario()).contains(q)
                              || safe(p.getEstilosStr()).contains(q)
                              || safe(p.getDescripcion()).contains(q)
                              || safe(p.getZonaCuerpo()).contains(q))
                    .toList();
                mostrados.setAll(filtradas);
                lblStatus.setText(filtradas.size() + " resultado(s) para \"" + txtBuscar.getText().trim() + "\"");
            }
            actualizarBotonesPaginacion();
        });
        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            mostrarError("Error", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    private void actualizarBotonesPaginacion() {
        if (btnPrev != null) btnPrev.setDisable(currentPage == 0);
        if (btnNext != null) btnNext.setDisable(currentPage >= totalPages - 1);
        if (lblPagina != null) lblPagina.setText("Pág. " + (currentPage + 1) + " / " + Math.max(1, totalPages));
    }

    @FXML private void btnRecargar() { currentPage = 0; cargar(); }

    @FXML
    private void btnPrev() {
        if (currentPage > 0) { currentPage--; cargar(); }
    }

    @FXML
    private void btnNext() {
        if (currentPage < totalPages - 1) { currentPage++; cargar(); }
    }

    @FXML
    private void btnCrear() {
        mostrarFormulario(null).ifPresent(datos -> {
            Task<Publicacion> task = new Task<>() {
                @Override protected Publicacion call() throws Exception { return service.crearPublicacion(datos); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al crear", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEditar() {
        Publicacion sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una publicación."); return; }

        mostrarFormularioEdicion(sel).ifPresent(datos -> {
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception {
                    return service.actualizarPublicacion(sel.getIdPublicacion(), datos);
                }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e -> mostrarError("Error al editar", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    private Optional<Publicacion> mostrarFormulario(Publicacion original) {
        Dialog<Publicacion> dialog = new Dialog<>();
        dialog.setTitle("Nueva Publicación");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        ColumnConstraints cc0 = new ColumnConstraints(); cc0.setHgrow(Priority.NEVER);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setHgrow(Priority.NEVER); cc1.setMaxWidth(280);
        grid.getColumnConstraints().addAll(cc0, cc1);

        ComboBox<Usuario> cbUsuario = new ComboBox<>(FXCollections.observableArrayList(usuarios));
        cbUsuario.setPrefWidth(250);
        TextField fEstilo = new TextField();
        TextField fZona   = new TextField();
        TextArea  fDesc   = new TextArea(); fDesc.setPrefRowCount(3); fDesc.setPrefWidth(250);

        final String[] fotoDataUrl = {null};
        ImageView preview = new ImageView();
        preview.setFitWidth(80); preview.setFitHeight(80); preview.setPreserveRatio(true);
        preview.setStyle("-fx-effect: dropshadow(gaussian,rgba(0,0,0,.4),6,0,0,1);");
        Button btnFoto = new Button("Seleccionar imagen...");
        btnFoto.setOnAction(ev -> {
            Stage s = (Stage) grid.getScene().getWindow();
            String dataUrl = ImageUtils.seleccionarComoDataUrl(s);
            if (dataUrl != null) {
                fotoDataUrl[0] = dataUrl;
                String b64 = dataUrl.split(",", 2)[1];
                byte[] bytes = Base64.getDecoder().decode(b64);
                preview.setImage(new Image(new ByteArrayInputStream(bytes), 80, 80, true, true));
                btnFoto.setText("Cambiar imagen");
            }
        });
        HBox fotoBox = new HBox(10, preview, btnFoto);
        fotoBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Usuario *:"),   0, 0); grid.add(cbUsuario, 1, 0);
        grid.add(new Label("Foto *:"),      0, 1); grid.add(fotoBox,   1, 1);
        grid.add(new Label("Estilo:"),      0, 2); grid.add(fEstilo,   1, 2);
        grid.add(new Label("Zona cuerpo:"), 0, 3); grid.add(fZona,     1, 3);
        grid.add(new Label("Descripción:"), 0, 4); grid.add(fDesc,     1, 4);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vsCrear = new ValidationSupport();
        vsCrear.registerValidator(cbUsuario, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El usuario es obligatorio") : null);
        vsCrear.registerValidator(fEstilo, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 50 ? ValidationResult.fromError(c, "Máximo 50 caracteres") : null);
        vsCrear.registerValidator(fZona, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 50 ? ValidationResult.fromError(c, "Máximo 50 caracteres") : null);

        Button okCrear = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okCrear.addEventFilter(ActionEvent.ACTION, ev -> {
            if (fotoDataUrl[0] == null) {
                AnimationUtils.shake(btnFoto);
                DialogUtils.mostrarAviso("Selecciona una imagen antes de continuar.");
                ev.consume();
                return;
            }
            if (vsCrear.isInvalid()) {
                vsCrear.getRegisteredControls().forEach(ctrl -> {
                    if (vsCrear.getValidationResult().getErrors().stream().anyMatch(err -> err.getTarget() == ctrl))
                        AnimationUtils.shake(ctrl);
                });
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Publicacion p = new Publicacion();
            p.setUsuario(cbUsuario.getValue());
            p.setFotoUrl(fotoDataUrl[0]);
            p.setEstilo(fEstilo.getText().trim());
            p.setZonaCuerpo(fZona.getText().trim());
            p.setDescripcion(fDesc.getText().trim());
            return p;
        });
        return dialog.showAndWait();
    }

    private Optional<Map<String, Object>> mostrarFormularioEdicion(Publicacion p) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Editar Publicación #" + p.getIdPublicacion());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        ColumnConstraints cc0 = new ColumnConstraints(); cc0.setHgrow(Priority.NEVER);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setHgrow(Priority.NEVER); cc1.setMaxWidth(280);
        grid.getColumnConstraints().addAll(cc0, cc1);

        TextField fEstilo = new TextField(s(p.getEstilosStr()));
        TextField fZona   = new TextField(s(p.getZonaCuerpo()));
        TextArea  fDesc   = new TextArea(s(p.getDescripcion())); fDesc.setPrefRowCount(3); fDesc.setPrefWidth(250);

        final String[] fotoDataUrl = {p.getFotoUrl()};
        ImageView preview = new ImageView();
        preview.setFitWidth(80); preview.setFitHeight(80); preview.setPreserveRatio(true);
        preview.setStyle("-fx-effect: dropshadow(gaussian,rgba(0,0,0,.4),6,0,0,1);");
        if (fotoDataUrl[0] != null && fotoDataUrl[0].contains(",")) {
            try {
                byte[] bytes = Base64.getDecoder().decode(fotoDataUrl[0].split(",", 2)[1]);
                preview.setImage(new Image(new ByteArrayInputStream(bytes), 80, 80, true, true));
            } catch (Exception ignored) {}
        }
        Button btnFoto = new Button(fotoDataUrl[0] != null ? "Cambiar imagen" : "Seleccionar imagen...");
        btnFoto.setOnAction(ev -> {
            Stage st = (Stage) grid.getScene().getWindow();
            String dataUrl = ImageUtils.seleccionarComoDataUrl(st);
            if (dataUrl != null) {
                fotoDataUrl[0] = dataUrl;
                byte[] bytes = Base64.getDecoder().decode(dataUrl.split(",", 2)[1]);
                preview.setImage(new Image(new ByteArrayInputStream(bytes), 80, 80, true, true));
                btnFoto.setText("Cambiar imagen");
            }
        });
        HBox fotoBox = new HBox(10, preview, btnFoto);
        fotoBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Foto:"),        0, 0); grid.add(fotoBox,  1, 0);
        grid.add(new Label("Estilo:"),      0, 1); grid.add(fEstilo,  1, 1);
        grid.add(new Label("Zona cuerpo:"), 0, 2); grid.add(fZona,    1, 2);
        grid.add(new Label("Descripción:"), 0, 3); grid.add(fDesc,    1, 3);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vsEditar = new ValidationSupport();
        vsEditar.registerValidator(fEstilo, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 50 ? ValidationResult.fromError(c, "Máximo 50 caracteres") : null);
        vsEditar.registerValidator(fZona, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 50 ? ValidationResult.fromError(c, "Máximo 50 caracteres") : null);

        Button okEditar = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okEditar.addEventFilter(ActionEvent.ACTION, ev -> {
            if (vsEditar.isInvalid()) {
                vsEditar.getRegisteredControls().forEach(ctrl -> {
                    if (vsEditar.getValidationResult().getErrors().stream().anyMatch(err -> err.getTarget() == ctrl))
                        AnimationUtils.shake(ctrl);
                });
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Map<String, Object> datos = new LinkedHashMap<>();
            if (fotoDataUrl[0] != null)        datos.put("fotoUrl",     fotoDataUrl[0]);
            if (!fEstilo.getText().isBlank())   datos.put("estilo",      fEstilo.getText().trim());
            if (!fZona.getText().isBlank())     datos.put("zonaCuerpo",  fZona.getText().trim());
            if (!fDesc.getText().isBlank())     datos.put("descripcion", fDesc.getText().trim());
            return datos;
        });
        return dialog.showAndWait();
    }

    @FXML
    private void btnVerComentarios() {
        Publicacion sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una publicación."); return; }

        NavigationContext.setFiltroPublicacion(sel.getIdPublicacion());
        Button btnNav = (Button) tableView.getScene().lookup("#btnNavComentarios");
        if (btnNav != null) btnNav.fire();
    }

    @FXML
    private void btnEliminar() {
        Publicacion p = tableView.getSelectionModel().getSelectedItem();
        if (p == null) { mostrarAviso("Selecciona una publicación."); return; }

        if (!DialogUtils.confirmar("Confirmar eliminación", "¿Eliminar la publicación #" + p.getIdPublicacion() + "?")) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception { return service.eliminarPublicacion(p.getIdPublicacion()); }
        };
        task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo eliminar."); });
        task.setOnFailed(e    -> mostrarError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private void mostrarError(String t, String m) { DialogUtils.mostrarError(t, m); }
    private void mostrarAviso(String m)            { DialogUtils.mostrarAviso(m); }
    private String safe(String s) { return s != null ? s.toLowerCase() : ""; }
    private String s(String v)    { return v != null ? v : ""; }
}
