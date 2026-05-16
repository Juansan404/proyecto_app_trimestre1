package com.javafx.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_URL = "https://tattooage-backend-293514144387.europe-west1.run.app";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static ApiClient instance;

    private ApiClient() {}

    public static ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    private HttpRequest.Builder baseBuilder(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isEmpty())
            builder.header("Authorization", "Bearer " + token);
        return builder;
    }

    public HttpResponse<String> get(String endpoint) throws Exception {
        return client.send(baseBuilder(endpoint).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> post(String endpoint, String jsonBody) throws Exception {
        return client.send(baseBuilder(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> put(String endpoint, String jsonBody) throws Exception {
        return client.send(baseBuilder(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> delete(String endpoint) throws Exception {
        return client.send(baseBuilder(endpoint).DELETE().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
