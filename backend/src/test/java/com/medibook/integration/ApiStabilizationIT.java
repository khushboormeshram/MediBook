package com.medibook.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ApiStabilizationIT {
    private static final String API = System.getProperty("medibook.apiUrl", "http://localhost:8080");
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    @Test void defaultDoctorDirectoryAcceptsNullFiltersAndReturnsAList() throws Exception {
        HttpResponse<String> response = get("/api/doctors", null);
        assertEquals(200, response.statusCode(), response.body());
        assertTrue(json.readTree(response.body()).isArray());
    }

    @Test void doctorSearchAndProfileArePublicAndUsable() throws Exception {
        HttpResponse<String> filtered = get("/api/doctors?specialty=Cardiology&location=Bengaluru", null);
        assertEquals(200, filtered.statusCode(), filtered.body());
        assertTrue(json.readTree(filtered.body()).size() > 0);

        HttpResponse<String> profile = get("/api/doctors/3", null);
        assertEquals(200, profile.statusCode(), profile.body());
        assertEquals("Dr. Meera Iyer", json.readTree(profile.body()).path("name").asText());
    }

    @Test void explicitPaginationIsAConsumablePage() throws Exception {
        HttpResponse<String> response = get("/api/doctors?page=0&size=1", null);
        assertEquals(200, response.statusCode(), response.body());
        JsonNode page = json.readTree(response.body());
        assertTrue(page.path("content").isArray());
        assertEquals(1, page.path("content").size());
    }

    @Test void demoPatientLoginAndDisabledMailHealthCheckWork() throws Exception {
        HttpResponse<String> login = post("/api/auth/login", null,
                "{\"email\":\"patient@medibook.local\",\"password\":\"password\"}");
        assertEquals(200, login.statusCode(), login.body());
        assertFalse(json.readTree(login.body()).path("token").asText().isBlank());

        HttpResponse<String> health = get("/actuator/health", null);
        assertEquals(200, health.statusCode(), health.body());
        assertEquals("UP", json.readTree(health.body()).path("status").asText());
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(API + path)).GET();
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String token, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(API + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
