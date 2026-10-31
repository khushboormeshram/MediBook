package com.medibook.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/** End-to-end concurrency check against a running API and its PostgreSQL database.
 * It deliberately leaves the winning appointment and two test accounts in the database.
 */
class AppointmentConcurrencyIT {
    private static final String API = System.getProperty("medibook.apiUrl", "http://localhost:8080");
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @Test void onlyOnePatientCanClaimTheSameDoctorSlotAtOnce() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String first = register("race-a-" + suffix + "@medibook.local");
        String second = register("race-b-" + suffix + "@medibook.local");
        JsonNode slot = findSlot();
        String payload = "{\"doctorId\":3,\"startAt\":\"" + slot.path("startAt").asText()
                + "\",\"reason\":\"Concurrent booking regression\"}";

        CompletableFuture<HttpResponse<String>> a = postAsync("/api/appointments", first, payload);
        CompletableFuture<HttpResponse<String>> b = postAsync("/api/appointments", second, payload);
        HttpResponse<String> responseA = a.join();
        HttpResponse<String> responseB = b.join();

        List<Integer> statuses = new ArrayList<>(List.of(responseA.statusCode(), responseB.statusCode()));
        statuses.sort(Integer::compareTo);
        assertEquals(List.of(200, 409), statuses,
                "one transaction must book the slot and the other must receive a conflict");
    }

    private String register(String email) throws Exception {
        HttpResponse<String> response = post("/api/auth/register", null,
                "{\"fullName\":\"Concurrency Test\",\"email\":\"" + email
                        + "\",\"password\":\"RaceTestPass123!\",\"role\":\"PATIENT\"}");
        assertEquals(200, response.statusCode(), response.body());
        return json.readTree(response.body()).path("token").asText();
    }

    private JsonNode findSlot() throws Exception {
        for (int offset = 2; offset < 20; offset++) {
            LocalDate date = LocalDate.now().plusDays(offset);
            HttpResponse<String> response = get("/api/doctors/3/availability?date=" + date, null);
            assertEquals(200, response.statusCode(), response.body());
            JsonNode slots = json.readTree(response.body());
            if (!slots.isEmpty()) return slots.get(0);
        }
        fail("No doctor 3 availability was found in the next 19 days");
        return null;
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(API + path)).GET();
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private CompletableFuture<HttpResponse<String>> postAsync(String path, String token, String body) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(API + path))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String token, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(API + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
