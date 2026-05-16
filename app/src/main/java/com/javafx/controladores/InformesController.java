package com.javafx.controladores;

import com.javafx.model.*;
import com.javafx.report.ReportService;
import com.javafx.service.*;
import com.javafx.util.AppPreferences;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InformesController {

    // ── FXML ────────────────────────────────────────────────────────────────
    @FXML private ComboBox<String>  cbTipo;
    @FXML private Button            btnGenerar;
    @FXML private Button            btnGuardar;
    @FXML private Button            btnAbrirExterno;
    @FXML private Label             lblEstado;
    @FXML private ComboBox<String>  cbFiltroEstado;
    @FXML private Label             lblRol;
    @FXML private ComboBox<String>  cbFiltroRol;
    @FXML private TextField         txtBuscar;
    @FXML private HBox              barFechas;
    @FXML private DatePicker        dpDesde;
    @FXML private DatePicker        dpHasta;
    @FXML private StackPane         pdfContainer;
    @FXML private Label             lblPlaceholder;
    @FXML private Label             lblStatus;
    @FXML private ProgressIndicator loadingSpinner;

    // ── Visor PDF (creado programáticamente) ────────────────────────────────
    private ScrollPane  previewScroll;
    private VBox        previewPages;
    private Label       lblPageInfo;

    // ── Estado interno ───────────────────────────────────────────────────────
    private byte[] ultimoPdf;

    // ── Servicios ────────────────────────────────────────────────────────────
    private final CitaService          citaService          = new CitaService();
    private final UsuarioService       usuarioService       = new UsuarioService();
    private final ArtistaService       artistaService       = new ArtistaService();
    private final SolicitudService     solicitudService     = new SolicitudService();
    private final PublicacionService   publicacionService   = new PublicacionService();
    private final ComentarioService    comentarioService    = new ComentarioService();
    private final EstiloService        estiloService        = new EstiloService();
    private final ConversacionService  conversacionService  = new ConversacionService();
    private final ReportService        reportService        = new ReportService();

    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void initialize() {
        // ── Selector de tipo ──────────────────────────────────────────────
        cbTipo.setItems(FXCollections.observableArrayList(
                "Citas", "Usuarios", "Artistas", "Solicitudes", "Publicaciones",
                "Comentarios", "Estilos", "Top Likes", "Conversaciones"));
        cbTipo.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, tipo) -> actualizarFiltros(tipo));

        // ── Construir visor de páginas ────────────────────────────────────
        previewPages = new VBox(12);
        previewPages.setAlignment(Pos.TOP_CENTER);
        previewPages.setPadding(new Insets(16));
        previewPages.setStyle("-fx-background-color: #1a1d30;");

        previewScroll = new ScrollPane(previewPages);
        previewScroll.setFitToWidth(true);
        previewScroll.setStyle(
                "-fx-background-color: #1a1d30;" +
                "-fx-background: #1a1d30;" +
                "-fx-border-color: transparent;");
        previewScroll.setMaxWidth(Double.MAX_VALUE);
        previewScroll.setMaxHeight(Double.MAX_VALUE);

        // Barra de info de página (abajo del todo, dentro del pdfContainer)
        lblPageInfo = new Label();
        lblPageInfo.setStyle("-fx-text-fill: #6b7094; -fx-font-size: 11px;" +
                             "-fx-background-color: rgba(15,18,32,0.85);" +
                             "-fx-padding: 4 12; -fx-background-radius: 4;");
        StackPane.setAlignment(lblPageInfo, javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setMargin(lblPageInfo, new Insets(0, 12, 8, 0));
    }

    // ── Gestión de filtros ───────────────────────────────────────────────────

    private void actualizarFiltros(String tipo) {
        if (tipo == null) return;

        show(lblEstado, false);  show(cbFiltroEstado, false);
        show(lblRol,    false);  show(cbFiltroRol,    false);
        show(barFechas, false);  show(txtBuscar, true);

        cbFiltroEstado.getItems().clear(); cbFiltroEstado.setValue(null);
        cbFiltroRol.getItems().clear();    cbFiltroRol.setValue(null);
        txtBuscar.clear();

        switch (tipo) {
            case "Citas" -> {
                show(lblEstado, true); show(cbFiltroEstado, true);
                cbFiltroEstado.setItems(FXCollections.observableArrayList(
                        "Todos", "Pendiente", "Confirmada", "Cancelada", "Completada"));
                cbFiltroEstado.setValue("Todos");
                show(barFechas, true);
            }
            case "Solicitudes" -> {
                show(lblEstado, true); show(cbFiltroEstado, true);
                cbFiltroEstado.setItems(FXCollections.observableArrayList(
                        "Todos", "Pendiente", "Aceptada", "Rechazada", "Completada"));
                cbFiltroEstado.setValue("Todos");
            }
            case "Usuarios" -> {
                show(lblRol, true); show(cbFiltroRol, true);
                cbFiltroRol.setItems(FXCollections.observableArrayList(
                        "Todos", "ADMIN", "ARTISTA", "CLIENTE"));
                cbFiltroRol.setValue("Todos");
            }
            default -> {}
        }
    }

    // ── Acciones ─────────────────────────────────────────────────────────────

    @FXML
    private void generar() {
        String tipo = cbTipo.getValue();
        if (tipo == null) { lblStatus.setText("Selecciona un tipo de informe."); return; }

        lblStatus.setText("Generando informe...");
        show(loadingSpinner, true);
        btnGenerar.setDisable(true);
        btnGuardar.setDisable(true);
        btnAbrirExterno.setDisable(true);
        previewPages.getChildren().clear();

        Task<byte[]> task = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                return switch (tipo) {
                    case "Citas"          -> generarCitas();
                    case "Usuarios"       -> generarUsuarios();
                    case "Artistas"       -> generarArtistas();
                    case "Solicitudes"    -> generarSolicitudes();
                    case "Publicaciones"  -> generarPublicaciones();
                    case "Comentarios"    -> generarComentarios();
                    case "Estilos"        -> generarEstilos();
                    case "Top Likes"      -> generarTopLikes();
                    case "Conversaciones" -> generarConversaciones();
                    default -> throw new IllegalArgumentException("Tipo: " + tipo);
                };
            }
        };

        task.setOnSucceeded(e -> {
            show(loadingSpinner, false);
            btnGenerar.setDisable(false);
            ultimoPdf = task.getValue();
            renderizarPreview(ultimoPdf);
            btnGuardar.setDisable(false);
            btnAbrirExterno.setDisable(false);
        });

        task.setOnFailed(e -> {
            show(loadingSpinner, false);
            btnGenerar.setDisable(false);
            lblStatus.setText("Error: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    private void guardar() {
        if (ultimoPdf == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar informe PDF");
        fc.setInitialFileName("informe_" + cbTipo.getValue().toLowerCase()
                + "_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));

        File dirConf = new File(AppPreferences.getInstance().getPdfDirectory());
        if (dirConf.exists() && dirConf.isDirectory()) fc.setInitialDirectory(dirConf);

        File dest = fc.showSaveDialog(pdfContainer.getScene().getWindow());
        if (dest == null) return;

        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(ultimoPdf);
            AppPreferences.getInstance().setPdfDirectory(dest.getParent());
            lblStatus.setText("Guardado: " + dest.getAbsolutePath());
        } catch (Exception ex) {
            lblStatus.setText("Error al guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void abrirExterno() {
        if (ultimoPdf == null) return;
        try {
            File temp = Files.createTempFile("tattooage_informe_", ".pdf").toFile();
            temp.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(temp)) {
                fos.write(ultimoPdf);
            }
            Desktop.getDesktop().open(temp);
        } catch (Exception ex) {
            lblStatus.setText("No se pudo abrir el visor: " + ex.getMessage());
        }
    }

    @FXML
    private void limpiarFechas() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
    }

    // ── Renderizado del preview ───────────────────────────────────────────────

    /**
     * Carga el PDF con PDFBox, renderiza cada página como imagen y las muestra
     * apiladas verticalmente en el ScrollPane interno.
     * Toda la parte pesada se hace en un hilo de fondo.
     */
    private void renderizarPreview(byte[] pdfBytes) {
        // Mostrar el visor (puede que sea la primera vez)
        show(lblPlaceholder, false);
        if (!pdfContainer.getChildren().contains(previewScroll)) {
            pdfContainer.getChildren().add(previewScroll);
        }
        if (!pdfContainer.getChildren().contains(lblPageInfo)) {
            pdfContainer.getChildren().add(lblPageInfo);
        }

        lblStatus.setText("Renderizando preview...");

        Task<List<Image>> renderTask = new Task<>() {
            @Override
            protected List<Image> call() throws Exception {
                List<Image> paginas = new ArrayList<>();
                try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
                    PDFRenderer renderer = new PDFRenderer(doc);
                    int total = doc.getNumberOfPages();
                    for (int i = 0; i < total; i++) {
                        BufferedImage bi = renderer.renderImageWithDPI(i, 144);
                        paginas.add(SwingFXUtils.toFXImage(bi, null));
                    }
                }
                return paginas;
            }
        };

        renderTask.setOnSucceeded(e -> {
            List<Image> paginas = renderTask.getValue();
            previewPages.getChildren().clear();

            for (Image img : paginas) {
                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                // Ancho adaptativo: ocupa el scroll menos el padding
                iv.fitWidthProperty().bind(
                        previewScroll.widthProperty().subtract(48));
                iv.setStyle(
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 16, 0, 0, 4);");
                previewPages.getChildren().add(iv);
            }

            int n = paginas.size();
            lblPageInfo.setText(n + " página" + (n != 1 ? "s" : ""));
            lblStatus.setText(n + " página" + (n != 1 ? "s" : "") + " — listo");
        });

        renderTask.setOnFailed(e -> {
            // Si PDFBox no está disponible o el render falla, mostrar aviso sin romper la app
            lblStatus.setText("Preview no disponible — usa 'Guardar PDF' para verlo.");
            lblPageInfo.setText("Sin preview");
            previewPages.getChildren().clear();
            Label aviso = new Label("No se pudo renderizar la previsualización.\n" +
                    "Usa 'Guardar PDF' y ábrelo con tu visor.");
            aviso.setStyle("-fx-text-fill: #6b7094; -fx-font-size: 13px;" +
                           "-fx-text-alignment: center; -fx-wrap-text: true;");
            aviso.setMaxWidth(400);
            previewPages.getChildren().add(aviso);
        });

        new Thread(renderTask).start();
    }

    // ── Generadores por tipo ──────────────────────────────────────────────────

    private byte[] generarCitas() throws Exception {
        List<Cita> lista = citaService.listarCitas();

        String estado = cbFiltroEstado.getValue();
        if (estado != null && !estado.equals("Todos"))
            lista = lista.stream().filter(c -> estado.equals(c.getEstado())).collect(Collectors.toList());

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (desde != null) { String s = desde.toString(); lista = lista.stream().filter(c -> c.getFechaCita() != null && c.getFechaCita().compareTo(s) >= 0).collect(Collectors.toList()); }
        if (hasta != null) { String s = hasta.toString(); lista = lista.stream().filter(c -> c.getFechaCita() != null && c.getFechaCita().compareTo(s) <= 0).collect(Collectors.toList()); }

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(c ->
                c.getNombreCliente().toLowerCase().contains(q)
                || c.getNombreArtista().toLowerCase().contains(q)
                || safe(c.getSala()).contains(q)).collect(Collectors.toList());

        // ── Filtros activos ───────────────────────────────────────────────
        List<String> filtrosActivos = new ArrayList<>();
        if (estado != null && !estado.equals("Todos"))  filtrosActivos.add("Estado: " + estado);
        if (desde != null)                              filtrosActivos.add("Desde: " + desde);
        if (hasta != null)                              filtrosActivos.add("Hasta: " + hasta);
        if (!q.isEmpty())                               filtrosActivos.add("Búsqueda: \"" + txtBuscar.getText().trim() + "\"");
        String filtros = String.join("   ·   ", filtrosActivos);

        List<String> et = List.of("ID", "Cliente", "Artista", "Fecha", "Hora", "Duración", "Precio €", "Estado", "Sala");
        List<String> ca = List.of("id", "cliente", "artista", "fecha", "hora", "duracion", "precio", "estado", "sala");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Cita c : lista) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",       str(c.getIdCita()));
            r.put("cliente",  c.getNombreCliente());
            r.put("artista",  c.getNombreArtista());
            r.put("fecha",    s(c.getFechaCita()));
            r.put("hora",     s(c.getHoraInicio()));
            r.put("duracion", c.getDuracionAproximada() != null ? c.getDuracionAproximada() + " min" : "");
            r.put("precio",   c.getPrecio() != null ? String.format("%.2f", c.getPrecio()) : "");
            r.put("estado",   s(c.getEstado()));
            r.put("sala",     s(c.getSala()));
            rows.add(r);
        }
        setStatus(rows.size(), "citas");
        return reportService.generar("Informe de Citas", filtros, et, ca, rows);
    }

    private byte[] generarUsuarios() throws Exception {
        List<Usuario> lista = usuarioService.listarUsuarios();

        String rol = cbFiltroRol.getValue();
        if (rol != null && !rol.equals("Todos"))
            lista = lista.stream().filter(u -> rol.equals(u.getRol())).collect(Collectors.toList());

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(u ->
                u.getNombreCompleto().toLowerCase().contains(q)
                || safe(u.getEmail()).contains(q)).collect(Collectors.toList());

        // ── Filtros activos ───────────────────────────────────────────────
        List<String> filtrosActivos = new ArrayList<>();
        if (rol != null && !rol.equals("Todos"))    filtrosActivos.add("Rol: " + rol);
        if (!q.isEmpty())                           filtrosActivos.add("Búsqueda: \"" + txtBuscar.getText().trim() + "\"");
        String filtros = String.join("   ·   ", filtrosActivos);

        List<String> et = List.of("ID", "Nombre", "Apellidos", "Email", "Rol", "Activo", "Registro");
        List<String> ca = List.of("id", "nombre", "apellidos", "email", "rol", "activo", "registro");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Usuario u : lista) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",        str(u.getIdUsuario()));
            r.put("nombre",    s(u.getNombre()));
            r.put("apellidos", s(u.getApellidos()));
            r.put("email",     s(u.getEmail()));
            r.put("rol",       s(u.getRol()));
            r.put("activo",    Boolean.TRUE.equals(u.getActivo()) ? "Sí" : "No");
            r.put("registro",  fecha(u.getCreadoEn()));
            rows.add(r);
        }
        setStatus(rows.size(), "usuarios");
        return reportService.generar("Informe de Usuarios", filtros, et, ca, rows);
    }

    private byte[] generarArtistas() throws Exception {
        List<PerfilArtista> lista = artistaService.listarArtistas();

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(a ->
                a.getNombreArtista().toLowerCase().contains(q)
                || safe(a.getEspecialidades()).contains(q)
                || safe(a.getInstagram()).contains(q)).collect(Collectors.toList());

        // ── Filtros activos ───────────────────────────────────────────────
        String filtros = q.isEmpty() ? "" : "Búsqueda: \"" + txtBuscar.getText().trim() + "\"";

        List<String> et = List.of("Artista", "Estudio", "Especialidades", "Años Exp.", "Instagram", "€/hora", "Disponible");
        List<String> ca = List.of("artista", "estudio", "especialidades", "anos", "instagram", "precio", "disponible");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (PerfilArtista a : lista) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("artista",        a.getNombreArtista());
            r.put("estudio",        a.getNombreEstudio());
            r.put("especialidades", s(a.getEspecialidades()));
            r.put("anos",           a.getAnosExperiencia() != null ? a.getAnosExperiencia().toString() : "");
            r.put("instagram",      s(a.getInstagram()));
            r.put("precio",         a.getPrecioHora() != null ? String.format("%.2f", a.getPrecioHora()) : "");
            r.put("disponible",     Boolean.TRUE.equals(a.getDisponible()) ? "Sí" : "No");
            rows.add(r);
        }
        setStatus(rows.size(), "artistas");
        return reportService.generar("Informe de Artistas", filtros, et, ca, rows);
    }

    private byte[] generarSolicitudes() throws Exception {
        List<SolicitudCita> lista = solicitudService.listarSolicitudes();

        String estado = cbFiltroEstado.getValue();
        if (estado != null && !estado.equals("Todos"))
            lista = lista.stream().filter(sol -> estado.equals(sol.getEstado())).collect(Collectors.toList());

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(sol ->
                sol.getNombreCliente().toLowerCase().contains(q)
                || sol.getNombreArtista().toLowerCase().contains(q)
                || safe(sol.getZonaCuerpo()).contains(q)).collect(Collectors.toList());

        // ── Filtros activos ───────────────────────────────────────────────
        List<String> filtrosActivos = new ArrayList<>();
        if (estado != null && !estado.equals("Todos"))  filtrosActivos.add("Estado: " + estado);
        if (!q.isEmpty())                               filtrosActivos.add("Búsqueda: \"" + txtBuscar.getText().trim() + "\"");
        String filtros = String.join("   ·   ", filtrosActivos);

        List<String> et = List.of("ID", "Cliente", "Artista", "Zona", "Tamaño", "Presupuesto €", "Estado", "Fecha");
        List<String> ca = List.of("id", "cliente", "artista", "zona", "tamano", "presupuesto", "estado", "fecha");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (SolicitudCita sol : lista) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",          str(sol.getIdSolicitud()));
            r.put("cliente",     sol.getNombreCliente());
            r.put("artista",     sol.getNombreArtista());
            r.put("zona",        s(sol.getZonaCuerpo()));
            r.put("tamano",      s(sol.getTamano()));
            r.put("presupuesto", sol.getPresupuestoAprox() != null ? String.format("%.2f", sol.getPresupuestoAprox()) : "");
            r.put("estado",      s(sol.getEstado()));
            r.put("fecha",       fecha(sol.getCreadoEn()));
            rows.add(r);
        }
        setStatus(rows.size(), "solicitudes");
        return reportService.generar("Informe de Solicitudes", filtros, et, ca, rows);
    }

    private byte[] generarPublicaciones() throws Exception {
        List<Publicacion> lista = publicacionService.listarPublicaciones();

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(p ->
                p.getNombreUsuario().toLowerCase().contains(q)
                || safe(p.getEstilo()).contains(q)
                || safe(p.getZonaCuerpo()).contains(q)).collect(Collectors.toList());

        // ── Filtros activos ───────────────────────────────────────────────
        String filtros = q.isEmpty() ? "" : "Búsqueda: \"" + txtBuscar.getText().trim() + "\"";

        List<String> et = List.of("ID", "Usuario", "Estilo", "Zona cuerpo", "Likes", "Fecha");
        List<String> ca = List.of("id", "usuario", "estilo", "zona", "likes", "fecha");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Publicacion p : lista) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",      str(p.getIdPublicacion()));
            r.put("usuario", p.getNombreUsuario());
            r.put("estilo",  s(p.getEstilo()));
            r.put("zona",    s(p.getZonaCuerpo()));
            r.put("likes",   p.getLikesCount() != null ? p.getLikesCount().toString() : "0");
            r.put("fecha",   fecha(p.getCreadoEn()));
            rows.add(r);
        }
        setStatus(rows.size(), "publicaciones");
        return reportService.generar("Informe de Publicaciones", filtros, et, ca, rows);
    }

    private byte[] generarComentarios() throws Exception {
        List<com.javafx.model.Comentario> lista = comentarioService.listarComentarios();

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(c ->
                c.getNombreUsuario().toLowerCase().contains(q)
                || safe(c.getContenido()).contains(q)).collect(Collectors.toList());

        String filtros = q.isEmpty() ? "" : "Búsqueda: \"" + txtBuscar.getText().trim() + "\"";

        List<String> et = List.of("ID", "Pub. ID", "Autor", "Contenido", "Fecha");
        List<String> ca = List.of("id", "pubId", "autor", "contenido", "fecha");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (com.javafx.model.Comentario c : lista) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",       str(c.getIdComentario()));
            r.put("pubId",    str(c.getIdPublicacion()));
            r.put("autor",    c.getNombreUsuario());
            String cont = c.getContenido() != null ? c.getContenido() : "";
            r.put("contenido", cont.length() > 80 ? cont.substring(0, 80) + "…" : cont);
            r.put("fecha",    fecha(c.getCreadoEn()));
            rows.add(r);
        }
        setStatus(rows.size(), "comentarios");
        return reportService.generar("Informe de Comentarios", filtros, et, ca, rows);
    }

    private byte[] generarEstilos() throws Exception {
        List<com.javafx.model.Estilo> lista = estiloService.listarTop();

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream()
                .filter(e -> safe(e.getNombre()).contains(q)).collect(Collectors.toList());

        String filtros = q.isEmpty() ? "" : "Búsqueda: \"" + txtBuscar.getText().trim() + "\"";

        List<String> et = List.of("Ranking", "Estilo");
        List<String> ca = List.of("ranking", "nombre");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("ranking", String.valueOf(i + 1));
            r.put("nombre",  s(lista.get(i).getNombre()));
            rows.add(r);
        }
        setStatus(rows.size(), "estilos");
        return reportService.generar("Catálogo de Estilos (por popularidad)", filtros, et, ca, rows);
    }

    private byte[] generarTopLikes() throws Exception {
        List<Publicacion> lista = publicacionService.listarPublicaciones();
        lista = lista.stream()
                .sorted(Comparator.comparingInt(p -> -(p.getLikesCount() != null ? p.getLikesCount() : 0)))
                .collect(Collectors.toList());

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(p ->
                p.getNombreUsuario().toLowerCase().contains(q)
                || safe(p.getEstilosStr()).contains(q)
                || safe(p.getZonaCuerpo()).contains(q)).collect(Collectors.toList());

        String filtros = q.isEmpty() ? "" : "Búsqueda: \"" + txtBuscar.getText().trim() + "\"";

        List<String> et = List.of("Pos.", "ID", "Artista", "Estilos", "Zona", "Likes", "Fecha");
        List<String> ca = List.of("pos", "id", "artista", "estilos", "zona", "likes", "fecha");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            Publicacion p = lista.get(i);
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("pos",     String.valueOf(i + 1));
            r.put("id",      str(p.getIdPublicacion()));
            r.put("artista", p.getNombreUsuario());
            r.put("estilos", p.getEstilosStr());
            r.put("zona",    s(p.getZonaCuerpo()));
            r.put("likes",   p.getLikesCount() != null ? p.getLikesCount().toString() : "0");
            r.put("fecha",   fecha(p.getCreadoEn()));
            rows.add(r);
        }
        setStatus(rows.size(), "publicaciones");
        return reportService.generar("Ranking de Publicaciones por Likes", filtros, et, ca, rows);
    }

    private byte[] generarConversaciones() throws Exception {
        List<com.javafx.model.Conversacion> lista = conversacionService.listarTodas();

        String q = txtBuscar.getText().trim().toLowerCase();
        if (!q.isEmpty()) lista = lista.stream().filter(c ->
                c.getNombresParticipantes().toLowerCase().contains(q)).collect(Collectors.toList());

        String filtros = q.isEmpty() ? "" : "Búsqueda: \"" + txtBuscar.getText().trim() + "\"";

        List<String> et = List.of("ID", "Participantes", "Fecha inicio");
        List<String> ca = List.of("id", "participantes", "fecha");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (com.javafx.model.Conversacion c : lista) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",            str(c.getIdConversacion()));
            r.put("participantes", c.getNombresParticipantes());
            r.put("fecha",         fecha(c.getCreadoEn()));
            rows.add(r);
        }
        setStatus(rows.size(), "conversaciones");
        return reportService.generar("Informe de Conversaciones directas", filtros, et, ca, rows);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void show(javafx.scene.Node n, boolean v) {
        n.setVisible(v); n.setManaged(v);
    }

    private void setStatus(int total, String tipo) {
        Platform.runLater(() ->
                lblStatus.setText(total + " " + tipo + " incluidos en el informe."));
    }

    private String s(String v)    { return v != null ? v : ""; }
    private String str(Object v)  { return v != null ? v.toString() : ""; }
    private String safe(String v) { return v != null ? v.toLowerCase() : ""; }
    private String fecha(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(0, 10);
    }
}
