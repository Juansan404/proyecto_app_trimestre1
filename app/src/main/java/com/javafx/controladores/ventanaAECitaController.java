package com.javafx.controladores;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.javafx.dao.CitaDAO;
import com.javafx.dao.ClienteDAO;
import com.javafx.dao.TatuadorDAO;
import com.javafx.modelos.Cita;
import com.javafx.modelos.Cliente;
import com.javafx.modelos.Tatuador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ventanaAECitaController implements Initializable {

    @FXML
    private ChoiceBox<Cliente> choiceCliente;

    @FXML
    private ChoiceBox<Tatuador> choiceTatuador;

    @FXML
    private DatePicker dateFecha;

    @FXML
    private ChoiceBox<String> choiceSala;

    @FXML
    private TextField txtDuracion;

    @FXML
    private TextField txtPrecio;

    @FXML
    private ChoiceBox<String> choiceEstado;

    @FXML
    private TextArea txtNotas;

    private CitaDAO citaDAO;
    private ClienteDAO clienteDAO;
    private TatuadorDAO tatuadorDAO;
    private Cita citaAEditar;
    private boolean modoEdicion = false;

    public void setCitaAEditar(Cita cita) {
        this.citaAEditar = cita;
        this.modoEdicion = true;
        cargarDatosCita();
    }

    @FXML
    void buttonCancelar(MouseEvent event) {
        cerrarVentana();
    }

    @FXML
    void buttonGuardar(MouseEvent event) {
        try {
            Cliente clienteSeleccionado = choiceCliente.getValue();
            Tatuador tatuadorSeleccionado = choiceTatuador.getValue();
            LocalDate fechaLocal = dateFecha.getValue();
            int duracion = Integer.parseInt(txtDuracion.getText().trim());
            String precioTexto = txtPrecio.getText().trim().replace(',', '.');
            double precio = Double.parseDouble(precioTexto);
            String estadoStr = choiceEstado.getValue();
            String sala = choiceSala.getValue();
            String notas = txtNotas.getText().trim();

            Date fechaSQL = Date.valueOf(fechaLocal);
            Cita.EstadoCita estado = Cita.EstadoCita.fromString(estadoStr);

            boolean exito;
            if (modoEdicion) {
                citaAEditar.setId_cliente(clienteSeleccionado.getId_cliente());
                citaAEditar.setId_artista(tatuadorSeleccionado.getId_artista());
                citaAEditar.setFecha_cita(fechaSQL);
                citaAEditar.setDuracion_aproximada(duracion);
                citaAEditar.setPrecio(precio);
                citaAEditar.setEstado(estado);
                citaAEditar.setSala(sala);
                citaAEditar.setNotas(notas);
                citaAEditar.setFoto_diseno(null);

                exito = citaDAO.actualizarCita(citaAEditar);
            } else {
                Cita nuevaCita = new Cita(0, clienteSeleccionado.getId_cliente(), tatuadorSeleccionado.getId_artista(),
                    fechaSQL, duracion, precio, estado, sala, null, notas);
                exito = citaDAO.insertarCita(nuevaCita);
            }

            if (exito) {
                mostrarAlerta("Éxito", modoEdicion ? "Cita actualizada correctamente" : "Cita creada correctamente", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error", "No se pudo " + (modoEdicion ? "actualizar" : "crear") + " la cita", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        citaDAO = new CitaDAO();
        clienteDAO = new ClienteDAO();
        tatuadorDAO = new TatuadorDAO();

        cargarClientes();
        cargarTatuadores();
        configurarChoiceSala();
        configurarChoiceEstado();
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.cargarClientes();
        choiceCliente.setItems(FXCollections.observableArrayList(clientes));
        choiceCliente.setConverter(new javafx.util.StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente != null ? cliente.getNombre() + " " + cliente.getApellidos() : "";
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });
    }

    private void cargarTatuadores() {
        List<Tatuador> tatuadores = tatuadorDAO.cargarTatuadores();
        choiceTatuador.setItems(FXCollections.observableArrayList(tatuadores));
        choiceTatuador.setConverter(new javafx.util.StringConverter<Tatuador>() {
            @Override
            public String toString(Tatuador tatuador) {
                return tatuador != null ? tatuador.getNombre() + " " + tatuador.getApellidos() : "";
            }

            @Override
            public Tatuador fromString(String string) {
                return null;
            }
        });
    }

    private void configurarChoiceSala() {
        choiceSala.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
    }

    private void configurarChoiceEstado() {
        choiceEstado.setItems(FXCollections.observableArrayList(
            "Pendiente", "Confirmada", "Cancelada", "Completada"
        ));
    }

    private void cargarDatosCita() {
        if (citaAEditar != null) {
            for (Cliente c : choiceCliente.getItems()) {
                if (c.getId_cliente() == citaAEditar.getId_cliente()) {
                    choiceCliente.setValue(c);
                    break;
                }
            }

            for (Tatuador t : choiceTatuador.getItems()) {
                if (t.getId_artista() == citaAEditar.getId_artista()) {
                    choiceTatuador.setValue(t);
                    break;
                }
            }

            dateFecha.setValue(citaAEditar.getFecha_cita().toLocalDate());
            txtDuracion.setText(String.valueOf(citaAEditar.getDuracion_aproximada()));
            txtPrecio.setText(String.valueOf(citaAEditar.getPrecio()));
            choiceEstado.setValue(citaAEditar.getEstado().getValor());
            choiceSala.setValue(citaAEditar.getSala());
            txtNotas.setText(citaAEditar.getNotas());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) choiceCliente.getScene().getWindow();
        stage.close();
    }

}
