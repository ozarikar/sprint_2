package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import robot.RemoteBot;

public class RemoteBotTest {

    @Test
    public void testRemoteBotNameAndIp() {
        RemoteBot bot = new RemoteBot("TestRemote", "127.0.0.1:9090");
        assertEquals("TestRemote", bot.getName());
        assertEquals("127.0.0.1:9090", bot.getIp());
        assertEquals(0, bot.getScore());
    }

    @Test
    public void testRemoteBotGiveOutcome() {
        RemoteBot bot = new RemoteBot("TestRemote", "127.0.0.1:9090");
        bot.giveOutcome("Opponent", "COOPERATE", "DEFECT", 0);
        assertEquals(0, bot.getScore());
        bot.giveOutcome("Opponent", "DEFECT", "COOPERATE", 5);
        assertEquals(5, bot.getScore());
    }

    @Test
    public void testRemoteBotGetActionFallsBackOnConnectionError() {
        // Port 1 is privileged and nothing is listening on it, so connect() is
        // refused almost immediately on every major OS. RemoteBot now also uses
        // a 2s connect timeout, so the worst case is still fast.
        RemoteBot bot = new RemoteBot("TestRemote", "127.0.0.1:1");
        String action = bot.getAction();
        // Should default to COOPERATE when it can't reach the server
        assertEquals("COOPERATE", action);
    }

    @Test
    public void testRemoteBotScoreAccumulation() {
        RemoteBot bot = new RemoteBot("TestRemote", "127.0.0.1:9090");
        bot.giveOutcome("Opp", "COOPERATE", "COOPERATE", 3);
        bot.giveOutcome("Opp", "COOPERATE", "COOPERATE", 3);
        assertEquals(6, bot.getScore());
    }
}
