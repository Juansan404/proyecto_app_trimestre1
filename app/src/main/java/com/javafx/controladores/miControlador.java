package com.javafx.controladores;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


import com.javafx.dao.CitaDAO;
import com.javafx.dao.ClienteDAO;
import com.javafx.dao.TatuadorDAO;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.javafx.utils.ImageUtils;
import com.javafx.utils.StageUtils;
import com.javafx.utils.CSSUtils;
import com.javafx.utils.AnimationUtils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;



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
    private javafx.scene.layout.BorderPane borderPanePrincipal;

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

    @FXML
    private VBox contenedorInformes;

    @FXML
    private javafx.scene.control.CheckBox checkIncrustado;

    @FXML
    private ComboBox<String> comboEstadoInforme;

    @FXML
    private WebView wv;

    // Variables para controlar el panel de informes
    private boolean panelInformesActivo = false;

    // Parámetros de informe
    @SuppressWarnings("rawtypes")
    Map parametros = new HashMap();
    

    //OPCIONES MENU
    @FXML
    void menuClientes(ActionEvent event) {
        contenedorPrincipal.setVisible(false);
        contenedorTablaTatuadores.setVisible(false);
        contenedorTablaCitas.setVisible(false);
        contenedorInformes.setVisible(false);
        contenedorTablaClientes.setVisible(true);
        contenedorTablaClientes.toFront();
        AnimationUtils.fadeIn(contenedorTablaClientes);
        panelClientesActivo = true;
        panelTatuadoresActivo = false;
        panelCitasActivo = false;
        panelInformesActivo = false;
        actualizarEstadoBotones();
    }

    @FXML
    void menuTatuadores(ActionEvent event) {
        contenedorPrincipal.setVisible(false);
        contenedorTablaClientes.setVisible(false);
        contenedorTablaCitas.setVisible(false);
        contenedorInformes.setVisible(false);
        contenedorTablaTatuadores.setVisible(true);
        contenedorTablaTatuadores.toFront();
        AnimationUtils.fadeIn(contenedorTablaTatuadores);
        panelClientesActivo = false;
        panelTatuadoresActivo = true;
        panelCitasActivo = false;
        panelInformesActivo = false;
        actualizarEstadoBotones();
    }

    @FXML
    void menuCitas(ActionEvent event) {
        contenedorPrincipal.setVisible(false);
        contenedorTablaClientes.setVisible(false);
        contenedorTablaTatuadores.setVisible(false);
        contenedorInformes.setVisible(false);
        contenedorTablaCitas.setVisible(true);
        contenedorTablaCitas.toFront();
        AnimationUtils.fadeIn(contenedorTablaCitas);
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = true;
        panelInformesActivo = false;
        actualizarEstadoBotones();
    }

    @FXML
    void menuInformes(ActionEvent event) {
        contenedorPrincipal.setVisible(false);
        contenedorTablaClientes.setVisible(false);
        contenedorTablaTatuadores.setVisible(false);
        contenedorTablaCitas.setVisible(false);
        contenedorInformes.setVisible(true);
        contenedorInformes.toFront();
        AnimationUtils.fadeIn(contenedorInformes);
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = false;
        panelInformesActivo = true;
        actualizarEstadoBotones();
    }

    @FXML
    void menuInicio(ActionEvent event) {
        contenedorTablaClientes.setVisible(false);
        contenedorTablaTatuadores.setVisible(false);
        contenedorTablaCitas.setVisible(false);
        contenedorInformes.setVisible(false);
        contenedorPrincipal.setVisible(true);
        contenedorPrincipal.toFront();
        AnimationUtils.fadeIn(contenedorPrincipal);
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = false;
        panelInformesActivo = false;
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
            Scene scene = new Scene(root);
            CSSUtils.aplicarEstilos(scene);
            stage.setScene(scene);
            StageUtils.setAppIcon(stage);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir ventana de salida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void menuGuia(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanaGuia.fxml"));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Guía de Usuario");
            Scene scene = new Scene(root);
            CSSUtils.aplicarEstilos(scene);
            stage.setScene(scene);
            StageUtils.setAppIcon(stage);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir ventana de guía: " + e.getMessage());
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
    private Button btnClientesPanel;

    @FXML
    private Button btnTatuadoresPanel;

    @FXML
    private Button btnCitasPanel;

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
            Scene scene = new Scene(root);
            CSSUtils.aplicarEstilos(scene);
            stage.setScene(scene);
            StageUtils.setAppIcon(stage);
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
            Scene scene = new Scene(root);
            CSSUtils.aplicarEstilos(scene);
            stage.setScene(scene);
            StageUtils.setAppIcon(stage);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir ventana " + titulo + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    //BOTONES STACKPANE PRINCIPAL
    @FXML
    void btnClientes(MouseEvent event) {
        contenedorPrincipal.setVisible(false);
        contenedorTablaTatuadores.setVisible(false);
        contenedorTablaCitas.setVisible(false);
        contenedorTablaClientes.setVisible(true);
        contenedorTablaClientes.toFront();
        AnimationUtils.fadeIn(contenedorTablaClientes);
        panelClientesActivo = true;
        panelTatuadoresActivo = false;
        panelCitasActivo = false;
        actualizarEstadoBotones();
    }

    @FXML
    void btnTatuadores(MouseEvent event) {
        contenedorPrincipal.setVisible(false);
        contenedorTablaClientes.setVisible(false);
        contenedorTablaCitas.setVisible(false);
        contenedorTablaTatuadores.setVisible(true);
        contenedorTablaTatuadores.toFront();
        AnimationUtils.fadeIn(contenedorTablaTatuadores);
        panelClientesActivo = false;
        panelTatuadoresActivo = true;
        panelCitasActivo = false;
        actualizarEstadoBotones();
    }

    @FXML
    void btnCitas(MouseEvent event) {
        contenedorPrincipal.setVisible(false);
        contenedorTablaClientes.setVisible(false);
        contenedorTablaTatuadores.setVisible(false);
        contenedorTablaCitas.setVisible(true);
        contenedorTablaCitas.toFront();
        AnimationUtils.fadeIn(contenedorTablaCitas);
        panelClientesActivo = false;
        panelTatuadoresActivo = false;
        panelCitasActivo = true;
        actualizarEstadoBotones();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Aplicar clases de estilo a los contenedores
        aplicarClasesEstilo();

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

        // Configurar visibilidad inicial del contenedor de informes
        contenedorInformes.setVisible(false);

        // Inicializar ComboBox de estados para informes
        comboEstadoInforme.getItems().addAll("Todos", "Pendiente", "Confirmada", "Cancelada", "Completada");
        comboEstadoInforme.setValue("Todos");

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

        // Ocultar todos los contenedores de tablas y mostrar solo la pantalla principal
        contenedorTablaClientes.setVisible(false);
        contenedorTablaTatuadores.setVisible(false);
        contenedorTablaCitas.setVisible(false);
        contenedorPrincipal.setVisible(true);
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

        // Columna de botón para ver imagen
        TableColumn<Cita, Void> colImagen = new TableColumn<>("Foto");
        colImagen.setPrefWidth(80);

        Callback<TableColumn<Cita, Void>, TableCell<Cita, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Cita, Void> call(final TableColumn<Cita, Void> param) {
                final TableCell<Cita, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("Ver");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Cita cita = getTableView().getItems().get(getIndex());
                            mostrarImagen(cita);
                        });
                        btn.getStyleClass().add("button-secondary");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Cita cita = getTableView().getItems().get(getIndex());
                            // Solo mostrar el botón si hay imagen
                            if (cita.getFoto_diseno() != null && cita.getFoto_diseno().length > 0) {
                                setGraphic(btn);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
                return cell;
            }
        };

        colImagen.setCellFactory(cellFactory);

        tableViewCitas.getColumns().addAll(colId, colCliente, colArtista, colFecha, colPrecio, colEstado, colSala, colNotas, colImagen);
        tableViewCitas.setItems(listaCitas);
    }

    private void mostrarImagen(Cita cita) {
        if (cita.getFoto_diseno() == null || cita.getFoto_diseno().length == 0) {
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alerta.setTitle("Sin imagen");
            alerta.setHeaderText(null);
            alerta.setContentText("Esta cita no tiene foto de diseño");
            alerta.showAndWait();
            return;
        }

        try {
            // Crear una ventana modal para mostrar la imagen
            Stage ventanaImagen = new Stage();
            ventanaImagen.setTitle("Foto de Diseño - Cita #" + cita.getId_cita());
            ventanaImagen.initModality(Modality.APPLICATION_MODAL);

            ImageView imageView = new ImageView();
            ImageUtils.mostrarImagenEnImageView(imageView, cita.getFoto_diseno(), 600, 600);

            StackPane root = new StackPane();
            root.getChildren().add(imageView);
            root.setStyle("-fx-padding: 20; -fx-background-color: #2c2c2c;");

            Scene scene = new Scene(root);
            ventanaImagen.setScene(scene);
            StageUtils.setAppIcon(ventanaImagen);
            ventanaImagen.show();
        } catch (Exception e) {
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("Error al mostrar la imagen: " + e.getMessage());
            alerta.showAndWait();
            e.printStackTrace();
        }
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

    private void aplicarClasesEstilo() {
        // Aplicar imagen de fondo al BorderPane principal
        borderPanePrincipal.getStyleClass().add("background-container");

        // Aplicar clases de estilo a los contenedores
        contenedorPrincipal.getStyleClass().add("container-principal");
        contenedorTablaClientes.getStyleClass().add("container-form");
        contenedorTablaTatuadores.getStyleClass().add("container-form");
        contenedorTablaCitas.getStyleClass().add("container-form");

        // Aplicar clases de estilo a los botones de acción
        btnAnadir.getStyleClass().add("button-success");
        btnEditar.getStyleClass().add("button-primary");
        btnBorrar.getStyleClass().add("button-danger");
        btnBuscar.getStyleClass().add("button-secondary");
        btnResetearFiltro.getStyleClass().add("button-secondary");

        // Aplicar clases de estilo a los botones del panel principal
        btnClientesPanel.getStyleClass().add("button-primary");
        btnTatuadoresPanel.getStyleClass().add("button-primary");
        btnCitasPanel.getStyleClass().add("button-primary");
    }

    // ===================== MÉTODOS DE INFORMES =====================

    @FXML
    @SuppressWarnings("unchecked")
    void btnInforme(ActionEvent event) {
        // Obtener el estado seleccionado del ComboBox
        String estadoSeleccionado = comboEstadoInforme.getValue();

        // Configurar el parámetro ESTADO_FILTRO
        parametros.clear();
        parametros.put("ESTADO_FILTRO", estadoSeleccionado);

        if (checkIncrustado.isSelected()) {
            // Informe incrustado en el WebView
            lanzaInforme("/informes/citas_por_sala.jrxml", parametros, 0);
        } else {
            // Informe en ventana nueva
            lanzaInforme("/informes/citas_por_sala.jrxml", parametros, 1);
        }
    }

    @FXML
    void btnInformeGrafico(ActionEvent event) {
        // Informe gráfico sin parámetros (excluye canceladas automáticamente en la query)
        parametros.clear();

        if (checkIncrustado.isSelected()) {
            // Informe incrustado en el WebView
            lanzaInforme("/informes/informe_graficov2.jrxml", parametros, 0);
        } else {
            // Informe en ventana nueva
            lanzaInforme("/informes/informe_graficov2.jrxml", parametros, 1);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void lanzaInforme(String rutaInf, Map param, int tipo) {
        try {
            // Crear carpeta informes si no existe
            File carpetaInformes = new File("informes");
            if (!carpetaInformes.exists()) {
                carpetaInformes.mkdirs();
            }

            // Compilar el informe .jrxml
            JasperReport report = JasperCompileManager.compileReport(getClass().getResourceAsStream(rutaInf));

            try {
                // Obtener conexión
                Connection conexion = DatabaseConnection.getConnection();

                // Llenar el informe con los datos de la conexión
                JasperPrint jasperPrint = JasperFillManager.fillReport(report, param, conexion);

                if (!jasperPrint.getPages().isEmpty()) {
                    // Nombre base del archivo
                    String nombreBase = rutaInf.substring(rutaInf.lastIndexOf('/') + 1, rutaInf.lastIndexOf('.'));

                    // Exportar a PDF
                    String pdfOutputPath = "informes/" + nombreBase + "_informe.pdf";
                    JasperExportManager.exportReportToPdfFile(jasperPrint, pdfOutputPath);

                    // Exportar a HTML
                    String outputHtmlFile = "informes/" + nombreBase + "_informe.html";
                    JasperExportManager.exportReportToHtmlFile(jasperPrint, outputHtmlFile);

                    // Mostrar en WebView
                    if (tipo == 0) {
                        wv.getEngine().load(new File(outputHtmlFile).toURI().toString());
                    } else {
                        WebView wvnuevo = new WebView();
                        wvnuevo.getEngine().load(new File(outputHtmlFile).toURI().toString());
                        StackPane stackPane = new StackPane(wvnuevo);
                        Scene scene = new Scene(stackPane, 900, 600);
                        Stage stage = new Stage();
                        stage.setTitle("Informe de Citas");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setResizable(true);
                        stage.setScene(scene);
                        stage.show();
                    }

                    // Mostrar mensaje de éxito
                    String estadoFiltro = (String) param.get("ESTADO_FILTRO");
                    String mensajeFiltro = (estadoFiltro == null || estadoFiltro.equals("Todos")) ? "todos los estados" : estadoFiltro;
                    System.out.println("Informe generado correctamente para: " + mensajeFiltro);

                } else {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Información");
                    alert.setHeaderText("Sin resultados");
                    alert.setContentText("No se encontraron citas para el estado seleccionado: " + comboEstadoInforme.getValue());
                    alert.showAndWait();
                }

            } catch (JRException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error al generar el informe");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        } catch (JRException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
