package com.javafx.service;

import com.javafx.model.PerfilArtista;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ArtistaService {

    private final ApiClient api = ApiClient.getInstance();

    public List<PerfilArtista> listarArtistas() throws Exception {
        HttpResponse<String> r = api.get("/api/artistas");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), PerfilArtista.class);
    }

    public PerfilArtista obtenerArtista(Long id) throws Exception {
        HttpResponse<String> r = api.get("/api/artistas/" + id);
        if (r.statusCode() != 200)
            throw new RuntimeException("Error " + r.statusCode() + " al obtener artista");
        return JsonUtil.fromJson(r.body(), PerfilArtista.class);
    }

    public boolean crearPerfil(Long idUsuario, PerfilArtista perfil) throws Exception {
        HttpResponse<String> r = api.post("/api/artistas/" + idUsuario + "/perfil",
                JsonUtil.toJson(perfil));
        return r.statusCode() == 200 || r.statusCode() == 201;
    }

    public boolean actualizarPerfil(Long idUsuario, Map<String, Object> datos) throws Exception {
        HttpResponse<String> r = api.put("/api/artistas/" + idUsuario + "/perfil",
                JsonUtil.toJson(datos));
        return r.statusCode() == 200;
    }
}
