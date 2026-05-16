package com.javafx.controladores;

import com.javafx.model.Notificacion;
import com.javafx.model.Usuario;
import com.javafx.service.NotificacionService;
import com.javafx.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;

public class NotificacionesController {

    @FXML private ComboBox<Usuario>              cbUsuario;
    @FXML private Button                         btnCargar;
    @FXML private Button                         btnMarcarTodas;
    @FXML private Label                          lblContador;
    @FXML private TableView<Notificacion>        tblNotificaciones;
    @FXML private TableColumn<Notificacion, String>  colTipo;
    @FXML private TableColumn<Notificacion, String>  colDescripcion;
    @FXML private TableColumn<Notificacion, String>  colLeido;
    @FXML private TableColumn<Notificacion, String>  colFecha;
    @FXML private Label                          lblStatus;

    private final NotificacionService notifService  = new NotificacionService();
    private final UsuarioService      usuarioService = new UsuarioService();

    private final ObservableList<Notificacion> datos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Columnas
        colTipo.setCellValueFactory(cd -> {
            String tipo = cd.getValue().getTipo();
            return new javafx.beans.property.SimpleStringProperty(tipo != null ? tipo : "");
        });
        colDescripcion.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getDescripcion()));
        colLeido.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        Boolean.TRUE.equals(cd.getValue().getLeido()) ? "✓" : "•"));
        colFecha.setCellValueFactory(cd -> {
            String raw = cd.getValue().getCreadoEn();
            String fmt = (raw != null && raw.length() >= 10) ? raw.substring(0, 10) : "";
            return new javafx.beans.property.SimpleStringProperty(fmt);
        });

        tblNotificaciones.setItems(datos);

        // Estilo de fila: negrita si no leída
        tblNotificaciones.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Notificacion item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("");
                if (item != null && !empty && !Boolean.TRUE.equals(item.getLeido())) {
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        // Doble clic → marcar leída
        tblNotificaciones.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) marcarSeleccionadaLeida();
        });

        // ComboBox de usuarios
        cbUsuario.setConverter(new StringConverter<>() {
            @Override public String toString(Usuario u)   { return u != null ? u.getNombreCompleto() : ""; }
            @Override public Usuario fromString(String s) { return null; }
        });

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        Task<List<Usuario>> task = new Task<>() {
            @Override protected List<Usuario> call() throws Exception {
                return usuarioService.listarUsuarios();
            }
        };
        task.setOnSucceeded(e -> cbUsuario.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> lblStatus.setText("Error al cargar usuarios."));
        new Thread(task).start();
    }

    @FXML
    private void cargar() {
        Usuario u = cbUsuario.getValue();
        if (u == null) { lblStatus.setText("Selecciona un usuario primero."); return; }

        lblStatus.setText("Cargando...");
        datos.clear();

        Task<List<Notificacion>> task = new Task<>() {
            @Override protected List<Notificacion> call() throws Exception {
                return notifService.getByUsuario(u.getIdUsuario());
            }
        };
        task.setOnSucceeded(e -> {
            List<Notificacion> lista = task.getValue();
            datos.setAll(lista);
            long noLeidas = notifService.countNoLeidas(lista);
            lblContador.setText(noLeidas + " no leída" + (noLeidas != 1 ? "s" : ""));
            lblStatus.setText(lista.size() + " notificaciones cargadas.");
        });
        task.setOnFailed(e -> lblStatus.setText("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    private void marcarTodasLeidas() {
        Usuario u = cbUsuario.getValue();
        if (u == null) return;

        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return notifService.marcarTodasLeidas(u.getIdUsuario());
            }
        };
        task.setOnSucceeded(e -> {
            datos.forEach(n -> n.setLeido(true));
            tblNotificaciones.refresh();
            lblContador.setText("0 no leídas");
            lblStatus.setText("Todas marcadas como leídas.");
        });
        task.setOnFailed(e -> lblStatus.setText("Error al marcar: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void marcarSeleccionadaLeida() {
        Notificacion n = tblNotificaciones.getSelectionModel().getSelectedItem();
        if (n == null || Boolean.TRUE.equals(n.getLeido())) return;

        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return notifService.marcarLeida(n.getIdNotificacion());
            }
        };
        task.setOnSucceeded(e -> {
            n.setLeido(true);
            tblNotificaciones.refresh();
            long noLeidas = notifService.countNoLeidas(datos);
            lblContador.setText(noLeidas + " no leída" + (noLeidas != 1 ? "s" : ""));
        });
        task.setOnFailed(e -> lblStatus.setText("Error al marcar: " + task.getException().getMessage()));
        new Thread(task).start();
    }
}
