package com.javafx.controladores;

import com.javafx.util.AppPreferences;
import com.javafx.util.SessionManager;
import com.javafx.util.ThemeManager;
import com.javafx.util.ThemeManager.ThemeMode;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class ConfiguracionController {

    @FXML private Label     lblAvatar;
    @FXML private Label     lblEmail;
    @FXML private Label     lblRol;

    @FXML private Button    btnDark;
    @FXML private Button    btnSystem;
    @FXML private Button    btnLight;
    @FXML private Label     lblThemeInfo;
    @FXML private Label     lblSystemDetected;

    @FXML private TextField txtPdfDir;

    @FXML
    private void initialize() {
        cargarDatosCuenta();
        actualizarToggle(ThemeManager.getInstance().getMode());
        txtPdfDir.setText(AppPreferences.getInstance().getPdfDirectory());
    }

    // ── Cuenta ──────────────────────────────────────────────────────────

    private void cargarDatosCuenta() {
        SessionManager session = SessionManager.getInstance();
        String email = session.getEmail();
        String rol   = session.getRole();

        lblEmail.setText(email != null ? email : "—");
        lblRol.setText(rol != null ? rol.toUpperCase() : "—");

        if (email != null && !email.isEmpty()) {
            lblAvatar.setText(String.valueOf(Character.toUpperCase(email.charAt(0))));
        }
    }

    // ── Acciones de toggle ───────────────────────────────────────────────

    @FXML
    private void setDarkMode() {
        ThemeManager.getInstance().setMode(ThemeMode.DARK);
        actualizarToggle(ThemeMode.DARK);
    }

    @FXML
    private void setSystemMode() {
        ThemeManager.getInstance().setMode(ThemeMode.SYSTEM);
        actualizarToggle(ThemeMode.SYSTEM);
    }

    @FXML
    private void setLightMode() {
        ThemeManager.getInstance().setMode(ThemeMode.LIGHT);
        actualizarToggle(ThemeMode.LIGHT);
    }

    // ── Directorio PDF ───────────────────────────────────────────────────

    @FXML
    private void seleccionarDirectorioPdf() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta para guardar PDFs");

        // Abrir en el directorio actual si existe
        File actual = new File(AppPreferences.getInstance().getPdfDirectory());
        if (actual.exists() && actual.isDirectory()) dc.setInitialDirectory(actual);

        File seleccionado = dc.showDialog(txtPdfDir.getScene().getWindow());
        if (seleccionado != null) {
            AppPreferences.getInstance().setPdfDirectory(seleccionado.getAbsolutePath());
            txtPdfDir.setText(seleccionado.getAbsolutePath());
        }
    }

    @FXML
    private void resetearDirectorioPdf() {
        AppPreferences.getInstance().resetPdfDirectory();
        txtPdfDir.setText(AppPreferences.getInstance().getPdfDirectory());
    }

    // ── UI interna ───────────────────────────────────────────────────────

    private void actualizarToggle(ThemeMode mode) {
        btnDark.getStyleClass().remove("theme-btn-active");
        btnSystem.getStyleClass().remove("theme-btn-active");
        btnLight.getStyleClass().remove("theme-btn-active");

        switch (mode) {
            case DARK -> {
                btnDark.getStyleClass().add("theme-btn-active");
                lblThemeInfo.setText("Tema activo: Oscuro");
                lblSystemDetected.setText("");
            }
            case SYSTEM -> {
                btnSystem.getStyleClass().add("theme-btn-active");
                lblThemeInfo.setText("Tema activo: Sistema");
                lblSystemDetected.setText("— " + detectarNombreTema());
            }
            case LIGHT -> {
                btnLight.getStyleClass().add("theme-btn-active");
                lblThemeInfo.setText("Tema activo: Claro");
                lblSystemDetected.setText("");
            }
        }
    }

    /** Devuelve el nombre del tema que el SO tiene configurado actualmente. */
    private String detectarNombreTema() {
        try {
            ColorScheme scheme = Platform.getPreferences().getColorScheme();
            if (scheme == ColorScheme.DARK)  return "SO usa tema oscuro";
            if (scheme == ColorScheme.LIGHT) return "SO usa tema claro";
        } catch (Exception ignored) {}
        return "tema del SO no detectado";
    }
}
