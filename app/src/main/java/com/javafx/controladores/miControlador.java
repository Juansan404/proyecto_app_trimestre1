package com.javafx.controladores;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import com.javafx.database.DatabaseConnection;
import com.javafx.modelos.Cita;
import com.javafx.modelos.Cliente;
import com.javafx.modelos.Tatuador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class miControlador implements Initializable{

    // Listas observables para las tablas
    private ObservableList<Cliente> listaClientes;
    private ObservableList<Tatuador> listaTatuadores;
    private ObservableList<Cita> listaCitas;

    // Variables para controlar qué panel está activo
    private boolean panelClientesActivo = false;
    private boolean panelTatuadoresActivo = false;
    private boolean panelCitasActivo = false;

    //PANES PRINCIPALES
    @FXML
    private StackPane stackPanePrincipal;

    @FXML
    private VBox contenedorPrincipal;

    @FXML
    private VBox contenedorTablaCitas;

    @FXML
    private VBox contenedorTablaClientes;

    @FXML
    private VBox contenedorTablaTatuadores;
    

    //OPCIONES MENU
    @FXML
    void menuClientes(ActionEvent event) {
        contenedorTablaClientes.toFront();
        panelClientesActivo = true;
        panelTatuadoresActivo = false;
        panelCitasActivo = false;
        actualizarEstadoBotonEditar();
    }

    @FXML
    void menuTatuadores(ActionEvent event) {
        contenedorTablaTatuadores.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = true;
        panelCitasActivo = false;
        actualizarEstadoBotonEditar();
    }

    @FXML
    void menuCitas(ActionEvent event) {
        contenedorTablaCitas.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = true;
        actualizarEstadoBotonEditar();
    }

    @FXML
    void menuExit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanaExit.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Salir");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir ventana de salida: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // TABLAS
    @FXML
    private TableView<Cita> tableViewCitas;

    @FXML
    private TableView<Cliente> tableViewClientes;

    @FXML
    private TableView<Tatuador> tableViewTatuadores;

    //BOTONES MANIPULACION TABLAS
    @FXML
    private Button btnEditar;

    @FXML
    void btnAnadir(MouseEvent event) {
        String panelActivo = getPanelActivo();
        String rutaFXML = "";
        String titulo = "";

        if (panelActivo.equals("clientes")) {
            rutaFXML = "/ventanaAECliente.fxml";
            titulo = "Añadir Cliente";
        } else if (panelActivo.equals("tatuadores")) {
            rutaFXML = "/ventanaAETatuador.fxml";
            titulo = "Añadir Tatuador";
        } else if (panelActivo.equals("citas")) {
            rutaFXML = "/ventanaAECita.fxml";
            titulo = "Añadir Cita";
        }

        abrirVentana(rutaFXML, titulo);
    }

    @FXML
    void btnEditar(MouseEvent event) {
        String panelActivo = getPanelActivo();
        String rutaFXML = "";
        String titulo = "";

        if (panelActivo.equals("clientes")) {
            rutaFXML = "/ventanaAECliente.fxml";
            titulo = "Editar Cliente";
        } else if (panelActivo.equals("tatuadores")) {
            rutaFXML = "/ventanaAETatuador.fxml";
            titulo = "Editar Tatuador";
        } else if (panelActivo.equals("citas")) {
            rutaFXML = "/ventanaAECita.fxml";
            titulo = "Editar Cita";
        }

        abrirVentana(rutaFXML, titulo);
    }

    @FXML
    void btnBorrar(MouseEvent event) {
        
    }

    @FXML
    void btnBuscar(MouseEvent event) {
        String panelActivo = getPanelActivo();
        String rutaFXML = "";
        String titulo = "";

        if (panelActivo.equals("clientes")) {
            rutaFXML = "/ventanaBCliente.fxml";
            titulo = "Buscar Cliente";
        } else if (panelActivo.equals("tatuadores")) {
            rutaFXML = "/ventanaBTatuador.fxml";
            titulo = "Buscar Tatuador";
        } else if (panelActivo.equals("citas")) {
            rutaFXML = "/ventanaBCita.fxml";
            titulo = "Buscar Cita";
        }

        abrirVentana(rutaFXML, titulo);
    }


    private String getPanelActivo() {
        if (panelClientesActivo) {
            return "clientes";
        } else if (panelTatuadoresActivo) {
            return "tatuadores";
        } else if (panelCitasActivo) {
            return "citas";
        }

        // Por defecto, retornamos citas
        return "citas";
    }

    private void actualizarEstadoBotonEditar() {
        if (panelClientesActivo) {
            btnEditar.setDisable(tableViewClientes.getSelectionModel().getSelectedItem() == null);
        } else if (panelTatuadoresActivo) {
            btnEditar.setDisable(tableViewTatuadores.getSelectionModel().getSelectedItem() == null);
        } else if (panelCitasActivo) {
            btnEditar.setDisable(tableViewCitas.getSelectionModel().getSelectedItem() == null);
        } else {
            btnEditar.setDisable(true);
        }
    }


    private void abrirVentana(String rutaFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir ventana " + titulo + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    //BOTONES STACKPANE PRINCIPAL
    @FXML
    void btnClientes(MouseEvent event) {
        contenedorTablaClientes.toFront();
        panelClientesActivo = true;
        panelTatuadoresActivo = false;
        panelCitasActivo = false;
        actualizarEstadoBotonEditar();
    }

    @FXML
    void btnTatuadores(MouseEvent event) {
        contenedorTablaTatuadores.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = true;
        panelCitasActivo = false;
        actualizarEstadoBotonEditar();
    }

    @FXML
    void btnCitas(MouseEvent event) {
        contenedorTablaCitas.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = true;
        actualizarEstadoBotonEditar();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Inicializar listas observables
        listaClientes = FXCollections.observableArrayList();
        listaTatuadores = FXCollections.observableArrayList();
        listaCitas = FXCollections.observableArrayList();

        // Configurar tablas
        configurarTablaClientes();
        configurarTablaTatuadores();
        configurarTablaCitas();

        // Cargar datos
        cargarClientes();
        cargarTatuadores();
        cargarCitas();

        // Deshabilitar el botón de editar por defecto
        btnEditar.setDisable(false);

        // Agregar listeners a las tablas para habilitar/deshabilitar el botón de editar
        tableViewClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (panelClientesActivo) {
                btnEditar.setDisable(newSelection == null);
            }
        });

        tableViewTatuadores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (panelTatuadoresActivo) {
                btnEditar.setDisable(newSelection == null);
            }
        });

        tableViewCitas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (panelCitasActivo) {
                btnEditar.setDisable(newSelection == null);
            }
        });

        // Mostrar pantalla principal
        contenedorPrincipal.toFront();
    }

    @SuppressWarnings("unchecked")
    private void configurarTablaClientes() {
        tableViewClientes.getColumns().clear();

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

        tableViewClientes.getColumns().addAll(colId, colNombre, colApellidos, colTelefono, colEmail);
        tableViewClientes.setItems(listaClientes);
    }

    @SuppressWarnings("unchecked")
    private void configurarTablaTatuadores() {
        tableViewTatuadores.getColumns().clear();

        TableColumn<Tatuador, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id_artista"));
        colId.setPrefWidth(50);

        TableColumn<Tatuador, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(100);

        TableColumn<Tatuador, String> colApellidos = new TableColumn<>("Apellidos");
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colApellidos.setPrefWidth(120);

        TableColumn<Tatuador, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(100);

        TableColumn<Tatuador, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(130);

        TableColumn<Tatuador, Boolean> colActivo = new TableColumn<>("Activo");
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setPrefWidth(60);

        tableViewTatuadores.getColumns().addAll(colId, colNombre, colApellidos, colTelefono, colEmail, colActivo);
        tableViewTatuadores.setItems(listaTatuadores);
    }

    @SuppressWarnings("unchecked")
    private void configurarTablaCitas() {
        tableViewCitas.getColumns().clear();

        TableColumn<Cita, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id_cita"));
        colId.setPrefWidth(40);

        TableColumn<Cita, Integer> colCliente = new TableColumn<>("ID Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("id_cliente"));
        colCliente.setPrefWidth(80);

        TableColumn<Cita, Integer> colArtista = new TableColumn<>("ID Artista");
        colArtista.setCellValueFactory(new PropertyValueFactory<>("id_artista"));
        colArtista.setPrefWidth(80);

        TableColumn<Cita, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha_cita"));
        colFecha.setPrefWidth(100);

        TableColumn<Cita, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(70);

        TableColumn<Cita, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(100);

        TableColumn<Cita, String> colSala = new TableColumn<>("Sala");
        colSala.setCellValueFactory(new PropertyValueFactory<>("sala"));
        colSala.setPrefWidth(60);

        tableViewCitas.getColumns().addAll(colId, colCliente, colArtista, colFecha, colPrecio, colEstado, colSala);
        tableViewCitas.setItems(listaCitas);
    }

    private void cargarClientes() {
        listaClientes.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM CLIENTES");

            while (resultado.next()) {
                Cliente cliente = new Cliente(
                    resultado.getInt("id_cliente"),
                    resultado.getString("nombre"),
                    resultado.getString("apellidos"),
                    resultado.getString("telefono"),
                    resultado.getString("email"),
                    resultado.getDate("fecha_nacimiento"),
                    resultado.getString("notas")
                );
                listaClientes.add(cliente);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarTatuadores() {
        listaTatuadores.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM TATUADORES");

            while (resultado.next()) {
                Tatuador tatuador = new Tatuador(
                    resultado.getInt("id_artista"),
                    resultado.getString("nombre"),
                    resultado.getString("apellidos"),
                    resultado.getString("telefono"),
                    resultado.getString("email"),
                    resultado.getBoolean("activo")
                );
                listaTatuadores.add(tatuador);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar tatuadores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarCitas() {
        listaCitas.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet resultado = st.executeQuery("SELECT * FROM CITAS");

            while (resultado.next()) {
                Cita cita = new Cita(
                    resultado.getInt("id_cita"),
                    resultado.getInt("id_cliente"),
                    resultado.getInt("id_artista"),
                    resultado.getDate("fecha_cita"),
                    resultado.getInt("duracion_aproximada"),
                    resultado.getDouble("precio"),
                    Cita.EstadoCita.fromString(resultado.getString("estado")),
                    resultado.getString("sala"),
                    resultado.getBytes("foto_diseno"),
                    resultado.getString("notas")
                );
                listaCitas.add(cita);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar citas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refrescarTablas() {
        cargarClientes();
        cargarTatuadores();
        cargarCitas();
    }
}
