package com.javafx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafx.util.ApiClient;
import com.javafx.util.SessionManager;

import java.net.http.HttpResponse;

public class AuthService {

    private final ApiClient api = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean login(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(
                mapper.createObjectNode()
                        .put("email", email)
                        .put("password", password));

        HttpResponse<String> response = api.post("/api/auth/login", body);

        if (response.statusCode() == 200) {
            JsonNode json = mapper.readTree(response.body());
            SessionManager sm = SessionManager.getInstance();
            sm.setToken(json.path("token").asText());
            sm.setUserId(json.path("idUsuario").asLong());
            sm.setEmail(json.path("email").asText());
            sm.setRole(json.path("rol").asText());
            return true;
        }
        return false;
    }

    public boolean register(String nombre, String apellidos, String email, String password, String telefono) throws Exception {
        var node = mapper.createObjectNode()
                .put("nombre", nombre)
                .put("apellidos", apellidos)
                .put("email", email)
                .put("password", password)
                .put("telefono", telefono);

        String body = mapper.writeValueAsString(node);
        HttpResponse<String> response = api.post("/api/auth/register", body);
        return response.statusCode() == 200 || response.statusCode() == 201;
    }

    public void logout() {
        SessionManager.getInstance().clearSession();
    }
}
