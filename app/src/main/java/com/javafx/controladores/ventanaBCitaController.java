package com.javafx.controladores;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.javafx.dao.CitaDAO;
import com.javafx.modelos.Cita;
import com.javafx.utils.StageUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ventanaBCitaController implements Initializable{

    @FXML
    private ComboBox<String> comboCriterio;

    @FXML
    private TextField txtValor;

    @FXML
    private DatePicker dateFechaInicio;

    @FXML
    private DatePicker dateFechaFin;

    private CitaDAO citaDAO;
    private miControlador controladorPrincipal;

    public void setControladorPrincipal(miControlador controlador) {
        this.controladorPrincipal = controlador;
    }

    @FXML
    void buttonAceptar(MouseEvent event) {
        String criterio = comboCriterio.getValue();
        String valor = txtValor.getText().trim();
        LocalDate fechaInicio = dateFechaInicio.getValue();
        LocalDate fechaFin = dateFechaFin.getValue();

        Date fechaInicioSQL = fechaInicio != null ? Date.valueOf(fechaInicio) : null;
        Date fechaFinSQL = fechaFin != null ? Date.valueOf(fechaFin) : null;

        boolean tieneCriterio = criterio != null && !valor.isEmpty();
        boolean tieneFechas = fechaInicioSQL != null && fechaFinSQL != null;

        if (!tieneCriterio && !tieneFechas) {
            mostrarAlerta("Advertencia", "Debe ingresar al menos un criterio de búsqueda o un rango de fechas", Alert.AlertType.WARNING);
            return;
        }

        // Convertir criterio a minúsculas para que coincida con el DAO
        String criterioLowerCase = criterio != null ? criterio.toLowerCase() : null;

        List<Cita> resultados = citaDAO.buscarCitas(criterioLowerCase, valor, fechaInicioSQL, fechaFinSQL);

        if (resultados.isEmpty()) {
            mostrarAlerta("Sin resultados", "No se encontraron citas con los criterios especificados", Alert.AlertType.INFORMATION);
        } else {
            if (controladorPrincipal != null) {
                controladorPrincipal.actualizarTablaCitasConResultados(resultados);
            }
            cerrarVentana();
        }
    }

    @FXML
    void buttonCancelar(MouseEvent event) {
        cerrarVentana();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        citaDAO = new CitaDAO();

        comboCriterio.setItems(FXCollections.observableArrayList(
            "Cliente", "Artista", "Estado", "Sala"
        ));

        // Detectar Enter en el campo de texto
        txtValor.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                buttonAceptar(null);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        Stage alertStage = (Stage) alerta.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);
        alerta.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) comboCriterio.getScene().getWindow();
        stage.close();
    }

}
