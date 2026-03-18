package robot;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * A proxy robot that delegates getAction() to a remote client machine via HTTP.
 *
 * When the tournament calls getAction(), RemoteBot makes a GET request to
 * http://{ip}/action on the client's machine. The client's NetworkedTournamentClient
 * serves this endpoint, calling its local bot (e.g. HumanBot) and returning the result.
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
        this.restClient = RestClient.create();
    }

    @Override
    public String getAction() {
        try {
            String action = restClient.get()
                .uri("http://" + ip + "/action")
                .retrieve()
                .body(String.class);
            return (action != null) ? action.trim().toUpperCase() : "COOPERATE";
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
