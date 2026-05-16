package com.javafx.utils;

import com.javafx.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class DetalleUtils {

    public static void mostrar(Usuario u) {
        Builder b = new Builder("Usuario", u.getIdUsuario());
        b.fila("ID",                   u.getIdUsuario());
        b.fila("Nombre",               u.getNombreCompleto());
        b.fila("Email",                u.getEmail());
        b.fila("Rol",                  u.getRol());
        b.fila("Estado",               Boolean.TRUE.equals(u.getActivo()) ? "Activo" : "Inactivo");
        b.fila("Publicaciones",        u.getNumeroPublicaciones());
        b.fila("Seguidores",           u.getSeguidores());
        b.fila("Seguidos",             u.getSeguidos());
        b.fila("Bio",                  u.getBio());
        b.fila("Creado",               fecha(u.getCreadoEn()));
        b.imagen("Avatar",             u.getAvatar());
        b.mostrar();
    }

    public static void mostrar(PerfilArtista p) {
        Builder b = new Builder("Artista", p.getIdPerfil());
        b.fila("ID",               p.getIdPerfil());
        b.fila("Artista",          p.getNombreArtista());
        b.fila("Estudio",          p.getNombreEstudio());
        b.fila("Especialidades",   p.getEspecialidades());
        b.fila("Años exp.",        p.getAnosExperiencia());
        b.fila("Instagram",        p.getInstagram());
        b.fila("Precio/hora",      p.getPrecioHora() != null ? String.format("%.2f €/h", p.getPrecioHora()) : null);
        b.fila("Disponible",       Boolean.TRUE.equals(p.getDisponible()) ? "Sí" : "No");
        b.fila("Portfolio URL",    p.getPortfolioUrl());
        b.fila("Seguidores",       p.getSeguidores());
        b.mostrar();
    }

    public static void mostrar(Publicacion p) {
        Builder b = new Builder("Publicación", p.getIdPublicacion());
        b.fila("ID",          p.getIdPublicacion());
        b.fila("Autor",       p.getNombreUsuario());
        b.fila("Estilo",      p.getEstilo());
        b.fila("Zona cuerpo", p.getZonaCuerpo());
        b.fila("Descripción", p.getDescripcion());
        b.fila("Likes",       p.getLikesCount());
        b.fila("Fecha",       fecha(p.getCreadoEn()));
        b.imagen("Foto",      p.getFotoUrl());
        b.mostrar();
    }

    public static void mostrar(Comentario c) {
        Builder b = new Builder("Comentario", c.getIdComentario());
        b.fila("ID",             c.getIdComentario());
        b.fila("Usuario",        c.getNombreUsuario());
        b.fila("Publicación #",  c.getIdPublicacion());
        b.fila("Contenido",      c.getContenido());
        b.fila("Fecha",          fecha(c.getCreadoEn()));
        b.mostrar();
    }

    public static void mostrar(SolicitudCita s) {
        Builder b = new Builder("Solicitud de cita", s.getIdSolicitud());
        b.fila("ID",               s.getIdSolicitud());
        b.fila("Cliente",          s.getNombreCliente());
        b.fila("Artista",          s.getNombreArtista());
        b.fila("Estado",           s.getEstado());
        b.fila("Descripción",      s.getDescripcion());
        b.fila("Zona cuerpo",      s.getZonaCuerpo());
        b.fila("Tamaño",           s.getTamano());
        b.fila("Presupuesto",      s.getPresupuestoAprox() != null ? String.format("%.2f €", s.getPresupuestoAprox()) : null);
        b.fila("Fecha preferida",  fecha(s.getFechaPreferida()));
        b.fila("Notas artista",    s.getNotasArtista());
        b.fila("Creada",           fecha(s.getCreadoEn()));
        b.imagen("Foto ref.",      s.getFotoReferencia());
        b.mostrar();
    }

    public static void mostrar(Estudio e) {
        Builder b = new Builder("Estudio", e.getIdEstudio());
        b.fila("ID",           e.getIdEstudio());
        b.fila("Nombre",       e.getNombre());
        b.fila("Ciudad",       e.getCiudad());
        b.fila("Dirección",    e.getDireccion());
        b.fila("Teléfono",     e.getTelefono());
        b.fila("Email",        e.getEmail());
        b.fila("Web",          e.getWeb());
        b.fila("Instagram",    e.getInstagram());
        b.fila("Localización", e.getLocalizacion());
        b.fila("Descripción",  e.getDescripcion());
        b.fila("Creado",       fecha(e.getCreadoEn()));
        b.imagen("Foto portada", e.getFotoPortada());
        b.mostrar();
    }

    public static void mostrar(Mensaje m) {
        Builder b = new Builder("Mensaje", m.getIdMensaje());
        b.fila("ID",           m.getIdMensaje());
        b.fila("Solicitud #",  m.getIdSolicitud());
        b.fila("Remitente",    m.getNombreRemitente());
        b.fila("Leído",        Boolean.TRUE.equals(m.getLeido()) ? "Sí" : "No");
        b.fila("Contenido",    m.getContenido());
        b.fila("Fecha",        fecha(m.getCreadoEn()));
        b.mostrar();
    }

    public static void mostrar(Cita c) {
        Builder b = new Builder("Cita", c.getIdCita());
        b.fila("ID",           c.getIdCita());
        b.fila("Cliente",      c.getNombreCliente());
        b.fila("Artista",      c.getNombreArtista());
        b.fila("Estado",       c.getEstado());
        b.fila("Fecha",        fecha(c.getFechaCita()));
        b.fila("Hora inicio",  c.getHoraInicio());
        b.fila("Duración",     c.getDuracionAproximada() != null ? c.getDuracionAproximada() + " min" : null);
        b.fila("Precio",       c.getPrecio() != null ? String.format("%.2f €", c.getPrecio()) : null);
        b.fila("Sala",         c.getSala());
        b.fila("Notas",        c.getNotas());
        b.fila("Solicitud #",  c.getIdSolicitud());
        b.fila("Creada",       fecha(c.getCreadoEn()));
        b.imagen("Foto diseño", c.getFotoDiseno());
        b.mostrar();
    }

    // ─────────── Builder interno ───────────

    private static class Builder {
        private final String titulo;
        private final GridPane grid = new GridPane();
        private int row = 0;

        Builder(String tipo, Object id) {
            titulo = tipo + (id != null ? " #" + id : "");
            grid.setHgap(16);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 24, 4, 24));

            ColumnConstraints colLabel = new ColumnConstraints();
            colLabel.setMinWidth(140);
            colLabel.setMaxWidth(140);

            ColumnConstraints colValue = new ColumnConstraints();
            colValue.setHgrow(Priority.ALWAYS);
            colValue.setMinWidth(240);

            grid.getColumnConstraints().addAll(colLabel, colValue);
        }

        void fila(String label, Object valor) {
            if (valor == null) return;
            String texto = valor.toString().strip();
            if (texto.isEmpty()) return;

            Label lbl = new Label(label + ":");
            lbl.setStyle("-fx-font-weight: 700;");
            lbl.setWrapText(true);

            if (texto.length() > 100) {
                TextArea ta = new TextArea(texto);
                ta.setEditable(false);
                ta.setWrapText(true);
                ta.setPrefRowCount(3);
                ta.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(ta, Priority.ALWAYS);
                grid.add(lbl, 0, row);
                grid.add(ta, 1, row);
            } else {
                Label val = new Label(texto);
                val.setWrapText(true);
                val.setMaxWidth(Double.MAX_VALUE);
                grid.add(lbl, 0, row);
                grid.add(val, 1, row);
            }
            row++;
        }

        void imagen(String label, String base64) {
            if (base64 == null || base64.isBlank()) return;
            try {
                String data = base64.contains(",") ? base64.split(",", 2)[1] : base64;
                byte[] bytes = Base64.getDecoder().decode(data);
                Image img = new Image(new ByteArrayInputStream(bytes), 240, 240, true, true);
                if (img.isError()) return;

                ImageView iv = new ImageView(img);
                iv.setFitWidth(240);
                iv.setFitHeight(240);
                iv.setPreserveRatio(true);

                Label lbl = new Label(label + ":");
                lbl.setStyle("-fx-font-weight: 700;");
                grid.add(lbl, 0, row);
                grid.add(iv, 1, row);
                row++;
            } catch (Exception ignored) {}
        }

        void mostrar() {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(titulo);
            dialog.setHeaderText(titulo);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            ScrollPane scroll = new ScrollPane(grid);
            scroll.setFitToWidth(true);
            scroll.setPrefViewportHeight(420);
            scroll.setStyle("-fx-background-color: transparent;");

            dialog.getDialogPane().setContent(scroll);
            dialog.getDialogPane().setPrefWidth(520);
            DialogUtils.estilizar(dialog);
            dialog.showAndWait();
        }
    }

    private static String fecha(String s) {
        if (s == null || s.isBlank()) return null;
        return s.length() >= 16 ? s.substring(0, 16).replace("T", " ") : s;
    }
}
