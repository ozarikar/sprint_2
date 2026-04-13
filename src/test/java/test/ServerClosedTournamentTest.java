package test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(
    classes = server.NetworkedTournamentServer.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ServerClosedTournamentTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    public void testRegisterToClosedTournamentReturnsFalse() throws Exception {
        // First registration — fills the tournament (maxPlayers=3, adds 1 human + 2 autofill bots)
        String json1 = "{\"robotName\":\"First\",\"tournamentName\":\"Gamma Open\",\"ip\":\"\"}";
        HttpRequest req1 = HttpRequest.newBuilder()
            .uri(URI.create(url("/register")))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json1))
            .build();
        HttpResponse<String> firstResponse = http.send(req1, HttpResponse.BodyHandlers.ofString());
        // register() fills remaining spots synchronously before returning,
        // so the tournament is already closed at this point — no sleep needed.
        assertEquals("true", firstResponse.body());

        // Second registration — tournament is now closed
        String json2 = "{\"robotName\":\"Second\",\"tournamentName\":\"Gamma Open\",\"ip\":\"\"}";
        HttpRequest req2 = HttpRequest.newBuilder()
            .uri(URI.create(url("/register")))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json2))
            .build();
        HttpResponse<String> response = http.send(req2, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("false", response.body());
    }
}
