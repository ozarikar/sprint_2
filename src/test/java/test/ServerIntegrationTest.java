package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for NetworkedTournamentServer.
 * Uses Java's built-in HttpClient (JDK 17) — no extra Spring test dependencies needed.
 *
 * Spring caches the application context across test classes with identical config,
 * so the server bean (and its in-memory tournaments list) is reused. We call
 * seedTournaments() in @BeforeEach to reset state so tests never poison each other.
 */
@SpringBootTest(
    classes = server.NetworkedTournamentServer.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ServerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private server.NetworkedTournamentServer serverBean;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    public void resetServerState() {
        serverBean.seedTournaments();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    public void testGetTournamentsReturnsList() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url("/tournaments")))
            .GET()
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.startsWith("["), "Expected JSON array, got: " + body);
    }

    @Test
    public void testGetTournamentsHasThreeTournaments() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url("/tournaments")))
            .GET()
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        // 3 tournaments pre-configured: Alpha League, Beta Cup, Gamma Open
        assertTrue(body.contains("Alpha League"), "Missing Alpha League");
        assertTrue(body.contains("Beta Cup"),     "Missing Beta Cup");
        assertTrue(body.contains("Gamma Open"),   "Missing Gamma Open");
        assertTrue(body.contains("\"open\""),     "Missing open field");
    }

    @Test
    public void testRegisterSuccessReturnsTrue() throws Exception {
        String json = "{\"robotName\":\"TestBot\",\"tournamentName\":\"Alpha League\",\"ip\":\"\"}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url("/register")))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("true", response.body());
    }

    @Test
    public void testRegisterToUnknownTournamentReturnsFalse() throws Exception {
        String json = "{\"robotName\":\"TestBot\",\"tournamentName\":\"DoesNotExist\",\"ip\":\"\"}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url("/register")))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("false", response.body());
    }

    @Test
    public void testRegisterWithIpSucceeds() throws Exception {
        String json = "{\"robotName\":\"RemoteBot1\",\"tournamentName\":\"Beta Cup\",\"ip\":\"127.0.0.1:9999\"}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url("/register")))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("true", response.body());
    }
}
