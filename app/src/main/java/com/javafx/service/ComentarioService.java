package com.javafx.service;

import com.javafx.model.Comentario;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class ComentarioService {

    private final ApiClient api = ApiClient.getInstance();

    public List<Comentario> listarComentarios() throws Exception {
        HttpResponse<String> r = api.get("/api/comentarios");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Comentario.class);
    }

    public List<Comentario> listarPorPublicacion(Long idPublicacion) throws Exception {
        HttpResponse<String> r = api.get("/api/comentarios/publicacion/" + idPublicacion);
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Comentario.class);
    }

    public Comentario crearComentario(Comentario c) throws Exception {
        HttpResponse<String> r = api.post("/api/comentarios", JsonUtil.toJson(c));
        if (r.statusCode() != 200 && r.statusCode() != 201)
            throw new RuntimeException("Error " + r.statusCode() + ": " + r.body());
        return JsonUtil.fromJson(r.body(), Comentario.class);
    }

    public boolean eliminarComentario(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/comentarios/" + id);
        return r.statusCode() == 204;
    }
}
