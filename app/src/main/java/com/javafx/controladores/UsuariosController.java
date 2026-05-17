package com.javafx.controladores;

import com.javafx.model.Usuario;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UsuariosController {

    @FXML private TableView<Usuario>        tableViewUsuarios;
    @FXML private TableColumn<Usuario, Long>    colId;
    @FXML private TableColumn<Usuario, String>  colNombre;
    @FXML private TableColumn<Usuario, String>  colApellidos;
    @FXML private TableColumn<Usuario, String>  colEmail;
    @FXML private TableColumn<Usuario, String>  colRol;
    @FXML private TableColumn<Usuario, Boolean> colActivo;
    @FXML private TableColumn<Usuario, Long>    colSeguidores;
    @FXML private TableColumn<Usuario, Long>    colSeguidos;
    @FXML private TextField   txtBuscar;
    @FXML private ComboBox<String> comboFiltroRol;
    @FXML private Label       lblStatus;
    @FXML private ProgressIndicator loadingSpinner;

    private final UsuarioService service = new UsuarioService();
    private final ObservableList<Usuario> todos    = FXCollections.observableArrayList();
    private final ObservableList<Usuario> mostrados = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarColumnas();
        configurarFiltros();
        cargar();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colSeguidores.setCellValueFactory(new PropertyValueFactory<>("seguidores"));
        colSeguidos.setCellValueFactory(new PropertyValueFactory<>("seguidos"));

        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                boolean activo = item;
                setText(activo ? "Activo" : "Inactivo");
                setStyle(activo
                        ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Coloreado por rol
        colRol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(switch (item) {
                    case "ADMIN"   -> "-fx-text-fill: #e94560; -fx-font-weight: bold;";
                    case "ARTISTA" -> "-fx-text-fill: #9b59b6; -fx-font-weight: bold;";
                    default        -> "-fx-text-fill: #3498db;";
                });
            }
        });

        tableViewUsuarios.setItems(mostrados);
        tableViewUsuarios.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Usuario sel = tableViewUsuarios.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                Task<Void> task = new Task<>() {
                    @Override protected Void call() throws Exception {
                        sel.setNumeroPublicaciones(service.getNumPublicaciones(sel.getIdUsuario()));
                        service.cargarContadores(sel);
                        return null;
                    }
                };
                task.setOnSucceeded(ev -> DetalleUtils.mostrar(sel));
                task.setOnFailed(ev -> DetalleUtils.mostrar(sel));
                new Thread(task).start();
            }
        });
    }

    private void configurarFiltros() {
        comboFiltroRol.setItems(FXCollections.observableArrayList("Todos", "ADMIN", "ARTISTA", "CLIENTE"));
        comboFiltroRol.setValue("Todos");
        txtBuscar.textProperty().addListener((o, v, n) -> filtrar());
        comboFiltroRol.valueProperty().addListener((o, v, n) -> filtrar());
    }

    private void filtrar() {
        String busq = txtBuscar.getText().toLowerCase();
        String rol  = comboFiltroRol.getValue();

        List<Usuario> filtrados = todos.stream().filter(u -> {
            boolean matchBusq = busq.isEmpty()
                    || safe(u.getNombre()).contains(busq)
                    || safe(u.getApellidos()).contains(busq)
                    || safe(u.getEmail()).contains(busq);
            boolean matchRol = "Todos".equals(rol) || rol.equals(u.getRol());
            return matchBusq && matchRol;
        }).toList();

        mostrados.setAll(filtrados);
        lblStatus.setText(filtrados.size() + " usuarios");
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        Task<List<Usuario>> task = new Task<>() {
            @Override protected List<Usuario> call() throws Exception {
                return service.listarUsuarios();
            }
        };
        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            List<Usuario> lista = task.getValue().stream()
                    .sorted(java.util.Comparator.comparing(u -> u.getIdUsuario() != null ? u.getIdUsuario() : Long.MAX_VALUE))
                    .toList();
            todos.setAll(lista);
            filtrar();
            cargarContadores(lista);
        });
        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            mostrarError("Error al cargar usuarios", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    private void cargarContadores(List<Usuario> lista) {
        if (lista.isEmpty()) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                Map<Integer, long[]> bulk = service.getContadoresBulk();
                lista.forEach(u -> {
                    long[] c = bulk.get(u.getIdUsuario().intValue());
                    if (c != null) { u.setSeguidores(c[0]); u.setSeguidos(c[1]); }
                });
                return null;
            }
        };
        task.setOnSucceeded(e -> tableViewUsuarios.refresh());
        new Thread(task).start();
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnCrear() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Crear nuevo usuario");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(16));
        ColumnConstraints cc0 = new ColumnConstraints(); cc0.setHgrow(Priority.NEVER);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setHgrow(Priority.NEVER); cc1.setMaxWidth(280);
        grid.getColumnConstraints().addAll(cc0, cc1);

        TextField fNombre    = new TextField();
        TextField fApellidos = new TextField();
        TextField fEmail     = new TextField();
        PasswordField fPass  = new PasswordField();
        TextField fTelefono  = new TextField();
        ComboBox<String> comboRol = new ComboBox<>();
        comboRol.getItems().addAll("CLIENTE", "ARTISTA", "ADMIN");
        comboRol.setValue("CLIENTE");

        grid.add(new Label("Nombre *:"),    0, 0); grid.add(fNombre,    1, 0);
        grid.add(new Label("Apellidos:"),   0, 1); grid.add(fApellidos, 1, 1);
        grid.add(new Label("Email *:"),     0, 2); grid.add(fEmail,     1, 2);
        grid.add(new Label("Contraseña *:"),0, 3); grid.add(fPass,      1, 3);
        grid.add(new Label("Teléfono:"),    0, 4); grid.add(fTelefono,  1, 4);
        grid.add(new Label("Rol:"),         0, 5); grid.add(comboRol,   1, 5);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vsCrear = new ValidationSupport();
        vsCrear.registerValidator(fNombre, false, (javafx.scene.control.Control c, String val) -> {
            if (val == null || val.isBlank()) return ValidationResult.fromError(c, "El nombre es obligatorio");
            if (val.length() > 50)           return ValidationResult.fromError(c, "Máximo 50 caracteres");
            return null;
        });
        vsCrear.registerValidator(fApellidos, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 100 ? ValidationResult.fromError(c, "Máximo 100 caracteres") : null);
        vsCrear.registerValidator(fEmail, false, (javafx.scene.control.Control c, String val) -> {
            if (val == null || val.isBlank()) return ValidationResult.fromError(c, "El email es obligatorio");
            if (!val.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")) return ValidationResult.fromError(c, "Formato de email no válido");
            if (val.length() > 100)          return ValidationResult.fromError(c, "Máximo 100 caracteres");
            return null;
        });
        vsCrear.registerValidator(fPass, false, (javafx.scene.control.Control c, String val) -> {
            if (val == null || val.isBlank()) return ValidationResult.fromError(c, "La contraseña es obligatoria");
            if (val.length() < 6)            return ValidationResult.fromError(c, "Mínimo 6 caracteres");
            return null;
        });
        vsCrear.registerValidator(fTelefono, false, (javafx.scene.control.Control c, String val) ->
            val != null && !val.isBlank() && !val.matches("^[+]?[0-9 \\-]{6,15}$")
                ? ValidationResult.fromError(c, "Solo dígitos, espacios y guiones (6-15 chars)") : null);

        Button okCrear = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okCrear.addEventFilter(ActionEvent.ACTION, ev -> {
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
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception {
                    return service.crearUsuario(
                            fNombre.getText().trim(), fApellidos.getText().trim(),
                            fEmail.getText().trim(), fPass.getText(),
                            fTelefono.getText().trim(), comboRol.getValue());
                }
            };
            task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo crear. ¿Email ya existe?"); });
            task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
            new Thread(task).start();
            return null;
        });
        dialog.showAndWait();
    }

    @FXML
    private void btnEditar() {
        Usuario u = tableViewUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) { mostrarAviso("Selecciona un usuario."); return; }

        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.setHeaderText("Editar: " + u.getNombreCompleto());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(16));
        ColumnConstraints cc0 = new ColumnConstraints(); cc0.setHgrow(Priority.NEVER);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setHgrow(Priority.NEVER); cc1.setMaxWidth(280);
        grid.getColumnConstraints().addAll(cc0, cc1);

        TextField txtNombre    = new TextField(u.getNombre()    != null ? u.getNombre()    : "");
        TextField txtApellidos = new TextField(u.getApellidos() != null ? u.getApellidos() : "");
        TextField txtTelefono  = new TextField();
        TextArea  txtBio       = new TextArea(u.getBio() != null ? u.getBio() : "");
        txtBio.setPrefRowCount(3); txtBio.setPrefWidth(250); txtBio.setWrapText(true);
        ComboBox<String> comboRol = new ComboBox<>();
        comboRol.getItems().addAll("ADMIN", "ARTISTA", "CLIENTE");
        comboRol.setValue(u.getRol() != null ? u.getRol() : "CLIENTE");

        // Picker de avatar
        final String[] avatarDataUrl = {u.getAvatar()};
        ImageView avatarPreview = new ImageView();
        avatarPreview.setFitWidth(56); avatarPreview.setFitHeight(56); avatarPreview.setPreserveRatio(true);
        avatarPreview.setStyle("-fx-effect: dropshadow(gaussian,rgba(0,0,0,.4),6,0,0,1);");
        if (avatarDataUrl[0] != null && avatarDataUrl[0].contains(",")) {
            try {
                byte[] bytes = Base64.getDecoder().decode(avatarDataUrl[0].split(",", 2)[1]);
                avatarPreview.setImage(new Image(new ByteArrayInputStream(bytes), 56, 56, true, true));
            } catch (Exception ignored) {}
        }
        Button btnAvatar = new Button(avatarDataUrl[0] != null ? "Cambiar avatar" : "Seleccionar avatar...");
        btnAvatar.setOnAction(ev -> {
            Stage st = (Stage) grid.getScene().getWindow();
            String dataUrl = ImageUtils.seleccionarComoDataUrl(st);
            if (dataUrl != null) {
                avatarDataUrl[0] = dataUrl;
                byte[] bytes = Base64.getDecoder().decode(dataUrl.split(",", 2)[1]);
                avatarPreview.setImage(new Image(new ByteArrayInputStream(bytes), 56, 56, true, true));
                btnAvatar.setText("Cambiar avatar");
            }
        });
        HBox avatarBox = new HBox(10, avatarPreview, btnAvatar);
        avatarBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Nombre:"),    0, 0); grid.add(txtNombre,    1, 0);
        grid.add(new Label("Apellidos:"), 0, 1); grid.add(txtApellidos, 1, 1);
        grid.add(new Label("Teléfono:"),  0, 2); grid.add(txtTelefono,  1, 2);
        grid.add(new Label("Bio:"),       0, 3); grid.add(txtBio,       1, 3);
        grid.add(new Label("Avatar:"),    0, 4); grid.add(avatarBox,    1, 4);
        grid.add(new Label("Rol:"),       0, 5); grid.add(comboRol,     1, 5);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vsEditar = new ValidationSupport();
        vsEditar.registerValidator(txtNombre, false, (javafx.scene.control.Control c, String val) -> {
            if (val == null || val.isBlank()) return ValidationResult.fromError(c, "El nombre es obligatorio");
            if (val.length() > 50)           return ValidationResult.fromError(c, "Máximo 50 caracteres");
            return null;
        });
        vsEditar.registerValidator(txtApellidos, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 100 ? ValidationResult.fromError(c, "Máximo 100 caracteres") : null);
        vsEditar.registerValidator(txtTelefono, false, (javafx.scene.control.Control c, String val) ->
            val != null && !val.isBlank() && !val.matches("^[+]?[0-9 \\-]{6,15}$")
                ? ValidationResult.fromError(c, "Solo dígitos, espacios y guiones (6-15 chars)") : null);

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
            u.setNombre(txtNombre.getText().trim());
            u.setApellidos(txtApellidos.getText().trim());
            u.setBio(txtBio.getText().trim().isEmpty() ? null : txtBio.getText().trim());
            u.setAvatar(avatarDataUrl[0]);
            u.setRol(comboRol.getValue());
            return u;
        });

        Optional<Usuario> result = dialog.showAndWait();
        result.ifPresent(editado -> {
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception {
                    return service.actualizarUsuario(editado.getIdUsuario(), editado);
                }
            };
            task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo guardar."); });
            task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnDesactivar() {
        Usuario u = tableViewUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) { mostrarAviso("Selecciona un usuario."); return; }

        boolean nuevo = !Boolean.TRUE.equals(u.getActivo());
        u.setActivo(nuevo);

        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return service.actualizarUsuario(u.getIdUsuario(), u);
            }
        };
        task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else u.setActivo(!nuevo); });
        task.setOnFailed(e -> { u.setActivo(!nuevo); mostrarError("Error", task.getException().getMessage()); });
        new Thread(task).start();
    }

    @FXML
    private void btnEliminar() {
        Usuario u = tableViewUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) { mostrarAviso("Selecciona un usuario."); return; }

        if (!DialogUtils.confirmar("Confirmar eliminación", "¿Eliminar a " + u.getNombreCompleto() + "?")) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return service.eliminarUsuario(u.getIdUsuario());
            }
        };
        task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo eliminar."); });
        task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private void mostrarError(String titulo, String msg) { DialogUtils.mostrarError(titulo, msg); }
    private void mostrarAviso(String msg)                { DialogUtils.mostrarAviso(msg); }

    private String safe(String s) { return s != null ? s.toLowerCase() : ""; }
}
