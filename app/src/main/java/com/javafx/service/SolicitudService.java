package com.javafx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafx.model.Mensaje;
import com.javafx.model.SolicitudCita;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class SolicitudService {

    private final ApiClient api = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<SolicitudCita> listarSolicitudes() throws Exception {
        HttpResponse<String> r = api.get("/api/solicitudes");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), SolicitudCita.class);
    }

    public boolean cambiarEstado(Long id, String nuevoEstado) throws Exception {
        String body = mapper.writeValueAsString(
                mapper.createObjectNode().put("estado", nuevoEstado));
        HttpResponse<String> r = api.put("/api/solicitudes/" + id + "/estado", body);
        return r.statusCode() == 200;
    }

    public SolicitudCita crearSolicitud(SolicitudCita s) throws Exception {
        HttpResponse<String> r = api.post("/api/solicitudes", mapper.writeValueAsString(s));
        if (r.statusCode() != 200 && r.statusCode() != 201)
            throw new RuntimeException("Error " + r.statusCode() + ": " + r.body());
        return JsonUtil.fromJson(r.body(), SolicitudCita.class);
    }

    public boolean actualizarSolicitud(Long id, SolicitudCita s) throws Exception {
        HttpResponse<String> r = api.put("/api/solicitudes/" + id, mapper.writeValueAsString(s));
        return r.statusCode() == 200;
    }

    public boolean eliminarSolicitud(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/solicitudes/" + id);
        return r.statusCode() == 200 || r.statusCode() == 204;
    }

    public List<Mensaje> obtenerMensajes(Long idSolicitud) throws Exception {
        HttpResponse<String> r = api.get("/api/mensajes/solicitud/" + idSolicitud);
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Mensaje.class);
    }
}
