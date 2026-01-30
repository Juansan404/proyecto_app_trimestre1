package com.javafx.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Utilidad para mostrar indicadores de progreso durante operaciones largas.
 * Proporciona un overlay semi-transparente con spinner y mensaje de estado.
 */
public class ProgressUtils {

    private static final String OVERLAY_STYLE = "-fx-background-color: rgba(0, 0, 0, 0.5);";
    private static final String CONTAINER_STYLE = "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 30;";
    private static final String MESSAGE_STYLE = "-fx-font-size: 14px; -fx-text-fill: #333;";

    /**
     * Crea un overlay de progreso con spinner y mensaje.
     *
     * @param mensaje Mensaje a mostrar durante la carga
     * @return VBox conteniendo el spinner y el mensaje
     */
    public static VBox crearOverlayProgreso(String mensaje) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(60, 60);

        Label lblMensaje = new Label(mensaje);
        lblMensaje.setStyle(MESSAGE_STYLE);

        VBox contenedor = new VBox(15);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setStyle(CONTAINER_STYLE);
        contenedor.getChildren().addAll(spinner, lblMensaje);
        contenedor.setMaxSize(250, 150);

        return contenedor;
    }

    /**
     * Crea el panel overlay completo (fondo semi-transparente + contenedor).
     *
     * @param mensaje Mensaje a mostrar durante la carga
     * @return StackPane con el overlay completo
     */
    public static StackPane crearPanelOverlay(String mensaje) {
        VBox contenedor = crearOverlayProgreso(mensaje);

        StackPane overlay = new StackPane();
        overlay.setStyle(OVERLAY_STYLE);
        overlay.getChildren().add(contenedor);
        overlay.setAlignment(Pos.CENTER);

        return overlay;
    }

    /**
     * Ejecuta una tarea en segundo plano mostrando el overlay de progreso.
     *
     * @param <T>           Tipo de resultado de la tarea
     * @param stackPane     StackPane donde se mostrará el overlay
     * @param mensaje       Mensaje a mostrar durante la carga
     * @param tarea         Callable que ejecuta la operación (en hilo secundario)
     * @param onSuccess     Consumer que procesa el resultado (en hilo de UI)
     * @param onError       Consumer que maneja errores (en hilo de UI)
     */
    public static <T> void ejecutarConProgreso(
            StackPane stackPane,
            String mensaje,
            Callable<T> tarea,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        StackPane overlay = crearPanelOverlay(mensaje);

        // Añadir overlay al stackPane
        Platform.runLater(() -> stackPane.getChildren().add(overlay));

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return tarea.call();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                stackPane.getChildren().remove(overlay);
                if (onSuccess != null) {
                    onSuccess.accept(task.getValue());
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                stackPane.getChildren().remove(overlay);
                if (onError != null) {
                    onError.accept(task.getException());
                }
            });
        });

        // Ejecutar en un nuevo hilo
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Ejecuta una tarea en segundo plano sin retorno de valor.
     *
     * @param stackPane     StackPane donde se mostrará el overlay
     * @param mensaje       Mensaje a mostrar durante la carga
     * @param tarea         Runnable que ejecuta la operación
     * @param onSuccess     Runnable que se ejecuta al completar
     * @param onError       Consumer que maneja errores
     */
    public static void ejecutarConProgreso(
            StackPane stackPane,
            String mensaje,
            Runnable tarea,
            Runnable onSuccess,
            Consumer<Throwable> onError) {

        ejecutarConProgreso(
                stackPane,
                mensaje,
                () -> {
                    tarea.run();
                    return null;
                },
                result -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },
                onError);
    }

    /**
     * Muestra un overlay de progreso manualmente.
     *
     * @param stackPane StackPane donde se mostrará el overlay
     * @param mensaje   Mensaje a mostrar
     * @return El overlay creado (para poder eliminarlo después)
     */
    public static StackPane mostrarOverlay(StackPane stackPane, String mensaje) {
        StackPane overlay = crearPanelOverlay(mensaje);
        Platform.runLater(() -> stackPane.getChildren().add(overlay));
        return overlay;
    }

    /**
     * Oculta un overlay de progreso.
     *
     * @param stackPane StackPane que contiene el overlay
     * @param overlay   El overlay a eliminar
     */
    public static void ocultarOverlay(StackPane stackPane, StackPane overlay) {
        Platform.runLater(() -> stackPane.getChildren().remove(overlay));
    }
}
