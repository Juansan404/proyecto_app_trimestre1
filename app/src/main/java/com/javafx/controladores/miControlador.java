package com.javafx.controladores;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.javafx.dao.CitaDAO;
import com.javafx.dao.ClienteDAO;
import com.javafx.dao.TatuadorDAO;
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

    // DAOs
    private ClienteDAO clienteDAO;
    private TatuadorDAO tatuadorDAO;
    private CitaDAO citaDAO;

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
        actualizarEstadoBotones();
    }

    @FXML
    void menuTatuadores(ActionEvent event) {
        contenedorTablaTatuadores.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = true;
        panelCitasActivo = false;
        actualizarEstadoBotones();
    }

    @FXML
    void menuCitas(ActionEvent event) {
        contenedorTablaCitas.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = true;
        actualizarEstadoBotones();
    }

    @FXML
    void menuExit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanaExit.fxml"));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
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
    private Button btnAnadir;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnBorrar;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnResetearFiltro;

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

        abrirVentana(rutaFXML, titulo, null);
    }

    @FXML
    void btnEditar(MouseEvent event) {
        String panelActivo = getPanelActivo();
        String rutaFXML = "";
        String titulo = "";
        Object objetoSeleccionado = null;

        if (panelActivo.equals("clientes")) {
            rutaFXML = "/ventanaAECliente.fxml";
            titulo = "Editar Cliente";
            objetoSeleccionado = tableViewClientes.getSelectionModel().getSelectedItem();
        } else if (panelActivo.equals("tatuadores")) {
            rutaFXML = "/ventanaAETatuador.fxml";
            titulo = "Editar Tatuador";
            objetoSeleccionado = tableViewTatuadores.getSelectionModel().getSelectedItem();
        } else if (panelActivo.equals("citas")) {
            rutaFXML = "/ventanaAECita.fxml";
            titulo = "Editar Cita";
            objetoSeleccionado = tableViewCitas.getSelectionModel().getSelectedItem();
        }

        abrirVentana(rutaFXML, titulo, objetoSeleccionado);
    }

    @FXML
    void btnBorrar(MouseEvent event) {
        String panelActivo = getPanelActivo();

        if (panelActivo.equals("clientes")) {
            Cliente clienteSeleccionado = tableViewClientes.getSelectionModel().getSelectedItem();
            if (clienteSeleccionado != null) {
                javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar eliminación");
                confirmacion.setHeaderText("¿Está seguro de eliminar este cliente?");
                confirmacion.setContentText("Esta acción no se puede deshacer.");

                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        if (clienteDAO.eliminarCliente(clienteSeleccionado.getId_cliente())) {
                            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                            alerta.setTitle("Éxito");
                            alerta.setHeaderText(null);
                            alerta.setContentText("Cliente eliminado correctamente");
                            alerta.showAndWait();
                            refrescarTablas();
                        } else {
                            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                            alerta.setTitle("Error");
                            alerta.setHeaderText(null);
                            alerta.setContentText("No se pudo eliminar el cliente");
                            alerta.showAndWait();
                        }
                    }
                });
            }
        } else if (panelActivo.equals("tatuadores")) {
            Tatuador tatuadorSeleccionado = tableViewTatuadores.getSelectionModel().getSelectedItem();
            if (tatuadorSeleccionado != null) {
                javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar eliminación");
                confirmacion.setHeaderText("¿Está seguro de eliminar este tatuador?");
                confirmacion.setContentText("Esta acción no se puede deshacer.");

                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        if (tatuadorDAO.eliminarTatuador(tatuadorSeleccionado.getId_artista())) {
                            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                            alerta.setTitle("Éxito");
                            alerta.setHeaderText(null);
                            alerta.setContentText("Tatuador eliminado correctamente");
                            alerta.showAndWait();
                            refrescarTablas();
                        } else {
                            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                            alerta.setTitle("Error");
                            alerta.setHeaderText(null);
                            alerta.setContentText("No se pudo eliminar el tatuador");
                            alerta.showAndWait();
                        }
                    }
                });
            }
        } else if (panelActivo.equals("citas")) {
            Cita citaSeleccionada = tableViewCitas.getSelectionModel().getSelectedItem();
            if (citaSeleccionada != null) {
                javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar eliminación");
                confirmacion.setHeaderText("¿Está seguro de eliminar esta cita?");
                confirmacion.setContentText("Esta acción no se puede deshacer.");

                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        if (citaDAO.eliminarCita(citaSeleccionada.getId_cita())) {
                            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                            alerta.setTitle("Éxito");
                            alerta.setHeaderText(null);
                            alerta.setContentText("Cita eliminada correctamente");
                            alerta.showAndWait();
                            refrescarTablas();
                        } else {
                            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                            alerta.setTitle("Error");
                            alerta.setHeaderText(null);
                            alerta.setContentText("No se pudo eliminar la cita");
                            alerta.showAndWait();
                        }
                    }
                });
            }
        }
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

        abrirVentanaBusqueda(rutaFXML, titulo);
    }

    @FXML
    void btnResetearFiltro(MouseEvent event) {
        refrescarTablas();
        btnResetearFiltro.setVisible(false);
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

    private void actualizarEstadoBotones() {
        boolean algunPanelActivo = panelClientesActivo || panelTatuadoresActivo || panelCitasActivo;

        // Mostrar u ocultar botones según si hay un panel activo
        btnAnadir.setVisible(algunPanelActivo);
        btnAnadir.setDisable(!algunPanelActivo);

        btnEditar.setVisible(algunPanelActivo);
        btnBorrar.setVisible(algunPanelActivo);

        btnBuscar.setVisible(algunPanelActivo);
        btnBuscar.setDisable(!algunPanelActivo);

        // Actualizar estado de los botones editar y borrar según la selección
        if (panelClientesActivo) {
            boolean tieneSeleccion = tableViewClientes.getSelectionModel().getSelectedItem() != null;
            btnEditar.setDisable(!tieneSeleccion);
            btnBorrar.setDisable(!tieneSeleccion);
        } else if (panelTatuadoresActivo) {
            boolean tieneSeleccion = tableViewTatuadores.getSelectionModel().getSelectedItem() != null;
            btnEditar.setDisable(!tieneSeleccion);
            btnBorrar.setDisable(!tieneSeleccion);
        } else if (panelCitasActivo) {
            boolean tieneSeleccion = tableViewCitas.getSelectionModel().getSelectedItem() != null;
            btnEditar.setDisable(!tieneSeleccion);
            btnBorrar.setDisable(!tieneSeleccion);
        } else {
            btnEditar.setDisable(true);
            btnBorrar.setDisable(true);
        }
    }


    private void abrirVentana(String rutaFXML, String titulo, Object objetoAEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
            Parent root = loader.load();

            if (objetoAEditar != null) {
                if (objetoAEditar instanceof Cita) {
                    ventanaAECitaController controller = loader.getController();
                    controller.setCitaAEditar((Cita) objetoAEditar);
                } else if (objetoAEditar instanceof Cliente) {
                    ventanaAEClienteController controller = loader.getController();
                    controller.setClienteAEditar((Cliente) objetoAEditar);
                } else if (objetoAEditar instanceof Tatuador) {
                    ventanaAETatuadorController controller = loader.getController();
                    controller.setTatuadorAEditar((Tatuador) objetoAEditar);
                }
            }

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refrescarTablas());
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir ventana " + titulo + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirVentanaBusqueda(String rutaFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
            Parent root = loader.load();

            if (rutaFXML.contains("Cita")) {
                ventanaBCitaController controller = loader.getController();
                controller.setControladorPrincipal(this);
            } else if (rutaFXML.contains("Cliente")) {
                ventanaBClienteController controller = loader.getController();
                controller.setControladorPrincipal(this);
            } else if (rutaFXML.contains("Tatuador")) {
                ventanaBTatuadorController controller = loader.getController();
                controller.setControladorPrincipal(this);
            }

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
        actualizarEstadoBotones();
    }

    @FXML
    void btnTatuadores(MouseEvent event) {
        contenedorTablaTatuadores.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = true;
        panelCitasActivo = false;
        actualizarEstadoBotones();
    }

    @FXML
    void btnCitas(MouseEvent event) {
        contenedorTablaCitas.toFront();
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = true;
        actualizarEstadoBotones();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Inicializar DAOs
        clienteDAO = new ClienteDAO();
        tatuadorDAO = new TatuadorDAO();
        citaDAO = new CitaDAO();

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

        // Agregar listeners a las tablas para habilitar/deshabilitar los botones de editar y borrar
        tableViewClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (panelClientesActivo) {
                boolean tieneSeleccion = newSelection != null;
                btnEditar.setDisable(!tieneSeleccion);
                btnBorrar.setDisable(!tieneSeleccion);
            }
        });

        tableViewTatuadores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (panelTatuadoresActivo) {
                boolean tieneSeleccion = newSelection != null;
                btnEditar.setDisable(!tieneSeleccion);
                btnBorrar.setDisable(!tieneSeleccion);
            }
        });

        tableViewCitas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (panelCitasActivo) {
                boolean tieneSeleccion = newSelection != null;
                btnEditar.setDisable(!tieneSeleccion);
                btnBorrar.setDisable(!tieneSeleccion);
            }
        });

        // Ocultar y deshabilitar botones al inicio (pantalla principal activa)
        actualizarEstadoBotones();

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

        TableColumn<Cliente, java.sql.Date> colFechaNac = new TableColumn<>("Fecha Nacimiento");
        colFechaNac.setCellValueFactory(new PropertyValueFactory<>("fecha_nac"));
        colFechaNac.setPrefWidth(120);

        TableColumn<Cliente, String> colNotas = new TableColumn<>("Notas");
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
        colNotas.setPrefWidth(150);

        tableViewClientes.getColumns().addAll(colId, colNombre, colApellidos, colTelefono, colEmail, colFechaNac, colNotas);
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

        TableColumn<Cita, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colCliente.setPrefWidth(120);

        TableColumn<Cita, String> colArtista = new TableColumn<>("Artista");
        colArtista.setCellValueFactory(new PropertyValueFactory<>("nombreArtista"));
        colArtista.setPrefWidth(120);

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

        TableColumn<Cita, String> colNotas = new TableColumn<>("Notas");
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
        colNotas.setPrefWidth(150);

        tableViewCitas.getColumns().addAll(colId, colCliente, colArtista, colFecha, colPrecio, colEstado, colSala, colNotas);
        tableViewCitas.setItems(listaCitas);
    }

    private void cargarClientes() {
        listaClientes.clear();
        listaClientes.addAll(clienteDAO.cargarClientes());
    }

    private void cargarTatuadores() {
        listaTatuadores.clear();
        listaTatuadores.addAll(tatuadorDAO.cargarTatuadores());
    }

    private void cargarCitas() {
        listaCitas.clear();
        listaCitas.addAll(citaDAO.cargarCitas());
    }

    public void refrescarTablas() {
        cargarClientes();
        cargarTatuadores();
        cargarCitas();
    }

    public void actualizarTablaCitasConResultados(List<Cita> resultados) {
        listaCitas.clear();
        listaCitas.addAll(resultados);
        btnResetearFiltro.setVisible(true);
    }

    public void actualizarTablaClientesConResultados(List<Cliente> resultados) {
        listaClientes.clear();
        listaClientes.addAll(resultados);
        btnResetearFiltro.setVisible(true);
    }

    public void actualizarTablaTatuadoresConResultados(List<Tatuador> resultados) {
        listaTatuadores.clear();
        listaTatuadores.addAll(resultados);
        btnResetearFiltro.setVisible(true);
    }
}
