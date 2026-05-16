package com.javafx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafx.model.Estilo;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;

public class EstiloService {

    private final ApiClient    api    = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Todos los estilos ordenados por popularidad (likes totales). */
    public List<Estilo> listarTop() throws Exception {
        HttpResponse<String> r = api.get("/api/estilos");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Estilo.class);
    }

    /** Estilos preferidos de un artista dado su idUsuario. */
    public List<Estilo> getByArtista(Long idUsuario) throws Exception {
        HttpResponse<String> r = api.get("/api/estilos/artista/" + idUsuario);
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Estilo.class);
    }

    /** Reemplaza los estilos preferidos del artista. */
    public boolean updateArtista(Long idUsuario, List<String> nombres) throws Exception {
        HttpResponse<String> r = api.put("/api/estilos/artista/" + idUsuario,
                mapper.writeValueAsString(nombres));
        return r.statusCode() == 200;
    }
}
