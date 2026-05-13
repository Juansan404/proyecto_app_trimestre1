package com.javafx.controladores;

import com.javafx.model.Publicacion;
import com.javafx.service.PublicacionService;
import com.javafx.util.SessionManager;
import com.javafx.utils.DialogUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

public class RevisionImagenesController {

    @FXML private FlowPane        flowPane;
    @FXML private Label           lblStatus;
    @FXML private Label           lblContador;
    @FXML private ProgressIndicator loadingSpinner;

    private final PublicacionService service = new PublicacionService();

    @FXML
    private void initialize() {
        cargar();
    }

    @FXML
    private void btnRecargar() {
        cargar();
    }

    private void cargar() {
        lblStatus.setText("Cargando...");
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        flowPane.getChildren().clear();

        Task<List<Publicacion>> task = new Task<>() {
            @Override protected List<Publicacion> call() throws Exception {
                return service.listarPendientesRevision();
            }
        };

        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            List<Publicacion> lista = task.getValue();
            lblContador.setText(lista.isEmpty() ? "Sin imágenes pendientes" : lista.size() + " pendiente(s) de revisión");
            lblStatus.setText(lista.size() + " imágenes pendientes");
            lista.forEach(p -> flowPane.getChildren().add(crearTarjeta(p)));
        });

        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            lblStatus.setText("Error al cargar");
            DialogUtils.mostrarError("Error", task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private VBox crearTarjeta(Publicacion pub) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(220);
        card.setStyle(
            "-fx-background-color: #1a1a2e;" +
            "-fx-border-color: #2d2d4e;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"
        );

        // Imagen
        ImageView imgView = new ImageView();
        imgView.setFitWidth(196);
        imgView.setFitHeight(196);
        imgView.setPreserveRatio(false);
        imgView.setSmooth(true);
        imgView.setStyle("-fx-background-radius: 6;");

        String fotoUrl = pub.getFotoUrl();
        if (fotoUrl != null && fotoUrl.contains(",")) {
            try {
                byte[] bytes = Base64.getDecoder().decode(fotoUrl.split(",", 2)[1]);
                imgView.setImage(new Image(new ByteArrayInputStream(bytes), 196, 196, false, true));
            } catch (Exception ignored) {
                imgView.setImage(null);
            }
        } else if (fotoUrl != null && fotoUrl.startsWith("http")) {
            imgView.setImage(new Image(fotoUrl, 196, 196, false, true, true));
        }

        // Info usuario + fecha
        String usuario = pub.getNombreUsuario();
        String fecha   = pub.getCreadoEn() != null && pub.getCreadoEn().length() >= 10
                         ? pub.getCreadoEn().substring(0, 10) : "";
        Label lblUsuario = new Label(usuario);
        lblUsuario.setStyle("-fx-text-fill: #c8c8e8; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblUsuario.setWrapText(true);
        lblUsuario.setMaxWidth(196);

        Label lblFecha = new Label(fecha);
        lblFecha.setStyle("-fx-text-fill: #6b7094; -fx-font-size: 11px;");

        if (pub.getEstilosStr() != null && !pub.getEstilosStr().isBlank()) {
            Label lblEstilo = new Label(pub.getEstilosStr());
            lblEstilo.setStyle("-fx-text-fill: #e94560; -fx-font-size: 11px;");
            lblEstilo.setMaxWidth(196);
            lblEstilo.setWrapText(true);
            card.getChildren().addAll(imgView, lblUsuario, lblEstilo, lblFecha);
        } else {
            card.getChildren().addAll(imgView, lblUsuario, lblFecha);
        }

        // Botones Aprobar / Rechazar
        Button btnAprobar = new Button("Aprobar");
        btnAprobar.setMaxWidth(Double.MAX_VALUE);
        btnAprobar.setStyle(
            "-fx-background-color: #27ae60; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-font-size: 12px;" +
            "-fx-background-radius: 6; -fx-cursor: hand;"
        );

        Button btnRechazar = new Button("Rechazar");
        btnRechazar.setMaxWidth(Double.MAX_VALUE);
        btnRechazar.setStyle(
            "-fx-background-color: #C0392B; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-font-size: 12px;" +
            "-fx-background-radius: 6; -fx-cursor: hand;"
        );

        btnAprobar.setOnAction(e  -> ejecutarRevision(pub, true,  btnAprobar, btnRechazar, card));
        btnRechazar.setOnAction(e -> ejecutarRevision(pub, false, btnAprobar, btnRechazar, card));

        HBox btnRow = new HBox(8, btnAprobar, btnRechazar);
        btnRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(btnAprobar,  javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(btnRechazar, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().add(btnRow);
        return card;
    }

    private void ejecutarRevision(Publicacion pub, boolean mantener,
                                   Button btnA, Button btnR, VBox card) {
        String accion = mantener ? "aprobar" : "rechazar";
        if (!DialogUtils.confirmar("Confirmar revisión",
                "¿Quieres " + accion + " la publicación de " + pub.getNombreUsuario() + "?")) return;

        btnA.setDisable(true);
        btnR.setDisable(true);

        Long idAdmin = SessionManager.getInstance().getUserId();
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return service.revisionAdmin(pub.getIdPublicacion(), mantener, idAdmin);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                Platform.runLater(() -> flowPane.getChildren().remove(card));
                int remaining = flowPane.getChildren().size();
                Platform.runLater(() -> {
                    lblStatus.setText(remaining + " imágenes pendientes");
                    lblContador.setText(remaining == 0 ? "Sin imágenes pendientes" : remaining + " pendiente(s) de revisión");
                });
            } else {
                btnA.setDisable(false);
                btnR.setDisable(false);
                DialogUtils.mostrarError("Error", "No se pudo completar la revisión.");
            }
        });

        task.setOnFailed(e -> {
            btnA.setDisable(false);
            btnR.setDisable(false);
            DialogUtils.mostrarError("Error", task.getException().getMessage());
        });

        new Thread(task).start();
    }
}
