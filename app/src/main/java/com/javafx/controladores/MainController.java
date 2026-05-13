package com.javafx.controladores;

import com.javafx.service.AuthService;
import com.javafx.utils.CSSUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {

    @FXML private BorderPane mainPane;
    @FXML private StackPane  contentPane;
    @FXML private Label      lblSectionTitle;
    @FXML private Button     btnNavUsuarios;
    @FXML private Button     btnNavEstudios;
    @FXML private Button     btnNavArtistas;
    @FXML private Button     btnNavPublicaciones;
    @FXML private Button     btnNavCitas;
    @FXML private Button     btnNavSolicitudes;
    @FXML private Button     btnNavComentarios;
    @FXML private Button     btnNavMensajes;
    @FXML private Button     btnNavMensajesDirectos;
    @FXML private Button     btnNavNotificaciones;
    @FXML private Button     btnNavRevision;
    @FXML private Button     btnNavInformes;
    @FXML private Button     btnNavConfiguracion;

    private Button activeBtn;
    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        navCitas();
    }

    @FXML private void navUsuarios()      { cargar("/fxml/Usuarios.fxml",      "Usuarios",      btnNavUsuarios); }
    @FXML private void navEstudios()      { cargar("/fxml/Estudios.fxml",      "Estudios",      btnNavEstudios); }
    @FXML private void navArtistas()      { cargar("/fxml/Artistas.fxml",      "Artistas",      btnNavArtistas); }
    @FXML private void navPublicaciones() { cargar("/fxml/Publicaciones.fxml", "Publicaciones", btnNavPublicaciones); }
    @FXML private void navCitas()         { cargar("/fxml/Citas.fxml",         "Citas",         btnNavCitas); }
    @FXML private void navSolicitudes()   { cargar("/fxml/Solicitudes.fxml",   "Solicitudes",   btnNavSolicitudes); }
    @FXML private void navComentarios()   { cargar("/fxml/Comentarios.fxml",   "Comentarios",   btnNavComentarios); }
    @FXML private void navMensajes()           { cargar("/fxml/Mensajes.fxml",        "Mensajes",          btnNavMensajes); }
    @FXML private void navMensajesDirectos()  { cargar("/fxml/MensajesDirectos.fxml",  "Mensajes directos", btnNavMensajesDirectos); }
    @FXML private void navNotificaciones()    { cargar("/fxml/Notificaciones.fxml",    "Notificaciones",    btnNavNotificaciones); }
    @FXML private void navRevision()          { cargar("/fxml/RevisionImagenes.fxml",  "Revisión de imágenes", btnNavRevision); }
    @FXML private void navInformes()          { cargar("/fxml/Informes.fxml",          "Informes",          btnNavInformes); }
    @FXML private void navConfiguracion() { cargar("/fxml/Configuracion.fxml", "Configuración",  btnNavConfiguracion); }

    private void cargar(String ruta, String titulo, Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
            Node content = loader.load();
            contentPane.getChildren().setAll(content);
            lblSectionTitle.setText(titulo);

            content.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(220), content);
            ft.setFromValue(0); ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), content);
            tt.setFromX(18); tt.setToX(0);
            new ParallelTransition(ft, tt).play();

            if (activeBtn != null) activeBtn.getStyleClass().remove("sidebar-btn-active");
            btn.getStyleClass().add("sidebar-btn-active");
            activeBtn = btn;
        } catch (Exception e) {
            System.err.println("Error al cargar " + ruta + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void btnLogout() {
        try {
            authService.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            CSSUtils.aplicarEstilos(scene);

            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TattooAge — Login");
            stage.setMaximized(false);
            stage.setWidth(760);
            stage.setHeight(640);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
