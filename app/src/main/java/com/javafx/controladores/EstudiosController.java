package com.javafx.controladores;

import com.javafx.model.Estudio;
import com.javafx.service.EstudioService;
import com.javafx.utils.AnimationUtils;
import com.javafx.utils.DetalleUtils;
import com.javafx.utils.DialogUtils;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class EstudiosController {

    @FXML private TableView<Estudio>         tableView;
    @FXML private TableColumn<Estudio, Long>   colId;
    @FXML private TableColumn<Estudio, String> colNombre;
    @FXML private TableColumn<Estudio, String> colCiudad;
    @FXML private TableColumn<Estudio, String> colTelefono;
    @FXML private TableColumn<Estudio, String> colEmail;
    @FXML private TableColumn<Estudio, String> colWeb;
    @FXML private TableColumn<Estudio, String> colLocalizacion;
    @FXML private TextField txtBuscar;
    @FXML private Label     lblStatus;
    @FXML private ProgressIndicator loadingSpinner;

    private final EstudioService service = new EstudioService();
    private final ObservableList<Estudio> todos     = FXCollections.observableArrayList();
    private final ObservableList<Estudio> mostrados  = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEstudio"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colWeb.setCellValueFactory(new PropertyValueFactory<>("web"));
        colLocalizacion.setCellValueFactory(new PropertyValueFactory<>("localizacion"));
        colWeb.setCellValueFactory(new PropertyValueFactory<>("web"));

        // Celda de localización: texto azul subrayado clicable → Google Maps
        colLocalizacion.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) { setText(null); setGraphic(null); setStyle(""); return; }
                Label lbl = new Label(item);
                lbl.setTextFill(Color.web("#5b9ef4"));
                lbl.setUnderline(true);
                lbl.setFont(Font.font(12));
                lbl.setOnMouseClicked(ev -> abrirEnMaps(item));
                lbl.setStyle("-fx-cursor: hand;");
                setGraphic(lbl);
                setText(null);
            }
        });

        // Celda de web: texto azul subrayado clicable → abre en navegador
        colWeb.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) { setText(null); setGraphic(null); setStyle(""); return; }
                Label lbl = new Label(item);
                lbl.setTextFill(Color.web("#5b9ef4"));
                lbl.setUnderline(true);
                lbl.setFont(Font.font(12));
                lbl.setOnMouseClicked(ev -> abrirUrl(item));
                lbl.setStyle("-fx-cursor: hand;");
                setGraphic(lbl);
                setText(null);
            }
        });

        tableView.setItems(mostrados);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Estudio sel = tableView.getSelectionModel().getSelectedItem();
                if (sel != null) DetalleUtils.mostrar(sel);
            }
        });
        txtBuscar.textProperty().addListener((o, v, n) -> filtrar());
        cargar();
    }

    @FXML
    private void btnAbrirMaps() {
        Estudio sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un estudio."); return; }
        if (sel.getLocalizacion() == null || sel.getLocalizacion().isBlank()) {
            mostrarAviso("Este estudio no tiene localización registrada."); return;
        }
        abrirEnMaps(sel.getLocalizacion());
    }

    private void abrirEnMaps(String localizacion) {
        try {
            String q = URLEncoder.encode(localizacion, StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(new URI("https://maps.google.com/?q=" + q));
        } catch (Exception ex) {
            mostrarError("Error", "No se pudo abrir Google Maps: " + ex.getMessage());
        }
    }

    private void abrirUrl(String url) {
        try {
            String href = url.startsWith("http://") || url.startsWith("https://") ? url : "https://" + url;
            Desktop.getDesktop().browse(new URI(href));
        } catch (Exception ex) {
            mostrarError("Error", "No se pudo abrir la URL: " + ex.getMessage());
        }
    }

    private void filtrar() {
        String q = txtBuscar.getText().toLowerCase();
        mostrados.setAll(todos.stream().filter(e ->
                q.isEmpty()
                || safe(e.getNombre()).contains(q)
                || safe(e.getCiudad()).contains(q)
                || safe(e.getEmail()).contains(q)
                || safe(e.getWeb()).contains(q)
        ).toList());
        lblStatus.setText(mostrados.size() + " estudios");
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        Task<List<Estudio>> task = new Task<>() {
            @Override protected List<Estudio> call() throws Exception { return service.listarEstudios(); }
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
            mostrarError("Error al cargar", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnCrear() {
        mostrarFormulario(null).ifPresent(estudio -> {
            Task<Estudio> task = new Task<>() {
                @Override protected Estudio call() throws Exception { return service.crearEstudio(estudio); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e    -> mostrarError("Error al crear", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEditar() {
        Estudio sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un estudio."); return; }

        mostrarFormulario(sel).ifPresent(editado -> {
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception { return service.actualizarEstudio(editado.getIdEstudio(), editado); }
            };
            task.setOnSucceeded(e -> cargar());
            task.setOnFailed(e    -> mostrarError("Error al editar", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnEliminar() {
        Estudio sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un estudio."); return; }

        if (!DialogUtils.confirmar("Confirmar eliminación", "¿Eliminar el estudio \"" + sel.getNombre() + "\"?")) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception { return service.eliminarEstudio(sel.getIdEstudio()); }
        };
        task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo eliminar."); });
        task.setOnFailed(e    -> mostrarError("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    private Optional<Estudio> mostrarFormulario(Estudio original) {
        Dialog<Estudio> dialog = new Dialog<>();
        dialog.setTitle(original == null ? "Nuevo Estudio" : "Editar Estudio");
        dialog.setHeaderText(original == null ? "Crear nuevo estudio" : "Editar: " + original.getNombre());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        ColumnConstraints cc0 = new ColumnConstraints(); cc0.setHgrow(Priority.NEVER);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setHgrow(Priority.NEVER); cc1.setMaxWidth(280);
        grid.getColumnConstraints().addAll(cc0, cc1);

        TextField fNombre       = new TextField(original != null ? s(original.getNombre())       : "");
        TextField fDireccion    = new TextField(original != null ? s(original.getDireccion())    : "");
        TextField fCiudad       = new TextField(original != null ? s(original.getCiudad())       : "");
        TextField fTelefono     = new TextField(original != null ? s(original.getTelefono())     : "");
        TextField fEmail        = new TextField(original != null ? s(original.getEmail())        : "");
        TextField fWeb          = new TextField(original != null ? s(original.getWeb())          : "");
        TextField fInstagram    = new TextField(original != null ? s(original.getInstagram())    : "");
        TextField fLocalizacion = new TextField(original != null ? s(original.getLocalizacion()) : "");
        TextArea  fDesc         = new TextArea (original != null ? s(original.getDescripcion())  : "");
        fDesc.setPrefRowCount(3); fDesc.setPrefWidth(250);
        fLocalizacion.setPromptText("Ej: Calle Mayor 1, Madrid");

        grid.add(new Label("Nombre:"),         0, 0); grid.add(fNombre,       1, 0);
        grid.add(new Label("Dirección:"),      0, 1); grid.add(fDireccion,    1, 1);
        grid.add(new Label("Ciudad:"),         0, 2); grid.add(fCiudad,       1, 2);
        grid.add(new Label("Teléfono:"),       0, 3); grid.add(fTelefono,     1, 3);
        grid.add(new Label("Email:"),          0, 4); grid.add(fEmail,        1, 4);
        grid.add(new Label("Web:"),            0, 5); grid.add(fWeb,          1, 5);
        grid.add(new Label("Instagram:"),      0, 6); grid.add(fInstagram,    1, 6);
        grid.add(new Label("Localización:"),   0, 7); grid.add(fLocalizacion, 1, 7);
        grid.add(new Label("Descripción:"),    0, 8); grid.add(fDesc,         1, 8);

        dialog.getDialogPane().setContent(grid);

        ValidationSupport vs = new ValidationSupport();
        vs.registerValidator(fNombre, false, (javafx.scene.control.Control c, String val) -> {
            if (val == null || val.isBlank()) return ValidationResult.fromError(c, "El nombre es obligatorio");
            if (val.length() > 100)          return ValidationResult.fromError(c, "Máximo 100 caracteres");
            return null;
        });
        vs.registerValidator(fDireccion, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 200 ? ValidationResult.fromError(c, "Máximo 200 caracteres") : null);
        vs.registerValidator(fCiudad, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 100 ? ValidationResult.fromError(c, "Máximo 100 caracteres") : null);
        vs.registerValidator(fTelefono, false, (javafx.scene.control.Control c, String val) ->
            val != null && !val.isBlank() && !val.matches("^[+]?[0-9 \\-]{6,15}$")
                ? ValidationResult.fromError(c, "Solo dígitos, espacios y guiones (6-15 chars)") : null);
        vs.registerValidator(fEmail, false, (javafx.scene.control.Control c, String val) -> {
            if (val == null || val.isBlank()) return null;
            if (!val.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")) return ValidationResult.fromError(c, "Formato de email no válido");
            if (val.length() > 100) return ValidationResult.fromError(c, "Máximo 100 caracteres");
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
            Estudio e = original != null ? original : new Estudio();
            e.setNombre(fNombre.getText().trim());
            e.setDireccion(fDireccion.getText().trim());
            e.setCiudad(fCiudad.getText().trim());
            e.setTelefono(fTelefono.getText().trim());
            e.setEmail(fEmail.getText().trim());
            e.setWeb(fWeb.getText().trim());
            e.setInstagram(fInstagram.getText().trim());
            e.setLocalizacion(fLocalizacion.getText().trim());
            e.setDescripcion(fDesc.getText().trim());
            return e;
        });
        return dialog.showAndWait();
    }

    private void mostrarError(String t, String m) { DialogUtils.mostrarError(t, m); }
    private void mostrarAviso(String m)            { DialogUtils.mostrarAviso(m); }
    private String safe(String s) { return s != null ? s.toLowerCase() : ""; }
    private String s(String v)    { return v != null ? v : ""; }
}
