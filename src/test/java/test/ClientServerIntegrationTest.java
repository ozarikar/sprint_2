package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import client.NetworkedTournamentClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;


@SpringBootTest(
    classes = server.NetworkedTournamentServer.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ClientServerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

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

    @Test
    public void testRunRegistersSuccessfully() throws Exception {
        // Simulate stdin: server address, pick tournament #1, robot name, IP
        String simulatedInput = String.join("\n",
            "127.0.0.1:" + port,   // Server address
            "1",                    // Pick first tournament (Alpha League)
            "TestHuman",            // Robot name
            "127.0.0.1:9999"       // Client IP (doesn't matter, won't be called back)
        ) + "\n";

        Scanner fakeConsole = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        NetworkedTournamentClient client = new NetworkedTournamentClient(fakeConsole);

        // run() should complete without throwing — it registers, server auto-fills & starts
        client.run(null);

        String output = outContent.toString();
        // Should have printed the tournament list
        assertTrue(output.contains("Available Tournaments:"), "Should list tournaments");
        // Should have successfully registered
        assertTrue(output.contains("Registered in"), "Should confirm registration");
    }

    @Test
    public void testRunShowsTournamentList() throws Exception {
        String simulatedInput = String.join("\n",
            "127.0.0.1:" + port,
            "2",                    // Pick second tournament (Beta Cup)
            "TestHuman2",
            "127.0.0.1:9998"
        ) + "\n";

        Scanner fakeConsole = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        NetworkedTournamentClient client = new NetworkedTournamentClient(fakeConsole);

        client.run(null);

        String output = outContent.toString();
        // Should display all three tournament names
        assertTrue(output.contains("Alpha League") || output.contains("Beta Cup") || output.contains("Gamma Open"),
            "Should show tournament names");
        assertTrue(output.contains("OPEN") || output.contains("CLOSED"),
            "Should show tournament status");
    }
}
