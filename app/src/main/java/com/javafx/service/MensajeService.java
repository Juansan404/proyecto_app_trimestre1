package com.javafx.service;

import com.javafx.model.Mensaje;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class MensajeService {

    private final ApiClient api = ApiClient.getInstance();

    public List<Mensaje> listarMensajes() throws Exception {
        HttpResponse<String> r = api.get("/api/mensajes");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Mensaje.class);
    }

    public List<Mensaje> listarPorSolicitud(Long idSolicitud) throws Exception {
        HttpResponse<String> r = api.get("/api/mensajes/solicitud/" + idSolicitud);
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Mensaje.class);
    }

    public Mensaje crearMensaje(Mensaje m) throws Exception {
        HttpResponse<String> r = api.post("/api/mensajes", JsonUtil.toJson(m));
        if (r.statusCode() != 200 && r.statusCode() != 201)
            throw new RuntimeException("Error " + r.statusCode() + ": " + r.body());
        return JsonUtil.fromJson(r.body(), Mensaje.class);
    }

    public boolean eliminarMensaje(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/mensajes/" + id);
        return r.statusCode() == 204;
    }
}
