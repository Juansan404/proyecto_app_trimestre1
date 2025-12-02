package com.javafx.controladores;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.javafx.dao.ClienteDAO;
import com.javafx.modelos.Cliente;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ventanaBClienteController implements Initializable {

    @FXML
    private ComboBox<String> comboCriterio;

    @FXML
    private TextField txtValor;

    private ClienteDAO clienteDAO;
    private miControlador controladorPrincipal;
    private ventanaAECitaController controladorCita;

    public void setControladorPrincipal(miControlador controlador) {
        this.controladorPrincipal = controlador;
    }

    public void setControladorCita(ventanaAECitaController controlador) {
        this.controladorCita = controlador;
    }

    @FXML
    void buttonAceptar(MouseEvent event) {
        String criterio = comboCriterio.getValue();
        String valor = txtValor.getText().trim();

        if (criterio == null || valor.isEmpty()) {
            mostrarAlerta("Advertencia", "Debe seleccionar un criterio e ingresar un valor de búsqueda", Alert.AlertType.WARNING);
            return;
        }

        List<Cliente> resultados = clienteDAO.buscarClientes(criterio, valor);

        if (resultados.isEmpty()) {
            mostrarAlerta("Sin resultados", "No se encontraron clientes con los criterios especificados", Alert.AlertType.INFORMATION);
        } else {
            // Si se llamó desde el controlador de citas, mostrar ventana de selección
            if (controladorCita != null) {
                mostrarVentanaSeleccion(resultados);
            } else if (controladorPrincipal != null) {
                // Si se llamó desde el controlador principal, actualizar tabla
                controladorPrincipal.actualizarTablaClientesConResultados(resultados);
                cerrarVentana();
            }
        }
    }

    @FXML
    void buttonCancelar(MouseEvent event) {
        cerrarVentana();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clienteDAO = new ClienteDAO();

        comboCriterio.setItems(FXCollections.observableArrayList(
            "Nombre", "Apellidos", "Email", "Telefono"
        ));
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) comboCriterio.getScene().getWindow();
        stage.close();
    }

    @SuppressWarnings("unchecked")
    private void mostrarVentanaSeleccion(List<Cliente> clientes) {
        Stage ventanaSeleccion = new Stage();
        ventanaSeleccion.setTitle("Seleccionar Cliente");

        // Crear tabla
        TableView<Cliente> tabla = new TableView<>();
        tabla.setItems(FXCollections.observableArrayList(clientes));

        TableColumn<Cliente, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id_cliente"));
        colId.setPrefWidth(50);

        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(100);

        TableColumn<Cliente, String> colApellidos = new TableColumn<>("Apellidos");
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colApellidos.setPrefWidth(120);

        TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(100);

        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(150);

        TableColumn<Cliente, java.sql.Date> colFechaNac = new TableColumn<>("Fecha Nacimiento");
        colFechaNac.setCellValueFactory(new PropertyValueFactory<>("fecha_nac"));
        colFechaNac.setPrefWidth(120);

        TableColumn<Cliente, String> colNotas = new TableColumn<>("Notas");
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
        colNotas.setPrefWidth(150);

        tabla.getColumns().addAll(colId, colNombre, colApellidos, colTelefono, colEmail, colFechaNac, colNotas);

        // Crear botón seleccionar
        Button btnSeleccionar = new Button("Seleccionar");
        btnSeleccionar.setOnAction(e -> {
            Cliente seleccionado = tabla.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                controladorCita.setClienteSeleccionado(seleccionado);
                ventanaSeleccion.close();
                cerrarVentana();
            } else {
                mostrarAlerta("Advertencia", "Debe seleccionar un cliente", Alert.AlertType.WARNING);
            }
        });

        // Crear layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(tabla, btnSeleccionar);

        Scene scene = new Scene(layout, 900, 400);
        ventanaSeleccion.setScene(scene);
        ventanaSeleccion.show();
    }

}
