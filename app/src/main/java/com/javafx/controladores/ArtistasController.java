package com.javafx.controladores;

import com.fasterxml.jackson.databind.JsonNode;
import com.javafx.model.PerfilArtista;
import com.javafx.model.Usuario;
import com.javafx.model.Valoracion;
import com.javafx.service.AdminService;
import com.javafx.service.ArtistaService;
import com.javafx.service.ValoracionService;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;
import com.javafx.utils.AnimationUtils;
import com.javafx.utils.DetalleUtils;
import com.javafx.utils.DialogUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import com.javafx.service.UsuarioService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArtistasController {

    @FXML private TableView<PerfilArtista>              tableView;
    @FXML private TableColumn<PerfilArtista, String>    colNombre;
    @FXML private TableColumn<PerfilArtista, String>    colEstudio;
    @FXML private TableColumn<PerfilArtista, String>    colEspecialidades;
    @FXML private TableColumn<PerfilArtista, Integer>   colAnios;
    @FXML private TableColumn<PerfilArtista, String>    colInstagram;
    @FXML private TableColumn<PerfilArtista, Double>    colPrecio;
    @FXML private TableColumn<PerfilArtista, Boolean>   colActivo;
    @FXML private TableColumn<PerfilArtista, Long>      colSeguidores;
    @FXML private TableColumn<PerfilArtista, String>    colValoracion;
    @FXML private TextField  txtBuscar;
    @FXML private Label      lblStatus;
    @FXML private ProgressIndicator loadingSpinner;

    private final AdminService       adminService     = new AdminService();
    private final ArtistaService     service          = new ArtistaService();
    private final UsuarioService     usuarioService   = new UsuarioService();
    private final ValoracionService  valoracionService = new ValoracionService();
    private final ObservableList<PerfilArtista> todos     = FXCollections.observableArrayList();
    private final ObservableList<PerfilArtista> mostrados  = FXCollections.observableArrayList();
    private List<Usuario> artistas = List.of();

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreArtista()));
        colEstudio.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreEstudio()));
        colEspecialidades.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEspecialidades() != null ? data.getValue().getEspecialidades() : ""));
        colAnios.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getAnosExperiencia()));
        colInstagram.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getInstagram() != null ? data.getValue().getInstagram() : ""));
        colPrecio.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrecioHora()));
        colActivo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDisponible()));
        colSeguidores.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSeguidores()));
        colValoracion.setCellValueFactory(data -> {
            PerfilArtista a = data.getValue();
            String txt = a.getTotalValoraciones() == 0
                    ? "—"
                    : String.format("%.1f ★ (%d)", a.getMediaValoracion(), a.getTotalValoraciones());
            return new javafx.beans.property.SimpleStringProperty(txt);
        });
        colValoracion.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("—") ? "-fx-text-fill: #7c80a8;" : "-fx-text-fill: #FFB800; -fx-font-weight: bold;");
            }
        });

        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item ? "Activo" : "Inactivo");
                setStyle(item
                        ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f €/h", item));
            }
        });

        tableView.setItems(mostrados);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                PerfilArtista sel = tableView.getSelectionModel().getSelectedItem();
                if (sel != null) DetalleUtils.mostrar(sel);
            }
        });
        txtBuscar.textProperty().addListener((o, v, n) -> filtrar());
        cargarArtistas();
        cargar();
    }

    private void cargarArtistas() {
        Task<List<Usuario>> t = new Task<>() {
            @Override protected List<Usuario> call() throws Exception {
                return usuarioService.listarUsuarios().stream()
                        .filter(u -> "ARTISTA".equals(u.getRol()))
                        .toList();
            }
        };
        t.setOnSucceeded(e -> artistas = t.getValue());
        new Thread(t).start();
    }

    private void filtrar() {
        String q = txtBuscar.getText().toLowerCase();
        mostrados.setAll(todos.stream().filter(a ->
                q.isEmpty()
                || a.getNombreArtista().toLowerCase().contains(q)
                || safe(a.getEspecialidades()).contains(q)
                || safe(a.getInstagram()).contains(q)
        ).toList());
        lblStatus.setText(mostrados.size() + " artistas");
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        Task<List<PerfilArtista>> task = new Task<>() {
            @Override protected List<PerfilArtista> call() throws Exception { return service.listarArtistas(); }
        };
        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            todos.setAll(task.getValue());
            filtrar();
            cargarDatosAsincronos(task.getValue());
        });
        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            mostrarError("Error al cargar artistas", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    private void cargarDatosAsincronos(List<PerfilArtista> lista) {
        for (PerfilArtista a : lista) {
            if (a.getUsuario() == null) continue;
            long idUsuario = a.getUsuario().getIdUsuario();
            new Thread(() -> {
                // Seguidores
                try {
                    var r = ApiClient.getInstance().get("/api/usuarios/" + idUsuario + "/contadores");
                    if (r.statusCode() == 200) {
                        @SuppressWarnings("unchecked")
                        Map<String, Number> contadores = JsonUtil.fromJson(r.body(), Map.class);
                        Number seg = contadores.get("seguidores");
                        if (seg != null) {
                            a.setSeguidores(seg.longValue());
                            Platform.runLater(() -> tableView.refresh());
                        }
                    }
                } catch (Exception ignored) {}
                // Valoraciones
                try {
                    Map<String, Object> resumen = valoracionService.getResumen(idUsuario);
                    Number media = (Number) resumen.get("media");
                    Number total = (Number) resumen.get("total");
                    if (media != null) a.setMediaValoracion(media.doubleValue());
                    if (total != null) a.setTotalValoraciones(total.longValue());
                    Platform.runLater(() -> tableView.refresh());
                } catch (Exception ignored) {}
            }).start();
        }
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnVerValoraciones() {
        PerfilArtista sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un artista."); return; }
        if (sel.getUsuario() == null) { mostrarAviso("No se puede obtener el ID del artista."); return; }

        long idArtista = sel.getUsuario().getIdUsuario();

        Task<List<Valoracion>> task = new Task<>() {
            @Override protected List<Valoracion> call() throws Exception {
                return valoracionService.getValoraciones(idArtista);
            }
        };
        task.setOnSucceeded(e -> mostrarDialogoValoraciones(sel, task.getValue()));
        task.setOnFailed(e -> mostrarError("Error", "No se pudieron cargar las valoraciones."));
        new Thread(task).start();
    }

    private void mostrarDialogoValoraciones(PerfilArtista artista, List<Valoracion> valoraciones) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Valoraciones — " + artista.getNombreArtista());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        DialogUtils.estilizar(dialog);
        dialog.getDialogPane().setPrefWidth(540);

        VBox container = new VBox(10);
        container.setPadding(new Insets(16));

        // Resumen
        String resumenTxt = ValoracionService.formatResumen(artista.getMediaValoracion(), artista.getTotalValoraciones());
        Label lblResumen = new Label(resumenTxt);
        lblResumen.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFB800;");

        Separator sep = new Separator();

        container.getChildren().addAll(lblResumen, sep);

        if (valoraciones.isEmpty()) {
            Label lblVacio = new Label("Este artista aún no tiene valoraciones.");
            lblVacio.setStyle("-fx-text-fill: #7c80a8; -fx-font-style: italic;");
            container.getChildren().add(lblVacio);
        } else {
            for (Valoracion v : valoraciones) {
                VBox card = new VBox(4);
                card.setPadding(new Insets(10, 12, 10, 12));
                card.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8;");

                HBox header = new HBox(10);
                Label lblNombre = new Label(v.getNombreCliente());
                lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #e0e0e0;");
                Label lblEstrellas = new Label(v.getEstrellas());
                lblEstrellas.setStyle("-fx-text-fill: #FFB800; -fx-font-size: 14px;");
                Label lblFecha = new Label(v.getFechaCorta());
                lblFecha.setStyle("-fx-text-fill: #6b7094; -fx-font-size: 11px;");
                HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
                header.getChildren().addAll(lblNombre, lblEstrellas, spacer, lblFecha);

                card.getChildren().add(header);
                if (v.getComentario() != null && !v.getComentario().isBlank()) {
                    Label lblComentario = new Label(v.getComentario());
                    lblComentario.setStyle("-fx-text-fill: #a0a0c0; -fx-font-size: 13px;");
                    lblComentario.setWrapText(true);
                    card.getChildren().add(lblComentario);
                }

                // Botón eliminar (admin)
                Button btnDel = new Button("Eliminar");
                btnDel.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 4;");
                final int idValoracion = v.getIdValoracion();
                btnDel.setOnAction(ev -> {
                    if (!DialogUtils.confirmar("Eliminar valoración", "¿Eliminar la valoración de " + v.getNombreCliente() + "?")) return;
                    Task<Boolean> delTask = new Task<>() {
                        @Override protected Boolean call() throws Exception {
                            return valoracionService.eliminar(idValoracion);
                        }
                    };
                    delTask.setOnSucceeded(ev2 -> {
                        if (Boolean.TRUE.equals(delTask.getValue())) {
                            container.getChildren().remove(card);
                            cargar();
                        } else {
                            mostrarError("Error", "No se pudo eliminar.");
                        }
                    });
                    delTask.setOnFailed(ev2 -> mostrarError("Error", delTask.getException().getMessage()));
                    new Thread(delTask).start();
                });
                card.getChildren().add(btnDel);

                container.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

    @FXML
    private void btnCrear() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Perfil Artista");
        dialog.setHeaderText("Crear nuevo perfil de artista");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));

        ComboBox<Usuario> cbArtista = new ComboBox<>(FXCollections.observableArrayList(artistas));
        cbArtista.setPrefWidth(250);
        TextField txtEsp    = new TextField();
        TextField txtAnios  = new TextField();
        TextField txtInsta  = new TextField();
        TextField txtPrecio = new TextField();
        TextField txtPort   = new TextField();
        CheckBox  chkDisp   = new CheckBox(); chkDisp.setSelected(true);

        grid.add(new Label("Artista (usuario) *:"), 0, 0); grid.add(cbArtista, 1, 0);
        grid.add(new Label("Especialidades:"),       0, 1); grid.add(txtEsp,    1, 1);
        grid.add(new Label("Años experiencia:"),     0, 2); grid.add(txtAnios,  1, 2);
        grid.add(new Label("Instagram:"),            0, 3); grid.add(txtInsta,  1, 3);
        grid.add(new Label("Precio/hora (€):"),      0, 4); grid.add(txtPrecio, 1, 4);
        grid.add(new Label("Portfolio URL:"),         0, 5); grid.add(txtPort,   1, 5);
        grid.add(new Label("Disponible:"),            0, 6); grid.add(chkDisp,   1, 6);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vsCrear = new ValidationSupport();
        vsCrear.registerValidator(cbArtista, false, (javafx.scene.control.Control c, Usuario val) ->
            val == null ? ValidationResult.fromError(c, "El artista es obligatorio") : null);
        vsCrear.registerValidator(txtAnios, false, (javafx.scene.control.Control c, String val) -> {
            if (val != null && !val.isBlank()) try {
                if (Integer.parseInt(val.trim()) < 0) return ValidationResult.fromError(c, "Debe ser positivo");
            } catch (NumberFormatException ex) { return ValidationResult.fromError(c, "Número entero requerido"); }
            return null;
        });
        vsCrear.registerValidator(txtPrecio, false, (javafx.scene.control.Control c, String val) -> {
            if (val != null && !val.isBlank()) try {
                if (Double.parseDouble(val.trim()) < 0) return ValidationResult.fromError(c, "Debe ser positivo");
            } catch (NumberFormatException ex) { return ValidationResult.fromError(c, "Número decimal requerido (ej. 45.00)"); }
            return null;
        });
        vsCrear.registerValidator(txtInsta, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 100 ? ValidationResult.fromError(c, "Máximo 100 caracteres") : null);

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
            PerfilArtista p = new PerfilArtista();
            p.setEspecialidades(txtEsp.getText().trim());
            try { p.setAnosExperiencia(Integer.parseInt(txtAnios.getText().trim())); } catch (Exception ignored) {}
            p.setInstagram(txtInsta.getText().trim());
            try { p.setPrecioHora(Double.parseDouble(txtPrecio.getText().trim())); } catch (Exception ignored) {}
            p.setPortfolioUrl(txtPort.getText().trim());
            p.setDisponible(chkDisp.isSelected());

            final long idUsuario = cbArtista.getValue().getIdUsuario();
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception {
                    return service.crearPerfil(idUsuario, p);
                }
            };
            task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo crear. ¿Ya existe perfil?"); });
            task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
            new Thread(task).start();
            return null;
        });
        dialog.showAndWait();
    }

    @FXML
    private void btnEditar() {
        PerfilArtista p = tableView.getSelectionModel().getSelectedItem();
        if (p == null) { mostrarAviso("Selecciona un artista."); return; }

        Long idUsuario = p.getUsuario() != null ? p.getUsuario().getIdUsuario() : null;
        if (idUsuario == null) { mostrarAviso("No se puede obtener el ID del artista."); return; }

        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Editar Perfil Artista");
        dialog.setHeaderText("Editar: " + p.getNombreArtista());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.estilizar(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField txtEspecialidades = new TextField(p.getEspecialidades() != null ? p.getEspecialidades() : "");
        TextField txtAnios          = new TextField(p.getAnosExperiencia() != null ? p.getAnosExperiencia().toString() : "");
        TextField txtInstagram      = new TextField(p.getInstagram() != null ? p.getInstagram() : "");
        TextField txtPrecio         = new TextField(p.getPrecioHora() != null ? p.getPrecioHora().toString() : "");
        TextField txtPortfolio      = new TextField(p.getPortfolioUrl() != null ? p.getPortfolioUrl() : "");
        CheckBox  chkDisponible     = new CheckBox();
        chkDisponible.setSelected(Boolean.TRUE.equals(p.getDisponible()));

        grid.add(new Label("Especialidades:"),  0, 0); grid.add(txtEspecialidades, 1, 0);
        grid.add(new Label("Años experiencia:"),0, 1); grid.add(txtAnios,          1, 1);
        grid.add(new Label("Instagram:"),       0, 2); grid.add(txtInstagram,      1, 2);
        grid.add(new Label("Precio/hora (€):"), 0, 3); grid.add(txtPrecio,         1, 3);
        grid.add(new Label("Portfolio URL:"),   0, 4); grid.add(txtPortfolio,      1, 4);
        grid.add(new Label("Disponible:"),      0, 5); grid.add(chkDisponible,     1, 5);
        dialog.getDialogPane().setContent(grid);

        ValidationSupport vsEditar = new ValidationSupport();
        vsEditar.registerValidator(txtAnios, false, (javafx.scene.control.Control c, String val) -> {
            if (val != null && !val.isBlank()) try {
                if (Integer.parseInt(val.trim()) < 0) return ValidationResult.fromError(c, "Debe ser positivo");
            } catch (NumberFormatException ex) { return ValidationResult.fromError(c, "Número entero requerido"); }
            return null;
        });
        vsEditar.registerValidator(txtPrecio, false, (javafx.scene.control.Control c, String val) -> {
            if (val != null && !val.isBlank()) try {
                if (Double.parseDouble(val.trim()) < 0) return ValidationResult.fromError(c, "Debe ser positivo");
            } catch (NumberFormatException ex) { return ValidationResult.fromError(c, "Número decimal requerido (ej. 45.00)"); }
            return null;
        });
        vsEditar.registerValidator(txtInstagram, false, (javafx.scene.control.Control c, String val) ->
            val != null && val.length() > 100 ? ValidationResult.fromError(c, "Máximo 100 caracteres") : null);

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
            if (!txtEspecialidades.getText().isBlank()) datos.put("especialidades", txtEspecialidades.getText().trim());
            if (!txtAnios.getText().isBlank())          { try { datos.put("anosExperiencia", Integer.parseInt(txtAnios.getText().trim())); } catch (NumberFormatException ignored) {} }
            if (!txtInstagram.getText().isBlank())      datos.put("instagram", txtInstagram.getText().trim());
            if (!txtPrecio.getText().isBlank())         { try { datos.put("precioHora", Double.parseDouble(txtPrecio.getText().trim())); } catch (NumberFormatException ignored) {} }
            if (!txtPortfolio.getText().isBlank())      datos.put("portfolioUrl", txtPortfolio.getText().trim());
            datos.put("disponible", chkDisponible.isSelected());
            return datos;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(datos -> {
            Task<Boolean> task = new Task<>() {
                @Override protected Boolean call() throws Exception {
                    return service.actualizarPerfil(idUsuario, datos);
                }
            };
            task.setOnSucceeded(e -> { if (task.getValue()) cargar(); else mostrarError("Error", "No se pudo guardar."); });
            task.setOnFailed(e -> mostrarError("Error", task.getException().getMessage()));
            new Thread(task).start();
        });
    }

    @FXML
    private void btnPendientes() {
        Task<List<com.javafx.model.Usuario>> task = new Task<>() {
            @Override protected List<com.javafx.model.Usuario> call() throws Exception {
                return adminService.getPendientes();
            }
        };
        task.setOnSucceeded(e -> mostrarDialogoPendientes(task.getValue()));
        task.setOnFailed(e -> mostrarError("Error", "No se pudieron cargar los artistas pendientes."));
        new Thread(task).start();
    }

    private void mostrarDialogoPendientes(List<com.javafx.model.Usuario> pendientes) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Artistas pendientes de aprobación");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        DialogUtils.estilizar(dialog);
        dialog.getDialogPane().setPrefWidth(560);

        VBox container = new VBox(10);
        container.setPadding(new Insets(16));

        if (pendientes.isEmpty()) {
            Label lbl = new Label("No hay artistas pendientes de aprobación.");
            lbl.setStyle("-fx-text-fill: #7c80a8; -fx-font-style: italic;");
            container.getChildren().add(lbl);
        } else {
            Label lblCount = new Label(pendientes.size() + " artista(s) esperando aprobación");
            lblCount.setStyle("-fx-font-size: 13px; -fx-text-fill: #FFB800; -fx-font-weight: bold;");
            container.getChildren().addAll(lblCount, new Separator());

            List<VBox> cards = new ArrayList<>();
            for (com.javafx.model.Usuario u : pendientes) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(12, 14, 12, 14));
                card.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8;");

                Label lblNombre = new Label(u.getNombreCompleto());
                lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e0e0e0;");

                Label lblEmail = new Label(u.getEmail());
                lblEmail.setStyle("-fx-text-fill: #6b7094; -fx-font-size: 12px;");

                HBox botones = new HBox(8);
                Button btnAprobar = new Button("✓  Aprobar");
                btnAprobar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 16;");
                Button btnRechazar = new Button("✕  Rechazar");
                btnRechazar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 16;");

                btnAprobar.setOnAction(ev -> {
                    btnAprobar.setDisable(true); btnRechazar.setDisable(true);
                    Task<Boolean> t = new Task<>() {
                        @Override protected Boolean call() throws Exception { return adminService.aprobar(u.getIdUsuario()); }
                    };
                    t.setOnSucceeded(ev2 -> {
                        if (Boolean.TRUE.equals(t.getValue())) {
                            container.getChildren().remove(card);
                            cards.remove(card);
                            cargar();
                        } else {
                            btnAprobar.setDisable(false); btnRechazar.setDisable(false);
                            mostrarError("Error", "No se pudo aprobar al artista.");
                        }
                    });
                    t.setOnFailed(ev2 -> { btnAprobar.setDisable(false); btnRechazar.setDisable(false); mostrarError("Error", t.getException().getMessage()); });
                    new Thread(t).start();
                });

                btnRechazar.setOnAction(ev -> {
                    if (!DialogUtils.confirmar("Rechazar artista", "¿Rechazar la solicitud de " + u.getNombreCompleto() + "?")) return;
                    btnAprobar.setDisable(true); btnRechazar.setDisable(true);
                    Task<Boolean> t = new Task<>() {
                        @Override protected Boolean call() throws Exception { return adminService.rechazar(u.getIdUsuario()); }
                    };
                    t.setOnSucceeded(ev2 -> {
                        if (Boolean.TRUE.equals(t.getValue())) {
                            container.getChildren().remove(card);
                            cards.remove(card);
                        } else {
                            btnAprobar.setDisable(false); btnRechazar.setDisable(false);
                            mostrarError("Error", "No se pudo rechazar al artista.");
                        }
                    });
                    t.setOnFailed(ev2 -> { btnAprobar.setDisable(false); btnRechazar.setDisable(false); mostrarError("Error", t.getException().getMessage()); });
                    new Thread(t).start();
                });

                botones.getChildren().addAll(btnAprobar, btnRechazar);
                card.getChildren().addAll(lblNombre, lblEmail, botones);
                cards.add(card);
                container.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

    private void mostrarAviso(String msg) { DialogUtils.mostrarAviso(msg); }
    private void mostrarError(String t, String m) { DialogUtils.mostrarError(t, m); }
    private String safe(String s) { return s != null ? s.toLowerCase() : ""; }
}
