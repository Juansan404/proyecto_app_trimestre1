package com.javafx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafx.model.Cita;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class CitaService {

    private final ApiClient api = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Cita> listarCitas() throws Exception {
        HttpResponse<String> r = api.get("/api/citas");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Cita.class);
    }

    public Cita obtenerCita(Long id) throws Exception {
        HttpResponse<String> r = api.get("/api/citas/" + id);
        if (r.statusCode() != 200)
            throw new RuntimeException("Error " + r.statusCode() + " al obtener cita");
        return JsonUtil.fromJson(r.body(), Cita.class);
    }

    public Cita crearCita(Cita cita) throws Exception {
        HttpResponse<String> r = api.post("/api/citas", JsonUtil.toJson(cita));
        if (r.statusCode() != 200 && r.statusCode() != 201)
            throw new RuntimeException("Error " + r.statusCode() + " al crear cita.\n" + r.body());
        return JsonUtil.fromJson(r.body(), Cita.class);
    }

    public boolean actualizarCita(Long id, Cita cita) throws Exception {
        HttpResponse<String> r = api.put("/api/citas/" + id, JsonUtil.toJson(cita));
        return r.statusCode() == 200;
    }

    public boolean eliminarCita(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/citas/" + id);
        return r.statusCode() == 200 || r.statusCode() == 204;
    }

    public boolean cambiarEstado(Long id, String nuevoEstado) throws Exception {
        String body = mapper.writeValueAsString(
                mapper.createObjectNode().put("estado", nuevoEstado));
        HttpResponse<String> r = api.put("/api/citas/" + id + "/estado", body);
        return r.statusCode() == 200;
    }
}
