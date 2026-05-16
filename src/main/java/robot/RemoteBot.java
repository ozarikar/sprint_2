package robot;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * A proxy robot that delegates getAction() to a remote client machine via HTTP.
 *
 * When the tournament calls getAction(), RemoteBot POSTs the current history
 * snapshot to /action on the client's machine. The client's
 * NetworkedTournamentClient applies that history to its local bot (so
 * strategies like TitForTat can react to the last move) and returns the action.
 *
 * Game and Tournament never deal with networking — RemoteBot looks identical to
 * any other Robot from their perspective.
 */
public class RemoteBot extends Robot {
    private int score;
    private final String ip;
    private final RestClient restClient;

    public RemoteBot(String name, String ip) {
        super(name);
        this.ip = ip;
        this.score = 0;
        // Short timeouts so an unreachable / hung client never blocks the tournament.
        // Without this, RestClient.create() inherits JDK defaults (potentially infinite),
        // which is why tests pointing at a dead port could hang indefinitely.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String getAction() {
        try {
            
            Map<String, List<String>> historySnapshot = getHistory();

            String action = restClient.post()
                .uri("http://" + ip + "/action")
                .contentType(MediaType.APPLICATION_JSON)
                .body(historySnapshot)
                .retrieve()
                .body(String.class);

            if (action == null) {
                return "COOPERATE";
            }
            String normalized = action.trim().toUpperCase();
            // An empty or whitespace-only response is just as broken as null
            // for the tournament's purposes — both used to fall through and
            // return "" to the game loop. Default both to COOPERATE.
            return normalized.isEmpty() ? "COOPERATE" : normalized;
        } catch (RestClientException e) {
            System.err.println("[RemoteBot] Could not reach " + ip + ": " + e.getMessage()
                + " — defaulting to COOPERATE");
            return "COOPERATE";
        }
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public void giveOutcome(String opponentName, String myAction,
                            String opponentAction, int myScore) {
        super.giveOutcome(opponentName, myAction, opponentAction, myScore);
        this.score += myScore;
    }

    public String getIp() {
        return ip;
    }
}
