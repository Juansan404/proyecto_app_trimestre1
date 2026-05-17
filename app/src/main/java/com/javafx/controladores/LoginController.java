package com.javafx.controladores;

import com.javafx.service.AuthService;
import com.javafx.utils.CSSUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController {

    // ── Login panel ──
    @FXML private VBox              panelLogin;
    @FXML private TextField         txtLoginEmail;
    @FXML private PasswordField     txtLoginPassword;
    @FXML private TextField         txtLoginPasswordVisible;
    @FXML private Button            btnLoginShowPass;
    @FXML private Button  btnAcceder;
    @FXML private HBox    loginSpinnerBox;
    @FXML private FontIcon icoLoginShowPass;
    @FXML private Label             lblLoginError;

    private boolean loginPassVisible = false;

    // ── Register panel ──
    @FXML private VBox          panelRegistro;
    @FXML private TextField     txtRegNombre;
    @FXML private TextField     txtRegApellidos;
    @FXML private TextField     txtRegEmail;
    @FXML private PasswordField txtRegPassword;
    @FXML private TextField     txtRegTelefono;
    @FXML private Label         lblRegError;

    // ── Tab toggle buttons ──
    @FXML private Button btnTabLogin;
    @FXML private Button btnTabReg;

    private static final String STYLE_TAB_ACTIVE =
        "-fx-background-color: #e94560; -fx-text-fill: white; " +
        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold; " +
        "-fx-padding: 10 0; -fx-background-radius: 10 0 0 10; " +
        "-fx-cursor: hand; -fx-border-color: transparent; -fx-effect: none; " +
        "-fx-min-height: 0; -fx-pref-height: 40;";

    private static final String STYLE_TAB_ACTIVE_RIGHT =
        "-fx-background-color: #e94560; -fx-text-fill: white; " +
        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold; " +
        "-fx-padding: 10 0; -fx-background-radius: 0 10 10 0; " +
        "-fx-cursor: hand; -fx-border-color: transparent; -fx-effect: none; " +
        "-fx-min-height: 0; -fx-pref-height: 40;";

    private static final String STYLE_TAB_INACTIVE_LEFT =
        "-fx-background-color: transparent; -fx-text-fill: #aaaacc; " +
        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; " +
        "-fx-padding: 10 0; -fx-background-radius: 10 0 0 10; " +
        "-fx-cursor: hand; -fx-border-color: transparent; -fx-effect: none; " +
        "-fx-min-height: 0; -fx-pref-height: 40;";

    private static final String STYLE_TAB_INACTIVE_RIGHT =
        "-fx-background-color: transparent; -fx-text-fill: #aaaacc; " +
        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; " +
        "-fx-padding: 10 0; -fx-background-radius: 0 10 10 0; " +
        "-fx-cursor: hand; -fx-border-color: transparent; -fx-effect: none; " +
        "-fx-min-height: 0; -fx-pref-height: 40;";

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        txtLoginEmail.setText("admin@tattooage.es");
        txtLoginPassword.setText("Tattoo2024!");
    }

    // ──────────────────────── TAB TOGGLE ────────────────────────

    @FXML
    private void mostrarLogin() {
        panelLogin.setVisible(true);
        panelLogin.setManaged(true);
        panelRegistro.setVisible(false);
        panelRegistro.setManaged(false);
        btnTabLogin.setStyle(STYLE_TAB_ACTIVE);
        btnTabReg.setStyle(STYLE_TAB_INACTIVE_RIGHT);
    }

    @FXML
    private void mostrarRegistro() {
        panelLogin.setVisible(false);
        panelLogin.setManaged(false);
        panelRegistro.setVisible(true);
        panelRegistro.setManaged(true);
        btnTabLogin.setStyle(STYLE_TAB_INACTIVE_LEFT);
        btnTabReg.setStyle(STYLE_TAB_ACTIVE_RIGHT);
    }

    // ──────────────────────── PASSWORD TOGGLE ────────────────────────

    @FXML
    private void toggleLoginPassword() {
        loginPassVisible = !loginPassVisible;
        if (loginPassVisible) {
            txtLoginPasswordVisible.setText(txtLoginPassword.getText());
            txtLoginPasswordVisible.setVisible(true);
            txtLoginPasswordVisible.setManaged(true);
            txtLoginPassword.setVisible(false);
            txtLoginPassword.setManaged(false);
            icoLoginShowPass.setIconLiteral("fas-eye-slash");
            icoLoginShowPass.setIconColor(Color.web("#e94560"));
        } else {
            txtLoginPassword.setText(txtLoginPasswordVisible.getText());
            txtLoginPassword.setVisible(true);
            txtLoginPassword.setManaged(true);
            txtLoginPasswordVisible.setVisible(false);
            txtLoginPasswordVisible.setManaged(false);
            icoLoginShowPass.setIconLiteral("fas-eye");
            icoLoginShowPass.setIconColor(Color.web("#505580"));
        }
    }

    // ──────────────────────── LOGIN ────────────────────────

    @FXML
    private void btnLogin() {
        String email    = txtLoginEmail.getText().trim();
        String password = loginPassVisible
            ? txtLoginPasswordVisible.getText()
            : txtLoginPassword.getText();

        ocultarMensaje(lblLoginError);

        if (email.isEmpty() || password.isEmpty()) {
            mostrarError(lblLoginError, "Introduce email y contrasena.");
            return;
        }

        setLoginCargando(true);
        new Thread(() -> {
            try {
                boolean ok = authService.login(email, password);
                Platform.runLater(() -> {
                    setLoginCargando(false);
                    if (ok) {
                        abrirPanelPrincipal();
                    } else {
                        mostrarError(lblLoginError, "Email o contrasena incorrectos.");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoginCargando(false);
                    mostrarError(lblLoginError, "Error de conexion: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void abrirPanelPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            CSSUtils.aplicarEstilos(scene);

            Stage stage = (Stage) txtLoginEmail.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TattooAge \u2014 Panel de Administracion");
            stage.setMaximized(true);
        } catch (Exception ex) {
            mostrarError(lblLoginError, "Error al abrir el panel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ──────────────────────── REGISTER ────────────────────────

    @FXML
    private void btnRegistrar() {
        String nombre    = txtRegNombre.getText().trim();
        String apellidos = txtRegApellidos.getText().trim();
        String email     = txtRegEmail.getText().trim();
        String password  = txtRegPassword.getText();
        String telefono  = txtRegTelefono.getText().trim();

        ocultarMensaje(lblRegError);

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            mostrarError(lblRegError, "Nombre, email y contrasena son obligatorios.");
            return;
        }
        if (password.length() < 6) {
            mostrarError(lblRegError, "La contrasena debe tener al menos 6 caracteres.");
            return;
        }

        new Thread(() -> {
            try {
                boolean ok = authService.register(nombre, apellidos, email, password, telefono);
                Platform.runLater(() -> {
                    if (ok) {
                        mostrarOk(lblRegError, "Cuenta creada. Ahora inicia sesion.");
                        mostrarLogin();
                        txtLoginEmail.setText(email);
                        txtLoginPassword.requestFocus();
                    } else {
                        mostrarError(lblRegError, "No se pudo crear la cuenta. El email puede estar en uso.");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                    mostrarError(lblRegError, "Error de conexion: " + ex.getMessage()));
            }
        }).start();
    }

    private void setLoginCargando(boolean cargando) {
        btnAcceder.setDisable(cargando);
        btnAcceder.setText(cargando ? "Conectando..." : "ACCEDER");
        loginSpinnerBox.setVisible(cargando);
        loginSpinnerBox.setManaged(cargando);
        txtLoginEmail.setDisable(cargando);
        txtLoginPassword.setDisable(cargando);
        txtLoginPasswordVisible.setDisable(cargando);
        btnLoginShowPass.setDisable(cargando);
    }

    // ──────────────────────── HELPERS ────────────────────────

    private void mostrarError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px; " +
                     "-fx-background-color: rgba(233,69,96,0.12); " +
                     "-fx-background-radius: 6; -fx-padding: 8 12;");
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void mostrarOk(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px; " +
                     "-fx-background-color: rgba(76,175,80,0.12); " +
                     "-fx-background-radius: 6; -fx-padding: 8 12;");
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void ocultarMensaje(Label lbl) {
        lbl.setVisible(false);
        lbl.setManaged(false);
    }
}
