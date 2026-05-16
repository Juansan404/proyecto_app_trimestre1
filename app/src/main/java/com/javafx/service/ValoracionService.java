package com.javafx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.javafx.model.Valoracion;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ValoracionService {

    private final ApiClient api = ApiClient.getInstance();

    public Map<String, Object> getResumen(long idArtista) throws Exception {
        HttpResponse<String> r = api.get("/api/valoraciones/artista/" + idArtista + "/resumen");
        if (r.statusCode() != 200) return Map.of("media", 0.0, "total", 0);
        @SuppressWarnings("unchecked")
        Map<String, Object> m = JsonUtil.fromJson(r.body(), Map.class);
        return m;
    }

    public List<Valoracion> getValoraciones(long idArtista) throws Exception {
        HttpResponse<String> r = api.get("/api/valoraciones/artista/" + idArtista);
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Valoracion.class);
    }

    public boolean eliminar(int idValoracion) throws Exception {
        HttpResponse<String> r = api.delete("/api/valoraciones/" + idValoracion);
        return r.statusCode() == 200 || r.statusCode() == 204;
    }

    /** Formats resumen as "4.5 ★ (12)" or "Sin valoraciones" */
    public static String formatResumen(double media, long total) {
        if (total == 0) return "Sin valoraciones";
        return String.format("%.1f ★ (%d)", media, total);
    }
}
