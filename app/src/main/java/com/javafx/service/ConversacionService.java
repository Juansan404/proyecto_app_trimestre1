package com.javafx.service;

import com.javafx.model.Conversacion;
import com.javafx.model.MensajeDirecto;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ConversacionService {

    private final ApiClient api = ApiClient.getInstance();

    public List<Conversacion> listarPorUsuario(Long idUsuario) throws Exception {
        HttpResponse<String> r = api.get("/api/conversaciones/usuario/" + idUsuario);
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Conversacion.class);
    }

    public List<Conversacion> listarTodas() throws Exception {
        HttpResponse<String> r = api.get("/api/conversaciones");
        if (r.statusCode() == 200) return JsonUtil.fromJsonList(r.body(), Conversacion.class);
        return List.of();
    }

    public List<MensajeDirecto> getMensajes(Long idConversacion) throws Exception {
        HttpResponse<String> r = api.get("/api/conversaciones/" + idConversacion + "/mensajes");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), MensajeDirecto.class);
    }

    public boolean eliminarMensaje(Long idMensaje) throws Exception {
        HttpResponse<String> r = api.delete("/api/mensajes-directos/" + idMensaje);
        return r.statusCode() == 200 || r.statusCode() == 204;
    }
}
