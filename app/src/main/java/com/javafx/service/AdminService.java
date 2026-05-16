package com.javafx.service;

import com.javafx.model.Usuario;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class AdminService {

    private final ApiClient api = ApiClient.getInstance();

    public List<Usuario> getPendientes() throws Exception {
        HttpResponse<String> r = api.get("/api/admin/artistas/pendientes");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Usuario.class);
    }

    public boolean aprobar(Long idUsuario) throws Exception {
        HttpResponse<String> r = api.put("/api/admin/artistas/" + idUsuario + "/aprobar", "{}");
        return r.statusCode() == 200;
    }

    public boolean rechazar(Long idUsuario) throws Exception {
        HttpResponse<String> r = api.put("/api/admin/artistas/" + idUsuario + "/rechazar", "{}");
        return r.statusCode() == 200;
    }
}
