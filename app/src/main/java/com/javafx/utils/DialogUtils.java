package com.javafx.utils;

import com.javafx.util.ThemeManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class DialogUtils {

    private static Image logoImage = null;

    private static Image getLogo() {
        if (logoImage == null) {
            try {
                logoImage = new Image(
                    DialogUtils.class.getResourceAsStream("/images/logo.png"),
                    32, 32, true, true);
            } catch (Exception ignored) {}
        }
        return logoImage;
    }

    /** Aplica el tema activo de la app a cualquier Dialog o Alert */
    public static void estilizar(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(
            DialogUtils.class.getResource("/styles.css").toExternalForm());
        if (!ThemeManager.getInstance().isDarkMode()) {
            pane.getStylesheets().add(
                DialogUtils.class.getResource("/styles-light.css").toExternalForm());
        }
        pane.getStyleClass().add("app-dialog");

        // Icono TattooAge en la barra de título de la ventana del diálogo
        dialog.setOnShowing(e -> {
            Stage stage = (Stage) pane.getScene().getWindow();
            Image logo = getLogo();
            if (logo != null) stage.getIcons().add(logo);
        });

        // Logo como graphic en el header del diálogo
        Image logo = getLogo();
        if (logo != null && dialog.getGraphic() == null) {
            dialog.setGraphic(new ImageView(logo));
        }

        // Clase adicional según tipo de alerta
        if (dialog instanceof Alert alert) {
            switch (alert.getAlertType()) {
                case ERROR       -> pane.getStyleClass().add("error");
                case WARNING     -> pane.getStyleClass().add("warning");
                default          -> {}
            }
        }
    }

    // ────────── Helpers de uso rápido ──────────

    public static void mostrarError(String titulo, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(titulo);
            a.setHeaderText(titulo);
            a.setContentText(msg);
            estilizar(a);
            a.show();
        });
    }

    public static void mostrarAviso(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Aviso");
        a.setHeaderText("Atención");
        a.setContentText(msg);
        estilizar(a);
        a.showAndWait();
    }

    public static boolean confirmar(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(titulo);
        a.setHeaderText(titulo);
        estilizar(a);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }
}
