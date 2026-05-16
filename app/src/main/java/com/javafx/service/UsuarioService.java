package com.javafx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafx.model.Usuario;
import com.javafx.util.ApiClient;
import com.javafx.util.JsonUtil;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioService {

    private final ApiClient    api    = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Usuario> listarUsuarios() throws Exception {
        HttpResponse<String> r = api.get("/api/usuarios");
        if (r.statusCode() != 200) return List.of();
        return JsonUtil.fromJsonList(r.body(), Usuario.class);
    }

    public Usuario obtenerUsuario(Long id) throws Exception {
        HttpResponse<String> r = api.get("/api/usuarios/" + id);
        if (r.statusCode() != 200)
            throw new RuntimeException("Error " + r.statusCode() + " al obtener usuario");
        return JsonUtil.fromJson(r.body(), Usuario.class);
    }

    public boolean actualizarUsuario(Long id, Usuario usuario) throws Exception {
        HttpResponse<String> r = api.put("/api/usuarios/" + id, JsonUtil.toJson(usuario));
        return r.statusCode() == 200;
    }

    public boolean crearUsuario(String nombre, String apellidos, String email,
                                String password, String telefono, String rol) throws Exception {
        var node = mapper.createObjectNode()
                .put("nombre", nombre)
                .put("apellidos", apellidos)
                .put("email", email)
                .put("password", password)
                .put("telefono", telefono)
                .put("rol", rol);
        HttpResponse<String> r = api.post("/api/auth/register", mapper.writeValueAsString(node));
        return r.statusCode() == 200 || r.statusCode() == 201;
    }

    public long getNumPublicaciones(Long id) throws Exception {
        HttpResponse<String> r = api.get("/api/publicaciones/count/usuario/" + id);
        if (r.statusCode() != 200) return 0;
        return Long.parseLong(r.body().trim());
    }

    public Map<Integer, long[]> getContadoresBulk() throws Exception {
        HttpResponse<String> r = api.get("/api/usuarios/contadores");
        Map<Integer, long[]> result = new HashMap<>();
        if (r.statusCode() != 200) return result;
        JsonNode root = JsonUtil.getMapper().readTree(r.body());
        root.fields().forEachRemaining(entry -> {
            int id = Integer.parseInt(entry.getKey());
            long seg  = entry.getValue().path("seguidores").asLong(0);
            long sigd = entry.getValue().path("seguidos").asLong(0);
            result.put(id, new long[]{seg, sigd});
        });
        return result;
    }

    public void cargarContadores(Usuario u) {
        try {
            HttpResponse<String> r = api.get("/api/usuarios/" + u.getIdUsuario() + "/contadores");
            if (r.statusCode() != 200) return;
            var node = JsonUtil.getMapper().readTree(r.body());
            u.setSeguidores(node.path("seguidores").asLong(0));
            u.setSeguidos(node.path("seguidos").asLong(0));
        } catch (Exception ignored) {}
    }

    public boolean eliminarUsuario(Long id) throws Exception {
        HttpResponse<String> r = api.delete("/api/usuarios/" + id);
        return r.statusCode() == 200 || r.statusCode() == 204;
    }
}
