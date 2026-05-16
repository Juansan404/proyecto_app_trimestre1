package com.javafx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafx.model.Publicacion;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class PublicacionService {

    private final ApiClient    api    = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Publicacion> listarPublicaciones() throws Exception {
        HttpResponse<String> r = api.get("/api/publicaciones");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Publicacion.class);
    }

    /** Returns a page of publications. Result[0] = List<Publicacion>, Result[1] = Integer totalPages */
    public Object[] listarPaginado(int page, int size) throws Exception {
        HttpResponse<String> r = api.get("/api/publicaciones?page=" + page + "&size=" + size);
        if (r.statusCode() != 200) return new Object[]{List.of(), 0};
        var tree = JsonUtil.getMapper().readTree(r.body());
        List<Publicacion> content = JsonUtil.fromJsonList(tree.get("content").toString(), Publicacion.class);
        int totalPages = tree.get("totalPages").asInt(1);
        return new Object[]{content, totalPages};
    }

    public Publicacion crearPublicacion(Publicacion p) throws Exception {
        HttpResponse<String> r = api.post("/api/publicaciones", JsonUtil.toJson(p));
        if (r.statusCode() != 200 && r.statusCode() != 201)
            throw new RuntimeException("Error " + r.statusCode() + ": " + r.body());
        return JsonUtil.fromJson(r.body(), Publicacion.class);
    }

    public boolean actualizarPublicacion(Long id, Map<String, Object> datos) throws Exception {
        HttpResponse<String> r = api.put("/api/publicaciones/" + id, mapper.writeValueAsString(datos));
        return r.statusCode() == 200;
    }

    public boolean eliminarPublicacion(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/publicaciones/" + id);
        return r.statusCode() == 200 || r.statusCode() == 204;
    }

    public List<Publicacion> listarPendientesRevision() throws Exception {
        HttpResponse<String> r = api.get("/api/publicaciones/pendientes-revision");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Publicacion.class);
    }

    public boolean revisionAdmin(Long idPublicacion, boolean mantener, Long idAdmin) throws Exception {
        var node = mapper.createObjectNode()
                .put("mantener", mantener)
                .put("idAdmin", idAdmin);
        HttpResponse<String> r = api.post("/api/publicaciones/" + idPublicacion + "/revision-admin",
                mapper.writeValueAsString(node));
        return r.statusCode() == 200 || r.statusCode() == 204;
    }
}
