package com.javafx.service;

import com.javafx.model.Notificacion;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class NotificacionService {

    private final ApiClient api = ApiClient.getInstance();

    /** Todas las notificaciones de un usuario. */
    public List<Notificacion> getByUsuario(Long idUsuario) throws Exception {
        HttpResponse<String> r = api.get("/api/notificaciones/" + idUsuario);
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Notificacion.class);
    }

    /** Marca una notificación como leída. */
    public boolean marcarLeida(Long idNotificacion) throws Exception {
        HttpResponse<String> r = api.put("/api/notificaciones/" + idNotificacion + "/leer", "");
        return r.statusCode() == 200;
    }

    /** Marca todas las notificaciones de un usuario como leídas. */
    public boolean marcarTodasLeidas(Long idUsuario) throws Exception {
        HttpResponse<String> r = api.put("/api/notificaciones/leer-todas/" + idUsuario, "");
        return r.statusCode() == 200;
    }

    /** Cuenta no leídas localmente sin llamada extra al backend. */
    public long countNoLeidas(List<Notificacion> lista) {
        return lista.stream().filter(n -> !Boolean.TRUE.equals(n.getLeido())).count();
    }
}
