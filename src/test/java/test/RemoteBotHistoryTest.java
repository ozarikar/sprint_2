package test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import robot.RemoteBot;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug-fix regression tests for {@link RemoteBot#getAction()}.
 *
 * Pre-fix behavior: RemoteBot did a bodyless GET /action, so the remote bot
 * never saw the opponent's history and any history-aware strategy hosted on a
 * client machine behaved as if every round were the first. This file pins down
 * the corrected wire contract — POST /action with a JSON body matching
 * {@code Robot.getHistory()} — by standing up a real loopback HTTP server,
 * capturing what RemoteBot sends, and checking response normalisation.
 *
 * Uses the JDK's built-in {@link HttpServer}; no new dependencies.
 */
public class RemoteBotHistoryTest {

    /** Captured request data from the stub server, populated per-request. */
    private static final class CapturedRequest {
        String method;
        String path;
        String contentType;
        String body;
    }

    private HttpServer server;
    private int port;
    private CapturedRequest captured;
    /** Response the stub should write back. Tests mutate this before calling getAction(). */
    private final AtomicReference<String> responseBody = new AtomicReference<>("COOPERATE");
    private final AtomicReference<Integer> responseStatus = new AtomicReference<>(200);

    @BeforeEach
    public void startStubServer() throws Exception {
        captured = new CapturedRequest();

        // Port 0 -> let the OS allocate a free port; avoids flaky port collisions
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();

        server.createContext("/action", (HttpExchange ex) -> {
            captured.method = ex.getRequestMethod();
            captured.path = ex.getRequestURI().getPath();
            captured.contentType = ex.getRequestHeaders().getFirst("Content-Type");

            // Drain request body
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ex.getRequestBody().transferTo(baos);
            captured.body = baos.toString(StandardCharsets.UTF_8);

            byte[] respBytes = responseBody.get().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "text/plain;charset=UTF-8");
            ex.sendResponseHeaders(responseStatus.get(), respBytes.length);
            ex.getResponseBody().write(respBytes);
            ex.close();
        });
        server.start();
    }

    @AfterEach
    public void stopStubServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private String ip() {
        return "127.0.0.1:" + port;
    }

    // -----------------------------------------------------------------
    // The core bug-fix assertions: wire shape must be POST /action +
    // JSON body containing the bot's history snapshot.
    // -----------------------------------------------------------------

    @Test
    public void getActionUsesPostNotGet() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        responseBody.set("COOPERATE");

        bot.getAction();

        // Pre-fix: was "GET". This is the regression assertion that locks the
        // new contract in place.
        assertEquals("POST", captured.method,
            "RemoteBot must POST so it can include a history body; was: " + captured.method);
        assertEquals("/action", captured.path);
    }

    @Test
    public void getActionSendsJsonContentType() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        responseBody.set("COOPERATE");

        bot.getAction();

        assertNotNull(captured.contentType, "Content-Type header must be set");
        assertTrue(captured.contentType.toLowerCase().contains("application/json"),
            "Expected application/json content type, got: " + captured.contentType);
    }

    @Test
    public void getActionSendsEmptyHistoryOnFirstCall() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        responseBody.set("COOPERATE");

        bot.getAction();

        // No giveOutcome has been called yet, so history must serialise as an
        // empty JSON object. This is what proves the body is a history map and
        // not, say, hard-coded null.
        assertEquals("{}", captured.body.trim(),
            "Brand-new RemoteBot should POST an empty history map, got: " + captured.body);
    }

    @Test
    public void getActionSendsAccumulatedHistory() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        // Simulate two rounds the server-side tournament already ran for us.
        bot.giveOutcome("Rival", "COOPERATE", "DEFECT", 0);
        bot.giveOutcome("Rival", "DEFECT",   "DEFECT", 1);
        responseBody.set("DEFECT");

        bot.getAction();

        // The Robot's history format is "myAction,oppAction,myScore" per entry,
        // grouped by opponent name. We assert structural fragments rather than
        // exact JSON to stay tolerant of map ordering / whitespace.
        assertTrue(captured.body.contains("\"Rival\""),
            "History body must include opponent name; got: " + captured.body);
        assertTrue(captured.body.contains("COOPERATE,DEFECT,0"),
            "History body must include round-1 entry; got: " + captured.body);
        assertTrue(captured.body.contains("DEFECT,DEFECT,1"),
            "History body must include round-2 entry; got: " + captured.body);
    }

    // -----------------------------------------------------------------
    // Response handling: normalisation + empty-body fallback. The
    // empty-body case is a regression — the old code returned "" to the
    // tournament when the server replied with whitespace.
    // -----------------------------------------------------------------

    @Test
    public void getActionReturnsResponseUppercaseTrimmed() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        responseBody.set("  defect\n");

        String result = bot.getAction();

        assertEquals("DEFECT", result,
            "Response must be trimmed and upper-cased before reaching the tournament");
    }

    @Test
    public void getActionDefaultsToCooperateOnEmptyBody() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        responseBody.set("");

        String result = bot.getAction();

        // Pre-fix: returned "" which broke the game scoring logic. Post-fix:
        // we treat an empty/whitespace response as identical to a missing one.
        assertEquals("COOPERATE", result,
            "Empty server response must fall back to COOPERATE, got: " + result);
    }

    @Test
    public void getActionDefaultsToCooperateOnWhitespaceOnlyBody() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        responseBody.set("   \n\t  ");

        String result = bot.getAction();

        assertEquals("COOPERATE", result,
            "Whitespace-only server response must fall back to COOPERATE, got: " + result);
    }

    @Test
    public void getActionDefaultsToCooperateOnServerError() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        responseStatus.set(500);
        responseBody.set("boom");

        String result = bot.getAction();

        // RestClient.retrieve() raises on 5xx -> caught -> COOPERATE.
        assertEquals("COOPERATE", result);
    }

    // -----------------------------------------------------------------
    // History snapshot independence: mutating the bot after the call
    // must not change what was already sent (sanity check on the
    // serialisation order).
    // -----------------------------------------------------------------

    @Test
    public void historyMapReturnedFromGetHistoryIsADefensiveCopy() {
        RemoteBot bot = new RemoteBot("Tester", ip());
        bot.giveOutcome("Rival", "COOPERATE", "COOPERATE", 3);

        Map<String, List<String>> snapshot = bot.getHistory();
        snapshot.clear(); // attempt to corrupt internal state

        responseBody.set("COOPERATE");
        bot.getAction();

        // If getHistory() returned the internal reference, clearing it above
        // would have made the wire body "{}" — the assertion below would fail.
        assertTrue(captured.body.contains("Rival"),
            "Robot.getHistory() must return a defensive copy; body was: " + captured.body);
    }
}
