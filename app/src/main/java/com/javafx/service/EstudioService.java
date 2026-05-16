package com.javafx.service;

import com.javafx.model.Estudio;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class EstudioService {

    private final ApiClient api = ApiClient.getInstance();

    public List<Estudio> listarEstudios() throws Exception {
        HttpResponse<String> r = api.get("/api/estudios");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Estudio.class);
    }

    public Estudio crearEstudio(Estudio estudio) throws Exception {
        HttpResponse<String> r = api.post("/api/estudios", JsonUtil.toJson(estudio));
        if (r.statusCode() != 200 && r.statusCode() != 201)
            throw new RuntimeException("Error " + r.statusCode() + " al crear estudio");
        return JsonUtil.fromJson(r.body(), Estudio.class);
    }

    public boolean actualizarEstudio(Long id, Estudio estudio) throws Exception {
        HttpResponse<String> r = api.put("/api/estudios/" + id, JsonUtil.toJson(estudio));
        return r.statusCode() == 200;
    }

    public boolean eliminarEstudio(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/estudios/" + id);
        return r.statusCode() == 200 || r.statusCode() == 204;
    }
}
