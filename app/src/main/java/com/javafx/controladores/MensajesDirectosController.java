package com.javafx.controladores;

import com.javafx.model.Conversacion;
import com.javafx.model.MensajeDirecto;
import com.javafx.service.ConversacionService;
import com.javafx.utils.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class MensajesDirectosController {

    @FXML private TableView<Conversacion>       tableConversaciones;
    @FXML private TableColumn<Conversacion, Long>   colConvId;
    @FXML private TableColumn<Conversacion, String> colConvParticipantes;
    @FXML private TableColumn<Conversacion, String> colConvFecha;

    @FXML private TableView<MensajeDirecto>       tableMensajes;
    @FXML private TableColumn<MensajeDirecto, Long>    colMsgId;
    @FXML private TableColumn<MensajeDirecto, String>  colMsgRemitente;
    @FXML private TableColumn<MensajeDirecto, String>  colMsgContenido;
    @FXML private TableColumn<MensajeDirecto, String>  colMsgLeido;
    @FXML private TableColumn<MensajeDirecto, String>  colMsgFecha;

    @FXML private TextField       txtBuscar;
    @FXML private Label           lblStatus;
    @FXML private Label           lblConvTitulo;
    @FXML private ProgressIndicator loadingSpinner;

    private final ConversacionService service = new ConversacionService();
    private final ObservableList<Conversacion>  todas     = FXCollections.observableArrayList();
    private final ObservableList<Conversacion>  mostradas = FXCollections.observableArrayList();
    private final ObservableList<MensajeDirecto> mensajes  = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colConvId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getIdConversacion()));
        colConvParticipantes.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNombresParticipantes()));
        colConvFecha.setCellValueFactory(d -> {
            String f = d.getValue().getCreadoEn();
            return new javafx.beans.property.SimpleStringProperty(
                    f != null && f.length() >= 16 ? f.substring(0, 16).replace("T", " ") : "");
        });

        colMsgId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getIdMensaje()));
        colMsgRemitente.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNombreRemitente()));
        colMsgContenido.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getContenido()));
        colMsgLeido.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        Boolean.TRUE.equals(d.getValue().getLeido()) ? "Sí" : "No"));
        colMsgFecha.setCellValueFactory(d -> {
            String f = d.getValue().getCreadoEn();
            return new javafx.beans.property.SimpleStringProperty(
                    f != null && f.length() >= 16 ? f.substring(0, 16).replace("T", " ") : "");
        });

        tableConversaciones.setItems(mostradas);
        tableMensajes.setItems(mensajes);

        tableConversaciones.getSelectionModel().selectedItemProperty().addListener((obs, old, conv) -> {
            if (conv != null) cargarMensajes(conv);
        });

        txtBuscar.textProperty().addListener((o, v, n) -> filtrar());
        cargar();
    }

    private void filtrar() {
        String q = txtBuscar.getText().toLowerCase();
        mostradas.setAll(todas.stream().filter(c ->
                q.isEmpty() || c.getNombresParticipantes().toLowerCase().contains(q)
        ).toList());
        lblStatus.setText(mostradas.size() + " conversaciones");
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        Task<List<Conversacion>> task = new Task<>() {
            @Override protected List<Conversacion> call() throws Exception {
                return service.listarTodas();
            }
        };
        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            todas.setAll(task.getValue());
            filtrar();
        });
        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            DialogUtils.mostrarError("Error al cargar conversaciones", task.getException().getMessage());
            lblStatus.setText("Error");
        });
        new Thread(task).start();
    }

    private void cargarMensajes(Conversacion conv) {
        lblConvTitulo.setText(conv.getNombresParticipantes());
        mensajes.clear();
        Task<List<MensajeDirecto>> task = new Task<>() {
            @Override protected List<MensajeDirecto> call() throws Exception {
                return service.getMensajes(conv.getIdConversacion());
            }
        };
        task.setOnSucceeded(e -> mensajes.setAll(task.getValue()));
        task.setOnFailed(e ->
                DialogUtils.mostrarError("Error", "No se pudieron cargar los mensajes."));
        new Thread(task).start();
    }

    @FXML private void btnRecargar() { cargar(); }

    @FXML
    private void btnEliminar() {
        Conversacion c = tableConversaciones.getSelectionModel().getSelectedItem();
        if (c == null) { DialogUtils.mostrarAviso("Selecciona una conversación."); return; }
        DialogUtils.mostrarAviso("La eliminación de conversaciones no está soportada por el servidor.");
    }
}
