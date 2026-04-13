package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import client.NetworkedTournamentClient;


public class NetworkedTournamentClientTest {

    @Test
    public void testGetActionReturnsCooperateWhenBotIsNull() {
        // Before run() is called, bot is null, so getAction() defaults to "COOPERATE"
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        assertEquals("COOPERATE", client.getAction());
    }

    @Test
    public void testGetActionCalledMultipleTimes() {
        // Should consistently return "COOPERATE" when no bot is set
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        assertEquals("COOPERATE", client.getAction());
        assertEquals("COOPERATE", client.getAction());
        assertEquals("COOPERATE", client.getAction());
    }
}
