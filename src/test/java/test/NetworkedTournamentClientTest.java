package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import client.NetworkedTournamentClient;
import robot.HumanBot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;


public class NetworkedTournamentClientTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    // --- getAction() tests ---

    @Test
    public void testGetActionReturnsCooperateWhenBotIsNull() {
        // Default constructor: bot is null
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        assertEquals("COOPERATE", client.getAction());
    }

    @Test
    public void testGetActionDelegatesToBotWhenSet() {
        // Use the Scanner constructor to avoid reading real stdin
        Scanner fakeConsole = new Scanner(new ByteArrayInputStream("".getBytes()));
        NetworkedTournamentClient client = new NetworkedTournamentClient(fakeConsole);

        // Set a HumanBot that will return DEFECT
        Scanner botInput = new Scanner(new ByteArrayInputStream("DEFECT\n".getBytes()));
        HumanBot bot = new HumanBot("TestPlayer", botInput);
        client.setBot(bot);

        // Now getAction() should delegate to bot.getAction()
        assertEquals("DEFECT", client.getAction());
    }

    @Test
    public void testGetActionDelegatesToBotCooperate() {
        Scanner fakeConsole = new Scanner(new ByteArrayInputStream("".getBytes()));
        NetworkedTournamentClient client = new NetworkedTournamentClient(fakeConsole);

        Scanner botInput = new Scanner(new ByteArrayInputStream("COOPERATE\n".getBytes()));
        HumanBot bot = new HumanBot("TestPlayer", botInput);
        client.setBot(bot);

        assertEquals("COOPERATE", client.getAction());
    }

    // --- Constructor tests ---

    @Test
    public void testDefaultConstructorCreatesClient() {
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        assertNotNull(client);
        // bot is null, so getAction() returns default
        assertEquals("COOPERATE", client.getAction());
    }

    @Test
    public void testScannerConstructorCreatesClient() {
        Scanner fakeConsole = new Scanner(new ByteArrayInputStream("test\n".getBytes()));
        NetworkedTournamentClient client = new NetworkedTournamentClient(fakeConsole);
        assertNotNull(client);
        assertEquals("COOPERATE", client.getAction());
    }
}
